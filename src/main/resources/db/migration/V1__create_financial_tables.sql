CREATE TYPE currency_type AS ENUM ('POLYCHAIN_COIN');

CREATE TYPE wallet_type AS ENUM ('USER', 'PLATFORM');

CREATE TYPE ledger_entry_type AS ENUM (
    'DEPOSIT',
    'WITHDRAWAL',
    'BET_PLACED',   -- user → platform when bet placed
    'BET_WIN',      -- platform → user when wager won
    'BET_REFUND'    -- platform → user when wager cancelled
);

CREATE TABLE wallets (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     TEXT        NOT NULL,
    wallet_type wallet_type NOT NULL DEFAULT 'USER',
    currency    currency_type NOT NULL,
    available   BIGINT      NOT NULL DEFAULT 0 CHECK (available >= 0),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, currency)
);

CREATE TABLE ledger_entries (
    id          UUID              PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id   UUID              NOT NULL REFERENCES wallets(id),
    type        ledger_entry_type NOT NULL,
    amount      BIGINT            NOT NULL CHECK (amount > 0),
    ref_id      TEXT,
    created_at  TIMESTAMPTZ       NOT NULL DEFAULT now()
);

CREATE INDEX idx_ledger_wallet_id ON ledger_entries(wallet_id);
CREATE INDEX idx_ledger_ref_id    ON ledger_entries(ref_id);
CREATE INDEX idx_wallets_user_id  ON wallets(user_id);

INSERT INTO wallets (user_id, wallet_type, currency, available)
VALUES
    ('platform', 'PLATFORM', 'POLYCHAIN_COIN', 0),
    ('platform_0', 'PLATFORM', 'POLYCHAIN_COIN', 0),
    ('platform_1', 'PLATFORM', 'POLYCHAIN_COIN', 0),
    ('platform_2', 'PLATFORM', 'POLYCHAIN_COIN', 0),
    ('platform_3', 'PLATFORM', 'POLYCHAIN_COIN', 0),
    ('platform_4', 'PLATFORM', 'POLYCHAIN_COIN', 0),
    ('platform_5', 'PLATFORM', 'POLYCHAIN_COIN', 0),
    ('platform_6', 'PLATFORM', 'POLYCHAIN_COIN', 0),
    ('platform_7', 'PLATFORM', 'POLYCHAIN_COIN', 0),
    ('platform_8', 'PLATFORM', 'POLYCHAIN_COIN', 0),
    ('platform_9', 'PLATFORM', 'POLYCHAIN_COIN', 0);