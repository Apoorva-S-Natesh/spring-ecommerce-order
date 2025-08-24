-- Drop existing tables to ensure a clean slate for schema creation.
-- The order is important due to foreign key constraints.
DROP TABLE IF EXISTS cart_statistics;
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS carts;
DROP TABLE IF EXISTS members;
DROP TABLE IF EXISTS product_options;
DROP TABLE IF EXISTS products;

-- -----------------------------------------------------
-- Table `products`
-- Corresponds to the Product.kt entity.
-- -----------------------------------------------------
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DOUBLE NOT NULL,
    quantity INT NOT NULL,
    image_url VARCHAR(255) NOT NULL
);

-- -----------------------------------------------------
-- Table `product_options`
-- Corresponds to the ProductOption.kt entity.
-- -----------------------------------------------------
CREATE TABLE product_options (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    product_id BIGINT NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- -----------------------------------------------------
-- Table `members`
-- Corresponds to the Member.kt entity.
-- -----------------------------------------------------
CREATE TABLE members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL
);

-- -----------------------------------------------------
-- Table `carts`
-- Corresponds to the Cart.kt entity.
-- -----------------------------------------------------
CREATE TABLE carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quantity INT NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    member_id BIGINT NOT NULL UNIQUE,
    FOREIGN KEY (member_id) REFERENCES members(id)
);

-- -----------------------------------------------------
-- Table `cart_items`
-- Corresponds to the CartItem.kt entity.
-- -----------------------------------------------------
CREATE TABLE cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_option_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (cart_id) REFERENCES carts(id),
    FOREIGN KEY (product_option_id) REFERENCES product_options(id),
    UNIQUE (cart_id, product_option_id)
);

-- -----------------------------------------------------
-- Table `payments`
-- Corresponds to the Payment.kt entity.
-- -----------------------------------------------------
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    checkout_session_id VARCHAR(255),
    amount BIGINT,
    currency VARCHAR(255),
    status VARCHAR(255),
    payment_method VARCHAR(255),
    created_at TIMESTAMP,
    last_payment_error VARCHAR(255)
);

-- -----------------------------------------------------
-- Table `orders`
-- Corresponds to the Order.kt entity.
-- -----------------------------------------------------
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    order_date TIMESTAMP NOT NULL,
    payment_id BIGINT UNIQUE,
    FOREIGN KEY (member_id) REFERENCES members(id),
    FOREIGN KEY (payment_id) REFERENCES payments(id)
);

-- -----------------------------------------------------
-- Table `order_items`
-- Corresponds to the OrderItem.kt entity.
-- -----------------------------------------------------
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_option_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DOUBLE NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_option_id) REFERENCES product_options(id)
);

-- -----------------------------------------------------
-- Table `cart_statistics`
-- Corresponds to the CartStatistics.kt entity.
-- -----------------------------------------------------
CREATE TABLE cart_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_item_id BIGINT NOT NULL,
    cart_id BIGINT NOT NULL,
    product_option_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    added_at TIMESTAMP NOT NULL,
    FOREIGN KEY (cart_id) REFERENCES carts(id),
    FOREIGN KEY (product_option_id) REFERENCES product_options(id)
);
