package dev.cypdashuhn.extendedinventory.ui.inventory

import dev.cypdashuhn.extendedinventory.actions.InventoryActions
import dev.cypdashuhn.extendedinventory.db.ItemManager
import dev.cypdashuhn.extendedinventory.db.SlotCache
import dev.cypdashuhn.extendedinventory.hotbar.HotbarManager
import dev.cypdashuhn.extendedinventory.util.mm
import dev.rooster.core.util.createItem
import dev.rooster.ui.items.InterfaceItem
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

internal fun editSessionItems(): List<InterfaceItem<IIC>> =
    listOf(
        inventoryItem()
            .atSlot(6, 4)
            .usedWhen { context.isIdle }
            .displayAs(
                createItem(
                    Material.BOOK,
                    mm("<white>Edit Mode"),
                    listOf(mm("<gray>Click to edit inventory slots."))
                )
            ).onClick {
                context.mode = InterfaceMode.EDITING
                InventoryInterface.openInventory(click.player, context)
            },
        inventoryItem()
            .atSlot(6, 3)
            .usedWhen { context.mode == InterfaceMode.EDITING }
            .displayAs(
                createItem(
                    Material.WRITABLE_BOOK,
                    mm("<green>Save"),
                    listOf(mm("<gray>Save changes and exit edit mode."))
                )
            ).onClick {
                stagePendingEdits(click.player, context)
                savePendingEdits(click.player, context)
                context.mode = InterfaceMode.NORMAL
                InventoryInterface.openInventory(click.player, context)
            },
        inventoryItem()
            .atSlot(6, 4)
            .usedWhen { context.mode == InterfaceMode.EDITING }
            .displayAs(
                createItem(
                    Material.BARRIER,
                    mm("<red>Discard"),
                    listOf(mm("<gray>Discard changes and exit edit mode."))
                )
            ).onClick {
                context.pendingChanges.clear()
                context.mode = InterfaceMode.NORMAL
                InventoryInterface.openInventory(click.player, context)
            },
    )

internal fun pendingKey(x: Int, y: Int) = "$x:$y"

internal fun parsePendingKey(key: String): Pair<Int, Int> {
    val i = key.indexOf(':')
    return key.substring(0, i).toInt() to key.substring(i + 1).toInt()
}

internal fun encodePendingItem(item: ItemStack): String =
    ItemManager
        .encode(item.serializeAsBytes())

internal fun decodePendingItem(encoded: String): ItemStack? =
    if (encoded.isEmpty()) null else ItemStack.deserializeBytes(ItemManager.decode(encoded))

internal fun stagePendingEdits(player: Player, ctx: IIC) {
    val inventory = player.openInventory.topInventory
    for (slot in 0 until BOTTOM_BAR_START) {
        val (gridX, gridY) = ctx.contentSlotToGrid(slot)
        val key = pendingKey(gridX, gridY)
        if (SlotCache.getSlot(ctx.profileId, gridX, gridY)?.anchorId != null) continue
        val item = inventory.getItem(slot)
        ctx.pendingChanges[key] =
            if (item == null || item.type.isAir) "" else encodePendingItem(item)
    }
}

internal fun savePendingEdits(player: Player, context: IIC) {
    context.pendingChanges.forEach { (key, encoded) ->
        val (x, y) = parsePendingKey(key)
        InventoryActions.setItem(context.profileId, x, y, decodePendingItem(encoded))
    }
    context.pendingChanges.clear()
    SlotCache.invalidateProfile(context.profileId)
    HotbarManager.mirrorToHotbar(player)
}
