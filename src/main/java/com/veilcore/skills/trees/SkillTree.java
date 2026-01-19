package com.veilcore.skills.trees;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.veilcore.skills.Skill;

/**
 * Represents a skill tree for a specific skill
 * Contains nodes and their connections/prerequisites
 */
public class SkillTree {
    private final Skill skill;
    private final Map<String, SkillTreeNode> nodes;
    private final Map<String, Set<String>> connections; // fromNode -> Set of toNodes
    private final Map<String, Integer> minLevelRequirements; // connection -> min level required

    public SkillTree(Skill skill) {
        this.skill = skill;
        this.nodes = new HashMap<>();
        this.connections = new HashMap<>();
        this.minLevelRequirements = new HashMap<>();
    }

    public Skill getSkill() {
        return skill;
    }

    /**
     * Add a node to the tree
     */
    public void addNode(SkillTreeNode node) {
        nodes.put(node.getId(), node);
    }

    /**
     * Get a node by ID
     */
    public SkillTreeNode getNode(String nodeId) {
        return nodes.get(nodeId);
    }

    /**
     * Get all nodes in the tree
     */
    public Collection<SkillTreeNode> getAllNodes() {
        return nodes.values();
    }

    /**
     * Get all node IDs
     */
    public Set<String> getNodeIds() {
        return nodes.keySet();
    }

    /**
     * Add a connection between two nodes
     * Any level of the source node allows unlocking the target
     */
    public void addConnection(String fromNodeId, String toNodeId) {
        addConnection(fromNodeId, toNodeId, 1);
    }

    /**
     * Add a connection with a minimum level requirement on the source node
     */
    public void addConnection(String fromNodeId, String toNodeId, int minLevel) {
        connections.computeIfAbsent(fromNodeId, k -> new HashSet<>()).add(toNodeId);
        String connectionKey = fromNodeId + "->" + toNodeId;
        minLevelRequirements.put(connectionKey, minLevel);
    }

    /**
     * Get all nodes connected from a specific node
     */
    public Set<String> getConnectedNodes(String nodeId) {
        return new HashSet<>(connections.getOrDefault(nodeId, new HashSet<>()));
    }

    /**
     * Get all prerequisite nodes for a specific node
     */
    public Set<String> getPrerequisites(String nodeId) {
        Set<String> prerequisites = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : connections.entrySet()) {
            if (entry.getValue().contains(nodeId)) {
                prerequisites.add(entry.getKey());
            }
        }
        return prerequisites;
    }

    /**
     * Get minimum level required on a prerequisite node
     */
    public int getMinLevelRequirement(String fromNodeId, String toNodeId) {
        String connectionKey = fromNodeId + "->" + toNodeId;
        return minLevelRequirements.getOrDefault(connectionKey, 1);
    }

    /**
     * Check if a node can be unlocked based on prerequisites
     */
    public boolean canUnlockNode(String nodeId, Set<String> unlockedNodes, Map<String, Integer> nodeLevels) {
        // Root node is always available
        if (nodeId.equals("root")) {
            return true;
        }

        Set<String> prerequisites = getPrerequisites(nodeId);
        
        // If no prerequisites, node is not accessible
        if (prerequisites.isEmpty()) {
            return false;
        }

        // Check if at least one prerequisite is satisfied
        for (String prereq : prerequisites) {
            if (unlockedNodes.contains(prereq)) {
                int prereqLevel = nodeLevels.getOrDefault(prereq, 0);
                int minLevel = getMinLevelRequirement(prereq, nodeId);
                if (prereqLevel >= minLevel) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if a node is available for unlocking
     * (prerequisites met but node itself not yet unlocked)
     */
    public boolean isNodeAvailable(String nodeId, Set<String> unlockedNodes, Map<String, Integer> nodeLevels) {
        return !unlockedNodes.contains(nodeId) && canUnlockNode(nodeId, unlockedNodes, nodeLevels);
    }

    /**
     * Get all nodes that can currently be unlocked
     */
    public Set<String> getAvailableNodes(Set<String> unlockedNodes, Map<String, Integer> nodeLevels) {
        Set<String> available = new HashSet<>();
        for (String nodeId : nodes.keySet()) {
            if (isNodeAvailable(nodeId, unlockedNodes, nodeLevels)) {
                available.add(nodeId);
            }
        }
        return available;
    }

    /**
     * Get all connections in the tree
     */
    public Map<String, Set<String>> getAllConnections() {
        return new HashMap<>(connections);
    }

    /**
     * Check if a specific connection exists
     */
    public boolean hasConnection(String fromNodeId, String toNodeId) {
        return connections.getOrDefault(fromNodeId, new HashSet<>()).contains(toNodeId);
    }

    @Override
    public String toString() {
        return "SkillTree[" + skill.getDisplayName() + "] with " + nodes.size() + " nodes";
    }
}
