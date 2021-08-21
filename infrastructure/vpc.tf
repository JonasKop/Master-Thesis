#
# VPC Resources
#  * VPC
#  * Subnets
#  * Internet Gateway
#  * Route Table
#

resource "aws_vpc" "cbtt" {
  cidr_block = "10.0.0.0/16"

  tags = tomap({
    "Name"                                      = "terraform-eks-cbtt-node",
    "kubernetes.io/cluster/${var.cluster-name}" = "shared",
  })
}

resource "aws_subnet" "cbtt" {
  count = 2

  availability_zone       = data.aws_availability_zones.available.names[count.index]
  cidr_block              = "10.0.${count.index}.0/24"
  map_public_ip_on_launch = true
  vpc_id                  = aws_vpc.cbtt.id

  tags = tomap({
    "Name"                                      = "terraform-eks-cbtt-node",
    "kubernetes.io/cluster/${var.cluster-name}" = "shared",
  })
}

resource "aws_internet_gateway" "cbtt" {
  vpc_id = aws_vpc.cbtt.id

  tags = {
    Name = "terraform-eks-cbtt"
  }
}

resource "aws_route_table" "cbtt" {
  vpc_id = aws_vpc.cbtt.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.cbtt.id
  }
}

resource "aws_route_table_association" "cbtt" {
  count = 2

  subnet_id      = aws_subnet.cbtt.*.id[count.index]
  route_table_id = aws_route_table.cbtt.id
}
