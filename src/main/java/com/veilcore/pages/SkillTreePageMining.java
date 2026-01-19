package com.veilcore.pages;

import java.util.Map;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.veilcore.profile.Profile;
import com.veilcore.skills.ProfileSkills;
import com.veilcore.skills.Skill;
import com.veilcore.skills.tokens.SkillToken.TokenTier;
import com.veilcore.skills.trees.PlayerSkillTreeData;
import com.veilcore.skills.trees.SkillTree;
import com.veilcore.skills.trees.SkillTreeNode;
import com.veilcore.skills.trees.SkillTreeRegistry;

public class SkillTreePageMining extends InteractiveCustomUIPage<SkillTreePageMining.TreeEventData> {
    
    private final Profile profile;
    private final PlayerRef playerRef;
    private final SkillTree tree;
    
    public static class TreeEventData {
        public String action;
        public String nodeId;
        
        public static final BuilderCodec<TreeEventData> CODEC =
                BuilderCodec.builder(TreeEventData.class, TreeEventData::new)
                    .append(new KeyedCodec<>("Action", Codec.STRING),
                        (TreeEventData o, String v) -> o.action = v,
                        (TreeEventData o) -> o.action)
                    .add()
                    .append(new KeyedCodec<>("NodeId", Codec.STRING),
                        (TreeEventData o, String v) -> o.nodeId = v,
                        (TreeEventData o) -> o.nodeId)
                    .add()
                    .build();
    }
    
