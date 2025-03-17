-- DROP TYPE public.agent_status;

CREATE TYPE public.agent_status AS ENUM (
	'ACTIVE',
	'DISABLED',
	'ARCHIVED',
	'DRAFT');

COMMENT ON TYPE public.agent_status IS 'Defines the current operational state of an agent';

-- DROP TYPE public.audit_event_type;

CREATE TYPE public.audit_event_type AS ENUM (
	'FAILED_LOGIN',
	'SUCCESSFUL_LOGIN',
	'SESSION_STARTED',
	'SESSION_ENDED',
	'ROLE_ASSIGNMENT_CHANGE',
	'CONFIGURATION_UPDATE',
	'DATA_EXPORT',
	'OTHER');

COMMENT ON TYPE public.audit_event_type IS 'Categorizes different types of security and audit events';

-- DROP TYPE public.company_size;

CREATE TYPE public.company_size AS ENUM (
	'SMALL',
	'MEDIUM',
	'LARGE',
	'ENTERPRISE');

COMMENT ON TYPE public.company_size IS 'Categorizes companies by their approximate number of employees';

-- DROP TYPE public.company_status;

CREATE TYPE public.company_status AS ENUM (
	'ACTIVE',
	'SUSPENDED',
	'ARCHIVED');

COMMENT ON TYPE public.company_status IS 'Defines the current state of a company account';

-- DROP TYPE public.company_vertical;

CREATE TYPE public.company_vertical AS ENUM (
	'TECH',
	'PHARMACEUTICAL',
	'FINANCE',
	'RETAIL',
	'OTHER');

COMMENT ON TYPE public.company_vertical IS 'Categorizes companies by their industry sector';

-- DROP TYPE public.invitation_status;

CREATE TYPE public.invitation_status AS ENUM (
	'PENDING',
	'SENT',
	'EXPIRED',
	'CANCELLED',
	'ACCEPTED');

COMMENT ON TYPE public.invitation_status IS 'Tracks the current state of an invitation';

-- DROP TYPE public.knowledge_base_status;

CREATE TYPE public.knowledge_base_status AS ENUM (
	'ACTIVE',
	'BUILDING',
	'DISABLED',
	'ARCHIVED',
	'DRAFT');

COMMENT ON TYPE public.knowledge_base_status IS 'Defines the current operational state of a knowledge base';

-- DROP TYPE public.notification_delivery_method;

CREATE TYPE public.notification_delivery_method AS ENUM (
	'IN_APP',
	'EMAIL',
	'SMS',
	'WEBHOOK',
	'PUSH');

COMMENT ON TYPE public.notification_delivery_method IS 'Defines how notifications are delivered to users';

-- DROP TYPE public.notification_priority;

CREATE TYPE public.notification_priority AS ENUM (
	'LOW',
	'MEDIUM',
	'HIGH',
	'CRITICAL');

COMMENT ON TYPE public.notification_priority IS 'Defines the urgency level of notifications';

-- DROP TYPE public.notification_status;

CREATE TYPE public.notification_status AS ENUM (
	'PENDING',
	'DELIVERED',
	'FAILED');

COMMENT ON TYPE public.notification_status IS 'Defines the delivery status of notifications';

-- DROP TYPE public.notification_type;

CREATE TYPE public.notification_type AS ENUM (
	'FAILED_LOGIN_NOTIFICATION',
	'SESSION_EXPIRY',
	'PASSWORD_RESET',
	'INVITATION',
	'CONFIG_CHANGE',
	'BILLING_EVENT',
	'AGENT_STATUS_CHANGE',
	'KNOWLEDGE_BASE_UPDATE',
	'SECURITY_ALERT',
	'TASK_ASSIGNMENT',
	'SYSTEM_MAINTENANCE',
	'GENERAL');

COMMENT ON TYPE public.notification_type IS 'Categorizes different types of system notifications';

-- DROP TYPE public.user_role;

CREATE TYPE public.user_role AS ENUM (
	'SUPER_ADMIN',
	'COMPANY_ADMIN',
	'SPACE_ADMIN',
	'REGULAR_USER');

COMMENT ON TYPE public.user_role IS 'Defines the permission levels for users in the system';

-- DROP TYPE public.user_status;

