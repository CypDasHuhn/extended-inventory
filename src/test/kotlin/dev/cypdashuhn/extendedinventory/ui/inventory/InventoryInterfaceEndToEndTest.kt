package dev.cypdashuhn.extendedinventory.ui.inventory

import dev.rooster.ui.context.InMemoryInterfaceContextProvider
import dev.rooster.ui.sql.SqlInterfaceContextProvider
import org.bukkit.Material
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * End-to-end state machine: open -> edit -> place -> save -> verify -> scroll.
 * Exercises both the in-memory and the production SQL context providers so that
 * serialization issues are surfaced too.
 */
abstract class InventoryInterfaceEndToEndTest : UiHarness() {
    @Test
    fun `full edit save scroll lifecycle`() {
        // ── Step 1: open ────────────────────────────────────────────────
        open()
        step("open")

        assertEquals(InterfaceMode.NORMAL, context().mode)
        assertNull(slotMaterial(22), "center slot (22) should be empty on first open")
        assertEquals(Material.BOOK, slotMaterial(49), "slot 49 should be the Edit Mode button")

        // ── Step 2: enter edit mode ─────────────────────────────────────
        step("click edit mode (49)") { click(49) }

        assertEquals(InterfaceMode.EDITING, context().mode)
        assertEquals(
            Material.WRITABLE_BOOK,
            slotMaterial(48),
            "slot 48 should become the Save button"
        )
        assertEquals(Material.BARRIER, slotMaterial(49), "slot 49 should become the Discard button")

        // ── Step 3: place STONE at the center slot (0,0) ────────────────
        // Vanilla handles content clicks in edit mode, so write the chest slot
        // directly to model the result of a click that puts STONE into (0,0).
        setChest(22, ItemStack(Material.STONE))
        step("vanilla: place stone at center (22)")

        assertEquals(Material.STONE, slotMaterial(22), "chest slot 22 should hold the placed item")

        // ── Step 4: save ─────────────────────────────────────────────────
        step("save (48)") { click(48) }

        assertEquals(InterfaceMode.NORMAL, context().mode)
        assertEquals(
            Material.STONE,
            UiHarness.dumpItem(1, 0, 0),
            "saved item should be in the DB at (0,0)"
        )
        assertEquals(
            Material.STONE,
            slotMaterial(22),
            "saved item should be visible at center slot (22)"
        )

        // ── Step 5: scroll ───────────────────────────────────────────────
        step("scroll down (53)") { click(53) }

        assertEquals(1, context().position)
        assertEquals(
            Material.STONE,
            slotMaterial(13),
            "after scrolling, the item should move up one row to slot 13"
        )
        assertNull(slotMaterial(22), "center slot (22) should now show grid (0,1), which is empty")
    }

    @Test
    fun `scroller moves the grid row by row`() {
        open()
        step("open")

        // Seed STONE at (0,0) and DIRT one row below at (0,1).
        InventoryManagerSeed.seed(1, 0, 0, Material.STONE)
        InventoryManagerSeed.seed(1, 0, 1, Material.DIRT)

        open()
        step("reopen with seeded data")

        assertEquals(Material.STONE, slotMaterial(22), "center slot should show STONE at (0,0)")
        assertEquals(Material.DIRT, slotMaterial(31), "slot 31 (row 3) should show DIRT at (0,1)")

        step("scroll down (53)") { click(53) }
        assertEquals(1, context().position)
        assertEquals(
            Material.STONE,
            slotMaterial(13),
            "after scroll, STONE should move up one row to slot 13"
        )
        assertEquals(
            Material.DIRT,
            slotMaterial(22),
            "after scroll, DIRT should move up one row to slot 22"
        )
    }

    @Test
    fun `group delete removes a region`() {
        InventoryManagerSeed.seed(1, 0, 0, Material.STONE)
        InventoryManagerSeed.seed(1, 1, 0, Material.STONE)

        open()
        step("open")

        step("click group delete (47)") { click(47) }
        assertEquals(InterfaceMode.GROUP_DELETE_A, context().mode)

        step("pick corner A at (0,0) -> slot 22") { click(22) }
        assertEquals(InterfaceMode.GROUP_DELETE_B, context().mode)

        step("pick corner B at (1,0) -> slot 23") { click(23) }
        assertEquals(InterfaceMode.NORMAL, context().mode)
        assertTrue(context().cornerA == (0 to 0) && context().cornerB == (1 to 0))

        step("confirm delete (51)") { click(51) }
        assertTrue(context().groupDeleteConfirmed)

        step("final delete (51)") { click(51) }

        assertNull(UiHarness.dumpItem(1, 0, 0), "item at (0,0) should be deleted")
        assertNull(UiHarness.dumpItem(1, 1, 0), "item at (1,0) should be deleted")
    }

