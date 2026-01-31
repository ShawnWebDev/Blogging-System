CREATE TABLE `users` (
    `id` int NOT NULL AUTO_INCREMENT,
    `username` varchar(255) NOT NULL,
    `password` varchar(255) NOT NULL,
    `date_created` date NOT NULL DEFAULT (curdate()),
    `is_active` tinyint(1) NOT NULL DEFAULT '1',
    `email` varchar(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `Users_UNIQUE` (`username`)
);


CREATE TABLE `roles` (
    `id` int NOT NULL AUTO_INCREMENT,
    `role` enum('ADMIN','USER') NOT NULL,
    PRIMARY KEY (`id`)
);


CREATE TABLE `users_roles` (
    `user_id` int NOT NULL,
    `role_id` int NOT NULL,
    UNIQUE KEY (`user_id`,`role_id`),
    KEY `Users_Roles_Roles_FK` (`role_id`),
    KEY `Users_Roles_Users_FK` (`user_id`),
    CONSTRAINT `Users_Roles_Roles_FK` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
    CONSTRAINT `Users_Roles_Users_FK` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
);


CREATE TABLE `blog_entries` (
    `id` int NOT NULL AUTO_INCREMENT,
    `content` text NOT NULL,
    `title` varchar(255) NOT NULL,
    `date_published` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `date_updated` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    `is_public` tinyint(1) NOT NULL,
    `author_id` int NOT NULL,
    PRIMARY KEY (`id`),
    KEY `Blog_Entry_Users_FK` (`author_id`),
    CONSTRAINT `Blog_Entry_Users_FK` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`)
);


CREATE TABLE `comments` (
    `id` int NOT NULL AUTO_INCREMENT,
    `content` varchar(500) NOT NULL,
    `date_created` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `date_updated` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
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
    `category` varchar(255) NOT NULL,
    `description` varchar(500) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `Categories_UNIQUE` (`category`)
);


CREATE TABLE `posts_categories` (
    `post_id` int NOT NULL,
    `category_id` int NOT NULL,
    UNIQUE KEY  (`post_id`,`category_id`),
    KEY `Posts_Categories_Categories_FK` (`category_id`),
    KEY `Posts_Categories_Blog_Entry_FK` (`post_id`),
    CONSTRAINT `Posts_Categories_Blog_Entry_FK` FOREIGN KEY (`post_id`) REFERENCES `blog_entries` (`id`) ON DELETE CASCADE,
    CONSTRAINT `Posts_Categories_Categories_FK` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
);