CREATE TYPE public.user_status AS ENUM (
	'ACTIVE',
	'SUSPENDED',
	'BLOCKED',
	'ARCHIVED');

COMMENT ON TYPE public.user_status IS 'Defines the current state of a user account';

-- public.companies definition

-- Drop table

-- DROP TABLE public.companies;

CREATE TABLE public.companies (
	id serial4 NOT NULL, -- Primary key - unique company identifier
	"name" varchar(255) NOT NULL, -- Official company name
	description text NULL, -- Detailed description of company business and purpose
	"size" public.company_size NOT NULL, -- Size classification based on employee count
	vertical public.company_vertical NOT NULL, -- Primary industry sector the company operates in
	"status" public.company_status DEFAULT 'ACTIVE'::company_status NOT NULL, -- Current operational status of the company
	created_at timestamptz(6) DEFAULT now() NULL, -- Timestamp when company record was created
	updated_at timestamptz(6) DEFAULT now() NULL, -- Timestamp when company record was last modified
	CONSTRAINT companies_pkey PRIMARY KEY (id)
);
COMMENT ON TABLE public.companies IS 'Stores organization information for multi-tenant system';

-- Column comments

COMMENT ON COLUMN public.companies.id IS 'Primary key - unique company identifier';
COMMENT ON COLUMN public.companies."name" IS 'Official company name';
COMMENT ON COLUMN public.companies.description IS 'Detailed description of company business and purpose';
COMMENT ON COLUMN public.companies."size" IS 'Size classification based on employee count';
COMMENT ON COLUMN public.companies.vertical IS 'Primary industry sector the company operates in';
COMMENT ON COLUMN public.companies."status" IS 'Current operational status of the company';
COMMENT ON COLUMN public.companies.created_at IS 'Timestamp when company record was created';
COMMENT ON COLUMN public.companies.updated_at IS 'Timestamp when company record was last modified';


-- public.knowledge_types definition

-- Drop table

-- DROP TABLE public.knowledge_types;

CREATE TABLE public.knowledge_types (
	id serial4 NOT NULL, -- Primary key - unique knowledge type identifier
	"name" varchar(100) NOT NULL, -- Name of the knowledge type
	description text NULL, -- Detailed description of this knowledge type
	created_at timestamptz(6) DEFAULT now() NULL, -- Timestamp when knowledge type was created
	updated_at timestamptz(6) DEFAULT now() NULL, -- Timestamp when knowledge type was last modified
	CONSTRAINT knowledge_types_pkey PRIMARY KEY (id)
);
COMMENT ON TABLE public.knowledge_types IS 'Stores types of knowledge sources that agents can use';

-- Column comments

COMMENT ON COLUMN public.knowledge_types.id IS 'Primary key - unique knowledge type identifier';
COMMENT ON COLUMN public.knowledge_types."name" IS 'Name of the knowledge type';
COMMENT ON COLUMN public.knowledge_types.description IS 'Detailed description of this knowledge type';
COMMENT ON COLUMN public.knowledge_types.created_at IS 'Timestamp when knowledge type was created';
COMMENT ON COLUMN public.knowledge_types.updated_at IS 'Timestamp when knowledge type was last modified';


-- public.agents definition

-- Drop table

-- DROP TABLE public.agents;

CREATE TABLE public.agents (
	id serial4 NOT NULL, -- Primary key - unique agent identifier
	company_id int4 NOT NULL, -- Foreign key to company that owns this agent
	"name" varchar(255) NOT NULL, -- Display name of the agent
	description text NULL, -- Detailed description of the agent purpose and capabilities
	agent_protocol text NULL, -- Protocol configuration that defines agent behavior
	"status" public.agent_status DEFAULT 'DRAFT'::agent_status NOT NULL, -- Current operational status of the agent
	created_at timestamptz(6) DEFAULT now() NULL, -- Timestamp when agent was created
	updated_at timestamptz(6) DEFAULT now() NULL, -- Timestamp when agent was last modified
	CONSTRAINT agents_pkey PRIMARY KEY (id),
	CONSTRAINT agents_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id)
);
CREATE INDEX idx_agents_company_id ON public.agents USING btree (company_id);
CREATE INDEX idx_agents_status ON public.agents USING btree (status);
COMMENT ON TABLE public.agents IS 'Stores AI agents configured for each company';