    @Test
    fun `group move translates a region`() {
        InventoryManagerSeed.seed(1, 0, 0, Material.STONE)
        InventoryManagerSeed.seed(1, 0, 1, Material.DIRT)

        open()
        step("open")

        step("click group move (48)") { click(48) }
        assertEquals(InterfaceMode.GROUP_MOVE_A, context().mode)

        step("pick corner A at (0,0) -> slot 22") { click(22) }
        assertEquals(InterfaceMode.GROUP_MOVE_B, context().mode)

        step("pick corner B at (0,1) -> slot 31") { click(31) }
        assertEquals(InterfaceMode.GROUP_MOVE_TARGET, context().mode)

        step("pick target at (2,0) -> slot 24") { click(24) }
        assertEquals(InterfaceMode.NORMAL, context().mode)

        step("confirm move (51)") { click(51) }
        assertTrue(context().groupMoveConfirmed)

        step("final move (51)") { click(51) }

        assertNull(UiHarness.dumpItem(1, 0, 0), "source (0,0) should be empty after move")
        assertNull(UiHarness.dumpItem(1, 0, 1), "source (0,1) should be empty after move")
        assertEquals(Material.STONE, UiHarness.dumpItem(1, 2, 0), "STONE should move to (2,0)")
        assertEquals(Material.DIRT, UiHarness.dumpItem(1, 2, 1), "DIRT should move to (2,1)")
    }

    @Test
    fun `normal mode copies an item to the cursor without removing it`() {
        InventoryManagerSeed.seed(1, 0, 0, Material.STONE)

        open()
        step("open")

        step("click center (22) in normal mode") { click(22) }

        assertEquals(
            Material.STONE,
            cursor()?.type,
            "normal-mode click should copy the item to the cursor"
        )
        assertEquals(
            Material.STONE,
            UiHarness.dumpItem(1, 0, 0),
            "source item should remain stored"
        )
    }

    @Test
    fun `player inventory clicks are not intercepted by the interface`() {
        open()
        step("open")
        step("enter edit mode (49)") { click(49) }

        val event = stepEvent("click own inventory slot") { clickBottom(0) }

        assertFalse(event.isCancelled, "player-inventory clicks must pass through to vanilla")
        assertEquals(
            InterfaceMode.EDITING,
            context().mode,
            "interface must not react to a player-inventory click"
        )
        assertTrue(
            context().pendingChanges.isEmpty(),
            "no pending change should be recorded for a player-inventory click"
        )
    }

    @Test
    fun `edit mode content clicks are not cancelled`() {
        open()
        step("open")
        step("enter edit mode (49)") { click(49) }

        val event = stepEvent("click content slot (22) in edit mode") { click(22) }

        assertFalse(event.isCancelled, "content clicks in edit mode must pass through to vanilla")
        assertEquals(InterfaceMode.EDITING, context().mode)
        assertTrue(
            context().pendingChanges.isEmpty(),
            "no pending change should be recorded before save"
        )
    }

    @Test
    fun `edit mode scroll stages unsaved edits`() {
        open()
        step("open")
        step("enter edit mode (49)") { click(49) }

        // Vanilla place STONE at (0,0) == slot 22.
        setChest(22, ItemStack(Material.STONE))
        step("vanilla: place stone at center (22)")

        // Scroll down then back up; the unsaved edit must survive both re-renders.
        step("scroll down (53)") { click(53) }
        assertEquals(1, context().position)

        step("scroll up (53 right-click)") { click(53, ClickType.RIGHT) }
        assertEquals(0, context().position)

        step("save (48)") { click(48) }

        assertEquals(
            Material.STONE,
            UiHarness.dumpItem(1, 0, 0),
            "edit should survive scrolling and be saved"
        )
    }

    @Test
    fun `grab item from own inventory and place it into the interface`() {
        player.inventory.setItem(0, ItemStack(Material.STONE))

        open()
        step("open")
        step("enter edit mode (49)") { click(49) }

        // Pick up STONE from the player's own hotbar: vanilla moves it onto the cursor.
        val event = stepEvent("click own hotbar slot 0") { clickBottom(0) }
        assertFalse(event.isCancelled, "the pick-up click must not be cancelled")
        setCursor(ItemStack(Material.STONE))
        player.inventory.setItem(0, null)

        // Vanilla place the cursor item into the interface's center slot.
        setChest(22, ItemStack(Material.STONE))
        setCursor(null)
        step("vanilla: place stone into interface (22)")

        step("save (48)") { click(48) }

        assertEquals(
            Material.STONE,
            UiHarness.dumpItem(1, 0, 0),
            "item should be persisted after save"
        )
    }
}

object InventoryManagerSeed {
    fun seed(profileId: Int, x: Int, y: Int, material: Material) {
        dev.cypdashuhn.extendedinventory.db.InventoryManager.setItem(
            profileId,
            x,
            y,
            dev.cypdashuhn.extendedinventory.db.ItemManager
                .store(ItemStack(material)),
        )
        dev.cypdashuhn.extendedinventory.db.SlotCache
            .invalidateProfile(profileId)
    }
}

class InventoryInterfaceEndToEndSqlTest : InventoryInterfaceEndToEndTest() {
    override fun provider() = SqlInterfaceContextProvider()

    override fun traceFileName() = "e2e-sql"
}

class InventoryInterfaceEndToEndInMemoryTest : InventoryInterfaceEndToEndTest() {
    override fun provider() = InMemoryInterfaceContextProvider()

    override fun traceFileName() = "e2e-inmemory"
}