    public SkillTreePageMining(@Nonnull PlayerRef playerRef, Profile profile) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, TreeEventData.CODEC);
        this.profile = profile;
        this.playerRef = playerRef;
        this.tree = SkillTreeRegistry.getInstance().getSkillTree(Skill.MINING);
    }
    
    @Override
    public void build(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull UICommandBuilder cmd,
            @Nonnull UIEventBuilder evt,
            @Nonnull Store<EntityStore> store
    ) {
        cmd.append("Pages/SkillTreePageMining.ui");
        
        ProfileSkills skills = profile.getSkills();
        PlayerSkillTreeData treeData = skills.getTreeData();
        
        Player player = store.getComponent(ref, Player.getComponentType());
        String displayName = player != null ? player.getDisplayName() : "Player";
        cmd.set("#PlayerName.Text", displayName);
        cmd.set("#SkillName.Text", "Mining Skill Tree");
        
        // Set token counts
        Map<TokenTier, Integer> tokens = treeData.getAllTokenCounts(Skill.MINING.getId());
        cmd.set("#BasicTokens.Text", String.valueOf(tokens.getOrDefault(TokenTier.BASIC, 0)));
        cmd.set("#AdvancedTokens.Text", String.valueOf(tokens.getOrDefault(TokenTier.ADVANCED, 0)));
        cmd.set("#MasterTokens.Text", String.valueOf(tokens.getOrDefault(TokenTier.MASTER, 0)));
        
        // Build tree nodes
        buildTreeNodes(cmd, evt, treeData);
        
        // Close button
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton", 
            new EventData().append("Action", "Close"));
        
        // Back to skills button
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#BackButton", 
            new EventData().append("Action", "Back"));
    }
    
    private void buildTreeNodes(UICommandBuilder cmd, UIEventBuilder evt, PlayerSkillTreeData treeData) {
        // Root node - always unlocked
        SkillTreeNode rootNode = tree.getNode("root");
        if (rootNode != null) {
            cmd.set("#RootNodeName.Text", rootNode.getName());
            cmd.set("#RootNodeStatus.Text", "UNLOCKED");
        }
        
        // Mining Fortune node (upgradable 50 levels)
        SkillTreeNode fortuneNode = tree.getNode("mining_fortune");
        if (fortuneNode != null) {
            int currentLevel = treeData.getNodeLevel(Skill.MINING.getId(), "mining_fortune");
            boolean isUnlocked = currentLevel > 0;
            
            cmd.set("#FortuneNodeName.Text", fortuneNode.getName());
            cmd.set("#FortuneNodeLevel.Text", currentLevel + "/" + fortuneNode.getMaxLevel());
            
            if (currentLevel >= fortuneNode.getMaxLevel()) {
                cmd.set("#FortuneNodeStatus.Text", "MAX LEVEL");
            } else if (isUnlocked) {
                int nextCost = fortuneNode.getTokenCost(currentLevel + 1);
                cmd.set("#FortuneNodeStatus.Text", "Upgrade: " + nextCost + " tokens");
                
                // Can upgrade?
                evt.addEventBinding(CustomUIEventBindingType.Activating, "#FortuneNodeButton", 
                    new EventData().append("Action", "Upgrade").append("NodeId", "mining_fortune"));
            } else {
                int unlockCost = fortuneNode.getTokenCost(1);
                cmd.set("#FortuneNodeStatus.Text", "Unlock: " + unlockCost + " tokens");
                
                evt.addEventBinding(CustomUIEventBindingType.Activating, "#FortuneNodeButton", 
                    new EventData().append("Action", "Unlock").append("NodeId", "mining_fortune"));
            }
            
            // Description
            String description = fortuneNode.getDescription(Math.max(1, currentLevel));
            cmd.set("#FortuneNodeDescription.Text", description);
        }
    }
    
    @Override
    public void handleDataEvent(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull TreeEventData data
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());
        
        switch (data.action) {
            case "Close":
                player.getPageManager().setPage(ref, store, com.hypixel.hytale.protocol.packets.interface_.Page.None);
                break;
            case "Back":
                player.getPageManager().openCustomPage(ref, store, new SkillsPageMining(playerRef, profile));
                break;
            case "Unlock":
            case "Upgrade":
                handleNodeUpgrade(ref, store, data.nodeId);
                break;
        }
    }
    
    private void handleNodeUpgrade(Ref<EntityStore> ref, Store<EntityStore> store, String nodeId) {
        SkillTreeNode node = tree.getNode(nodeId);
        if (node == null) {
            sendNotification("Error", "Invalid node!", "#FF5555");
            return;
        }
        
        ProfileSkills skills = profile.getSkills();
        PlayerSkillTreeData treeData = skills.getTreeData();
        int currentLevel = treeData.getNodeLevel(Skill.MINING.getId(), nodeId);
        int nextLevel = currentLevel + 1;
        
        // Check if can upgrade
        if (nextLevel > node.getMaxLevel()) {
            sendNotification("Max Level", "This node is already at max level!", "#FF5555");
            return;
        }
        
        // Check token cost
        int cost = node.getTokenCost(nextLevel);
        TokenTier requiredTier = node.getRequiredTokenTier();
        Map<TokenTier, Integer> tokens = treeData.getAllTokenCounts(Skill.MINING.getId());
        int availableTokens = tokens.getOrDefault(requiredTier, 0);
        
        if (availableTokens < cost) {
            sendNotification("Insufficient Tokens", 
                "Need " + cost + " " + requiredTier.getDisplayName() + " tokens (have " + availableTokens + ")",
                "#FF5555");
            return;
        }
        
        // Spend tokens and upgrade node
        if (!treeData.useTokens(Skill.MINING.getId(), requiredTier, cost)) {
            sendNotification("Error", "Failed to spend tokens!", "#FF5555");
            return;
        }
        treeData.setNodeLevel(Skill.MINING.getId(), nodeId, nextLevel);
        
        // Save profile via profile manager
        // Profile data is saved automatically when updated
        
        // Notify success
        String action = currentLevel == 0 ? "Unlocked" : "Upgraded";
        sendNotification(action + "!", 
            node.getName() + " (Level " + nextLevel + "/" + node.getMaxLevel() + ")",
            "#55FF55");
        
        // Refresh page
        Player player = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
        player.getPageManager().openCustomPage(ref, store, new SkillTreePageMining(playerRef, profile));
    }
    
    private void sendNotification(String title, String message, String color) {
        PacketHandler packetHandler = playerRef.getPacketHandler();
        Message primary = Message.raw(title).color(color).bold(true);
        Message secondary = Message.raw(message).color("#FFFFFF");
        ItemWithAllMetadata icon = new ItemStack("Tool_Pickaxe_Stone", 1).toPacket();
        NotificationUtil.sendNotification(packetHandler, primary, secondary, icon);
    }
}
