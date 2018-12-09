package org.klava.sudaku.layouts

import org.klava.sudaku.Layout

class EnLayout : Layout("en") {
    init {

        arrayOf("e", "i", "j", "h", "", "f", "g", "ful", "ing").forEachIndexed { i, s ->
            t(0, i, s)
        }

        arrayOf("m", "o", "n", "k", "", "l", "x", "gth", "z").forEachIndexed { i, s ->
            t(1, i, s)
        }

        arrayOf("p", "r", "s", "u", "", "t", "age", "ble", "q").forEachIndexed { i, s ->
            t(2, i, s)
        }

        arrayOf("b", "c", "d", "a", "", "ck").forEachIndexed { i, s ->
            t(3, i, s)
        }

        arrayOf("v", "w", "y", "th").forEachIndexed { i, s ->
            t(5, i, s)
        }

        l(7, 0, "en", "en")
        l(7, 2, "ru", "ru")
    }
}