@file:JvmName("NavigationComponentsKt")
@file:JvmMultifileClass

package dev.korafx.navigation

import dev.korafx.components.breadcrumb
import dev.korafx.components.breadcrumbItem
import dev.korafx.dsl.state.collectLatestIn
import javafx.scene.layout.HBox
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.CoroutineScope

fun <R : Route> routeBreadcrumb(
    scope: CoroutineScope,
    navigator: Navigator<R>,
    separator: String = "/",
    includeHome: Boolean = true,
    homeText: String = "Home",
    text: (NavigationLocation<R>) -> String = { it.route.title },
    onSelect: (NavigationLocation<R>) -> Unit = { navigator.navigatePath(it.fullPath) },
    init: HBox.() -> Unit = {},
): HBox =
    breadcrumb(
        items = navigator.state.value.toBreadcrumbItems(
            navigator = navigator,
            includeHome = includeHome,
            homeText = homeText,
            text = text,
        ).toComponentBreadcrumbItems(),
        separator = separator,
        onSelect = onSelect,
        init = init,
    ).also { root ->
        navigator.state.collectLatestIn(scope) { state ->
            runNavigationOnFxThread {
                root.replaceBreadcrumbChildren(
                    items = state.toBreadcrumbItems(
                        navigator = navigator,
                        includeHome = includeHome,
                        homeText = homeText,
                        text = text,
                    ),
                    separator = separator,
                    onSelect = onSelect,
                )
            }
        }
    }

private fun <R : Route> HBox.replaceBreadcrumbChildren(
    items: List<RouteBreadcrumbEntry<R>>,
    separator: String,
    onSelect: (NavigationLocation<R>) -> Unit,
) {
    val rendered = breadcrumb(
        items = items.toComponentBreadcrumbItems(),
        separator = separator,
        onSelect = onSelect,
    )
    val renderedChildren = rendered.children.toList()
    rendered.children.clear()
    children.setAll(renderedChildren)
}

private fun <R : Route> NavigationState<R>.toBreadcrumbItems(
    navigator: Navigator<R>,
    includeHome: Boolean,
    homeText: String,
    text: (NavigationLocation<R>) -> String,
): List<RouteBreadcrumbEntry<R>> {
    val entries = mutableListOf<RouteBreadcrumbEntry<R>>()
    val currentPath = currentLocation.path

    if (includeHome) {
        navigator.homeLocation(this)?.let { homeLocation ->
            entries += RouteBreadcrumbEntry(
                location = homeLocation,
                text = homeText,
                current = currentPath == "/",
            )
        }
    }

    if (!includeHome || currentPath != "/") {
        val hierarchy = nestedRouteEntries(navigator, text)
        val pathEntries = pathSegmentEntries(navigator, text)
        val routeEntries = hierarchy.takeIf { it.isNotEmpty() } ?: pathEntries

        routeEntries.forEach { entry ->
            if (!includeHome || entry.location.path != "/") {
                entries.addOrReplaceDuplicatePath(entry)
            }
        }
    }

    if (entries.isEmpty()) {
        entries += RouteBreadcrumbEntry(
            location = currentLocation,
            text = text(currentLocation),
            current = true,
        )
    }

    return entries.markLastCurrent()
}

private fun <R : Route> NavigationState<R>.nestedRouteEntries(
    navigator: Navigator<R>,
    text: (NavigationLocation<R>) -> String,
): List<RouteBreadcrumbEntry<R>> {
    val nestedCurrentRoute = currentRoute as? NestedPathRoute ?: return emptyList()
    val routeById = routes.associateBy(Route::id)
    val chain = mutableListOf<R>()
    val visited = linkedSetOf<String>()
    var route: R? = routes.firstOrNull { it.id == nestedCurrentRoute.id }

    while (route != null && visited.add(route.id)) {
        chain += route
        route = (route as? NestedPathRoute)
            ?.parentRouteId
            ?.let(routeById::get)
    }

    return chain
        .asReversed()
        .mapNotNull { route ->
            val location =
                if (route.id == currentLocation.route.id) {
                    currentLocation
                } else {
                    navigator.locationForRoute(route, currentLocation)
                }
            location?.let {
                RouteBreadcrumbEntry(
                    location = it,
                    text = text(it),
                    current = it.fullPath == currentLocation.fullPath,
                )
            }
        }
}

