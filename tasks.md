# AI Utilities and Email Service Tasks

## AI Integration Configuration

- [x] **Create shared configuration file for AI APIs**
  - **Why**: A single configuration file for both AI services simplifies management and ensures consistent settings.
  - **What**: Create a properties file to store API keys and configuration parameters for both OpenAI and Claude services, including:
    - API keys for both services
    - Model identifiers ("o3-mini-2025-01-31" for OpenAI and "claude-3-7-sonnet-20250219" for Claude)
    - Token limits (64000 for OpenAI and 8192 for Claude)
    - Common parameters like temperature, top_p, etc.

## OpenAI Integration

- [x] **Implement OpenAI utility class for chat completion**
  - **Why**: A dedicated utility class encapsulates OpenAI API interactions for chat completion, providing a clean interface for the rest of the application.
  - **What**: Create a utility class that:
    - Loads configuration from the shared properties file
    - Establishes connection to OpenAI API using the specified key
    - Focuses exclusively on chat completion functionality using the "o3-mini-2025-01-31" model
    - Respects the 64000 token limit for this model
    - Implements retry logic with hardcoded retry attempts (e.g., 3 retries) for transient errors
    - Handles different error scenarios including rate limits, authentication failures, invalid requests, and server errors
    - Provides methods to customize request parameters (temperature, max tokens, etc.)
    - Implements proper resource management and cleanup

## Claude Integration

- [x] **Implement Claude utility class for chat completion**
  - **Why**: A dedicated utility for Claude API interactions makes it easier to swap between AI services and maintains clean separation of concerns.
  - **What**: Create a utility class that:
    - Loads configuration from the shared properties file
    - Establishes connection to Claude API using the specified key
    - Focuses exclusively on chat completion functionality using the "claude-3-7-sonnet-20250219" model
    - Respects the 8192 token limit for this model
    - Implements retry logic with hardcoded retry attempts for handling transient errors
    - Handles Claude-specific error scenarios including rate limits, authentication failures, invalid requests, and server errors
    - Provides methods to customize request parameters (temperature, max tokens, etc.)
    - Implements proper response parsing and extraction
    - Ensures efficient resource management

## AI Utilities Testing

- [x] **Create test cases for OpenAI chat completion utility**
  - **Why**: Testing ensures the OpenAI integration functions correctly and handles errors appropriately.
  - **What**: Create a test class that:
    - Tests successful chat completion API calls
    - Tests retry mechanism by simulating transient failures
    - Tests error handling for various error types (rate limits, authentication, etc.)
    - Verifies proper response parsing
    - Tests with different conversation contexts
    - Verifies handling of token limit (64000) edge cases

- [x] **Create test cases for Claude chat completion utility**
  - **Why**: Testing ensures the Claude integration functions correctly and handles errors appropriately.
  - **What**: Create a test class that:
    - Tests successful chat completion API calls
    - Tests retry mechanism by simulating transient failures
    - Tests error handling for various error types
    - Verifies proper response parsing
    - Tests with different conversation contexts
    - Verifies handling of token limit (8192) edge cases

## Email Service

- [x] **Create separate email configuration file**
  - **Why**: Keeping email configuration in a dedicated properties file improves security and allows for independent updates to email settings.
  - **What**: Create a separate properties file specifically for email configuration that includes:
    - SMTP server host (email-smtp.ap-south-1.amazonaws.com)
    - SMTP port (465)
    - Authentication settings (enabled)
    - SSL settings (enabled)
    - Username (AKIAXWWAENHRKEXWAEU2)
    - Password (BJ+IiVs1NLQg/cOoWTF3Woedp1prO9crMRH0ZK2Cv2HY)
    - From address (no-reply@leucinetech.com)

- [x] **Implement email configuration utility**
  - **Why**: A dedicated configuration utility ensures proper loading and validation of email settings.
  - **What**: Create a utility class that:
    - Loads the email configuration from the separate properties file
    - Validates the required properties are present
    - Provides a clean API for accessing the email configuration values
    - Handles configuration changes at runtime if needed

- [x] **Implement email sending service**
  - **Why**: A dedicated service for email functionality provides a clean interface and reusable component for the application.
  - **What**: Create an email service that:
    - Uses the email configuration utility to get SMTP connection details
    - Provides methods for sending plain text and HTML emails
    - Supports attachments
    - Handles multiple recipients (To, CC, BCC)
    - Implements error handling for various email sending failures
    - Includes logging for email operations
    - Properly manages resources and connections

- [x] **Create test cases for email service**
  - **Why**: Testing ensures the email functionality works correctly before being used in production.
  - **What**: Create a test class that:
    - Tests email sending to verify correct configuration, using "nupur.bhaisare@leucinetech.com" as the test recipient email address
    - Tests different email formats (plain text, HTML)
    - Tests with attachments
    - Tests with multiple recipients (including "nupur.bhaisare@leucinetech.com" as the primary recipient)
    - Verifies error handling for connection failures, invalid addresses, etc.