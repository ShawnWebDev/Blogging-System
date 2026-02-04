
INSERT INTO `users` (username, password, date_created, is_active, role)
VALUES
    ('TestAdmin', '$2a$10$bKQEC46DI0S.2SlYfTxLZOM9dabIOFaeQ/eOW8raycdxgopNVAc8y', '2025-10-05', TRUE, 'ADMIN'),
    ('TestUser', '$2a$10$bKQEC46DI0S.2SlYfTxLZOM9dabIOFaeQ/eOW8raycdxgopNVAc8y', '2025-10-05', TRUE, 'USER'),
    ('TestUser2', '$2a$10$bKQEC46DI0S.2SlYfTxLZOM9dabIOFaeQ/eOW8raycdxgopNVAc8y', '2025-10-15', TRUE, 'USER'),
    ('Te5tU$er', '$2a$10$bKQEC46DI0S.2SlYfTxLZOM9dabIOFaeQ/eOW8raycdxgopNVAc8y', '2026-01-03', TRUE, 'USER');


INSERT INTO `categories` (category, description)
VALUES
    ('Test Category 1', 'Just a test category 1.'),
    ('Test Category 2', 'Just a test category 2.'),
    ('Test Category 3', 'Just a test category 3.');

INSERT INTO `blog_entries` (title, date_published, date_updated, content, is_public, author_id)
VALUES
    ('Test Public Post 1', '2025-09-30', '2025-09-30','Test Post 1 - TestAdmin content is here.',TRUE, 1),
    ('Test Private Post 2', '2025-10-02', '2025-10-03', 'Test Private Post 2 - TestAdmin content is here.',FALSE, 1),
    ('Test Public Post 2', '2025-10-03', '2025-10-05','Test Public Post 2 - TestUser content is here.',TRUE, 2),
    ('Test Public Post 3', '2025-10-03', '2025-12-31','Test Public Post 3 - TestUser2 content is here.',TRUE, 3);

INSERT INTO `posts_categories` (post_id, category_id)
VALUES
    (1,1),
    (1,2),
    (2,1),
    (2,2),
    (3,1),
    (3,2),
    (3,3),
    (4,3);

INSERT INTO `comments` (content, date_created, parent_comment_id, author_id, post_id)
VALUES
    ('Test Comment on Test Post 1', '2025-10-01', null, 1, 1),
    ('Test Reply 1 to Comment 1 on Test Post 1', '2025-10-02', 1, 2, 1),
    ('Test Reply 2 to Comment 1 on Test Post 1', '2025-10-05', 1, 3, 1),
    ('Test Comment on Test Post 3', '2025-10-01', null, 1, 3),
    ('Test Comment 2 on Test Post 1', '2025-10-01', null, 2, 1),
    ('Test Reply 1 to Comment 2 on Test Post 1', '2025-10-01', 5, 2, 1),
    ('Test Comment 3 on Test Post 1', '2025-10-01', null, 3, 1),
    ('Test Reply 1 to Reply 1 of Comment 1 on Test Post 1', '2025-10-01', 2, 3, 1);