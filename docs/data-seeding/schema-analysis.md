# Database Schema Analysis for Data Seeding

## Overview
This document provides an analysis of the Celestra database schema to support the data seeding process. The analysis focuses on understanding table relationships, constraints, and data requirements to ensure proper seeding order and realistic data generation.

## Database Structure

### Enum Types
The database uses PostgreSQL enum types for various categorical fields:

1. **agent_status**: ACTIVE, DISABLED, ARCHIVED, DRAFT
2. **audit_event_type**: FAILED_LOGIN, SUCCESSFUL_LOGIN, SESSION_STARTED, SESSION_ENDED, ROLE_ASSIGNMENT_CHANGE, CONFIGURATION_UPDATE, DATA_EXPORT, OTHER
3. **company_size**: SMALL, MEDIUM, LARGE, ENTERPRISE
4. **company_status**: ACTIVE, SUSPENDED, ARCHIVED
5. **company_vertical**: TECH, PHARMACEUTICAL, FINANCE, RETAIL, OTHER
6. **invitation_status**: PENDING, SENT, EXPIRED, CANCELLED, ACCEPTED
7. **knowledge_base_status**: ACTIVE, BUILDING, DISABLED, ARCHIVED, DRAFT
8. **notification_delivery_method**: IN_APP, EMAIL, SMS, WEBHOOK, PUSH
9. **notification_priority**: LOW, MEDIUM, HIGH, CRITICAL
10. **notification_status**: PENDING, DELIVERED, FAILED
11. **notification_type**: Multiple types including FAILED_LOGIN_NOTIFICATION, SESSION_EXPIRY, etc.
12. **user_role**: SUPER_ADMIN, COMPANY_ADMIN, SPACE_ADMIN, REGULAR_USER
13. **user_status**: ACTIVE, SUSPENDED, BLOCKED, ARCHIVED

### Tables and Relationships

#### Independent Tables (No Foreign Key Dependencies)
1. **companies**
   - Primary table for organizations
   - Contains basic company information (name, description, size, vertical, status)
   - Referenced by many other tables

2. **knowledge_types**
   - Stores types of knowledge sources
   - Contains name and description
   - Referenced by knowledge_sources

#### First-Level Dependent Tables
1. **agents**
   - Depends on: companies
   - Stores AI agents configured for each company
   - Contains agent name, description, protocol, and status

2. **knowledge_bases**
   - Depends on: companies
   - Stores collections of knowledge for company agents
   - Contains name, description, and status

3. **users**
   - Depends on: companies (optional - null for super admins)
   - Stores all user accounts
   - Contains role, email, name, password_hash, and status

#### Second-Level Dependent Tables
1. **agent_knowledge_bases**
   - Depends on: agents, knowledge_bases
   - Junction table linking agents to knowledge bases
   - Contains only foreign keys and creation timestamp

2. **knowledge_sources**
   - Depends on: knowledge_bases, knowledge_types
   - Stores specific sources of knowledge
   - Contains name and timestamps

3. **audit_logs**
   - Depends on: users (optional)
   - Stores security audit trail
   - Contains event details, IP address, and various metadata

4. **failed_logins**
   - Depends on: users (optional)
   - Records unsuccessful authentication attempts
   - Contains IP address, timestamp, and failure reason

5. **invitations**
   - Depends on: users
   - Tracks system access invitations
   - Contains token, status, timestamps, and resend count

6. **notifications**
   - Depends on: users, companies (optional)
   - Stores system and user notifications
   - Contains message details, status, and delivery information

7. **password_history**
   - Depends on: users
   - Stores previous user passwords
   - Contains password hash and timestamp

8. **user_lockouts**
   - Depends on: users
   - Tracks account access restrictions
   - Contains lockout period, reason, and failed attempts

9. **user_sessions**
   - Depends on: users
   - Tracks active authenticated sessions
   - Contains session token, IP address, and expiration

#### Third-Level Dependent Tables
1. **audit_change_logs**
   - Depends on: audit_logs
   - Stores detailed before/after values for changes
   - Contains column name, old value, and new value

## Foreign Key Constraints

The following foreign key constraints must be respected during data seeding:

