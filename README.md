# NexusBlock Network - Hypixel-Style Minecraft Server

A complete Minecraft server implementation inspired by Hypixel, featuring a hub system, full Hypixel-style SkyBlock game mode, minigame framework, and public server access support.

## Features

### Core Features
- **Hub System**: Fully featured lobby with game selector, cosmetics menu, and player profiles
- **Hypixel-SkyBlock**: Full implementation with minions, quests, NPC shops, HotM skill tree, and treasure system
- **Minigame Framework**: Extensible framework for creating custom minigames (BedWars, Duels, etc.)
- **Economy System**: Dual currency system (coins and gems) with Vault integration
- **Authentication**: Optional account system for cracked/offline mode servers
- **Chat System**: Formatted chat with spam filtering and cooldowns

### SkyBlock Features (Hypixel-Style)
- **Minion System**: Automated resource gathering with tiers, storage, and offline collection
- **Quest System**: Story quests, daily challenges, and progression tracking
- **NPC Shops**: Full vendor system with buy/sell mechanics
- **Heart of the Mountain (HotM)**: Mining skill tree with perks and powder currency
- **Treasure System**: Spawning treasure chests with randomized rewards

### Technical Features
- **Cross-Version Support**: Works with Minecraft 1.8 through 1.21+ using ViaVersion
- **Cracked Support**: Full offline-mode/cracked account support
- **High Performance**: Optimized for Paper/Spigot 1.8.8 with modern JVM settings
- **Modular Design**: Plugin-based architecture with separate managers
- **Database**: SQLite storage with easy migration path to MySQL
- **Public Access Ready**: Velocity proxy setup for domain-based server access

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
- `/minion` - Minion management commands
- `/quests` or `/quest` - Open quest log
- `/hotm` - Open Heart of the Mountain interface
- `/shop` - Open shop catalog
- `/sell` - Sell items to shop

### Minion Commands
- `/minion place <type>` - Place a minion
- `/minion upgrade` - Upgrade selected minion
- `/minion storage` - Access minion storage
- `/minion collect` - Collect all items
- `/minion help` - View minion help

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

### Public Server Access

For making your server accessible over the internet, see `PUBLIC_ACCESS_README.md`:

```bash
# Quick setup for public access
./public-setup.sh
```

This will:
1. Install all dependencies
2. Build the plugin
3. Download Velocity proxy
4. Download Paper servers
5. Generate secure forwarding secrets
6. Configure everything for public access

#### Server Architecture
```
Internet (Port 25565)
       ↓
   Velocity Proxy
       ↓
   ┌───┴───┐
   ↓       ↓
Hub    SkyBlock
(25566) (25567)
```

#### Domain Setup
1. Buy a domain (e.g., from Namecheap)
2. Create A record: `play.yourdomain.com` → Your Public IP
3. Open port 25565 on your router/firewall
4. Start the proxy: `./velocity/start.sh`
```

## Architecture

```
NexusServer/
├── src/main/java/com/nexus/
│   ├── core/                    # Main plugin class and commands
│   │   ├── commands/           # Command executors
│   │   └── listeners/          # Event listeners
│   ├── hub/                    # Hub management
│   ├── skyblock/               # Hypixel-style SkyBlock
│   │   ├── minions/           # Minion system
│   │   ├── quests/            # Quest system
│   │   ├── shops/             # NPC shops
│   │   ├── hotm/              # Heart of the Mountain
│   │   └── treasure/          # Treasure chests
│   ├── minigames/              # Minigame framework
│   ├── economy/                # Economy system
│   ├── auth/                   # Authentication
│   ├── database/               # Database management
│   └── utils/                  # Utility classes
├── velocity/                   # Velocity proxy for public access
│   ├── config/                # Proxy configuration
│   └── start.sh               # Proxy startup script
├── server-files/
│   ├── plugins/               # Plugin JARs
│   ├── worlds/                # World data
│   └── logs/                  # Server logs
├── PUBLIC_ACCESS_README.md    # Public server guide
└── pom.xml                    # Maven build configuration
```

## Hypixel-Style SkyBlock Features

This implementation includes full Hypixel-style SkyBlock features:

### Minion System
Automated resource gathering that works even while offline:
- **30+ Minion Types**: From Cobblestone to Enderman minions
- **11 Tiers**: Each tier increases speed and capacity
- **Storage System**: Minions collect items in their inventory
- **Offline Collection**: Calculate resources gathered while away
- **Upgrades**: Spend coins to upgrade minion tiers

### Quest System
- **Story Quests**: Progression through the SkyBlock adventure
- **Mining Quests**: Gather ores and resources
- **Farming Quests**: Grow and harvest crops
- **Combat Quests**: Defeat mobs and bosses
- **Rewards**: Coins, gems, and experience

### NPC Shops
- **8 Shop Types**: Coal Miner, Iron Merchant, Gold Merchant, Diamond Dealer, Farm Merchant, Lumberjack, Builder, Magic Merchant
- **Buy/Sell**: Purchase items and sell resources
- **Dynamic Pricing**: Based on supply and demand

### Heart of the Mountain (HotM)
Mining skill tree with 5 tiers:
- **Mining Speed**: Up to 30% faster mining
- **Mining Fortune**: Up to 65% bonus drops
- **Special Perks**: Crystal Infusion, Titan Perseverance, Chimera
- **Powder System**: Mithril and Gemstone Powder

### Treasure System
- **5 Treasure Types**: Common, Uncommon, Rare, Epic, Legendary
- **Spawning**: Random treasure chests appear around the world
- **Rewards**: Coins, ores, gems, and rare items
- **Timers**: Chests despawn after time limit

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
