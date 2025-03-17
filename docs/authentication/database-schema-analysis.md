# Authentication Database Schema Analysis

## Overview

This document provides an analysis of the database schema related to authentication and user management in the Celestra system. Understanding this structure is essential before implementing authentication features.

## User Management Tables

### Users Table (`public.users`)

The central table for user management with the following key fields:
- `id`: Primary key and unique identifier
- `company_id`: Foreign key to associated company (null for super admins)
- `role`: User permission level (ENUM: SUPER_ADMIN, COMPANY_ADMIN, SPACE_ADMIN, REGULAR_USER)
- `email`: Used for authentication and communication
- `name`: User's full name
- `password_hash`: Securely hashed user password
- `status`: Current account status (ENUM: ACTIVE, SUSPENDED, BLOCKED, ARCHIVED)
- `created_at` and `updated_at`: Timestamps for record creation and modification

Notes:
- The table has a foreign key relationship to the companies table
- Super admins have a null company_id
- No unique constraint on email, which might need to be addressed
- No additional user profile information (could be added in a separate table if needed)

## Authentication-Related Tables

### User Sessions Table (`public.user_sessions`)

Tracks active authenticated user sessions:
- `id`: Primary key
- `user_id`: Foreign key to users table
- `session_token`: Secure cryptographic token for session validation
- `ip_address`: IP address of client that created session
- `user_agent`: Browser/client information
- `created_at`: When session was established
- `expires_at`: When session will automatically terminate

Notes:
- No index on session_token, which might be needed for efficient lookups
- No explicit session termination field (could use a status enum if needed)

### Failed Logins Table (`public.failed_logins`)

Records unsuccessful authentication attempts:
- `id`: Primary key
- `user_id`: Foreign key to user account (if known/valid)
- `ip_address`: IP address where login attempt originated
- `attempted_at`: Timestamp when login attempt occurred
- `failure_reason`: Description of why authentication failed
- `email`: Email address used in the login attempt (especially when user_id is unknown)

Notes:
- Has an index on email for efficient lookups
- Can track attempts even when user is not found (using email field)

### User Lockouts Table (`public.user_lockouts`)

Tracks temporary or permanent account access restrictions:
- `id`: Primary key
- `user_id`: Foreign key to locked user account
- `lockout_start`: Timestamp when lockout began
- `lockout_end`: Timestamp when lockout expires (null if permanent)
- `failed_attempts`: Number of failed login attempts that triggered lockout
- `reason`: Description of why account was locked
- `created_at` and `updated_at`: Timestamps for record creation and modification

Notes:
- No index on user_id, which might be needed for efficient lookups
- No status field to track lockout state (active, expired, manually removed)

### Password History Table (`public.password_history`)

Stores previous user passwords to prevent reuse:
- `id`: Primary key
- `user_id`: Foreign key to associated user account
- `password_hash`: Hashed version of previous password
- `created_at`: Timestamp when this password was initially set

Notes:
- No index on user_id, which might be needed for efficient lookups
- No limit on the number of password history entries per user

## Invitation System

### Invitations Table (`public.invitations`)

Tracks pending and processed system access invitations:
- `id`: Primary key
- `user_id`: Foreign key to invited user account
- `token`: Secure random token for invitation validation
- `status`: Current state of the invitation process (ENUM: PENDING, SENT, EXPIRED, CANCELLED, ACCEPTED)
- `sent_at`: Timestamp when invitation was sent to user
- `expires_at`: Timestamp when invitation becomes invalid
- `resend_count`: Number of times invitation has been resent
- `created_at` and `updated_at`: Timestamps for record creation and modification

Notes:
- No index on token, which might be needed for efficient lookups
- No field for the inviter (who sent the invitation)
- No field for invitation type or role assignment

## Audit and Security Tables

### Audit Logs Table (`public.audit_logs`)

