package com.example.fairsplit

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LanguageManager {

    /** Use language tags like: "", "en", "af"
     *  "" = follow system  */
    fun apply(context: Context, tag: String) {
        // Save
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit()
            .putString("lang", tag)
            .apply()

        // Apply (triggers recomposition on next Activity recreation)
        val list = if (tag.isBlank()) {
            LocaleListCompat.getEmptyLocaleList() // follow system
        } else {
            LocaleListCompat.forLanguageTags(tag)
        }
        AppCompatDelegate.setApplicationLocales(list)
    }

    fun currentTag(context: Context): String {
        return context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getString("lang", "") ?: ""
    }
}
