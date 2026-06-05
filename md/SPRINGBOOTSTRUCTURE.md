```markdown
# Spring Boot Backend Structure - Standard Layered Architecture

## Package Structure

```
src/main/java/com.example.app/
├── Application.java
│
├── entity/                 # JPA entities (database mapping)
│   ├── User.java
│   ├── Product.java
│   └── Order.java
│
├── dto/                    # Data Transfer Objects
│   ├── request/
│   │   ├── UserRequest.java
│   │   └── LoginRequest.java
│   └── response/
│       ├── UserResponse.java
│       └── ErrorResponse.java
│
├── repository/             # Data access layer
│   ├── UserRepository.java
│   ├── ProductRepository.java
│   └── OrderRepository.java
│
├── service/                # Business logic layer
│   ├── UserService.java
│   ├── UserServiceImpl.java
│   ├── ProductService.java
│   └── OrderService.java
│
├── controller/             # REST endpoints
│   ├── UserController.java
│   ├── ProductController.java
│   └── OrderController.java
│
├── exception/              # Custom exceptions & handlers
│   ├── BusinessException.java
│   ├── ResourceNotFoundException.java
│   └── GlobalExceptionHandler.java
│
├── config/                 # Configuration classes
│   ├── SecurityConfig.java
│   └── SwaggerConfig.java
│
└── util/                   # Helpers & utilities
    ├── DateUtils.java
    └── ValidationUtils.java
```

---

## Layer Responsibilities

### 1. Entity Layer (`entity/`)
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String email;
    
    // getters, setters, constructors
}
```

**Rules:**
- Maps directly to database tables
- Contains JPA annotations only
- No business logic

---

### 2. DTO Layer (`dto/`)

**Request DTO:**
```java
public record UserRequest(
    @NotBlank(message = "Name is required")
    String name,
    
    @Email(message = "Invalid email")
    String email
) {}
```

**Response DTO:**
```java
public record UserResponse(
    Long id,
    String name,
    String email
) {}
```

**Rules:**
- Never expose entities in API responses
- Use `record` (Java 17+) or simple POJOs
- Add validation annotations on request DTOs

---

### 3. Repository Layer (`repository/`)
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByNameContaining(String name);
}
```

**Rules:**
- Extends `JpaRepository<T, ID>`
- Custom queries as method names
- No implementation code

---

### 4. Service Layer (`service/`)

**Interface:**
```java
public interface UserService {
    UserResponse createUser(UserRequest request);
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    void deleteUser(Long id);
}
```

**Implementation:**
```java
@Service
@Transactional
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    
    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }
    
    @Override
    public UserResponse createUser(UserRequest request) {
        // Validate business rules
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email already exists");
        }
        
        // Map request -> entity
        User user = modelMapper.map(request, User.class);
        
        // Save to database
        User savedUser = userRepository.save(user);
        
        // Map entity -> response
        return modelMapper.map(savedUser, UserResponse.class);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return modelMapper.map(user, UserResponse.class);
    }
}
```

**Rules:**
- `@Service` annotation
- `@Transactional` for write operations
- `@Transactional(readOnly = true)` for queries
- All business logic here
- Map between Entity and DTO

---

### 5. Controller Layer (`controller/`)
```java
@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
```

**Rules:**
- `@RestController` + `@RequestMapping`
- No business logic (delegate to service)
- Use `@Valid` for request validation
- Return proper HTTP status codes

---

## Standard Request Flow

```
HTTP Request
    ↓
[Controller] - Validate input (@Valid)
    ↓
[Service]    - Business logic, validation
    ↓
[Repository] - Database operations
    ↓
[Entity]     - JPA persistence
    ↓
[Service]    - Map Entity → DTO
    ↓
[Controller] - Return DTO as response
    ↓
HTTP Response
```

---

## Best Practices Summary

| Layer | Do | Don't |
|-------|----|-------|
| **Entity** | JPA annotations only | Add business logic |
| **DTO** | Validation annotations | Expose sensitive data |
| **Repository** | Extend JpaRepository | Write implementation |
| **Service** | Business logic, transactions | Handle HTTP concerns |
| **Controller** | Request/response handling | Business logic, DB access |

---

## File Naming Convention

| Layer | Class Name | Example |
|-------|-----------|---------|
| Entity | `{Name}` | `User.java` |
| DTO | `{Name}Request/Response` | `UserRequest.java` |
| Repository | `{Name}Repository` | `UserRepository.java` |
| Service | `{Name}Service` | `UserService.java` |
| Service Impl | `{Name}ServiceImpl` | `UserServiceImpl.java` |
| Controller | `{Name}Controller` | `UserController.java` |

---

## Dependencies (Maven)

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Mapping -->
    <dependency>
        <groupId>org.modelmapper</groupId>
        <artifactId>modelmapper</artifactId>
        <version>3.1.1</version>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
</dependencies>
```
```