1. agents → companies
2. knowledge_bases → companies
3. users → companies (optional)
4. agent_knowledge_bases → agents
5. agent_knowledge_bases → knowledge_bases
6. knowledge_sources → knowledge_bases
7. knowledge_sources → knowledge_types
8. audit_logs → users (optional)
9. audit_logs → users (signed_by, optional)
10. failed_logins → users (optional)
11. invitations → users
12. notifications → users
13. notifications → companies (optional)
14. password_history → users
15. user_lockouts → users
16. user_sessions → users
17. audit_change_logs → audit_logs

## Data Seeding Order

Based on the foreign key constraints, the tables should be seeded in the following order:

1. **Independent Tables**
   - companies
   - knowledge_types

2. **First-Level Dependent Tables**
   - users
   - agents
   - knowledge_bases

3. **Second-Level Dependent Tables**
   - agent_knowledge_bases
   - knowledge_sources
   - audit_logs
   - failed_logins
   - invitations
   - notifications
   - password_history
   - user_lockouts
   - user_sessions

4. **Third-Level Dependent Tables**
   - audit_change_logs

## Required vs. Optional Fields

### Required Fields (NOT NULL)
- companies: name, size, vertical, status
- knowledge_types: name
- agents: company_id, name, status
- knowledge_bases: company_id, name, status
- users: role, email, name, password_hash, status
- agent_knowledge_bases: agent_id, knowledge_base_id
- knowledge_sources: knowledge_base_id, knowledge_type_id, name
- audit_logs: event_type
- invitations: user_id, token, status, resend_count
- notifications: user_id, notification_type, title, message, priority, status, delivery_method
- password_history: user_id, password_hash
- user_lockouts: user_id, failed_attempts
- user_sessions: user_id, session_token, expires_at
- audit_change_logs: audit_log_id, column_name

### Optional Fields (NULL allowed)
- companies: description, created_at, updated_at
- knowledge_types: description, created_at, updated_at
- agents: description, agent_protocol, created_at, updated_at
- knowledge_bases: description, created_at, updated_at
- users: company_id, created_at, updated_at
- agent_knowledge_bases: created_at
- knowledge_sources: created_at, updated_at
- audit_logs: user_id, event_description, ip_address, signed_by, digital_signature, reason, table_name, record_id, group_id, created_at
- failed_logins: user_id, ip_address, attempted_at, failure_reason
- invitations: sent_at, expires_at, created_at, updated_at
- notifications: company_id, read_at, action_url, expires_at, delivered_at, created_at, updated_at
- password_history: created_at
- user_lockouts: lockout_start, lockout_end, reason, created_at, updated_at
- user_sessions: ip_address, user_agent, created_at
- audit_change_logs: old_value, new_value, created_at

## Data Volume Considerations

For effective testing and demonstration, the following data volumes are recommended:

1. **Core Entities**
   - Companies: 5-10
   - Users: 20-50 (distributed across companies)
   - Agents: 10-20 (distributed across companies)
   - Knowledge Bases: 15-30 (distributed across companies)
   - Knowledge Types: 5-10
   - Knowledge Sources: 30-60 (distributed across knowledge bases)

2. **Junction Tables**
   - Agent Knowledge Bases: 20-40 (creating realistic agent-knowledge base relationships)

3. **Activity Data**
   - User Sessions: 50-100
   - Failed Logins: 20-40
   - Audit Logs: 100-200
   - Audit Change Logs: 200-400
   - Notifications: 100-200
   - Invitations: 20-40
   - Password History: 50-100
   - User Lockouts: 10-20

## Special Considerations

1. **Enum Values**
   - All enum fields must use values from their respective enum types
   - Distribution should be realistic (e.g., most users should be ACTIVE, fewer SUSPENDED)

2. **Timestamps**
   - created_at should always be before updated_at
   - For time-sensitive records (e.g., sessions, invitations), ensure logical time progression
   - Some records should be in the past, some current, and some in the future

3. **Relationships**
   - Ensure realistic distribution (e.g., not all users belong to one company)
   - Create meaningful patterns (e.g., companies with multiple agents, knowledge bases with multiple sources)

4. **Passwords**
   - Generate realistic password hashes (using appropriate hashing algorithm)
   - Ensure password history entries are unique per user

5. **Tokens and IDs**
   - Generate realistic session tokens and invitation tokens
   - Use UUIDs where appropriate (e.g., audit_logs.group_id)

## Conclusion

This analysis provides a comprehensive understanding of the database schema structure, relationships, and constraints. Following this plan will ensure that data is seeded in the correct order, with appropriate values, and in realistic volumes to support application testing and demonstration.