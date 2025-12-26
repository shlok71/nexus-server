#!/bin/bash

# NexusBlock Network - Setup Script
# This script sets up the complete server environment

echo "==========================================="
echo "  NexusBlock Network - Setup Script"
echo "==========================================="
echo ""

# Check for Java
echo "Checking Java installation..."
if ! command -v java &> /dev/null; then
    echo "Java not found! Installing OpenJDK..."
    apt-get update -qq > /dev/null 2>&1
    apt-get install -y openjdk-8-jdk > /dev/null 2>&1
    echo "Java installed successfully!"
else
    echo "Java found: $(java -version 2>&1 | head -n 1)"
fi

# Check for Maven
echo ""
echo "Checking Maven installation..."
if ! command -v mvn &> /dev/null; then
    echo "Maven not found! Installing Maven..."
    apt-get update -qq > /dev/null 2>&1
    apt-get install -y maven > /dev/null 2>&1
    echo "Maven installed successfully!"
else
    echo "Maven found: $(mvn -version | head -n 1)"
fi

# Build the plugin
echo ""
echo "Building NexusCore plugin..."
cd "$(dirname "$0")"
mvn clean package -DskipTests > /dev/null 2>&1

if [ $? -eq 0 ]; then
    echo "Plugin built successfully!"
else
    echo "Plugin build failed! Please check errors above."
    exit 1
fi

# Download ViaVersion for cross-version support
echo ""
echo "Downloading ViaVersion plugin..."
cd "$(dirname "$0")/server-files/plugins"
if [ ! -f "ViaVersion.jar" ]; then
    curl -sL https://ci.viaversion.com/job/ViaVersion-1.8/lastSuccessfulBuild/artifact/target/ViaVersion-*.jar -o ViaVersion.jar
    echo "ViaVersion downloaded!"
else
    echo "ViaVersion already present!"
fi

# Download Paper API if needed
echo ""
echo "Downloading Paper server..."
cd "$(dirname "$0")/server-files"
if [ ! -f "paper.jar" ] && [ ! -f "spigot.jar" ]; then
    # Download Paper 1.8.8
    curl -sL https://papermc.io/api/v1/paper/1.8.8/latest/download -o paper.jar
    echo "Paper server downloaded!"
else
    echo "Server JAR already present!"
fi

# Make scripts executable
echo ""
echo "Setting permissions..."
cd "$(dirname "$0")"
chmod +x build.sh start.sh setup.sh

echo ""
echo "==========================================="
echo "  Setup Complete!"
echo "==========================================="
echo ""
echo "Next steps:"
echo "1. Edit server-files/server.properties to configure your server"
echo "2. Build the plugin: ./build.sh"
echo "3. Start the server: ./start.sh"
echo ""
echo "For cross-version support (1.8-1.21+):"
echo "  - ViaVersion is automatically downloaded"
echo "  - Configure in plugins/ViaVersion/config.yml"
echo ""
echo "For cracked/offline support:"
echo "  - online-mode is already set to false in server.properties"
echo "  - Use NexusAuth for account protection (optional)"
echo ""
