package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurants")
data class Restaurant(
  @PrimaryKey val id: String,
  val name: String,
  val description: String = "",
  val logoUrl: String = "",
  val adminToken: String,
  val themeName: String = "Moderno", // "Moderno", "Clásico", "Elegante"
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class Category(
  @PrimaryKey val id: String,
  val restaurantId: String,
  val name: String,
  val displayOrder: Int = 0
)

@Entity(tableName = "menu_items")
data class MenuItem(
  @PrimaryKey val id: String,
  val restaurantId: String,
  val categoryId: String,
  val name: String,
  val description: String = "",
  val price: Double,
  val imageUrl: String = "",
  val isAvailable: Boolean = true,
  val isVegetarian: Boolean = false,
  val isGlutenFree: Boolean = false,
  val isSpicy: Boolean = false,
  val allergens: String = "" // comma separated e.g. "Lácteos,Frutos Secos"
)
