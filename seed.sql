-- =============================================================
-- ImportEase — Local Test Data Seeder
-- Run: psql -U postgres -h localhost -d importease -f seed.sql
-- =============================================================

-- Enable pg_trgm extension (required for similarity search)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ── Suppliers ────────────────────────────────────────────────
INSERT INTO suppliers (name, email, phone, address, shipping_origin, subscription_tier)
VALUES
  ('Kumasi Fresh Exports', 'info@kumasifresh.com', '+233 54 123 4567', 'Kumasi, Ashanti Region', 'Tema', 'PREMIUM'),
  ('Accra Textiles Ltd', 'sales@accratextiles.com', '+233 24 987 6543', 'Accra, Greater Accra', 'Tema', 'FREE'),
  ('Northern Grains Cooperative', 'admin@northerngrains.com', '+233 55 567 8901', 'Tamale, Northern Region', 'Takoradi', 'PREMIUM'),
  ('Volta Crafts & Goods', 'hello@voltacrafts.com', '+233 20 345 6789', 'Ho, Volta Region', 'Tema', 'FREE'),
  ('Western Cocoa Traders', 'info@westerncocoa.com', '+233 50 111 2233', 'Takoradi, Western Region', 'Takoradi', 'PREMIUM');

-- ── Products ─────────────────────────────────────────────────
INSERT INTO products (name, description, price, quantity, image_url, supplier_id)
VALUES
  -- Kumasi Fresh Exports (supplier_id = 1)
  ('Premium Cocoa Beans', 'High-grade fermented cocoa beans sourced directly from smallholder farmers in Ashanti Region. Ideal for chocolate production.', 45.00, 5000, 'https://images.unsplash.com/photo-1600778094833-4d4c888b18d0?w=400', 1),
  ('Organic Shea Butter', 'Unrefined, cold-pressed shea butter from Northern Ghana. 100% natural, rich in vitamins A and E.', 12.50, 2000, 'https://images.unsplash.com/photo-1611930022073-b7a4ba5fcccd?w=400', 1),
  ('Fresh Pineapples', 'Sweet sugarloaf pineapples harvested at peak ripeness. Export quality, 6-8 pieces per crate.', 28.00, 1000, 'https://images.unsplash.com/photo-1550258987-190a2d41a8ba?w=400', 1),

  -- Accra Textiles Ltd (supplier_id = 2)
  ('Kente Cloth (Handwoven)', 'Authentic handwoven Kente cloth from Volta Region. Each piece is unique. Width: 120cm, various patterns available.', 85.00, 150, 'https://images.unsplash.com/photo-1597047084897-51e81819a0cf?w=400', 2),
  ('African Print Cotton Fabric', 'High-quality 100% cotton Ankara fabric. Vibrant patterns, 6-yard length. Perfect for tailoring.', 18.00, 800, 'https://images.unsplash.com/photo-1578587018452-892baced9f38?w=400', 2),

  -- Northern Grains Cooperative (supplier_id = 3)
  ('Organic Long-Grain Rice', 'Premium perfumed long-grain rice grown in the fertile fields of Northern Ghana. 50kg bag.', 65.00, 300, 'https://images.unsplash.com/photo-1586201375761-83865001e31c?w=400', 3),
  ('Sorghum Grain', 'Red sorghum, high-yield variety. Suitable for brewing, flour, and animal feed. 50kg bag.', 32.00, 400, 'https://images.unsplash.com/photo-1594377708097-92f4c92a59fe?w=400', 3),

  -- Volta Crafts & Goods (supplier_id = 4)
  ('Handmade Beaded Jewelry', 'Traditional Ghanaian beadwork — necklaces and bracelets made from recycled glass beads.', 15.00, 500, 'https://images.unsplash.com/photo-1609081219090-a6d81d3085bf?w=400', 4),
  ('Wooden Mask (Carved)', 'Hand-carved ceremonial mask from Volta Region. Each mask tells a story. Approx 30cm height.', 55.00, 60, 'https://images.unsplash.com/photo-1567095761054-7a02e69e5c43?w=400', 4),

  -- Western Cocoa Traders (supplier_id = 5)
  ('Fair Trade Cocoa Butter', 'Pure cocoa butter pressed from premium West African cocoa beans. Food-grade, 1kg block.', 22.00, 1000, 'https://images.unsplash.com/photo-1600778094833-4d4c888b18d0?w=400', 5),
  ('Dried Ginger Root', 'Sun-dried organic ginger root. Whole pieces, export quality. 25kg bag.', 38.00, 250, 'https://images.unsplash.com/photo-1615485500834-bc10199a0f8e?w=400', 5),
  ('Hibiscus Flower (Dried)', 'Dried hibiscus flowers (Zobo). Rich deep red color, perfect for teas and beverages. 10kg bag.', 16.00, 600, 'https://images.unsplash.com/photo-1595853035070-59a39fe84de3?w=400', 5);

-- ── Reviews ──────────────────────────────────────────────────
INSERT INTO reviews (product_id, author_name, rating, comment, created_at)
VALUES
  (1, 'John D.', 5, 'Excellent quality cocoa beans. My chocolate batch turned out fantastic!', NOW() - INTERVAL '10 days'),
  (1, 'Maria K.', 4, 'Good beans, consistent quality. Slight delay in shipping but worth it.', NOW() - INTERVAL '5 days'),
  (2, 'Aisha M.', 5, 'Best shea butter I have ever used. My skin feels amazing.', NOW() - INTERVAL '3 days'),
  (4, 'Kwame A.', 5, 'Stunning Kente cloth. The craftsmanship is outstanding.', NOW() - INTERVAL '7 days'),
  (5, 'Lisa R.', 4, 'Beautiful Ankara prints. Colors are vibrant and fabric feels premium.', NOW() - INTERVAL '2 days'),
  (6, 'Samuel O.', 5, 'The rice is aromatic and cooks perfectly. Will order again.', NOW() - INTERVAL '14 days'),
  (9, 'Esi Y.', 5, 'Beautiful mask! It adds such character to my living room.', NOW() - INTERVAL '1 day'),
  (11, 'David W.', 4, 'Good quality ginger. Strong aroma and flavor.', NOW() - INTERVAL '6 days');
