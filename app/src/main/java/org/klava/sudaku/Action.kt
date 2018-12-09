package org.klava.sudaku

sealed class Action {
    data class Text(val text: String) : Action()
    data class KeyCode(val code: Int) : Action()
    data class Layout(val layoutName: String) : Action()
    data class Cursor(val dx: Int) : Action()
}