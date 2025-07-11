# Backend

## Development Instructions

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- PostgreSQL
- MongoDB
- IDE (IntelliJ IDEA or Visual Studio Code recommended)

### Database Setup
1. Use lowercase letters and underscores for database names
2. Follow the format `levelup_<service>_db` for service databases
   - User Service: `levelup_user_db`
3. Update database credentials in configuration files

### Port Mapping
- Config Server: 8888
- Eureka Server: 8761
- API Gateway: 8080
- User Service: 8081
- Course Service: 8082
- Assessment Service: 8083

### Service Startup Order
**Important: Services must be started in this specific order:**

1. Config Server *(don't use this yet)*
2. Eureka Server
3. API Gateway
4. Other services (User, Course, Assessment)
