-- Create password_reset_tokens table

CREATE TABLE public.password_reset_tokens (
    id serial4 NOT NULL, -- Primary key - unique token identifier
    user_id int4 NOT NULL, -- Foreign key to user account
    token varchar(255) NOT NULL, -- Secure random token for password reset validation
    created_at timestamptz(6) DEFAULT now() NULL, -- Timestamp when token was created
    expires_at timestamptz(6) NOT NULL, -- Timestamp when token becomes invalid
    used_at timestamptz(6) NULL, -- Timestamp when token was used (null if not used)
    CONSTRAINT password_reset_tokens_pkey PRIMARY KEY (id),
    CONSTRAINT password_reset_tokens_token_key UNIQUE (token),
    CONSTRAINT password_reset_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);

CREATE INDEX idx_password_reset_tokens_token ON public.password_reset_tokens USING btree (token);
CREATE INDEX idx_password_reset_tokens_user_id ON public.password_reset_tokens USING btree (user_id);

COMMENT ON TABLE public.password_reset_tokens IS 'Stores password reset tokens for forgot password functionality';

-- Column comments

COMMENT ON COLUMN public.password_reset_tokens.id IS 'Primary key - unique token identifier';
COMMENT ON COLUMN public.password_reset_tokens.user_id IS 'Foreign key to user account';
COMMENT ON COLUMN public.password_reset_tokens.token IS 'Secure random token for password reset validation';
COMMENT ON COLUMN public.password_reset_tokens.created_at IS 'Timestamp when token was created';
COMMENT ON COLUMN public.password_reset_tokens.expires_at IS 'Timestamp when token becomes invalid';
COMMENT ON COLUMN public.password_reset_tokens.used_at IS 'Timestamp when token was used (null if not used)';