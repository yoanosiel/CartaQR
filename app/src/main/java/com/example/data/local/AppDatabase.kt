package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Category
import com.example.data.model.MenuItem
import com.example.data.model.Restaurant

@Database(
  entities = [Restaurant::class, Category::class, MenuItem::class],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun restaurantDao(): RestaurantDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "carta_qr_database"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
