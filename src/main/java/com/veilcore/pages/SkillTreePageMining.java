package com.veilcore.pages;

import java.util.Map;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.veilcore.profile.Profile;
import com.veilcore.skills.Skill;
import com.veilcore.skills.tokens.SkillToken.TokenTier;
import com.veilcore.skills.trees.PlayerSkillTreeData;
import com.veilcore.skills.trees.SkillTree;
import com.veilcore.skills.trees.SkillTreeNode;
import com.veilcore.skills.trees.SkillTreeRegistry;

/**
 * Dynamic skill tree page for Mining
 * Displays all nodes in a grid layout with real-time updates
 */
public class SkillTreePageMining extends InteractiveCustomUIPage<SkillTreePageMining.TreeEventData> {
    
    private final Profile profile;
    private final PlayerRef playerRef;
    private String selectedNodeId = null;
    
    public static class TreeEventData {
        public String action;
        
        public static final BuilderCodec<TreeEventData> CODEC =
                BuilderCodec.builder(TreeEventData.class, TreeEventData::new)
                    .append(new KeyedCodec<>("Action", Codec.STRING),
                        (TreeEventData o, String v) -> o.action = v,
                        (TreeEventData o) -> o.action)
                    .add()
                    .build();
    }
    
    public SkillTreePageMining(@Nonnull PlayerRef playerRef, Profile profile) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, TreeEventData.CODEC);
        this.profile = profile;
        this.playerRef = playerRef;
    }
    
    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder cmd, 
                     @Nonnull UIEventBuilder evt, @Nonnull Store<EntityStore> store) {
        
        cmd.append("Pages/SkillTreePageMining.ui");
        
        PlayerSkillTreeData treeData = profile.getSkills().getTreeData();
        SkillTree miningTree = SkillTreeRegistry.getInstance().getSkillTree(Skill.MINING);
        
        if (miningTree == null) {
            cmd.set("#TreeMessage.Text", "Error: Mining tree not found!");
            return;
        }
        
        // Update token displays
        Map<TokenTier, Integer> tokens = treeData.getAllTokenCounts(Skill.MINING.getId());
        cmd.set("#BasicTokens.Text", String.valueOf(tokens.getOrDefault(TokenTier.BASIC, 0)));
        cmd.set("#AdvancedTokens.Text", String.valueOf(tokens.getOrDefault(TokenTier.ADVANCED, 0)));
        cmd.set("#MasterTokens.Text", String.valueOf(tokens.getOrDefault(TokenTier.MASTER, 0)));
        
        // Build the node grid dynamically
        buildNodeGrid(cmd, evt, miningTree, treeData);
        
        // Update info panel if a node is selected
        if (selectedNodeId != null) {
            updateInfoPanel(cmd, evt, miningTree, treeData, selectedNodeId);
        } else {
            // Default info panel
            cmd.set("#NodeName.Text", "Select a node");
            cmd.set("#NodeLevel.Text", "");
            cmd.set("#NodeDescription.Text", "Click on a node to view details and upgrade options.");
            cmd.set("#NodeCost.Text", "");
            cmd.set("#NodeStatus.Text", "");
        }
        
        // Event bindings for buttons
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton",
            new EventData().append("Action", "Close"));
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#ResetButton",
            new EventData().append("Action", "Reset"));
    }
    
    private void buildNodeGrid(UICommandBuilder cmd, UIEventBuilder evt, SkillTree tree, PlayerSkillTreeData treeData) {
        // Clear the tree message
        cmd.set("#TreeMessage.Text", "");
        
        // TODO: For now, just show a simplified list of nodes
        // In future, this should render a proper grid layout based on TreeGridPosition
        StringBuilder nodeList = new StringBuilder();
        nodeList.append("Available Nodes:\n\n");
        
        for (SkillTreeNode node : tree.getAllNodes()) {
            int currentLevel = treeData.getNodeLevel(Skill.MINING.getId(), node.getId());
            int maxLevel = node.getMaxLevel();
            
            String status;
            if (currentLevel >= maxLevel) {
                status = "✓ MAX";
            } else if (currentLevel > 0) {
                status = String.format("Level %d/%d", currentLevel, maxLevel);
            } else {
                boolean canAfford = treeData.canAffordNode(Skill.MINING.getId(), node, currentLevel);
                status = canAfford ? "Available" : "Locked";
            }
            
            nodeList.append(String.format("%s - %s\n", node.getName(), status));
        }
        
        cmd.set("#TreeMessage.Text", nodeList.toString());
    }
    
    private void updateInfoPanel(UICommandBuilder cmd, UIEventBuilder evt, SkillTree tree, 
                                 PlayerSkillTreeData treeData, String nodeId) {
        SkillTreeNode node = tree.getNode(nodeId);
        if (node == null) {
            cmd.set("#NodeName.Text", "Node not found");
            return;
        }
        
        int currentLevel = treeData.getNodeLevel(Skill.MINING.getId(), nodeId);
        int maxLevel = node.getMaxLevel();
        int nextLevel = currentLevel + 1;
        
        // Update node info
        cmd.set("#NodeName.Text", node.getName());
        
        if (currentLevel >= maxLevel) {
            cmd.set("#NodeLevel.Text", String.format("MAX LEVEL (%d)", maxLevel));
            cmd.set("#NodeDescription.Text", node.getDescription(currentLevel));
            cmd.set("#NodeCost.Text", "");
            cmd.set("#NodeStatus.Text", "This node is at maximum level!");
            // Disable upgrade button (we'll need to handle this in the UI)
        } else {
            cmd.set("#NodeLevel.Text", String.format("Level %d / %d", currentLevel, maxLevel));
            cmd.set("#NodeDescription.Text", node.getDescription(nextLevel));
            
            int cost = node.getTokenCost(nextLevel);
            TokenTier requiredTier = node.getRequiredTokenTier();
            cmd.set("#NodeCost.Text", String.format("Cost: %d %s %s", cost, requiredTier.getSymbol(), requiredTier.getDisplayName()));
            
            boolean canAfford = treeData.canAffordNode(Skill.MINING.getId(), node, currentLevel);
            if (canAfford) {
                cmd.set("#NodeStatus.Text", "");
                evt.addEventBinding(CustomUIEventBindingType.Activating, "#UpgradeButton",
                    new EventData().append("Action", "Upgrade_" + nodeId));
            } else {
                cmd.set("#NodeStatus.Text", String.format("Not enough tokens!\nNeed %d %s tokens", cost, requiredTier.getDisplayName()));
            }
        }
    }
    
    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, 
                                TreeEventData data) {
        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerSkillTreeData treeData = profile.getSkills().getTreeData();
        SkillTree miningTree = SkillTreeRegistry.getInstance().getSkillTree(Skill.MINING);
        
        if (data.action == null) {
            return;
        }
        
        // Handle upgrade actions (format: "Upgrade_<nodeId>")
        if (data.action.startsWith("Upgrade_")) {
            String nodeId = data.action.substring(8); // Remove "Upgrade_" prefix
            if (miningTree != null) {
                SkillTreeNode node = miningTree.getNode(nodeId);
                if (node != null) {
                    int currentLevel = treeData.getNodeLevel(Skill.MINING.getId(), nodeId);
                    
                    if (currentLevel < node.getMaxLevel()) {
                        int cost = node.getTokenCost(currentLevel + 1);
                        TokenTier requiredTier = node.getRequiredTokenTier();
                        
                        if (treeData.useTokens(Skill.MINING.getId(), requiredTier, cost)) {
                            treeData.setNodeLevel(Skill.MINING.getId(), nodeId, currentLevel + 1);
                            
                            playerRef.sendMessage(Message.raw(String.format(
                                "Upgraded %s to level %d!", 
                                node.getName(), 
                                currentLevel + 1
                            )).color("#55FF55"));
                            
                            selectedNodeId = nodeId; // Keep same node selected
                            rebuild();
                        } else {
                            playerRef.sendMessage(Message.raw("Not enough tokens!").color("#FF5555"));
                        }
                    }
                }
            }
            return;
        }
        
        // Handle select node actions (format: "Select_<nodeId>")
        if (data.action.startsWith("Select_")) {
            selectedNodeId = data.action.substring(7); // Remove "Select_" prefix
            rebuild();
            return;
        }
        
        // Handle other actions
        switch (data.action) {
            case "Reset":
                // Reset the entire tree
                if (miningTree != null) {
                    Map<TokenTier, Integer> refundedTokens = treeData.resetSkillTree(Skill.MINING.getId(), miningTree);
                    
                    int totalRefunded = refundedTokens.values().stream().mapToInt(Integer::intValue).sum();
                    if (totalRefunded > 0) {
                        playerRef.sendMessage(Message.raw(String.format(
                            "Reset Mining skill tree! Refunded: %d◆ %d◈ %d✦", 
                            refundedTokens.getOrDefault(TokenTier.BASIC, 0),
                            refundedTokens.getOrDefault(TokenTier.ADVANCED, 0),
                            refundedTokens.getOrDefault(TokenTier.MASTER, 0)
                        )).color("#FFD700"));
                    } else {
                        playerRef.sendMessage(Message.raw("Nothing to reset!").color("#FF5555"));
                    }
                    
                    selectedNodeId = null;
                    rebuild();
                }
                break;
                
            case "Close":
                player.getPageManager().setPage(ref, store, 
                    com.hypixel.hytale.protocol.packets.interface_.Page.None);
                break;
        }
    }
}
