package de.mietmoments.verwaltung.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import de.mietmoments.verwaltung.ui.components.ElegantMagicBackground
import de.mietmoments.verwaltung.ui.components.MiloCompanion
import de.mietmoments.verwaltung.ui.theme.MietMomentsTheme

private enum class Route(val value: String, val label: String) {
    Home("home", "Start"), Week("week", "Woche"), Customers("customers", "Kunden"), Items("items", "Artikel"), Locations("locations", "Locations")
}

@Composable
fun MietMomentsRoot(viewModel: AppViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MietMomentsTheme {
        ElegantMagicBackground(animationsEnabled = state.settings.animationsEnabled) {
            Box(Modifier.fillMaxSize()) {
                when {
                    state.loading -> Splash()
                    !state.configured -> SetupScreen(state.settings, state.syncing, viewModel::saveSetup)
                    else -> MainApp(state, viewModel)
                }
                if (!state.loading) {
                    MiloCompanion(
                        enabled = state.settings.momoEnabled,
                        animationsEnabled = state.settings.animationsEnabled,
                        online = state.online,
                        syncing = state.syncing,
                        eventCount = state.snapshot?.events.orEmpty().size
                    )
                }
            }
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
    val topLevel = Route.entries.any { it.value == current }
    val infinite = rememberInfiniteTransition(label = "sync")
    val spin by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (state.syncing && state.settings.animationsEnabled) 360f else 0f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
        label = "sync-spin"
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    AnimatedContent(
                        targetState = when (current) {
                            "settings" -> "Einstellungen"
                            "customer" -> "Kundendetails"
                            "week" -> "Wochenplan"
                            "customers" -> "Kunden"
                            "items" -> "Artikel"
                            "locations" -> "Locations"
                            else -> "MietMoments"
                        },
                        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                        label = "title"
                    ) { title ->
                        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            Icon(
                                Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(title, fontWeight = FontWeight.Black)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::sync, enabled = !state.syncing) {
                        if (state.syncing && !state.settings.animationsEnabled) CircularProgressIndicator()
                        else Icon(Icons.Rounded.Refresh, "Synchronisieren", Modifier.rotate(spin))
                    }
                    IconButton(onClick = { nav.navigate("settings") }) {
                        Icon(Icons.Rounded.Settings, "Einstellungen")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .82f),
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = .92f),
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            if (topLevel) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .86f),
                    tonalElevation = 0.dp
                ) {
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
                                    if (route == Route.Week && state.snapshot?.events.orEmpty().isNotEmpty()) {
                                        Badge { Text(state.snapshot?.events?.size.toString()) }
                                    }
                                }) { Icon(icon, route.label) }
                            },
                            label = { Text(route.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Route.Home.value,
            modifier = Modifier.padding(padding)
        ) {
            composable(Route.Home.value) {
                DashboardScreen(
                    snapshot = state.snapshot,
                    online = state.online,
                    momoEnabled = state.settings.momoEnabled,
                    animations = state.settings.animationsEnabled,
                    syncing = state.syncing,
                    onSync = viewModel::sync,
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
            composable(Route.Locations.value) {
                LocationsScreen(state.snapshot?.locations.orEmpty()) { openNavigation(context, it) }
            }
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
    Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        CircularProgressIndicator()
    }
}

private fun openNavigation(context: android.content.Context, address: String) {
    if (address.isBlank()) return
    val geo = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, geo)) }
}

private fun openUri(context: android.content.Context, uri: String) {
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri))) }
}
