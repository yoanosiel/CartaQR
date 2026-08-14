package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.RestaurantDao
import com.example.data.model.Category
import com.example.data.model.MenuItem
import com.example.data.model.Restaurant
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class MenuRepository(context: Context) {
  private val dao: RestaurantDao = AppDatabase.getDatabase(context).restaurantDao()

  val allRestaurants: Flow<List<Restaurant>> = dao.getAllRestaurants()

  suspend fun getRestaurantById(id: String): Restaurant? {
    return dao.getRestaurantById(id)
  }

  suspend fun getRestaurantByAdminToken(token: String): Restaurant? {
    return dao.getRestaurantByAdminToken(token)
  }

  suspend fun createRestaurant(
    name: String,
    description: String,
    logoUrl: String,
    themeName: String = "Moderno"
  ): Restaurant {
    val id = "rest_${UUID.randomUUID().toString().take(8)}"
    val adminToken = "ADM-${UUID.randomUUID().toString().take(6).uppercase()}"
    val restaurant = Restaurant(
      id = id,
      name = name,
      description = description,
      logoUrl = logoUrl,
      adminToken = adminToken,
      themeName = themeName
    )
    dao.insertRestaurant(restaurant)

    // Insert default categories
    val cat1 = Category(id = "cat_${UUID.randomUUID().toString().take(6)}", restaurantId = id, name = "Entrantes", displayOrder = 1)
    val cat2 = Category(id = "cat_${UUID.randomUUID().toString().take(6)}", restaurantId = id, name = "Platos Principales", displayOrder = 2)
    val cat3 = Category(id = "cat_${UUID.randomUUID().toString().take(6)}", restaurantId = id, name = "Postres y Bebidas", displayOrder = 3)
    
    dao.insertCategory(cat1)
    dao.insertCategory(cat2)
    dao.insertCategory(cat3)

    // Insert sample menu items for instant demo delight
    val item1 = MenuItem(
      id = "item_${UUID.randomUUID().toString().take(6)}",
      restaurantId = id,
      categoryId = cat1.id,
      name = "Croquetas Caseras de Jamón",
      description = "Bechamel cremosa con jamón ibérico de bellota (6 ud.)",
      price = 9.50,
      imageUrl = "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=500",
      isAvailable = true,
      allergens = "Lácteos, Gluten"
    )
    val item2 = MenuItem(
      id = "item_${UUID.randomUUID().toString().take(6)}",
      restaurantId = id,
      categoryId = cat2.id,
      name = "Hamburguesa Gourmet Trufada",
      description = "Carne 100% vacuno, queso cheddar fundido, cebolla caramelizada y mahonesa de trufa",
      price = 14.90,
      imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500",
      isAvailable = true,
      allergens = "Gluten, Lácteos, Huevo"
    )
    val item3 = MenuItem(
      id = "item_${UUID.randomUUID().toString().take(6)}",
      restaurantId = id,
      categoryId = cat3.id,
      name = "Tarta de Queso Cobre",
      description = "Tarta horneada al estilo San Sebastián con corazón cremoso",
      price = 6.00,
      imageUrl = "https://images.unsplash.com/photo-1533134242443-d4fd215305ad?w=500",
      isAvailable = true,
      isVegetarian = true,
      allergens = "Lácteos, Huevo"
    )

    dao.insertMenuItem(item1)
    dao.insertMenuItem(item2)
    dao.insertMenuItem(item3)

    return restaurant
  }

  suspend fun updateRestaurant(restaurant: Restaurant) {
    dao.insertRestaurant(restaurant)
  }

  suspend fun deleteRestaurant(id: String) {
    dao.deleteRestaurant(id)
  }

  // Category management
  fun getCategories(restaurantId: String): Flow<List<Category>> {
    return dao.getCategoriesForRestaurant(restaurantId)
  }

  suspend fun addCategory(restaurantId: String, name: String): Category {
    val existing = dao.getCategoriesList(restaurantId)
    val category = Category(
      id = "cat_${UUID.randomUUID().toString().take(6)}",
      restaurantId = restaurantId,
      name = name,
      displayOrder = existing.size + 1
    )
    dao.insertCategory(category)
    return category
  }

  suspend fun deleteCategory(id: String) {
    dao.deleteCategory(id)
  }

  // Menu item management
  fun getMenuItems(restaurantId: String): Flow<List<MenuItem>> {
    return dao.getMenuItemsForRestaurant(restaurantId)
  }

  suspend fun saveMenuItem(item: MenuItem) {
    dao.insertMenuItem(item)
  }

  suspend fun deleteMenuItem(id: String) {
    dao.deleteMenuItem(id)
  }

  suspend fun toggleMenuItemAvailability(id: String, isAvailable: Boolean) {
    dao.updateMenuItemAvailability(id, isAvailable)
  }
}
