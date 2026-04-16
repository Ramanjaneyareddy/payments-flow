CREATE TABLE payments (
    id               BINARY(16)     NOT NULL,
    sender_id        VARCHAR(50)    NOT NULL,
    receiver_id      VARCHAR(50)    NOT NULL,
    amount           DECIMAL(19,4)  NOT NULL,
    currency         VARCHAR(3)     NOT NULL,
    status           VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    transaction_id   VARCHAR(100)   UNIQUE NULL,
    rejection_reason VARCHAR(500)   NULL,
    created_at       DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_payments PRIMARY KEY (id),
    CONSTRAINT chk_status CHECK (status IN ('PENDING','FRAUD_CHECK','APPROVED','REJECTED','COMPLETED','FAILED','REVIEW'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Core payments table for PayFlow platform';

CREATE INDEX idx_payments_sender_id   ON payments (sender_id);
CREATE INDEX idx_payments_receiver_id ON payments (receiver_id);
CREATE INDEX idx_payments_status      ON payments (status);
CREATE INDEX idx_payments_created_at  ON payments (created_at DESC);