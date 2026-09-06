package dev.cypdashuhn.extendedinventory.db

import dev.cypdashuhn.extendedinventory.TestDatabase
import dev.cypdashuhn.extendedinventory.hotbar.HotbarMode
import dev.cypdashuhn.extendedinventory.hotbar.PlayerState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PlayerStateStoreTest {
    @BeforeEach
    fun setUp() {
        TestDatabase.clear()
    }

    @Test
    fun `save then load round trips state`() {
        PlayerStateStore.save("uuid-1", PlayerState(profileId = 3, x = 12, y = -7, anchored = true, mode = HotbarMode.LOCKED))

        val loaded = PlayerStateStore.load("uuid-1")

        assertEquals(3, loaded?.profileId)
        assertEquals(12, loaded?.x)
        assertEquals(-7, loaded?.y)
        assertEquals(true, loaded?.anchored)
        assertEquals(HotbarMode.LOCKED, loaded?.mode)
    }

    @Test
    fun `save overwrites existing state`() {
        PlayerStateStore.save("uuid-1", PlayerState(profileId = 1, x = 0, y = 0))
        PlayerStateStore.save("uuid-1", PlayerState(profileId = 2, x = 5, y = 9, anchored = true, mode = HotbarMode.LOCKED))

        val loaded = PlayerStateStore.load("uuid-1")

        assertEquals(2, loaded?.profileId)
        assertEquals(5, loaded?.x)
        assertEquals(9, loaded?.y)
        assertEquals(true, loaded?.anchored)
    }

    @Test
    fun `load with no stored state returns null`() {
        assertNull(PlayerStateStore.load("unknown-uuid"))
    }
}
