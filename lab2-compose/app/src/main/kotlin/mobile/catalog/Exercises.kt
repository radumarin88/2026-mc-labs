package mobile.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

/*
 * Lab 2 exercises. One task in two movements. First you learn the fundamentals of Compose on small
 * pieces (a row, a stateful button, a hoisted control); then you build a real multi-screen app: a
 * searchable, filterable catalog with a detail screen you navigate to. Exercises 1 to 3 are the
 * fundamentals; 4 and 5 are list pieces; 6 assembles the list screen; 7 and 8 are the detail screen
 * and the navigation graph. This is the shape of the app your team will build for the project.
 */

/**
 * Exercise 1. Your first composable: one row of the list.
 * A composable is just a function marked @Composable that describes UI by calling other composables.
 * Show [item].name as a title, [item].category as a small label, and [item].tagline beneath, in a
 * Column that fills the width, is padded, and calls [onClick] when tapped. Use Material 3 typography.
 */
@Composable
fun ItemRow(item: Item, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
    ) {
        Text(text = item.name, style = MaterialTheme.typography.titleMedium)
        Text(text = item.category, style = MaterialTheme.typography.labelMedium)
        Text(text = item.tagline, style = MaterialTheme.typography.bodyMedium)
    }
}

/**
 * Exercise 2. State and recomposition: a stateful toggle.
 * The Boolean is remembered and observable, so flipping it recomposes the button. The state lives
 * INSIDE this composable, which is exactly what the next exercise changes.
 */
@Composable
fun FavoriteToggle(modifier: Modifier = Modifier) {
    var favorite by remember { mutableStateOf(false) }
    TextButton(onClick = { favorite = !favorite }, modifier = modifier) {
        Text(if (favorite) "★ Saved" else "☆ Save")
    }
}

/**
 * Exercise 3. State hoisting: a stateless labelled switch.
 * Holds no state of its own: it reflects [checked] and reports [onCheckedChange], and the caller
 * owns the Boolean. State down, events up. The label takes the leftover width via Modifier.weight.
 */
@Composable
fun LabeledSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/**
 * Exercise 4. A stateless row of category filter chips.
 * One FilterChip labelled "All" (selected when [selected] is null), then one per category (selected
 * when it equals [selected]). Tapping reports the choice through [onSelect] (null for "All").
 */
@Composable
fun CategoryChips(
    categories: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("All") })
        categories.forEach { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onSelect(category) },
                label = { Text(category) },
            )
        }
    }
}

/**
 * Exercise 5. The scrollable list, with an empty state.
 * If [items] is empty, show "No matching items" centered. Otherwise render the items in a LazyColumn,
 * one ItemRow each, keyed by item id, reporting a tapped id via [onItemClick].
 */
@Composable
fun ItemList(items: List<Item>, onItemClick: (String) -> Unit, modifier: Modifier = Modifier) {
    if (items.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No matching items")
        }
        return
    }
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(items = items, key = { it.id }) { item ->
            ItemRow(item = item, onClick = { onItemClick(item.id) })
        }
    }
}

/**
 * Exercise 6. The list SCREEN: it owns the search and filter state and combines the pieces.
 * Keep a search query and a selected category in remember state; compute the visible items from
 * SampleItems.all (category matches or is null, AND the name contains the query, case-insensitive).
 * The state lives in the composable, so it resets on rotation. Lab 4 moves it to a ViewModel.
 */
@Composable
fun CatalogListScreen(onItemClick: (String) -> Unit, modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf<String?>(null) }
    val visible = SampleItems.all.filter { item ->
        (category == null || item.category == category) &&
            item.name.contains(query.trim(), ignoreCase = true)
    }
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search") },
            modifier = Modifier.fillMaxWidth(),
        )
        CategoryChips(
            categories = SampleItems.categories,
            selected = category,
            onSelect = { category = it },
        )
        ItemList(items = visible, onItemClick = onItemClick)
    }
}

/**
 * Exercise 7. The detail screen.
 * Show [item].name (headline), [item].category (label), a HorizontalDivider, [item].tagline, and
 * [item].description in a padded Column, plus a "Back" button that calls [onBack].
 */
@Composable
fun ItemDetail(item: Item, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = item.name, style = MaterialTheme.typography.headlineSmall)
        Text(text = item.category, style = MaterialTheme.typography.labelLarge)
        HorizontalDivider()
        Text(text = item.tagline, style = MaterialTheme.typography.titleMedium)
        Text(text = item.description, style = MaterialTheme.typography.bodyLarge)
        TextButton(onClick = onBack) { Text("Back") }
    }
}

// Type-safe navigation routes. Each destination is a @Serializable type; the detail route carries
// just the item id, not the whole Item.
@Serializable
data object ListRoute

@Serializable
data class DetailRoute(val itemId: String)

/**
 * Exercise 8. Wire the two screens together with a navigation graph.
 * NavHost starting at [ListRoute]: the list navigates to DetailRoute(id) on a tap; the detail route
 * reads the id back, looks the item up with SampleItems.byId, and shows ItemDetail. The route
 * carries the ID, not the whole Item, so the detail screen looks up its own data.
 */
@Composable
fun CatalogNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
) {
    NavHost(navController = navController, startDestination = ListRoute, modifier = modifier) {
        composable<ListRoute> {
            CatalogListScreen(onItemClick = { id -> navController.navigate(DetailRoute(id)) })
        }
        composable<DetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<DetailRoute>()
            ItemDetail(
                item = SampleItems.byId(route.itemId),
                onBack = { navController.popBackStack() },
            )
        }
    }
}
