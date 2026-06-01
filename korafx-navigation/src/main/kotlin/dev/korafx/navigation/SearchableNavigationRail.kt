@file:JvmName("NavigationComponentsKt")
@file:JvmMultifileClass

package dev.korafx.navigation

import dev.korafx.dsl.bindTextBidirectional
import dev.korafx.dsl.bindVisible
import dev.korafx.dsl.label
import dev.korafx.dsl.sidebar
import dev.korafx.dsl.textField
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

fun <R : Route> searchableNavigationRail(
    scope: CoroutineScope,
    navigator: Navigator<R>,
    routes: List<R> = navigator.routes,
    width: Double = 220.0,
    spacing: Double = 10.0,
    searchPrompt: String = "Search routes",
    emptyText: String = "No routes match the current search.",
    matches: (route: R, query: String) -> Boolean = ::defaultRouteSearchMatcher,
    init: VBox.() -> Unit = {},
    buttonInit: Button.(R) -> Unit = {},
): VBox {
    val query = MutableStateFlow("")
    val routeList = routes.toList()

    return sidebar(
        width = width,
        spacing = spacing,
        init = init,
    ) {
        textField {
            promptText = searchPrompt
            bindTextBidirectional(scope, query)
        }

        routeList.forEach { route ->
            add(
                routeButton(
                    scope = scope,
                    navigator = navigator,
                    route = route,
                ) {
                    maxWidth = Double.MAX_VALUE
                    buttonInit(route)
                },
            ).bindVisible(scope, query.map { value -> matches(route, value) })
        }

        label(emptyText) {
            styleClass += "navigation-empty-state"
        }.bindVisible(
            scope = scope,
            flow = query.map { value ->
                value.isNotBlank() && routeList.none { route -> matches(route, value) }
            },
        )
    }
}

private fun <R : Route> defaultRouteSearchMatcher(
    route: R,
    query: String,
): Boolean {
    val normalized = query.trim()
    if (normalized.isBlank()) {
        return true
    }

    return route.id.contains(normalized, ignoreCase = true) ||
        route.title.contains(normalized, ignoreCase = true)
}
