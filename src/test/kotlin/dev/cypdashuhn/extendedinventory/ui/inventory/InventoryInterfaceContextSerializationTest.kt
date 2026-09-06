package dev.cypdashuhn.extendedinventory.ui.inventory

import com.google.gson.Gson
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * The SqlInterfaceContextProvider persists the interface context as Gson JSON on
 * every click. Pending edits therefore must be representable as plain JSON (no
 * ItemStack values, no Pair map keys), otherwise they are corrupted on reload.
 */
class InventoryInterfaceContextSerializationTest {
    private val gson = Gson()

    private fun roundTrip(context: InventoryInterfaceContext): InventoryInterfaceContext {
        val json = gson.toJson(context)
        return gson.fromJson(json, InventoryInterfaceContext::class.java)
    }

    @Test
    fun `pending changes survive gson round trip`() {
        val ctx = InventoryInterfaceContext(profileId = 1)
        ctx.pendingChanges["2:3"] = "base64-of-stone"
        ctx.pendingChanges["-4:5"] = "" // cleared slot
        ctx.pendingChanges["0:-7"] = "base64-of-dirt"

        val back = roundTrip(ctx)

        assertEquals("base64-of-stone", back.pendingChanges["2:3"])
        assertEquals("", back.pendingChanges["-4:5"])
        assertEquals("base64-of-dirt", back.pendingChanges["0:-7"])
        assertEquals(3, back.pendingChanges.size)
    }

    @Test
    fun `scalar context fields survive gson round trip`() {
        val ctx = InventoryInterfaceContext(profileId = 42, centerX = -3, centerY = 7)
        ctx.mode = InterfaceMode.EDITING
        ctx.cornerA = 1 to 2
        ctx.cornerB = 3 to 4
        ctx.targetCorner = 5 to 6
        ctx.targetPreviewPositions = setOf(7 to 8)
        ctx.groupDeleteConfirmed = true

        val back = roundTrip(ctx)

        assertEquals(42, back.profileId)
        assertEquals(-3, back.centerX)
        assertEquals(7, back.centerY)
        assertEquals(InterfaceMode.EDITING, back.mode)
        assertEquals(1 to 2, back.cornerA)
        assertEquals(3 to 4, back.cornerB)
        assertEquals(5 to 6, back.targetCorner)
        assertEquals(setOf(7 to 8), back.targetPreviewPositions)
        assertTrue(back.groupDeleteConfirmed)
    }

    @Test
    fun `scroll position survives gson round trip`() {
        val ctx = InventoryInterfaceContext(profileId = 1)
        ctx.position = 4

        val back = roundTrip(ctx)

        assertEquals(4, back.position)
    }
}
