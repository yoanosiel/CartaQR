package com.example.ui.screens

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Restaurant
import com.example.data.repository.MenuRepository
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  repository: MenuRepository,
  onCreateClick: () -> Unit,
  onAdminClick: () -> Unit,
  onViewMenuClick: (String) -> Unit,
  onAdminMenuClick: (String) -> Unit
) {
  val restaurants by repository.allRestaurants.collectAsStateWithLifecycle(initialValue = emptyList())

  // Automatic Cloud Sync state animation
  var isCloudSyncing by remember { mutableStateOf(false) }

  // Periodic automatic sync pulse simulation
  LaunchedEffect(Unit) {
    while (true) {
      delay(4000)
      isCloudSyncing = true
      delay(1500)
      isCloudSyncing = false
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
  val rotationAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "rotation"
  )

  Scaffold(
    containerColor = SleekBg,
    topBar = {
      TopAppBar(
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SleekPurple),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.QrCode2,
                contentDescription = "CartaQR Logo",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
              )
            }
            Text(
              text = "CartaQR",
              fontWeight = FontWeight.Bold,
              fontSize = 22.sp,
              color = SleekTextPrimary
            )
          }
        },
        actions = {
          // Cloud sync status view in the top-right corner
          Surface(
            shape = CircleShape,
            color = SleekPurpleContainer.copy(alpha = 0.6f),
            modifier = Modifier.padding(end = 12.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              if (isCloudSyncing) {
                Icon(
                  imageVector = Icons.Outlined.Sync,
                  contentDescription = "Sincronizando...",
                  tint = SleekPurple,
                  modifier = Modifier
                    .size(16.dp)
                    .rotate(rotationAngle)
                )
                Text(
                  text = "Sincronizando...",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = SleekPurple
                )
              } else {
                Icon(
                  imageVector = Icons.Outlined.CloudDone,
                  contentDescription = "Nube Sincronizada",
                  tint = SleekGreenAvailable,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = "Nube ok",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = SleekTextSecondary
                )
              }
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = SleekBg)
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      contentPadding = PaddingValues(bottom = 32.dp)
    ) {
      // Hero Banner Card
      item {
        Card(
          shape = RoundedCornerShape(28.dp),
          colors = CardDefaults.cardColors(containerColor = SleekPurpleContainer),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Text(
              text = "Crea y administra la carta digital de tu negocio",
              fontSize = 24.sp,
              fontWeight = FontWeight.ExtraBold,
              lineHeight = 30.sp,
              color = SleekOnPurpleContainer
            )

            Text(
              text = "Genera un código QR listo para imprimir. Tus clientes escanean y ven tu menú al instante.",
              fontSize = 14.sp,
              color = SleekTextSecondary,
              lineHeight = 20.sp
            )

            Button(
              onClick = onCreateClick,
              shape = CircleShape,
              colors = ButtonDefaults.buttonColors(containerColor = SleekPurple),
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(Modifier.width(8.dp))
              Text("Crear nueva carta", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
          }
        }
      }

      // Quick Action Option Card
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onAdminClick() },
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = androidx.compose.foundation.BorderStroke(1.dp, SleekSubtleBorder)
        ) {
          Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(SleekAccentPill),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Outlined.VpnKey, contentDescription = null, tint = SleekPurple)
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Tengo un código / Token",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = SleekTextPrimary
              )
              Text(
                text = "Accede a editar tu carta desde cualquier dispositivo",
                fontSize = 12.sp,
                color = SleekTextSecondary
              )
            }
            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = null,
              tint = SleekPurple
            )
          }
        }
      }

      // Recent Local Menus Header
      item {
        Spacer(Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Mis Cartas Creadas (${restaurants.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = SleekTextPrimary
          )
        }
      }

      if (restaurants.isEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, SleekSubtleBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.RestaurantMenu,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = SleekPurple.copy(alpha = 0.6f)
              )
              Text(
                text = "No tienes ninguna carta guardada",
                fontWeight = FontWeight.SemiBold,
                color = SleekTextPrimary
              )
              Text(
                text = "Toca en 'Crear nueva carta' para empezar a personalizar el menú de tu restaurante.",
                fontSize = 13.sp,
                color = SleekTextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp)
              )
            }
          }
        }
      } else {
        items(restaurants) { restaurant ->
          RestaurantItemCard(
            restaurant = restaurant,
            onViewMenu = { onViewMenuClick(restaurant.id) },
            onAdminMenu = { onAdminMenuClick(restaurant.adminToken) }
          )
        }
      }
    }
  }
}

@Composable
fun RestaurantItemCard(
  restaurant: Restaurant,
  onViewMenu: () -> Unit,
  onAdminMenu: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = androidx.compose.foundation.BorderStroke(1.dp, SleekSubtleBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = restaurant.name,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = SleekTextPrimary
          )
          if (restaurant.description.isNotBlank()) {
            Text(
              text = restaurant.description,
              fontSize = 13.sp,
              color = SleekTextSecondary,
              maxLines = 1
            )
          }
        }

        Surface(
          color = SleekAccentPill,
          shape = CircleShape
        ) {
          Text(
            text = "Token: ${restaurant.adminToken}",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = SleekPurple
          )
        }
      }

      HorizontalDivider(color = SleekSubtleBorder)

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedButton(
          onClick = onViewMenu,
          modifier = Modifier.weight(1f),
          shape = CircleShape,
          colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekPurple)
        ) {
          Icon(Icons.Outlined.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(6.dp))
          Text("Ver como cliente", fontSize = 13.sp)
        }

        Button(
          onClick = onAdminMenu,
          modifier = Modifier.weight(1f),
          shape = CircleShape,
          colors = ButtonDefaults.buttonColors(containerColor = SleekPurple)
        ) {
          Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(6.dp))
          Text("Administrar", fontSize = 13.sp)
        }
      }
    }
  }
}
