package com.modloader.bm3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.modloader.bm3.ui.components.BottomNavBarItem
import com.modloader.bm3.ui.components.ImageItem
import com.modloader.bm3.ui.components.ResponsiveCardsSection
import com.modloader.bm3.ui.features.ModsScreen
import com.modloader.bm3.ui.theme.BM3Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BM3Theme {
                BM3App()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BM3App() {
    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        // rememberSaveable because it would be weird for navbar and drawer to change on rotation
        var selectedDrawerIndex by rememberSaveable { mutableIntStateOf(0) }
        var selectedNavBarItemIndex by rememberSaveable { mutableIntStateOf(0) }
        val navBarItems = listOf(
            BottomNavBarItem(
                title = "Mods",
                description = "Mods",
                onSelectedIcon = painterResource(R.drawable.ic_grid_view_filled),
                unselectedIcon = painterResource(R.drawable.ic_grid_view),
                hasBadge = true

            ),
            BottomNavBarItem(
                title = "Modpacks",
                description = "Modpacks",
                onSelectedIcon = painterResource(R.drawable.ic_stacks_filled),
                unselectedIcon = painterResource(R.drawable.ic_stacks),
                hasBadge = false
            ),
            BottomNavBarItem(
                title = "Settings",
                description = "Settings",
                onSelectedIcon = painterResource(R.drawable.ic_settings_filled),
                unselectedIcon = painterResource(R.drawable.ic_settings),
                hasBadge = false
            )
        )
        ModalNavigationDrawer(
            drawerState = drawerState,

            drawerContent = {
                ModalDrawerSheet {
                }
            },
            gesturesEnabled = false,
        ) { }
        Scaffold(
            content = { padding ->
                ModsScreen(padding)
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { /* TODO */ },
                    text = { Text("Play Modded") },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_play),
                            contentDescription = "Play Modded"
                        )
                    },
                )
            },
            bottomBar = {
                NavigationBar {
                    navBarItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            label = { Text(item.title) },
                            selected = index == selectedNavBarItemIndex,
                            onClick = {
                                selectedNavBarItemIndex = index
                            },
                            icon = {
                                Icon(
                                    painter = if (index == selectedNavBarItemIndex) {
                                        item.onSelectedIcon
                                    } else {
                                        item.unselectedIcon
                                    }, contentDescription = item.description
                                )
                            },
                        )
                    }
                }
            }
        )
    }
}

