package de.mietmoments.verwaltung.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.mietmoments.verwaltung.ui.screens.CustomerDetailScreen
import de.mietmoments.verwaltung.ui.screens.CustomersScreen
import de.mietmoments.verwaltung.ui.screens.DashboardScreen
import de.mietmoments.verwaltung.ui.screens.ItemsScreen
import de.mietmoments.verwaltung.ui.screens.LocationsScreen
import de.mietmoments.verwaltung.ui.screens.SettingsScreen
import de.mietmoments.verwaltung.ui.screens.SetupScreen
import de.mietmoments.verwaltung.ui.screens.WeekScreen
import de.mietmoments.verwaltung.ui.theme.MietMomentsTheme

private enum class Route(val value: String, val label: String) {
    Home("home", "Start"), Week("week", "Woche"), Customers("customers", "Kunden"), Items("items", "Artikel"), Locations("locations", "Locations")
}

@Composable
fun MietMomentsRoot(viewModel: AppViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MietMomentsTheme {
        when {
            state.loading -> Splash()
            !state.configured -> SetupScreen(state.settings, state.syncing, viewModel::saveSetup)
            else -> MainApp(state, viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainApp(state: AppUiState, viewModel: AppViewModel) {
    val nav = rememberNavController()
    val backEntry by nav.currentBackStackEntryAsState()
    val current = backEntry?.destination?.route.orEmpty()
    val context = LocalContext.current
    val spin by animateFloatAsState(if (state.syncing && state.settings.animationsEnabled) 360f else 0f, label = "sync-spin")
    val topLevel = Route.entries.any { it.value == current }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (current) {
                            "settings" -> "Einstellungen"
                            "customer" -> "Kundendetails"
                            else -> "MietMoments"
                        },
                        fontWeight = FontWeight.Black
                    )
                },
                actions = {
                    IconButton(onClick = viewModel::sync, enabled = !state.syncing) {
                        if (state.syncing && !state.settings.animationsEnabled) CircularProgressIndicator()
                        else Icon(Icons.Rounded.Refresh, "Synchronisieren", Modifier.rotate(spin))
                    }
                    IconButton(onClick = { nav.navigate("settings") }) { Icon(Icons.Rounded.Settings, "Einstellungen") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            if (topLevel) BottomAppBar {
                Route.entries.forEach { route ->
                    NavigationBarItem(
                        selected = current == route.value,
                        onClick = {
                            nav.navigate(route.value) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            val icon = when (route) {
                                Route.Home -> Icons.Rounded.Home
                                Route.Week -> Icons.Rounded.CalendarMonth
                                Route.Customers -> Icons.Rounded.People
                                Route.Items -> Icons.Rounded.Inventory2
                                Route.Locations -> Icons.Rounded.Place
                            }
                            BadgedBox(badge = {
                                if (route == Route.Week && state.snapshot?.events.orEmpty().isNotEmpty()) Badge { Text(state.snapshot?.events?.size.toString()) }
                            }) { Icon(icon, route.label) }
                        },
                        label = { Text(route.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController = nav, startDestination = Route.Home.value, modifier = Modifier.padding(padding)) {
            composable(Route.Home.value) {
                DashboardScreen(
                    snapshot = state.snapshot,
                    online = state.online,
                    momoEnabled = state.settings.momoEnabled,
                    animations = state.settings.animationsEnabled,
                    onOpenWeek = { nav.navigate(Route.Week.value) },
                    onOpenCustomers = { nav.navigate(Route.Customers.value) },
                    onOpenItems = { nav.navigate(Route.Items.value) },
                    onOpenLocations = { nav.navigate(Route.Locations.value) },
                    onOpenCustomer = { cid, oid -> viewModel.openCustomer(cid, oid); nav.navigate("customer") }
                )
            }
            composable(Route.Week.value) {
                WeekScreen(
                    weekStart = state.weekStart,
                    events = state.snapshot?.events.orEmpty(),
                    momoEnabled = state.settings.momoEnabled,
                    animations = state.settings.animationsEnabled,
                    onPrev = { viewModel.moveWeek(-7) },
                    onToday = viewModel::currentWeek,
                    onNext = { viewModel.moveWeek(7) },
                    onOpenCustomer = { cid, oid -> viewModel.openCustomer(cid, oid); nav.navigate("customer") },
                    onNavigate = { address -> openNavigation(context, address) }
                )
            }
            composable(Route.Customers.value) {
                CustomersScreen(state.snapshot?.customers.orEmpty()) { cid, oid ->
                    viewModel.openCustomer(cid, oid)
                    nav.navigate("customer")
                }
            }
            composable(Route.Items.value) { ItemsScreen(state.snapshot?.items.orEmpty()) }
            composable(Route.Locations.value) { LocationsScreen(state.snapshot?.locations.orEmpty()) { openNavigation(context, it) } }
            composable("customer") {
                CustomerDetailScreen(
                    detail = state.customerDetail,
                    loading = state.customerLoading,
                    momoEnabled = state.settings.momoEnabled,
                    animations = state.settings.animationsEnabled,
                    onCall = { openUri(context, "tel:${Uri.encode(it)}") },
                    onMail = { openUri(context, "mailto:${Uri.encode(it)}") },
                    onNavigate = { openNavigation(context, it) }
                )
            }
            composable("settings") { SettingsScreen(state.settings, viewModel::updatePreferences) }
        }
    }
}

@Composable
private fun Splash() {
    Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) { CircularProgressIndicator() }
}

private fun openNavigation(context: android.content.Context, address: String) {
    if (address.isBlank()) return
    val geo = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
    val intent = Intent(Intent.ACTION_VIEW, geo)
    runCatching { context.startActivity(intent) }
}

private fun openUri(context: android.content.Context, uri: String) {
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri))) }
}
