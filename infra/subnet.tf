resource "aws_subnet" "subnet_public" {
  count                   = 3
  vpc_id                  = aws_vpc.autoflow_vpc.id
  cidr_block              = cidrsubnet(aws_vpc.autoflow_vpc.cidr_block, 4, count.index)
  map_public_ip_on_launch = true
  availability_zone       = ["us-east-1a", "us-east-1b", "us-east-1c"][count.index]

  tags = {
    "Name" = "public-subnet"

    "kubernetes.io/role/elb"                       = "1"
    "kubernetes.io/cluster/eks-autoflow-terraform" = "shared"
  }

}

resource "aws_subnet" "subnet_private" {
  count                   = 3
  vpc_id                  = aws_vpc.autoflow_vpc.id
  cidr_block              = cidrsubnet(aws_vpc.autoflow_vpc.cidr_block, 4, count.index + 4)
  map_public_ip_on_launch = false
  availability_zone       = ["us-east-1a", "us-east-1b", "us-east-1c"][count.index]


  tags = {
    Name = "private-subnet"

    "kubernetes.io/role/internal-elb"              = "1"
    "kubernetes.io/cluster/eks-autoflow-terraform" = "shared"
  }

}