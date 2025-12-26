#!/bin/bash

# NexusBlock Network - Server Start Script
# This script starts the Minecraft server

echo "==========================================="
echo "  NexusBlock Network - Starting Server"
echo "==========================================="

# Navigate to server directory
cd "$(dirname "$0")/server-files"

# Check for Paper/Spigot jar
if [ ! -f "spigot.jar" ] && [ ! -f "paper.jar" ]; then
    echo "Error: No server JAR found!"
    echo "Please download Paper or Spigot 1.8.8 and place it in the server-files directory."
    echo "Download: https://papermc.io/downloads"
    exit 1
fi

# Determine which JAR to use
SERVER_JAR=""
if [ -f "paper.jar" ]; then
    SERVER_JAR="paper.jar"
elif [ -f "spigot.jar" ]; then
    SERVER_JAR="spigot.jar"
fi

echo "Using server JAR: $SERVER_JAR"

# Check for Java
if ! command -v java &> /dev/null; then
    echo "Error: Java not found!"
    echo "Please install Java 8 or higher."
    exit 1
fi

# Display Java version
echo "Java version:"
java -version 2>&1 | head -n 1

# Set memory allocation
MAX_MEMORY=${MAX_MEMORY:-2G}
MIN_MEMORY=${MIN_MEMORY:-1G}

echo "Memory: $MIN_MEMORY - $MAX_MEMORY"

# Start the server
echo ""
echo "Starting server..."
echo "Server will be ready in a few moments..."
echo ""

java -Xms$MIN_MEMORY -Xmx$MAX_MEMORY -XX:+UseG1GC -jar $SERVER_JAR nogui

echo ""
echo "Server stopped."
