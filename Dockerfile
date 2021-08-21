#############
# Arguments #
#############
ARG UBUNTU_VERSION=20.04

###############
# Final image #
###############
FROM ubuntu:$UBUNTU_VERSION as setup

# Define versions
ENV TERRAFORM_VERSION=0.14.7 \
    HELM_VERSION=v3.5.2 \
    HELMFILE_VERSION=v0.138.4 \
    KUBECTL_VERSION=v1.20.4 \
    BIN_DIR=/usr/local/bin \
    AWS_VERSION=2.0.30 \
    AWS_IAM_AUTHENTICATOR_VERSION=1.19.6 \
    DEBIAN_FRONTEND=noninteractive

RUN apt-get update \
    && apt-get upgrade -y \
    && apt-get install -y curl unzip git jq dnsutils \ 
    # Install terraform
    && curl https://releases.hashicorp.com/terraform/${TERRAFORM_VERSION}/terraform_${TERRAFORM_VERSION}_linux_amd64.zip -Lo terraform.zip \
    && unzip terraform.zip \
    && mv terraform $BIN_DIR \
    # Install helm
    && curl https://get.helm.sh/helm-${HELM_VERSION}-linux-amd64.tar.gz -Lo helm.tar.gz \
    && tar -xf helm.tar.gz \ 
    && mv linux-amd64/helm $BIN_DIR \
    # Install kubectl
    && curl https://dl.k8s.io/release/${KUBECTL_VERSION}/bin/linux/amd64/kubectl -Lo ${BIN_DIR}/kubectl \
    && chmod +x ${BIN_DIR}/kubectl \
    # Install aws-iam-authenticator
    && curl https://amazon-eks.s3.us-west-2.amazonaws.com/${AWS_IAM_AUTHENTICATOR_VERSION}/2021-01-05/bin/linux/amd64/aws-iam-authenticator -Lo ${BIN_DIR}/aws-iam-authenticator \
    && chmod +x ${BIN_DIR}/aws-iam-authenticator \
    # Install aws-cli
    && curl https://awscli.amazonaws.com/awscli-exe-linux-x86_64-${AWS_VERSION}.zip -Lo awscliv2.zip \
    && unzip awscliv2.zip \
    && ./aws/install
