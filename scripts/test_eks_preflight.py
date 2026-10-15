import json
import os
import unittest
from unittest.mock import patch

import eks_preflight


IDENTITY = {"Account": "123456789012"}
ACTIVE_CLUSTER = {"cluster": {"status": "ACTIVE"}}
ACTIVE_GROUP = {"nodegroup": {"status": "ACTIVE", "health": {"issues": []}}}


def node(ready):
    return {"status": {"conditions": [{"type": "Ready", "status": "True" if ready else "False"}]}}


class PreflightTests(unittest.TestCase):
    def setUp(self):
        self.env = patch.dict(os.environ, {
            "AWS_EXPECTED_ACCOUNT_ID": "123456789012", "AWS_REGION": "us-east-1",
            "EKS_PREFLIGHT_TIMEOUT_SECONDS": "1", "EKS_PREFLIGHT_POLL_INTERVAL_SECONDS": "1",
        }, clear=False)
        self.env.start()
        self.addCleanup(self.env.stop)
        self.sequence = [json.dumps(IDENTITY), json.dumps(ACTIVE_CLUSTER), json.dumps(ACTIVE_GROUP), "arn:aws:eks:us-east-1:123456789012:cluster/eks-autoflow", json.dumps({"items": [node(True), node(True)]})]

    def execute(self, **kwargs):
        merged = dict(kwargs)
        side_effect = merged.pop("side_effect", None)
        with patch.object(eks_preflight, "run", side_effect=side_effect or self.sequence.copy()), patch.object(eks_preflight.time, "sleep"):
            return eks_preflight.main()

    def test_wrong_account(self):
        with patch.dict(os.environ, {"AWS_EXPECTED_ACCOUNT_ID": "000000000000"}), patch.object(eks_preflight, "run", return_value=json.dumps(IDENTITY)):
            with self.assertRaisesRegex(eks_preflight.PreflightError, "Conta AWS incorreta"):
                eks_preflight.main()

    def test_invalid_or_expired_credentials(self):
        with patch.object(eks_preflight, "run", side_effect=eks_preflight.PreflightError("Credenciais AWS inválidas/expiradas")):
            with self.assertRaisesRegex(eks_preflight.PreflightError, "Credenciais AWS"):
                eks_preflight.main()

    def test_cluster_unavailable_times_out(self):
        with patch.object(eks_preflight, "run", side_effect=[json.dumps(IDENTITY), eks_preflight.PreflightError("Falha em aws eks describe-cluster: indisponível")]), patch.object(eks_preflight.time, "monotonic", side_effect=[0, 0, 0, 2, 2]), patch.object(eks_preflight.time, "sleep"):
            with self.assertRaisesRegex(eks_preflight.PreflightError, "Timeout"):
                eks_preflight.main()

    def test_degraded_nodegroup_reports_health(self):
        degraded = {"nodegroup": {"status": "DEGRADED", "health": {"issues": [{"code": "NodeCreationFailure"}]}}}
        with patch.object(eks_preflight, "run", side_effect=[json.dumps(IDENTITY), json.dumps(ACTIVE_CLUSTER), json.dumps(degraded)]), patch.object(eks_preflight.time, "monotonic", side_effect=[0, 0, 0, 2, 2]), patch.object(eks_preflight.time, "sleep"):
            with self.assertRaisesRegex(eks_preflight.PreflightError, "NodeCreationFailure"):
                eks_preflight.main()

    def test_zero_nodes_times_out(self):
        self.sequence[-1] = json.dumps({"items": []})
        with patch.object(eks_preflight, "run", side_effect=self.sequence), patch.object(eks_preflight.time, "monotonic", side_effect=[0, 0, 0, 2, 2]), patch.object(eks_preflight.time, "sleep"):
            with self.assertRaisesRegex(eks_preflight.PreflightError, "Nós insuficientes"):
                eks_preflight.main()

    def test_one_ready_node_times_out(self):
        self.sequence[-1] = json.dumps({"items": [node(True), node(False)]})
        with patch.object(eks_preflight, "run", side_effect=self.sequence), patch.object(eks_preflight.time, "monotonic", side_effect=[0, 0, 0, 2, 2]), patch.object(eks_preflight.time, "sleep"):
            with self.assertRaisesRegex(eks_preflight.PreflightError, "1 Ready"):
                eks_preflight.main()

    def test_two_ready_nodes_pass(self):
        self.assertEqual(self.execute(), 0)

    def test_command_failure_is_reported(self):
        with patch.object(eks_preflight, "run", side_effect=eks_preflight.PreflightError("Comando não encontrado: kubectl")), patch.object(eks_preflight.time, "monotonic", side_effect=[0, 0, 0, 2, 2]), patch.object(eks_preflight.time, "sleep"):
            with self.assertRaisesRegex(eks_preflight.PreflightError, "kubectl"):
                eks_preflight.main()

    def test_wrong_context_fails_immediately(self):
        values = self.sequence[:3] + ["outro-cluster"]
        with patch.object(eks_preflight, "run", side_effect=values):
            with self.assertRaisesRegex(eks_preflight.PreflightError, "Contexto kubectl incorreto"):
                eks_preflight.main()

    def test_pipeline_blocks_terraform_and_kubectl_on_hosted_runner(self):
        from pathlib import Path
        workflow = Path(__file__).parents[1] / ".github" / "workflows" / "pipeline.yml"
        content = workflow.read_text(encoding="utf-8")
        blocked_job = content.split("  deployment-blocked-private-eks:", 1)[1]
        self.assertIn("runs-on: ubuntu-latest", blocked_job)
        self.assertIn("exit 1", blocked_job)
        self.assertNotRegex(content, r"(?m)^\s+run:.*terraform\s+(?:plan|apply)")
        self.assertNotRegex(content, r"(?m)^\s+run:.*kubectl\s")

    def test_images_are_published_only_from_develop_push_with_commit_sha(self):
        from pathlib import Path
        workflow = Path(__file__).parents[1] / ".github" / "workflows" / "pipeline.yml"
        content = workflow.read_text(encoding="utf-8")
        publish = content.split("  docker-publish:", 1)[1].split("  deployment-blocked-private-eks:", 1)[0]
        self.assertIn("github.event_name == 'push'", publish)
        self.assertIn("refs/heads/develop", publish)
        self.assertIn("${{ github.sha }}", publish)
        self.assertNotIn("pull_request", publish)
        self.assertNotIn(":latest", content)


if __name__ == "__main__":
    unittest.main()
