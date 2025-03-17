# Application Feature Data Requirements

## Overview
This document outlines the data requirements for the Celestra application features based on the review of the feature documentation. Understanding these requirements is essential for creating realistic and useful test data that supports application testing and demonstration.

## Core Application Features

### 1. Agentic Application Framework
Based on the agentic-application-specifications.md document, the application implements an LLM-powered agentic framework with the following components:

#### LLM Integration Layer
- **Data Requirements**: 
  - Configuration data for LLM models
  - Prompt templates and response formats
  - Context management settings

#### Cognitive System
- **Data Requirements**:
  - Perception processing rules
  - Reasoning patterns
  - Understanding generation templates

#### Action System
- **Data Requirements**:
  - Goal definitions
  - Planning templates
  - Execution rules
  - Progress monitoring metrics

#### Memory System
- **Data Requirements**:
  - Working memory structures
  - Long-term knowledge repositories
  - Context preservation rules

#### Learning System
- **Data Requirements**:
  - Learning strategies
  - Adaptation mechanisms
  - Performance metrics

### 2. Deviation Investigation Workflow
Based on the investigator.md document, the application implements a structured workflow for investigating deviations in manufacturing processes:

#### Deviation Type Detection
- **Data Requirements**:
  - Deviation descriptions
  - Classification criteria for OOS, OOT, and Yield Deviation types
  - Confidence levels

#### Information Extraction
- **Data Requirements**:
  - Product information (product codes, batch numbers, manufacturing stages)
  - Measurement information (parameters, methods, values, units)
  - Context information (timestamps, operators, locations)

#### Deviation Validation
- **Data Requirements**:
  - Product parameters and specifications
  - Trend rules and statistical control limits
  - Yield standards and historical data

#### Impact Assessment
- **Data Requirements**:
  - Product master data (quality requirements, manufacturing processes)
  - Historical deviation records
  - Parameter relationships
  - Impact patterns

#### Severity Assessment
- **Data Requirements**:
  - Severity classification criteria
  - Historical severity trends
  - SOP-based classification rules

#### Root Cause Analysis
- **Data Requirements**:
  - Ishikawa categories (People, Methods, Machines, Materials, Measurements, Environment)
  - Contributing factors
  - Investigation tasks and evidence requirements

## Data Requirements by Entity Type

### Companies
- Variety of company sizes and verticals
- Mix of active and non-active statuses
- Realistic company names and descriptions

### Users
- Distribution across different roles (SUPER_ADMIN, COMPANY_ADMIN, SPACE_ADMIN, REGULAR_USER)
- Mix of active and non-active statuses
- Realistic user names and email addresses
- Properly hashed passwords

### Agents
- Various agent types with different purposes
- Mix of statuses (ACTIVE, DISABLED, ARCHIVED, DRAFT)
- Realistic agent protocols and descriptions
- Association with companies

### Knowledge Bases
- Different knowledge base types
- Mix of statuses (ACTIVE, BUILDING, DISABLED, ARCHIVED, DRAFT)
- Association with companies
- Realistic names and descriptions

### Knowledge Types
- Various types representing different knowledge sources
- Detailed descriptions of each type

### Knowledge Sources
- Association with knowledge bases and types
- Realistic source names

### Audit Logs
- Various event types (login events, configuration changes, etc.)
- Mix of user-initiated and system events
- Realistic IP addresses and timestamps
- Some with associated audit change logs

### Notifications
- Various notification types
- Different priorities and delivery methods
- Mix of read and unread notifications
- Some with expiration dates
- Association with users and companies

### User Sessions
- Active and expired sessions
- Realistic session tokens and user agents
- Association with users

### Other Entities
- Failed logins with realistic failure reasons
- Invitations in various states
- Password history entries
- User lockouts with realistic reasons

## Special Data Patterns

### Deviation Investigation Data
To support the deviation investigation workflow, the data should include:

1. **Product Data**:
   - Products with well-defined specifications
   - Batch information with manufacturing stages
   - Measurement parameters with expected values and limits

2. **Deviation Records**:
   - Mix of OOS, OOT, and Yield Deviation types
   - Various severity levels
   - Different root causes across Ishikawa categories

3. **Historical Patterns**:
   - Recurring issues with specific products or processes
   - Trends in deviation types and root causes
   - Patterns in impact and severity

### Agent Interaction Data
To support the agentic application framework, the data should include:

1. **Agent Configurations**:
   - Different agent types with specific purposes
   - Various configuration settings
   - Association with knowledge bases

2. **Knowledge Structures**:
   - Organized knowledge sources
   - Different types of knowledge
   - Relationships between knowledge elements

3. **Interaction History**:
   - Records of agent actions and decisions
   - User interactions with agents
   - Learning and adaptation patterns

## Data Volume and Distribution

### Core Entities
- **Companies**: 5-10 with varied sizes and verticals
- **Users**: 20-50 distributed across companies and roles
- **Agents**: 10-20 with different statuses and purposes
- **Knowledge Bases**: 15-30 with varied content types
- **Knowledge Types**: 5-10 representing different source types
- **Knowledge Sources**: 30-60 distributed across knowledge bases

### Activity Data
- **Audit Logs**: 100-200 covering various event types
- **Notifications**: 100-200 with different types and statuses
- **User Sessions**: 50-100 with some active and some expired
- **Failed Logins**: 20-40 with various failure reasons
- **Invitations**: 20-40 in different states
- **Password History**: 50-100 entries across users
- **User Lockouts**: 10-20 with different durations and reasons

## Conclusion

The data seeding process should create a comprehensive dataset that supports all the application features described in the documentation. The data should be realistic, varied, and interconnected to enable effective testing and demonstration of the application's capabilities.