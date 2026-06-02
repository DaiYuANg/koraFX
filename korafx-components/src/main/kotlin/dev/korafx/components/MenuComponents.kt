package dev.korafx.components

import dev.korafx.dsl.MenuBarBuilder
import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.menuBar
import javafx.scene.control.MenuBar

fun appMenuBar(
    init: MenuBar.() -> Unit = {},
    content: MenuBarBuilder.() -> Unit,
): MenuBar =
    menuBar(
        init = {
            styleClass += "app-menu-bar"
            init()
        },
        content = content,
    )

fun NodeContainerBuilder.appMenuBar(
    init: MenuBar.() -> Unit = {},
    content: MenuBarBuilder.() -> Unit,
): MenuBar =
    add(
        dev.korafx.components.appMenuBar(
            init = init,
            content = content,
        ),
    )
