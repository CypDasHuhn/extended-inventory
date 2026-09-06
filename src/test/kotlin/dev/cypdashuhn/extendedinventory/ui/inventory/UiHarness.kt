package dev.cypdashuhn.extendedinventory.ui.inventory

import dev.cypdashuhn.extendedinventory.TestDatabase
import dev.cypdashuhn.extendedinventory.db.InventoryManager
import dev.cypdashuhn.extendedinventory.db.ItemManager
import dev.rooster.core.RoosterServices
import dev.rooster.ui.RoosterUI
import dev.rooster.ui.context.InterfaceContextProvider
import org.bukkit.Material
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.entity.PlayerMock
import org.mockbukkit.mockbukkit.plugin.PluginMock
import org.mockbukkit.mockbukkit.simulate.entity.PlayerSimulation
import java.io.File

/**
 * State-machine driver for the inventory UI. Each [step] applies an input,
 * advances a tick, and snapshots the resulting screen (top inventory + context)
 * into a text file so the full input/output trace can be analyzed.
 *
 * Concrete subclasses supply the [InterfaceContextProvider] under test.
 */
abstract class UiHarness {
    abstract fun provider(): InterfaceContextProvider

    protected abstract fun traceFileName(): String

    lateinit var server: ServerMock
    lateinit var plugin: PluginMock
    lateinit var player: PlayerMock

    private val traceFile: File get() = File("build/ui-screens/${traceFileName()}.txt")
    private var stepIndex = 0

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.createMockPlugin("ExtendedInventory")

        TestDatabase.connect()
        TestDatabase.clear()

        val services = RoosterServices()
        services.set(provider())
        RoosterUI.init(plugin, listOf(InventoryInterface), services)

        player = server.addPlayer()

        traceFile.parentFile.mkdirs()
        traceFile.writeText("")
        stepIndex = 0
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    fun tick() = server.scheduler.performTicks(1L)

    fun click(slot: Int, clickType: ClickType = ClickType.LEFT): InventoryClickEvent =
        PlayerSimulation(player).simulateInventoryClick(player.openInventory, clickType, slot)

    /** Clicks a slot in the player's own inventory (the bottom part of the view). */
    fun clickBottom(slot: Int, clickType: ClickType = ClickType.LEFT): InventoryClickEvent {
        val topSize = player.openInventory.topInventory.size
        return PlayerSimulation(player).simulateInventoryClick(
            player.openInventory,
            clickType,
            topSize + slot
        )
    }

    fun setCursor(item: ItemStack?) = player.setItemOnCursor(item ?: ItemStack.empty())

    /** Directly writes a slot in the top chest, simulating what vanilla does on an uncancelled click. */
    fun setChest(slot: Int, item: ItemStack?) =
        player.openInventory.topInventory.setItem(slot, item)

    fun cursor(): ItemStack? = player.itemOnCursor

    fun topItem(slot: Int): ItemStack? = player.openInventory.topInventory.getItem(slot)

    /** Material at [slot], or null when the slot is empty (null/AIR). */
    fun slotMaterial(slot: Int): Material? = topItem(slot)?.type?.takeUnless { it.isAir }

    fun context(): InventoryInterfaceContext = InventoryInterface.getContext(player)

    fun open(centerX: Int = 0, centerY: Int = 0) {
        InventoryInterface
            .openInventory(
                player,
                InventoryInterfaceContext(profileId = 1, centerX = centerX, centerY = centerY)
            )
        tick()
    }

    fun step(label: String, action: () -> Unit = {}) {
        action()
        tick()
        snapshot(label)
    }

    /** Like [step], but returns the value produced by [action] (e.g. the fired event). */
    fun <T> stepEvent(label: String, action: () -> T): T {
        val value = action()
        tick()
        snapshot(label)
        return value
    }

    private fun snapshot(label: String) {
        stepIndex++
        val sb = StringBuilder()
        sb.appendLine()
        sb.appendLine("══════════════════════════════════════════════════════════")
        sb.appendLine("STEP $stepIndex: $label")
        sb.appendLine("══════════════════════════════════════════════════════════")

        val ctx = context()
        sb
            .appendLine(
                "context: profileId=${ctx.profileId} center=(${ctx.centerX}, ${ctx.centerY}) mode=${ctx.mode} position=${ctx.position}"
            )
        sb.appendLine("pendingChanges=${ctx.pendingChanges}")
        sb.appendLine("cursor=${cursor()?.type ?: "-"}")

        sb.appendLine("top inventory (slot: material):")
        val inv = player.openInventory.topInventory
        for (row in 0 until 6) {
            val line =
                (0 until 9).joinToString(" ") { col ->
                    val slot = row * 9 + col
                    val item = inv.getItem(slot)
                    val name = if (item == null || item.type.isAir) "-" else item.type.name
                    "%02d:%s".format(slot, name.padEnd(20))
                }
            sb.appendLine("  $line")
        }
        traceFile.appendText(sb.toString())
    }

    companion object {
        fun dumpItem(profileId: Int, x: Int, y: Int): Material? {
            val slot = InventoryManager.getSlot(profileId, x, y) ?: return null
            val itemId = slot.itemId ?: return null
            return ItemManager.getItem(itemId)?.type
        }
    }
}
