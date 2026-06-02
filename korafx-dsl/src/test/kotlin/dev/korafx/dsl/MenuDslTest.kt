package dev.korafx.dsl

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MenuDslTest {
    @Test
    fun `action item wires menu action`() {
        FxTestSupport.runOnFxThread {
            var executed = 0

            val menuBar = menuBar {
                menu("File") {
                    actionItem("Run") {
                        executed += 1
                    }
                }
            }

            menuBar.menus.single().items.single().fire()

            assertEquals(1, executed)
        }
    }

    @Test
    fun `action item action wins over init event handler`() {
        FxTestSupport.runOnFxThread {
            var executed = 0

            val menuBar = menuBar {
                menu("File") {
                    actionItem(
                        text = "Run",
                        init = {
                            onAction {
                                executed = -1
                            }
                        },
                    ) {
                        executed = 1
                    }
                }
            }

            menuBar.menus.single().items.single().fire()

            assertEquals(1, executed)
        }
    }

    @Test
    fun `menu bar can be added from node container builder`() {
        FxTestSupport.runOnFxThread {
            val root = panel {
                menuBar {
                    menu("View") {
                        actionItem("Refresh") {}
                    }
                }
            }

            assertTrue(root.children.single() is javafx.scene.control.MenuBar)
        }
    }
}
