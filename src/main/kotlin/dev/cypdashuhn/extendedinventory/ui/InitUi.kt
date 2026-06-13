package dev.cypdashuhn.extendedinventory.ui

import dev.cypdashuhn.extendedinventory.ui.anchor.AnchorDetailInterface
import dev.cypdashuhn.extendedinventory.ui.anchor.AnchorListInterface
import dev.cypdashuhn.extendedinventory.ui.inventory.InventoryInterface
import dev.cypdashuhn.extendedinventory.ui.profile.PlayerInviteInterface
import dev.cypdashuhn.extendedinventory.ui.profile.ProfileDetailInterface
import dev.cypdashuhn.extendedinventory.ui.profile.ProfileInterface
import dev.rooster.core.RoosterModuleBuilder
import dev.rooster.ui.sql.SqlInterfaceContextProvider.Companion.addSqlInterfaceContextProvider
import dev.rooster.ui.ui

fun RoosterModuleBuilder.initUi() {
    services.addSqlInterfaceContextProvider()
    ui(
        listOf(
            InventoryInterface,
            AnchorListInterface,
            AnchorDetailInterface,
            ProfileInterface,
            ProfileDetailInterface,
            PlayerInviteInterface,
        ),
    )
}