private fun <R : Route> NavigationState<R>.pathSegmentEntries(
    navigator: Navigator<R>,
    text: (NavigationLocation<R>) -> String,
): List<RouteBreadcrumbEntry<R>> =
    currentLocation.pathPrefixes().map { path ->
        val matched = if (path == currentLocation.path) {
            currentLocation
        } else {
            navigator.matchPath(path)
        }
        val location = matched ?: currentLocation.copy(
            path = path,
            fullPath = path,
            params = emptyMap(),
            query = RouteQuery.Empty,
            hash = null,
            meta = RouteMeta.Empty,
        )
        RouteBreadcrumbEntry(
            location = location,
            text = matched?.let(text) ?: path.lastSegmentTitle(),
            current = path == currentLocation.path,
        )
    }

private fun <R : Route> Navigator<R>.homeLocation(state: NavigationState<R>): NavigationLocation<R>? =
    if (state.currentLocation.path == "/") {
        state.currentLocation
    } else {
        locationForPath("/")
    }

private fun <R : Route> Navigator<R>.locationForPath(path: String): NavigationLocation<R>? =
    routeList
        .asSequence()
        .mapNotNull { route -> locationForRoute(route, path) }
        .firstOrNull { it.path == path }

private fun <R : Route> Navigator<R>.locationForRoute(
    route: R,
    currentLocation: NavigationLocation<R>,
): NavigationLocation<R>? {
    val pattern = route.compiledPath(routePathById)
    val path = runCatching {
        RoutePattern.build(pattern = pattern, params = currentLocation.params)
    }.getOrNull() ?: return null
    return locationForRoute(route, path)
}

private fun <R : Route> Navigator<R>.locationForRoute(
    route: R,
    path: String,
): NavigationLocation<R>? {
    val pattern = route.compiledPath(routePathById)
    val match = RoutePattern.match(pattern, path) ?: return null
    return route.toLocation(match)
}

private fun <R : Route> MutableList<RouteBreadcrumbEntry<R>>.addOrReplaceDuplicatePath(
    entry: RouteBreadcrumbEntry<R>,
) {
    val existingIndex = indexOfLast { it.location.path == entry.location.path }
    if (existingIndex >= 0) {
        this[existingIndex] = entry
    } else {
        this += entry
    }
}

private fun <R : Route> List<RouteBreadcrumbEntry<R>>.markLastCurrent(): List<RouteBreadcrumbEntry<R>> =
    mapIndexed { index, entry ->
        entry.copy(current = index == lastIndex)
    }

private fun <R : Route> List<RouteBreadcrumbEntry<R>>.toComponentBreadcrumbItems() =
    map { entry ->
        breadcrumbItem(
            value = entry.location,
            text = entry.text,
            current = entry.current,
        )
    }

private fun NavigationLocation<*>.pathPrefixes(): List<String> {
    val normalized = RoutePattern.normalize(path)
    if (normalized == "/") {
        return listOf("/")
    }

    val prefixes = mutableListOf<String>()
    var current = ""
    normalized.trim('/').split("/").forEach { segment ->
        current += "/$segment"
        prefixes += current
    }
    return prefixes
}

private fun String.lastSegmentTitle(): String =
    trim('/')
        .substringAfterLast("/")
        .let { segment -> URLDecoder.decode(segment, StandardCharsets.UTF_8) }
        .replace(Regex("[-_]+"), " ")
        .split(" ")
        .filter(String::isNotBlank)
        .joinToString(" ") { word ->
            word.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase() else char.toString()
            }
        }
        .ifBlank { "/" }

private data class RouteBreadcrumbEntry<R : Route>(
    val location: NavigationLocation<R>,
    val text: String,
    val current: Boolean,
)
