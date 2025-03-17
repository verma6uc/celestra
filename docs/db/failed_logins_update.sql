-- Add email column to failed_logins table
ALTER TABLE public.failed_logins ADD COLUMN email varchar(255) NULL;

-- Add comment for the new column
COMMENT ON COLUMN public.failed_logins.email IS 'Email address used in the login attempt (especially when user_id is unknown)';

-- Create index on email for faster lookups
CREATE INDEX idx_failed_logins_email ON public.failed_logins USING btree (email);