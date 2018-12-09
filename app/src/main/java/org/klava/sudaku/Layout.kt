package org.klava.sudaku

import android.view.KeyEvent

abstract class Layout(val name: String) {

    private val keymap = Array(2) { Array(9) { arrayOfNulls<Key>(9) } }

    fun getKey(startZone: Int, endZone: Int, shiftState: Int) = keymap[shiftState][startZone][endZone]

    fun getKeys(shiftState: Int) = keymap[shiftState]
    fun getKeys(zone: Int, shiftState: Int) = keymap[shiftState][zone]

    protected fun t(startZone: Int, endZone: Int, text: String) {
        if (text.isEmpty()) return
        keymap[0][startZone][endZone] = Key(Action.Text(text), text)
        keymap[1][startZone][endZone] = Key(Action.Text(text.toUpperCase()), text.toUpperCase())
    }

    protected fun t(startZone: Int, endZone: Int, text: Pair<String, String>) {
        val (normal, shifted) = text
        if (!normal.isEmpty())
            keymap[0][startZone][endZone] = Key(Action.Text(normal), normal)
        if (!shifted.isEmpty())
            keymap[1][startZone][endZone] = Key(Action.Text(shifted), shifted)
    }

    protected fun c(startZone: Int, endZone: Int, label: String, code: Int) {
        Key(Action.KeyCode(code), label).let {
            keymap[0][startZone][endZone] = it
            keymap[1][startZone][endZone] = it
        }
    }

    protected fun l(startZone: Int, endZone: Int, label: String, layout: String) {
        Key(Action.Layout(layout), label).let {
            keymap[0][startZone][endZone] = it
            keymap[1][startZone][endZone] = it
        }
    }

    init {

        arrayOf("." to "(", "," to "{", "\"" to "[").forEachIndexed { i, s ->
            t(3, i + 6, s)
        }

        arrayOf("-" to "_", "'" to "]", ";" to "}", ":" to ")").forEachIndexed { i, s ->
            t(5, i + 5, s)
        }

        arrayOf("!" to "?", "%" to "@", "#" to "$", "1" to "<", "" to "", "5" to "`", "2" to "^", "3" to "", "4" to "").forEachIndexed { i, s ->
            t(6, i, s)
        }

        c(7, 1, "nav", KEYCODE_CURSOR_MODE)
        c(7, 3, "⏎", KeyEvent.KEYCODE_ENTER)
        c(7, 5, "⇤", KEYCODE_DELETE_WORD)
        c(7, 6, "⇧", KeyEvent.KEYCODE_SHIFT_LEFT)
        c(7, 7, "␣", KeyEvent.KEYCODE_SPACE)
        c(7, 8, "⌫", KeyEvent.KEYCODE_DEL)

        arrayOf("/" to "\\", "+" to "=", "*" to "|", "6" to "~", "" to "", "0" to ">", "7" to "", "8" to "", "9" to "").forEachIndexed { i, s ->
            t(8, i, s)
        }
    }
}

const val KEYCODE_CURSOR_MODE = 100500
const val KEYCODE_DELETE_WORD = 100501