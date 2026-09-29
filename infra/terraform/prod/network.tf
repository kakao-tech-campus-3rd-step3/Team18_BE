# 현행 EC2는 단일 인스턴스에 컨테이너를 모아두었으므로 별도 네트워크 구성이 없다.
# GCP에서는 Cloud SQL과 Redis를 비공개로 두기 위해 VPC가 필요하다.

resource "google_compute_network" "main" {
  name                    = "dongarium"
  auto_create_subnetworks = false
}

resource "google_compute_subnetwork" "main" {
  name          = "dongarium-${var.region}"
  ip_cidr_range = "10.10.0.0/20"
  region        = var.region
  network       = google_compute_network.main.id

  # Cloud Run이 이 서브넷으로 직접 나가도록(Direct VPC egress) 설정할 때 사용한다.
  private_ip_google_access = true
}

# Cloud SQL과 Memorystore에 비공개 IP를 주기 위한 구성.
resource "google_compute_global_address" "private_service_range" {
  name          = "dongarium-private-service-range"
  purpose       = "VPC_PEERING"
  address_type  = "INTERNAL"
  prefix_length = 16
  network       = google_compute_network.main.id
}

resource "google_service_networking_connection" "private_service_access" {
  network                 = google_compute_network.main.id
  service                 = "servicenetworking.googleapis.com"
  reserved_peering_ranges = [google_compute_global_address.private_service_range.name]
}
