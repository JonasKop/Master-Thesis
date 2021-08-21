#!/bin/bash

set -e

# Get ingress lb ip
kubectl wait deployment -n ingress-nginx ingress-nginx-controller --for=condition=available --timeout=600s

helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

DOMAIN=$(kubectl get service -n ingress-nginx ingress-nginx-controller -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')
IPS_RAW="$(dig +short "$DOMAIN" @resolver1.opendns.com)"
until [ "$IPS_RAW" != "" ]; do
  echo "Cannot find DNS yet, trying again in 3s"
  IPS_RAW=$(dig +short "$DOMAIN" @resolver1.opendns.com | xargs)
  sleep 3
done
echo $IPS_RAW

IPS="$(echo $IPS_RAW | xargs)"

createHelmIndexedArgument() {
  arr=($3)
  return=""
  for ((i = 0; i < ${#arr[@]}; ++i)); do
    return="$return $1[$i]$2${arr[$i]}$4"
  done
  echo $return
}

grafana_hosts=$(createHelmIndexedArgument "--set grafana.ingress.hosts" "=grafana." "$IPS" ".nip.io")
cbtt_hosts=$(createHelmIndexedArgument "--set ingress.hosts" "=cbtt." "$IPS" ".nip.io")

GRAFANA_HOST="grafana.$(echo $IPS | awk '{print $1;}').nip.io"
CBTT_HOST="cbtt.$(echo $IPS | awk '{print $1;}').nip.io"

helm upgrade \
  --install \
  --create-namespace \
  -n cbtt \
  cbtt \
  ./cbtt \
  --set registry.url="$REGISTRY_URL" \
  --set registry.prefix="$REGISTRY_PREFIX" \
  --set registry.username="$REGISTRY_USERNAME" \
  --set registry.password="$REGISTRY_PASSWORD" \
  --set client.username="$CLIENT_USERNAME" \
  --set client.password="$CLIENT_PASSWORD" \
  --set client.image="$SUITE_CLIENT_IMAGE" \
  --set server.image="$SUITE_SERVER_IMAGE" \
  --set grafanaUrl="$GRAFANA_HOST" \
  $cbtt_hosts \
  --set-file config="/tmp/config.yaml"

kubectl rollout restart deploy -n cbtt cbtt

kubectl create namespace monitoring >/dev/null 2>&1 || true
kubectl apply -n monitoring -f grafana-configMap.yaml

helm delete -n monitoring prometheus-grafana >/dev/null 2>&1 || true

helm upgrade \
  --install \
  --create-namespace \
  -n monitoring \
  prometheus-grafana \
  prometheus-community/kube-prometheus-stack \
  --set grafana.ingress.enabled=true \
  $grafana_hosts \
  --set grafana.adminPassword=$GRAFANA_PASSWORD \
  --set prometheus.prometheusSpec.scrapeInterval=1s

cat <<EOF
You can reinstall prometheus by running the following command:

helm delete -n monitoring prometheus-grafana && helm upgrade \
  --install \
  --create-namespace \
  -n monitoring \
  prometheus-grafana \
  prometheus-community/kube-prometheus-stack \
  --set grafana.ingress.enabled=true \
  $grafana_hosts \
  --set grafana.adminPassword=$GRAFANA_PASSWORD \
  --set prometheus.prometheusSpec.scrapeInterval=1s
EOF

echo "You can visit grafana on https://$GRAFANA_HOST"
echo "You can visit cbtt on https://$CBTT_HOST"
