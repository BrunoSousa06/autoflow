resource "kubectl_manifest" "autoflow_config" {
  yaml_body = templatefile("${path.module}/../k8s/configmap.yaml", {
    rds_address = aws_db_instance.autoflow_rds.address
  })

  depends_on = [aws_eks_cluster.autoflow, aws_db_instance.autoflow_rds, kubectl_manifest.backend_service
  ]
}

resource "kubectl_manifest" "autoflow_secrets" {
  yaml_body = templatefile("${path.module}/../k8s/secret.yaml", {
    db_password = var.database_password
    db_username = var.database_username
  })
}


resource "kubectl_manifest" "frontend_config" {
  yaml_body = templatefile("${path.module}/../k8s/frontend-configmap.yaml", {
    backend_service_address = data.kubernetes_service_v1.backend_lb.status[0].load_balancer[0].ingress[0].hostname
  })

  depends_on = [time_sleep.wait_seconds, kubectl_manifest.backend_service,
  ]
}
