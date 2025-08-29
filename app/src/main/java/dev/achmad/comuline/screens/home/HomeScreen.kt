package dev.achmad.comuline.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.core.screen.Screen
import dev.achmad.comuline.R
import dev.achmad.comuline.theme.AppTheme

object HomeScreen: Screen {
    private fun readResolve(): Any = HomeScreen

    @Composable
    override fun Content() {

    }

}

@Composable
private fun HomeScreen(

) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {

        }
    ) { contentPadding ->

    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    AppTheme {
        HomeScreen()
    }
}