-- Column comments

COMMENT ON COLUMN public.agents.id IS 'Primary key - unique agent identifier';
COMMENT ON COLUMN public.agents.company_id IS 'Foreign key to company that owns this agent';
COMMENT ON COLUMN public.agents."name" IS 'Display name of the agent';
COMMENT ON COLUMN public.agents.description IS 'Detailed description of the agent purpose and capabilities';
COMMENT ON COLUMN public.agents.agent_protocol IS 'Protocol configuration that defines agent behavior';
COMMENT ON COLUMN public.agents."status" IS 'Current operational status of the agent';
COMMENT ON COLUMN public.agents.created_at IS 'Timestamp when agent was created';
COMMENT ON COLUMN public.agents.updated_at IS 'Timestamp when agent was last modified';


-- public.knowledge_bases definition

-- Drop table

-- DROP TABLE public.knowledge_bases;

CREATE TABLE public.knowledge_bases (
	id serial4 NOT NULL, -- Primary key - unique knowledge base identifier
	company_id int4 NOT NULL, -- Foreign key to company that owns this knowledge base
	"name" varchar(255) NOT NULL, -- Display name of the knowledge base
	description text NULL, -- Detailed description of the knowledge base contents and purpose
	"status" public.knowledge_base_status DEFAULT 'DRAFT'::knowledge_base_status NOT NULL, -- Current operational status of the knowledge base
	created_at timestamptz(6) DEFAULT now() NULL, -- Timestamp when knowledge base was created
	updated_at timestamptz(6) DEFAULT now() NULL, -- Timestamp when knowledge base was last modified
	CONSTRAINT knowledge_bases_pkey PRIMARY KEY (id),
	CONSTRAINT knowledge_bases_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id)
);
CREATE INDEX idx_knowledge_bases_company_id ON public.knowledge_bases USING btree (company_id);
CREATE INDEX idx_knowledge_bases_status ON public.knowledge_bases USING btree (status);
COMMENT ON TABLE public.knowledge_bases IS 'Stores collections of knowledge for company agents to use';

-- Column comments

COMMENT ON COLUMN public.knowledge_bases.id IS 'Primary key - unique knowledge base identifier';
COMMENT ON COLUMN public.knowledge_bases.company_id IS 'Foreign key to company that owns this knowledge base';
COMMENT ON COLUMN public.knowledge_bases."name" IS 'Display name of the knowledge base';
COMMENT ON COLUMN public.knowledge_bases.description IS 'Detailed description of the knowledge base contents and purpose';
COMMENT ON COLUMN public.knowledge_bases."status" IS 'Current operational status of the knowledge base';
COMMENT ON COLUMN public.knowledge_bases.created_at IS 'Timestamp when knowledge base was created';
COMMENT ON COLUMN public.knowledge_bases.updated_at IS 'Timestamp when knowledge base was last modified';


-- public.knowledge_sources definition

-- Drop table

-- DROP TABLE public.knowledge_sources;

CREATE TABLE public.knowledge_sources (
	id serial4 NOT NULL, -- Primary key - unique knowledge source identifier
	knowledge_base_id int4 NOT NULL, -- Foreign key to knowledge base this source belongs to
	knowledge_type_id int4 NOT NULL, -- Foreign key to type of knowledge source
	"name" varchar(255) NOT NULL, -- Display name of the knowledge source
	created_at timestamptz(6) DEFAULT now() NULL, -- Timestamp when knowledge source was added
	updated_at timestamptz(6) DEFAULT now() NULL, -- Timestamp when knowledge source was last modified
	CONSTRAINT knowledge_sources_pkey PRIMARY KEY (id),
	CONSTRAINT knowledge_sources_knowledge_base_id_fkey FOREIGN KEY (knowledge_base_id) REFERENCES public.knowledge_bases(id),
	CONSTRAINT knowledge_sources_knowledge_type_id_fkey FOREIGN KEY (knowledge_type_id) REFERENCES public.knowledge_types(id)
);
CREATE INDEX idx_knowledge_sources_knowledge_base_id ON public.knowledge_sources USING btree (knowledge_base_id);
CREATE INDEX idx_knowledge_sources_knowledge_type_id ON public.knowledge_sources USING btree (knowledge_type_id);
COMMENT ON TABLE public.knowledge_sources IS 'Stores specific sources of knowledge within knowledge bases';

