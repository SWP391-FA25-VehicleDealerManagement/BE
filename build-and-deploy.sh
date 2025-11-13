#!/bin/bash

echo "=========================================="
echo "Build và Deploy Backend"
echo "=========================================="

# 1. Build backend
echo "1. Building backend..."
./mvnw clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo "✅ Build thành công!"

# 2. Tạo tên file với timestamp
TIMESTAMP=$(date +%Y%m%d%H%M%S)
JAR_NAME="evm-management-${TIMESTAMP}.jar"

echo ""
echo "2. Tạo file: $JAR_NAME"
cp target/evm-*.jar "/opt/evm/releases/$JAR_NAME"

# 3. Update symlink
echo "3. Update symlink..."
rm -f /opt/evm/current/app.jar
ln -s "/opt/evm/releases/$JAR_NAME" /opt/evm/current/app.jar

# 4. Restart service
echo "4. Restart evm service..."
sudo systemctl restart evm

# 5. Wait và check status
echo "5. Chờ 15 giây để app khởi động..."
sleep 15

echo "6. Kiểm tra status:"
sudo systemctl status evm --no-pager -l | tail -20

echo ""
echo "7. Kiểm tra port 8080:"
sudo lsof -i :8080

echo ""
echo "=========================================="
echo "✅ Deploy hoàn tất!"
echo "=========================================="
echo ""
echo "Test backend:"
echo "  curl http://localhost:8080/swagger-ui/index.html"
echo "  curl http://54.179.165.189:8080/swagger-ui/index.html"

