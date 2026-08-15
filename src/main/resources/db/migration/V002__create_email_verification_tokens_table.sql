CREATE TABLE email_verification_tokens (
    token varchar PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES users(id),
    expires_at timestamptz NOT NULL,
    consumed_at timestamptz
);

CREATE INDEX idx_email_verification_tokens_user_id ON email_verification_tokens (user_id);
