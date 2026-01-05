# Project Management System

A comprehensive project management system built with Spring Boot 3.2.2 and Java 21, featuring project tracking, issue management, team collaboration, and subscription-based payment integration.

## 🚀 Features

### Core Functionality
- **Project Management**: Create, update, delete, and search projects with category and tag filtering
- **Issue Tracking**: Full CRUD operations for issues with status tracking, priority levels, and assignee management
- **User Management**: User registration, authentication, role-based access control (RBAC)
- **Team Collaboration**: Project invitations, team member management, and real-time messaging
- **Comments & Attachments**: Add comments and file attachments to projects and issues
- **Subscription System**: Tiered subscription plans (FREE, MONTHLY, ANNUAL) with payment integration

### Payment Integration
- **VNPay**: Vietnamese payment gateway integration
- **Momo**: E-wallet payment integration
- Automated subscription upgrades and payment tracking

### Security
- **OAuth2 Resource Server**: JWT-based authentication
- **Role-Based Access Control**: Fine-grained permissions system
- **Token Management**: Secure token generation and invalidation

## 🛠️ Technology Stack

### Backend Framework
- **Spring Boot 3.2.2**: Core framework
- **Java 21**: Programming language
- **Spring Data JPA**: Data persistence
- **Spring Security**: Authentication and authorization
- **Spring Validation**: Input validation

### Database
- **MySQL**: Primary database
- **H2**: In-memory database for testing

### Code Generation & Mapping
- **MapStruct 1.5.5**: Object mapping
- **QueryDSL 5.0.0**: Type-safe queries
- **Lombok 1.18.30**: Boilerplate code reduction

### API Documentation
- **SpringDoc OpenAPI 2.1.0**: Swagger/OpenAPI 3.0 documentation

### Code Quality & Testing
- **Spotless**: Code formatting
- **JaCoCo**: Code coverage
- **JUnit 5**: Unit testing
- **Testcontainers**: Integration testing with MySQL containers

### Additional Libraries
- **Spring Mail**: Email notifications
- **Thymeleaf**: Email templates
- **Jackson**: JSON processing
- **RestTemplate**: HTTP client

## 📋 Prerequisites

- **Java 21** or higher
- **Maven 3.6+**
- **MySQL 8.0+**
- **Git**

## 🔧 Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd springboot-ms
```

### 2. Configure Database
Create a MySQL database and update the configuration:

```properties
# src/main/resources/application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/your_database_name
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Configure Payment Gateways (Optional)
Update VNPay and Momo credentials in the configuration files:
- VNPay: `VNPayProperties.java`
- Momo: Payment provider configuration

### 4. Build the Project
```bash
mvn clean install
```

### 5. Run the Application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## 🏗️ Project Structure

```
springboot-ms/
├── src/main/java/com/hieu/ms/
│   ├── configuration/       # Application configuration
│   ├── controller/          # REST API controllers
│   ├── dto/                 # Data Transfer Objects
│   │   ├── request/         # Request DTOs
│   │   └── response/        # Response DTOs
│   ├── entity/              # JPA entities
│   ├── event/               # Application events
│   ├── exception/           # Custom exceptions
│   ├── feature/             # Feature modules (e.g., subscription)
│   ├── mapper/              # MapStruct mappers
│   ├── payment/             # Payment provider implementations
│   │   ├── momo/            # Momo integration
│   │   └── vnpay/           # VNPay integration
│   ├── repository/          # JPA repositories
│   ├── service/             # Business logic services
│   └── validator/           # Custom validators
├── src/main/resources/
│   └── application.properties
├── src/test/                # Test files
├── pom.xml                  # Maven configuration
└── README.md
```

## 🔑 Key Components

### Entities
- **User**: User accounts with roles and subscriptions
- **Project**: Projects with teams, issues, and attachments
- **Issue**: Tasks/issues with status, priority, and assignees
- **Subscription**: User subscription plans
- **Payment**: Payment transaction records
- **Role & Permission**: RBAC implementation
- **Message & Comment**: Communication features
- **Invitation**: Team invitation system

### Controllers
- `UserController`: User management
- `ProjectController`: Project operations
- `IssueController`: Issue tracking
- `SubscriptionController`: Subscription management
- `AuthenticationController`: Login/logout
- `RoleController`: Role management
- `PermissionController`: Permission management
- `MessageController`: Messaging
- `CommentController`: Comments
- `AttachmentController`: File uploads
- `PaymentWebhookController`: Payment callbacks

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Run with Coverage
```bash
mvn clean test jacoco:report
```

Coverage report will be available at: `target/site/jacoco/index.html`

### Integration Tests
Integration tests use Testcontainers to spin up MySQL containers automatically.

## 🎨 Code Formatting

The project uses Spotless for code formatting:

```bash
# Check formatting
mvn spotless:check

# Apply formatting
mvn spotless:apply
```

Formatting is automatically applied during compilation.

## 🔒 Security Configuration

### JWT Authentication
- Access tokens are validated using OAuth2 Resource Server
- Custom JWT decoder with token validation
- Token invalidation support

### Role-Based Access Control
- Users have multiple roles
- Roles contain multiple permissions
- Fine-grained access control on endpoints

## 💳 Payment Flow

1. User requests subscription upgrade via `/subscriptions/upgrade`
2. System generates payment URL (VNPay or Momo)
3. User completes payment on provider's page
4. Provider sends webhook to `/payment/webhook`
5. System validates payment and updates subscription
6. Subscription audit trail is maintained

## 📧 Email Notifications

- Project invitations
- Subscription updates
- Configured with Spring Mail and Thymeleaf templates

## 🚀 Deployment

### Production Build
```bash
mvn clean package -DskipTests
```

The executable JAR will be in `target/ms-0.0.1-SNAPSHOT.jar`

### Run Production JAR
```bash
java -jar target/ms-0.0.1-SNAPSHOT.jar
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License.

## 👥 Authors

- **Hieu** - Initial work

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- MapStruct for object mapping
- QueryDSL for type-safe queries
- All contributors and supporters
