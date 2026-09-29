# 현행 EC2의 shared/.env와 GitHub Secret APPLICATION_YML로 주입하던 값들.
# 값은 Terraform으로 넣지 않는다. 껍데기만 만들고 사람이 직접 버전을 추가한다.
#   gcloud secrets versions add <이름> --data-file=-
locals {
  secret_ids = [
    "db-password",         # 앱이 사용하는 DB 비밀번호 (현행 DB_PASSWORD)
    "jwt-secret",          # JWT_SECRET
    "kakao-client-id",     # KAKAO_CLIENT_ID
    "kakao-client-secret", # KAKAO_CLIENT_SECRET
    "smtp-username",       # SMTP_USERNAME (Gmail 주소)
    "smtp-password",       # SMTP_PASSWORD (Gmail 앱 비밀번호)
    "discord-webhook-url", # DISCORD_WEBHOOK_URL (logback Discord appender)
  ]
}

resource "google_secret_manager_secret" "app" {
  for_each  = toset(local.secret_ids)
  secret_id = each.value

  replication {
    user_managed {
      replicas {
        location = var.region
      }
    }
  }
}
