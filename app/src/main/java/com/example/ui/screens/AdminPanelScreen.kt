package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.Category
import com.example.data.model.MenuItem
import com.example.data.model.Restaurant
import com.example.data.repository.MenuRepository
import com.example.ui.theme.*
import com.example.utils.QrGenerator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
  adminToken: String,
  repository: MenuRepository,
  onBackClick: () -> Unit,
  onPreviewMenu: (String) -> Unit
) {
  var restaurant by remember { mutableStateOf<Restaurant?>(null) }
  var isLoading by remember { mutableStateOf(true) }

  val coroutineScope = rememberCoroutineScope()
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current

  var selectedTab by remember { mutableIntStateOf(0) } // 0: Platos, 1: Categorías, 2: Ajustes & QR

  var showAddItemDialog by remember { mutableStateOf(false) }
  var showAddCategoryDialog by remember { mutableStateOf(false) }
  var showQrDialog by remember { mutableStateOf(false) }

  LaunchedEffect(adminToken) {
    isLoading = true
    restaurant = repository.getRestaurantByAdminToken(adminToken)
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
    Scaffold(
      containerColor = SleekBg,
      topBar = {
        TopAppBar(
          title = { Text("Administración") },
          navigationIcon = {
            IconButton(onClick = onBackClick) {
              Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
          }
        )
      }
    ) { padding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Icon(Icons.Outlined.Error, contentDescription = null, tint = SleekRedSoldOut, modifier = Modifier.size(48.dp))
          Text("Token inválido o carta no encontrada", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
          Button(onClick = onBackClick, colors = ButtonDefaults.buttonColors(containerColor = SleekPurple)) {
            Text("Volver al inicio")
          }
        }
      }
    }
    return
  }

  val categories by repository.getCategories(rest.id).collectAsStateWithLifecycle(initialValue = emptyList())
  val menuItems by repository.getMenuItems(rest.id).collectAsStateWithLifecycle(initialValue = emptyList())

  Scaffold(
    containerColor = SleekBg,
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(rest.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Panel Admin • Token: ${rest.adminToken}", fontSize = 12.sp, color = SleekPurple)
          }
        },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = SleekTextPrimary)
          }
        },
        actions = {
          IconButton(onClick = { showQrDialog = true }) {
            Icon(Icons.Outlined.QrCode, contentDescription = "Ver Código QR", tint = SleekPurple)
          }
          IconButton(onClick = { onPreviewMenu(rest.id) }) {
            Icon(Icons.Outlined.Visibility, contentDescription = "Ver como cliente", tint = SleekPurple)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = SleekBg)
      )
    },
    floatingActionButton = {
      if (selectedTab == 0) {
        FloatingActionButton(
          onClick = {
            if (categories.isEmpty()) {
              Toast.makeText(context, "Crea primero una categoría", Toast.LENGTH_SHORT).show()
              selectedTab = 1
            } else {
              showAddItemDialog = true
            }
          },
          containerColor = SleekPurple,
          contentColor = Color.White,
          shape = CircleShape
        ) {
          Icon(Icons.Default.Add, contentDescription = "Añadir Plato")
        }
      } else if (selectedTab == 1) {
        FloatingActionButton(
          onClick = { showAddCategoryDialog = true },
          containerColor = SleekPurple,
          contentColor = Color.White,
          shape = CircleShape
        ) {
          Icon(Icons.Default.Add, contentDescription = "Añadir Categoría")
        }
      }
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      // Tab Row
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = SleekBg,
        contentColor = SleekPurple,
        divider = { HorizontalDivider(color = SleekSubtleBorder) }
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = { Text("Platos (${menuItems.size})", fontWeight = FontWeight.SemiBold) }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("Categorías (${categories.size})", fontWeight = FontWeight.SemiBold) }
        )
        Tab(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          text = { Text("Ajustes y QR", fontWeight = FontWeight.SemiBold) }
        )
      }

      when (selectedTab) {
        0 -> ItemsTabContent(
          categories = categories,
          items = menuItems,
          onToggleAvailable = { id, isAvail ->
            coroutineScope.launch { repository.toggleMenuItemAvailability(id, isAvail) }
          },
          onDeleteItem = { id ->
            coroutineScope.launch { repository.deleteMenuItem(id) }
          }
        )
        1 -> CategoriesTabContent(
          categories = categories,
          onDeleteCategory = { id ->
            coroutineScope.launch { repository.deleteCategory(id) }
          }
        )
        2 -> SettingsTabContent(
          restaurant = rest,
          onUpdateRestaurant = { updated ->
            coroutineScope.launch {
              repository.updateRestaurant(updated)
              restaurant = updated
              Toast.makeText(context, "Ajustes guardados", Toast.LENGTH_SHORT).show()
            }
          },
          onShowQr = { showQrDialog = true }
        )
      }
    }

    // Add Item Dialog
    if (showAddItemDialog) {
      AddItemDialog(
        categories = categories,
        restaurantId = rest.id,
        onDismiss = { showAddItemDialog = false },
        onSave = { newItem ->
          coroutineScope.launch {
            repository.saveMenuItem(newItem)
            showAddItemDialog = false
          }
        }
      )
    }

    // Add Category Dialog
    if (showAddCategoryDialog) {
      AddCategoryDialog(
        onDismiss = { showAddCategoryDialog = false },
        onSave = { catName ->
          coroutineScope.launch {
            repository.addCategory(rest.id, catName)
            showAddCategoryDialog = false
          }
        }
      )
    }

    // QR Dialog
    if (showQrDialog) {
      AlertDialog(
        onDismissRequest = { showQrDialog = false },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White,
        title = {
          Text("Código QR de tu Carta", fontWeight = FontWeight.Bold)
        },
        text = {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              "Imprime o comparte este código QR para que tus clientes puedan ver el menú digital:",
              fontSize = 13.sp,
              color = SleekTextSecondary
            )

            val qrBitmap = remember(rest.id) {
              QrGenerator.generateQrBitmap("https://cartaqr.app/menu/${rest.id}", size = 512)
            }

            Image(
              bitmap = qrBitmap,
              contentDescription = "Código QR",
              modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, SleekSubtleBorder, RoundedCornerShape(16.dp))
                .padding(8.dp)
            )

            OutlinedButton(
              onClick = {
                clipboardManager.setText(AnnotatedString("https://cartaqr.app/menu/${rest.id}"))
                Toast.makeText(context, "Enlace del menú copiado", Toast.LENGTH_SHORT).show()
              },
              shape = CircleShape,
              colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekPurple)
            ) {
              Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(8.dp))
              Text("Copiar Enlace de Carta")
            }
          }
        },
        confirmButton = {
          TextButton(onClick = { showQrDialog = false }) {
            Text("Cerrar", fontWeight = FontWeight.Bold, color = SleekPurple)
          }
        }
      )
    }
  }
}

