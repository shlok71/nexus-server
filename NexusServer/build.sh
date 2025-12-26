#!/bin/bash

# NexusBlock Network - Build Script
# This script builds the NexusCore plugin

echo "==========================================="
echo "  NexusBlock Network - Plugin Build"
echo "==========================================="

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven not found! Installing Maven..."
    apt-get update -qq
    apt-get install -y maven > /dev/null 2>&1
fi

# Navigate to project directory
cd "$(dirname "$0")"

# Clean previous builds
echo "Cleaning previous builds..."
mvn clean > /dev/null 2>&1

# Build the plugin
echo "Building NexusCore plugin..."
mvn package -DskipTests

# Check if build was successful
if [ $? -eq 0 ]; then
    echo ""
    echo "==========================================="
    echo "  Build Successful!"
    echo "==========================================="
    echo "Plugin location: target/NexusCore-1.0.0.jar"
    echo ""
    echo "To deploy:"
    echo "  1. Copy the JAR to your server's plugins folder"
    echo "  2. Download ViaVersion plugin for cross-version support"
    echo "  3. Start your server"
    echo ""
else
    echo ""
    echo "==========================================="
    echo "  Build Failed!"
    echo "==========================================="
    echo "Please check the error messages above."
    exit 1
fi
