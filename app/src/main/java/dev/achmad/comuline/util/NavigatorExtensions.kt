package dev.achmad.comuline.util

import cafe.adriel.voyager.navigator.Navigator
import dev.achmad.comuline.components.ResultScreen

fun Navigator.popWithResult(data: Map<String, Any?>) {
    val prev = if (items.size < 2) null else items[items.size - 2] as? ResultScreen
    prev?.arguments = HashMap(data)
    pop()
}

fun Navigator.popWithResult(vararg pairs: Pair<String, Any?>) {
    popWithResult(mapOf(*pairs))
}