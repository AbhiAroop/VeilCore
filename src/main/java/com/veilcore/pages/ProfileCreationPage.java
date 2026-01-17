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

    public ProfileCreationPage(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, ProfileCreationEventData.CODEC);
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

        // Bind Create button with profile name input
        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#CreateButton",
            new EventData()
                .append("Action", "Create")
                .append("@ProfileName", "#ProfileNameInput.Value")
        );

        // Bind Cancel button
        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#CancelButton",
            new EventData().append("Action", "Cancel")
        );
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
                playerRef.sendMessage(Message.raw("§cProfile name cannot be empty!"));
                return;
            }
            
            if (profileName.length() > 20) {
                playerRef.sendMessage(Message.raw("§cProfile name must be 20 characters or less!"));
                return;
            }
            
            // Create the profile
            Profile newProfile = VeilCorePlugin.getInstance().getProfileManager()
                .createProfile(player.getUuid(), profileName);
            
            if (newProfile != null) {
                // Set as active profile
                VeilCorePlugin.getInstance().getProfileManager()
                    .setActiveProfile(player.getUuid(), newProfile.getProfileId());
                
                // Reset player to spawn (clear inventory, reset stats, teleport)
                VeilCorePlugin.getInstance().getStateManager()
                    .resetPlayerToSpawn(ref, store, player);
                
                playerRef.sendMessage(Message.raw("§aProfile '" + profileName + "' created successfully!"));
                
                // Close UI
                player.getPageManager().setPage(ref, store, Page.None);
            } else {
                playerRef.sendMessage(Message.raw("§cFailed to create profile. You may have reached the maximum (3) or the name already exists."));
            }
        } else {
            // Cancel - just close the UI
            player.getPageManager().setPage(ref, store, Page.None);
        }
    }
}
