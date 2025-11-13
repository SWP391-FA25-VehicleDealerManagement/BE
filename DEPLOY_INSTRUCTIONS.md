# 🚀 Hướng dẫn Deploy Backend

## ⚠️ Vấn đề hiện tại
- Frontend trên Vercel (`https://evm-tau.vercel.app/`) gọi tới backend bị block
- Nguyên nhân: Backend chưa có CORS config mới

## ✅ Giải pháp: Deploy backend với CORS mới

### Bước 1: SSH vào server
```bash
ssh -i "path/to/your/key.pem" ubuntu@54.179.165.189
```

### Bước 2: Trên server, chạy lệnh sau

```bash
# Di chuyển đến thư mục project (nếu có git repo trên server)
cd /path/to/evm

# Pull code mới (nếu đã push lên git)
git pull origin thanh

# Build backend
./mvnw clean package -DskipTests

# Tạo file với timestamp
TIMESTAMP=$(date +%Y%m%d%H%M%S)
JAR_NAME="evm-management-${TIMESTAMP}.jar"

# Copy file JAR vào thư mục releases
sudo cp target/evm-*.jar "/opt/evm/releases/$JAR_NAME"

# Update symlink
sudo rm -f /opt/evm/current/app.jar
sudo ln -s "/opt/evm/releases/$JAR_NAME" /opt/evm/current/app.jar

# Restart service
sudo systemctl restart evm

# Chờ app khởi động
sleep 15

# Kiểm tra status
sudo systemctl status evm --no-pager -l

# Kiểm tra port
sudo lsof -i :8080

# Test CORS
curl -H "Origin: https://evm-tau.vercel.app" -H "Access-Control-Request-Method: POST" -H "Access-Control-Request-Headers: Content-Type" -X OPTIONS http://localhost:8080/api/auth/login -v
```

## 🔍 Kiểm tra sau khi deploy

1. **Test Swagger UI:**
   ```bash
   curl http://54.179.165.189:8080/swagger-ui/index.html
   ```

2. **Test CORS từ Vercel:**
   - Mở browser: https://evm-tau.vercel.app/
   - F12 > Console
   - Thử login

## 📝 Nếu không có code trên server

Upload file JAR đã build sẵn:

```bash
# Trên máy Windows, build trước:
cd D:\Code\Spring\SWP\evm
.\mvnw.cmd clean package -DskipTests

# Upload lên server bằng SCP hoặc FileZilla:
# - Source: D:\Code\Spring\SWP\evm\target\evm-*.jar
# - Destination: ubuntu@54.179.165.189:/opt/evm/releases/

# Sau đó SSH vào server và chạy:
TIMESTAMP=$(date +%Y%m%d%H%M%S)
sudo mv /opt/evm/releases/evm-*.jar "/opt/evm/releases/evm-management-${TIMESTAMP}.jar"
sudo rm -f /opt/evm/current/app.jar
sudo ln -s "/opt/evm/releases/evm-management-${TIMESTAMP}.jar" /opt/evm/current/app.jar
sudo systemctl restart evm
```

## 🎯 Sau khi backend deploy xong

**Vẫn cần push code Frontend lên Vercel** để code mới được deploy:

1. **Fix Git permission:**
   ```bash
   # Trên máy Windows
   cd D:\Code\Spring\SWP\FE
   git push
   ```

2. **Hoặc Manual Redeploy trên Vercel:**
   - Vào: https://vercel.com/dashboard
   - Chọn project `evm-tau`
   - Tab `Deployments`
   - Click `...` → `Redeploy`

