# Full-Stack Blogging System (SSR + HTMX)

## Overview
A modern blogging platform that feels like a highly interactive single-page application, using server-side rendering and Hypermedia without a heavy JavaScript front-end.

## Goals
1. To gain more experience and deeper understanding of Hypermedia-Driven Development best practices and conventions.
   * Utilize HTMX to swap HTML fragments dynamically to reduce latency and complexity.
2. Optimize for Performance & SEO
   * Eliminate the need for separate frontend build, large JavaScript bundles, and frontend state management.
   * Better SEO with fully rendered HTML for better indexability.
   * No CORS issues.
3. Deepen Spring Boot and Relational Data Expertise
   * System planning, modularity, and testing strategies.
   * Design schema with focus on normalization and performance.

## Requirements
1. Tech Stack:
   * *Backend:* Java 21+, Spring Boot 3, Spring MVC, Spring Data JPA, Spring Security.
   * *Templating:* SSR Thymeleaf.
   * *Interactivity:* HTMX & Vanilla CSS.
   * *Database:* MariaDB.
   * *Deployment:* AWS EC2 + S3(for DB backups in private bucket and css/image files in public bucket), Nginx(for reverse proxy & SSL with Certbot).
2. Core Features:
   * *CRUD operations* on Blog Posts, Post Comments, and Categories -
     * Filter posts by category and/or date. 
     * Pagination on post lists .
   * Single Page App *Feel* -
     * No full browser refreshes to get new content.
   * *Security* -
     * Role-Based Access to differentiate between users and admin.
     * Session based auth with CSRF.
3. Structured logging and observability with Actuator.
4. Cache frequently accessed posts.
5. JavaDoc documentation.

## Architecture
Will follow a multi-tier architecture of:
1. View Layer (Thymeleaf, HTMX, CSS): Utilizes fully rendered HTML with Thymeleaf, HTMX intercepts user interactions and requests the needed Thymeleaf fragments for partial page updates.
2. Controller Layer (Spring MVC): Returns full HTML views or Thymeleaf fragments as requested by HTMX.
3. Service Layer (Java, Spring Boot): Handles logic and coordinates between persistence and controller layers.
4. Persistence Layer (Spring Data JPA): Manages data flow to and from MariaDB.

### Future Scaling Considerations
* Decouple MariaDB by migrating to its own EC2 instance.
* AWS load balancing and multiple EC2 instances for application & separate JWT service instead of sessions.

## Planned Endpoints
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