@Composable
fun ItemsTabContent(
  categories: List<Category>,
  items: List<MenuItem>,
  onToggleAvailable: (String, Boolean) -> Unit,
  onDeleteItem: (String) -> Unit
) {
  if (items.isEmpty()) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(32.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Icon(Icons.Outlined.Restaurant, contentDescription = null, tint = SleekPurple, modifier = Modifier.size(48.dp))
        Text("Tu carta no tiene platos aún", fontWeight = FontWeight.Bold, color = SleekTextPrimary)
        Text("Toca en el botón '+' para añadir tu primer plato o bebida.", fontSize = 13.sp, color = SleekTextSecondary)
      }
    }
  } else {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      contentPadding = PaddingValues(bottom = 80.dp)
    ) {
      items(items) { item ->
        val catName = categories.find { it.id == item.categoryId }?.name ?: "General"
        AdminMenuItemCard(
          item = item,
          categoryName = catName,
          onToggleAvailable = { onToggleAvailable(item.id, it) },
          onDelete = { onDeleteItem(item.id) }
        )
      }
    }
  }
}

@Composable
fun AdminMenuItemCard(
  item: MenuItem,
  categoryName: String,
  onToggleAvailable: (Boolean) -> Unit,
  onDelete: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = androidx.compose.foundation.BorderStroke(1.dp, SleekSubtleBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Surface(
          color = SleekAccentPill,
          shape = CircleShape
        ) {
          Text(categoryName, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SleekPurple)
        }

        Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SleekTextPrimary)

        if (item.description.isNotBlank()) {
          Text(item.description, fontSize = 12.sp, color = SleekTextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }

        Text("${"%.2f".format(item.price)} €", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = SleekPurple)
      }

      Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            if (item.isAvailable) "Disponible" else "Agotado",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (item.isAvailable) SleekGreenAvailable else SleekRedSoldOut
          )
          Spacer(Modifier.width(4.dp))
          Switch(
            checked = item.isAvailable,
            onCheckedChange = onToggleAvailable,
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = SleekGreenAvailable
            )
          )
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
          Icon(Icons.Outlined.Delete, contentDescription = "Eliminar plato", tint = SleekRedSoldOut, modifier = Modifier.size(18.dp))
        }
      }
    }
  }
}

