package config

import (
	"context"
	"fmt"
	"github.com/JonasKop/Master-Thesis.git/internal/pkg/lib"
	"github.com/pkg/errors"
	batchv1 "k8s.io/api/batch/v1"
	corev1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/resource"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"
	"log"
	"strings"
)

type Repository struct {
	Name string `yaml:"name"`
	URL  string `yaml:"url"`
	Dir  string `yaml:"dir"`
}

func (repo Repository) genMetadata() metav1.ObjectMeta {
	checksum, err := lib.GenChecksum(repo)
	if err != nil {
		log.Panic(err)
	}
	annotations := make(map[string]string)
	annotations["checksum"] = checksum
	meta := metav1.ObjectMeta{
		Name:        fmt.Sprintf("cbtt-repo-%s", repo.Name),
		Annotations: annotations,
	}
	return meta
}

func (repo Repository) createPVCSpec() corev1.PersistentVolumeClaimSpec {
	rl := make(map[corev1.ResourceName]resource.Quantity)
	rl["storage"] = resource.MustParse("3Gi")

	spec := corev1.PersistentVolumeClaimSpec{
		AccessModes: []corev1.PersistentVolumeAccessMode{corev1.ReadWriteOnce},
		Resources: corev1.ResourceRequirements{
			Requests: rl,
		},
	}
	return spec
}

func (repo Repository) CreatePVC() *corev1.PersistentVolumeClaim {
	pvc := &corev1.PersistentVolumeClaim{
		ObjectMeta: repo.genMetadata(),
		Spec:       repo.createPVCSpec(),
	}
	return pvc
}

func (repo Repository) genJobSpec(config Config) batchv1.JobSpec {
	vol := corev1.Volume{
		Name: fmt.Sprintf("cbtt-repo-%s", repo.Name),
		VolumeSource: corev1.VolumeSource{
			PersistentVolumeClaim: &corev1.PersistentVolumeClaimVolumeSource{
				ClaimName: fmt.Sprintf("cbtt-repo-%s", repo.Name),
			},
		},
	}

	backoffLimit := int32(1)

	spec := batchv1.JobSpec{
		Template: corev1.PodTemplateSpec{
			Spec: corev1.PodSpec{
				Containers:    repo.genJobContainers(config),
				Volumes:       []corev1.Volume{vol},
				RestartPolicy: corev1.RestartPolicyAlways,
			},
		},
		BackoffLimit: &backoffLimit,
	}
	return spec
}

func (repo Repository) genJobContainers(config Config) []corev1.Container {
	volMount := corev1.VolumeMount{
		MountPath: config.Workdir,
		Name:      fmt.Sprintf("cbtt-repo-%s", repo.Name),
		ReadOnly:  false,
	}

	containers := []corev1.Container{{
		Name:         "repo",
		Image:        "alpine:3.13.2",
		Command:      repo.genJobCommand(config),
		VolumeMounts: []corev1.VolumeMount{volMount},
	}}
	return containers
}

func (repo Repository) genJobCommand(config Config) []string {
	cmds := []string{
		"apk add git rsync",
		fmt.Sprintf("git clone %s repo", repo.URL),
		fmt.Sprintf("cd repo && rsync -a %s/ %s/", repo.Dir, config.Workdir),
	}
	initLine := strings.Join(cmds, " && ")

	return []string{"sh", "-c", initLine}
}

func (repo Repository) CreateJob(config Config) *batchv1.Job {
	job := &batchv1.Job{
		ObjectMeta: repo.genMetadata(),
		Spec:       repo.genJobSpec(config),
	}
	return job
}

type K8sRepo struct {
	Job       *batchv1.Job
	PVC       *corev1.PersistentVolumeClaim
	Namespace string
	Clientset *kubernetes.Clientset
	Ctx       *context.Context
}

func (k K8sRepo) Apply() error {
	batchClient := k.Clientset.BatchV1().Jobs(k.Namespace)
	_, err := batchClient.Create(*k.Ctx, k.Job, metav1.CreateOptions{})
	if err != nil {
		return errors.Wrap(err, "applying repo job")
	}

	coreClient := k.Clientset.CoreV1().PersistentVolumeClaims(k.Namespace)
	_, err = coreClient.Create(*k.Ctx, k.PVC, metav1.CreateOptions{})
	if err != nil {
		return errors.Wrap(err, "applying repo pvc")
	}
	return nil
}

func getRepoPVCs(clientset *kubernetes.Clientset, namespace string, ctx context.Context) ([]corev1.PersistentVolumeClaim, error) {
	client := clientset.CoreV1().PersistentVolumeClaims(namespace)
	pvcs, err := client.List(ctx, metav1.ListOptions{})
	if err != nil {
		return nil, errors.Wrap(err, "listing repo pvcs")
	}
	repoClaims := make([]corev1.PersistentVolumeClaim, 0)
	for _, v := range pvcs.Items {
		if strings.HasPrefix(v.Name, "cbtt-repo-") {
			repoClaims = append(repoClaims, v)
		}
	}
	return repoClaims, err
}

func getRepoJobs(clientset *kubernetes.Clientset, namespace string, ctx context.Context) ([]batchv1.Job, error) {
	client := clientset.BatchV1().Jobs(namespace)
	pvcs, err := client.List(ctx, metav1.ListOptions{})
	if err != nil {
		return nil, errors.Wrap(err, "listing repo jobs")
	}
	jobClaims := make([]batchv1.Job, 0)
	for _, v := range pvcs.Items {
		if strings.HasPrefix(v.Name, "cbtt-repo-") {
			jobClaims = append(jobClaims, v)
		}
	}
	return jobClaims, err
}

func deleteRepo(clientset *kubernetes.Clientset, namespace, name string, ctx context.Context) error {
	batchClient := clientset.BatchV1().Jobs(namespace)
	err := batchClient.Delete(ctx, name, metav1.DeleteOptions{})
	if err != nil {
		return errors.Wrap(err, "deleting repo job")
	}

	coreClient := clientset.CoreV1().PersistentVolumeClaims(namespace)
	err = coreClient.Delete(ctx, name, metav1.DeleteOptions{})
	if err != nil {
		return errors.Wrap(err, "deleting repo pvc")
	}
	return err
}

func createRepo(repo Repository) error {
	k := K8sRepo{
		Job:       repo.CreateJob(config),
		PVC:       repo.CreatePVC(),
		Namespace: namespace,
		Clientset: clientset,
		Ctx:       ctx,
	}
	if err := k.Apply(); err != nil {
		return errors.Wrap(err, "applying repo")
	}
	return nil
}

func SyncRepos(clientset *kubernetes.Clientset, namespace string, ctx context.Context) error {
	pvcs, err := getRepoPVCs(clientset, namespace, ctx)
	if err != nil {
		return errors.Wrap(err, "synchronizing repos and getting pvcs")
	}
	jobs, err := getRepoJobs(clientset, namespace, ctx)
	if err != nil {
		return errors.Wrap(err, "synchronizing repos and getting jobs")
	}
}

func nameAndChecksum(o metav1.ObjectMeta) {

}
