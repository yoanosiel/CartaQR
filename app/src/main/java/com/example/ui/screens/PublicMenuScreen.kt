package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.Category
import com.example.data.model.MenuItem
import com.example.data.model.Restaurant
import com.example.data.repository.MenuRepository
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicMenuScreen(
  restaurantId: String,
  repository: MenuRepository,
  onBackClick: () -> Unit
) {
  var restaurant by remember { mutableStateOf<Restaurant?>(null) }
  var isLoading by remember { mutableStateOf(true) }

  var searchQuery by remember { mutableStateOf("") }
  var selectedCategoryId by remember { mutableStateOf<String?>(null) }
  var filterVegetarian by remember { mutableStateOf(false) }
  var filterGlutenFree by remember { mutableStateOf(false) }

  LaunchedEffect(restaurantId) {
    isLoading = true
    restaurant = repository.getRestaurantById(restaurantId)
    isLoading = false
  }

  if (isLoading) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      CircularProgressIndicator(color = SleekPurple)
    }
    return
  }

  val rest = restaurant
  if (rest == null) {
    Scaffold(containerColor = SleekBg) { padding ->
      Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("Carta no encontrada", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
          Button(onClick = onBackClick, colors = ButtonDefaults.buttonColors(containerColor = SleekPurple)) {
            Text("Volver")
          }
        }
      }
    }
    return
  }

  val categories by repository.getCategories(rest.id).collectAsStateWithLifecycle(initialValue = emptyList())
  val menuItems by repository.getMenuItems(rest.id).collectAsStateWithLifecycle(initialValue = emptyList())

  // Dynamic Theme Colors based on rest.themeName
  val themeBg = when (rest.themeName) {
    "Clásico" -> ClasicoBg
    "Elegante" -> EleganteBg
    else -> SleekBg
  }
  val themeCardBg = when (rest.themeName) {
    "Clásico" -> ClasicoCard
    "Elegante" -> EleganteCard
    else -> Color.White
  }
  val themeTextColor = when (rest.themeName) {
    "Elegante" -> Color.White
    else -> SleekTextPrimary
  }
  val themeAccent = when (rest.themeName) {
    "Clásico" -> ClasicoAccent
    "Elegante" -> EleganteGold
    else -> SleekPurple
  }

  val filteredItems = remember(menuItems, searchQuery, selectedCategoryId, filterVegetarian, filterGlutenFree) {
    menuItems.filter { item ->
      val matchesCategory = selectedCategoryId == null || item.categoryId == selectedCategoryId
      val matchesSearch = searchQuery.isBlank() ||
          item.name.contains(searchQuery, ignoreCase = true) ||
          item.description.contains(searchQuery, ignoreCase = true)
      val matchesVeg = !filterVegetarian || item.isVegetarian
      val matchesGluten = !filterGlutenFree || item.isGlutenFree

      matchesCategory && matchesSearch && matchesVeg && matchesGluten
    }
  }

  Scaffold(
    containerColor = themeBg,
    topBar = {
      TopAppBar(
        title = {
          Text(
            rest.name,
            fontWeight = FontWeight.Bold,
            color = themeTextColor
          )
        },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = themeTextColor)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = themeBg)
      )
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      contentPadding = PaddingValues(bottom = 32.dp)
    ) {
      // Restaurant Header Info Card
      item {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = themeCardBg),
          border = androidx.compose.foundation.BorderStroke(1.dp, SleekSubtleBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .padding(20.dp)
              .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            if (rest.logoUrl.isNotBlank()) {
              AsyncImage(
                model = rest.logoUrl,
                contentDescription = "Logo Restaurant",
                modifier = Modifier
                  .size(72.dp)
                  .clip(CircleShape)
                  .border(2.dp, themeAccent, CircleShape),
                contentScale = ContentScale.Crop
              )
            } else {
              Box(
                modifier = Modifier
                  .size(60.dp)
                  .clip(CircleShape)
                  .background(themeAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Outlined.RestaurantMenu, contentDescription = null, tint = themeAccent, modifier = Modifier.size(32.dp))
              }
            }

            Text(
              rest.name,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 22.sp,
              color = themeTextColor,
              textAlign = TextAlign.Center
            )

            if (rest.description.isNotBlank()) {
              Text(
                rest.description,
                fontSize = 13.sp,
                color = themeTextColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
              )
            }

            Surface(
              color = themeAccent.copy(alpha = 0.12f),
              shape = CircleShape
            ) {
              Text(
                "Carta Digital • ${menuItems.size} productos",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = themeAccent
              )
            }
          }
        }
      }

      // Search & Filters Section
      item {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar platos, ingredientes...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = themeAccent) },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { searchQuery = "" }) {
                  Icon(Icons.Default.Close, contentDescription = "Limpiar")
                }
              }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = themeCardBg,
              unfocusedContainerColor = themeCardBg,
              focusedBorderColor = themeAccent
            )
          )

          // Category Chips Row
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            item {
              FilterChip(
                selected = selectedCategoryId == null,
                onClick = { selectedCategoryId = null },
                label = { Text("Todos") },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = themeAccent,
                  selectedLabelColor = Color.White
                )
              )
            }
            items(categories) { cat ->
              FilterChip(
                selected = selectedCategoryId == cat.id,
                onClick = { selectedCategoryId = cat.id },
                label = { Text(cat.name) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = themeAccent,
                  selectedLabelColor = Color.White
                )
              )
            }
          }

          // Preference Filter Chips
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
              selected = filterVegetarian,
              onClick = { filterVegetarian = !filterVegetarian },
              label = { Text("Vegetariano 🌱") },
              shape = CircleShape
            )
            FilterChip(
              selected = filterGlutenFree,
              onClick = { filterGlutenFree = !filterGlutenFree },
              label = { Text("Sin Gluten 🌾") },
              shape = CircleShape
            )
          }
        }
      }

      // Items List
      if (filteredItems.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            contentAlignment = Alignment.Center
          ) {
            Text("No se encontraron productos en este apartado.", color = themeTextColor.copy(alpha = 0.6f))
          }
        }
      } else {
        items(filteredItems) { item ->
          PublicMenuItemCard(
            item = item,
            themeCardBg = themeCardBg,
            themeTextColor = themeTextColor,
            themeAccent = themeAccent
          )
        }
      }
    }
  }
}

