# Full-Stack Blogging System (SSR + HTMX)

## Overview
A modern blogging platform that behaves like a interactive single-page application, using server-side rendering and HATEOAS without the need for two separate code bases as HTMX is written in the templates declaratively.

## Goals
1. To gain more experience and deeper understanding of api best practices and conventions.
2. Optimize for Performance & SEO
    * Utilize HTMX to swap small HTML fragments dynamically.
    * Eliminate the need for separate frontend codebase, build system, and frontend state management.
    * Better SEO with fully rendered HTML for better indexability.
3. Deepen System Design, Spring Boot, and Relational Data Expertise
    * System planning, modularity, and testing strategies.
    * Design schema with focus on normalization and performance.

## Requirements
1. Tech Stack:
    * *Backend:* Java 25, Spring Boot 4, Spring MVC, Spring Security, Spring JDBC Client
    * *Templating:* Thymeleaf
    * *Interactivity:* HTMX, JS, CSS
    * *Database:* MariaDB
    * *Deployment:* 
      * AWS EC2 (app and DB) & S3 (static assets),
      * CloudFront (reverse proxy, WAF, HTTPS, and cdn),
2. Core Features:
    * *CRUD operations* on Blog Posts, Post Comments, and Categories -
        * Filter Blog posts by category.
    * Single Page App *Feel* -
        * No full browser refreshes to get new content, HTMX swaps new content as needed.
    * *Security*
        * Role-Based Access to differentiate between users and admin.
        * Session based auth with CSRF token required when changing data state.
    * *Content*
        * Written in Markdown,
        * Rendered to HTML with CommonMark
3. Testing:
   * *Unit:* JUnit & Mockito to test helper methods and error handling in Services
   * *Integration:* JUnit/JdbcTest and TestContainers to test various CRUD functionality in DAOs
     * *End to End:* @SpringBootTest (full application context), JUnit, TestContainers, TestRestTemplate to test interaction from controller to data and back.

## Architecture
Will follow a multi-tier architecture of:
1. View Layer (Thymeleaf, HTMX, JS, CSS): Server rendered HTML with Thymeleaf, HTMX intercepts user interactions and requests the needed Thymeleaf fragments for partial page updates.
2. Web Layer (Spring MVC, Embedded Apache Tomcat): Spring MVC routes and controls the http response of full HTML Thymeleaf templates or Thymeleaf fragments as requested by web browser or HTMX (full page load vs partial). Tomcat handles HTTP request parsing/response serving, request threads, and servlet lifecycle.
3. Service Layer (Java, Spring): Handles logic and coordination of persistence and web layers.
4. Persistence Layer (Spring JDBC Client / MariaDb): Manages data.

### Future Scaling Considerations
* Migrating MariaDB to its own EC2 or RDS instance.
* If memory constrained: Scale vertically with larger instance, if compute constrained: scale horizontally with AWS CloudFront and Load balancer in front of multiple small EC2 instances for application.
* Pagination on BlogEntries/Comments lists.
* Cache BlogEntries after HTML is generated from Markdown, invalidate on update/delete. In memory if single-instance (Caffeine, H2, hashmap), external if distributed (Redis).