@Composable
fun CategoriesTabContent(
  categories: List<Category>,
  onDeleteCategory: (String) -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    contentPadding = PaddingValues(bottom = 80.dp)
  ) {
    items(categories) { cat ->
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, SleekSubtleBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(SleekPurpleContainer),
              contentAlignment = Alignment.Center
            ) {
              Text("${cat.displayOrder}", fontWeight = FontWeight.Bold, color = SleekPurple)
            }
            Text(cat.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SleekTextPrimary)
          }

          IconButton(onClick = { onDeleteCategory(cat.id) }) {
            Icon(Icons.Outlined.Delete, contentDescription = "Eliminar", tint = SleekRedSoldOut)
          }
        }
      }
    }
  }
}

@Composable
fun SettingsTabContent(
  restaurant: Restaurant,
  onUpdateRestaurant: (Restaurant) -> Unit,
  onShowQr: () -> Unit
) {
  var name by remember(restaurant) { mutableStateOf(restaurant.name) }
  var description by remember(restaurant) { mutableStateOf(restaurant.description) }
  var logoUrl by remember(restaurant) { mutableStateOf(restaurant.logoUrl) }
  var selectedTheme by remember(restaurant) { mutableStateOf(restaurant.themeName) }

  val clipboardManager = LocalClipboardManager.current
  val context = LocalContext.current

  val galleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    uri?.let { logoUrl = it.toString() }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = SleekPurpleContainer),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text("TOKEN ADMINISTRADOR", fontSize = 10.sp, color = SleekPurple, fontWeight = FontWeight.Bold)
          Text(restaurant.adminToken, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = SleekOnPurpleContainer)
        }
        IconButton(onClick = {
          clipboardManager.setText(AnnotatedString(restaurant.adminToken))
          Toast.makeText(context, "Token copiado al portapapeles", Toast.LENGTH_SHORT).show()
        }) {
          Icon(Icons.Default.ContentCopy, contentDescription = null, tint = SleekPurple)
        }
      }
    }

    OutlinedTextField(
      value = name,
      onValueChange = { name = it },
      label = { Text("Nombre del Restaurante") },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp)
    )

    OutlinedTextField(
      value = description,
      onValueChange = { description = it },
      label = { Text("Descripción") },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp)
    )

    // Logo Selection Card
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      border = androidx.compose.foundation.BorderStroke(1.dp, SleekSubtleBorder),
      modifier = Modifier
        .fillMaxWidth()
        .clickable { galleryLauncher.launch("image/*") }
    ) {
      Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        if (logoUrl.isNotBlank()) {
          AsyncImage(
            model = logoUrl,
            contentDescription = "Logo del restaurante",
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .size(52.dp)
              .clip(CircleShape)
              .border(2.dp, SleekPurple, CircleShape)
          )
          Column(modifier = Modifier.weight(1f)) {
            Text(
              "Logo actual",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = SleekTextPrimary
            )
            Text(
              "Toca para cambiar de la galería",
              fontSize = 12.sp,
              color = SleekTextSecondary
            )
          }
          IconButton(onClick = { logoUrl = "" }) {
            Icon(
              Icons.Outlined.Delete,
              contentDescription = "Eliminar logo",
              tint = SleekRedSoldOut
            )
          }
        } else {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(SleekPurpleContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              Icons.Outlined.AddPhotoAlternate,
              contentDescription = null,
              tint = SleekPurple
            )
          }
          Column(modifier = Modifier.weight(1f)) {
            Text(
              "Subir Logo de la Galería",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = SleekTextPrimary
            )
            Text(
              "Selecciona una foto desde tu teléfono",
              fontSize = 12.sp,
              color = SleekTextSecondary
            )
          }
        }
      }
    }

    Text("Diseño Visual de la Carta:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      listOf("Moderno", "Clásico", "Elegante").forEach { themeName ->
        FilterChip(
          selected = selectedTheme == themeName,
          onClick = { selectedTheme = themeName },
          label = { Text(themeName) },
          shape = CircleShape
        )
      }
    }

    Button(
      onClick = {
        onUpdateRestaurant(
          restaurant.copy(
            name = name,
            description = description,
            logoUrl = logoUrl,
            themeName = selectedTheme
          )
        )
      },
      shape = CircleShape,
      colors = ButtonDefaults.buttonColors(containerColor = SleekPurple),
      modifier = Modifier.fillMaxWidth()
    ) {
      Text("Guardar Cambios")
    }

    OutlinedButton(
      onClick = onShowQr,
      shape = CircleShape,
      modifier = Modifier.fillMaxWidth()
    ) {
      Icon(Icons.Outlined.QrCode, contentDescription = null)
      Spacer(Modifier.width(8.dp))
      Text("Ver Código QR Grande")
    }
  }
}

