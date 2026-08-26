package com.example.data.remote

import android.content.Context
import android.content.SharedPreferences

object SupabaseConfig {
  private const val PREF_NAME = "supabase_config"
  private const val KEY_URL = "supabase_url"
  private const val KEY_KEY = "supabase_key"

  fun getUrl(context: Context): String {
    val prefs = getPrefs(context)
    return prefs.getString(KEY_URL, "https://zoaetmigtjdrglzuziqw.supabase.co") ?: ""
  }

  fun getKey(context: Context): String {
    val prefs = getPrefs(context)
    return prefs.getString(KEY_KEY, "sb_publishable_kIcTp2Uqg8RsY0oN_ZVfOg_GUltEXw0") ?: ""
  }

  fun saveConfig(context: Context, url: String, key: String) {
    getPrefs(context).edit().putString(KEY_URL, url.trim()).putString(KEY_KEY, key.trim()).apply()
  }

  fun isConfigured(context: Context): Boolean {
    return getUrl(context).isNotBlank() && getKey(context).isNotBlank()
  }

  private fun getPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
  }
}
