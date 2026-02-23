
INSERT INTO `users` (username, password, date_created, is_active, role)
VALUES
    ('TestAdmin', '$2a$10$bKQEC46DI0S.2SlYfTxLZOM9dabIOFaeQ/eOW8raycdxgopNVAc8y', '2025-10-05', TRUE, 'ADMIN'),
    ('TestUser', '$2a$10$bKQEC46DI0S.2SlYfTxLZOM9dabIOFaeQ/eOW8raycdxgopNVAc8y', '2025-10-05', TRUE, 'USER'),
    ('TestUser2', '$2a$10$bKQEC46DI0S.2SlYfTxLZOM9dabIOFaeQ/eOW8raycdxgopNVAc8y', '2025-10-15', TRUE, 'USER'),
    ('Te5tU$er', '$2a$10$bKQEC46DI0S.2SlYfTxLZOM9dabIOFaeQ/eOW8raycdxgopNVAc8y', '2026-01-03', TRUE, 'USER');


INSERT INTO `categories` (category_name, description)
VALUES
    ('Test Category 1', 'Just a test category 1.'),
    ('Test Category 2', 'Just a test category 2.'),
    ('Test Category 3', 'Just a test category 3.');

INSERT INTO `blog_entries` (title, description, created_at, updated_at, content, slug, thumbnail_url, thumbnail_alt)
VALUES
    ('Test Post 1', 'Test Description 1', '2025-09-30', '2025-09-30','[{"text":"Test Post 1 - TestAdmin content is here.", "type":"PARAGRAPH"}]', 'test-post-1', '', ''),
    ('Test Post 2', 'Test Description 2', '2025-10-02', '2025-10-03', '[{"text":"Test Post 2 - TestAdmin content is here.", "type":"PARAGRAPH"}]', 'test-post-2', '', ''),
    ('Test Post 3', 'Test Description 3', '2025-10-03', '2025-10-05','[{"text":"Test Post 3 - TestAdmin content is here.", "type":"PARAGRAPH"}]',  'test-post-3', '', ''),
    ('Test Post 4', 'Test Description 4', '2025-10-03', '2025-12-31','[{"text":"Test Post 4 - TestAdmin content is here.", "type":"PARAGRAPH"}]',  'test-post-4', '', ''),
    ('Test Post 5', 'Test Description 5', '2026-02-06', null,'[{"text":"Test Post 5 - TestAdmin content is here.", "type":"PARAGRAPH"}]', 'test-post-5', '', '');

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
    (5,1);

INSERT INTO `comments` (content, created_at, updated_at, parent_comment_id, author_id, post_id)
VALUES
    ('Test Comment on Test Post 1', '2025-10-01', null, null, 1, 1),
    ('Test Reply 1 to Comment 1 on Test Post 1', '2025-10-02', null,1, 2, 1),
    ('Test Reply 2 to Comment 1 on Test Post 1', '2025-10-05', null,1, 3, 1),
    ('Test Comment on Test Post 3', '2025-10-01', null, null, 1, 3),
    ('Test Comment 2 on Test Post 1', '2025-10-01', null, null, 2, 1),
    ('Test Reply 1 to Comment 2 on Test Post 1', '2025-10-01', null,5, 2, 1),
    ('Test Comment 3 on Test Post 1', '2025-10-01', null, null, 3, 1),
    ('Test Reply 1 to Reply 1 of Comment 1 on Test Post 1', '2025-10-01', null,2, 3, 1);