# Request Flow

How a request moves through this Spring Boot backend, from startup to response.
Everything here is based on the actual code under
`backend/src/main/java/org/genspark/backend/`.

## Application Startup

The entry point is `BackendApplication.java`:

```java
@SpringBootApplication
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
```

**`@SpringBootApplication`** is three annotations in one:

- `@SpringBootConfiguration` — marks this class as a source of bean definitions.
- `@ComponentScan` — tells Spring to scan this package (`org.genspark.backend`)
  and everything under it for classes annotated with `@RestController`,
  `@Service`, `@Repository`, `@Component`, etc.
- `@EnableAutoConfiguration` — Spring Boot looks at what's on the classpath
  (Spring Web, Spring Data JPA, the MySQL driver) and auto-configures sensible
  defaults: an embedded Tomcat server, a `DispatcherServlet`, a `DataSource`
  pointing at the URL in `application.properties`, a Hibernate
  `EntityManagerFactory`, a Jackson JSON converter, and so on.

**`SpringApplication.run(...)`** is what actually boots the app. In order, it:

1. Creates the Spring `ApplicationContext` (the container that holds all beans).
2. Runs component scanning and registers a bean definition for every annotated
   class it finds — `CourseController`, `EnrollmentService`, `DashboardService`,
   the repository interfaces, etc.
3. Instantiates those beans and wires their dependencies. Because each class
   uses constructor injection with `@Autowired`, Spring builds them in
   dependency order — e.g. `CourseRepository` and `UserService` are created
   before `EnrollmentService`, which needs all three of
   `EnrollmentRepository`, `UserService`, and `CourseService` in its
   constructor.
4. For each `interface ... extends JpaRepository`, Spring Data generates a
   proxy implementation at runtime (including derived queries like
   `findByUserIdAndCourseId`).
5. Connects to MySQL using the settings in
   `backend/src/main/resources/application.properties`
   (`jdbc:mysql://localhost:3306/lms`) and, because `ddl-auto=update`, lets
   Hibernate create or alter tables to match the `@Entity` classes.
6. Starts embedded Tomcat on port 8080 and registers the `DispatcherServlet`
   as the front controller for all incoming HTTP requests.

Once `run()` returns, the app is listening and ready.

## Request Lifecycle

Worked example: **`POST /api/enrollments?userId=1&courseId=1`**

1. **HTTP request arrives.** Tomcat accepts the TCP connection and hands the
   request to the `DispatcherServlet` (Spring's front controller, registered
   at startup).

2. **DispatcherServlet routes it.** It asks its `HandlerMapping` which
   controller method matches `POST /api/enrollments`. The match is
   `EnrollmentController.enroll(...)`, found via the class-level
   `@RequestMapping("/api/enrollments")` plus the method-level `@PostMapping`
   in `controller/EnrollmentController.java`.

3. **Argument binding.** Spring reads the `userId` and `courseId` query
   parameters and converts them to `Long` because the method parameters are
   annotated `@RequestParam`. There is no `@RequestBody` here, so no JSON is
   parsed on the way in.

4. **EnrollmentController receives it.**
   `EnrollmentController.enroll(Long userId, Long courseId)` runs and
   immediately delegates: `return enrollmentService.enroll(userId, courseId);`

5. **EnrollmentService does the work.**
   `service/EnrollmentService.java`, method `enroll(Long, Long)`:
   - `enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)` — guard
     against a duplicate enrollment; throws `RuntimeException` if already
     enrolled.
   - `userService.getUserById(userId)` → `UserService.getUserById` →
     `userRepository.findById(userId)`, throwing
     `"User not found with id: ..."` if absent.
   - `courseService.getCourseById(courseId)` → `CourseService.getCourseById` →
     `courseRepository.findById(courseId)`, throwing
     `"Course not found with id: ..."` if absent.
   - `enrollmentRepository.save(new Enrollment(user, course))` — persist the
     new row. `Enrollment`'s `@PrePersist onCreate()` sets `enrolledAt` just
     before the insert.

6. **Hibernate translates repository calls into SQL.** The repository methods
   are Spring Data proxies backed by Hibernate. `existsBy...` becomes a
   `SELECT`, `findById` becomes a `SELECT ... WHERE id = ?`, and `save`
   becomes an `INSERT INTO enrollments (...)`. With `spring.jpa.show-sql=true`
   these are printed to the console.

