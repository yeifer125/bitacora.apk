package com.tubitacora.plantas.ui.navigation

import android.net.Uri
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tubitacora.plantas.data.local.entity.PlantEntity
import com.tubitacora.plantas.ui.IA.AiScreen
import com.tubitacora.plantas.ui.addplant.AddPlantScreen
import com.tubitacora.plantas.ui.expenses.ExpensesScreen
import com.tubitacora.plantas.ui.home.HomeScreen
import com.tubitacora.plantas.ui.plantdetail.PlantDetailScreen
import com.tubitacora.plantas.ui.plantlogs.PlantLogsScreen
import com.tubitacora.plantas.ui.settings.RiegoSettingsScreen
import com.tubitacora.plantas.ui.stats.StatsScreen
import com.tubitacora.plantas.ui.weather.WeatherScreen
import com.tubitacora.plantas.ui.splash.SplashScreenHacker
import com.tubitacora.plantas.viewmodel.LogViewModel
import com.tubitacora.plantas.viewmodel.PlantPhotoViewModel
import com.tubitacora.plantas.viewmodel.PlantViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavHost(
    plantViewModel: PlantViewModel
) {
    val navController = rememberNavController()

    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = NavRoutes.SPLASH // ✅ AHORA EMPIEZA EN EL SPLASH
        ) {
            composable(NavRoutes.SPLASH) {
                SplashScreenHacker(onTimeout = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true } // ✅ BORRA EL SPLASH DEL HISTORIAL
                    }
                })
            }
            
            homeNav(navController, plantViewModel, this@SharedTransitionLayout)
            addPlantNav(navController, plantViewModel)
            weatherNav(navController)
            plantDetailNav(navController, plantViewModel, this@SharedTransitionLayout)
            plantLogsNav(navController)
            statsNav(navController)
            riegoSettingsNav(navController, plantViewModel)
            aiScreenNav(navController)
            expensesNav(navController)
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
private fun NavGraphBuilder.homeNav(
    navController: NavController,
    plantViewModel: PlantViewModel,
    sharedTransitionScope: SharedTransitionScope
) {
    composable(NavRoutes.HOME) { 
        val plants = plantViewModel.plants.collectAsState(initial = emptyList()).value

        HomeScreen(
            plants = plants,
            animatedContentScope = this,
            sharedTransitionScope = sharedTransitionScope,
            onAddClick = { navController.navigate(NavRoutes.ADD_PLANT) },
            onPlantClick = { plantId, plantName, plantingDate, photoUri ->
                val encodedName = Uri.encode(plantName)
                var route = "${NavRoutes.PLANT_DETAIL}/$plantId/$encodedName/$plantingDate"
                if (photoUri != null) {
                    route += "?photoUri=${Uri.encode(photoUri)}"
                }
                navController.navigate(route)
            },
            onSettingsClick = { navController.navigate(NavRoutes.RIEGO_SETTINGS) },
            onWeatherClick = { navController.navigate(NavRoutes.WEATHER) },
            onDeletePlant = { plant -> plantViewModel.deletePlant(plant) },
            onWaterPlant = { plantId -> plantViewModel.addWateringLog(plantId) },
            onNavigateToStats = { plantId -> navController.navigate("stats/$plantId") },
            onNavigateToAi = { plant ->
                navController.currentBackStackEntry?.savedStateHandle?.set("plant", plant)
                navController.navigate(NavRoutes.AI_SCREEN)
            }
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
private fun NavGraphBuilder.plantDetailNav(navController: NavController, plantViewModel: PlantViewModel, sharedTransitionScope: SharedTransitionScope) {
    composable(
        route = "${NavRoutes.PLANT_DETAIL}/{plantId}/{plantName}/{plantingDate}?photoUri={photoUri}",
        arguments = listOf(
            navArgument("plantId") { type = NavType.LongType },
            navArgument("plantName") { type = NavType.StringType },
            navArgument("plantingDate") { type = NavType.LongType },
            navArgument("photoUri") { 
                type = NavType.StringType 
                nullable = true
            }
        )
    ) { backStackEntry ->
        val plantId = backStackEntry.arguments?.getLong("plantId") ?: return@composable
        val plantName = backStackEntry.arguments?.getString("plantName")?.let { Uri.decode(it) } ?: return@composable
        val plantingDate = backStackEntry.arguments?.getLong("plantingDate") ?: return@composable
        val photoUri = backStackEntry.arguments?.getString("photoUri")?.let { Uri.decode(it) }

        val logViewModel: LogViewModel = viewModel()
        val photoViewModel: PlantPhotoViewModel = viewModel()
        val plant = plantViewModel.plants.collectAsState(initial = emptyList()).value.find { it.id == plantId }

        PlantDetailScreen(
            plantId = plantId,
            plantName = plantName,
            plantingDate = plantingDate,
            initialPhotoUri = photoUri,
            logViewModel = logViewModel,
            photoViewModel = photoViewModel,
            animatedContentScope = this,
            sharedTransitionScope = sharedTransitionScope,
            onBack = { navController.popBackStack() },
            onNavigateToLogs = { val encodedName = Uri.encode(plantName); navController.navigate("${NavRoutes.PLANT_LOGS}/$plantId/$encodedName") },
            onNavigateToExpenses = { val encodedName = Uri.encode(plantName); navController.navigate("${NavRoutes.EXPENSES}/$plantId/$encodedName") },
            onNavigateToAi = { if (plant != null) { navController.currentBackStackEntry?.savedStateHandle?.set("plant", plant); navController.navigate(NavRoutes.AI_SCREEN) } }
        )
    }
}

private fun NavGraphBuilder.addPlantNav(navController: NavController, plantViewModel: PlantViewModel) {
    composable(NavRoutes.ADD_PLANT) {
        AddPlantScreen(
            onSave = { name, type, freq, date, notes ->
                plantViewModel.insertPlant(PlantEntity(
                    name = name, 
                    type = type, 
                    plantingDate = date, 
                    wateringFrequencyDays = freq, 
                    notes = notes
                ))
                navController.popBackStack()
            },
            onCancel = { navController.popBackStack() }
        )
    }
}

private fun NavGraphBuilder.weatherNav(navController: NavController) {
    composable(NavRoutes.WEATHER) { WeatherScreen(onBack = { navController.popBackStack() }) }
}

private fun NavGraphBuilder.plantLogsNav(navController: NavController) {
    composable(
        route = "${NavRoutes.PLANT_LOGS}/{plantId}/{plantName}",
        arguments = listOf(navArgument("plantId") { type = NavType.LongType }, navArgument("plantName") { type = NavType.StringType })
    ) { backStackEntry ->
        val plantId = backStackEntry.arguments?.getLong("plantId") ?: return@composable
        val plantName = backStackEntry.arguments?.getString("plantName")?.let { Uri.decode(it) } ?: return@composable
        PlantLogsScreen(plantId = plantId, plantName = plantName, logViewModel = viewModel(), onBack = { navController.popBackStack() })
    }
}

private fun NavGraphBuilder.statsNav(navController: NavController) {
    composable(
        route = "stats/{plantId}",
        arguments = listOf(navArgument("plantId") { type = NavType.LongType })
    ) { backStackEntry ->
        val plantId = backStackEntry.arguments?.getLong("plantId") ?: return@composable
        StatsScreen(plantId = plantId, onBack = { navController.popBackStack() })
    }
}

private fun NavGraphBuilder.riegoSettingsNav(navController: NavController, plantViewModel: PlantViewModel) {
    composable(NavRoutes.RIEGO_SETTINGS) {
        RiegoSettingsScreen(plantViewModel = plantViewModel, onBack = { navController.popBackStack() })
    }
}

private fun NavGraphBuilder.aiScreenNav(navController: NavController) {
    composable(NavRoutes.AI_SCREEN) {
        val plant = navController.previousBackStackEntry?.savedStateHandle?.get<PlantEntity>("plant")
        AiScreen(plant = plant)
    }
}

private fun NavGraphBuilder.expensesNav(navController: NavController) {
    composable(
        route = "${NavRoutes.EXPENSES}/{plantId}/{plantName}",
        arguments = listOf(navArgument("plantId") { type = NavType.LongType }, navArgument("plantName") { type = NavType.StringType })
    ) { backStackEntry ->
        val plantId = backStackEntry.arguments?.getLong("plantId") ?: return@composable
        val plantName = backStackEntry.arguments?.getString("plantName")?.let { Uri.decode(it) } ?: return@composable
        ExpensesScreen(plantId = plantId, plantName = plantName, onBack = { navController.popBackStack() })
    }
}
