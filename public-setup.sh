#!/bin/bash

# NexusBlock Network - Complete Server Setup
# This script sets up everything needed for public access

echo "==========================================="
echo "  NexusBlock Network - Public Server Setup"
echo "==========================================="
echo ""

# Color codes
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[✓]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[!]${NC} $1"
}

print_error() {
    echo -e "${RED}[✗]${NC} $1"
}

# Step 1: Check Java
echo "Step 1: Checking Java installation..."
if command -v java &> /dev/null; then
    print_status "Java found: $(java -version 2>&1 | head -n 1)"
else
    print_error "Java not found! Installing..."
    apt-get update -qq > /dev/null 2>&1
    apt-get install -y openjdk-17-jdk > /dev/null 2>&1
    print_status "Java 17 installed"
fi
echo ""

# Step 2: Check Maven
echo "Step 2: Checking Maven..."
if command -v mvn &> /dev/null; then
    print_status "Maven found: $(mvn -version | head -n 1)"
else
    print_warning "Maven not found! Installing..."
    apt-get update -qq > /dev/null 2>&1
    apt-get install -y maven > /dev/null 2>&1
    print_status "Maven installed"
fi
echo ""

# Step 3: Build the plugin
echo "Step 3: Building NexusCore plugin..."
cd "$(dirname "$0")"
if [ -f "pom.xml" ]; then
    mvn clean package -DskipTests > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        print_status "Plugin built successfully!"
    else
        print_error "Plugin build failed!"
        exit 1
    fi
else
    print_error "pom.xml not found!"
    exit 1
fi
echo ""

# Step 4: Download required server software
echo "Step 4: Downloading server software..."

# Download Velocity
cd velocity
if [ ! -f "velocity.jar" ]; then
    print_warning "Downloading Velocity proxy..."
    curl -sL https://api.papermc.io/v2/projects/velocity/versions/3.3.0-SNAPSHOT/builds/396/downloads/velocity-3.3.0-SNAPSHOT-396.jar -o velocity.jar
    if [ -f "velocity.jar" ]; then
        print_status "Velocity downloaded!"
    else
        print_warning "Could not download Velocity automatically"
        echo "  Please download from: https://papermc.io/software/velocity"
    fi
else
    print_status "Velocity already present"
fi

# Download Paper for backend servers
cd ../server-files
if [ ! -f "paper.jar" ]; then
    print_warning "Downloading Paper 1.8.8..."
    curl -sL https://api.papermc.io/v2/projects/paper/versions/1.8.8/builds/445/downloads/paper-1.8.8-445.jar -o paper.jar
    if [ -f "paper.jar" ]; then
        print_status "Paper 1.8.8 downloaded!"
    else
        print_error "Could not download Paper!"
    fi
else
    print_status "Paper already present"
fi

# Download ViaVersion for cross-version support
if [ ! -f "plugins/ViaVersion.jar" ]; then
    print_warning "Downloading ViaVersion..."
    mkdir -p plugins
    curl -sL https://ci.viaversion.com/job/ViaVersion-1.8/lastSuccessfulBuild/artifact/target/ViaVersion-*.jar -o plugins/ViaVersion.jar
    if [ -f "plugins/ViaVersion.jar" ]; then
        print_status "ViaVersion downloaded!"
    fi
fi
echo ""

# Step 5: Configure for public access
echo "Step 5: Configuring for public access..."

# Generate secure forwarding secret
SECRET=$(cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 32 | head -n 1)
cd ../velocity/config

# Update velocity.toml with secret
if [ -f "velocity.toml" ]; then
    sed -i "s/forwarding-secret = \"YourSecretKeyHere-ChangeThisToSomethingSecure!\"/forwarding-secret = \"$SECRET\"/" velocity.toml
    print_status "Updated forwarding secret"
fi
cd ../..
echo ""

# Step 6: Create startup scripts
echo "Step 6: Creating startup scripts..."
chmod +x velocity/start.sh
chmod +x start.sh
chmod +x build.sh
print_status "Scripts made executable"
echo ""

# Summary
echo "==========================================="
echo "  Setup Complete!"
echo "==========================================="
echo ""
echo -e "${GREEN}Next Steps for Public Access:${NC}"
echo ""
echo "1. PORT FORWARDING (Required):"
echo "   - Open TCP/UDP port 25565 on your router"
echo "   - Forward to this machine's local IP"
echo ""
echo "2. DOMAIN CONFIGURATION (Optional but recommended):"
echo "   - Buy a domain (e.g., from Namecheap/GoDaddy)"
echo "   - Create A record: play.yourdomain.com -> YOUR_PUBLIC_IP"
echo "   - Optionally create SRV record for custom port"
echo ""
echo "3. START THE SERVERS:"
echo "   Terminal 1: ./velocity/start.sh    (Proxy)"
echo "   Terminal 2: ./start.sh              (Hub server)"
echo "   Terminal 3: (for SkyBlock: ./start-skyblock.sh)"
echo ""
echo "4. CONNECT:"
echo "   - Direct IP: YOUR_PUBLIC_IP:25565"
echo "   - With domain: play.yourdomain.com"
echo ""
echo "5. CHECK CONNECTIVITY:"
echo "   - Use https://mcsrvstat.org/ to check if server is visible"
echo ""
print_warning "Important Security Notes:"
echo "   - Keep your forwarding secret secure"
echo "   - Don't expose backend ports (25566, 25567) to the internet"
echo "   - Use a firewall to block unauthorized access"
echo ""
echo "For detailed setup guide, see: PUBLIC_ACCESS_README.md"
