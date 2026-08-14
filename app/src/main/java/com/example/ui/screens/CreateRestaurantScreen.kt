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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Storefront
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Restaurant
import com.example.data.repository.MenuRepository
import com.example.ui.theme.*
import com.example.utils.QrGenerator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRestaurantScreen(
  repository: MenuRepository,
  onBackClick: () -> Unit,
  onCreated: (String) -> Unit // Navigates to Admin Panel with adminToken
) {
  var name by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var logoUrl by remember { mutableStateOf("") }
  var selectedTheme by remember { mutableStateOf("Moderno") }

  var isLoading by remember { mutableStateOf(false) }
  var createdRestaurant by remember { mutableStateOf<Restaurant?>(null) }

  val coroutineScope = rememberCoroutineScope()
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current

  val galleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    uri?.let { logoUrl = it.toString() }
  }

  Scaffold(
    containerColor = SleekBg,
    topBar = {
      TopAppBar(
        title = {
          Text(
            "Crear nueva carta",
            fontWeight = FontWeight.Bold,
            color = SleekTextPrimary
          )
        },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = SleekTextPrimary)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = SleekBg)
      )
    }
  ) { padding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
      ) {
        // Step Header Card
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = androidx.compose.foundation.BorderStroke(1.dp, SleekSubtleBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(SleekPurpleContainer),
                contentAlignment = Alignment.Center
              ) {
                Text("1", fontWeight = FontWeight.Bold, color = SleekPurple)
              }
              Spacer(Modifier.width(12.dp))
              Text(
                "Datos de tu negocio",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = SleekTextPrimary
              )
            }
            Text(
              "Ingresa el nombre y los datos básicos. Podrás cambiar esto en cualquier momento desde tu panel de administración.",
              fontSize = 13.sp,
              color = SleekTextSecondary
            )
          }
        }

        // Form Fields
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Nombre del Restaurante / Bar / Cafetería *") },
          placeholder = { Text("Ej: La Trattoria de María") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          leadingIcon = {
            Icon(Icons.Outlined.Storefront, contentDescription = null, tint = SleekPurple)
          }
        )

        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Descripción corta o eslogan (Opcional)") },
          placeholder = { Text("Ej: Cocina artesanal, pizzas al horno de leña") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          maxLines = 3
        )

        // Logo Picker Section
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
                  .size(56.dp)
                  .clip(CircleShape)
                  .border(2.dp, SleekPurple, CircleShape)
              )
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  "Logo seleccionado",
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
                  .size(48.dp)
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
                  fontSize = 15.sp,
                  color = SleekTextPrimary
                )
                Text(
                  "Selecciona una imagen de tu dispositivo (Opcional)",
                  fontSize = 12.sp,
                  color = SleekTextSecondary
                )
              }
            }
          }
        }

        // Theme Selector
        Text(
          "Selecciona el diseño visual de la carta:",
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = SleekTextPrimary
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          listOf("Moderno", "Clásico", "Elegante").forEach { themeName ->
            val isSelected = selectedTheme == themeName
            Surface(
              modifier = Modifier
                .weight(1f)
                .clickable { selectedTheme = themeName },
              shape = RoundedCornerShape(16.dp),
              color = if (isSelected) SleekPurpleContainer else Color.White,
              border = androidx.compose.foundation.BorderStroke(
                if (isSelected) 2.dp else 1.dp,
                if (isSelected) SleekPurple else SleekSubtleBorder
              )
            ) {
              Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Text(
                  text = themeName,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = if (isSelected) SleekOnPurpleContainer else SleekTextPrimary
                )
                if (isSelected) {
                  Icon(Icons.Default.Check, contentDescription = null, tint = SleekPurple, modifier = Modifier.size(16.dp))
                }
              }
            }
          }
        }

        Spacer(Modifier.height(12.dp))

        Button(
          onClick = {
            if (name.isBlank()) {
              Toast.makeText(context, "Por favor introduce un nombre", Toast.LENGTH_SHORT).show()
              return@Button
            }
            isLoading = true
            coroutineScope.launch {
              val newRest = repository.createRestaurant(
                name = name,
                description = description,
                logoUrl = logoUrl,
                themeName = selectedTheme
              )
              createdRestaurant = newRest
              isLoading = false
            }
          },
          enabled = !isLoading && name.isNotBlank(),
          modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
          shape = CircleShape,
          colors = ButtonDefaults.buttonColors(containerColor = SleekPurple)
        ) {
          if (isLoading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
          } else {
            Icon(Icons.Default.QrCode, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Crear Carta y Generar QR", fontSize = 16.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      // Dialog shown after creation
      createdRestaurant?.let { rest ->
        AlertDialog(
          onDismissRequest = {},
          shape = RoundedCornerShape(28.dp),
          containerColor = Color.White,
          title = {
            Text(
              "¡Carta Creada con Éxito!",
              fontWeight = FontWeight.ExtraBold,
              fontSize = 20.sp,
              color = SleekTextPrimary
            )
          },
          text = {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(16.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                "Guarda tu Token de Administración para poder editar tu carta desde cualquier dispositivo:",
                fontSize = 13.sp,
                color = SleekTextSecondary
              )

              Surface(
                shape = RoundedCornerShape(16.dp),
                color = SleekAccentPill,
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(14.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text("TU TOKEN DE ACCESO", fontSize = 10.sp, color = SleekPurple, fontWeight = FontWeight.Bold)
                    Text(rest.adminToken, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = SleekTextPrimary)
                  }
                  IconButton(onClick = {
                    clipboardManager.setText(AnnotatedString(rest.adminToken))
                    Toast.makeText(context, "Token copiado al portapapeles", Toast.LENGTH_SHORT).show()
                  }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copiar token", tint = SleekPurple)
                  }
                }
              }

              // Render generated QR Code
              val qrBitmap = remember(rest.id) {
                QrGenerator.generateQrBitmap("https://cartaqr.app/menu/${rest.id}", size = 400)
              }

              Image(
                bitmap = qrBitmap,
                contentDescription = "Código QR de la carta",
                modifier = Modifier
                  .size(180.dp)
                  .clip(RoundedCornerShape(16.dp))
                  .border(1.dp, SleekSubtleBorder, RoundedCornerShape(16.dp))
                  .padding(8.dp)
              )
            }
          },
          confirmButton = {
            Button(
              onClick = {
                onCreated(rest.adminToken)
              },
              shape = CircleShape,
              colors = ButtonDefaults.buttonColors(containerColor = SleekPurple),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("Ir al Panel de Administración", fontWeight = FontWeight.Bold)
            }
          }
        )
      }
    }
  }
}
