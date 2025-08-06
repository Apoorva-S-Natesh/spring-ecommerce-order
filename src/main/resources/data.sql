INSERT INTO products (name, price, quantity, image_url) VALUES
                                                            ('Car', 1000.0, 5, 'https://images.unsplash.com/photo-1494905998402-395d579af36f?w=400&h=400&fit=crop'),
                                                            ('Bike', 200.0, 10, 'https://images.unsplash.com/photo-1571068316344-75bc76f77890?w=400&h=400&fit=crop'),
                                                            ('Truck', 30000.0, 2, 'https://images.unsplash.com/photo-1586190848861-99aa4a171e90?w=400&h=400&fit=crop'),
                                                            ('Laptop', 1500.0, 15, 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=400&h=400&fit=crop'),
                                                            ('Phone', 800.0, 25, 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400&h=400&fit=crop');

INSERT INTO product_options (name, quantity, product_id) VALUES
                                                             ('Blue', 5, 1),
                                                             ('Red', 10, 2),
                                                             ('Yellow', 10, 2);

--  admin pw: secret
INSERT INTO members (id, email, password, name, role) VALUES
    (1, 'test@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Test User', 'USER'),
    (2, 'admin@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Admin User', 'ADMIN'),
    (3, 'test2@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Test User2', 'USER');

INSERT INTO carts (member_id, quantity, updated_at) VALUES
    (1, 1, TIMESTAMP '2025-07-25 08:00:00.000'),
    (3, 2, TIMESTAMP '2025-08-05 08:00:00.000');

INSERT INTO cart_items (cart_id, product_option_id, quantity, updated_at) VALUES
  (1, 1, 1,TIMESTAMP '2025-07-25 08:00:00.000'),
  (2, 2, 2,TIMESTAMP '2025-08-05 08:00:00.000'),
  (1, 3, 2,TIMESTAMP '2025-08-05 08:00:00.000');

INSERT INTO cart_statistics (cart_item_id, cart_id, product_option_id, quantity, added_at) VALUES
  (1, 1, 1, 1, TIMESTAMP '2025-07-25 08:00:00.000'),
  (2, 2, 2, 2, TIMESTAMP '2025-08-05 08:00:00.000');
