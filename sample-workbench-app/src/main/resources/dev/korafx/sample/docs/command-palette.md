# Command Palette

`korafx-command-palette` is a lightweight command launcher for desktop workflows.

## What it offers

- Fuzzy filtering across command title/description/group.
- Keyboard and mouse selection.
- Host visibility control + close behavior.
- Command model decoupled from UI rendering.
- Shared command model for palette, menu bar and toolbar surfaces.

## Example

```kotlin
val host = CommandPaletteHost(commands = commands)

button("Commands") {
  onAction { host.show() }
}

commandMenuBar(host, groupOrder = listOf("Navigation", "Theme"))
commandToolbar(host, commandIds = listOf("theme.previous", "theme.next"))
commandPalette(host)
```

Commands are registered by the application layer, keeping modules focused on rendering.
