# Terraform 상태 파일을 둘 버킷과 필요한 API를 켜는 단계.
# 이 디렉터리만 로컬 상태를 쓰고, prod 디렉터리는 여기서 만든 버킷을 원격 상태로 사용한다.
# 실행: terraform init && terraform apply -var project_id=<프로젝트> -var billing_account=<결제계정>

terraform {
  required_version = ">= 1.9"

  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 6.0"
    }
  }
}

provider "google" {
  project = var.project_id
  region  = var.region
}

variable "project_id" {
  description = "GCP 프로젝트 ID"
  type        = string
}

variable "region" {
  description = "기본 리전 (현행 AWS ap-northeast-2와 가장 가까운 asia-northeast3)"
  type        = string
  default     = "asia-northeast3"
}

variable "state_bucket_name" {
  description = "Terraform 상태 버킷 이름 (전역에서 고유해야 한다)"
  type        = string
}

# 이전 작업에 필요한 API만 켠다.
resource "google_project_service" "required" {
  for_each = toset([
    "artifactregistry.googleapis.com",
    "certificatemanager.googleapis.com",
    "cloudresourcemanager.googleapis.com",
    "compute.googleapis.com",
    "dns.googleapis.com",
    "iamcredentials.googleapis.com",
    "logging.googleapis.com",
    "monitoring.googleapis.com",
    "redis.googleapis.com",
    "run.googleapis.com",
    "secretmanager.googleapis.com",
    "servicenetworking.googleapis.com",
    "sqladmin.googleapis.com",
    "storage.googleapis.com",
    "sts.googleapis.com",
    "vpcaccess.googleapis.com",
  ])

  service = each.value

  # API를 껐다 켜는 일이 없도록 파괴 시 비활성화하지 않는다.
  disable_on_destroy = false
}

resource "google_storage_bucket" "tfstate" {
  name     = var.state_bucket_name
  location = var.region

  # 상태 파일은 실수로 지우면 복구가 어렵다.
  versioning {
    enabled = true
  }

  uniform_bucket_level_access = true
  public_access_prevention    = "enforced"

  lifecycle {
    prevent_destroy = true
  }

  depends_on = [google_project_service.required]
}

output "state_bucket" {
  value = google_storage_bucket.tfstate.name
}
