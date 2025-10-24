Electric Vehicle Management (EVM)
 
 # Introduce
This is the backend service for the electric Vehicle Management system
It proivdes REST APIs to support user management, dealer management, vehicle listing and authentication using Java with Spring framework

# Requirement
- Java 17+
- Maven 3.9.11
- SQL server
- IDE/Code editor

# Setup
- Clone the repository:
  Bash
  git clone https://github.com/SWP391-FA25-VehicleDealerManagement/BE.git

- Cofiguration database connection
  spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=DealerManagementSystem;encrypt=false;trustServerCertificate=true
  spring.datasource.username=root
  spring.datasource.password=yourpassword

- Run project
    mvn spring-boot:run

  
