package dev.cypdashuhn.extendedinventory.ui

import dev.rooster.core.RoosterModuleBuilder
import dev.rooster.ui.sql.SqlInterfaceContextProvider.Companion.addSqlInterfaceContextProvider
import dev.rooster.ui.ui

fun RoosterModuleBuilder.initUi() {
    services.addSqlInterfaceContextProvider()
    ui(
        listOf(
            inventory.InventoryInterface,
            anchor.AnchorListInterface,
            anchor.AnchorDetailInterface,
            profile.ProfileInterface,
            profile.ProfileDetailInterface,
            profile.PlayerInviteInterface,
        ),
    )
}
