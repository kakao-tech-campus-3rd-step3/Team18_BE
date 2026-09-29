# 현행: EC2의 redis:7-alpine 컨테이너. 사용 메모리 1.65MB, 키 20개, AOF 꺼짐, maxmemory 제한 없음.
# 영속성이 없는 현행과 같은 성격이므로 복제 없는 BASIC 등급을 쓴다.

resource "google_redis_instance" "main" {
  name           = "dongarium"
  tier           = "BASIC"
  memory_size_gb = 1
  region         = var.region

  redis_version = "REDIS_7_0"

  # 비공개 서비스 접근으로 VPC 내부에서만 연결한다.
  authorized_network      = google_compute_network.main.id
  connect_mode            = "PRIVATE_SERVICE_ACCESS"
  transit_encryption_mode = "DISABLED"

  depends_on = [google_service_networking_connection.private_service_access]
}
