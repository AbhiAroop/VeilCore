package com.veilcore.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.veilcore.VeilCorePlugin;
import com.veilcore.profile.Profile;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Profile selection page - displays existing profiles and allows creating new ones.
 */
public class ProfileSelectionPage extends InteractiveCustomUIPage<ProfileSelectionPage.ProfileSelectionEventData> {

    private final List<Profile> profiles;

    /**
     * Event data for profile selection.
     */
    public static class ProfileSelectionEventData {
        public String action;      // "Select", "Delete", or "CreateNew"
        public String profileId;   // UUID of selected profile
        
        public static final BuilderCodec<ProfileSelectionEventData> CODEC = 
            BuilderCodec.builder(ProfileSelectionEventData.class, ProfileSelectionEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), 
                    (ProfileSelectionEventData o, String v) -> o.action = v, 
                    (ProfileSelectionEventData o) -> o.action)
                .add()
                .append(new KeyedCodec<>("ProfileId", Codec.STRING), 
                    (ProfileSelectionEventData o, String v) -> o.profileId = v, 
                    (ProfileSelectionEventData o) -> o.profileId)
                .add()
                .build();
    }

    public ProfileSelectionPage(@Nonnull PlayerRef playerRef, @Nonnull List<Profile> profiles) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, ProfileSelectionEventData.CODEC);
        this.profiles = profiles;
    }

    @Override
    public void build(
        @Nonnull Ref<EntityStore> ref,
        @Nonnull UICommandBuilder cmd,
        @Nonnull UIEventBuilder evt,
        @Nonnull Store<EntityStore> store
    ) {
        // Load the UI layout file
        cmd.append("Pages/ProfileSelectionPage.ui");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        
        // Get player to check active profile
        Player player = store.getComponent(ref, Player.getComponentType());
        UUID activeProfileId = VeilCorePlugin.getInstance().getProfileManager()
            .getActiveProfileId(player.getUuid());
        
        // Update each profile slot with data
        for (int i = 1; i <= 3; i++) {
            if (i <= profiles.size()) {
                // Profile exists - set its data
                Profile profile = profiles.get(i - 1);
                boolean isActive = profile.getProfileId().equals(activeProfileId);
                
                // Show active indicator in name
                String profileName = profile.getProfileName();
                if (isActive) {
                    profileName = "★ " + profileName + " (ACTIVE)";
                }
                cmd.set("#Profile" + i + "Name.Text", profileName);
                cmd.set("#Profile" + i + "Level.Text", "Level " + profile.getLevel());
                
                // Convert Instant to LocalDateTime for formatting
                LocalDateTime lastPlayed = LocalDateTime.ofInstant(
                    profile.getLastPlayedAt(), 
                    ZoneId.systemDefault()
                );
                cmd.set("#Profile" + i + "LastPlayed.Text", 
                    "Last: " + formatter.format(lastPlayed));
                
                // Bind events - skip SELECT button for active profile
                if (!isActive) {
                    evt.addEventBinding(
                        CustomUIEventBindingType.Activating,
                        "#Profile" + i + "SelectButton",
                        new EventData()
                            .append("Action", "Select")
                            .append("ProfileId", profile.getProfileId().toString())
                    );
                } else {
                    // Active profile - change button text but don't bind event
                    cmd.set("#Profile" + i + "SelectButton.Text", "ACTIVE");
                }
                
                // Always bind delete button (active profile deletion is prevented in handler)
                evt.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    "#Profile" + i + "DeleteButton",
                    new EventData()
                        .append("Action", "Delete")
                        .append("ProfileId", profile.getProfileId().toString())
                );
            } else {
                // Empty slot - set default text and make buttons say "Empty"
                cmd.set("#Profile" + i + "Name.Text", "Empty Slot");
                cmd.set("#Profile" + i + "Level.Text", "");
                cmd.set("#Profile" + i + "LastPlayed.Text", "");
                cmd.set("#Profile" + i + "SelectButton.Text", "EMPTY");
                cmd.set("#Profile" + i + "DeleteButton.Text", "");
            }
        }
        
        // Bind create new button
        evt.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#CreateNewButton",
            new EventData().append("Action", "CreateNew")
        );
    }

    @Override
    public void handleDataEvent(
        @Nonnull Ref<EntityStore> ref,
        @Nonnull Store<EntityStore> store,
        @Nonnull ProfileSelectionEventData data
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());

        if ("Select".equals(data.action)) {
            try {
                UUID profileId = UUID.fromString(data.profileId);
                
                // Get current active profile to save its state
                UUID currentActiveId = VeilCorePlugin.getInstance().getProfileManager()
                    .getActiveProfileId(player.getUuid());
                
                Profile profile = VeilCorePlugin.getInstance().getProfileManager()
                    .getProfile(player.getUuid(), profileId);
                
                if (profile != null) {
                    // Save current profile state if one is active
                    if (currentActiveId != null) {
                        Profile currentProfile = VeilCorePlugin.getInstance().getProfileManager()
                            .getProfile(player.getUuid(), currentActiveId);
                        if (currentProfile != null) {
                            VeilCorePlugin.getInstance().getStateManager()
                                .savePlayerStateToProfile(ref, store, player, currentProfile);
                        }
                    }
                    
                    // Set as active profile
                    VeilCorePlugin.getInstance().getProfileManager()
                        .setActiveProfile(player.getUuid(), profileId);
                    
                    // Load new profile state
                    VeilCorePlugin.getInstance().getStateManager()
                        .loadProfileStateToPlayer(ref, store, player, profile);
                    
                    playerRef.sendMessage(Message.raw("§aLoaded profile: " + profile.getProfileName()));
                    
                    // Close UI
                    player.getPageManager().setPage(ref, store, Page.None);
                } else {
                    playerRef.sendMessage(Message.raw("§cProfile not found!"));
                }
            } catch (IllegalArgumentException e) {
                playerRef.sendMessage(Message.raw("§cInvalid profile ID!"));
            }
        } else if ("Delete".equals(data.action)) {
            try {
                UUID profileId = UUID.fromString(data.profileId);
                Profile profile = VeilCorePlugin.getInstance().getProfileManager()
                    .getProfile(player.getUuid(), profileId);
                
                if (profile != null) {
                    // Check if this is the active profile
                    UUID activeProfileId = VeilCorePlugin.getInstance().getProfileManager()
                        .getActiveProfileId(player.getUuid());
                    boolean isDeletingActive = profileId.equals(activeProfileId);
                    
                    // Delete the profile (now allowed even if active)
                    boolean deleted = VeilCorePlugin.getInstance().getProfileManager()
                        .deleteProfile(player.getUuid(), profileId);
                    
                    if (deleted) {
                        playerRef.sendMessage(Message.raw("§aDeleted profile: " + profile.getProfileName()));
                        
                        // Get remaining profiles
                        List<Profile> updatedProfiles = VeilCorePlugin.getInstance().getProfileManager()
                            .getProfiles(player.getUuid());
                        
                        if (isDeletingActive) {
                            // Deleted the active profile
                            if (updatedProfiles.isEmpty()) {
                                // No profiles left - open FORCED creation page (cannot cancel)
                                ProfileCreationPage creationPage = new ProfileCreationPage(playerRef, false);
                                player.getPageManager().openCustomPage(ref, store, creationPage);
                            } else {
                                // Switch to first available profile
                                Profile newActiveProfile = updatedProfiles.get(0);
                                VeilCorePlugin.getInstance().getProfileManager()
                                    .setActiveProfile(player.getUuid(), newActiveProfile.getProfileId());
                                
                                // Load the new profile's state
                                VeilCorePlugin.getInstance().getStateManager()
                                    .loadProfileStateToPlayer(ref, store, player, newActiveProfile);
                                
                                playerRef.sendMessage(Message.raw("§eSwitched to profile: " + newActiveProfile.getProfileName()));
                                
                                // Show updated selection page
                                ProfileSelectionPage newPage = new ProfileSelectionPage(playerRef, updatedProfiles);
                                player.getPageManager().openCustomPage(ref, store, newPage);
                            }
                        } else {
                            // Deleted a non-active profile - just refresh the page
                            ProfileSelectionPage newPage = new ProfileSelectionPage(playerRef, updatedProfiles);
                            player.getPageManager().openCustomPage(ref, store, newPage);
                        }
                    } else {
                        playerRef.sendMessage(Message.raw("§cFailed to delete profile!"));
                    }
                }
            } catch (IllegalArgumentException e) {
                playerRef.sendMessage(Message.raw("§cInvalid profile ID!"));
            }
        } else if ("CreateNew".equals(data.action)) {
            // Open profile creation page - cancellable since they already have profiles
            ProfileCreationPage creationPage = new ProfileCreationPage(playerRef, true);
            player.getPageManager().openCustomPage(ref, store, creationPage);
        }
    }
}
