#!/bin/bash

# NexusBlock Network - Velocity Proxy Startup Script
# This script starts the Velocity proxy server for public access

echo "==========================================="
echo "  NexusBlock Network - Proxy Server"
echo "==========================================="

# Navigate to velocity directory
cd "$(dirname "$0")/velocity"

# Check for Velocity JAR
if [ ! -f "velocity.jar" ]; then
    echo "Error: velocity.jar not found!"
    echo "Please download Velocity from:"
    echo "  https://papermc.io/software/velocity"
    echo ""
    echo "And place it in the velocity directory."
    exit 1
fi

# Check for Java 17+
if ! command -v java &> /dev/null; then
    echo "Error: Java not found!"
    exit 1
fi

echo "Java version:"
java -version 2>&1 | head -n 1

# Set memory allocation
MAX_MEMORY=${MAX_MEMORY:-2G}
MIN_MEMORY=${MIN_MEMORY:-1G}

echo "Memory: $MIN_MEMORY - $MAX_MEMORY"

# Start Velocity proxy
echo ""
echo "Starting Velocity proxy..."
echo "Server will be ready when players connect!"
echo ""

java -Xms$MIN_MEMORY -Xmx$MAX_MEMORY -XX:+UseG1GC -XX:+ParallelRefProcEnabled -jar velocity.jar nogui

echo ""
echo "Proxy server stopped."
