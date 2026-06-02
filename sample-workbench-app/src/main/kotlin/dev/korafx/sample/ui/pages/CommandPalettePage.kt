package dev.korafx.sample.ui.pages

import dev.korafx.commandpalette.commandMenuBar
import dev.korafx.commandpalette.commandToolbar
import dev.korafx.components.actionBar
import dev.korafx.components.section
import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.ghostButton
import dev.korafx.dsl.onAction
import dev.korafx.sample.viewmodel.WorkbenchAction

fun NodeContainerBuilder.commandPalettePage(context: WorkbenchPageContext) {
    section(
        title = "Command palette host",
        description = "Commands are registered once and can navigate modules, switch theme or run application actions.",
    ) {
        actionBar(alignEnd = false) {
            button("Open Command Palette") {
                onAction {
                    context.commandPaletteHost.show()
                }
            }
            ghostButton("Go to Source Editor") {
                onAction {
                    context.viewModel.dispatch(WorkbenchAction.NavigateModule("source-editor"))
                }
            }
        }
    }

    section(
        title = "Shared command surfaces",
        description = "The same command host can render a global menu bar, toolbar actions and the keyboard palette.",
    ) {
        commandMenuBar(
            host = context.commandPaletteHost,
            groupOrder = listOf("Navigation", "Theme"),
        )
        commandToolbar(
            host = context.commandPaletteHost,
            commandIds = listOf("theme.previous", "theme.next", "theme.toggle"),
            separateGroups = false,
        )
    }
}
