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

/**
 * Profile creation page - allows players to create a new profile.
 */
public class ProfileCreationPage extends InteractiveCustomUIPage<ProfileCreationPage.ProfileCreationEventData> {

    private final boolean cancellable;
    private boolean profileCreated = false;

    /**
     * Event data for profile creation.
     */
    public static class ProfileCreationEventData {
        public String action;       // "Create" or "Cancel"
        public String profileName;  // Name entered by player
        
        public static final BuilderCodec<ProfileCreationEventData> CODEC = 
            BuilderCodec.builder(ProfileCreationEventData.class, ProfileCreationEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), 
                    (ProfileCreationEventData o, String v) -> o.action = v, 
                    (ProfileCreationEventData o) -> o.action)
                .add()
                .append(new KeyedCodec<>("@ProfileName", Codec.STRING), 
                    (ProfileCreationEventData o, String v) -> o.profileName = v, 
                    (ProfileCreationEventData o) -> o.profileName)
                .add()
                .build();
    }

    /**
     * Create a profile creation page.
     * 
     * @param playerRef The player reference
     * @param cancellable If true, player can cancel (used when creating additional profiles).
     *                    If false, UI cannot be cancelled via ESC or cancel button.
     */
    public ProfileCreationPage(@Nonnull PlayerRef playerRef, boolean cancellable) {
        // Use CanDismissOrCloseThroughInteraction when cancellable
        // Use CanDismiss when NOT cancellable (counterintuitive but CanDismiss = can only dismiss through interaction)
        super(playerRef, 
            cancellable ? CustomPageLifetime.CanDismissOrCloseThroughInteraction : CustomPageLifetime.CanDismiss,
            ProfileCreationEventData.CODEC);
        this.cancellable = cancellable;
    }

    @Override
    public void build(
        @Nonnull Ref<EntityStore> ref,
        @Nonnull UICommandBuilder commandBuilder,
        @Nonnull UIEventBuilder eventBuilder,
        @Nonnull Store<EntityStore> store
    ) {
        // Load the UI layout
        commandBuilder.append("Pages/ProfileCreationPage.ui");

        // Update title based on whether it's cancellable
        if (!cancellable) {
            commandBuilder.set("#TitleText.Text", "Create Your Profile (Required)");
        }

        // Bind Create button with profile name input
        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#CreateButton",
            new EventData()
                .append("Action", "Create")
                .append("@ProfileName", "#ProfileNameInput.Value")
        );

        // Bind Cancel button only if cancellable
        if (cancellable) {
            eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#CancelButton",
                new EventData().append("Action", "Cancel")
            );
        }
        // Note: When not cancellable, we don't create the cancel button at all
    }

    @Override
    public void onDismiss(
        @Nonnull Ref<EntityStore> ref,
        @Nonnull Store<EntityStore> store
    ) {
        // If not cancellable and profile wasn't created, reopen after a brief delay
        if (!cancellable && !profileCreated) {
            playerRef.sendMessage(Message.raw("You must create a profile to continue!").color("#FF5555"));
            
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player != null) {
                // Use a separate thread with delay to avoid immediate recursion
                new Thread(() -> {
                    try {
                        Thread.sleep(100); // 100ms delay
                        // Reopen the UI
                        ProfileCreationPage newPage = new ProfileCreationPage(playerRef, false);
                        player.getPageManager().openCustomPage(ref, store, newPage);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }).start();
            }
        }
    }

    @Override
    public void handleDataEvent(
        @Nonnull Ref<EntityStore> ref,
        @Nonnull Store<EntityStore> store,
        @Nonnull ProfileCreationEventData data
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());

        if ("Create".equals(data.action)) {
            String profileName = data.profileName != null ? data.profileName.trim() : "";
            
            if (profileName.isEmpty()) {
                playerRef.sendMessage(Message.raw("Profile name cannot be empty!").color("#FF5555"));
                return;
            }
            
            if (profileName.length() > 20) {
                playerRef.sendMessage(Message.raw("Profile name must be 20 characters or less!").color("#FF5555"));
                return;
            }
            
            // Create the profile
            Profile newProfile = VeilCorePlugin.getInstance().getProfileManager()
                .createProfile(player.getUuid(), profileName);
            
            if (newProfile != null) {
                // Set as active profile
                VeilCorePlugin.getInstance().getProfileManager()
                    .setActiveProfile(player.getUuid(), newProfile.getProfileId());
                
                // Mark profile as created so onDismiss doesn't reopen
                profileCreated = true;
                
                // Remove from pending profile creation
                VeilCorePlugin.getInstance().removePendingProfileCreation(player.getUuid());
                
                // Reset player to spawn (clear inventory, reset stats, teleport)
                VeilCorePlugin.getInstance().getStateManager()
                    .resetPlayerToSpawn(ref, store, player);
                
                playerRef.sendMessage(Message.raw("Profile '" + profileName + "' created successfully!").color("#55FF55"));
                
                // Close UI
                player.getPageManager().setPage(ref, store, Page.None);
            } else {
                playerRef.sendMessage(Message.raw("Failed to create profile. You may have reached the maximum (3) or the name already exists.").color("#FF5555"));
                // Close UI so player can try again
                player.getPageManager().setPage(ref, store, Page.None);
            }
        } else {
            // Cancel - only close if cancellable
            if (cancellable) {
                player.getPageManager().setPage(ref, store, Page.None);
            } else {
                playerRef.sendMessage(Message.raw("You must create a profile to continue!").color("#FF5555"));
            }
        }
    }
}
