package dev.korafx.commandpalette

import dev.korafx.dsl.panel
import javafx.scene.control.Button
import javafx.scene.control.MenuBar
import javafx.scene.control.ToolBar
import javafx.scene.input.KeyCombination
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CommandSurfacesTest {
    @Test
    fun `command menu bar groups commands and executes enabled menu items`() {
        FxTestSupport.runOnFxThread {
            var executed = ""
            val shortcut = KeyCombination.keyCombination("Ctrl+O")
            val host = CommandPaletteHost(
                listOf(
                    command("open", "Open", "File", shortcut) { executed = "open" },
                    command("disabled", "Disabled", "File", enabled = { false }) { executed = "disabled" },
                    command("next-theme", "Next Theme", "Theme") { executed = "theme" },
                ),
            )

            val menuBar = commandMenuBar(host, groupOrder = listOf("File", "Theme"))
            val fileMenu = menuBar.menus[0]

            fileMenu.items[0].fire()
            fileMenu.items[1].fire()

            assertTrue("command-menu-bar" in menuBar.styleClass)
            assertEquals("File", fileMenu.text)
            assertEquals(shortcut, fileMenu.items[0].accelerator)
            assertTrue(fileMenu.items[1].isDisable)
            assertEquals("open", executed)
            assertEquals("Theme", menuBar.menus[1].text)
        }
    }

    @Test
    fun `command toolbar renders selected command ids in requested order`() {
        FxTestSupport.runOnFxThread {
            val executed = mutableListOf<String>()
            val host = CommandPaletteHost(
                listOf(
                    command("open", "Open", "File") { executed += "open" },
                    command("next-theme", "Next Theme", "Theme") { executed += "theme" },
                    command("ignored", "Ignored", "Theme") { executed += "ignored" },
                ),
            )

            val toolbar = commandToolbar(
                host = host,
                commandIds = listOf("next-theme", "open"),
                separateGroups = false,
            )

            assertTrue("command-toolbar" in toolbar.styleClass)
            assertEquals(2, toolbar.items.size)
            assertEquals("Next Theme", assertIs<Button>(toolbar.items[0]).text)
            assertEquals("Open", assertIs<Button>(toolbar.items[1]).text)

            assertIs<Button>(toolbar.items[0]).fire()

            assertEquals(listOf("theme"), executed)
        }
    }

    @Test
    fun `command surfaces can be added from node container builder`() {
        FxTestSupport.runOnFxThread {
            val host = CommandPaletteHost(listOf(command("run", "Run", "Build") {}))
            val root = panel {
                commandMenuBar(host)
                commandToolbar(host)
            }

            assertTrue("command-menu-bar" in assertIs<MenuBar>(root.children[0]).styleClass)
            assertTrue("command-toolbar" in assertIs<ToolBar>(root.children[1]).styleClass)
        }
    }

    private fun command(
        id: String,
        title: String,
        group: String,
        shortcut: KeyCombination? = null,
        enabled: () -> Boolean = { true },
        action: () -> Unit,
    ): CommandPaletteCommand =
        CommandPaletteCommand(
            id = id,
            title = title,
            group = group,
            action = action,
            shortcut = shortcut,
            enabled = enabled,
        )
}
