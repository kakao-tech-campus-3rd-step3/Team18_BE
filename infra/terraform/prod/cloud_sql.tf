# 현행: EC2의 mysql:8.0 컨테이너 (버전 8.0.43, utf8mb4/utf8mb4_unicode_ci, KST, max_connections=200)
# DB 크기는 4.5MB로 작다. 사양은 현행 컨테이너 사용량(약 412MB)을 기준으로 잡는다.

resource "google_sql_database_instance" "main" {
  name             = "dongarium"
  database_version = "MYSQL_8_0"
  region           = var.region

  # 실수로 삭제되지 않도록 한다. 삭제하려면 이 값을 false로 바꾸고 apply한 뒤 지운다.
  deletion_protection = true

  settings {
    tier              = var.db_tier
    availability_type = "ZONAL"
    disk_type         = "PD_SSD"
    disk_size         = 10
    disk_autoresize   = true

    ip_configuration {
      # 현행 MySQL은 외부에서 연결되지 않는다. 공개 IP를 만들지 않는다.
      ipv4_enabled    = false
      private_network = google_compute_network.main.id
    }

    backup_configuration {
      enabled                        = true
      start_time                     = "18:00" # UTC 18:00 = KST 03:00
      binary_log_enabled             = true    # 특정 시점 복구(PITR)에 필요
      transaction_log_retention_days = 7

      backup_retention_settings {
        retained_backups = 7
      }
    }

    maintenance_window {
      day          = 7  # 일요일
      hour         = 19 # UTC 19:00 = KST 04:00
      update_track = "stable"
    }

    # 현행 컨테이너 설정과 동일하게 맞춘다.
    database_flags {
      name  = "default_time_zone"
      value = "+09:00"
    }

    database_flags {
      name  = "character_set_server"
      value = "utf8mb4"
    }

    database_flags {
      name  = "max_connections"
      value = "200"
    }
  }

  depends_on = [google_service_networking_connection.private_service_access]
}

resource "google_sql_database" "app" {
  name      = var.db_name
  instance  = google_sql_database_instance.main.name
  charset   = "utf8mb4"
  collation = "utf8mb4_unicode_ci"
}

# 스테이징용 DB. 인스턴스는 운영과 공유하고 데이터베이스만 분리한다.
resource "google_sql_database" "staging" {
  name      = "${var.db_name}-staging"
  instance  = google_sql_database_instance.main.name
  charset   = "utf8mb4"
  collation = "utf8mb4_unicode_ci"
}

# 비밀번호는 Secret Manager에 넣어둔 값을 사용한다.
# 주의: 이 값은 Terraform state에 저장된다. state 버킷은 비공개이며 버전 관리가 켜져 있다.
data "google_secret_manager_secret_version" "db_password" {
  secret = google_secret_manager_secret.app["db-password"].secret_id
}

resource "google_sql_user" "app" {
  name     = var.db_user
  instance = google_sql_database_instance.main.name
  password = data.google_secret_manager_secret_version.db_password.secret_data
  host     = "%"
}
