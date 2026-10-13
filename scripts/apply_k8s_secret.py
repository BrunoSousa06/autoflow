#!/usr/bin/env python3
"""Aplica o Secret da aplicação com valores fornecidos pelo ambiente do processo."""

import json
import os
import subprocess
import sys


SECRET_NAME = "autoflow-secrets"
NAMESPACE = "default"
SECRET_KEYS = {
    "SPRING_DATASOURCE_USERNAME": "AUTOFLOW_DB_USERNAME",
    "SPRING_DATASOURCE_PASSWORD": "AUTOFLOW_DB_PASSWORD",
    "JWT_SECRET": "AUTOFLOW_JWT_SECRET",
    "MAIL_PASSWORD": "AUTOFLOW_MAIL_PASSWORD",
    "APP_PUBLIC_TOKEN_SECRET": "AUTOFLOW_PUBLIC_TOKEN_SECRET",
}


def main() -> int:
    missing = [env_name for env_name in SECRET_KEYS.values() if not os.environ.get(env_name)]
    if missing:
        print("Variáveis de ambiente obrigatórias ausentes ou vazias: " + ", ".join(missing), file=sys.stderr)
        return 2

    manifest = {
        "apiVersion": "v1",
        "kind": "Secret",
        "metadata": {"name": SECRET_NAME, "namespace": NAMESPACE},
        "type": "Opaque",
        "stringData": {key: os.environ[env_name] for key, env_name in SECRET_KEYS.items()},
    }
    payload = json.dumps(manifest, ensure_ascii=False).encode("utf-8")
    subprocess.run(
        ["kubectl", "apply", "--namespace", NAMESPACE, "--filename", "-"],
        input=payload,
        check=True,
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
