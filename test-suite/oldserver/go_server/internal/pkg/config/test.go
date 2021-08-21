package config

import (
	"context"
	"fmt"
	"github.com/pkg/errors"
	batchv1 "k8s.io/api/batch/v1"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"
	"strings"
)

type Test struct {
	Repo Repository `yaml:"repo"`
	Tool BuildTool  `yaml:"tool"`
}

func (t Test) genMetadata() metav1.ObjectMeta {
	meta := metav1.ObjectMeta{
		Name:        fmt.Sprintf("cbtt-test-%s-%s", t.Tool.Name, t.Repo.Name),
		Annotations: t.Tool.SecurityContext.SecurityAnnotations,
	}
	return meta
}

func (t Test) genSpec(config Config) batchv1.JobSpec {
	vol := corev1.Volume{
		Name: fmt.Sprintf("cbtt-repo-%s", t.Repo.Name),
		VolumeSource: corev1.VolumeSource{
			PersistentVolumeClaim: &corev1.PersistentVolumeClaimVolumeSource{
				ClaimName: fmt.Sprintf("cbtt-repo-%s", t.Repo.Name),
			},
		},
	}

	backoffLimit := int32(1)

	spec := batchv1.JobSpec{
		Template: corev1.PodTemplateSpec{
			Spec: corev1.PodSpec{
				Containers:    t.genContainers(config),
				Volumes:       []corev1.Volume{vol},
				RestartPolicy: corev1.RestartPolicyAlways,
			},
		},
		BackoffLimit: &backoffLimit,
	}
	return spec
}

func (t Test) genContainers(config Config) []corev1.Container {
	volMount := corev1.VolumeMount{
		MountPath: config.Workdir,
		Name:      fmt.Sprintf("cbtt-repo-%s", t.Repo.Name),
		ReadOnly:  false,
	}

	userID := int64(t.Tool.SecurityContext.UserID)

	ctx := corev1.SecurityContext{
		Privileged: &t.Tool.SecurityContext.Privileged,
		RunAsUser:  &userID,
	}

	containers := []corev1.Container{{
		Name:            "test",
		Image:           t.Tool.Image,
		Command:         t.genCommand(config),
		VolumeMounts:    []corev1.VolumeMount{volMount},
		SecurityContext: &ctx,
	}}
	return containers
}

func (t Test) genCommand(config Config) []string {
	commands := make([]string, 0)
	for _, cmd := range t.Tool.Command {
		cmd = strings.ReplaceAll(cmd, "${name}", t.Tool.Name)
		cmd = strings.ReplaceAll(cmd, "${workdir}", config.Workdir)
		commands = append(commands, cmd)
	}
	return commands
}

func (t Test) Create(config Config) batchv1.Job {
	job := batchv1.Job{
		ObjectMeta: t.genMetadata(),
		Spec:       t.genSpec(config),
	}
	return job
}

type K8sTest struct {
	Job       *batchv1.Job
	Namespace string
	Clientset *kubernetes.Clientset
	Ctx       *context.Context
}

func (k K8sTest) Apply() error {
	client := k.Clientset.BatchV1().Jobs(k.Namespace)
	_, err := client.Create(*k.Ctx, k.Job, metav1.CreateOptions{})
	if err != nil {
		return errors.Wrap(err, "applying test")
	}
	return nil
}
