package dev.cypdashuhn.extendedinventory.ui.inventory

import dev.cypdashuhn.extendedinventory.ui.profile.ProfileInterface
import dev.cypdashuhn.extendedinventory.ui.profile.ProfileInterfaceContext
import dev.cypdashuhn.extendedinventory.util.mm
import dev.rooster.core.util.createItem
import dev.rooster.ui.interfaces.ClickInfo
import dev.rooster.ui.items.InterfaceItem
import org.bukkit.Material

internal fun chromeItems(): List<InterfaceItem<IIC>> =
    listOf(
        inventoryItem()
            .atSlot(6, 0)
            .displayAs(
                createItem(
                    Material.PLAYER_HEAD,
                    mm("<white>Profiles"),
                    listOf(mm("<gray>Manage profiles."))
                )
            ).onClick { ProfileInterface.openRefreshed(click.player, ProfileInterfaceContext()) },
        inventoryItem()
            .atSlot(6, 1)
            .usedWhen { context.isIdle && context.section == BarSection.DEFAULT }
            .displayAs(
                createItem(
                    Material.HOPPER,
                    mm("<white>Group Actions"),
                    listOf(mm("<gray>Select, move or delete item regions."))
                )
            ).onClick {
                context.section = BarSection.GROUPS
                InventoryInterface.openInventory(click.player, context)
            },
        inventoryItem()
            .atSlot(6, 2)
            .usedWhen { context.isIdle && context.section == BarSection.DEFAULT }
            .displayAs(
                createItem(
                    Material.LEAD,
                    mm("<white>Anchor Actions"),
                    listOf(mm("<gray>Set or materialize anchors."))
                )
            ).onClick {
                context.section = BarSection.ANCHORS
                InventoryInterface.openInventory(click.player, context)
            },
        inventoryItem()
            .atSlot(6, 8)
            .displayAs(
                createItem(
                    Material.COMPASS,
                    mm("<white>Scroll"),
                    listOf(
                        mm("<gray>Left-click: scroll down"),
                        mm("<gray>Right-click: scroll up"),
                    )
                )
            ).onClick {
                if (context.mode == InterfaceMode.EDITING) {
                    stagePendingEdits(click.player, context)
                }
                context.position += scrollDelta()
                InventoryInterface.openInventory(click.player, context)
            },
        inventoryItem()
            .atSlot(6, 7)
            .displayAs(
                createItem(
                    Material.COMPASS,
                    mm("<white>Scroll Horizontal"),
                    listOf(
                        mm("<gray>Left-click: shift view east"),
                        mm("<gray>Right-click: shift view west"),
                    )
                )
            ).onClick {
                if (context.mode == InterfaceMode.EDITING) {
                    stagePendingEdits(click.player, context)
                }
                context.centerX += scrollDelta()
                InventoryInterface.openInventory(click.player, context)
            },
    )

private fun ClickInfo<IIC>.scrollDelta(): Int {
    val step = if (click.event.click.isShiftClick) 5 else 1
    return if (click.event.click.isRightClick) -step else step
}