-- Column comments

COMMENT ON COLUMN public.knowledge_sources.id IS 'Primary key - unique knowledge source identifier';
COMMENT ON COLUMN public.knowledge_sources.knowledge_base_id IS 'Foreign key to knowledge base this source belongs to';
COMMENT ON COLUMN public.knowledge_sources.knowledge_type_id IS 'Foreign key to type of knowledge source';
COMMENT ON COLUMN public.knowledge_sources."name" IS 'Display name of the knowledge source';
COMMENT ON COLUMN public.knowledge_sources.created_at IS 'Timestamp when knowledge source was added';
COMMENT ON COLUMN public.knowledge_sources.updated_at IS 'Timestamp when knowledge source was last modified';


-- public.users definition

-- Drop table

-- DROP TABLE public.users;

CREATE TABLE public.users (
	id serial4 NOT NULL, -- Primary key - unique user identifier
	company_id int4 NULL, -- Foreign key to associated company (null for super admins)
	"role" public.user_role NOT NULL, -- Permission level within the system
	email varchar(255) NOT NULL, -- Email address used for authentication and communication
	"name" varchar(255) NOT NULL, -- User's full name
	password_hash varchar(255) NOT NULL, -- Securely hashed user password
	"status" public.user_status DEFAULT 'ACTIVE'::user_status NOT NULL, -- Current status of user account
	created_at timestamptz(6) DEFAULT now() NULL, -- Timestamp when user account was created
	updated_at timestamptz(6) DEFAULT now() NULL, -- Timestamp when user account was last modified
	CONSTRAINT users_pkey PRIMARY KEY (id),
	CONSTRAINT users_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id)
);
COMMENT ON TABLE public.users IS 'Stores all user accounts in the system';

-- Column comments

COMMENT ON COLUMN public.users.id IS 'Primary key - unique user identifier';
COMMENT ON COLUMN public.users.company_id IS 'Foreign key to associated company (null for super admins)';
COMMENT ON COLUMN public.users."role" IS 'Permission level within the system';
COMMENT ON COLUMN public.users.email IS 'Email address used for authentication and communication';
COMMENT ON COLUMN public.users."name" IS 'User''s full name';
COMMENT ON COLUMN public.users.password_hash IS 'Securely hashed user password';
COMMENT ON COLUMN public.users."status" IS 'Current status of user account';
COMMENT ON COLUMN public.users.created_at IS 'Timestamp when user account was created';
COMMENT ON COLUMN public.users.updated_at IS 'Timestamp when user account was last modified';


-- public.agent_knowledge_bases definition

-- Drop table

-- DROP TABLE public.agent_knowledge_bases;

CREATE TABLE public.agent_knowledge_bases (
	id serial4 NOT NULL, -- Primary key - unique association identifier
	agent_id int4 NOT NULL, -- Foreign key to associated agent
	knowledge_base_id int4 NOT NULL, -- Foreign key to associated knowledge base
	created_at timestamptz(6) DEFAULT now() NULL, -- Timestamp when association was created
	CONSTRAINT agent_knowledge_bases_agent_id_knowledge_base_id_key UNIQUE (agent_id, knowledge_base_id),
	CONSTRAINT agent_knowledge_bases_pkey PRIMARY KEY (id),
	CONSTRAINT agent_knowledge_bases_agent_id_fkey FOREIGN KEY (agent_id) REFERENCES public.agents(id),
	CONSTRAINT agent_knowledge_bases_knowledge_base_id_fkey FOREIGN KEY (knowledge_base_id) REFERENCES public.knowledge_bases(id)
);
CREATE INDEX idx_agent_knowledge_bases_agent_id ON public.agent_knowledge_bases USING btree (agent_id);
CREATE INDEX idx_agent_knowledge_bases_knowledge_base_id ON public.agent_knowledge_bases USING btree (knowledge_base_id);
COMMENT ON TABLE public.agent_knowledge_bases IS 'Junction table linking agents to their knowledge bases';

-- Column comments

