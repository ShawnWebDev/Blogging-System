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
   * *Backend:* Java 21+, Spring Boot 4, Spring MVC, Spring Security, Spring JDBC.
   * *Templating:* SSR with Thymeleaf.
   * *Interactivity:* HTMX & Vanilla CSS.
   * *Database:* MariaDB.
   * *Deployment:* AWS EC2 + S3(for DB backups in private bucket and css/image files in public bucket), Nginx(for reverse proxy & SSL with Certbot).
2. Core Features:
   * *CRUD operations* on Blog Posts, Post Comments, and Categories -
     * Text Editor for Creation & Update of Blog Post & Comment content JSONs.
     * Filter Blog posts by category.
   * Single Page App *Feel* -
     * Reduced full browser refreshes to get new content.
   * *Security*
     * Role-Based Access to differentiate between users and admin.
     * Session based auth with CSRF.
   * *Markdown-To-HTML*
     * CommonMark library for parsing MD files to HTML
3. Cache frequently accessed posts.

## Architecture
Will follow a multi-tier architecture of:
1. View Layer (Thymeleaf, HTMX, CSS): Utilizes fully rendered HTML with Thymeleaf, HTMX intercepts user interactions and requests the needed Thymeleaf fragments for partial page updates.
2. Controller Layer (Spring MVC): Returns full HTML views or Thymeleaf fragments as requested by Browser/HTMX.
3. Service Layer (Java, Spring Boot): Handles logic and coordinates between persistence and controller layers.
4. Persistence Layer (Spring JDBC / MariaDb): Manages data flow to and from MariaDB.

### Future Scaling Considerations
* Migrating MariaDB to its own EC2 instance.
* AWS load balancing multiple EC2 instances for application. 
* Pagination on BlogEntry/Comment lists.