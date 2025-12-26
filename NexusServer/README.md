# NexusBlock Network - Hypixel-Style Minecraft Server

A complete Minecraft server implementation inspired by Hypixel, featuring a hub system, SkyBlock game mode, minigame framework, and cross-version compatibility.

## Features

### Core Features
- **Hub System**: Fully featured lobby with game selector, cosmetics menu, and player profiles
- **SkyBlock**: Complete SkyBlock implementation with island creation, terrain generation, and resource farming
- **Minigame Framework**: Extensible framework for creating custom minigames
- **Economy System**: Dual currency system (coins and gems) with Vault integration
- **Authentication**: Optional account system for cracked/offline mode servers
- **Chat System**: Formatted chat with spam filtering and cooldowns

### Technical Features
- **Cross-Version Support**: Works with Minecraft 1.8 through 1.21+ using ViaVersion
- **Cracked Support**: Full offline-mode/cracked account support
- **High Performance**: Optimized for Paper/Spigot 1.8.8 with modern JVM settings
- **Modular Design**: Plugin-based architecture with separate managers
- **Database**: SQLite storage with easy migration path to MySQL

## Requirements

- Java 8 or higher (Java 17 recommended for modern versions)
- Maven for building
- Paper 1.8.8 or Spigot 1.8.8 as base server
- 2GB RAM minimum (4GB+ recommended)

## Quick Start

### 1. Run Setup
```bash
./setup.sh
```

This will:
- Install Java and Maven (if missing)
- Build the NexusCore plugin
- Download ViaVersion for cross-version support
- Download Paper server

### 2. Configure Server
Edit `server-files/server.properties`:
```properties
server-name=NexusBlock Network
server-port=25565
online-mode=false  # Set to false for cracked support
max-players=100
```

### 3. Start Server
```bash
./start.sh
```

### 4. Connect
Join using any Minecraft client version (1.8-1.21):
```
Server Address: localhost:25565
```

## Commands

### Hub Commands
- `/hub` or `/lobby` - Return to hub
- `/spawn` - Go to spawn point
- `/warp <name>` - Teleport to warp
- `/warp set <name>` - Create warp (requires permission)

### Communication Commands
- `/msg <player> <message>` - Send private message
- `/reply <message>` - Reply to last message
- `/tpa <player>` - Request teleport
- `/tpaccept` or `/tpdeny` - Accept/deny teleport

### Admin Commands
- `/nexus info` - Server information
- `/nexus tps` - View server TPS
- `/nexus reload` - Reload configuration
- `/nexus broadcast <msg>` - Broadcast message

### SkyBlock Commands
- `/island create` - Create new island
- `/island home` - Teleport to island
- `/island help` - View island commands

## Configuration

### Main Configuration (config.yml)
```yaml
hub:
  world-name: hub
  build-radius: 50
  protection:
    no-pvp: true
    no-hunger-loss: true
  double-jump:
    enabled: true
    max-jumps: 1

auth:
  enabled: false  # Set to true for cracked server security
  session-timeout: 300

economy:
  starting-coins: 1000
  starting-gems: 0

skyblock:
  world-name: skyblock
  default-island-size: 100
```

### Cross-Version Support

ViaVersion is automatically downloaded to support clients from 1.8 to 1.21+. Configure in `plugins/ViaVersion/config.yml`:

```yaml
supported-versions:
- "1.8.x"
- "1.9.x"
- "1.10.x"
- "1.11.x"
- "1.12.x"
- "1.13.x"
- "1.14.x"
- "1.15.x"
- "1.16.x"
- "1.17.x"
- "1.18.x"
- "1.19.x"
- "1.20.x"
- "1.21.x"
```

## Architecture

```
NexusServer/
├── src/main/java/com/nexus/
│   ├── core/                    # Main plugin class and commands
│   │   ├── commands/           # Command executors
│   │   └── listeners/          # Event listeners
│   ├── hub/                    # Hub management
│   ├── skyblock/               # SkyBlock game mode
│   ├── minigames/              # Minigame framework
│   ├── economy/                # Economy system
│   ├── auth/                   # Authentication
│   ├── database/               # Database management
│   └── utils/                  # Utility classes
├── server-files/
│   ├── plugins/                # Plugin JARs
│   ├── worlds/                 # World data
│   └── logs/                   # Server logs
└── pom.xml                     # Maven build configuration
```

## Adding Custom Minigames

Extend the `Minigame` class to create custom minigames:

```java
public class MyMinigame extends Minigame {
    public MyMinigame(MinigameManager manager) {
        super(manager, "mygame", "My Custom Game", 10);
        setLobbyLocation(new Location(Bukkit.getWorld("hub"), 0, 100, 0));
    }

    @Override
    public boolean addPlayer(Player player) {
        // Add player to game
        return true;
    }

    @Override
    public void removePlayer(Player player) {
        // Remove player from game
    }

    @Override
    public void onPlayerJoin(Player player) {
        player.sendMessage(ChatColor.GREEN + "Welcome to My Custom Game!");
    }

    @Override
    public void onPlayerLeave(Player player) {
        // Handle player leaving
    }

    @Override
    public void startGame() {
        // Start the game logic
    }

    @Override
    public void endGame() {
        // End the game and award rewards
    }
}
```

Register in `MinigameManager`:
```java
registerMinigame(new MyMinigame(this));
```

## Troubleshooting

### Server Won't Start
1. Check Java is installed: `java -version`
2. Check port 25565 is not in use
3. Check logs in `server-files/logs/` for errors

### Players Can't Connect
1. Verify `online-mode=false` in server.properties
2. Check firewall allows port 25565
3. Ensure ViaVersion is in plugins folder

### Plugin Errors
1. Check plugin loaded: `/nexus info`
2. View latest logs: `tail -f server-files/logs/latest.log`
3. Reload plugin: `/nexus reload`

### Performance Issues
1. Reduce `view-distance` in server.properties
2. Increase RAM allocation in start.sh
3. Disable unused world generation

## Credits

- **PaperMC** - High-performance server software
- **ViaVersion** - Cross-version compatibility
- **Vault** - Economy API integration

## License

This project is open source and available under the MIT License.

## Support

For issues and feature requests, please open a GitHub issue.
