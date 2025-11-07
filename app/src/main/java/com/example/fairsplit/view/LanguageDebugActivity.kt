package com.example.fairsplit.view

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import com.example.fairsplit.R

class LanguageDebugActivity : AppCompatActivity() {

    private lateinit var tvInfo: TextView
    private fun showInfo() {
        val cfg = resources.configuration
        val current = ConfigurationCompat.getLocales(cfg).get(0)
        val code = current?.toLanguageTag() ?: "(system)"
        // Show a couple of strings that should change with locale:
        val titleHome = getString(R.string.title_home)
        val login = getString(R.string.title_login)
        tvInfo.text = "Locale: $code\nHOME='$titleHome'  LOGIN='$login'"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_debug)
        title = "Language Debug"

        tvInfo = findViewById(R.id.tvInfo)
        showInfo()

        findViewById<Button>(R.id.btnSystem).setOnClickListener {
            getSharedPreferences("settings", MODE_PRIVATE).edit()
                .putString("lang", "").apply()
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
            recreate()
        }
        findViewById<Button>(R.id.btnEnglish).setOnClickListener {
            getSharedPreferences("settings", MODE_PRIVATE).edit()
                .putString("lang", "en").apply()
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
            recreate()
        }
        findViewById<Button>(R.id.btnAfrikaans).setOnClickListener {
            // IMPORTANT: we use af-ZA because your folder is values-af
            getSharedPreferences("settings", MODE_PRIVATE).edit()
                .putString("lang", "af-ZA").apply()
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("af-ZA"))
            recreate()
        }
    }

    override fun onResume() {
        super.onResume()
        showInfo()
    }
}
