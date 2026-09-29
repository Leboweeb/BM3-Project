package com.modloader.bm3.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.modloader.bm3.R
import com.modloader.bm3.ui.model.DownloadInfo
import com.modloader.bm3.ui.model.Mod
import com.modloader.bm3.utils.DownloadStatus


data class BottomNavBarItem(
    val title: String,
    val description: String,
    val onSelectedIcon: Painter,
    val unselectedIcon: Painter,
    val hasBadge: Boolean
)

fun LazyGridScope.gridHeader(
    content: @Composable LazyGridItemScope.() -> Unit
) {
    item(span = { GridItemSpan(this.maxLineSpan) }, content = content)
}

@Composable
fun ModCard(
    mod: Mod,
    onCardSelected: (Mod) -> Unit,
    onDownloadClicked: (Mod) -> Unit,
    onModCompleted: (Mod) -> Unit,
    downloadInfo: DownloadInfo?,
    maxHeight: Int = 300,
    maxWidth: Int = 300,
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .widthIn(0.dp, maxWidth.dp)
            .heightIn(0.dp, maxHeight.dp),
        // toggle bottom sheet on click
        onClick = { onCardSelected(mod) }) {
        Column {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                if (mod.thumbnail.isNullOrBlank()) {
                    Image(
                        painter = painterResource(R.drawable.ic_image),
                        contentDescription = mod.title,
                    )
                } else {
                    val decodedImageBytes = Base64.decode(mod.thumbnail, Base64.DEFAULT)
                    val decodedImageToString = String(decodedImageBytes)
                    if (Patterns.WEB_URL.matcher(decodedImageToString).matches()) {
                        AsyncImage(
                            model = mod.thumbnail, contentDescription = mod.description
                        )
                    } else {
                        Image(
                            bitmap = BitmapFactory.decodeByteArray(
                                decodedImageBytes, 0, decodedImageBytes.size
                            ).asImageBitmap(),
                            contentDescription = mod.title,
                        )
                    }
                }


            }
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(mod.title, style = MaterialTheme.typography.titleLarge)
                //            val description = if (imageItem.description.isNullOrBlank()) {
                //                LoremIpsum(25).values.first()
                //            } else { imageItem.description }
                //            Text(description)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,


                ) {


                downloadInfo?.let {
                    if (downloadInfo.state == DownloadStatus.SUCCESS) {
                        onModCompleted(mod)
                    }
                    if (downloadInfo.state !in listOf(
                            DownloadStatus.QUEUED,
                            DownloadStatus.FAILED
                        )
                    ) {
                        LinearProgressIndicator(
                            progress = { (downloadInfo.progress / 100.0f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                modifier = if (!mod.isInstalled) {
                    Modifier.fillMaxWidth()
                } else {
                    Modifier
                }.padding(horizontal = 16.dp),
                onClick = {
                    onDownloadClicked(mod)
                },
                enabled = !(mod.isInstalled or mod.isDownloading),
            ) {
                if (mod.isInstalled or mod.isDisabled) {
                    Text(stringResource(R.string.mod_installed))
                } else {
                    Text(stringResource(R.string.download_mod))
                }

            }
            if (mod.isInstalled) {
                Switch(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    checked = !mod.isDisabled, onCheckedChange = {},
                )
                IconButton(
                    onClick = {}, shape = RoundedCornerShape(50), colors = IconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error,
                        disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        disabledContentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete_outline),
                        contentDescription = "Delete mod ${mod.title}"
                    )
                }
            }
        }

    }
}


@Composable
fun ResponsiveCardsSection(
    sectionTitle: String,
    mods: List<Mod>,
    paddingValues: PaddingValues,
    onCardSelected: (Mod) -> Unit,
    onDownloadClicked: (Mod) -> Unit,
    onModCompleted: (Mod) -> Unit,
    getDownloadProgress: (Mod) -> DownloadInfo?,
    maxHeight: Dp = 1000.dp,
) {
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .heightIn(0.dp, maxHeight)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(300.dp),
        ) {
            gridHeader {
                Text(sectionTitle, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            }
            items(mods, key = { mod -> mod.id }) { mod ->
                ModCard(
                    mod,
                    onCardSelected,
                    onDownloadClicked,
                    onModCompleted,
                    getDownloadProgress(mod),
                )
            }
        }
    }
}
