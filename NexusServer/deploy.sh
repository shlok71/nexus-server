#!/bin/bash

# NexusBlock Network - Quick Deploy Script
# Run this on your cloud VM or local machine

echo "==========================================="
echo "  NexusBlock Network - Quick Deploy"
echo "==========================================="
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Check Java
echo "Checking Java..."
if command -v java &> /dev/null; then
    echo -e "${GREEN}✓${NC} Java found: $(java -version 2>&1 | head -n 1)"
else
    echo -e "${RED}✗${NC} Java not found!"
    echo "Install Java 17 with: sudo apt update && sudo apt install openjdk-17-jdk"
    exit 1
fi

# Create directories
echo ""
echo "Setting up directories..."
mkdir -p velocity/plugins velocity/config server-files/plugins server-files/logs server-files/worlds

# Download Velocity
echo ""
echo "Downloading Velocity proxy..."
if [ ! -f "velocity/velocity.jar" ]; then
    curl -sL https://api.papermc.io/v2/projects/velocity/versions/3.3.0-SNAPSHOT/builds/396/downloads/velocity-3.3.0-SNAPSHOT-396.jar -o velocity/velocity.jar
    echo -e "${GREEN}✓${NC} Velocity downloaded"
else
    echo -e "${GREEN}✓${NC} Velocity already present"
fi

# Download Paper
echo ""
echo "Downloading Paper 1.8.8..."
if [ ! -f "server-files/paper.jar" ]; then
    curl -sL https://api.papermc.io/v2/projects/paper/versions/1.8.8/builds/445/downloads/paper-1.8.8-445.jar -o server-files/paper.jar
    echo -e "${GREEN}✓${NC} Paper downloaded"
else
    echo -e "${GREEN}✓${NC} Paper already present"
fi

# Download ViaVersion
echo ""
echo "Downloading ViaVersion..."
if [ ! -f "server-files/plugins/ViaVersion.jar" ]; then
    curl -sL https://ci.viaversion.com/job/ViaVersion-1.8/lastSuccessfulBuild/artifact/target/ViaVersion-*.jar -o server-files/plugins/ViaVersion.jar
    echo -e "${GREEN}✓${NC} ViaVersion downloaded"
else
    echo -e "${GREEN}✓${NC} ViaVersion already present"
fi

# Generate forwarding secret
echo ""
echo "Generating secure forwarding secret..."
SECRET=$(cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 32 | head -n 1)
echo "$SECRET" > forwarding_secret.txt
echo -e "${GREEN}✓${NC} Secret saved to forwarding_secret.txt"

# Update Velocity config
echo ""
echo "Configuring Velocity..."
cat > velocity/config/velocity.toml << 'VELOCITY_EOF'
[minecraft]
compress-t = 1
proxy-protocol = false

[proxy]
bind = "0.0.0.0:25565"
motd = "&aWelcome to &bNexusBlock Network&a!"
max-players = 500
online-mode = false
player-info-forwarding-mode = "bungeeguard"
forwarding-secret = "SECRET_PLACEHOLDER"
enable-api = true

[servers]
hub = { address = "127.0.0.1:25566", restricted = false }
skyblock = { address = "127.0.0.1:25567", restricted = false }

[forced-hosts]
play.nexusblock.net = ["hub"]

[log]
timestamps = true
file = "velocity.log"

[commands]
plugins = ["velocity.plugins"]
version = ["velocity.version"]
VELOCITY_EOF

sed -i "s/SECRET_PLACEHOLDER/$SECRET/" velocity/config/velocity.toml
echo -e "${GREEN}✓${NC} Velocity configured"

# Update Paper config
echo ""
echo "Configuring Paper for IP forwarding..."
cat > server-files/spigot.yml << 'SPIGOT_EOF'
settings:
  bungeecord: true
  online-mode: false
SPIGOT_EOF

cat > server-files/paper.yml << 'PAPER_EOF'
settings:
  bungeecord: true
  online-mode: false

velocity-support:
  enabled: true
  online-mode: false
  forwarding-secret: "SECRET_PLACEHOLDER"
  secret: "SECRET_PLACEHOLDER"
PAPER_EOF

sed -i "s/SECRET_PLACEHOLDER/$SECRET/" server-files/paper.yml
echo -e "${GREEN}✓${NC} Paper configured"

# Make scripts executable
chmod +x velocity/start.sh start.sh start-skyblock.sh public-setup.sh

echo ""
echo "==========================================="
echo -e "${GREEN}  Deployment Complete!"
echo "==========================================="
echo ""
echo -e "${YELLOW}Next Steps:${NC}"
echo ""
echo "1. To START THE SERVERS:"
echo "   Terminal 1: ./velocity/start.sh"
echo "   Terminal 2: ./start.sh"
echo ""
echo "2. To CONNECT (on same machine):"
echo "   Server: localhost:25565"
echo ""
echo "3. To MAKE PUBLIC (on cloud VM):"
echo "   - Ensure port 25565 is open in firewall"
echo "   - Use your VM's public IP"
echo "   - Or configure domain DNS"
echo ""
echo "4. Your forwarding secret: $SECRET"
echo "   (Saved in forwarding_secret.txt)"
echo ""
echo -e "${YELLOW}For full public access guide, see:${NC} PUBLIC_ACCESS_README.md"
