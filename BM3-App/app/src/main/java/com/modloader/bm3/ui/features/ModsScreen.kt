package com.modloader.bm3.ui.features

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.modloader.bm3.R
import com.modloader.bm3.ui.components.BottomNavBarItem
import com.modloader.bm3.ui.components.ModCard
import com.modloader.bm3.ui.components.ResponsiveCardsSection
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModsScreen(viewModel: ModsViewModel = hiltViewModel()) {
    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )
        var isBackHandlerEnabled by rememberSaveable { mutableStateOf(true) }
        val backHandlerCoroutineScope = rememberCoroutineScope()
        val modsScreenState by viewModel.modsScreenState.collectAsStateWithLifecycle()
        val mods = modsScreenState.mods
        val showBottomSheet = modsScreenState.isBottomSheetShown
        val searchBarQuery = modsScreenState.searchBarQuery
        val context = LocalContext.current
        val focusManager = LocalFocusManager.current
        // rememberSaveable because it would be weird for navbar and drawer to change on rotation
        var isSearching by rememberSaveable { mutableStateOf(false) }
        var selectedNavBarItemIndex by rememberSaveable { mutableIntStateOf(0) }
        val borderColor by animateColorAsState(
            targetValue = if (isSearching) MaterialTheme.colorScheme.primary else Color.Transparent,
            label = "SearchBarBorderColor"
        )
        val modsContainerPadding = 8.dp
        val searchBarPadding = 16.dp
        val bottomSheetVerticalSpacing = 32.dp
        val bottomSheetContainerAllPadding = 16.dp
        BackHandler(enabled = isSearching) {
            focusManager.clearFocus()
            // clear mods that come from search results because it is unexpected for users
            viewModel.resetMods()
        }
        // enable second back handler to take precedence over first
        BackHandler(enabled = (!isSearching and isBackHandlerEnabled)) {
            Toast.makeText(context, "Press back again to confirm", Toast.LENGTH_SHORT).show()

            // Disable our handler so the VERY NEXT back press falls back to system back
            isBackHandlerEnabled = false

            // Re-enable after 2 seconds if the user didn't press back again
            backHandlerCoroutineScope.launch {
                delay(2000L.milliseconds)
                isBackHandlerEnabled = true
            }
        }
        val navBarItems = listOf(
            BottomNavBarItem(
                title = "Mods",
                description = "Mods",
                onSelectedIcon = painterResource(R.drawable.ic_grid_view_filled),
                unselectedIcon = painterResource(R.drawable.ic_grid_view),
                hasBadge = true

            ), BottomNavBarItem(
                title = "Modpacks",
                description = "Modpacks",
                onSelectedIcon = painterResource(R.drawable.ic_stacks_filled),
                unselectedIcon = painterResource(R.drawable.ic_stacks),
                hasBadge = false
            ), BottomNavBarItem(
                title = "Settings",
                description = "Settings",
                onSelectedIcon = painterResource(R.drawable.ic_settings_filled),
                unselectedIcon = painterResource(R.drawable.ic_settings),
                hasBadge = false
            )
        )
        Scaffold(topBar = {
            Column(Modifier.padding(vertical = searchBarPadding)) {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .semantics { traversalIndex = 0f }
                        .onFocusChanged(
                            {
                                isSearching = it.isFocused
                            })
                        .border(
                            width = 2.dp, color = borderColor, shape = CircleShape
                        ),
                    value = searchBarQuery,
                    onValueChange = viewModel::onSearchTextChanged,
                    placeholder = { Text(stringResource(R.string.search_for_a_mod)) },
                    leadingIcon = {

                    },
                    shape = CircleShape,
                    colors = TextFieldDefaults.colors(
                        //                    Remove the default underline, maybe keep error or disabled later IDK
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent
                    )
                )
            }
        }, content = { padding ->
            Column(
                Modifier.padding(modsContainerPadding)
            ) {
                if (!isSearching) {
                    LazyColumn(
                        modifier = Modifier.padding(modsContainerPadding)
                    ) {
                        items(count = 1) {
                            ResponsiveCardsSection(
                                "Installed Mods",
                                modsScreenState.installedMods,
                                padding,
                                viewModel::onCardSelected
                            )
                            ResponsiveCardsSection(
                                "Disabled Mods",
                                modsScreenState.disabledMods,
                                padding,
                                viewModel::onCardSelected
                            )

                        }
                    }
                } else {
                    ResponsiveCardsSection(
                        sectionTitle = "",
                        mods = mods,
                        paddingValues = padding,
                        viewModel::onCardSelected
                    )
                }
                if (showBottomSheet) {
                    ModalBottomSheet(
                        modifier = Modifier.fillMaxHeight(),
                        sheetState = sheetState,
                        onDismissRequest = {
                            viewModel.onDismissSheet()
                        }) {
                        Column(
                            modifier = Modifier.padding(bottomSheetContainerAllPadding),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(bottomSheetVerticalSpacing)
                        ) {
                            val currentMod = modsScreenState.currentModSelected
                            if (currentMod == null) {
                                Text(stringResource(R.string.no_mod_selected))
                            } else {
                                ModCard(currentMod, viewModel::onCardSelected)
                                Text(
                                    currentMod.description.orEmpty(),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }

                        }
                    }
                }
            }
        }, floatingActionButton = {
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
        }, bottomBar = {
            NavigationBar(
            ) {
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
        })
    }
}
