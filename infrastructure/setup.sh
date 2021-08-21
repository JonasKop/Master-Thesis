#!/bin/bash

set -e

mkdir -p ~/.aws
cat <<EOF >>~/.aws/credentials
[default]
aws_access_key_id = ${AWS_ACCESS_KEY_ID}
aws_secret_access_key = ${AWS_SECRET_ACCESS_KEY}
EOF

# Setup terraform
terraform init

if [ "$1" == "--destroy" ]; then
  terraform destroy --auto-approve
  exit 0
fi

terraform apply --auto-approve

mkdir -p ~/.kube
terraform show -json | jq -r '.values.outputs.kubeconfig.value' >~/.kube/config
chmod 600 ~/.kube/config

helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update

helm upgrade \
  --install \
  --create-namespace \
  -n ingress-nginx \
  ingress-nginx \
  ingress-nginx/ingress-nginx
