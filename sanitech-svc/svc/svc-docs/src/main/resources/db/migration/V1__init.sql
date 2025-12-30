-- =============================================================================
-- V1__init.sql
-- Schema base per svc-docs (Sanitech)
-- =============================================================================

CREATE TABLE IF NOT EXISTS documents (
  id UUID PRIMARY KEY,
  patient_id BIGINT NOT NULL,
  uploaded_by VARCHAR(128) NOT NULL,
  department_code VARCHAR(80) NOT NULL,
  document_type VARCHAR(64) NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  content_type VARCHAR(120) NOT NULL,
  size_bytes BIGINT NOT NULL,
  checksum_sha256 VARCHAR(64) NOT NULL,
  s3_key VARCHAR(512) NOT NULL UNIQUE,
  description VARCHAR(500),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
