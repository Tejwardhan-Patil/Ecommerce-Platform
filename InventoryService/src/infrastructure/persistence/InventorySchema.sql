-- Schema for Inventory Service
-- Create tables for products, stock levels, categories, suppliers, and inventory movements

CREATE TABLE categories (
    category_id SERIAL PRIMARY KEY,
    category_name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE suppliers (
    supplier_id SERIAL PRIMARY KEY,
    supplier_name VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products (
    product_id SERIAL PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    sku VARCHAR(100) UNIQUE NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    category_id INT,
    supplier_id INT,
    description TEXT,
    weight DECIMAL(10, 3),
    dimensions VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories (category_id),
    FOREIGN KEY (supplier_id) REFERENCES suppliers (supplier_id)
);

CREATE TABLE stock_levels (
    stock_level_id SERIAL PRIMARY KEY,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    warehouse_location VARCHAR(255),
    minimum_required INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products (product_id) ON DELETE CASCADE
);

CREATE TABLE inventory_movements (
    movement_id SERIAL PRIMARY KEY,
    product_id INT NOT NULL,
    stock_level_id INT,
    movement_type VARCHAR(50) NOT NULL CHECK (movement_type IN ('IN', 'OUT', 'ADJUSTMENT')),
    movement_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    quantity INT NOT NULL,
    reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products (product_id) ON DELETE CASCADE,
    FOREIGN KEY (stock_level_id) REFERENCES stock_levels (stock_level_id) ON DELETE SET NULL
);

CREATE TABLE inventory_audit_logs (
    audit_log_id SERIAL PRIMARY KEY,
    movement_id INT NOT NULL,
    user_id INT NOT NULL,
    change_description TEXT NOT NULL,
    change_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (movement_id) REFERENCES inventory_movements (movement_id) ON DELETE CASCADE
);

CREATE INDEX idx_product_name ON products (product_name);
CREATE INDEX idx_sku ON products (sku);
CREATE INDEX idx_category ON products (category_id);
CREATE INDEX idx_supplier ON products (supplier_id);

CREATE INDEX idx_product_id_stock_levels ON stock_levels (product_id);
CREATE INDEX idx_stock_warehouse_location ON stock_levels (warehouse_location);

CREATE INDEX idx_movement_date ON inventory_movements (movement_date);
CREATE INDEX idx_movement_type ON inventory_movements (movement_type);

-- Triggers for automatic stock adjustment

CREATE OR REPLACE FUNCTION adjust_stock_after_movement()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' OR TG_OP = 'UPDATE' THEN
        IF NEW.movement_type = 'IN' THEN
            UPDATE stock_levels
            SET quantity = quantity + NEW.quantity,
                updated_at = CURRENT_TIMESTAMP
            WHERE stock_level_id = NEW.stock_level_id;
        ELSIF NEW.movement_type = 'OUT' THEN
            UPDATE stock_levels
            SET quantity = quantity - NEW.quantity,
                updated_at = CURRENT_TIMESTAMP
            WHERE stock_level_id = NEW.stock_level_id;
        END IF;
    ELSIF TG_OP = 'DELETE' THEN
        -- Handle delete cases
        RAISE NOTICE 'Inventory movement deletion is not allowed.';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_adjust_stock_after_movement
AFTER INSERT OR UPDATE ON inventory_movements
FOR EACH ROW
EXECUTE FUNCTION adjust_stock_after_movement();

-- Stock replenishment view
CREATE VIEW stock_replenishment_view AS
SELECT
    p.product_name,
    s.quantity,
    s.minimum_required,
    (s.minimum_required - s.quantity) AS reorder_amount,
    p.sku,
    s.warehouse_location
FROM stock_levels s
JOIN products p ON p.product_id = s.product_id
WHERE s.quantity < s.minimum_required;

-- Stored procedure for bulk stock update
CREATE OR REPLACE PROCEDURE bulk_stock_update(
    IN product_id INT,
    IN new_quantity INT
)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE stock_levels
    SET quantity = new_quantity,
        updated_at = CURRENT_TIMESTAMP
    WHERE product_id = product_id;
END;
$$;

-- Views for product catalog
CREATE VIEW product_catalog AS
SELECT
    p.product_name,
    p.sku,
    c.category_name,
    s.supplier_name,
    p.price,
    p.weight,
    p.dimensions
FROM products p
JOIN categories c ON p.category_id = c.category_id
JOIN suppliers s ON p.supplier_id = s.supplier_id;

-- Data insertion for testing

INSERT INTO categories (category_name, description) VALUES ('Electronics', 'Electronic items and gadgets.');
INSERT INTO categories (category_name, description) VALUES ('Furniture', 'Home and office furniture.');

INSERT INTO suppliers (supplier_name, contact_email, phone_number, address)
VALUES ('Best Supplier Inc.', 'contact@bestsupplier.com', '+1234567890', '1234 Elm Street');

INSERT INTO products (product_name, sku, price, category_id, supplier_id, description, weight, dimensions)
VALUES ('Smartphone XYZ', 'SKU12345', 699.99, 1, 1, 'Latest model smartphone with 128GB storage.', 0.175, '6.1 x 2.8 x 0.3 inches');

INSERT INTO stock_levels (product_id, quantity, warehouse_location, minimum_required)
VALUES (1, 150, 'WH-A1', 50);

INSERT INTO inventory_movements (product_id, stock_level_id, movement_type, quantity, reason)
VALUES (1, 1, 'IN', 150, 'Initial stock load');