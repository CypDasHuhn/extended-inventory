package dev.cypdashuhn.extendedinventory.ui.inventory

import dev.cypdashuhn.extendedinventory.ui.anchor.AnchorListContext
import dev.cypdashuhn.extendedinventory.ui.anchor.AnchorListInterface
import dev.cypdashuhn.extendedinventory.ui.profile.ProfileInterface
import dev.cypdashuhn.extendedinventory.ui.profile.ProfileInterfaceContext
import dev.cypdashuhn.extendedinventory.util.mm
import dev.rooster.core.util.createItem
import dev.rooster.ui.items.InterfaceItem
import org.bukkit.Material

internal fun chromeItems(): List<InterfaceItem<IIC>> =
    listOf(
        inventoryItem()
            .atSlot(6, 1)
            .displayAs(
                createItem(
                    Material.BARRIER,
                    mm("<red>Back"),
                    listOf(mm("<gray>Close the interface."))
                )
            ).onClick { click.player.closeInventory() },
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
            .atSlot(6, 7)
            .displayAs(
                createItem(
                    Material.NAME_TAG,
                    mm("<white>Anchors"),
                    listOf(mm("<gray>View all anchors."))
                )
            ).routeTo(AnchorListInterface) { AnchorListContext(context.profileId) },
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
                val step = if (click.event.click.isShiftClick) 5 else 1
                val delta = if (click.event.click.isRightClick) -step else step
                context.position = (context.position + delta).coerceAtLeast(0)
                InventoryInterface.openInventory(click.player, context)
            },
    )