COMMENT ON COLUMN public.agent_knowledge_bases.id IS 'Primary key - unique association identifier';
COMMENT ON COLUMN public.agent_knowledge_bases.agent_id IS 'Foreign key to associated agent';
COMMENT ON COLUMN public.agent_knowledge_bases.knowledge_base_id IS 'Foreign key to associated knowledge base';
COMMENT ON COLUMN public.agent_knowledge_bases.created_at IS 'Timestamp when association was created';


-- public.audit_logs definition

-- Drop table

-- DROP TABLE public.audit_logs;

CREATE TABLE public.audit_logs (
	id serial4 NOT NULL, -- Primary key - unique audit record identifier
	user_id int4 NULL, -- Foreign key to user who performed action (if applicable)
	event_type public.audit_event_type NOT NULL, -- Category of security event
	event_description text NULL, -- Detailed information about what occurred
	ip_address varchar(45) NULL, -- IP address where action originated
	signed_by int4 NULL, -- Foreign key to user who verified/signed this audit record
	digital_signature varchar(512) NULL, -- Cryptographic signature to ensure audit integrity
	reason text NULL, -- Explanation for why the action was performed
	table_name varchar(100) NULL, -- Name of database table that was modified
	record_id varchar(100) NULL, -- Identifier of the specific database record that was modified
	group_id uuid NULL, -- UUID to group related audit events from a single logical operation
	created_at timestamptz(6) DEFAULT now() NULL, -- Timestamp when security event occurred
	CONSTRAINT audit_logs_pkey PRIMARY KEY (id),
	CONSTRAINT audit_logs_signed_by_fkey FOREIGN KEY (signed_by) REFERENCES public.users(id),
	CONSTRAINT audit_logs_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);
CREATE INDEX idx_audit_logs_created_at ON public.audit_logs USING btree (created_at);
CREATE INDEX idx_audit_logs_group_id ON public.audit_logs USING btree (group_id);
CREATE INDEX idx_audit_logs_record_id ON public.audit_logs USING btree (record_id);
CREATE INDEX idx_audit_logs_table_name ON public.audit_logs USING btree (table_name);
CREATE INDEX idx_audit_logs_user_id ON public.audit_logs USING btree (user_id);
COMMENT ON TABLE public.audit_logs IS 'Security audit trail for compliance and investigations with digital signatures';

-- Column comments

COMMENT ON COLUMN public.audit_logs.id IS 'Primary key - unique audit record identifier';
COMMENT ON COLUMN public.audit_logs.user_id IS 'Foreign key to user who performed action (if applicable)';
COMMENT ON COLUMN public.audit_logs.event_type IS 'Category of security event';
COMMENT ON COLUMN public.audit_logs.event_description IS 'Detailed information about what occurred';
COMMENT ON COLUMN public.audit_logs.ip_address IS 'IP address where action originated';
COMMENT ON COLUMN public.audit_logs.signed_by IS 'Foreign key to user who verified/signed this audit record';
COMMENT ON COLUMN public.audit_logs.digital_signature IS 'Cryptographic signature to ensure audit integrity';
COMMENT ON COLUMN public.audit_logs.reason IS 'Explanation for why the action was performed';
COMMENT ON COLUMN public.audit_logs.table_name IS 'Name of database table that was modified';
COMMENT ON COLUMN public.audit_logs.record_id IS 'Identifier of the specific database record that was modified';
COMMENT ON COLUMN public.audit_logs.group_id IS 'UUID to group related audit events from a single logical operation';
COMMENT ON COLUMN public.audit_logs.created_at IS 'Timestamp when security event occurred';


-- public.failed_logins definition

-- Drop table

-- DROP TABLE public.failed_logins;

CREATE TABLE public.failed_logins (
	id serial4 NOT NULL, -- Primary key - unique failed login record identifier
	user_id int4 NULL, -- Foreign key to user account (if known/valid)
	ip_address varchar(45) NULL, -- IP address where login attempt originated
	attempted_at timestamptz(6) DEFAULT now() NULL, -- Timestamp when login attempt occurred
	failure_reason varchar(255) NULL, -- Description of why authentication failed
	CONSTRAINT failed_logins_pkey PRIMARY KEY (id),
	CONSTRAINT failed_logins_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);
