terraform {
  required_version = ">= 1.9"

  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 6.0"
    }
  }

  # 버킷 이름은 bootstrap 단계의 출력값으로 채운다.
  # terraform init -backend-config="bucket=<상태버킷>"
  backend "gcs" {
    prefix = "prod"
  }
}

provider "google" {
  project = var.project_id
  region  = var.region
}
