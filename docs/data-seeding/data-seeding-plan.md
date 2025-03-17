# Comprehensive Data Seeding Plan

## Overview
This document outlines the comprehensive plan for seeding the Celestra database with realistic test data. The plan ensures that data is inserted in the correct order (respecting foreign key constraints), with appropriate relationships, and in a way that supports application features for testing and demonstration.

## Seeding Strategy

### 1. Seeding Order
Tables will be seeded in the following order to respect foreign key constraints:

#### Phase 1: Independent Tables
1. **companies**
2. **knowledge_types**

#### Phase 2: First-Level Dependent Tables
3. **users**
4. **agents**
5. **knowledge_bases**

#### Phase 3: Second-Level Dependent Tables
6. **agent_knowledge_bases**
7. **knowledge_sources**
8. **audit_logs**
9. **failed_logins**
10. **invitations**
11. **notifications**
12. **password_history**
13. **user_lockouts**
14. **user_sessions**

#### Phase 4: Third-Level Dependent Tables
15. **audit_change_logs**

### 2. Data Volume
The following data volumes will be generated:

#### Core Entities
- **companies**: 8 (2 SMALL, 3 MEDIUM, 2 LARGE, 1 ENTERPRISE)
- **users**: 40 (distributed across companies and roles)
- **agents**: 15 (distributed across companies)
- **knowledge_bases**: 25 (distributed across companies)
- **knowledge_types**: 8
- **knowledge_sources**: 50 (distributed across knowledge bases)

#### Junction Tables
- **agent_knowledge_bases**: 30 (creating realistic agent-knowledge base relationships)

#### Activity Data
- **user_sessions**: 80 (mix of active and expired)
- **failed_logins**: 30 (distributed across users)
- **audit_logs**: 150 (various event types)
- **audit_change_logs**: 300 (linked to audit logs)
- **notifications**: 150 (various types and statuses)
- **invitations**: 30 (various statuses)
- **password_history**: 80 (distributed across users)
- **user_lockouts**: 15 (distributed across users)

### 3. Data Generation Approach

#### Faker Methods
We will use the Java Faker library to generate realistic data for various fields:

- **Names**: Faker.name().fullName()
- **Companies**: Faker.company().name()
- **Emails**: Faker.internet().emailAddress()
- **Descriptions**: Faker.lorem().paragraph()
- **IP Addresses**: Faker.internet().ipV4Address()
- **User Agents**: Faker.internet().userAgent()
- **Timestamps**: Faker.date().past() or Faker.date().future()
- **Tokens**: UUID.randomUUID().toString()
- **Passwords**: BCrypt.hashpw(Faker.internet().password(), BCrypt.gensalt())

#### Enum Values
Enum values will be distributed realistically:

- **company_size**: Weighted distribution (more SMALL and MEDIUM than LARGE and ENTERPRISE)
- **company_status**: Mostly ACTIVE (80%), some SUSPENDED (15%), few ARCHIVED (5%)
- **agent_status**: Mix of ACTIVE (60%), DRAFT (20%), DISABLED (15%), ARCHIVED (5%)
- **knowledge_base_status**: Mix of ACTIVE (50%), BUILDING (30%), DRAFT (10%), DISABLED (5%), ARCHIVED (5%)
- **user_role**: Mostly REGULAR_USER (70%), some COMPANY_ADMIN (20%), few SPACE_ADMIN (8%), very few SUPER_ADMIN (2%)
- **user_status**: Mostly ACTIVE (85%), some SUSPENDED (10%), few BLOCKED (3%), very few ARCHIVED (2%)
- **invitation_status**: Mix of ACCEPTED (40%), PENDING (30%), SENT (15%), EXPIRED (10%), CANCELLED (5%)
- **notification_status**: Mix of DELIVERED (60%), PENDING (30%), FAILED (10%)
- **notification_priority**: Mostly MEDIUM (50%), some LOW (30%), some HIGH (15%), few CRITICAL (5%)
- **notification_delivery_method**: Mostly IN_APP (60%), some EMAIL (30%), few SMS (5%), few WEBHOOK (3%), few PUSH (2%)
- **audit_event_type**: Distributed across all types with more common events having higher frequencies

#### Relationships
Relationships between entities will be created to ensure realistic data patterns:

- **Users to Companies**: Most users will belong to a company, with a few super admins having null company_id
- **Agents to Companies**: Each company will have 1-3 agents
- **Knowledge Bases to Companies**: Each company will have 2-5 knowledge bases
- **Knowledge Sources to Knowledge Bases**: Each knowledge base will have 2-8 knowledge sources
- **Agent Knowledge Bases**: Each agent will be linked to 1-4 knowledge bases from its company
- **Notifications to Users**: Each user will have multiple notifications of various types
- **Audit Logs to Users**: Various audit events will be linked to different users
- **Password History to Users**: Each user will have 1-5 password history entries
- **User Sessions to Users**: Each user will have 1-3 sessions (some active, some expired)

