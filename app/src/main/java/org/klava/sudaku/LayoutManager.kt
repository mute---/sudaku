package org.klava.sudaku

import org.klava.sudaku.layouts.*

class LayoutManager {
    private val layouts = mutableMapOf<String, Layout>()
    lateinit var currentLayout : Layout
        private set

    fun changeLayout(layoutName: String) {
        layouts[layoutName]?.let {
            currentLayout = it
        }
    }

    init {
        layouts["en"] = EnLayout()
        layouts["ru"] = RuLayout()

        changeLayout("en")
    }
}