package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.RestaurantDao
import com.example.data.model.Category
import com.example.data.model.MenuItem
import com.example.data.model.Restaurant
import com.example.data.remote.SupabaseApi
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class MenuRepository(context: Context) {
  private val dao: RestaurantDao = AppDatabase.getDatabase(context).restaurantDao()
  private val supabase = SupabaseApi(context)

  val allRestaurants: Flow<List<Restaurant>> = dao.getAllRestaurants()

  suspend fun getRestaurantById(id: String): Restaurant? {
    return try {
      supabase.getRestaurantById(id)?.also { cacheRestaurantData(it) } ?: dao.getRestaurantById(id)
    } catch (_: Exception) { dao.getRestaurantById(id) }
  }

  suspend fun getRestaurantByAdminToken(token: String): Restaurant? {
    return try {
      supabase.getRestaurantByAdminToken(token)?.also { cacheRestaurantData(it) } ?: dao.getRestaurantByAdminToken(token)
    } catch (_: Exception) { dao.getRestaurantByAdminToken(token) }
  }

  private suspend fun cacheRestaurantData(restaurant: Restaurant) {
    dao.insertRestaurant(restaurant)
    try {
      supabase.getCategories(restaurant.id).forEach { dao.insertCategory(it) }
      supabase.getMenuItems(restaurant.id).forEach { dao.insertMenuItem(it) }
    } catch (_: Exception) { }
  }

  suspend fun createRestaurant(name: String, description: String, logoUrl: String, themeName: String = "Moderno"): Restaurant {
    val id = "rest_${UUID.randomUUID().toString().take(8)}"
    val adminToken = "ADM-${UUID.randomUUID().toString().take(6).uppercase()}"
    val restaurant = Restaurant(id, name, description, logoUrl, adminToken, themeName)
    dao.insertRestaurant(restaurant)

    val cat1 = Category("cat_${UUID.randomUUID().toString().take(6)}", id, "Entrantes", 1)
    val cat2 = Category("cat_${UUID.randomUUID().toString().take(6)}", id, "Platos Principales", 2)
    val cat3 = Category("cat_${UUID.randomUUID().toString().take(6)}", id, "Postres y Bebidas", 3)
    dao.insertCategory(cat1); dao.insertCategory(cat2); dao.insertCategory(cat3)

    val item1 = MenuItem("item_${UUID.randomUUID().toString().take(6)}", id, cat1.id, "Croquetas Caseras de Jamón", "Bechamel cremosa con jamón ibérico de bellota (6 ud.)", 9.50, "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=500", true, false, false, false, "Lácteos, Gluten")
    val item2 = MenuItem("item_${UUID.randomUUID().toString().take(6)}", id, cat2.id, "Hamburguesa Gourmet Trufada", "Carne 100% vacuno, queso cheddar fundido, cebolla caramelizada y mahonesa de trufa", 14.90, "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500", true, false, false, false, "Gluten, Lácteos, Huevo")
    val item3 = MenuItem("item_${UUID.randomUUID().toString().take(6)}", id, cat3.id, "Tarta de Queso Cobre", "Tarta horneada al estilo San Sebastián con corazón cremoso", 6.00, "https://images.unsplash.com/photo-1533134242443-d4fd215305ad?w=500", true, true, false, false, "Lácteos, Huevo")
    dao.insertMenuItem(item1); dao.insertMenuItem(item2); dao.insertMenuItem(item3)

    if (!supabase.syncRestaurantToCloud(restaurant, listOf(cat1, cat2, cat3), listOf(item1, item2, item3))) {
      android.util.Log.e("MenuRepository", "Restaurant created locally but Supabase sync failed")
    }
    return restaurant
  }

  suspend fun updateRestaurant(restaurant: Restaurant) {
    dao.insertRestaurant(restaurant)
    supabase.syncRestaurantToCloud(restaurant, dao.getCategoriesList(restaurant.id), dao.getMenuItemsList(restaurant.id))
  }

  suspend fun deleteRestaurant(id: String) { dao.deleteRestaurant(id) }

  fun getCategories(restaurantId: String): Flow<List<Category>> = dao.getCategoriesForRestaurant(restaurantId)

  suspend fun addCategory(restaurantId: String, name: String): Category {
    val category = Category("cat_${UUID.randomUUID().toString().take(6)}", restaurantId, name, dao.getCategoriesList(restaurantId).size + 1)
    dao.insertCategory(category)
    syncLocalRestaurant(restaurantId)
    return category
  }

  suspend fun deleteCategory(id: String) { dao.deleteCategory(id) }

  fun getMenuItems(restaurantId: String): Flow<List<MenuItem>> = dao.getMenuItemsForRestaurant(restaurantId)

  suspend fun saveMenuItem(item: MenuItem) {
    dao.insertMenuItem(item)
    syncLocalRestaurant(item.restaurantId)
  }

  suspend fun deleteMenuItem(id: String) { dao.deleteMenuItem(id) }

  suspend fun toggleMenuItemAvailability(id: String, isAvailable: Boolean) {
    dao.updateMenuItemAvailability(id, isAvailable)
    val item = dao.getMenuItemById(id) ?: return
    syncLocalRestaurant(item.restaurantId)
  }

  private suspend fun syncLocalRestaurant(restaurantId: String) {
    val restaurant = dao.getRestaurantById(restaurantId) ?: return
    supabase.syncRestaurantToCloud(restaurant, dao.getCategoriesList(restaurantId), dao.getMenuItemsList(restaurantId))
  }
}
