#!/usr/bin/env bash
set -euo pipefail

# =============================================
# Script to update RDS endpoint in systemd service
# =============================================

# Configuration
LIGHTSAIL_USER="${LIGHTSAIL_USER:-ubuntu}"
LIGHTSAIL_HOST="${LIGHTSAIL_HOST:-54.179.165.189}"
SSH_KEY="${SSH_KEY:-/c/Users/Asus/Downloads/LightsailDefaultKey-ap-southeast-1.pem}"
SERVICE_NAME="${SERVICE_NAME:-evm}"
SERVICE_FILE="/etc/systemd/system/${SERVICE_NAME}.service"

# New RDS endpoint (update this value)
NEW_RDS_ENDPOINT="${NEW_RDS_ENDPOINT:-evm-db.c3a0682col8l.ap-southeast-1.rds.amazonaws.com}"
DB_NAME="${DB_NAME:-DealerManagementSystem}"
DB_USERNAME="${DB_USERNAME:-admin}"
DB_PASSWORD="${DB_PASSWORD:-Ndtruong1808}"

# Build new connection string
NEW_CONNECTION_STRING="jdbc:sqlserver://${NEW_RDS_ENDPOINT}:1433;databaseName=${DB_NAME};encrypt=true;trustServerCertificate=true"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "=========================================="
echo "🔄 Updating RDS Endpoint"
echo "=========================================="
echo "Lightsail Server: ${LIGHTSAIL_USER}@${LIGHTSAIL_HOST}"
echo "Service: ${SERVICE_NAME}"
echo "New RDS Endpoint: ${NEW_RDS_ENDPOINT}"
echo "Database Name: ${DB_NAME}"
echo ""

# Validate SSH key exists
if [ ! -f "$SSH_KEY" ]; then
  echo -e "${RED}❌ Error: SSH key not found at: ${SSH_KEY}${NC}"
  echo "Please set SSH_KEY environment variable:"
  echo "  export SSH_KEY=/path/to/your/key.pem"
  exit 1
fi

# Validate endpoint format
if [[ ! "$NEW_RDS_ENDPOINT" =~ \.rds\.amazonaws\.com$ ]]; then
  echo -e "${YELLOW}⚠️  Warning: Endpoint doesn't look like a valid RDS endpoint${NC}"
  echo "Expected format: *.rds.amazonaws.com"
  read -p "Continue anyway? (y/N): " -n 1 -r
  echo
  if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    exit 1
  fi
fi

echo -e "${GREEN}[1/7]${NC} Testing SSH connection..."
if ! ssh -i "$SSH_KEY" -o ConnectTimeout=5 -o StrictHostKeyChecking=no "${LIGHTSAIL_USER}@${LIGHTSAIL_HOST}" "echo 'Connection successful'" > /dev/null 2>&1; then
  echo -e "${RED}❌ Error: Cannot connect to Lightsail server${NC}"
  echo "Please check:"
  echo "  1. SSH key path: ${SSH_KEY}"
  echo "  2. Server IP: ${LIGHTSAIL_HOST}"
  echo "  3. Network connectivity"
  exit 1
fi
echo -e "${GREEN}✓${NC} SSH connection successful"

