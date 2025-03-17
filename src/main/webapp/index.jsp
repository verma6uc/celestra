<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Celestra - Java Servlet Application</title>
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            line-height: 1.6;
            color: #333;
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
        }
        h1 {
            color: #2c3e50;
            border-bottom: 2px solid #3498db;
            padding-bottom: 10px;
        }
        .card {
            background-color: #f9f9f9;
            border-radius: 5px;
            padding: 20px;
            margin-bottom: 20px;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        }
        code {
            background-color: #f1f1f1;
            padding: 2px 5px;
            border-radius: 3px;
            font-family: Consolas, Monaco, 'Andale Mono', monospace;
        }
        .btn {
            display: inline-block;
            background-color: #3498db;
            color: white;
            padding: 10px 15px;
            text-decoration: none;
            border-radius: 4px;
            margin-right: 10px;
            margin-top: 10px;
        }
        .btn:hover {
            background-color: #2980b9;
        }
        pre {
            background-color: #f1f1f1;
            padding: 15px;
            border-radius: 5px;
            overflow-x: auto;
        }
        #response {
            min-height: 100px;
            background-color: #f8f9fa;
            border: 1px solid #ddd;
            padding: 10px;
            border-radius: 4px;
            margin-top: 20px;
        }
    </style>
</head>
<body>
    <h1>Welcome to Celestra</h1>
    
    <div class="card">
        <h2>About This Application</h2>
        <p>
            Celestra is a Java servlet-based web application that demonstrates:
        </p>
        <ul>
            <li>Java 17 features</li>
            <li>Servlet API usage</li>
            <li>JSON processing with Gson</li>
            <li>Maven project structure</li>
            <li>Tomcat 7 deployment</li>
        </ul>
    </div>
    
    <div class="card">
        <h2>Test the API</h2>
        <p>Click the buttons below to test the HelloServlet API:</p>
        
        <button class="btn" onclick="testGet()">Test GET Request</button>
        <button class="btn" onclick="testPost()">Test POST Request</button>
        
        <h3>Response:</h3>
        <pre id="response">Response will appear here...</pre>
    </div>
    
    <div class="card">
        <h2>API Endpoints</h2>
        <p>The following endpoints are available:</p>
        <ul>
            <li><code>GET /hello</code> - Returns a greeting message</li>
            <li><code>POST /hello</code> - Echoes back the received data</li>
        </ul>
    </div>
    
    <script>
        async function testGet() {
            try {
                const response = await fetch('hello');
                const data = await response.json();
                document.getElementById('response').textContent = JSON.stringify(data, null, 2);
            } catch (error) {
                document.getElementById('response').textContent = 'Error: ' + error.message;
            }
        }
        
        async function testPost() {
            try {
                const response = await fetch('hello', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        test: 'Hello from client',
                        timestamp: new Date().toISOString()
                    })
                });
                const data = await response.json();
                document.getElementById('response').textContent = JSON.stringify(data, null, 2);
            } catch (error) {
                document.getElementById('response').textContent = 'Error: ' + error.message;
            }
        }
    </script>
</body>
</html>
