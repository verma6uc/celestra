# Celestra

A Java 17 Servlet-based web application with Gson for JSON processing.

## Project Overview

Celestra is a simple Java web application that demonstrates:

- Java 17 features
- Servlet API usage
- JSON processing with Gson
- Maven project structure
- Tomcat 7 deployment via Maven plugin

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Web browser

## Project Structure

```
celestra/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── celestra/
│   │   │           ├── servlet/
│   │   │           │   └── HelloServlet.java
│   │   │           └── util/
│   │   │               └── JsonResponseUtil.java
│   │   ├── resources/
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   └── web.xml
│   │       └── index.jsp
│   └── test/
└── pom.xml
```

## Building and Running

### Build the Project

```bash
mvn clean package
```

### Run with Tomcat Maven Plugin

```bash
mvn tomcat7:run
```

The application will be available at: http://localhost:8080/

## API Endpoints

- `GET /hello` - Returns a greeting message
- `POST /hello` - Echoes back the received data

## Development

### Adding New Servlets

1. Create a new servlet class in the `com.celestra.servlet` package
2. Annotate it with `@WebServlet("/your-path")`
3. Implement the required HTTP methods (doGet, doPost, etc.)

### Working with JSON

Use the `JsonResponseUtil` class to send JSON responses:

```java
JsonObject jsonResponse = new JsonObject();
jsonResponse.addProperty("key", "value");
JsonResponseUtil.sendJsonResponse(response, jsonResponse);
```

## License

This project is open source and available under the MIT License.