DELETE FROM cart;
DELETE FROM members;
DELETE FROM products;

ALTER TABLE cart ALTER COLUMN id RESTART WITH 1;
ALTER TABLE members ALTER COLUMN id RESTART WITH 1;
ALTER TABLE products ALTER COLUMN id RESTART WITH 1;

INSERT INTO products (id, name, price, imageUrl)
VALUES (1, 'Test Product', 100.0, 'http://example.com/test.jpg');

INSERT INTO members (id, email, password, name, role)
VALUES (1, 'test@email.com', 'password', 'Test User', 'USER');
