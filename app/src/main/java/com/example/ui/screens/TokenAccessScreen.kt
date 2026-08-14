package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.MenuRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenAccessScreen(
  repository: MenuRepository,
  onBackClick: () -> Unit,
  onAdminFound: (String) -> Unit
) {
  var tokenInput by remember { mutableStateOf("") }
  var isLoading by remember { mutableStateOf(false) }

  val coroutineScope = rememberCoroutineScope()
  val context = LocalContext.current

  Scaffold(
    containerColor = SleekBg,
    topBar = {
      TopAppBar(
        title = { Text("Acceso con Token", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = SleekBg)
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(24.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .size(64.dp)
          .clip(CircleShape)
          .background(SleekPurpleContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Default.VpnKey, contentDescription = null, tint = SleekPurple, modifier = Modifier.size(32.dp))
      }

      Text(
        "Introduce tu Token de Administración",
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        color = SleekTextPrimary
      )

      Text(
        "Ingresa el código alfanumérico generado al crear tu carta (ejemplo: ADM-A1B2C3) para administrar tu menú.",
        fontSize = 13.sp,
        color = SleekTextSecondary,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
      )

      OutlinedTextField(
        value = tokenInput,
        onValueChange = { tokenInput = it.uppercase() },
        label = { Text("Token de Administración") },
        placeholder = { Text("ADM-XXXXXX") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        leadingIcon = {
          Icon(Icons.Default.VpnKey, contentDescription = null, tint = SleekPurple)
        }
      )

      Button(
        onClick = {
          val cleanToken = tokenInput.trim()
          if (cleanToken.isBlank()) {
            Toast.makeText(context, "Ingresa un token válido", Toast.LENGTH_SHORT).show()
            return@Button
          }
          isLoading = true
          coroutineScope.launch {
            val rest = repository.getRestaurantByAdminToken(cleanToken)
            isLoading = false
            if (rest != null) {
              onAdminFound(cleanToken)
            } else {
              Toast.makeText(context, "No se encontró ninguna carta con ese token", Toast.LENGTH_LONG).show()
            }
          }
        },
        enabled = !isLoading && tokenInput.isNotBlank(),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = SleekPurple),
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
      ) {
        if (isLoading) {
          CircularProgressIndicator(color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(24.dp))
        } else {
          Text("Acceder al Panel Admin", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
      }
    }
  }
}
