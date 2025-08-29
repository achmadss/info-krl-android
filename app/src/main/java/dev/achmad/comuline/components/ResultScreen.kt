package dev.achmad.comuline.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.screen.Screen

abstract class ResultScreen: Screen {
    var arguments: HashMap<String, Any?> = HashMap()

    @Composable
    final override fun Content() {
        val currentArguments = remember(arguments) {
            HashMap(arguments).also {
                arguments.clear() // Clear after capturing
            }
        }
        Content(currentArguments)
    }

    @Composable
    abstract fun Content(arguments: Map<String, Any?>)
}