COMMENT ON TABLE public.failed_logins IS 'Records unsuccessful authentication attempts';

-- Column comments

COMMENT ON COLUMN public.failed_logins.id IS 'Primary key - unique failed login record identifier';
COMMENT ON COLUMN public.failed_logins.user_id IS 'Foreign key to user account (if known/valid)';
COMMENT ON COLUMN public.failed_logins.ip_address IS 'IP address where login attempt originated';
COMMENT ON COLUMN public.failed_logins.attempted_at IS 'Timestamp when login attempt occurred';
COMMENT ON COLUMN public.failed_logins.failure_reason IS 'Description of why authentication failed';


-- public.invitations definition

-- Drop table

-- DROP TABLE public.invitations;

CREATE TABLE public.invitations (
	id serial4 NOT NULL, -- Primary key - unique invitation identifier
	user_id int4 NOT NULL, -- Foreign key to invited user account
	"token" varchar(255) NOT NULL, -- Secure random token for invitation validation
	"status" public.invitation_status DEFAULT 'PENDING'::invitation_status NOT NULL, -- Current state of the invitation process
	sent_at timestamptz(6) NULL, -- Timestamp when invitation was sent to user
	expires_at timestamptz(6) NULL, -- Timestamp when invitation becomes invalid
	resend_count int4 DEFAULT 0 NOT NULL, -- Number of times invitation has been resent
	created_at timestamptz(6) DEFAULT now() NULL, -- Timestamp when invitation was created
	updated_at timestamptz(6) DEFAULT now() NULL, -- Timestamp when invitation was last modified
	CONSTRAINT invitations_pkey PRIMARY KEY (id),
	CONSTRAINT invitations_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);
COMMENT ON TABLE public.invitations IS 'Tracks pending and processed system access invitations';

-- Column comments

COMMENT ON COLUMN public.invitations.id IS 'Primary key - unique invitation identifier';
COMMENT ON COLUMN public.invitations.user_id IS 'Foreign key to invited user account';
COMMENT ON COLUMN public.invitations."token" IS 'Secure random token for invitation validation';
COMMENT ON COLUMN public.invitations."status" IS 'Current state of the invitation process';
COMMENT ON COLUMN public.invitations.sent_at IS 'Timestamp when invitation was sent to user';
COMMENT ON COLUMN public.invitations.expires_at IS 'Timestamp when invitation becomes invalid';
COMMENT ON COLUMN public.invitations.resend_count IS 'Number of times invitation has been resent';
COMMENT ON COLUMN public.invitations.created_at IS 'Timestamp when invitation was created';
COMMENT ON COLUMN public.invitations.updated_at IS 'Timestamp when invitation was last modified';


-- public.notifications definition

-- Drop table

-- DROP TABLE public.notifications;

CREATE TABLE public.notifications (
	id serial4 NOT NULL, -- Primary key - unique notification identifier
	user_id int4 NOT NULL, -- Foreign key to user receiving the notification
	company_id int4 NULL, -- Foreign key to associated company (if applicable)
	notification_type public.notification_type NOT NULL, -- Category of notification
	title varchar(255) NOT NULL, -- Brief heading or subject of the notification
	message text NOT NULL, -- Full notification content or body
	priority public.notification_priority DEFAULT 'MEDIUM'::notification_priority NOT NULL, -- Urgency level of the notification
	"status" public.notification_status DEFAULT 'PENDING'::notification_status NOT NULL, -- Current delivery status of the notification
	delivery_method public.notification_delivery_method DEFAULT 'IN_APP'::notification_delivery_method NOT NULL, -- Channel used to deliver the notification
	read_at timestamptz(6) NULL, -- Timestamp when user viewed the notification (null if unread)
	action_url varchar(512) NULL, -- URL or path for user to take action on notification
	expires_at timestamptz(6) NULL, -- Timestamp when notification expires and should be hidden
	delivered_at timestamptz(6) NULL, -- Timestamp when notification was successfully delivered
	created_at timestamptz(6) DEFAULT now() NULL, -- Timestamp when notification was created
	updated_at timestamptz(6) DEFAULT now() NULL, -- Timestamp when notification was last modified
	CONSTRAINT notifications_pkey PRIMARY KEY (id),
	CONSTRAINT notifications_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id),
	CONSTRAINT notifications_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);