@Composable
fun PublicMenuItemCard(
  item: MenuItem,
  themeCardBg: Color,
  themeTextColor: Color,
  themeAccent: Color
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = themeCardBg),
    border = androidx.compose.foundation.BorderStroke(1.dp, SleekSubtleBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (item.imageUrl.isNotBlank()) {
        AsyncImage(
          model = item.imageUrl,
          contentDescription = item.name,
          modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(14.dp)),
          contentScale = ContentScale.Crop
        )
      }

      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            item.name,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = themeTextColor,
            modifier = Modifier.weight(1f)
          )

          Text(
            "${"%.2f".format(item.price)} €",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = themeAccent
          )
        }

        if (item.description.isNotBlank()) {
          Text(
            item.description,
            fontSize = 12.sp,
            color = themeTextColor.copy(alpha = 0.7f),
            lineHeight = 16.sp
          )
        }

        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (!item.isAvailable) {
            Surface(
              color = SleekRedSoldOut.copy(alpha = 0.15f),
              shape = CircleShape
            ) {
              Text(
                "Agotado",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = SleekRedSoldOut
              )
            }
          } else {
            Surface(
              color = SleekGreenAvailable.copy(alpha = 0.15f),
              shape = CircleShape
            ) {
              Text(
                "Disponible",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = SleekGreenAvailable
              )
            }
          }

          if (item.isVegetarian) {
            Text("🌱", fontSize = 12.sp)
          }
          if (item.isGlutenFree) {
            Text("🌾", fontSize = 12.sp)
          }
          if (item.allergens.isNotBlank()) {
            Text("⚠️ ${item.allergens}", fontSize = 10.sp, color = themeTextColor.copy(alpha = 0.5f))
          }
        }
      }
    }
  }
}
