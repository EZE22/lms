# LMS Backend API

Working reference for the Spring Boot LMS backend.

**Base URL:** `http://localhost:8080`

All request and response bodies are JSON. Auth and error codes are not implemented yet.

## Typical flow

1. `POST /api/courses` — create a course
2. `POST /api/courses/{courseId}/lessons` — add a lesson to it
3. `POST /api/users` — create a user
4. `POST /api/enrollments?userId=&courseId=` — enroll the user in the course
5. `POST /api/progress?userId=&lessonId=` — mark a lesson complete
6. `GET /api/dashboard/{userId}` — check the user's aggregated progress

## Endpoints

### Courses

#### POST /api/courses
Create a course.

Body:
```json
{ "title": "Intro to Java", "description": "Language fundamentals" }
```

#### GET /api/courses
List all courses.

No body or params.

### Lessons

#### POST /api/courses/{courseId}/lessons
Add a lesson to the given course.

Body:
```json
{ "title": "Variables and Types", "orderIndex": 1 }
```

#### GET /api/courses/{courseId}/lessons
List lessons in a course, ordered by `orderIndex` ascending.

No body or params.

### Users

#### POST /api/users
Create a user.

Body:
```json
{ "name": "Ada Lovelace", "email": "ada@example.com" }
```

### Enrollments

#### POST /api/enrollments?userId=&courseId=
Enroll a user in a course. Query params, no body.

Example: `POST /api/enrollments?userId=1&courseId=1`

#### GET /api/enrollments/user/{userId}
List a user's enrollments.

Example: `GET /api/enrollments/user/1`

### Progress

#### POST /api/progress?userId=&lessonId=
Mark a lesson complete for a user. Query params, no body. Idempotent — calling
it again for the same user/lesson updates the existing record instead of
creating a duplicate.

Example: `POST /api/progress?userId=1&lessonId=1`

#### GET /api/progress/user/{userId}
List a user's completed lessons.

Example: `GET /api/progress/user/1`

### Dashboard

#### GET /api/dashboard/{userId}
Aggregated view: user info plus each enrolled course with its total lesson
count and the user's completed lesson count.

Example: `GET /api/dashboard/1`

Response:
```json
{
  "userId": 1,
  "userName": "Ada Lovelace",
  "courses": [
    { "courseId": 1, "courseTitle": "Intro to Java", "totalLessons": 5, "completedLessons": 2 }
  ]
}
```
