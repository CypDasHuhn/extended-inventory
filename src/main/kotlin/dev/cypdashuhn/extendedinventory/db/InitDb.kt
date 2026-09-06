package dev.cypdashuhn.extendedinventory.db

import dev.rooster.core.RoosterModuleBuilder
import dev.rooster.db.db

fun RoosterModuleBuilder.initDb() {
    db(listOf(
        ProfileManager.Profiles,
        InventoryManager.InventorySlots,
        ItemManager.Items,
        AnchorManager.Anchors,
        PlayerProfileManager.PlayerProfiles,
        PlayerStateStore.PlayerStates,
    ))
}
