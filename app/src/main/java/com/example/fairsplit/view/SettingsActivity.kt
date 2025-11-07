package com.example.fairsplit.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.example.fairsplit.R
import com.example.fairsplit.databinding.ActivitySettingsBinding
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var b: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(b.root)

        title = getString(R.string.title_settings)

        // Prefill from SharedPreferences
        lifecycleScope.launch {
            setLoading(true)
            try {
                val s = SettingsLocal.load(this@SettingsActivity)
                b.etDisplayName.setText(s.displayName ?: "")
                b.etCurrencyCode.setText(s.currencyCode ?: "")
            } finally {
                setLoading(false)
            }
        }

        // SAVE → toast → go to Groups
        b.btnSave.setOnClickListener {
            lifecycleScope.launch {
                setLoading(true)
                try {
                    val name = b.etDisplayName.text?.toString()?.trim().orEmpty()
                    val code = b.etCurrencyCode.text?.toString()?.trim()
                        ?.uppercase(Locale.ROOT).orEmpty()

                    SettingsLocal.save(this@SettingsActivity, name, code)

                    applyLanguageFromSpinner() // apply + persist language

                    Toast.makeText(this@SettingsActivity, getString(R.string.msg_saved), Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this@SettingsActivity, GroupsActivity::class.java))
                    finish()
                } finally {
                    setLoading(false)
                }
            }
        }

        // Fetch rate demo
        b.btnFetchRates.setOnClickListener {
            lifecycleScope.launch {
                setLoading(true)
                try {
                    val code = b.etCurrencyCode.text?.toString()?.trim()
                        ?.uppercase(Locale.ROOT).orEmpty()
                    val rate = SettingsLocal.fetchRate(code)
                    b.tvRate.text = rate.toString()
                } finally {
                    setLoading(false)
                }
            }
        }

        initLanguageSpinner()
        initOfflineSwitchIfPresent()
    }

    private fun setLoading(on: Boolean) {
        b.progress.visibility = if (on) View.VISIBLE else View.GONE
        b.btnSave.isEnabled = !on
        b.btnFetchRates.isEnabled = !on
        b.etDisplayName.isEnabled = !on
        b.etCurrencyCode.isEnabled = !on
    }

    // --- Language spinner helpers ---

    private fun applyLanguageFromSpinner() {
        val spinner = findViewById<Spinner?>(R.id.spLanguage) ?: return

        val values = safeStringArray(R.array.language_values) ?: return
        val chosenIndex = spinner.selectedItemPosition.coerceIn(0, values.size - 1)
        val chosen = values[chosenIndex] // "", "en", "af"

        // Persist choice in the SAME prefs App.kt reads:
        // App.kt uses: getSharedPreferences("prefs", MODE_PRIVATE).getString("app_lang", "en")
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        prefs.edit().putString("app_lang", chosen).apply()

        // Apply NOW
        val locales = if (chosen.isBlank())
            LocaleListCompat.getEmptyLocaleList()
        else
            LocaleListCompat.forLanguageTags(chosen)
        AppCompatDelegate.setApplicationLocales(locales)

        // Refresh this activity’s UI immediately
        recreate()
    }

    private fun initLanguageSpinner() {
        val spinner = findViewById<Spinner?>(R.id.spLanguage) ?: return

        val names = safeStringArray(R.array.language_names)
        val values = safeStringArray(R.array.language_values)
        if (names == null || values == null || names.size != values.size) return

        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, names.toList())

        // Read from the SAME prefs App.kt uses
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val saved = prefs.getString("app_lang", "") ?: ""   // "", "en", or "af"
        val preselect = values.indexOf(saved).takeIf { it >= 0 } ?: 0
        spinner.setSelection(preselect, false)
    }

    // --- Optional offline switch ---
    private fun initOfflineSwitchIfPresent() {
        val sw = findViewById<SwitchMaterial?>(R.id.swOffline) ?: return

        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val isOffline = prefs.getBoolean("offline", false)
        sw.isChecked = isOffline

        val db = FirebaseFirestore.getInstance()
        if (isOffline) db.disableNetwork() else db.enableNetwork()

        sw.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("offline", checked).apply()
            if (checked) db.disableNetwork() else db.enableNetwork()
            val msg = if (checked) getString(R.string.msg_offline_on)
            else getString(R.string.msg_offline_off)
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun safeStringArray(resId: Int): Array<String>? = try {
        resources.getStringArray(resId)
    } catch (_: Throwable) {
        null
    }
}

// --- Local data store (unchanged for profile fields) ---
private object SettingsLocal {
    private const val PREF = "settings"
    private const val KEY_NAME = "display_name"
    private const val KEY_CCY = "currency_code"

    data class Settings(val displayName: String?, val currencyCode: String?)

    suspend fun load(context: Context): Settings = withContext(Dispatchers.IO) {
        val p = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        Settings(
            displayName = p.getString(KEY_NAME, null),
            currencyCode = p.getString(KEY_CCY, null)
        )
    }

    suspend fun save(context: Context, displayName: String, currencyCode: String) =
        withContext(Dispatchers.IO) {
            val p = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            p.edit()
                .putString(KEY_NAME, displayName)
                .putString(KEY_CCY, currencyCode)
                .apply()
        }

    suspend fun fetchRate(currencyCode: String): Double = withContext(Dispatchers.IO) {
        delay(300)
        when (currencyCode.uppercase(Locale.ROOT)) {
            "USD" -> 1.00
            "ZAR" -> 18.50
            "EUR" -> 0.92
            else -> 1.00
        }
    }
}
