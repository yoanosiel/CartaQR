package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.SupabaseConfig
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupabaseConfigScreen(
  onBackClick: () -> Unit
) {
  val context = LocalContext.current
  var url by remember { mutableStateOf(SupabaseConfig.getUrl(context)) }
  var anonKey by remember { mutableStateOf(SupabaseConfig.getKey(context)) }

  Scaffold(
    containerColor = SleekBg,
    topBar = {
      TopAppBar(
        title = { Text("Configuración Supabase", fontWeight = FontWeight.Bold) },
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
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(SleekAccentBlue),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Outlined.Storage, contentDescription = null, tint = SleekAccentBlueText)
        }
        Column {
          Text("Base de Datos Cloud Supabase", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SleekTextPrimary)
          Text("Opcional • Permite sincronizar tu carta online", fontSize = 12.sp, color = SleekTextSecondary)
        }
      }

      Text(
        "Si posees un proyecto en Supabase, introduce tus credenciales aquí para conectar CartaQR sin necesidad de cuentas de usuario ni registro.",
        fontSize = 13.sp,
        color = SleekTextSecondary
      )

      OutlinedTextField(
        value = url,
        onValueChange = { url = it },
        label = { Text("URL de Supabase Project") },
        placeholder = { Text("https://xyzcompany.supabase.co") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
      )

      OutlinedTextField(
        value = anonKey,
        onValueChange = { anonKey = it },
        label = { Text("Supabase Anon Key") },
        placeholder = { Text("eyJhbGciOiJIUzI1NiIsInR5cCI6...") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        maxLines = 4
      )

      Button(
        onClick = {
          SupabaseConfig.saveConfig(context, url, anonKey)
          Toast.makeText(context, "Configuración de Supabase guardada correctamente", Toast.LENGTH_SHORT).show()
          onBackClick()
        },
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = SleekPurple),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
      ) {
        Icon(Icons.Default.Check, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Guardar Configuración", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      }
    }
  }
}