@Composable
fun AddItemDialog(
  categories: List<Category>,
  restaurantId: String,
  onDismiss: () -> Unit,
  onSave: (MenuItem) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var priceText by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf(categories.firstOrNull()?.id ?: "") }
  var imageUrl by remember { mutableStateOf("") }
  var isVeg by remember { mutableStateOf(false) }
  var isGlutenFree by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = Color.White,
    title = { Text("Añadir Nuevo Plato", fontWeight = FontWeight.Bold) },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Nombre del Plato *") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
          value = priceText,
          onValueChange = { priceText = it },
          label = { Text("Precio (€) *") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Descripción o ingredientes") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        Text("Categoría:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          categories.forEach { cat ->
            FilterChip(
              selected = selectedCategory == cat.id,
              onClick = { selectedCategory = cat.id },
              label = { Text(cat.name, fontSize = 11.sp) },
              shape = CircleShape
            )
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          FilterChip(
            selected = isVeg,
            onClick = { isVeg = !isVeg },
            label = { Text("Vegetariano 🌱") },
            shape = CircleShape
          )
          FilterChip(
            selected = isGlutenFree,
            onClick = { isGlutenFree = !isGlutenFree },
            label = { Text("Sin Gluten 🌾") },
            shape = CircleShape
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val price = priceText.toDoubleOrNull() ?: 0.0
          if (name.isNotBlank() && selectedCategory.isNotBlank()) {
            val newItem = MenuItem(
              id = "item_${java.util.UUID.randomUUID().toString().take(6)}",
              restaurantId = restaurantId,
              categoryId = selectedCategory,
              name = name,
              description = description,
              price = price,
              imageUrl = imageUrl,
              isAvailable = true,
              isVegetarian = isVeg,
              isGlutenFree = isGlutenFree
            )
            onSave(newItem)
          }
        },
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = SleekPurple)
      ) {
        Text("Guardar Plato")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancelar")
      }
    }
  )
}

@Composable
fun AddCategoryDialog(
  onDismiss: () -> Unit,
  onSave: (String) -> Unit
) {
  var name by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(24.dp),
    containerColor = Color.White,
    title = { Text("Nueva Categoría", fontWeight = FontWeight.Bold) },
    text = {
      OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Nombre de la Categoría") },
        placeholder = { Text("Ej: Entrantes, Postres, Vinos") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
      )
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            onSave(name)
          }
        },
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = SleekPurple)
      ) {
        Text("Añadir")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancelar")
      }
    }
  )
}
