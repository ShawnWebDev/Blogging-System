
CREATE TABLE `users` (
     `id` int NOT NULL AUTO_INCREMENT,
     `username` varchar(64) NOT NULL,
     `password` varchar(72) NOT NULL,
     `date_created` date DEFAULT curdate(),
     `is_active` tinyint(1) DEFAULT 0,
     `role` enum('ADMIN','USER') NOT NULL,
     `email` varchar(255) NOT NULL,
     PRIMARY KEY (`id`),
     UNIQUE KEY `Users_UNIQUE` (`username`),
     UNIQUE KEY `users_email_unique` (`email`)
);

CREATE TABLE `blog_entries` (
    `id` int NOT NULL AUTO_INCREMENT,
    `content` text NOT NULL,
    `title` varchar(255) NOT NULL,
    `slug` varchar(255) NOT NULL,
    `description` varchar(500) NOT NULL,
    `created_at` datetime DEFAULT current_timestamp(),
    `updated_at` datetime DEFAULT NULL ON UPDATE current_timestamp(),
    `thumbnail_url` varchar(255) NOT NULL,
    `thumbnail_alt` varchar(255) NOT NULL,
    `in_progress` tinyint(1) NOT NULL,
    `code_url` varchar(255) DEFAULT NULL,
    `demo_url` varchar(255) DEFAULT NULL,
    `article_url` varchar(255) DEFAULT NULL,
    `is_portfolio` tinyint(1) NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `comments` (
    `id` int NOT NULL AUTO_INCREMENT,
    `content` varchar(500) NOT NULL,
    `created_at` datetime NOT NULL DEFAULT current_timestamp(),
    `updated_at` datetime DEFAULT NULL ON UPDATE current_timestamp(),
    `parent_comment_id` int DEFAULT NULL,
    `author_id` int NOT NULL,
    `post_id` int NOT NULL,
    `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `Comments_Comments_FK` (`parent_comment_id`),
    KEY `Comments_Users_FK` (`author_id`),
    KEY `Comments_Blog_Entries_FK` (`post_id`),
    CONSTRAINT `Comments_Blog_Entries_FK` FOREIGN KEY (`post_id`) REFERENCES `blog_entries` (`id`) ON DELETE CASCADE,
    CONSTRAINT `Comments_Comments_FK` FOREIGN KEY (`parent_comment_id`) REFERENCES `comments` (`id`) ON DELETE CASCADE,
    CONSTRAINT `comments_users_FK` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`)
);

CREATE TABLE `categories` (
        `id` int NOT NULL AUTO_INCREMENT,
        `category_name` varchar(100) NOT NULL,
        `description` varchar(255) NOT NULL,
        PRIMARY KEY (`id`),
        UNIQUE KEY `Categories_UNIQUE` (`category_name`)
);

CREATE TABLE `posts_categories` (
        `post_id` int NOT NULL,
        `category_id` int NOT NULL,
        UNIQUE KEY `post_id` (`post_id`,`category_id`),
        KEY `Posts_Categories_Categories_FK` (`category_id`),
        KEY `Posts_Categories_Blog_Entry_FK` (`post_id`),
        CONSTRAINT `Posts_Categories_Blog_Entry_FK` FOREIGN KEY (`post_id`) REFERENCES `blog_entries` (`id`) ON DELETE CASCADE,
        CONSTRAINT `Posts_Categories_Categories_FK` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE CASCADE
);

CREATE TABLE `verification` (
       `user_id` int NOT NULL,
       `otp` varchar(72) NOT NULL,
       `expiry` DATETIME NOT NULL,
       `resetCounter` int NOT NULL DEFAULT 0,
       PRIMARY KEY (`user_id`),
       KEY `verification_tokens_users_FK` (`user_id`),
       CONSTRAINT `verification_tokens_users_FK` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
);