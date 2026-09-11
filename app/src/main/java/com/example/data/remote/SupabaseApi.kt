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
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    fun getSupabaseUrl(): String =
        prefs.getString("supabase_url", "https://zoaetmigtjdrglzuziqw.supabase.co") ?: ""

    fun getSupabaseKey(): String =
        prefs.getString("supabase_key", "sb_publishable_kIcTp2Uqg8RsY0oN_ZVfOg_GUltEXw0") ?: ""

    fun saveConfig(url: String, key: String) {
        prefs.edit()
            .putString("supabase_url", url.trim().trimEnd('/'))
            .putString("supabase_key", key.trim())
            .apply()
    }

    fun isConfigured(): Boolean =
        getSupabaseUrl().isNotBlank() && getSupabaseKey().isNotBlank()

    private fun requestBuilder(path: String): Request.Builder = Request.Builder()
        .url("${getSupabaseUrl()}$path")
        .addHeader("apikey", getSupabaseKey())
        .addHeader("Authorization", "Bearer ${getSupabaseKey()}")

    suspend fun testConnection(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext Pair(false, "Configuración incompleta: URL/Key de Supabase.")
        try {
            val response = client.newCall(
                requestBuilder("/rest/v1/restaurants?select=id&limit=1").get().build()
            ).execute()
            response.use {
                if (it.isSuccessful) Pair(true, "Conexión exitosa con Supabase REST API")
                else Pair(false, "Supabase HTTP ${it.code}: ${it.body?.string() ?: it.message}")
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
        try {
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val restObj = JSONObject().apply {
                put("id", restaurant.id)
                put("name", restaurant.name)
                put("description", restaurant.description)
                put("logo_url", restaurant.logoUrl)
                put("admin_token", restaurant.adminToken)
                put("theme_name", restaurant.themeName)
            }
            val restResp = client.newCall(
                requestBuilder("/rest/v1/restaurants")
                    .addHeader("Prefer", "resolution=merge-duplicates")
                    .post(restObj.toString().toRequestBody(mediaType)).build()
            ).execute()
            if (!restResp.isSuccessful) {
                Log.e("SupabaseApi", "Restaurant sync failed ${restResp.code}: ${restResp.body?.string()}")
                return@withContext false
            }

            if (categories.isNotEmpty()) {
                val array = JSONArray()
                categories.forEach { c ->
                    array.put(JSONObject().apply {
                        put("id", c.id)
                        put("restaurant_id", c.restaurantId)
                        put("name", c.name)
                        put("display_order", c.displayOrder)
                    })
                }
                val response = client.newCall(
                    requestBuilder("/rest/v1/categories")
                        .addHeader("Prefer", "resolution=merge-duplicates")
                        .post(array.toString().toRequestBody(mediaType)).build()
                ).execute()
                if (!response.isSuccessful) {
                    Log.e("SupabaseApi", "Category sync failed ${response.code}: ${response.body?.string()}")
                    return@withContext false
                }
            }

            if (items.isNotEmpty()) {
                val array = JSONArray()
                items.forEach { i ->
                    array.put(JSONObject().apply {
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
                val response = client.newCall(
                    requestBuilder("/rest/v1/menu_items")
                        .addHeader("Prefer", "resolution=merge-duplicates")
                        .post(array.toString().toRequestBody(mediaType)).build()
                ).execute()
                if (!response.isSuccessful) {
                    Log.e("SupabaseApi", "Menu sync failed ${response.code}: ${response.body?.string()}")
                    return@withContext false
                }
            }
            true
        } catch (e: Exception) {
            Log.e("SupabaseApi", "Sync failed", e)
            false
        }
    }

    suspend fun getRestaurantById(id: String): Restaurant? = withContext(Dispatchers.IO) {
        getRestaurants("id=eq.${encode(id)}&limit=1").firstOrNull()
    }

    suspend fun getRestaurantByAdminToken(token: String): Restaurant? = withContext(Dispatchers.IO) {
        getRestaurants("admin_token=eq.${encode(token)}&limit=1").firstOrNull()
    }

    suspend fun getAllRestaurants(): List<Restaurant> = withContext(Dispatchers.IO) {
        getRestaurants("select=*&order=created_at.desc")
    }

    suspend fun getCategories(restaurantId: String): List<Category> = withContext(Dispatchers.IO) {
        val json = getArray("/rest/v1/categories?restaurant_id=eq.${encode(restaurantId)}&order=display_order.asc")
        buildList {
            for (i in 0 until json.length()) {
                val o = json.getJSONObject(i)
                add(Category(
                    id = o.getString("id"),
                    restaurantId = o.getString("restaurant_id"),
                    name = o.getString("name"),
                    displayOrder = o.optInt("display_order", o.optInt("sort_order", 0))
                ))
            }
        }
    }

    suspend fun getMenuItems(restaurantId: String): List<MenuItem> = withContext(Dispatchers.IO) {
        val json = getArray("/rest/v1/menu_items?restaurant_id=eq.${encode(restaurantId)}&order=sort_order.asc")
        buildList {
            for (i in 0 until json.length()) {
                val o = json.getJSONObject(i)
                add(MenuItem(
                    id = o.getString("id"),
                    restaurantId = o.getString("restaurant_id"),
                    categoryId = o.optString("category_id", ""),
                    name = o.getString("name"),
                    description = o.optString("description", ""),
                    price = o.optDouble("price", 0.0),
                    imageUrl = o.optString("image_url", ""),
                    isAvailable = o.optBoolean("is_available", o.optBoolean("available", true)),
                    isVegetarian = o.optBoolean("is_vegetarian", false),
                    isGlutenFree = o.optBoolean("is_gluten_free", false),
                    isSpicy = o.optBoolean("is_spicy", false),
                    allergens = o.optString("allergens", "")
                ))
            }
        }
    }

    private fun getRestaurants(filter: String): List<Restaurant> {
        val json = getArray("/rest/v1/restaurants?$filter")
        return buildList {
            for (i in 0 until json.length()) {
                val o = json.getJSONObject(i)
                add(Restaurant(
                    id = o.getString("id"),
                    name = o.getString("name"),
                    description = o.optString("description", ""),
                    logoUrl = o.optString("logo_url", ""),
                    adminToken = o.optString("admin_token", ""),
                    themeName = o.optString("theme_name", "Moderno"),
                    createdAt = try { o.getString("created_at").let { java.time.OffsetDateTime.parse(it).toInstant().toEpochMilli() } } catch (_: Exception) { System.currentTimeMillis() }
                ))
            }
        }
    }

    private fun getArray(path: String): JSONArray {
        val response = client.newCall(requestBuilder(path).get().build()).execute()
        response.use {
            val body = it.body?.string().orEmpty()
            if (!it.isSuccessful) throw IllegalStateException("Supabase HTTP ${it.code}: $body")
            return JSONArray(body)
        }
    }

    private fun encode(value: String): String =
        java.net.URLEncoder.encode(value, Charsets.UTF_8.name())
}
