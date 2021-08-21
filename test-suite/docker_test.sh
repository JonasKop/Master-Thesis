#!/bin/bash

set -e

if test -f ../.env; then
  export $(cat ../.env | xargs)
else
  echo "Missing .env file"
  exit 1
fi

docker build -t cbtt .
docker run \
  -v "$(pwd)/server/config.yaml:/config.yaml" \
  -v "$HOME/.kube/config:/root/.kube/config" \
  -v "$HOME/.aws:/root/.aws" \
  -e REGISTRY_URL=$REGISTRY_URL \
  -e REGISTRY_PREFIX=$REGISTRY_PREFIX \
  -e REGISTRY_USERNAME=$REGISTRY_USERNAME \
  -e REGISTRY_PASSWORD=$REGISTRY_PASSWORD \
  -e CONFIG_FILE=/config.yaml \
  -e GRAFANA_URL=https://grafana.com \
  -p 8080:8080 \
  -it cbtt sh -c "wget -O /bin/aws-iam-authenticator https://amazon-eks.s3.us-west-2.amazonaws.com/1.19.6/2021-01-05/bin/linux/amd64/aws-iam-authenticator && chmod +x /bin/aws-iam-authenticator && java -jar -Djava.security.egd=file:/dev/./urandom ./app.jar"
