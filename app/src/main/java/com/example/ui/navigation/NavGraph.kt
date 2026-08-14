package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.repository.MenuRepository
import com.example.ui.screens.*

object NavRoutes {
  const val HOME = "home"
  const val CREATE_RESTAURANT = "create_restaurant"
  const val ADMIN_PANEL = "admin_panel/{adminToken}"
  const val PUBLIC_MENU = "public_menu/{restaurantId}"
  const val TOKEN_ACCESS = "token_access"
  const val SUPABASE_CONFIG = "supabase_config"

  fun adminPanelRoute(token: String) = "admin_panel/$token"
  fun publicMenuRoute(restaurantId: String) = "public_menu/$restaurantId"
}

@Composable
fun CartaQRNavGraph(
  navController: NavHostController = rememberNavController(),
  repository: MenuRepository = MenuRepository(LocalContext.current)
) {
  NavHost(
    navController = navController,
    startDestination = NavRoutes.HOME
  ) {
    composable(NavRoutes.HOME) {
      HomeScreen(
        repository = repository,
        onCreateClick = { navController.navigate(NavRoutes.CREATE_RESTAURANT) },
        onAdminClick = { navController.navigate(NavRoutes.TOKEN_ACCESS) },
        onViewMenuClick = { restId -> navController.navigate(NavRoutes.publicMenuRoute(restId)) },
        onAdminMenuClick = { adminToken -> navController.navigate(NavRoutes.adminPanelRoute(adminToken)) }
      )
    }

    composable(NavRoutes.CREATE_RESTAURANT) {
      CreateRestaurantScreen(
        repository = repository,
        onBackClick = { navController.popBackStack() },
        onCreated = { adminToken ->
          navController.navigate(NavRoutes.adminPanelRoute(adminToken)) {
            popUpTo(NavRoutes.HOME)
          }
        }
      )
    }

    composable(
      route = NavRoutes.ADMIN_PANEL,
      arguments = listOf(navArgument("adminToken") { type = NavType.StringType })
    ) { backStackEntry ->
      val token = backStackEntry.arguments?.getString("adminToken") ?: ""
      AdminPanelScreen(
        adminToken = token,
        repository = repository,
        onBackClick = { navController.popBackStack() },
        onPreviewMenu = { restId -> navController.navigate(NavRoutes.publicMenuRoute(restId)) }
      )
    }

    composable(
      route = NavRoutes.PUBLIC_MENU,
      arguments = listOf(navArgument("restaurantId") { type = NavType.StringType })
    ) { backStackEntry ->
      val restId = backStackEntry.arguments?.getString("restaurantId") ?: ""
      PublicMenuScreen(
        restaurantId = restId,
        repository = repository,
        onBackClick = { navController.popBackStack() }
      )
    }

    composable(NavRoutes.TOKEN_ACCESS) {
      TokenAccessScreen(
        repository = repository,
        onBackClick = { navController.popBackStack() },
        onAdminFound = { adminToken ->
          navController.navigate(NavRoutes.adminPanelRoute(adminToken)) {
            popUpTo(NavRoutes.HOME)
          }
        }
      )
    }

    composable(NavRoutes.SUPABASE_CONFIG) {
      SupabaseConfigScreen(
        onBackClick = { navController.popBackStack() }
      )
    }
  }
}
