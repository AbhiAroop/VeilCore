package com.veilcore.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.veilcore.VeilCorePlugin;
import com.veilcore.profile.Profile;
import com.veilcore.profile.ProfileStats;

import javax.annotation.Nonnull;

/**
 * Admin command to set a player's stat value
 * Usage: /setstats <player> <stat> <value>
 * Example: /setstats PlayerName health 200
 */
public class SetStatsCommand extends AbstractPlayerCommand {

    private final VeilCorePlugin plugin;
    private final RequiredArg<String> targetArg;
    private final RequiredArg<String> statArg;
    private final RequiredArg<String> valueArg;

    public SetStatsCommand(VeilCorePlugin plugin) {
        super("setstats", "Set a player's stat value");
        this.plugin = plugin;
        
        // Define arguments
        this.targetArg = withRequiredArg("player", "Target player's username", ArgTypes.STRING);
        this.statArg = withRequiredArg("stat", "Stat name (e.g. health, armor, speed)", ArgTypes.STRING);
        this.valueArg = withRequiredArg("value", "New value for the stat", ArgTypes.STRING);
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        // Parse arguments
        String targetName = context.get(targetArg);
        String statName = context.get(statArg).toLowerCase();
        String valueStr = context.get(valueArg);
        
        // Find target player
        PlayerRef targetPlayerRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
        
        if (targetPlayerRef == null) {
            playerRef.sendMessage(Message.raw("Player '" + targetName + "' not found or not online!").color("#FF5555"));
            return;
        }
        
        Player targetPlayer = store.getComponent(targetPlayerRef.getReference(), Player.getComponentType());
        if (targetPlayer == null) {
            playerRef.sendMessage(Message.raw("Target player not found!").color("#FF5555"));
            return;
        }
        
        Profile profile = plugin.getProfileManager().getActiveProfile(targetPlayer.getUuid());
        if (profile == null) {
            playerRef.sendMessage(Message.raw("Target player doesn't have an active profile!").color("#FF5555"));
            return;
        }

        ProfileStats stats = profile.getStats();
        
        // Try to set the stat - handle different data types
        try {
            boolean success = setStat(stats, statName, valueStr);
            if (success) {
                // Save profile
                plugin.getProfileManager().saveProfile(profile);
                
                // If health stat was modified, update the player's max health
                if (statName.equals("health")) {
                    com.veilcore.listeners.HealthSyncListener.updatePlayerMaxHealthByUuid(
                        plugin, 
                        targetPlayer.getUuid(), 
                        stats.getHealth()
                    );
                }
                
                // If stamina stat was modified, update the player's max stamina
                if (statName.equals("stamina")) {
                    com.veilcore.listeners.StaminaSyncListener.updatePlayerMaxStaminaByUuid(
                        plugin, 
                        targetPlayer.getUuid(), 
                        stats.getStamina()
                    );
                }
                
                // If stamina regen stat was modified, suppress default builtin regen
                if (statName.equals("staminaregen") || statName.equals("stamina_regen")) {
                    com.veilcore.listeners.StaminaRegenModifier.updatePlayerStaminaRegenByUuid(
                        plugin,
                        targetPlayer.getUuid()
                    );
                }
                
                // If speed stat was modified, update the player's movement speed
                if (statName.equals("speed")) {
                    com.veilcore.listeners.SpeedSyncListener.updatePlayerSpeedByUuid(
                        plugin, 
                        targetPlayer.getUuid(), 
                        stats.getSpeed()
                    );
                }
                
                // Send success message to admin
                PacketHandler senderPacket = playerRef.getPacketHandler();
                Message senderPrimary = Message.raw("Stat Updated").color("#55FF55").bold(true);
                Message senderSecondary = Message.raw(String.format("%s's %s set to %s", targetName, statName, valueStr)).color("#FFFFFF");
                ItemWithAllMetadata senderIcon = new ItemStack("Hytale_Menu_Inventory", 1).toPacket();
                NotificationUtil.sendNotification(senderPacket, senderPrimary, senderSecondary, senderIcon);
                
                // Notify target player
                PacketHandler targetPacket = targetPlayerRef.getPacketHandler();
                Message targetPrimary = Message.raw("STAT UPDATED").color("#FFD700").bold(true);
                Message targetSecondary = Message.raw(String.format("Your %s has been set to %s by an admin", statName, valueStr)).color("#FFFFFF");
                ItemWithAllMetadata targetIcon = new ItemStack("Hytale_Menu_Inventory", 1).toPacket();
                NotificationUtil.sendNotification(targetPacket, targetPrimary, targetSecondary, targetIcon);
            } else {
                playerRef.sendMessage(Message.raw("Invalid stat name: " + statName).color("#FF5555"));
                playerRef.sendMessage(Message.raw("Available stats: health, armor, magicresist, physicaldamage, magicdamage, mana, totalmana, stamina, speed, criticaldamage, criticalchance, burstdamage, burstchance, cooldownreduction, lifesteal, rangeddamage, attackspeed, omnivamp, healthregen, miningfortune, farmingfortune, lootingfortune, fishingfortune, lurepotency, fishingresilience, fishingfocus, fishingprecision, seamonsteraffinity, treasuresense, manaregen, luck, currenthealth, foodlevel, saturation, exhaustion, explevel, expprogress, attackrange, buildrange, size, miningspeed, kills, deaths, playtime").color("#FFAA00"));
            }
        } catch (NumberFormatException e) {
            playerRef.sendMessage(Message.raw("Invalid value format for stat '" + statName + "': " + valueStr).color("#FF5555"));
            playerRef.sendMessage(Message.raw("Use a number (integer or decimal) for this stat").color("#FFAA00"));
        } catch (Exception e) {
            playerRef.sendMessage(Message.raw("Error setting stat: " + e.getMessage()).color("#FF5555"));
        }
    }
    
