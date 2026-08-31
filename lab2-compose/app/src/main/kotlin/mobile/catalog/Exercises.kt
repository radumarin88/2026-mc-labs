package mobile.catalog

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable

/*
 * Lab 2 exercises. One task in two movements. First you learn the fundamentals of Compose on small
 * pieces (a row, a stateful button, a hoisted control); then you build a real multi-screen app: a
 * searchable, filterable catalog with a detail screen you navigate to. Exercises 1 to 3 are the
 * fundamentals; 4 and 5 are list pieces; 6 assembles the list screen; 7 and 8 are the detail screen
 * and the navigation graph. This is the shape of the app your team will build for the project.
 * As you write, let Android Studio add the imports you need (Alt+Enter / the lightbulb).
 */

/**
 * Exercise 1. Your first composable: one row of the list.
 * A composable is just a function marked @Composable that describes UI by calling other composables.
 * Show [item].name as a title, [item].category as a small label, and [item].tagline beneath, in a
 * Column that fills the width, is padded, and calls [onClick] when tapped. Use Material 3 typography
 * (MaterialTheme.typography.titleMedium, labelMedium, bodyMedium) and Modifier.clickable { }.
 */
@Composable
fun ItemRow(item: Item, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TODO()
}

/**
 * Exercise 2. State and recomposition: a stateful toggle.
 * Keep a Boolean with `var favorite by remember { mutableStateOf(false) }` and show a TextButton
 * reading "☆ Save" when false and "★ Saved" when true; tapping it flips the value. Because
 * the state is remembered and observable, Compose recomposes the button when it changes. Preview it
 * and click it in the preview pane. The state lives INSIDE this composable, which you fix next.
 */
@Composable
fun FavoriteToggle(modifier: Modifier = Modifier) {
    TODO()
}

/**
 * Exercise 3. State hoisting: the same idea, made reusable.
 * A stateless control holds no state of its own: it takes the current value and reports events, and
 * the caller owns the value. Implement [LabeledSwitch] as a Row with the [label] on the left taking
 * the leftover width (Modifier.weight(1f)) and a Material 3 Switch on the right reflecting [checked]
 * and reporting [onCheckedChange]. State flows DOWN as a parameter, events flow UP as a callback.
 * To preview it, write a small wrapper that owns a Boolean and passes it down.
 */
@Composable
fun LabeledSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    TODO()
}

/**
 * Exercise 4. A stateless row of category filter chips.
 * Show one Material 3 FilterChip labelled "All" (selected when [selected] is null), then one chip
 * per category in [categories] (selected when it equals [selected]). Tapping a chip reports the
 * choice through [onSelect] (null for "All"). Put them in a horizontally scrollable Row. Same
 * hoisting shape as exercise 3, now with a real filter.
 */
@Composable
fun CategoryChips(
    categories: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    TODO()
}

/**
 * Exercise 5. The scrollable list, with an empty state.
 * If [items] is empty, show "No matching items" centered in the available space. Otherwise render
 * the items in a LazyColumn, one ItemRow each, keyed by item id, reporting a tapped id via [onItemClick].
 * A LazyColumn only composes the rows on screen, unlike a plain Column.
 */
@Composable
fun ItemList(items: List<Item>, onItemClick: (String) -> Unit, modifier: Modifier = Modifier) {
    TODO()
}

/**
 * Exercise 6. The list SCREEN: it owns the search and filter state and combines the pieces.
 * Keep a search query (String) and a selected category (String?, null = All) in remember state.
 * Compute the visible items from SampleItems.all: keep an item when the selected category is null
 * or matches its category, AND its name contains the query (case-insensitive). Lay out in a Column:
 * a search OutlinedTextField, then CategoryChips, then ItemList(visible, onItemClick).
 * (Run the app, then rotate the device: the search box clears. That state lives in a composable;
 * Lab 4 moves it to a ViewModel so it survives.)
 */
@Composable
fun CatalogListScreen(onItemClick: (String) -> Unit, modifier: Modifier = Modifier) {
    TODO()
}

/**
 * Exercise 7. The detail screen.
 * Show [item].name (headline), [item].category (label), a HorizontalDivider, [item].tagline, and
 * [item].description in a padded Column, plus a "Back" button that calls [onBack].
 */
@Composable
fun ItemDetail(item: Item, onBack: () -> Unit, modifier: Modifier = Modifier) {
    TODO()
}

// Type-safe navigation routes. Each destination is a @Serializable type; the detail route carries
// just the item id, not the whole Item.
@Serializable
data object ListRoute

@Serializable
data class DetailRoute(val itemId: String)

/**
 * Exercise 8. Wire the two screens together with a navigation graph.
 * Build a NavHost starting at [ListRoute] with two destinations:
 *   - ListRoute: show CatalogListScreen; on a tap, navigate to DetailRoute(id).
 *   - DetailRoute: read the id from the route, look the item up with SampleItems.byId, and show
 *     ItemDetail; Back should pop the back stack.
 * The route carries the ID, not the whole Item: the detail screen looks up its own data.
 */
@Composable
fun CatalogNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
) {
    TODO()
}
