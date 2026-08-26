package com.example.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.model.Category
import com.example.data.model.MenuItem
import com.example.data.model.Restaurant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SupabaseApi(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("supabase_config", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    fun getSupabaseUrl(): String {
        return prefs.getString("supabase_url", "https://zoaetmigtjdrglzuziqw.supabase.co") ?: ""
    }

    fun getSupabaseKey(): String {
        return prefs.getString("supabase_key", "sb_publishable_kIcTp2Uqg8RsY0oN_ZVfOg_GUltEXw0") ?: ""
    }

    fun saveConfig(url: String, key: String) {
        val cleanUrl = url.trim().trimEnd('/')
        prefs.edit()
            .putString("supabase_url", cleanUrl)
            .putString("supabase_key", key.trim())
            .apply()
    }

    fun isConfigured(): Boolean {
        return getSupabaseUrl().isNotBlank() && getSupabaseKey().isNotBlank()
    }

    suspend fun testConnection(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val url = getSupabaseUrl()
        val key = getSupabaseKey()
        if (url.isBlank() || key.isBlank()) {
            return@withContext Pair(false, "Configuración incompleta: Ingresa la URL y Key de Supabase.")
        }

        try {
            val request = Request.Builder()
                .url("$url/rest/v1/restaurants?select=id&limit=1")
                .addHeader("apikey", key)
                .addHeader("Authorization", "Bearer $key")
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Pair(true, "Conexión exitosa con Supabase REST API!")
            } else {
                Pair(false, "Error de respuesta (${response.code}): ${response.message}")
            }
        } catch (e: Exception) {
            Log.e("SupabaseApi", "Connection failed", e)
            Pair(false, "Error al conectar: ${e.localizedMessage}")
        }
    }

    suspend fun syncRestaurantToCloud(
        restaurant: Restaurant,
        categories: List<Category>,
        items: List<MenuItem>
    ): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false

        val url = getSupabaseUrl()
        val key = getSupabaseKey()
        val mediaType = "application/json; charset=utf-8".toMediaType()

        try {
            // 1. Upsert Restaurant
            val restObj = JSONObject().apply {
                put("id", restaurant.id)
                put("name", restaurant.name)
                put("description", restaurant.description)
                put("logo_url", restaurant.logoUrl)
                put("admin_token", restaurant.adminToken)
                put("theme_name", restaurant.themeName)
            }

            val restReq = Request.Builder()
                .url("$url/rest/v1/restaurants")
                .addHeader("apikey", key)
                .addHeader("Authorization", "Bearer $key")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(restObj.toString().toRequestBody(mediaType))
                .build()

            val restResp = client.newCall(restReq).execute()
            if (!restResp.isSuccessful) {
                Log.e("SupabaseApi", "Failed to sync restaurant: ${restResp.body?.string()}")
                return@withContext false
            }

            // 2. Upsert Categories
            if (categories.isNotEmpty()) {
                val catArray = JSONArray()
                categories.forEach { c ->
                    catArray.put(JSONObject().apply {
                        put("id", c.id)
                        put("restaurant_id", c.restaurantId)
                        put("name", c.name)
                        put("display_order", c.displayOrder)
                    })
                }

                val catReq = Request.Builder()
                    .url("$url/rest/v1/categories")
                    .addHeader("apikey", key)
                    .addHeader("Authorization", "Bearer $key")
                    .addHeader("Prefer", "resolution=merge-duplicates")
                    .post(catArray.toString().toRequestBody(mediaType))
                    .build()

                client.newCall(catReq).execute()
            }

            // 3. Upsert Menu Items
            if (items.isNotEmpty()) {
                val itemArray = JSONArray()
                items.forEach { i ->
                    itemArray.put(JSONObject().apply {
                        put("id", i.id)
                        put("restaurant_id", i.restaurantId)
                        put("category_id", i.categoryId)
                        put("name", i.name)
                        put("description", i.description)
                        put("price", i.price)
                        put("image_url", i.imageUrl)
                        put("is_available", i.isAvailable)
                        put("is_vegetarian", i.isVegetarian)
                        put("is_gluten_free", i.isGlutenFree)
                        put("is_spicy", i.isSpicy)
                        put("allergens", i.allergens)
                    })
                }

                val itemReq = Request.Builder()
                    .url("$url/rest/v1/menu_items")
                    .addHeader("apikey", key)
                    .addHeader("Authorization", "Bearer $key")
                    .addHeader("Prefer", "resolution=merge-duplicates")
                    .post(itemArray.toString().toRequestBody(mediaType))
                    .build()

                client.newCall(itemReq).execute()
            }

            true
        } catch (e: Exception) {
            Log.e("SupabaseApi", "Sync failed", e)
            false
        }
    }
}
