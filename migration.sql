-- Run this manually if Hibernate ddl-auto=update fails to add these columns.
-- Connect to your PostgreSQL database and execute:

ALTER TABLE users ADD COLUMN IF NOT EXISTS enabled BOOLEAN DEFAULT TRUE;
ALTER TABLE search_logs ADD COLUMN IF NOT EXISTS user_id UUID;
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS pay_supplier VARCHAR(255);
ALTER TABLE shipment_stages ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255) DEFAULT 'system';
ALTER TABLE shipment_stages ADD COLUMN IF NOT EXISTS updated_by_type VARCHAR(20) DEFAULT 'SYSTEM';
ALTER TABLE shipment_stages ADD COLUMN IF NOT EXISTS attachment_url TEXT;

CREATE TABLE IF NOT EXISTS shipment_documents (
    id UUID PRIMARY KEY,
    shipment_id UUID NOT NULL REFERENCES shipments(shipment_id),
    document_type VARCHAR(50) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_url TEXT NOT NULL,
    file_size BIGINT NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT NOW(),
    uploaded_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS shipment_checkpoints (
    id UUID PRIMARY KEY,
    shipment_id UUID NOT NULL REFERENCES shipments(shipment_id),
    location VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS shipment_event_logs (
    id UUID PRIMARY KEY,
    shipment_id UUID NOT NULL REFERENCES shipments(shipment_id),
    event_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    performed_by VARCHAR(255)
);
