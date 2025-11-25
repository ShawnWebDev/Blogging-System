CREATE TABLE `Roles` (
                         `id` int NOT NULL AUTO_INCREMENT,
                         `role` enum('ADMIN','USER') NOT NULL,
                         PRIMARY KEY (`id`)
);

CREATE TABLE `Users` (
                         `id` int NOT NULL AUTO_INCREMENT,
                         `username` varchar(255) NOT NULL,
                         `password` varchar(255) NOT NULL,
                         `date_created` date NOT NULL DEFAULT (curdate()),
                         `is_active` bool NOT NULL DEFAULT '1',
                         `email` varchar(255) NOT NULL,
                         PRIMARY KEY (`id`)
);

CREATE TABLE `Users_Roles` (
                               `user_id` int NOT NULL,
                               `role_id` int NOT NULL,
                               PRIMARY KEY (`user_id`,`role_id`),
                               CONSTRAINT `Users_Roles_Roles_FK` FOREIGN KEY (`role_id`) REFERENCES `Roles` (`id`) ON DELETE CASCADE,
                               CONSTRAINT `Users_Roles_Users_FK` FOREIGN KEY (`user_id`) REFERENCES `Users` (`id`)
);

CREATE TABLE `Blog_Entries` (
                                `id` int NOT NULL AUTO_INCREMENT,
                                `content` text NOT NULL,
                                `title` varchar(255) NOT NULL,
                                `date_published` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                `date_updated` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                `is_public` bool NOT NULL,
                                `author_id` int NOT NULL,
                                PRIMARY KEY (`id`),
                                CONSTRAINT `Blog_Entry_Users_FK` FOREIGN KEY (`author_id`) REFERENCES `Users` (`id`)
);

CREATE TABLE `Categories` (
                              `id` int NOT NULL AUTO_INCREMENT,
                              `category` varchar(255) NOT NULL,
                              `description` varchar(255) NOT NULL,
                              PRIMARY KEY (`id`)
);

CREATE TABLE `Posts_Categories` (
                                    `post_id` int NOT NULL,
                                    `category_id` int NOT NULL,
                                    PRIMARY KEY (`post_id`,`category_id`),
                                    CONSTRAINT `Posts_Categories_Blog_Entry_FK` FOREIGN KEY (`post_id`) REFERENCES `Blog_Entries` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
                                    CONSTRAINT `Posts_Categories_Categories_FK` FOREIGN KEY (`category_id`) REFERENCES `Categories` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE `Comments` (
                            `id` int NOT NULL AUTO_INCREMENT,
                            `content` text NOT NULL,
                            `date_created` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            `date_updated` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            `parent_comment_id` int DEFAULT NULL,
                            `author_id` int NOT NULL,
                            `post_id` int NOT NULL,
                            PRIMARY KEY (`id`),
                            CONSTRAINT `Comments_Blog_Entries_FK` FOREIGN KEY (`post_id`) REFERENCES `Blog_Entries` (`id`) ON DELETE CASCADE,
                            CONSTRAINT `Comments_Comments_FK` FOREIGN KEY (`parent_comment_id`) REFERENCES `Comments` (`id`) ON DELETE CASCADE,
                            CONSTRAINT `Comments_Users_FK` FOREIGN KEY (`author_id`) REFERENCES `Users` (`id`)
);
