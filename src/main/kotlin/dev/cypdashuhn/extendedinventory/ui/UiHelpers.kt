package dev.cypdashuhn.extendedinventory.ui

import dev.cypdashuhn.extendedinventory.util.mm
import dev.rooster.ui.interfaces.Context
import dev.rooster.ui.interfaces.constructors.confirmation.CancelInfo
import org.bukkit.entity.Player

internal fun <T : Context> CancelInfo<T>.player(): Player? =
    cancelEvent.clickInfo?.second?.player
        ?: cancelEvent.closeInfo?.player as? Player
