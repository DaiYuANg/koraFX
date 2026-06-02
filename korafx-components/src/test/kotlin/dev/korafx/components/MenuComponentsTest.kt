package dev.korafx.components

import dev.korafx.dsl.panel
import javafx.scene.control.MenuBar
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class MenuComponentsTest {
    @Test
    fun `app menu bar renders styled menus`() {
        FxTestSupport.runOnFxThread {
            var executed = false
            val menuBar = appMenuBar {
                menu("File") {
                    actionItem("Open") {
                        executed = true
                    }
                }
            }

            menuBar.menus.single().items.single().fire()

            assertTrue("app-menu-bar" in menuBar.styleClass)
            assertEquals("File", menuBar.menus.single().text)
            assertEquals("Open", menuBar.menus.single().items.single().text)
            assertTrue(executed)
        }
    }

    @Test
    fun `app menu bar can be added from node container builder`() {
        FxTestSupport.runOnFxThread {
            val root = panel {
                appMenuBar {
                    menu("View") {
                        actionItem("Refresh") {}
                    }
                }
            }

            assertTrue("app-menu-bar" in assertIs<MenuBar>(root.children.single()).styleClass)
        }
    }
}