CREATE INDEX idx_notifications_company_id ON public.notifications USING btree (company_id);
CREATE INDEX idx_notifications_created_at ON public.notifications USING btree (created_at);
CREATE INDEX idx_notifications_read_at ON public.notifications USING btree (read_at) WHERE (read_at IS NULL);
CREATE INDEX idx_notifications_status ON public.notifications USING btree (status);
CREATE INDEX idx_notifications_type ON public.notifications USING btree (notification_type);
CREATE INDEX idx_notifications_user_id ON public.notifications USING btree (user_id);
COMMENT ON TABLE public.notifications IS 'Stores system and user notifications with delivery status';

-- Column comments

COMMENT ON COLUMN public.notifications.id IS 'Primary key - unique notification identifier';
COMMENT ON COLUMN public.notifications.user_id IS 'Foreign key to user receiving the notification';
COMMENT ON COLUMN public.notifications.company_id IS 'Foreign key to associated company (if applicable)';
COMMENT ON COLUMN public.notifications.notification_type IS 'Category of notification';
COMMENT ON COLUMN public.notifications.title IS 'Brief heading or subject of the notification';
COMMENT ON COLUMN public.notifications.message IS 'Full notification content or body';
COMMENT ON COLUMN public.notifications.priority IS 'Urgency level of the notification';
COMMENT ON COLUMN public.notifications."status" IS 'Current delivery status of the notification';
COMMENT ON COLUMN public.notifications.delivery_method IS 'Channel used to deliver the notification';
COMMENT ON COLUMN public.notifications.read_at IS 'Timestamp when user viewed the notification (null if unread)';
COMMENT ON COLUMN public.notifications.action_url IS 'URL or path for user to take action on notification';
COMMENT ON COLUMN public.notifications.expires_at IS 'Timestamp when notification expires and should be hidden';
COMMENT ON COLUMN public.notifications.delivered_at IS 'Timestamp when notification was successfully delivered';
COMMENT ON COLUMN public.notifications.created_at IS 'Timestamp when notification was created';
COMMENT ON COLUMN public.notifications.updated_at IS 'Timestamp when notification was last modified';


-- public.password_history definition

-- Drop table

-- DROP TABLE public.password_history;

CREATE TABLE public.password_history (
	id serial4 NOT NULL, -- Primary key - unique password history record identifier
	user_id int4 NOT NULL, -- Foreign key to associated user account
	password_hash varchar(255) NOT NULL, -- Hashed version of previous password
	created_at timestamptz(6) DEFAULT now() NULL, -- Timestamp when this password was initially set
	CONSTRAINT password_history_pkey PRIMARY KEY (id),
	CONSTRAINT password_history_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);
COMMENT ON TABLE public.password_history IS 'Stores previous user passwords to prevent reuse';

-- Column comments

COMMENT ON COLUMN public.password_history.id IS 'Primary key - unique password history record identifier';
COMMENT ON COLUMN public.password_history.user_id IS 'Foreign key to associated user account';
COMMENT ON COLUMN public.password_history.password_hash IS 'Hashed version of previous password';
COMMENT ON COLUMN public.password_history.created_at IS 'Timestamp when this password was initially set';


-- public.user_lockouts definition

-- Drop table

-- DROP TABLE public.user_lockouts;

CREATE TABLE public.user_lockouts (
	id serial4 NOT NULL, -- Primary key - unique lockout record identifier
	user_id int4 NOT NULL, -- Foreign key to locked user account
	lockout_start timestamptz(6) DEFAULT now() NULL, -- Timestamp when lockout began
	lockout_end timestamptz(6) NULL, -- Timestamp when lockout expires (null if permanent)
	failed_attempts int4 DEFAULT 0 NOT NULL, -- Number of failed login attempts that triggered lockout
	reason text NULL, -- Description of why account was locked
	created_at timestamptz(6) DEFAULT now() NULL, -- Timestamp when lockout record was created
	updated_at timestamptz(6) DEFAULT now() NULL, -- Timestamp when lockout record was last modified
	CONSTRAINT user_lockouts_pkey PRIMARY KEY (id),
	CONSTRAINT user_lockouts_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);
