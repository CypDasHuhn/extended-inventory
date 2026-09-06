package dev.cypdashuhn.extendedinventory.hotbar

import dev.cypdashuhn.extendedinventory.db.AnchorManager
import dev.cypdashuhn.extendedinventory.db.BufferManager
import dev.cypdashuhn.extendedinventory.db.InventoryManager
import dev.cypdashuhn.extendedinventory.db.ItemManager
import dev.cypdashuhn.extendedinventory.db.PlayerProfileManager
import dev.cypdashuhn.extendedinventory.db.PlayerProfileStatus
import dev.cypdashuhn.extendedinventory.db.ProfileManager
import dev.cypdashuhn.extendedinventory.db.SlotCache
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

enum class HotbarMode { FREE, LOCKED }

data class PlayerState(
    var profileId: Int? = null,
    var x: Int = 0,
    var y: Int = 0,
    var anchored: Boolean = false,
    var mode: HotbarMode = HotbarMode.FREE,
)

object HotbarManager {
    private val states: MutableMap<String, PlayerState> = mutableMapOf()

    const val CENTER_SLOT = 4

    private val ANCHOR_KEY = NamespacedKey("extendedinventory", "anchor")
    private val ANCHOR_X_KEY = NamespacedKey("extendedinventory", "anchor_x")
    private val ANCHOR_Y_KEY = NamespacedKey("extendedinventory", "anchor_y")
    private val ANCHOR_NAME_KEY = NamespacedKey("extendedinventory", "anchor_name")

    fun getState(player: Player): PlayerState = states.getOrPut(player.uniqueId.toString()) { PlayerState() }

    fun ensureProfile(player: Player): Int {
        val state = getState(player)
        if (state.profileId != null) return state.profileId!!

        val primaryId = PlayerProfileManager.getPrimary(player)
        if (primaryId != null) {
            state.profileId = primaryId
            return primaryId
        }

        val profileId = ProfileManager.create("default", player)
        PlayerProfileManager.assign(player, profileId, PlayerProfileStatus.PRIMARY)
        state.profileId = profileId
        return profileId
    }

    fun mirrorToHotbar(player: Player) {
        val state = getState(player)
        val profileId = state.profileId ?: return
        val inv = player.inventory

        val row = SlotCache.getRow(profileId, state.y)
        for (i in 0..8) {
            val offsetX = i - CENTER_SLOT
            val dx = state.x + offsetX
            val slot = row[dx]
            inv.setItem(i, resolveSlotItem(slot))
        }
    }

    fun resolveSlotItem(slot: InventoryManager.SlotData?): ItemStack? {
        if (slot == null) return null
        if (slot.anchorId != null) {
            val anchor = AnchorManager.findById(slot.anchorId) ?: return null
            return createAnchorItem(anchor.name, anchor.x, anchor.y)
        }
        if (slot.itemId != null) {
            return ItemManager.getItem(slot.itemId)
        }
        return null
    }

    fun createAnchorItem(name: String, x: Int, y: Int): ItemStack {
        val item = ItemStack(Material.ENDER_PEARL)
        val meta = item.itemMeta
        meta.displayName(
            Component
                .text("Anchor: ", NamedTextColor.AQUA)
                .append(Component.text(name, NamedTextColor.WHITE))
                .append(Component.text(" (", NamedTextColor.GRAY))
                .append(Component.text("$x, $y", NamedTextColor.WHITE))
                .append(Component.text(")", NamedTextColor.GRAY))
        )
        meta.lore(listOf(
            Component.text("Right-click to jump to ($x, $y)", NamedTextColor.GRAY),
            Component.text("Anchor: $name", NamedTextColor.GRAY),
        ))
        val pdc = meta.persistentDataContainer
        pdc.set(ANCHOR_KEY, PersistentDataType.BOOLEAN, true)
        pdc.set(ANCHOR_X_KEY, PersistentDataType.INTEGER, x)
        pdc.set(ANCHOR_Y_KEY, PersistentDataType.INTEGER, y)
        pdc.set(ANCHOR_NAME_KEY, PersistentDataType.STRING, name)
        item.itemMeta = meta
        return item
    }

    fun navigate(player: Player, dx: Int, dy: Int) {
        val state = getState(player)
        if (state.anchored) return

        val profileId = state.profileId ?: return

        val currentItems = (0..8).map { player.inventory.getItem(it)?.clone() }
        BufferManager.push(player, currentItems, state.x to state.y)

        state.x += dx
        state.y += dy
        mirrorToHotbar(player)
    }

    fun jumpTo(player: Player, x: Int, y: Int) {
        val state = getState(player)

        val currentItems = (0..8).map { player.inventory.getItem(it)?.clone() }
        BufferManager.push(player, currentItems, state.x to state.y)

        state.x = x
        state.y = y
        mirrorToHotbar(player)
    }

    fun toggleAnchor(player: Player): Boolean {
        val state = getState(player)
        state.anchored = !state.anchored
        return state.anchored
    }

    fun setMode(player: Player, mode: HotbarMode) {
        getState(player).mode = mode
    }

    fun loadBuffer(player: Player, name: String? = null): Boolean {
        val entry = if (name != null) {
            BufferManager.loadByName(player, name)
        } else {
            BufferManager.pop(player)
        } ?: return false

        val state = getState(player)
        state.x = entry.position.first
        state.y = entry.position.second

        for (i in 0..8) {
            player.inventory.setItem(i, entry.items.getOrNull(i))
        }
        return true
    }

    fun isAnchorItem(item: ItemStack?): Boolean {
        if (item == null) return false
        val pdc = item.itemMeta?.persistentDataContainer ?: return false
        return pdc.get(ANCHOR_KEY, PersistentDataType.BOOLEAN) == true
    }

    fun resolveAnchorJump(item: ItemStack?): Pair<Int, Int>? {
        if (!isAnchorItem(item)) return null
        val pdc = item?.itemMeta?.persistentDataContainer ?: return null
        val x = pdc.get(ANCHOR_X_KEY, PersistentDataType.INTEGER) ?: return null
        val y = pdc.get(ANCHOR_Y_KEY, PersistentDataType.INTEGER) ?: return null
        return x to y
    }

    fun getAnchorName(item: ItemStack?): String? {
        if (!isAnchorItem(item)) return null
        return item?.itemMeta?.persistentDataContainer?.get(ANCHOR_NAME_KEY, PersistentDataType.STRING)
    }
}
