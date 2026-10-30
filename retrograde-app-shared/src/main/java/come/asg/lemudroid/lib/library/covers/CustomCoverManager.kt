package come.asg.lemudroid.lib.library.covers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import come.asg.lemudroid.lib.library.db.entity.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

/** Manages durable, processed custom cover artwork in app-private storage. */
class CustomCoverManager(private val appContext: Context) {
    fun getCoversDirectory(): File =
        File(appContext.getExternalFilesDir(null), "custom-covers").apply { mkdirs() }

    private fun coverFileForGame(gameId: Int): File = File(getCoversDirectory(), "cover_$gameId.jpg")

    /** Imports, center-crops, and scales artwork, returning a durable local URI. */
    suspend fun importCover(gameId: Int, sourceUri: Uri): String =
        withContext(Dispatchers.IO) {
            val bitmap = appContext.contentResolver.openInputStream(sourceUri)?.use {
                BitmapFactory.decodeStream(it)
            } ?: throw IllegalStateException("Cannot decode input stream for $sourceUri")
            val processed = process(bitmap)
            val destination = coverFileForGame(gameId)
            deleteCoversForGameId(gameId)
            FileOutputStream(destination).use { output ->
                check(processed.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)) {
                    "Cannot encode custom cover for game $gameId"
                }
            }
            if (processed !== bitmap) bitmap.recycle()
            processed.recycle()
            Uri.fromFile(destination).toString()
        }

    fun deleteCover(gameId: Int) = deleteCoversForGameId(gameId)

    fun deleteCoversForGames(games: List<Game>) {
        games.forEach { game ->
            if (game.customCoverUri != null) deleteCoversForGameId(game.id)
        }
    }

    private fun process(source: Bitmap): Bitmap {
        val side = minOf(source.width, source.height)
        val left = (source.width - side) / 2
        val top = (source.height - side) / 2
        val square = Bitmap.createBitmap(source, left, top, side, side)
        return if (side > MAX_DIMENSION) {
            Bitmap.createScaledBitmap(square, MAX_DIMENSION, MAX_DIMENSION, true).also {
                if (it !== square) square.recycle()
            }
        } else {
            square
        }
    }

    private fun deleteCoversForGameId(gameId: Int) {
        val prefix = "cover_$gameId."
        getCoversDirectory().listFiles()
            ?.filter { it.name.startsWith(prefix) }
            ?.forEach { file -> Timber.d("Deleted custom cover ${file.name}: ${file.delete()}") }
    }

    private companion object {
        const val MAX_DIMENSION = 512
        const val JPEG_QUALITY = 90
    }
}
