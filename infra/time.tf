resource "time_sleep" "wait_seconds" {
  depends_on = [
    kubectl_manifest.backend_service,
    kubectl_manifest.frontend_service
  ]
  create_duration = "120s"
}