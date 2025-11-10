INSERT INTO Roles(role)
VALUES
    ('USER'),
    ('ADMIN');

INSERT INTO Users(username, password, email, date_created, is_active)
VALUES
    ('TestAdmin', '$2a$10$bKQEC46DI0S.2SlYfTxLZOM9dabIOFaeQ/eOW8raycdxgopNVAc8y', 'TestEmail@email.com', '2025-10-05', TRUE),
    ('TestUser', '$2a$10$bKQEC46DI0S.2SlYfTxLZOM9dabIOFaeQ/eOW8raycdxgopNVAc8y', 'TestEmail@email.com', '2025-10-05', TRUE),
    ('TestUser2', '$2a$10$bKQEC46DI0S.2SlYfTxLZOM9dabIOFaeQ/eOW8raycdxgopNVAc8y', 'TestEmail@email.com', '2025-10-15', TRUE);

INSERT INTO Users_Roles(user_id, role_id)
VALUES
    (1, 1),
    (1, 2),
    (2, 1),
    (3, 1);

INSERT INTO Categories(category, description)
VALUES
    ('Test Category 1', 'Just a test category.'),
    ('Test Category 2', 'Just a test category 2.');

INSERT INTO Blog_Entries(title, date_published, date_updated, content, is_public, author_id)
VALUES
    ('Test Post 1', '2025-09-30', '2025-09-30','Test Post 1 - TestAdmin content is here.',TRUE, 1),
    ('Test Post 2', '2025-10-02', '2025-10-03', 'Test Post 2 - TestAdmin content is here.',FALSE, 1),
    ('Test Post 3', '2025-10-03', '2025-10-05','Test Post 3 - TestUser content is here.',TRUE, 2);

INSERT INTO Posts_Categories(post_id, category_id)
VALUES
    (1,1),
    (1,2),
    (2,1),
    (3,1);

INSERT INTO Comments(content, date_created, parent_comment_id, author_id, post_id)
VALUES
    ('Test Comment on Test Post 1', '2025-10-01', null, 1, 1),
    ('Test Reply to Comment 1 on Test Post 1', '2025-10-01', 1, 2, 1),
    ('Test Comment on Test Post 3', '2025-10-01', null, 1, 3);