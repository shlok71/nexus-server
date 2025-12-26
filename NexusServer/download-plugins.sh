#!/bin/bash

# NexusBlock Network - Plugin Download Script
# Downloads all essential plugins for the server

echo "========================================"
echo "  NexusBlock Network - Plugin Setup"
echo "========================================"
echo ""

PLUGINS_DIR="server-files/plugins"
MKDIR="mkdir -p"

# Create plugins directory if it doesn't exist
$MKDIR "$PLUGINS_DIR"

echo "Downloading essential plugins..."
echo ""

# ViaVersion - Cross-version compatibility
echo "[1/8] Downloading ViaVersion..."
curl -sL "https://viaversion.com/files/latest/ViaVersion.jar" -o "$PLUGINS_DIR/ViaVersion.jar"
if [ -f "$PLUGINS_DIR/ViaVersion.jar" ]; then
    echo "  ✓ ViaVersion downloaded"
else
    echo "  ✗ Failed to download ViaVersion"
fi

# ViaBackwards - Allow older clients to join
echo "[2/8] Downloading ViaBackwards..."
curl -sL "https://viaversion.com/files/latest/ViaBackwards.jar" -o "$PLUGINS_DIR/ViaBackwards.jar"
if [ -f "$PLUGINS_DIR/ViaBackwards.jar" ]; then
    echo "  ✓ ViaBackwards downloaded"
else
    echo "  ✗ Failed to download ViaBackwards"
fi

# ViaRewind - Allow newer clients to join old servers
echo "[3/8] Downloading ViaRewind..."
curl -sL "https://viaversion.com/files/latest/ViaRewind.jar" -o "$PLUGINS_DIR/ViaRewind.jar"
if [ -f "$PLUGINS_DIR/ViaRewind.jar" ]; then
    echo "  ✓ ViaRewind downloaded"
else
    echo "  ✗ Failed to download ViaRewind"
fi

# ProtocolLib - Required for ViaVersion
echo "[4/8] Downloading ProtocolLib..."
curl -sL "https://ci.dmulloy2.net/job/ProtocolLib/lastStableBuild/artifact/target/ProtocolLib.jar" -o "$PLUGINS_DIR/ProtocolLib.jar"
if [ -f "$PLUGINS_DIR/ProtocolLib.jar" ]; then
    echo "  ✓ ProtocolLib downloaded"
else
    echo "  ✗ Failed to download ProtocolLib"
fi

# Vault - Economy API
echo "[5/8] Downloading Vault..."
curl -sL "https://github.com/MilkBowl/VaultAPI/releases/download/1.7/Vault-1.7.jar" -o "$PLUGINS_DIR/Vault.jar"
if [ -f "$PLUGINS_DIR/Vault.jar" ]; then
    echo "  ✓ Vault downloaded"
else
    echo "  ✗ Failed to download Vault"
fi

# PlaceholderAPI - For custom placeholders
echo "[6/8] Downloading PlaceholderAPI..."
curl -sL "https://repo.extendedclip.com/content/repositories/placeholderapi/latest/PlaceholderAPI.jar" -o "$PLUGINS_DIR/PlaceholderAPI.jar"
if [ -f "$PLUGINS_DIR/PlaceholderAPI.jar" ]; then
    echo "  ✓ PlaceholderAPI downloaded"
else
    echo "  ✗ Failed to download PlaceholderAPI"
fi

# EssentialsX - Basic server utilities
echo "[7/8] Downloading EssentialsX..."
curl -sL "https://essentialsx.net/downloads/latest/EssentialsX.jar" -o "$PLUGINS_DIR/EssentialsX.jar"
if [ -f "$PLUGINS_DIR/EssentialsX.jar" ]; then
    echo "  ✓ EssentialsX downloaded"
else
    echo "  ✗ Failed to download EssentialsX"
fi

# LuckPerms - Permission management
echo "[8/8] Downloading LuckPerms..."
curl -sL "https://luckperms.net/download/loader/5.4/LuckPerms-Bukkit-5.4.jar" -o "$PLUGINS_DIR/LuckPerms.jar"
if [ -f "$PLUGINS_DIR/LuckPerms.jar" ]; then
    echo "  ✓ LuckPerms downloaded"
else
    echo "  ✗ Failed to download LuckPerms"
fi

echo ""
echo "========================================"
echo "  Plugin Download Complete!"
echo "========================================"
echo ""
echo "Plugins installed in: $PLUGINS_DIR"
echo ""
echo "Note: You may need to download some plugins manually"
echo "      as automated downloads may fail for some URLs."
echo ""
