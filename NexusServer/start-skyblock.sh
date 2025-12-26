#!/bin/bash

# SkyBlock Server Startup Script
# This starts the SkyBlock backend server

echo "==========================================="
echo "  NexusBlock Network - SkyBlock Server"
echo "==========================================="

# Navigate to server directory
cd "$(dirname "$0")"

# Check for Paper JAR
if [ ! -f "paper.jar" ]; then
    echo "Error: paper.jar not found!"
    echo "Please ensure the Paper server JAR is in this directory."
    exit 1
fi

# Check for Java
if ! command -v java &> /dev/null; then
    echo "Error: Java not found!"
    exit 1
fi

# Copy NexusCore plugin if built
if [ -f "../target/NexusCore-1.0.0.jar" ]; then
    cp ../target/NexusCore-1.0.0.jar plugins/
    echo "Copied NexusCore plugin to plugins/"
fi

# Copy ViaVersion if downloaded
if [ -f "../server-files/plugins/ViaVersion.jar" ]; then
    cp ../server-files/plugins/ViaVersion.jar plugins/
    echo "Copied ViaVersion plugin to plugins/"
fi

echo "Starting SkyBlock server..."
echo ""

# Start the server with optimized settings
java -Xms512M -Xmx4G \
    -XX:+UseG1GC \
    -XX:+ParallelRefProcEnabled \
    -XX:MaxGCPauseMillis=100 \
    -XX:+UnlockExperimentalVMOptions \
    -XX:G1NewSizePercent=30 \
    -XX:G1MaxNewSizePercent=50 \
    -XX:G1HeapRegionSize=8M \
    -XX:G1ReservePercent=20 \
    -XX:InitiatingHeapOccupancyPercent=15 \
    -XX:G1HeapWastePercent=5 \
    -jar paper.jar nogui

echo ""
echo "SkyBlock server stopped."
