package dev.achmad.comuline.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import drawHorizontalScrollbar
import drawVerticalScrollbar

@Composable
fun ScrollbarLazyVerticalGrid(
    columns: GridCells,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    userScrollEnabled: Boolean = true,
    positionOffset: Float? = null,
    content: LazyGridScope.() -> Unit,
) {
    val direction = LocalLayoutDirection.current
    val density = LocalDensity.current
    LazyVerticalGrid(
        columns = columns,
        modifier = modifier
            .drawVerticalScrollbar(
                state = state,
                reverseScrolling = reverseLayout,
                positionOffsetPx = positionOffset ?: remember(contentPadding) {
                    with(density) { contentPadding.calculateEndPadding(direction).toPx() }
                },
            ),
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        verticalArrangement = verticalArrangement,
        horizontalArrangement = horizontalArrangement,
        userScrollEnabled = userScrollEnabled,
        content = content,
    )
}

@Composable
fun ScrollbarLazyHorizontalGrid(
    rows: GridCells,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    userScrollEnabled: Boolean = true,
    content: LazyGridScope.() -> Unit,
) {
    val direction = LocalLayoutDirection.current
    val density = LocalDensity.current
    val positionOffset = remember(contentPadding) {
        with(density) { contentPadding.calculateEndPadding(direction).toPx() }
    }
    LazyHorizontalGrid(
        rows = rows,
        modifier = modifier
            .drawHorizontalScrollbar(
                state = state,
                reverseScrolling = reverseLayout,
                positionOffsetPx = positionOffset
            ),
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        verticalArrangement = verticalArrangement,
        horizontalArrangement = horizontalArrangement,
        userScrollEnabled = userScrollEnabled,
        content = content,
    )
}