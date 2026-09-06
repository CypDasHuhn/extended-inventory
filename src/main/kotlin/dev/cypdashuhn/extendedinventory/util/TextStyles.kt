package dev.cypdashuhn.extendedinventory.util

object T {
    const val green = "<green>"
    const val red = "<red>"
    const val white = "<white>"
    const val gray = "<gray>"
    const val yellow = "<yellow>"
    const val aqua = "<aqua>"
    const val blue = "<blue>"
    const val darkRed = "<dark_red>"
    const val lightPurple = "<light_purple>"
    const val bold = "<bold>"
}

fun positionMsg(x: Int, y: Int) =
    "${T.green}Position: (${T.white}$x${T.green}, ${T.white}$y${T.green})"

fun whiteQuoted(s: String) = "${T.white}'$s'${T.green}"

fun errorNotFound(what: String, name: String) = "${T.red}$what '${T.white}$name${T.red}' not found."
