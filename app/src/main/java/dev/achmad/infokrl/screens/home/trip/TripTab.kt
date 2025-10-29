package dev.achmad.infokrl.screens.home.trip

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import dev.achmad.infokrl.components.AppBar

object TripTab : Tab {
    private fun readResolve(): Any = TripTab

    override val options: TabOptions
        @Composable
        get() {
            val isSelected = LocalTabNavigator.current.current.key == key
            return TabOptions(
                index = 2u,
                title = "Trip", // TODO string resource
                icon = rememberVectorPainter(
                    when {
                        isSelected -> Icons.Default.Map
                        else -> Icons.Outlined.Map
                    }
                )
            )
        }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {

    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TripTab(
    loading: Boolean,
    onClickSubmit: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 4.dp
            ) {
                AppBar(
                    title = "Trip Planner" // TODO string resource
                )
            }
        }
    ) { contentPadding ->
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            item {

            }
        }
    }
}