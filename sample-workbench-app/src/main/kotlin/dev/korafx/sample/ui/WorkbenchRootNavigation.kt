package dev.korafx.sample.ui

import dev.korafx.components.section
import dev.korafx.dsl.bindDisable
import dev.korafx.dsl.bindText
import dev.korafx.dsl.bindTextBidirectional
import dev.korafx.dsl.bindVisible
import dev.korafx.dsl.button
import dev.korafx.dsl.label
import dev.korafx.dsl.onAction
import dev.korafx.dsl.scrollPane
import dev.korafx.dsl.sidebar
import dev.korafx.dsl.styleClasses
import dev.korafx.dsl.textField
import dev.korafx.framework.theme.ThemeStyleClass
import dev.korafx.navigation.Navigator
import dev.korafx.navigation.routeButton
import dev.korafx.sample.domain.ModuleCategory
import dev.korafx.sample.domain.ModuleShowcase
import dev.korafx.sample.navigation.WorkbenchRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal fun workbenchModuleSidebar(
    uiScope: CoroutineScope,
    navigator: Navigator<WorkbenchRoute>,
): javafx.scene.Node {
    val query = MutableStateFlow("")
    return scrollPane(
        init = {
            prefWidth = 300.0
            minWidth = 220.0
            maxWidth = 480.0
            isFitToWidth = true
        },
    ) {
        content {
            sidebar(width = 278.0, spacing = 12.0) {
                label("Modules") {
                    styleClasses(ThemeStyleClass.Headline)
                }
                textField {
                    promptText = "Search modules"
                    bindTextBidirectional(uiScope, query)
                }

                add(
                    routeButton(
                        scope = uiScope,
                        navigator = navigator,
                        route = WorkbenchRoute.Overview,
                        text = "Overview",
                    ) {
                        maxWidth = Double.MAX_VALUE
                    },
                ).bindVisible(uiScope, query.map { WorkbenchRoute.Overview.matchesQuery(it) })

                ModuleCategory.entries.forEach { category ->
                    val routes = WorkbenchRoute.moduleRoutes
                        .filter { route -> WorkbenchRoute.findModule(route.id)?.category == category }

                    label(category.title) {
                        styleClasses(ThemeStyleClass.Muted)
                    }.bindVisible(
                        uiScope,
                        query.map { value -> routes.any { it.matchesQuery(value) } },
                    )

                    routes.forEach { route ->
                        add(
                            routeButton(
                                scope = uiScope,
                                navigator = navigator,
                                route = route,
                                text = route.title,
                            ) {
                                maxWidth = Double.MAX_VALUE
                            },
                        ).bindVisible(uiScope, query.map { route.matchesQuery(it) })
                    }
                }

                label("No modules match the current search.") {
                    styleClasses(ThemeStyleClass.Muted)
                }.bindVisible(
                    scope = uiScope,
                    flow = query.map { value ->
                        value.isNotBlank() && WorkbenchRoute.all.none { route -> route.matchesQuery(value) }
                    },
                )

                section("Navigation") {
                    label("Current path") {
                        styleClasses(ThemeStyleClass.Muted)
                    }
                    label(navigator.currentLocation.fullPath) {
                        styleClasses(ThemeStyleClass.Muted)
                    }.bindText(uiScope, navigator.state.map { it.currentLocation.fullPath })

                    button("Back") {
                        bindDisable(uiScope, navigator.state.map { it.backStack.isEmpty() })
                        onAction {
                            uiScope.launch {
                                navigator.backAsync()
                            }
                        }
                    }
                    button("Forward") {
                        bindDisable(uiScope, navigator.state.map { it.forwardStack.isEmpty() })
                        onAction {
                            uiScope.launch {
                                navigator.forwardAsync()
                            }
                        }
                    }
                    button("Root") {
                        bindDisable(uiScope, navigator.state.map { it.currentRoute.id == WorkbenchRoute.Overview.id })
                        onAction {
                            uiScope.launch {
                                navigator.navigatePathAsync("/")
                            }
                        }
                    }
                }

                section("Quick Actions") {
                    button("Open Source Editor") {
                        onAction {
                            navigator.navigatePath("/components/source-editor")
                        }
                    }
                    button("Clear History") {
                        onAction {
                            navigator.clearNavigationHistory()
                        }
                    }
                }
            }
        }
    }
}

private fun WorkbenchRoute.matchesQuery(query: String): Boolean {
    val normalized = query.trim()
    if (normalized.isBlank()) {
        return true
    }

    val module = moduleId?.let(WorkbenchRoute::findModule)
    return searchableText(module).contains(normalized, ignoreCase = true)
}

private fun WorkbenchRoute.searchableText(module: ModuleShowcase?): String =
    buildString {
        append(title)
        append(' ')
        append(summary)
        append(' ')
        append(path)
        if (module != null) {
            append(' ')
            append(module.artifactName)
            append(' ')
            append(module.category.title)
            append(' ')
            append(module.tags.joinToString(" "))
        }
    }
