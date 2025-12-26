package com.nexus.core.utils;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * NMS utilities for version-independent operations
 * Handles packet sending, entity spawning, and other NMS operations
 */
public class NMSUtils {

    private final String version;
    private final int versionNumber;

    public NMSUtils() throws Exception {
        Server server = Bukkit.getServer();
        String packageName = server.getClass().getPackage().getName();
        this.version = packageName.substring(packageName.lastIndexOf('.') + 1);
        this.versionNumber = parseVersionNumber();

        Bukkit.getLogger().info("Detected server version: " + version);
    }

    /**
     * Parse version number from version string
     */
    private int parseVersionNumber() {
        try {
            if (version.startsWith("v1_8")) {
                return 8;
            } else if (version.startsWith("v1_9")) {
                return 9;
            } else if (version.startsWith("v1_10")) {
                return 10;
            } else if (version.startsWith("v1_11")) {
                return 11;
            } else if (version.startsWith("v1_12")) {
                return 12;
            } else if (version.startsWith("v1_13")) {
                return 13;
            } else if (version.startsWith("v1_14")) {
                return 14;
            } else if (version.startsWith("v1_15")) {
                return 15;
            } else if (version.startsWith("v1_16")) {
                return 16;
            } else if (version.startsWith("v1_17")) {
                return 17;
            } else if (version.startsWith("v1_18")) {
                return 18;
            } else if (version.startsWith("v1_19")) {
                return 19;
            } else if (version.startsWith("v1_20")) {
                return 20;
            } else if (version.startsWith("v1_21")) {
                return 21;
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return 8; // Default to 1.8
    }

    /**
     * Get version string
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get version number
     */
    public int getVersionNumber() {
        return versionNumber;
    }

    /**
     * Check if version is 1.9 or higher
     */
    public boolean isOneDotNinePlus() {
        return versionNumber >= 9;
    }

    /**
     * Check if version is 1.13 or higher
     */
    public boolean isOneDotThirteenPlus() {
        return versionNumber >= 13;
    }

    /**
     * Check if version is 1.16 or higher
     */
    public boolean isOneDotSixteenPlus() {
        return versionNumber >= 16;
    }

    /**
     * Get NMS class by name
     */
    public Class<?> getNMSClass(String className) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + version + "." + className);
    }

    /**
     * Get CraftBukkit class by name
     */
    public Class<?> getCraftBukkitClass(String className) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + version + "." + className);
    }

    /**
     * Send a packet to a player
     */
    public void sendPacket(Object playerConnection, Object packet) {
        try {
            Method sendMethod = playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet"));
            sendMethod.invoke(playerConnection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get player connection object
     */
    public Object getPlayerConnection(Entity entity) throws Exception {
        Class<?> craftPlayerClass = getCraftBukkitClass("entity.CraftPlayer");
        Object craftPlayer = craftPlayerClass.cast(entity);
        Method getHandleMethod = craftPlayerClass.getMethod("getHandle");
        return getHandleMethod.invoke(craftPlayer);
    }

    /**
     * Create a packet for spawning an entity
     */
    public Object createSpawnPacket(Entity entity) throws Exception {
        Class<?> entityClass = getNMSClass("Entity");
        Class<?> entityTypesClass = getNMSClass("EntityTypes");

        Object nmsEntity = entity.getClass().getMethod("getHandle").invoke(entity);
        int entityTypeId = (int) entityTypesClass.getMethod("a", entityClass).invoke(null, nmsEntity);

        // Create packet
        Class<?> packetPlayOutSpawnClass = getNMSClass("PacketPlayOutSpawn");
        Constructor<?> constructor = packetPlayOutSpawnClass.getConstructor(
            entityClass,
            int.class
        );

        return constructor.newInstance(nmsEntity, entityTypeId);
    }

    /**
     * Send title packet to player
     */
    public void sendTitle(Object playerConnection, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        try {
            // Title packet
            Class<?> packetPlayOutTitleClass = getNMSClass("PacketPlayOutTitle");
            Class<?> titlesClass = getNMSClass("PacketPlayOutTitle$EnumTitleAction");

            Object titleAction = titlesClass.getEnumConstants()[0]; // TITLE
            Object titlePacket = packetPlayOutTitleClass.getConstructor(
                titlesClass,
                getNMSClass("IChatBaseComponent"),
                int.class, int.class, int.class
            ).newInstance(titleAction, createChatComponent(title), fadeIn, stay, fadeOut);

            sendPacket(playerConnection, titlePacket);

            // Subtitle
            if (subtitle != null && !subtitle.isEmpty()) {
                Object subtitleAction = titlesClass.getEnumConstants()[1]; // SUBTITLE
                Object subtitlePacket = packetPlayOutTitleClass.getConstructor(
                    titlesClass,
                    getNMSClass("IChatBaseComponent"),
                    int.class, int.class, int.class
                ).newInstance(subtitleAction, createChatComponent(subtitle), fadeIn, stay, fadeOut);

                sendPacket(playerConnection, subtitlePacket);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Send action bar to player
     */
    public void sendActionBar(Object playerConnection, String message) {
        try {
            Class<?> packetPlayOutChatClass = getNMSClass("PacketPlayOutChat");
            Class<?> chatPacketClass = getNMSClass("PacketPlayOutChat$EnumPlayerChatActions");

            Object action = chatPacketClass.getEnumConstants()[2]; // ACTION_BAR

            Object chatComponent = createChatComponent(message);
            Object packet = packetPlayOutChatClass.getConstructor(
                getNMSClass("IChatBaseComponent"),
                chatPacketClass
            ).newInstance(chatComponent, action);

            sendPacket(playerConnection, packet);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a chat component from string
     */
    public Object createChatComponent(String text) throws Exception {
        Class<?> chatComponentClass = getNMSClass("IChatBaseComponent");
        Class<?> chatSerializerClass = getNMSClass("IChatBaseComponent$ChatSerializer");

        // Use the serializer to create a component from JSON
        Method serializeMethod = chatSerializerClass.getMethod("a", String.class);
        return serializeMethod.invoke(null, "{\"text\":\"" + text + "\"}");
    }

    /**
     * Resend all player information (for tablist updates)
     */
    public void updatePlayerInfo(org.bukkit.entity.Player player) {
        try {
            Object playerConnection = getPlayerConnection(player);
            Class<?> playerInfoDataClass = getNMSClass("PlayerInfoData");

            // Get player list packet
            Class<?> packetPlayOutPlayerInfoClass = getNMSClass("PacketPlayOutPlayerInfo");
            Class<?> playerInfoActionClass = getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction");

            Object updateAction = playerInfoActionClass.getEnumConstants()[2]; // UPDATE_GAME_MODE

            // Note: Full implementation would require building PlayerInfoData array
            // This is a simplified version

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get entity ID for an entity
     */
    public int getEntityId(Entity entity) throws Exception {
        Object nmsEntity = entity.getClass().getMethod("getHandle").invoke(entity);
        Field idField = nmsEntity.getClass().getField("id");
        return idField.getInt(nmsEntity);
    }
}
