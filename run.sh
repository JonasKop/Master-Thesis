#!/bin/bash

# Cleanup the test
cleanup() {
    docker stop setup-infra >/dev/null 2>&1 || true && docker rm setup-infra >/dev/null 2>&1 || true
    docker stop setup-kube >/dev/null 2>&1 || true && docker rm setup-kube >/dev/null 2>&1 || true
}

if [ "$1" == "--destroy" ]; then
    docker run \
        --name setup-infra \
        --env-file .env \
        -v "$(pwd)"/infrastructure:/app \
        -w /app \
        setup \
        ./setup.sh --destroy

    cleanup
    exit 0
fi

set -e

# Read .env file
if test -f .env; then
    export $(sed 's/#.*//' .env | xargs)
else
    echo "Missing .env file"
    exit 1
fi

cleanup

# Build setup image
docker build -t setup .

# Create infrastructure
docker run \
    --name setup-infra \
    --env-file .env \
    -v "$(pwd)"/infrastructure:/app \
    -w /app \
    setup \
    ./setup.sh

# Copy kubeconfig
mkdir -p ~/.kube
docker cp setup-infra:/root/.kube/config ~/.kube/config
chmod 600 ~/.kube/config

# Build and push test-suite images
docker build -t $SUITE_CLIENT_IMAGE test-suite/client
docker build -t $SUITE_SERVER_IMAGE test-suite/server
docker push $SUITE_SERVER_IMAGE
docker push $SUITE_CLIENT_IMAGE

# Install the applications to the cluster
docker run \
    -v "$(pwd)/test-suite/server/config.yaml:/tmp/config.yaml" \
    -v "$HOME/.kube/config:/root/.kube/config" \
    --name setup-kube \
    --env-file .env \
    -v "$(pwd)"/kube:/app \
    -w /app \
    setup \
    ./setup.sh

cleanup
