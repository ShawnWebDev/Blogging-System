
INSERT INTO `users` (username, password, date_created, is_active, role, email)
VALUES
    ('TestAdmin', '$2a$10$bKQEC46DI0S.2SlYfTxLZOM9dabIOFaeQ/eOW8raycdxgopNVAc8y', '2025-10-05', TRUE, 'ADMIN', 'TestAdmin@email.com'),
    ('TestUser', '$2a$10$bKQEC46DI0S.2SlYfTxLZOM9dabIOFaeQ/eOW8raycdxgopNVAc8y', '2025-10-05', TRUE, 'USER', 'TestUser@email.com'),
    ('TestUser2', '$2a$10$bKQEC46DI0S.2SlYfTxLZOM9dabIOFaeQ/eOW8raycdxgopNVAc8y', '2025-10-15', TRUE, 'USER', 'TestUser2@email.com'),
    ('Te5tU$er', '$2a$10$bKQEC46DI0S.2SlYfTxLZOM9dabIOFaeQ/eOW8raycdxgopNVAc8y', '2026-01-03', FALSE, 'USER', 'Te5tU$er@email.com');

INSERT INTO `categories` (category_name, description)
VALUES
    ('Test Category 1', 'Just a test category 1.'),
    ('Test Category 2', 'Just a test category 2.'),
    ('Test Category 3', 'Just a test category 3.'),
    ('Portfolio', 'Portfolio category.');

INSERT INTO `blog_entries` (title, description, content, slug, thumbnail_url, thumbnail_alt, in_progress, code_url, demo_url, is_portfolio)
VALUES
    ('Test Post 1', 'Test Description 1','##Test Post 1\n\nTestAdmin content is here.', 'test-post-1', 'url', 'alt', 0, null, null, 0),
    ('Test Post 2', 'Test Description 2','##Test Post 2\n\nTestAdmin content is here.', 'test-post-2', 'url', 'alt', 0, null, null, 0),
    ('Test Post 3', 'Test Description 3', '##Test Post 3\n\nTestAdmin content is here.',  'test-post-3', 'url', 'alt', 0, null, null, 0),
    ('Test Post 4', 'Test Description 4', '##Test Post 4\n\nTestAdmin content is here.',  'test-post-4', 'url', 'alt', 0, null, null, 0),
    ('Test Post 5', 'Test Description 5', '##Test Post 5\n\nTestAdmin content is here.', 'test-post-5', 'url', 'alt', 0, null, null, 0),
    ('Test Post 6', 'Test Description 6', '##Test Post 5\n\nTestAdmin content is here, unfinished...', 'test-post-6', 'url', 'alt', 1, null, null, 0);

INSERT INTO `posts_categories` (post_id, category_id)
VALUES
    (1,1),
    (1,2),
    (2,1),
    (2,2),
    (3,1),
    (3,2),
    (3,3),
    (4,3),
    (5,1),
    (6,1),
    (6,3);

INSERT INTO `comments` (content, created_at, updated_at, parent_comment_id, author_id, post_id)
VALUES
    ('Test Comment on Test Post 1', '2025-10-01', '2025-10-01', null, 1, 1),
    ('Test Reply 1 to Comment 1 on Test Post 1', '2025-10-02', '2025-10-02',1, 2, 1),
    ('Test Reply 2 to Comment 1 on Test Post 1', '2025-10-05', '2025-10-05',1, 3, 1),
    ('Test Comment on Test Post 3', '2025-10-01', '2025-10-01', null, 1, 3),
    ('Test Comment 2 on Test Post 1', '2025-10-01', '2025-10-01', null, 2, 1),
    ('Test Reply 1 to Comment 2 on Test Post 1', '2025-10-01', '2025-10-01',5, 2, 1),
    ('Test Comment 3 on Test Post 1', '2025-10-01', '2025-10-01', null, 3, 1),
    ('Test Reply 1 to Reply 1 of Comment 1 on Test Post 1', '2025-10-01', '2025-10-01',2, 3, 1);