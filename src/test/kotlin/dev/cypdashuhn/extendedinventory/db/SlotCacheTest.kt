package dev.cypdashuhn.extendedinventory.db

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class SlotCacheTest {

    @Test
    fun `getRow returns empty map for unpopulated row`() {
        val row = SlotCache.getRow(Int.MAX_VALUE, 0)
        assertTrue(row.isEmpty())
    }

    @Test
    fun `getSlot returns null for non-existent slot`() {
        val slot = SlotCache.getSlot(Int.MAX_VALUE, 0, 0)
        assertNull(slot)
    }

    @Test
    fun `invalidateProfile clears all cached rows`() {
        val profileId = Int.MAX_VALUE - 1
        SlotCache.setItem(profileId, 0, 0, null)
        SlotCache.getRow(profileId, 0)

        val preInvalidate = SlotCache.getRow(profileId, 0)
        assertNotNull(preInvalidate)

        SlotCache.invalidateProfile(profileId)
    }

    @Test
    fun `invalidateRow clears specific row`() {
        val profileId = Int.MAX_VALUE - 2
        SlotCache.setItem(profileId, 0, 0, null)
        SlotCache.setItem(profileId, 0, 1, null)
        SlotCache.getRow(profileId, 0)
        SlotCache.getRow(profileId, 1)

        SlotCache.invalidateRow(profileId, 1)

        val row0 = SlotCache.getRow(profileId, 0)
        assertNotNull(row0)

        SlotCache.invalidateProfile(profileId)
    }
}
