#!/bin/bash

# ERP System Run Script

echo "=== ERP System ==="
echo "Setting up and running the ERP application..."

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    echo "Please install Java 15 or higher"
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    echo "Please install Maven"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 15 ]; then
    echo "Error: Java 15 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

echo "Java version: $(java -version 2>&1 | head -n 1)"
echo "Maven version: $(mvn -version 2>&1 | head -n 1)"

# Compile the project
echo ""
echo "Compiling the project..."
mvn clean compile

if [ $? -ne 0 ]; then
    echo "Error: Compilation failed"
    exit 1
fi

# Run tests
echo ""
echo "Running tests..."
mvn test

if [ $? -ne 0 ]; then
    echo "Warning: Some tests failed, but continuing..."
fi

# Run the application
echo ""
echo "Starting ERP System..."
echo "Note: Make sure MariaDB is running and the database is set up"
echo "Default login: admin / admin123"
echo ""

mvn exec:java -Dexec.mainClass="com.sis.app.SISApplication"

echo ""
echo "ERP System has been stopped."


