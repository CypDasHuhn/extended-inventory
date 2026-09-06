package dev.cypdashuhn.extendedinventory.actions

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ValidationTest {
    @Test
    fun `valid names pass`() {
        assertTrue(isValidResourceName("test"))
        assertTrue(isValidResourceName("my-profile"))
        assertTrue(isValidResourceName("profile_1"))
        assertTrue(isValidResourceName("Test-Profile"))
        assertTrue(isValidResourceName("a"))
        assertTrue(isValidResourceName("abc123"))
    }

    @Test
    fun `invalid names fail`() {
        assertFalse(isValidResourceName(""))
        assertFalse(isValidResourceName("has space"))
        assertFalse(isValidResourceName("special!"))
        assertFalse(isValidResourceName("with.dots"))
        assertFalse(isValidResourceName("name@test"))
    }
}
