# Full-Stack Blogging System (SSR + HTMX)

## Overview
A modern blogging platform that feels like a highly interactive single-page application, using server-side rendering and Hypermedia without the need for two separate code bases as HTMX is written in the template/HTML declaratively .

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
    * *Backend:* Java 21+, Spring Boot 4, Spring MVC, Spring Security, Spring JDBC Client
    * *Templating:* Thymeleaf
    * *Interactivity:* HTMX & CSS
    * *Database:* MariaDB
    * *Deployment:* AWS EC2 & S3, Nginx, Amazon Corretto JDK 25, conservative JVM and InnoDB memory tuning
2. Core Features:
    * *CRUD operations* on Blog Posts, Post Comments, and Categories -
        * Filter Blog posts by category.
    * Single Page App *Feel* -
        * Reduced full browser refreshes to get new content.
    * *Security*
        * Role-Based Access to differentiate between users and admin.
        * Session based auth with CSRF token required when changing state.
    * *Content*
        * Written in Markdown,
        * Stored as a TEXT field in DB,
        * Rendered to HTML with CommonMark and sent to template
3. Testing:
   * *Unit:* JUnit & Mockito to test helper methods and error handling in Services
   * *Integration:* JUnit/JdbcTest and TestContainers to test various CRUD functionality in DAOs
     * *End to End:* @SpringBootTest (full application context), JUnit, TestContainers, TestRestTemplate to test interaction from controller to data and back.

## Architecture
Will follow a multi-tier architecture of:
1. View Layer (Thymeleaf, HTMX, CSS): Utilizes fully rendered HTML with Thymeleaf, HTMX intercepts user interactions and requests the needed Thymeleaf fragments for partial page updates.
2. Controller Layer (Spring MVC): Returns full HTML views or Thymeleaf fragments as requested by Browser/HTMX.
3. Service Layer (Java, Spring Boot): Handles logic and coordinates between persistence and controller layers.
4. Persistence Layer (Spring JDBC Client / MariaDb): Manages data flow to and from MariaDB.

### Future Scaling Considerations
* Migrating MariaDB to its own EC2 instance.
* AWS load balancing multiple EC2 instances for application.
* Pagination on BlogEntries/Comments lists.
* Cache BlogEntries after HTML is generated from Markdown.