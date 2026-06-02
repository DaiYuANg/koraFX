package dev.korafx.commandpalette

import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.scene.input.KeyCombination

data class CommandPaletteCommand(
    val id: String,
    val title: String,
    val description: String? = null,
    val group: String? = null,
    val shortcut: KeyCombination? = null,
    val enabled: () -> Boolean = { true },
    val action: () -> Unit = {},
) {
    fun isEnabled(): Boolean = enabled()
}

class CommandPaletteHost(
    commands: Iterable<CommandPaletteCommand> = emptyList(),
) {
    private val visible = SimpleBooleanProperty(false)

    val commands = FXCollections.observableArrayList<CommandPaletteCommand>()

    val visibleProperty: ReadOnlyBooleanProperty
        get() = visible

    val isVisible: Boolean
        get() = visible.get()

    init {
        setCommands(commands)
    }

    fun show() {
        visible.set(true)
    }

    fun hide() {
        visible.set(false)
    }

    fun toggle() {
        visible.set(!visible.get())
    }

    fun setCommands(commands: Iterable<CommandPaletteCommand>) {
        this.commands.setAll(commands.toList())
    }

    fun addCommand(command: CommandPaletteCommand): CommandPaletteCommand =
        command.also {
            commands += it
        }

    fun removeCommand(id: String): Boolean =
        commands.removeIf { it.id == id }
}
