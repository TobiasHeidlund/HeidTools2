package me.tubs.heidtools4.workApp.view.addCheckin.Components
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.BottomAppBarScrollBehavior
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val BottomAppBarHorizontalPadding = 16.dp - 12.dp
internal val BottomAppBarVerticalPadding = 16.dp - 12.dp

// Padding minus content padding
private val FABHorizontalPadding = 16.dp - BottomAppBarHorizontalPadding
private val FABVerticalPadding = 12.dp - BottomAppBarVerticalPadding

    @ExperimentalMaterial3Api
    @Composable
    fun MyBottomAppBar(
        actions: @Composable RowScope.() -> Unit,
        modifier: Modifier = Modifier,
        floatingActionButton: @Composable (() -> Unit)? = null,
        containerColor: Color = BottomAppBarDefaults.containerColor,
        contentColor: Color = contentColorFor(containerColor),
        tonalElevation: Dp = BottomAppBarDefaults.ContainerElevation,
        contentPadding: PaddingValues = BottomAppBarDefaults.ContentPadding,
        windowInsets: WindowInsets = BottomAppBarDefaults.windowInsets,
        scrollBehavior: BottomAppBarScrollBehavior? = null,
    ) = androidx.compose.material3.BottomAppBar(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        windowInsets = windowInsets,
        contentPadding = contentPadding,
        scrollBehavior = scrollBehavior
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Absolute.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = actions,
        )
        if (floatingActionButton != null) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .padding(
                        top = FABVerticalPadding,
                        end = FABHorizontalPadding
                    ),
                contentAlignment = Alignment.TopStart
            ) {
                floatingActionButton()
            }
        }
    }