7. **MySQL executes it.** Statements run against the `lms` schema over the
   JDBC connection configured at startup. The generated primary key comes
   back (the `id` column is `GenerationType.IDENTITY`).

8. **Response object flows back up.** The saved `Enrollment` (now with `id`
   and `enrolledAt` populated) is returned from `EnrollmentService.enroll`
   to `EnrollmentController.enroll`, which returns it to the
   `DispatcherServlet`.

9. **Jackson serializes it to JSON.** Because `EnrollmentController` is a
   `@RestController`, the return value is handled by
   `HttpMessageConverter` (Jackson), which turns the `Enrollment` object into
   a JSON body. Lombok's `@Getter` on the entity is what makes its fields
   readable to Jackson.

10. **HTTP response returned.** `DispatcherServlet` writes the JSON body with
    status `200 OK` and Tomcat sends it back over the connection.

`GET /api/enrollments/user/{userId}` follows the same path, except the id
comes from `@PathVariable`, the service calls
`enrollmentRepository.findByUserId(userId)` (a single `SELECT`), and Jackson
serializes a JSON array.

## Layer Responsibilities

| Layer | Annotation | Job | Examples in this repo |
|-------|-----------|-----|-----------------------|
| **Entity** | `@Entity` | A plain Java object mapped to a database table. Declares columns, relationships (`@ManyToOne`), constraints, and lifecycle hooks (`@PrePersist`). No business logic. | `entity/Course.java`, `entity/Lesson.java`, `entity/Enrollment.java`, `entity/User.java`, `entity/LessonProgress.java` |
| **Repository** | `extends JpaRepository` | Data access only. Inherits `save`, `findById`, `findAll`, etc. Add derived query methods by naming convention — Spring Data writes the SQL. | `repository/CourseRepository.java` (no custom methods), `repository/LessonRepository.java` (`findByCourseIdOrderByOrderIndexAsc`), `repository/LessonProgressRepository.java` (`findByUserIdAndLessonId`) |
| **Service** | `@Service` | Business logic and orchestration. Combines multiple repositories/services, enforces rules, does the "not found" checks, builds DTOs. Controllers stay thin because this layer is where decisions live. | `service/CourseService.java`, `service/EnrollmentService.java` (duplicate-check + cross-service lookups), `service/DashboardService.java` (aggregates three repositories into a DTO) |
| **Controller** | `@RestController` | HTTP boundary. Maps URLs to methods, binds `@RequestBody` / `@RequestParam` / `@PathVariable`, calls one service method, returns the result (Jackson handles JSON). No business logic. | `controller/CourseController.java`, `controller/EnrollmentController.java`, `controller/DashboardController.java` |

Rule of thumb: a request should pass **Controller → Service → Repository** and
the response back the same way. Controllers don't touch repositories directly;
entities don't contain logic.

## Package Structure

```
org.genspark.backend
├── BackendApplication.java     app entry point (@SpringBootApplication + main)
├── entity/                     JPA entities — one class per table
│   ├── Course.java
│   ├── Lesson.java
│   ├── User.java
│   ├── Enrollment.java
│   └── LessonProgress.java
├── repository/                 Spring Data JPA interfaces (data access)
│   ├── CourseRepository.java
│   ├── LessonRepository.java
│   ├── UserRepository.java
│   ├── EnrollmentRepository.java
│   └── LessonProgressRepository.java
├── service/                    business logic and orchestration
│   ├── CourseService.java
│   ├── LessonService.java
│   ├── UserService.java
│   ├── EnrollmentService.java
│   ├── LessonProgressService.java
│   └── DashboardService.java
├── controller/                 REST endpoints (@RestController, URL mapping)
│   ├── CourseController.java
│   ├── LessonController.java
│   ├── UserController.java
│   ├── EnrollmentController.java
│   ├── LessonProgressController.java
│   └── DashboardController.java
└── dto/                        response shapes that aren't entities
    ├── UserDashboardDto.java
    └── CourseProgressDto.java
```

Configuration lives outside the Java tree in
`backend/src/main/resources/application.properties` (datasource URL, JPA
settings). See [API.md](API.md) for the endpoint reference.
