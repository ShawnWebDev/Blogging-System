
CREATE TABLE `users` (
        `id` int(11) NOT NULL AUTO_INCREMENT,
        `username` varchar(255) NOT NULL,
        `password` varchar(255) NOT NULL,
        `date_created` date NOT NULL,
        `is_active` tinyint(1) NOT NULL DEFAULT 1,
        `role` enum('ADMIN','USER') NOT NULL,
        PRIMARY KEY (`id`),
        UNIQUE KEY `Users_UNIQUE` (`username`)
);

CREATE TABLE `blog_entries` (
        `id` int NOT NULL AUTO_INCREMENT,
        `content` text NOT NULL,
        `title` varchar(255) NOT NULL,
        `description` varchar(500) NOT NULL,
        `created_at` datetime NOT NULL,
        `updated_at` datetime,
        `slug` varchar(255) NOT NULL,
        `thumbnail_url` varchar(255) NOT NULL,
        `thumbnail_alt` varchar(255) NOT NULL,
        PRIMARY KEY (`id`),
        UNIQUE KEY `Slug_UNIQUE` (`slug`)
);

CREATE TABLE `comments` (
        `id` int NOT NULL AUTO_INCREMENT,
        `content` varchar(500) NOT NULL,
        `created_at` datetime NOT NULL,
        `updated_at` datetime,
        `parent_comment_id` int DEFAULT NULL,
        `author_id` int NOT NULL,
        `post_id` int NOT NULL,
        PRIMARY KEY (`id`),
        KEY `Comments_Comments_FK` (`parent_comment_id`),
        KEY `Comments_Users_FK` (`author_id`),
        KEY `Comments_Blog_Entries_FK` (`post_id`),
        CONSTRAINT `Comments_Blog_Entries_FK` FOREIGN KEY (`post_id`) REFERENCES `blog_entries` (`id`) ON DELETE CASCADE,
        CONSTRAINT `Comments_Comments_FK` FOREIGN KEY (`parent_comment_id`) REFERENCES `comments` (`id`) ON DELETE CASCADE,
        CONSTRAINT `Comments_Users_FK` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`)
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