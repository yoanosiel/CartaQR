package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Category
import com.example.data.model.MenuItem
import com.example.data.model.Restaurant
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {
  @Query("SELECT * FROM restaurants ORDER BY createdAt DESC")
  fun getAllRestaurants(): Flow<List<Restaurant>>

  @Query("SELECT * FROM restaurants WHERE id = :id LIMIT 1")
  suspend fun getRestaurantById(id: String): Restaurant?

  @Query("SELECT * FROM restaurants WHERE adminToken = :token LIMIT 1")
  suspend fun getRestaurantByAdminToken(token: String): Restaurant?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRestaurant(restaurant: Restaurant)

  @Query("DELETE FROM restaurants WHERE id = :id")
  suspend fun deleteRestaurant(id: String)

  @Query("SELECT * FROM categories WHERE restaurantId = :restaurantId ORDER BY displayOrder ASC")
  fun getCategoriesForRestaurant(restaurantId: String): Flow<List<Category>>

  @Query("SELECT * FROM categories WHERE restaurantId = :restaurantId ORDER BY displayOrder ASC")
  suspend fun getCategoriesList(restaurantId: String): List<Category>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCategory(category: Category)

  @Query("DELETE FROM categories WHERE id = :id")
  suspend fun deleteCategory(id: String)

  @Query("SELECT * FROM menu_items WHERE restaurantId = :restaurantId")
  fun getMenuItemsForRestaurant(restaurantId: String): Flow<List<MenuItem>>

  @Query("SELECT * FROM menu_items WHERE restaurantId = :restaurantId")
  suspend fun getMenuItemsList(restaurantId: String): List<MenuItem>

  @Query("SELECT * FROM menu_items WHERE id = :id LIMIT 1")
  suspend fun getMenuItemById(id: String): MenuItem?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMenuItem(item: MenuItem)

  @Query("DELETE FROM menu_items WHERE id = :id")
  suspend fun deleteMenuItem(id: String)

  @Query("UPDATE menu_items SET isAvailable = :isAvailable WHERE id = :id")
  suspend fun updateMenuItemAvailability(id: String, isAvailable: Boolean)
}
