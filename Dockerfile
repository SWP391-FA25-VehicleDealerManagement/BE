# Dùng OpenJDK 17
FROM openjdk:17-jdk-slim

# Cài Maven (fix lỗi mvnw)
RUN apt-get update && apt-get install -y maven

# Tạo thư mục
WORKDIR /app

# Copy pom.xml trước để cache
COPY pom.xml .

# Download dependencies (cache layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src src

# Build JAR (skip test)
RUN mvn clean package -DskipTests

# Chạy app (Render tự detect port)
EXPOSE 8080
CMD ["java", "-jar", "target/*.jar"]