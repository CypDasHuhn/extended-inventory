package dev.cypdashuhn.extendedinventory.ui.inventory

import dev.cypdashuhn.extendedinventory.TestDatabase
import dev.cypdashuhn.extendedinventory.db.InventoryManager
import dev.cypdashuhn.extendedinventory.db.ItemManager
import dev.rooster.core.RoosterServices
import dev.rooster.ui.RoosterUI
import dev.rooster.ui.sql.SqlInterfaceContextProvider
import org.bukkit.Material
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.entity.PlayerMock
import org.mockbukkit.mockbukkit.plugin.PluginMock
import org.mockbukkit.mockbukkit.simulate.entity.PlayerSimulation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull

class InventoryInterfaceGuiTest {

    private lateinit var server: ServerMock
    private lateinit var plugin: PluginMock
    private lateinit var player: PlayerMock

    private val profileId = 1

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.createMockPlugin("ExtendedInventory")

        TestDatabase.connect()
        TestDatabase.clear()

        val services = RoosterServices()
        services.set(SqlInterfaceContextProvider())
        RoosterUI.init(plugin, listOf(InventoryInterface), services)

        player = server.addPlayer()
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    private fun tick() = server.scheduler.performTicks(1L)

    private fun click(slot: Int, clickType: ClickType = ClickType.LEFT) =
        PlayerSimulation(player).simulateInventoryClick(player.openInventory, clickType, slot)

    private fun open() {
        InventoryInterface.openInventory(player, InventoryInterfaceContext(profileId))
        tick()
    }

    @Test
    fun `placing an item in edit mode and saving persists it`() {
        open()

        // Enter edit mode (Edit Mode button at slot 49).
        click(49)
        tick()

        // Put STONE on the cursor and click the center slot (grid 0,0 == slot 22).
        player.setItemOnCursor(ItemStack(Material.STONE))
        click(22)
        tick()

        // Save (Save button at slot 48).
        click(48)
        tick()

        val slot = InventoryManager.getSlot(profileId, 0, 0)
        assertNotNull(slot, "expected a slot to be saved at (0,0)")
        val itemId = slot!!.itemId
        assertNotNull(itemId, "expected the saved slot to reference an item")
        assertEquals(Material.STONE, ItemManager.getItem(itemId!!)?.type)
    }

    @Test
    fun `clearing an existing item and saving removes it`() {
        // Seed an existing item at (0,0).
        val id = ItemManager.store(ItemStack(Material.STONE))
        InventoryManager.setItem(profileId, 0, 0, id)

        open()

        // Enter edit mode.
        click(49)
        tick()

        // Pick up the item at the center slot (empty cursor + filled slot).
        click(22)
        tick()

        // Save.
        click(48)
        tick()

        assertNull(InventoryManager.getSlot(profileId, 0, 0))
    }
}
