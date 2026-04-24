package com.ext.kernelmanager.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ext.kernelmanager.presentation.screens.dashboard.DashboardScreen
import com.ext.kernelmanager.presentation.screens.technical.ui.TechnicalDashboardScreen
import com.ext.kernelmanager.presentation.screens.memory.ui.MemoryScreen
import com.ext.kernelmanager.presentation.screens.battery.ui.BatteryScreen
import com.ext.kernelmanager.presentation.screens.flasher.ui.FlasherScreen
import com.ext.kernelmanager.presentation.screens.logs.ui.LogsScreen
import com.ext.kernelmanager.presentation.screens.explorer.ui.ExplorerScreen
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Topology", Icons.Default.Home)
    object Technical : Screen("technical", "Instrumentation", Icons.Default.Info)
    object Memory : Screen("memory", "Memory", Icons.Default.List)
    object Battery : Screen("battery", "Power", Icons.Default.Favorite)
    object Flasher : Screen("flasher", "Flasher", Icons.Default.Build)
    object Logs : Screen("logs", "Live Logs", Icons.Default.Search)
    object Explorer : Screen("explorer", "Sysfs", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShell() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val items = listOf(
        Screen.Dashboard,
        Screen.Technical,
        Screen.Memory,
        Screen.Battery,
        Screen.Flasher,
        Screen.Logs,
        Screen.Explorer
    )
    var selectedItem by remember { mutableStateOf(items[0]) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "System Controller", 
                    modifier = Modifier.padding(24.dp), 
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                items.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.title) },
                        selected = item == selectedItem,
                        onClick = {
                            selectedItem = item
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(item.icon, contentDescription = null) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(selectedItem.title.uppercase(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = null)
                        }
                    }
                )
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(Screen.Dashboard.route) { DashboardScreen() }
                composable(Screen.Technical.route) { TechnicalDashboardScreen() }
                composable(Screen.Memory.route) { MemoryScreen() }
                composable(Screen.Battery.route) { BatteryScreen() }
                composable(Screen.Flasher.route) { FlasherScreen() }
                composable(Screen.Logs.route) { LogsScreen() }
                composable(Screen.Explorer.route) { ExplorerScreen() }
            }
        }
    }
}
