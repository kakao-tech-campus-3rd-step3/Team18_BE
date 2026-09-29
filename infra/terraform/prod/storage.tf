# 현행 S3 버킷 3개에 대응한다.
#   dongarium-club-image  : 동아리 이미지. 브라우저가 URL로 바로 읽는다 → 공개 읽기
#   dongarium-attachments : 공지 첨부. 서명된 URL로만 내려받는다 → 비공개
#   프론트 정적 파일       : CloudFront가 S3에서 읽어 서비스한다 → LB 백엔드 버킷으로 공개

resource "google_storage_bucket" "club_image" {
  name     = var.bucket_club_image
  location = var.region

  uniform_bucket_level_access = true
  public_access_prevention    = "inherited"
}

resource "google_storage_bucket_iam_member" "club_image_public" {
  bucket = google_storage_bucket.club_image.name
  role   = "roles/storage.objectViewer"
  member = "allUsers"
}

resource "google_storage_bucket" "attachments" {
  name     = var.bucket_attachments
  location = var.region

  uniform_bucket_level_access = true
  public_access_prevention    = "enforced"
}

resource "google_storage_bucket" "frontend" {
  name     = var.bucket_frontend
  location = var.region

  uniform_bucket_level_access = true
  public_access_prevention    = "inherited"

  # SPA 라우팅. LB에서도 404를 index.html로 돌려주지만, 버킷 단독 접근 시에도 동작하게 둔다.
  website {
    main_page_suffix = "index.html"
    not_found_page   = "index.html"
  }
}

resource "google_storage_bucket_iam_member" "frontend_public" {
  bucket = google_storage_bucket.frontend.name
  role   = "roles/storage.objectViewer"
  member = "allUsers"
}

# 앱이 이미지와 첨부파일을 읽고 쓸 수 있어야 한다. 현행 EC2 IAM 역할에 대응한다.
resource "google_storage_bucket_iam_member" "app_club_image" {
  bucket = google_storage_bucket.club_image.name
  role   = "roles/storage.objectAdmin"
  member = "serviceAccount:${google_service_account.app.email}"
}

resource "google_storage_bucket_iam_member" "app_attachments" {
  bucket = google_storage_bucket.attachments.name
  role   = "roles/storage.objectAdmin"
  member = "serviceAccount:${google_service_account.app.email}"
}

# 서명된 URL을 만들려면 서비스 계정이 자기 자신으로 서명할 수 있어야 한다.
# (현행 S3Presigner에 대응)
resource "google_project_iam_member" "app_token_creator" {
  project = var.project_id
  role    = "roles/iam.serviceAccountTokenCreator"
  member  = "serviceAccount:${google_service_account.app.email}"
}
