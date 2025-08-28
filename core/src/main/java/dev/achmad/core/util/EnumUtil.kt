package dev.achmad.core.util

inline fun <reified T> enumValueOfOrDefault(
    value: String?,
    default: T
): T where T : Enum<T> {
    if (value == null) return default
    return enumValues<T>()
        .find { it.name.equals(value, ignoreCase = true) }
        ?: default
}
