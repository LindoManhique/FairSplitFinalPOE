package com.example.fairsplit

import android.app.Application
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // ---- Language persistence (shared across the app) ----
        // SharedPreferences file/key used everywhere:
        //   file: "settings"   key: "lang"
        // Values:
        //   ""      -> follow system
        //   "en"    -> English
        //   "af-ZA" -> Afrikaans (South Africa)  <-- matches values-af
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val raw = prefs.getString("lang", "") ?: ""
        val saved = prefs.getString("app_lang", "en") ?: "en"

        val tag = when (raw.lowercase()) {
            "af" -> "af-ZA"          // normalize if something stored old value
            else -> raw
        }

        val locales = if (tag.isBlank())
            LocaleListCompat.getEmptyLocaleList()
        else
            LocaleListCompat.forLanguageTags(tag)

        AppCompatDelegate.setApplicationLocales(locales)

        // ---- Firestore offline mode ----
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
    }
}
