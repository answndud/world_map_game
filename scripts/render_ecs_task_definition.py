#!/usr/bin/env python3
"""Render a concrete ECS task definition from the sample template."""

from __future__ import annotations

import argparse
import json
import os
import re
import sys
from pathlib import Path
from typing import Any

PLACEHOLDER_PATTERN = re.compile(r"<[A-Z0-9_]+>")


def require_env(name: str) -> str:
    value = os.getenv(name, "").strip()
    if not value:
        raise SystemExit(f"Missing required environment variable: {name}")
    return value


def load_json(path: Path) -> dict[str, Any]:
    return json.loads(path.read_text(encoding="utf-8"))


def find_container(task_definition: dict[str, Any], name: str) -> dict[str, Any]:
    for container in task_definition.get("containerDefinitions", []):
        if container.get("name") == name:
            return container
    raise SystemExit(f"Container not found in task definition: {name}")


def set_named_entry(entries: list[dict[str, Any]], name: str, field: str, value: str) -> None:
    for entry in entries:
        if entry.get("name") == name:
            entry[field] = value
            return
    raise SystemExit(f"Entry not found in task definition: {name}")


def replace_placeholders(node: Any, replacements: dict[str, str]) -> Any:
    if isinstance(node, dict):
        return {key: replace_placeholders(value, replacements) for key, value in node.items()}
    if isinstance(node, list):
        return [replace_placeholders(item, replacements) for item in node]
    if isinstance(node, str):
        rendered = node
        for placeholder, replacement in replacements.items():
            rendered = rendered.replace(placeholder, replacement)
        return rendered
    return node


def main() -> int:
    parser = argparse.ArgumentParser(description="Render the ECS task definition template.")
    parser.add_argument("--input", required=True, type=Path, help="Path to the sample task definition JSON.")
    parser.add_argument("--output", required=True, type=Path, help="Path to write the rendered task definition JSON.")
    args = parser.parse_args()

    task_definition = load_json(args.input)
    container = find_container(task_definition, "worldmap-app")

    aws_region = require_env("AWS_REGION")
    aws_account_id = require_env("AWS_ACCOUNT_ID")
    image_uri = os.getenv("IMAGE_URI", "").strip()
    if not image_uri:
        image_uri = (
            f"{aws_account_id}.dkr.ecr.{aws_region}.amazonaws.com/"
            f"{require_env('ECR_REPOSITORY')}:{require_env('IMAGE_TAG')}"
        )

    task_definition["executionRoleArn"] = require_env("ECS_EXECUTION_ROLE_ARN")
    task_definition["taskRoleArn"] = require_env("ECS_TASK_ROLE_ARN")
    container["image"] = image_uri

    set_named_entry(
        container["environment"],
        "SPRING_DATASOURCE_URL",
        "value",
        f"jdbc:postgresql://{require_env('RDS_ENDPOINT')}:5432/worldmap",
    )
    set_named_entry(
        container["environment"],
        "SPRING_DATA_REDIS_HOST",
        "value",
        require_env("ELASTICACHE_ENDPOINT"),
    )
    set_named_entry(
        container["secrets"],
        "SPRING_DATASOURCE_PASSWORD",
        "valueFrom",
        require_env("SPRING_DATASOURCE_PASSWORD_SECRET_ARN"),
    )
    set_named_entry(
        container["secrets"],
        "WORLDMAP_ADMIN_BOOTSTRAP_PASSWORD",
        "valueFrom",
        require_env("ADMIN_BOOTSTRAP_PASSWORD_PARAMETER_ARN"),
    )
    container["logConfiguration"]["options"]["awslogs-group"] = require_env("CLOUDWATCH_LOG_GROUP")
    container["logConfiguration"]["options"]["awslogs-region"] = aws_region

    rendered_definition = replace_placeholders(
        task_definition,
        {
            "<ACCOUNT_ID>": aws_account_id,
            "<REGION>": aws_region,
            "<IMAGE_TAG>": os.getenv("IMAGE_TAG", "").strip(),
        },
    )

    output_text = json.dumps(rendered_definition, ensure_ascii=False, indent=2) + "\n"
    unresolved = sorted(set(PLACEHOLDER_PATTERN.findall(output_text)))
    if unresolved:
        raise SystemExit(f"Unresolved placeholders remain: {', '.join(unresolved)}")

    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(output_text, encoding="utf-8")
    return 0


if __name__ == "__main__":
    sys.exit(main())
