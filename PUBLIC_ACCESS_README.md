# NexusBlock Network - Public Server Access Guide

This guide explains how to make your NexusBlock server accessible to players over the internet using a public domain.

## Overview

The server uses a **Velocity proxy** architecture:
- **Velocity Proxy** (Port 25565): Receives all player connections
- **Hub Server** (Port 25566): Main lobby and game selection
- **SkyBlock Server** (Port 25567): SkyBlock gameplay
- **Backend servers** are NOT exposed to the internet - only the proxy is

## Prerequisites

### Hardware Requirements
- CPU: 2+ cores
- RAM: 4GB minimum (8GB recommended)
- Storage: 20GB+ SSD
- Network: Static IP or dynamic DNS

### Software Requirements
- Java 17 (for Velocity) or Java 8 (for Paper 1.8.8 backend)
- Maven (for building plugins)
- SSH access (for remote server management)

## Step 1: Port Forwarding

This is REQUIRED for players to connect from outside your network.

### For Home Servers

1. **Access your router**
   - Open browser to router IP (usually 192.168.1.1 or 10.0.0.1)
   - Login with admin credentials

2. **Find Port Forwarding section**
   - Usually under "Advanced" > "Port Forwarding" or "NAT"

3. **Create port forward rule**
   - Name: `NexusBlock`
   - Protocol: `TCP/UDP` (both)
   - External Port: `25565`
   - Internal Port: `25565`
   - Internal IP: Your server's local IP (e.g., 192.168.1.100)

4. **Save and test**
   - Use https://mcsrvstat.org/ to verify accessibility

### For Cloud Servers (AWS, DigitalOcean, etc.)

Ports are usually open by default, but ensure:

1. **Security Group rules**
   - Inbound: TCP 25565 from 0.0.0.0/0
   - Outbound: All traffic allowed

2. **Firewall settings**
   ```bash
   # Ubuntu
   sudo ufw allow 25565/tcp
   sudo ufw allow 25565/udp
   ```

## Step 2: Domain Configuration (Optional but Recommended)

Using a domain makes it easy for players to connect and allows server transfers.

### Buying a Domain
1. Purchase from providers like:
   - Namecheap ($5-10/year)
   - GoDaddy ($10-15/year)
   - Google Domains (now Squarespace)

### Creating DNS Records

#### Option A: A Record (Simpler)
```
Type: A
Name: play (or @ for root)
Value: YOUR_SERVER_PUBLIC_IP
TTL: 3600
```

Players connect: `play.yourdomain.com`

#### Option B: SRV Record (Custom Port)
If using a non-standard port:
```
Type: SRV
Name: _minecraft._tcp.play
Value: 10 25565 play.yourdomain.com
TTL: 3600
```

### Testing DNS
```bash
# Linux/Mac
nslookup play.yourdomain.com

# Windows
ping play.yourdomain.com
```

Should return your server's public IP.

## Step 3: Server Configuration

### Generate Secure Forwarding Secret

The proxy uses a secret for secure player data forwarding:

```bash
# Generate random secret
cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 32 | head -n 1
```

Update `velocity/config/velocity.toml`:
```toml
forwarding-secret = "YourGeneratedSecretHere"
```

### Configure Backend Servers

Update each Paper server's `spigot.yml`:
```yaml
bungeecord: true
```

Update each Paper server's `paper.yml`:
```yaml
velocity-support:
  enabled: true
  forwarding-secret: "YourGeneratedSecretHere"
  online-mode: false
```

## Step 4: Starting the Servers

### Terminal 1: Start Velocity Proxy
```bash
cd /path/to/NexusServer/velocity
./start.sh
```

### Terminal 2: Start Hub Server
```bash
cd /path/to/NexusServer/server-files
java -Xms2G -Xmx4G -jar paper.jar nogui
```

### Terminal 3: Start SkyBlock Server (Optional - separate terminal)
```bash
cd /path/to/NexusServer/server-files-skyblock
java -Xms2G -Xmx4G -jar paper.jar nogui
```

## Step 5: Testing Connectivity

### Local Test
```bash
# Connect from same machine
minecraft-java-launcher -> localhost:25565
```

### External Test
1. Send your public IP or domain to a friend
2. Have them try to connect
3. Check server logs for connection attempts

### Online Tools
- https://mcsrvstat.org/ - Server status checker
- https://ping.eu/port-chk/ - Port checker
- https://www.speedtest.net/ - Network speed test

## Troubleshooting

### Players Can't Connect

**Problem**: Connection timeout
**Solutions**:
- Check port is forwarded correctly
- Verify firewall allows port 25565
- Ensure server is running

**Problem**: "Connection refused"
**Solutions**:
- Velocity proxy not running
- Wrong port in connection string
- Server crashed - check logs

**Problem**: "Outdated server"
**Solutions**:
- Update ViaVersion plugin
- Check proxy version compatibility

### Connection Issues

**Lag or High Ping**:
- Reduce player count
- Optimize Paper settings
- Upgrade server hardware

**Random Disconnects**:
- Check network stability
- Increase timeout settings
- Update network drivers

## Security Considerations

### Protect Your Servers

1. **Don't expose backend ports**
   - Only port 25565 should be accessible
   - Backend servers (25566, 25567) stay private

2. **Use firewall**
   ```bash
   # Allow only 25565
   sudo ufw allow 25565/tcp
   sudo ufw allow 25565/udp
   sudo ufw enable
   ```

3. **Secure SSH**
   - Use key-based authentication
   - Change default SSH port
   - Install fail2ban

4. **Regular updates**
   - Keep Java updated
   - Update Velocity/Paper
   - Patch vulnerabilities

### Prevent Griefing

1. **Configure permissions**
   - Limit admin commands
   - Use permission plugins

2. **Back up regularly**
   ```bash
   # Backup script example
   tar -czf backup-$(date +%Y%m%d).tar.gz server-files/
   ```

3. **Monitor logs**
   - Check for suspicious activity
   - Set up alerts

## Performance Optimization

### Velocity Settings
```toml
[proxy]
compression-threshold = 1
login-rate-limit = 1500

[channels]
compression = 1
```

### Paper Settings
```yaml
world-settings:
  default:
    mob-spawn-range: 4
    despawn-range: 32
    entity-tracking-range: 64
    tick-rates:
      grass-spread: 4
      container-update: 1
```

### System Settings
```bash
# Add to /etc/sysctl.conf
net.core.somaxconn = 65535
net.ipv4.tcp_max_syn_backlog = 65535
net.core.netdev_max_backlog = 65535
```

Apply with: `sudo sysctl -p`

## Quick Reference

| Component | Port | Access |
|-----------|------|--------|
| Velocity Proxy | 25565 | Public (internet) |
| Hub Server | 25566 | Local only |
| SkyBlock Server | 25567 | Local only |

### Common Commands
```bash
# Check if port is open (Linux)
nc -zv localhost 25565

# Check listening ports
netstat -tulpn | grep java

# View active connections
ss -tulpn

# Check firewall status
sudo ufw status
```

## Support

If you encounter issues:

1. Check server logs in `velocity/logs/` and `server-files/logs/`
2. Search for similar issues on GitHub
3. Provide details when asking for help:
   - Operating system
   - Java version
   - Error messages
   - Steps to reproduce

## Next Steps

After successful setup:

1. **Customize server** - Edit config files
2. **Add plugins** - Download from SpigotMC
3. **Create content** - Build minigames
4. **Promote server** - Share on server lists
5. **Monitor growth** - Track player counts
