package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.config.AppConfigRegistry
import com.example.config.AppTabId
import com.example.ui.components.InAppUpdateDialog
import com.example.ui.screens.DuaScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.QuranPlaceholderScreen
import com.example.ui.screens.TasbihScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.theme.AppBackground
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.IslamicTeal
import com.example.ui.theme.SoftMintSurface
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UmmahDailyTheme
import com.example.ui.viewmodel.AppUpdateViewModel
import com.example.ui.viewmodel.DuaViewModel
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.TasbihViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            UmmahDailyTheme(darkTheme = false) {
                UmmahDailyAppShell()
            }
        }
    }
}

@Composable
fun UmmahDailyAppShell() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val tabs = AppConfigRegistry.bottomTabs

    val homeViewModel: HomeViewModel = viewModel()
    val duaViewModel: DuaViewModel = viewModel()
    val tasbihViewModel: TasbihViewModel = viewModel()
    val appUpdateViewModel: AppUpdateViewModel = viewModel()
    val availableUpdate by appUpdateViewModel.availableUpdate.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentAlignment = Alignment.TopCenter
    ) {
        Scaffold(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxSize(),
            containerColor = AppBackground,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                Surface(
                    shadowElevation = 12.dp,
                    color = CleanWhite,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NavigationBar(
                        containerColor = CleanWhite,
                        tonalElevation = 0.dp,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        tabs.forEach { tab ->
                            val selected = currentDestination?.hierarchy?.any {
                                it.route == tab.id.route
                            } == true

                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(tab.id.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.labelBn
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.labelBn,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = IslamicTeal,
                                    selectedTextColor = IslamicTeal,
                                    indicatorColor = SoftMintSurface,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("bottom_tab_${tab.id.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = tabs.first().id.route
            ) {
                composable(AppTabId.HOME.route) {
                    HomeScreen(
                        viewModel = homeViewModel,
                        contentBottomPadding = innerPadding,
                        onNavigateToTab = { targetTab ->
                            navController.navigate(targetTab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable(AppTabId.QURAN.route) {
                    QuranPlaceholderScreen(
                        contentBottomPadding = innerPadding
                    )
                }
                composable(AppTabId.DUA.route) {
                    DuaScreen(
                        viewModel = duaViewModel,
                        contentBottomPadding = innerPadding
                    )
                }
                composable(AppTabId.TASBIH.route) {
                    TasbihScreen(
                        viewModel = tasbihViewModel,
                        contentBottomPadding = innerPadding
                    )
                }
                composable(AppTabId.TOOLS.route) {
                    val selectedCityApi by homeViewModel.selectedCity.collectAsStateWithLifecycle()
                    val selectedCityNameBn = AppConfigRegistry.bangladeshCities
                        .firstOrNull { it.apiCity == selectedCityApi }
                        ?.nameBn
                        ?: "ঢাকা"
                    ToolsScreen(
                        contentBottomPadding = innerPadding,
                        selectedCityApi = selectedCityApi,
                        selectedCityNameBn = selectedCityNameBn,
                        onNavigateToTab = { targetTab ->
                            navController.navigate(targetTab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }

        availableUpdate?.let { updateInfo ->
            InAppUpdateDialog(
                updateInfo = updateInfo,
                onDismissOptional = { appUpdateViewModel.dismissOptionalUpdate() }
            )
        }
    }
}
