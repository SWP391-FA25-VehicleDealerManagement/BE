#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

APP_NAME="evm-management"
JAR_NAME="${APP_NAME}-0.0.1-SNAPSHOT.jar"   # chỉnh nếu đổi version hoặc set <finalName>
BUILD_CMD="./mvnw.cmd clean package -DskipTests"

LIGHTSAIL_USER="${LIGHTSAIL_USER:-ubuntu}"
LIGHTSAIL_HOST="${LIGHTSAIL_HOST:-54.179.165.189}"
SSH_KEY="${SSH_KEY:-/c/Users/Asus/Downloads/LightsailDefaultKey-ap-southeast-1.pem}"

REMOTE_DIR="${REMOTE_DIR:-/opt/evm}"
SERVICE_NAME="${SERVICE_NAME:-evm}"
TIMESTAMP=$(date +"%Y%m%d%H%M%S")

LOCAL_JAR="${SCRIPT_DIR}/target/${JAR_NAME}"
REMOTE_RELEASE="${REMOTE_DIR}/releases/${APP_NAME}-${TIMESTAMP}.jar"

echo "[1/5] Build jar"
pushd "${SCRIPT_DIR}" > /dev/null
$BUILD_CMD
popd > /dev/null

echo "[2/5] Upload jar"
scp -i "$SSH_KEY" "$LOCAL_JAR" "${LIGHTSAIL_USER}@${LIGHTSAIL_HOST}:${REMOTE_RELEASE}"

echo "[3/5] Update symlink & restart service"
ssh -i "$SSH_KEY" "${LIGHTSAIL_USER}@${LIGHTSAIL_HOST}" <<EOFREMOTE
  set -euo pipefail
  sudo systemctl stop ${SERVICE_NAME}
  sudo ln -sfn ${REMOTE_RELEASE} ${REMOTE_DIR}/current/app.jar
  sudo systemctl start ${SERVICE_NAME}
  sudo systemctl status ${SERVICE_NAME} --no-pager
EOFREMOTE

echo "[4/5] Tail logs"
ssh -i "$SSH_KEY" "${LIGHTSAIL_USER}@${LIGHTSAIL_HOST}" \
  "sudo journalctl -u ${SERVICE_NAME} -n 50 --no-pager"

echo "[5/5] Done"