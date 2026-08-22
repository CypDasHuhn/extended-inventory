package dev.cypdashuhn.extendedinventory.actions

val RESOURCE_NAME_REGEX = Regex("[a-zA-Z0-9_\\-]+")

fun isValidResourceName(name: String) = name.matches(RESOURCE_NAME_REGEX)
