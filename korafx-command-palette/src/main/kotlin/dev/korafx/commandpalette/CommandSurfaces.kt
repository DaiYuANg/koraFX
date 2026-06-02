package dev.korafx.commandpalette

import dev.korafx.dsl.NodeContainerBuilder
import javafx.collections.ListChangeListener
import javafx.scene.control.Button
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.Separator
import javafx.scene.control.ToolBar
import javafx.scene.control.Tooltip

class CommandMenuBar internal constructor(
    val host: CommandPaletteHost,
    private val groupOrder: List<String>?,
    private val includeUngrouped: Boolean,
    private val defaultMenuText: String,
) : MenuBar() {
    init {
        styleClass += "command-menu-bar"
        host.commands.addListener(ListChangeListener {
            refresh()
        })
        refresh()
    }

    fun refresh() {
        menus.setAll(groupedCommands().map { (group, commands) ->
            Menu(group).apply {
                styleClass += "command-menu"
                items.setAll(commands.map { it.toMenuItem() })
            }
        })
    }

    private fun groupedCommands(): List<Pair<String, List<CommandPaletteCommand>>> {
        val grouped = linkedMapOf<String, MutableList<CommandPaletteCommand>>()
        host.commands.forEach { command ->
            val group = command.group?.takeIf(String::isNotBlank) ?: defaultMenuText
            if (group != defaultMenuText || includeUngrouped) {
                grouped.getOrPut(group) { mutableListOf() } += command
            }
        }
        val ordered = (groupOrder ?: emptyList()) + grouped.keys.filterNot { it in (groupOrder ?: emptyList()) }
        return ordered.mapNotNull { group -> grouped[group]?.let { group to it.toList() } }
    }
}

class CommandToolbar internal constructor(
    val host: CommandPaletteHost,
    commandIds: Iterable<String>?,
    private val showText: Boolean,
    private val separateGroups: Boolean,
) : ToolBar() {
    private val commandIds = commandIds?.toList()

    init {
        styleClass += "command-toolbar"
        host.commands.addListener(ListChangeListener {
            refresh()
        })
        refresh()
    }

    fun refresh() {
        items.clear()
        var previousGroup: String? = null
        visibleCommands().forEach { command ->
            val group = command.group
            if (separateGroups && items.isNotEmpty() && group != previousGroup) {
                items += Separator()
            }
            previousGroup = group
            items += command.toButton(showText)
        }
    }

    private fun visibleCommands(): List<CommandPaletteCommand> {
        val ids = commandIds ?: return host.commands.toList()
        return ids.mapNotNull { id -> host.commands.firstOrNull { it.id == id } }
    }
}

fun commandMenuBar(
    host: CommandPaletteHost,
    groupOrder: Iterable<String>? = null,
    includeUngrouped: Boolean = true,
    defaultMenuText: String = "Commands",
    init: CommandMenuBar.() -> Unit = {},
): CommandMenuBar =
    CommandMenuBar(
        host = host,
        groupOrder = groupOrder?.toList(),
        includeUngrouped = includeUngrouped,
        defaultMenuText = defaultMenuText,
    ).apply(init)

fun commandToolbar(
    host: CommandPaletteHost,
    commandIds: Iterable<String>? = null,
    showText: Boolean = true,
    separateGroups: Boolean = true,
    init: CommandToolbar.() -> Unit = {},
): CommandToolbar =
    CommandToolbar(
        host = host,
        commandIds = commandIds,
        showText = showText,
        separateGroups = separateGroups,
    ).apply(init)

fun NodeContainerBuilder.commandMenuBar(
    host: CommandPaletteHost,
    groupOrder: Iterable<String>? = null,
    includeUngrouped: Boolean = true,
    defaultMenuText: String = "Commands",
    init: CommandMenuBar.() -> Unit = {},
): CommandMenuBar =
    add(dev.korafx.commandpalette.commandMenuBar(host, groupOrder, includeUngrouped, defaultMenuText, init))

fun NodeContainerBuilder.commandToolbar(
    host: CommandPaletteHost,
    commandIds: Iterable<String>? = null,
    showText: Boolean = true,
    separateGroups: Boolean = true,
    init: CommandToolbar.() -> Unit = {},
): CommandToolbar =
    add(dev.korafx.commandpalette.commandToolbar(host, commandIds, showText, separateGroups, init))

private fun CommandPaletteCommand.toMenuItem(): MenuItem =
    MenuItem(title).apply {
        styleClass += "command-menu-item"
        accelerator = shortcut
        isDisable = !isEnabled()
        setOnAction {
            executeIfEnabled()
        }
    }

private fun CommandPaletteCommand.toButton(showText: Boolean): Button =
    Button(if (showText) title else "").apply {
        styleClass += "command-toolbar-action"
        isDisable = !isEnabled()
        tooltip = Tooltip(description ?: id)
        setOnAction {
            executeIfEnabled()
        }
    }

private fun CommandPaletteCommand.executeIfEnabled() {
    if (isEnabled()) {
        action()
    }
}
