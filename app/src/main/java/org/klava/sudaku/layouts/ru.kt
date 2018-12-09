package org.klava.sudaku.layouts

import org.klava.sudaku.Layout

class RuLayout : Layout("ru") {
    init {

        arrayOf("е", "и", "й", "ё", "", "ж", "з", "ь", "ъ").forEachIndexed { i, s ->
            t(0, i, s)
        }

        arrayOf("м", "о", "н", "к", "", "л", "э", "ю", "я").forEachIndexed { i, s ->
            t(1, i, s)
        }

        arrayOf("п", "р", "с", "у", "", "т", "ф", "х", "ц").forEachIndexed { i, s ->
            t(2, i, s)
        }

        arrayOf("в", "г", "д", "а", "", "б").forEachIndexed { i, s ->
            t(3, i, s)
        }

        arrayOf("ш", "ч", "ы", "щ").forEachIndexed { i, s ->
            t(5, i, s)
        }

        l(7, 0, "en", "en")
        l(7, 2, "ru", "ru")
    }
}