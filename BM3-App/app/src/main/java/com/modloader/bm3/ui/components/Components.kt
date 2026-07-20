package com.modloader.bm3.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.modloader.bm3.R
import com.modloader.bm3.domain.model.ModModel
import java.net.URI
import androidx.core.net.toUri


data class BottomNavBarItem(
    val title: String,
    val description: String,
    val onSelectedIcon: Painter,
    val unselectedIcon: Painter,
    val hasBadge: Boolean
)

data class ImageItem(
    val title: String,
    val description: String?,
    val thumbnail : String?
)

fun LazyGridScope.gridHeader(
    content: @Composable LazyGridItemScope.() -> Unit
) {
    item(span = { GridItemSpan(this.maxLineSpan) }, content = content)
}


@Composable
fun ModCard(
    imageItem: ImageItem,
    maxHeight: Int = 300,
    maxWidth: Int = 300
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .widthIn(0.dp, maxWidth.dp)
            .heightIn(0.dp, maxHeight.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            if (imageItem.thumbnail.isNullOrBlank()) {
                Image(
                    painter = painterResource(R.drawable.ic_image),
                    contentDescription = imageItem.title,
                )
            }

            val decodedImageBytes = Base64.decode(imageItem.thumbnail, Base64.DEFAULT)
            val decodedImageToString = String(decodedImageBytes)
            if (Patterns.WEB_URL.matcher(decodedImageToString).matches()) {
                AsyncImage(
                    model = imageItem.thumbnail,
                    contentDescription = imageItem.description
                )
            } else {
                Image(
                    bitmap = BitmapFactory.decodeByteArray(decodedImageBytes,0,decodedImageBytes.size).asImageBitmap(),
                    contentDescription = imageItem.title,
                )
            }

        }
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(imageItem.title, style = MaterialTheme.typography.titleLarge)
            val description = if (imageItem.description.isNullOrBlank()) {
                LoremIpsum(25).values.first()
            } else { imageItem.description }
            Text(description)
        }
    }

}

@Composable
fun ResponsiveCardsSection(
    sectionTitle: String,
    mods : List<ModModel>,
    paddingValues: PaddingValues,
    maxHeight: Dp = 1000.dp
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
            items(mods) { mod ->
                ModCard(
                    ImageItem(
                        title = mod.title,
                        description = mod.description,
                        thumbnail = mod.thumbnail
                    )
                )
            }
        }
    }
}
