package dev.cypdashuhn.extendedinventory.actions

private val VALID_NAME = Regex("[a-zA-Z0-9_\\-]+")

fun isValidResourceName(name: String) = name.matches(VALID_NAME)