### 4. Special Data Patterns

To support the application features, we will create the following special data patterns:

#### Deviation Investigation Data
- Create a set of companies in the PHARMACEUTICAL vertical
- Generate agents specifically for deviation investigation
- Create knowledge bases with pharmaceutical manufacturing knowledge
- Generate notifications related to deviations (OOS, OOT, Yield Deviation)
- Create audit logs documenting deviation investigations

#### Agent Interaction Data
- Create agents with different protocols and purposes
- Link agents to appropriate knowledge bases
- Generate audit logs showing agent actions and decisions
- Create notifications related to agent activities

#### User Activity Patterns
- Create realistic login/logout patterns in user sessions
- Generate failed login attempts for some users
- Create lockout records for users with multiple failed logins
- Generate password history showing password changes over time

## Implementation Details

### 1. Seeding Classes Structure

We will implement the following Java classes for data seeding:

#### Utility Classes
- **DataSeeder**: Main coordinator class that orchestrates the seeding process
- **FakerUtil**: Utility class for generating realistic data using Faker
- **EnumUtil**: Utility class for handling enum values
- **PasswordUtil**: Utility class for generating password hashes
- **TimestampUtil**: Utility class for generating realistic timestamps

#### Entity Seeder Classes
- **CompanySeeder**: Seeds company data
- **KnowledgeTypeSeeder**: Seeds knowledge type data
- **UserSeeder**: Seeds user data
- **AgentSeeder**: Seeds agent data
- **KnowledgeBaseSeeder**: Seeds knowledge base data
- **AgentKnowledgeBaseSeeder**: Seeds agent-knowledge base relationships
- **KnowledgeSourceSeeder**: Seeds knowledge source data
- **AuditLogSeeder**: Seeds audit log data
- **FailedLoginSeeder**: Seeds failed login data
- **InvitationSeeder**: Seeds invitation data
- **NotificationSeeder**: Seeds notification data
- **PasswordHistorySeeder**: Seeds password history data
- **UserLockoutSeeder**: Seeds user lockout data
- **UserSessionSeeder**: Seeds user session data
- **AuditChangeLogSeeder**: Seeds audit change log data

### 2. Database Connection

The seeding process will use the existing DatabaseUtil class to obtain database connections. Each seeder class will:

1. Obtain a connection from DatabaseUtil
2. Prepare and execute batch inserts
3. Close the connection when done

### 3. Error Handling

Each seeder class will implement robust error handling:

- Try-catch blocks around database operations
- Logging of errors with detailed information
- Rollback of transactions on failure
- Ability to continue seeding other tables if one fails

### 4. Progress Reporting

The seeding process will include progress reporting:

- Console output showing current seeding step
- Count of records inserted for each table
- Timing information for each seeding operation
- Summary report at the end of the process

## Seeding Implementation Plan

### Phase 1: Setup and Utilities

1. Create the base DataSeeder class with the main method
2. Implement utility classes (FakerUtil, EnumUtil, PasswordUtil, TimestampUtil)
3. Set up logging and progress reporting

### Phase 2: Independent Tables

1. Implement CompanySeeder
2. Implement KnowledgeTypeSeeder
3. Test seeding of independent tables

### Phase 3: First-Level Dependent Tables

1. Implement UserSeeder
2. Implement AgentSeeder
3. Implement KnowledgeBaseSeeder
4. Test seeding of first-level dependent tables

### Phase 4: Second-Level Dependent Tables

1. Implement AgentKnowledgeBaseSeeder
2. Implement KnowledgeSourceSeeder
3. Implement AuditLogSeeder
4. Implement FailedLoginSeeder
5. Implement InvitationSeeder
6. Implement NotificationSeeder
7. Implement PasswordHistorySeeder
8. Implement UserLockoutSeeder
9. Implement UserSessionSeeder
10. Test seeding of second-level dependent tables

### Phase 5: Third-Level Dependent Tables

1. Implement AuditChangeLogSeeder
2. Test seeding of third-level dependent tables

### Phase 6: Integration and Testing

1. Integrate all seeders into the main DataSeeder class
2. Implement command-line options for selective seeding
3. Test the complete seeding process
4. Verify data integrity and relationships

## Conclusion

This comprehensive data seeding plan provides a structured approach to generating realistic test data for the Celestra application. By following this plan, we will create a dataset that:

1. Respects database constraints and relationships
2. Supports application features and use cases
3. Provides realistic data patterns for testing and demonstration
4. Can be easily regenerated or modified as needed

The implementation will be modular, robust, and well-documented, making it easy to maintain and extend in the future.