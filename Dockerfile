# Dùng OpenJDK 17 chính thức
FROM openjdk:17-jdk-slim

# Cài Maven (tránh lỗi mvnw)
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

# Tạo thư mục làm việc
WORKDIR /app

# Copy pom.xml trước để cache
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build JAR
RUN mvn clean package -DskipTests -B

# Chạy ứng dụng
EXPOSE 8080
CMD ["java", "-jar", "/app/target/*.jar"]