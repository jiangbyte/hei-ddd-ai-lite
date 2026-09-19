#!/usr/bin/env bash
# 一次性启动本仓库常用本地依赖容器（按已有 docker 容器名）。
set -euo pipefail

# 1. 后端默认依赖：MySQL / Redis（已在跑则 docker start 无副作用）
# 2. S3 兼容、向量库、Milvus UI（LLM 走 OpenAI，不再启 ollama）
CONTAINERS=(
  mysql
  redis
  silo
  milvus
  attu
)

echo "启动容器: ${CONTAINERS[*]}"
docker start "${CONTAINERS[@]}"

echo
docker ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}' \
  --filter name=mysql \
  --filter name=redis \
  --filter name=silo \
  --filter name=milvus \
  --filter name=attu
