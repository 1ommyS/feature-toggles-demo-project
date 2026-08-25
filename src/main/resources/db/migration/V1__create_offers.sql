CREATE TABLE offers (
    id UUID PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    cashback_percent NUMERIC(5, 2) NOT NULL CHECK (cashback_percent >= 0 AND cashback_percent <= 100),
    priority INTEGER NOT NULL DEFAULT 0 CHECK (priority >= 0),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_offers_active_created_at
    ON offers (active, created_at DESC);

INSERT INTO offers (id, title, cashback_percent, priority, active, created_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'Coffee cashback', 5.00, 10, TRUE, NOW() - INTERVAL '3 hours'),
    ('22222222-2222-2222-2222-222222222222', 'Travel cashback', 10.00, 100, TRUE, NOW() - INTERVAL '2 hours'),
    ('33333333-3333-3333-3333-333333333333', 'Groceries cashback', 7.50, 50, TRUE, NOW() - INTERVAL '1 hour');