    /**
     * Sets a stat on the ProfileStats object
     * @param stats The ProfileStats object
     * @param statName The name of the stat to set
     * @param valueStr The value as a string
     * @return true if the stat was set successfully, false if stat name is invalid
     */
    private boolean setStat(ProfileStats stats, String statName, String valueStr) throws NumberFormatException {
        switch (statName) {
            // Combat Stats (int)
            case "health":
                stats.setHealth(Integer.parseInt(valueStr));
                return true;
            case "armor":
                stats.setArmor(Integer.parseInt(valueStr));
                return true;
            case "magicresist":
            case "mr":
                stats.setMagicResist(Integer.parseInt(valueStr));
                return true;
            case "physicaldamage":
            case "pd":
                stats.setPhysicalDamage(Integer.parseInt(valueStr));
                return true;
            case "magicdamage":
            case "md":
                stats.setMagicDamage(Integer.parseInt(valueStr));
                return true;
            case "mana":
                stats.setMana(Integer.parseInt(valueStr));
                return true;
            case "totalmana":
                stats.setTotalMana(Integer.parseInt(valueStr));
                return true;
            case "stamina":
                stats.setStamina(Integer.parseInt(valueStr));
                return true;
            case "cooldownreduction":
            case "cdr":
                stats.setCooldownReduction(Integer.parseInt(valueStr));
                return true;
            case "rangeddamage":
            case "rd":
                stats.setRangedDamage(Integer.parseInt(valueStr));
                return true;
                
            // Combat Stats (double)
            case "speed":
                stats.setSpeed(Double.parseDouble(valueStr));
                return true;
            case "criticaldamage":
            case "critdmg":
                stats.setCriticalDamage(Double.parseDouble(valueStr));
                return true;
            case "criticalchance":
            case "critchance":
                stats.setCriticalChance(Double.parseDouble(valueStr));
                return true;
            case "burstdamage":
            case "burstdmg":
                stats.setBurstDamage(Double.parseDouble(valueStr));
                return true;
            case "burstchance":
                stats.setBurstChance(Double.parseDouble(valueStr));
                return true;
            case "lifesteal":
            case "ls":
                stats.setLifeSteal(Double.parseDouble(valueStr));
                return true;
            case "attackspeed":
            case "as":
                stats.setAttackSpeed(Double.parseDouble(valueStr));
                return true;
            case "omnivamp":
                stats.setOmnivamp(Double.parseDouble(valueStr));
                return true;
            case "healthregen":
            case "regen":
                stats.setHealthRegen(Double.parseDouble(valueStr));
                return true;
            case "staminaregen":
            case "stamina_regen":
                stats.setStaminaRegen(Double.parseDouble(valueStr));
                return true;
                
            // Fortune Stats (double)
            case "miningfortune":
                stats.setMiningFortune(Double.parseDouble(valueStr));
                return true;
            case "farmingfortune":
                stats.setFarmingFortune(Double.parseDouble(valueStr));
                return true;
            case "lootingfortune":
                stats.setLootingFortune(Double.parseDouble(valueStr));
                return true;
            case "fishingfortune":
                stats.setFishingFortune(Double.parseDouble(valueStr));
                return true;
                
            // Fishing Stats (int and double)
            case "lurepotency":
                stats.setLurePotency(Integer.parseInt(valueStr));
                return true;
            case "fishingresilience":
                stats.setFishingResilience(Double.parseDouble(valueStr));
                return true;
            case "fishingfocus":
                stats.setFishingFocus(Double.parseDouble(valueStr));
                return true;
            case "fishingprecision":
                stats.setFishingPrecision(Double.parseDouble(valueStr));
                return true;
            case "seamonsteraffinity":
                stats.setSeaMonsterAffinity(Double.parseDouble(valueStr));
                return true;
            case "treasuresense":
                stats.setTreasureSense(Double.parseDouble(valueStr));
                return true;
                
            // Resource Stats (int)
            case "manaregen":
                stats.setManaRegen(Integer.parseInt(valueStr));
                return true;
            case "luck":
                stats.setLuck(Integer.parseInt(valueStr));
                return true;
                
            // Minecraft Base Stats
            case "currenthealth":
                stats.setCurrentHealth(Double.parseDouble(valueStr));
                return true;
            case "foodlevel":
                stats.setFoodLevel(Integer.parseInt(valueStr));
                return true;
            case "saturation":
                stats.setSaturation(Float.parseFloat(valueStr));
                return true;
            case "exhaustion":
                stats.setExhaustion(Float.parseFloat(valueStr));
                return true;
            case "explevel":
                stats.setExpLevel(Integer.parseInt(valueStr));
                return true;
            case "expprogress":
                stats.setExpProgress(Float.parseFloat(valueStr));
                return true;
            case "attackrange":
                stats.setAttackRange(Double.parseDouble(valueStr));
                return true;
            case "buildrange":
                stats.setBuildRange(Double.parseDouble(valueStr));
                return true;
            case "size":
                stats.setSize(Double.parseDouble(valueStr));
                return true;
            case "miningspeed":
                stats.setMiningSpeed(Double.parseDouble(valueStr));
                return true;
                
            // Gameplay Tracking (int and long)
            case "kills":
                stats.setKills(Integer.parseInt(valueStr));
                return true;
            case "deaths":
                stats.setDeaths(Integer.parseInt(valueStr));
                return true;
            case "playtime":
                stats.setPlayTime(Long.parseLong(valueStr));
                return true;
                
            default:
                return false;
        }
    }
}