Security audit trail for compliance and investigations:
- `id`: Primary key
- `user_id`: Foreign key to user who performed action (if applicable)
- `event_type`: Category of security event (ENUM: FAILED_LOGIN, SUCCESSFUL_LOGIN, SESSION_STARTED, SESSION_ENDED, etc.)
- `event_description`: Detailed information about what occurred
- `ip_address`: IP address where action originated
- `signed_by`: Foreign key to user who verified/signed this audit record
- `digital_signature`: Cryptographic signature to ensure audit integrity
- `reason`: Explanation for why the action was performed
- `table_name`: Name of database table that was modified
- `record_id`: Identifier of the specific database record that was modified
- `group_id`: UUID to group related audit events from a single logical operation
- `created_at`: Timestamp when security event occurred

Notes:
- Has indexes on created_at, group_id, record_id, table_name, and user_id
- Supports digital signatures for log integrity
- Can track both user-initiated and system actions

### Audit Change Logs Table (`public.audit_change_logs`)

Stores detailed before/after values for changes in audited operations:
- `id`: Primary key
- `audit_log_id`: Foreign key to associated audit record
- `column_name`: Name of database column that was modified
- `old_value`: Value before the change was made
- `new_value`: Value after the change was made
- `created_at`: Timestamp when change was recorded

Notes:
- Has an index on audit_log_id
- Provides detailed tracking of specific field changes

## Notification System

### Notifications Table (`public.notifications`)

Stores system and user notifications with delivery status:
- `id`: Primary key
- `user_id`: Foreign key to user receiving the notification
- `company_id`: Foreign key to associated company (if applicable)
- `notification_type`: Category of notification (includes security-related types like FAILED_LOGIN_NOTIFICATION, PASSWORD_RESET)
- `title`: Brief heading or subject of the notification
- `message`: Full notification content or body
- `priority`: Urgency level (ENUM: LOW, MEDIUM, HIGH, CRITICAL)
- `status`: Current delivery status (ENUM: PENDING, DELIVERED, FAILED)
- `delivery_method`: Channel used to deliver the notification (ENUM: IN_APP, EMAIL, SMS, WEBHOOK, PUSH)
- Various timestamps and metadata fields

Notes:
- Has indexes on company_id, created_at, read_at, status, notification_type, and user_id
- Supports multiple delivery methods
- Can be used for security notifications and alerts

## Enum Types

Several enum types are defined for status tracking and categorization:
- `user_role`: SUPER_ADMIN, COMPANY_ADMIN, SPACE_ADMIN, REGULAR_USER
- `user_status`: ACTIVE, SUSPENDED, BLOCKED, ARCHIVED
- `invitation_status`: PENDING, SENT, EXPIRED, CANCELLED, ACCEPTED
- `audit_event_type`: FAILED_LOGIN, SUCCESSFUL_LOGIN, SESSION_STARTED, SESSION_ENDED, etc.
- `notification_type`: Includes security-related types like FAILED_LOGIN_NOTIFICATION, PASSWORD_RESET

## Relationships and Foreign Keys

Key relationships for authentication:
- Users belong to Companies (except super admins)
- User Sessions belong to Users
- Failed Logins may reference Users (if known)
- User Lockouts belong to Users
- Password History entries belong to Users
- Invitations belong to Users
- Audit Logs may reference Users (as actor or signer)
- Notifications belong to Users and optionally to Companies

## Performance Considerations

The schema includes several indexes to optimize common queries:
- Indexes on foreign keys for relationship lookups
- Indexes on status fields for filtering
- Indexes on timestamps for time-based queries
- Indexes on email in failed_logins table

## Missing Elements and Potential Improvements

Some elements that might be needed for a complete authentication system:
- No password reset token table (could be added or use invitations table)
- No remember-me token storage (for persistent logins)
- No two-factor authentication support
- No API token or OAuth integration tables
- Consider adding unique constraint on user email
- Consider adding indexes on frequently queried fields

## Conclusion

The database schema provides a solid foundation for implementing authentication features. It includes tables for user management, session tracking, failed login attempts, account lockouts, password history, invitations, and comprehensive audit logging. Some additional tables or fields might be needed for specific authentication features, but the core structure is in place.