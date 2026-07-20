package com.modloader.bm3.ui.features

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.modloader.bm3.ui.components.ResponsiveCardsSection
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@Composable
fun ModsScreen(padding : PaddingValues, viewModel: ModsViewModel = hiltViewModel() ) {
//    val imageItems = listOf(
//        ImageItem(title = "Sample Text", description = "", imageUrl = null),
//        ImageItem(title = "Sample Text", description = "", imageUrl = null),
//        ImageItem(title = "Sample Text", description = "", imageUrl = null),
//        ImageItem(title = "Sample Text", description = "", imageUrl = null),
//        ImageItem(title = "Sample Text", description = "", imageUrl = null),
//    )
    val modsScreenState = viewModel.modsScreenState.collectAsStateWithLifecycle().value
    val mods = modsScreenState.mods
    LazyColumn (
        modifier = Modifier.padding(8.dp)
    ) {
        items(count = 1) {
            ResponsiveCardsSection("Installed Mods",mods, padding)
            ResponsiveCardsSection("Disabled Mods",mods, padding)
        }
    }
}