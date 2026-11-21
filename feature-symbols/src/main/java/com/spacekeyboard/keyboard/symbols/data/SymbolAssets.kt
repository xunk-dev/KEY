package com.spacekeyboard.keyboard.symbols.data

import android.content.Context
import java.io.IOException

object SymbolAssets {
    private const val DIRECTORY = "symbols"

    fun load(context: Context): SymbolData = SymbolData.parse(
        symbolLines = read(context, "symbols.tsv"),
        featuredLines = read(context, "featured.tsv"),
        faceLines = read(context, "faces.tsv"),
        categoryLines = read(context, "face_categories.tsv"),
    )

    private fun read(context: Context, name: String): List<String> = try {
        context.assets.open("$DIRECTORY/$name").bufferedReader(Charsets.UTF_8).useLines { it.toList() }
    } catch (error: IOException) {
        emptyList()
    }
}