echo -e "${GREEN}[2/7]${NC} Connecting to Lightsail server..."
ssh -i "$SSH_KEY" "${LIGHTSAIL_USER}@${LIGHTSAIL_HOST}" <<EOFREMOTE
  set -euo pipefail
  
  echo "[3/7] Checking current service file..."
  if [ ! -f ${SERVICE_FILE} ]; then
    echo -e "\033[0;31m❌ Error: Service file ${SERVICE_FILE} not found!\033[0m"
    exit 1
  fi
  echo -e "\033[0;32m✓\033[0m Service file found"
  
  echo "[4/7] Backing up current service file..."
  BACKUP_FILE="${SERVICE_FILE}.backup.\$(date +%Y%m%d%H%M%S)"
  sudo cp ${SERVICE_FILE} "\${BACKUP_FILE}"
  echo -e "\033[0;32m✓\033[0m Backup created: \${BACKUP_FILE}"
  
  echo "[5/7] Updating RDS endpoint in service file..."
  
  # Escape special characters in connection string for sed
  ESCAPED_CONNECTION_STRING=\$(echo "${NEW_CONNECTION_STRING}" | sed 's/[[\.*^$()+?{|]/\\&/g')
  
  # Check if SPRING_DATASOURCE_URL exists
  if sudo grep -q "SPRING_DATASOURCE_URL" ${SERVICE_FILE}; then
    # Update existing SPRING_DATASOURCE_URL
    sudo sed -i "s|SPRING_DATASOURCE_URL=.*|SPRING_DATASOURCE_URL=\${ESCAPED_CONNECTION_STRING}|g" ${SERVICE_FILE}
    echo -e "\033[0;32m✓\033[0m Updated existing SPRING_DATASOURCE_URL"
  else
    # Add new SPRING_DATASOURCE_URL if it doesn't exist
    # Find the line with other Environment variables and add after it
    if sudo grep -q "Environment=" ${SERVICE_FILE}; then
      # Add after the last Environment line
      sudo sed -i "/Environment=.*/a Environment=\"SPRING_DATASOURCE_URL=\${ESCAPED_CONNECTION_STRING}\"" ${SERVICE_FILE}
      echo -e "\033[0;32m✓\033[0m Added new SPRING_DATASOURCE_URL"
    else
      # Add in [Service] section
      sudo sed -i "/\[Service\]/a Environment=\"SPRING_DATASOURCE_URL=\${ESCAPED_CONNECTION_STRING}\"" ${SERVICE_FILE}
      echo -e "\033[0;32m✓\033[0m Added SPRING_DATASOURCE_URL in [Service] section"
    fi
  fi
  
  # Also update DB_USERNAME and DB_PASSWORD if needed
  if sudo grep -q "DB_USERNAME" ${SERVICE_FILE}; then
    sudo sed -i "s|DB_USERNAME=.*|DB_USERNAME=${DB_USERNAME}|g" ${SERVICE_FILE}
    echo -e "\033[0;32m✓\033[0m Updated DB_USERNAME"
  else
    sudo sed -i "/\[Service\]/a Environment=\"DB_USERNAME=${DB_USERNAME}\"" ${SERVICE_FILE}
    echo -e "\033[0;32m✓\033[0m Added DB_USERNAME"
  fi
  
  if sudo grep -q "DB_PASSWORD" ${SERVICE_FILE}; then
    sudo sed -i "s|DB_PASSWORD=.*|DB_PASSWORD=${DB_PASSWORD}|g" ${SERVICE_FILE}
    echo -e "\033[0;32m✓\033[0m Updated DB_PASSWORD"
  else
    sudo sed -i "/\[Service\]/a Environment=\"DB_PASSWORD=${DB_PASSWORD}\"" ${SERVICE_FILE}
    echo -e "\033[0;32m✓\033[0m Added DB_PASSWORD"
  fi
  
  # Verify the update
  echo ""
  echo "Updated configuration:"
  sudo grep -E "SPRING_DATASOURCE_URL|DB_USERNAME|DB_PASSWORD" ${SERVICE_FILE} || true
  
  echo ""
  echo "[6/7] Reloading systemd daemon..."
  sudo systemctl daemon-reload
  echo -e "\033[0;32m✓\033[0m Systemd daemon reloaded"
  
  echo "[7/7] Restarting service..."
  sudo systemctl restart ${SERVICE_NAME}
  sleep 5
  
  # Check service status
  if sudo systemctl is-active --quiet ${SERVICE_NAME}; then
    echo -e "\033[0;32m✓\033[0m Service is running"
    sudo systemctl status ${SERVICE_NAME} --no-pager -l | head -15 || true
  else
    echo -e "\033[0;31m❌ Service failed to start!\033[0m"
    sudo systemctl status ${SERVICE_NAME} --no-pager -l || true
    exit 1
  fi
  
  echo ""
  echo "=========================================="
  echo -e "\033[0;32m✅ RDS Endpoint Update Completed!\033[0m"
  echo "=========================================="
  echo "Service file: ${SERVICE_FILE}"
  echo "New endpoint: ${NEW_RDS_ENDPOINT}"
  echo "Backup file: \${BACKUP_FILE}"
  echo ""
EOFREMOTE

echo ""
echo -e "${GREEN}[8/8]${NC} Checking service logs for database connection..."
echo "Recent logs (last 30 lines):"
echo "----------------------------------------"
ssh -i "$SSH_KEY" "${LIGHTSAIL_USER}@${LIGHTSAIL_HOST}" \
  "sudo journalctl -u ${SERVICE_NAME} -n 30 --no-pager" | tail -30 || true

echo ""
echo "=========================================="
echo -e "${GREEN}✅ Update Complete!${NC}"
echo "=========================================="
echo ""
echo "Summary:"
echo "  • RDS Endpoint: ${NEW_RDS_ENDPOINT}"
echo "  • Database: ${DB_NAME}"
echo "  • Service: ${SERVICE_NAME}"
echo ""
echo -e "${YELLOW}⚠️  If you see any database connection errors in the logs,${NC}"
echo "please check:"
echo "  1. RDS Security Group allows connection from Lightsail IP (54.179.165.189/32)"
echo "  2. RDS instance status is 'Available'"
echo "  3. Database name, username, and password are correct"
echo "  4. Port 1433 is open in Security Group"
echo ""
echo "To view full logs, run:"
echo "  ssh -i ${SSH_KEY} ${LIGHTSAIL_USER}@${LIGHTSAIL_HOST} 'sudo journalctl -u ${SERVICE_NAME} -f'"
echo ""