COMMENT ON TABLE public.user_lockouts IS 'Tracks temporary or permanent account access restrictions';

-- Column comments

COMMENT ON COLUMN public.user_lockouts.id IS 'Primary key - unique lockout record identifier';
COMMENT ON COLUMN public.user_lockouts.user_id IS 'Foreign key to locked user account';
COMMENT ON COLUMN public.user_lockouts.lockout_start IS 'Timestamp when lockout began';
COMMENT ON COLUMN public.user_lockouts.lockout_end IS 'Timestamp when lockout expires (null if permanent)';
COMMENT ON COLUMN public.user_lockouts.failed_attempts IS 'Number of failed login attempts that triggered lockout';
COMMENT ON COLUMN public.user_lockouts.reason IS 'Description of why account was locked';
COMMENT ON COLUMN public.user_lockouts.created_at IS 'Timestamp when lockout record was created';
COMMENT ON COLUMN public.user_lockouts.updated_at IS 'Timestamp when lockout record was last modified';


-- public.user_sessions definition

-- Drop table

-- DROP TABLE public.user_sessions;

CREATE TABLE public.user_sessions (
	id serial4 NOT NULL, -- Primary key - unique session identifier
	user_id int4 NOT NULL, -- Foreign key to user account for this session
	session_token varchar(255) NOT NULL, -- Secure cryptographic token for session validation
	ip_address varchar(45) NULL, -- IP address of client that created session
	user_agent varchar(512) NULL, -- Browser/client information
	created_at timestamptz(6) DEFAULT now() NULL, -- Timestamp when session was established
	expires_at timestamptz(6) NOT NULL, -- Timestamp when session will automatically terminate
	CONSTRAINT user_sessions_pkey PRIMARY KEY (id),
	CONSTRAINT user_sessions_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);
COMMENT ON TABLE public.user_sessions IS 'Tracks active authenticated user sessions';

-- Column comments

COMMENT ON COLUMN public.user_sessions.id IS 'Primary key - unique session identifier';
COMMENT ON COLUMN public.user_sessions.user_id IS 'Foreign key to user account for this session';
COMMENT ON COLUMN public.user_sessions.session_token IS 'Secure cryptographic token for session validation';
COMMENT ON COLUMN public.user_sessions.ip_address IS 'IP address of client that created session';
COMMENT ON COLUMN public.user_sessions.user_agent IS 'Browser/client information';
COMMENT ON COLUMN public.user_sessions.created_at IS 'Timestamp when session was established';
COMMENT ON COLUMN public.user_sessions.expires_at IS 'Timestamp when session will automatically terminate';


-- public.audit_change_logs definition

-- Drop table

-- DROP TABLE public.audit_change_logs;

CREATE TABLE public.audit_change_logs (
	id serial4 NOT NULL, -- Primary key - unique change log identifier
	audit_log_id int4 NOT NULL, -- Foreign key to associated audit record
	column_name varchar(100) NOT NULL, -- Name of database column that was modified
	old_value text NULL, -- Value before the change was made
	new_value text NULL, -- Value after the change was made
	created_at timestamptz(6) DEFAULT now() NULL, -- Timestamp when change was recorded
	CONSTRAINT audit_change_logs_pkey PRIMARY KEY (id),
	CONSTRAINT audit_change_logs_audit_log_id_fkey FOREIGN KEY (audit_log_id) REFERENCES public.audit_logs(id)
);
CREATE INDEX idx_audit_change_logs_audit_log_id ON public.audit_change_logs USING btree (audit_log_id);
COMMENT ON TABLE public.audit_change_logs IS 'Stores detailed before/after values for changes in audited operations';

-- Column comments

COMMENT ON COLUMN public.audit_change_logs.id IS 'Primary key - unique change log identifier';
COMMENT ON COLUMN public.audit_change_logs.audit_log_id IS 'Foreign key to associated audit record';
COMMENT ON COLUMN public.audit_change_logs.column_name IS 'Name of database column that was modified';
COMMENT ON COLUMN public.audit_change_logs.old_value IS 'Value before the change was made';
COMMENT ON COLUMN public.audit_change_logs.new_value IS 'Value after the change was made';
COMMENT ON COLUMN public.audit_change_logs.created_at IS 'Timestamp when change was recorded';