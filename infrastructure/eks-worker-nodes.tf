#
# EKS Worker Nodes Resources
#  * IAM role allowing Kubernetes actions to access other AWS services
#  * EKS Node Group to launch worker nodes
#

resource "aws_iam_role" "cbtt-node" {
  name = "terraform-eks-cbtt-node"

  assume_role_policy = <<POLICY
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "ec2.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
POLICY
}

resource "aws_iam_role_policy_attachment" "cbtt-node-AmazonEKSWorkerNodePolicy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy"
  role       = aws_iam_role.cbtt-node.name
}

resource "aws_iam_role_policy_attachment" "cbtt-node-AmazonEKS_CNI_Policy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy"
  role       = aws_iam_role.cbtt-node.name
}

resource "aws_iam_role_policy_attachment" "cbtt-node-AmazonEC2ContainerRegistryReadOnly" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
  role       = aws_iam_role.cbtt-node.name
}

resource "aws_eks_node_group" "cbtt" {
  cluster_name    = aws_eks_cluster.cbtt.name
  node_group_name = "cbtt"
  node_role_arn   = aws_iam_role.cbtt-node.arn
  subnet_ids      = aws_subnet.cbtt[*].id

  scaling_config {
    desired_size = 2
    max_size     = 2
    min_size     = 1
  }

  depends_on = [
    aws_iam_role_policy_attachment.cbtt-node-AmazonEKSWorkerNodePolicy,
    aws_iam_role_policy_attachment.cbtt-node-AmazonEKS_CNI_Policy,
    aws_iam_role_policy_attachment.cbtt-node-AmazonEC2ContainerRegistryReadOnly,
  ]
}
