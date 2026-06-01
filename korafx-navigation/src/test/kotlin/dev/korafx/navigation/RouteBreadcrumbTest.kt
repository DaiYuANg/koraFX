package dev.korafx.navigation

import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.control.Labeled
import javafx.scene.layout.HBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class RouteBreadcrumbTest {
    @Test
    fun `route breadcrumb renders initial path hierarchy`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(
            initialRoute = BreadcrumbRoute.SourceEditor,
            routes = BreadcrumbRoute.all,
        )

        try {
            val breadcrumb = FxTestSupport.run {
                lateinit var result: HBox
                runOnFxThread {
                    result = routeBreadcrumb(scope = scope, navigator = navigator)
                }
                result
            }

            FxTestSupport.waitForFxCondition {
                breadcrumb.breadcrumbItemTexts() == listOf("Home", "Components", "Source Editor")
            }

            assertIs<Hyperlink>(breadcrumb.children[0])
            assertIs<Hyperlink>(breadcrumb.children[2])
            val current = assertIs<Label>(breadcrumb.children[4])
            assertTrue("breadcrumb-item-current" in current.styleClass)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `route breadcrumb updates when navigator location changes`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(
            initialRoute = BreadcrumbRoute.Home,
            routes = BreadcrumbRoute.all,
        )

        try {
            val breadcrumb = FxTestSupport.run {
                lateinit var result: HBox
                runOnFxThread {
                    result = routeBreadcrumb(scope = scope, navigator = navigator)
                }
                result
            }

            FxTestSupport.waitForFxCondition {
                breadcrumb.breadcrumbItemTexts() == listOf("Home")
            }

            navigator.navigatePath("/components/source-editor")

            FxTestSupport.waitForFxCondition {
                breadcrumb.breadcrumbItemTexts() == listOf("Home", "Components", "Source Editor")
            }

            val current = assertIs<Label>(breadcrumb.children[4])
            assertEquals("Source Editor", current.text)
            assertTrue("breadcrumb-item-current" in current.styleClass)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `route breadcrumb navigates when non current crumb is clicked`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(
            initialRoute = BreadcrumbRoute.SourceEditor,
            routes = BreadcrumbRoute.all,
        )

        try {
            val breadcrumb = FxTestSupport.run {
                lateinit var result: HBox
                runOnFxThread {
                    result = routeBreadcrumb(scope = scope, navigator = navigator)
                }
                result
            }

            FxTestSupport.waitForFxCondition {
                breadcrumb.breadcrumbItemTexts() == listOf("Home", "Components", "Source Editor")
            }

            FxTestSupport.runOnFxThread {
                assertIs<Hyperlink>(breadcrumb.children[2]).fire()
            }

            FxTestSupport.waitForFxCondition {
                navigator.currentRoute == BreadcrumbRoute.Components &&
                    navigator.currentLocation.fullPath == "/components" &&
                    breadcrumb.breadcrumbItemTexts() == listOf("Home", "Components")
            }

            val current = assertIs<Label>(breadcrumb.children[2])
            assertEquals("Components", current.text)
            assertTrue("breadcrumb-item-current" in current.styleClass)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `route breadcrumb omits home when no root route exists`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val navigator = Navigator(
            initialRoute = BreadcrumbRouteWithoutHome.SourceEditor,
            routes = BreadcrumbRouteWithoutHome.all,
        )

        try {
            val breadcrumb = FxTestSupport.run {
                lateinit var result: HBox
                runOnFxThread {
                    result = routeBreadcrumb(scope = scope, navigator = navigator)
                }
                result
            }

            FxTestSupport.waitForFxCondition {
                breadcrumb.breadcrumbItemTexts() == listOf("Components", "Source Editor")
            }
        } finally {
            scope.cancel()
        }
    }

    private fun HBox.breadcrumbItemTexts(): List<String> =
        children
            .filter { node -> "breadcrumb-item" in node.styleClass }
            .map { node -> assertIs<Labeled>(node).text }
}

private data class BreadcrumbRoute(
    override val id: String,
    override val title: String,
    override val path: String,
) : PathRoute {
    companion object {
        val Home = BreadcrumbRoute("home", "Home", "/")
        val Components = BreadcrumbRoute("components", "Components", "/components")
        val SourceEditor = BreadcrumbRoute("source-editor", "Source Editor", "/components/source-editor")

        val all: List<BreadcrumbRoute>
            get() = listOf(Home, Components, SourceEditor)
    }
}

private data class BreadcrumbRouteWithoutHome(
    override val id: String,
    override val title: String,
    override val path: String,
) : PathRoute {
    companion object {
        val Components = BreadcrumbRouteWithoutHome("components", "Components", "/components")
        val SourceEditor = BreadcrumbRouteWithoutHome("source-editor", "Source Editor", "/components/source-editor")

        val all: List<BreadcrumbRouteWithoutHome>
            get() = listOf(Components, SourceEditor)
    }
}
