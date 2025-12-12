# Blogging System REST API 

## Goals
1. To gain more experience and deeper understanding of REST API best practices and conventions.
   * By utilizing proper HTTP methods, status codes, error handling, and Java object to JSON serialization/deserialization - DTO.
   * Proper authentication and authorization (JWT and Role-Based Access Control).
   
2. Learn and get more experience building full-stack applications by:
   * Planning components, system modularity, and testing strategies.
   * Designing the ERD and schema of the relational database focused on normalization and performance for SQL and RDMS experience.
   * Building this API with Java & Spring for Object-Oriented Design experience utilizing Controller, Service, & Repository layers.
   * Displaying it with a JavaScript Front-End Framework.

## Requirements
1. Tech Stack:
   * Java Spring Boot, MySQL, Angular or Vue + Typescript, AWS (EC2, ECR, S3)
   * JWT (for stateless authentication),
   * Docker and Nginx
2. CRUD operations on Blog Posts, Post Comments, and Users:
   * Filter posts by category, author, and/or date. 
   * Pagination and sorting for post lists
3. Single Page App 
4. Security:
   * Role-Based Access to differentiate between user and admin.
   * Secure endpoints with JWT and Rate limiting.
5. Structured logging and observability with Actuator.
6. Cache frequently accessed posts.
7. OpenAPI documentation for endpoints

## Architecture
Will follow a multi-tier architecture of:
1. Backend:
   * Java/Spring Boot.
   * Exposes REST endpoints, handle logic + security, and interaction with database layer.
   * Controller, Service, Repository.
2. Database:
   * H2 for development & testing / MySQL for production.
   * Stores all persistent data for Users, Roles, Blog Entries, Blog Comments, and Blog Categories.
   * Normalized schema with constraints for one-to-many and many-to-many relationships.
3. Frontend:
   * Typescript + Angular OR VUE (have not decided yet, would need to learn basics of Angular if so).
   * Single Page Application for consuming REST API, rendering user interface, and client side routing. 
   * HTTP/JSON communication.
4. Deployment:
   * Docker to contain Spring API and MySQL database.
   * Nginx for reverse proxy, to serve any static files, and SSL/TLS decryption.
   * Single AWS EC2 instance for cost efficiency.
   * ECR to hold the images for future migration to ECS if needed for scaling.

### Future Considerations
* AWS ECR to store Docker images (API and MySQL).
* Possibility to decouple MySQL by migrating to its own RDS or EC2 instance.
* Scaling the backend can be done with AWS ALB load balancing of multiple EC2 instances or utilization of ECS for container orchestration and horizontal instance scaling with API Docker image in ECR.
* Scaling the frontend can be done with S3 and CloudFront or its own EC2 instance(s)

## Planned API Endpoints
Role Based access - Entries can be set as `PUBLIC` or `PRIVATE` - meaning they will not show on public `GET` endpoints if requesting user is not the Author.

Planned Authorities - `PUBLIC` (not authenticated), `USER` (basic user/author) and `ADMIN` (administrator)

| Resource  | HTTP Method | Endpoint                               | Description                                                                                                                                          | Access Role                        |
|:----------|:------------|:---------------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------|:-----------------------------------|
| BlogEntry | `GET`       | `/api/posts`                           | Retrieve public all entries (with optional filters - categoryName, username, afterDate, beforeDate, and pagination/sorting).                         | `PUBLIC`                           |
| BlogEntry | `GET`       | `/api/posts/me`                        | Retrieve all public and private entries of authenticated user (with optional filters - categoryName, afterDate, beforeDate, and pagination/sorting). | `AUTHOR(USER)`                     |
| BlogEntry | `GET`       | `/api/posts/{id}`                      | Retrieve an entry by id. (if authenticated, will allow getting private entry if user is author)                                                      | `PUBLIC, USER`                     |
| BlogEntry | `POST`      | `/api/posts`                           | Create a new entry.                                                                                                                                  | `USER`                             |                                        
| BlogEntry | `PUT`       | `/api/posts/{id}`                      | Update an entry.                                                                                                                                     | `AUTHOR(USER), ADMIN`              |
| BlogEntry | `DELETE`    | `/api/posts/{id}`                      | Delete an entry.                                                                                                                                     | `AUTHOR(USER), ADMIN`              |
| Comment   | `GET`       | `/api/posts/{id}/comments`             | Retrieve all top-level comments for entry with pagination.                                                                                           | `USER`                             |
| Comment   | `GET`       | `/api/comments/me`                     | Retrieve all comments of authenticated user.                                                                                                         | `USER`                             |
| Comment   | `GET`       | `/api/comments/{parentCommentId}`      | Retrieve all replies to a specific parent comment.                                                                                                   | `USER`                             |
| Comment   | `GET`       | `/api/comments/comment/{commentId}`    | Retrieve a specific comment.                                                                                                                         | `USER`                             |
| Comment   | `POST`      | `/api/posts/{id}/comments`             | Create a new comment (optional '?parentId=' request param for creating a reply).                                                                     | `USER`                             |
| Comment   | `PUT`       | `/api/posts/{id}/comments/{commentId}` | Update a comment's text.                                                                                                                             | `USER, ADMIN`                      |
| Comment   | `DELETE`    | `/api/comments/{commentId}`            | Delete a comment's text (Sets comment text to 'Comment removed by (Comment Author, Blog Author, OR Admin)')                                          | `BLOG/COMMENT AUTHOR(USER), ADMIN` |
| AppUser   | `POST`      | `/api/auth/register`                   | Register a new user account.                                                                                                                         | `PUBLIC`                           |
| AppUser   | `POST`      | `/api/auth/login`                      | Authenticate user and receive a JWT.                                                                                                                 | `PUBLIC`                           |
| Category  | `GET`       | `/api/categories`                      | Retrieve all categories and count of posts with each.                                                                                                | `PUBLIC`                           |
| Category  | `POST`      | `/api/categories`                      | Create a new category.                                                                                                                               | `ADMIN`                            |
| Category  | `PUT`       | `/api/categories/{id}`                 | Update a category.                                                                                                                                   | `ADMIN`                            |
| Category  | `DELETE`    | `/api/categories/{id}`                 | Delete a category.                                                                                                                                   | `ADMIN`                            |
|           |             |                                        |                                                                                                                                                      |                                    |
