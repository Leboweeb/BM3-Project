package com.modloader.bm3

import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @JvmField
    @Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

//    @Test
//    fun canDownloadInvariantModToCache() {
//        val tmpDir = tmpFolder.newFolder()
//        val steamodded = Pair("Steamodded", "Steamodded/smods")
//        runTest {
//            val githubApiModManager = GithubApiModManager()
//            // should be fine because unit tests run on local dev machine
//            val progressFlow =
//                githubApiModManager.downloadMod(tmpDir, steamodded.second, steamodded.first)
//            progressFlow.collect {
//                // output progress of download to terminal for now
//                println("Download progress is $it%.")
//            }
//        }
//        // make sure that mods actually downloaded and that they are actually .zip files
//        val firstFile = File(tmpDir, steamodded.first)
//        val isZipFile: (File) -> Boolean = { file ->
//            file.extension == "zip"
//        }
//        assert(firstFile.isFile and isZipFile(firstFile))
//    }
}