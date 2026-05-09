package com.modloader.bm3

import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.lifecycleScope
import com.modloader.bm3.ui.theme.BM3Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BM3Theme {
                BM3App()
            }
        }
        // query lmm.shorty systems apk for files
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val loveFSAuthority = "systems.shorty.lmm.balatro"
                val rootURI = DocumentsContract.buildChildDocumentsUri(loveFSAuthority,"")
                try {
                    val cursor = contentResolver.query(
                        rootURI,
                        arrayOf(
                            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                            DocumentsContract.Document.COLUMN_MIME_TYPE,
                            DocumentsContract.Document.COLUMN_DOCUMENT_ID
                        ),
                        null, null, null
                    )

                    cursor?.use {
                        if (it.moveToFirst()) {
                            val nameIdx =
                                it.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                            val idIdx = it.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)

                            // 3. Log what we found
                            do {
                                val name = it.getString(nameIdx)
                                val docId = it.getString(idIdx)
                                Log.d("LOVE_TEST", "Found: $name (ID: $docId)")
                            } while (it.moveToNext())

                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Found ${it.count} files in LÖVE!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Log.w("LOVE_TEST", "Folder 'saves' found but is empty.")


                        } ?: Log.e("LOVE_TEST", "Failed to query LÖVE provider. Is it installed?")
                    }
                }
                catch (e : Exception) {
                    Log.e("LOVE_TEST","Error: ${e.message}")
                }
            }

        }
    }
}

@PreviewScreenSizes
@Composable
fun BM3App() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            painterResource(it.icon),
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Greeting(
                name = "Android",
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    HOME("Home", R.drawable.ic_home),
    FAVORITES("Favorites", R.drawable.ic_favorite),
    PROFILE("Profile", R.drawable.ic_account_box),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BM3Theme {
        Greeting("Android")
    }
}