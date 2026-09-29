# GCP 인프라 (Terraform)

현행 AWS 운영 환경을 GCP에서 동일하게 재현하기 위한 코드다.
현행 구성은 [`infra/inventory/current-aws.md`](../inventory/current-aws.md)에 정리되어 있다.

## 디렉터리

| 경로 | 내용 | 상태 파일 |
|---|---|---|
| `bootstrap/` | 필요한 API 활성화, Terraform 상태 버킷 생성 | 로컬 |
| `prod/` | 운영 인프라 | `bootstrap/`에서 만든 GCS 버킷 |

## 적용 순서

```bash
# 0) 로그인 (GCP 계정과 프로젝트가 있어야 한다)
gcloud auth login
gcloud auth application-default login
gcloud config set project <프로젝트 ID>

# 1) bootstrap: API와 상태 버킷
cd infra/terraform/bootstrap
terraform init
terraform apply -var project_id=<프로젝트 ID> -var state_bucket_name=<버킷 이름>

# 2) prod
cd ../prod
terraform init -backend-config="bucket=<위에서 만든 버킷 이름>"
cp terraform.tfvars.example terraform.tfvars   # 값 수정
terraform plan
terraform apply
```

## 적용 후 사람이 해야 하는 일

1. **Secret Manager 값 입력.** 코드는 빈 항목만 만든다. 값은 직접 넣는다.
   ```bash
   printf '%s' '<값>' | gcloud secrets versions add db-password --data-file=-
   ```
   대상 항목은 `terraform output secret_ids`로 확인한다.

2. **GitHub Secrets 등록.** 두 저장소(`Team18_BE`, `Team18_FE`)에 아래를 넣는다.
   | Secret | 값 |
   |---|---|
   | `GCP_WORKLOAD_IDENTITY_PROVIDER` | `terraform output workload_identity_provider` |
   | `GCP_SERVICE_ACCOUNT` | `terraform output deployer_service_account` |

## 현행과의 대응

| 현행 (AWS) | 이 코드 |
|---|---|
| ECR `dongarium-server` | `google_artifact_registry_repository.server` |
| EC2 인스턴스 프로파일 | `google_service_account.app` |
| `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` GitHub Secret | Workload Identity Federation (`iam.tf`) |
| `shared/.env`, `APPLICATION_YML` GitHub Secret | Secret Manager (`secrets.tf`) |
| 단일 EC2 (네트워크 구성 없음) | VPC + 서브넷. Cloud SQL·Redis를 비공개로 두기 위해 필요 (`network.tf`) |

## 아직 없는 것 (후속 작업)

- Cloud SQL, Memorystore, GCS 버킷
- Cloud Run, 로드밸런서, Cloud CDN, Cloud DNS
- 모니터링 스택
