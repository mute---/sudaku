package org.klava.sudaku

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.ExtractedTextRequest
import kotlin.math.max

class Sudaku : InputMethodService(), IKeyboardEventListener {

    private val layoutManager = LayoutManager()
    private lateinit var inputView: InputView

    private var shiftState = ShiftState.Off

    override fun onCreateInputView(): View {
        inputView = InputView(this).apply {
            setLayout(layoutManager.currentLayout)
            setEventListener(this@Sudaku)
        }

        return inputView
    }

    override fun onInput(action: Action) {
        when (action) {
            is Action.Text -> {
                currentInputConnection.commitText("", 0)
                currentInputConnection.commitText(action.text, 1)
                if (shiftState == ShiftState.On) {
                    shiftState = ShiftState.Off
                    inputView.setShiftState(shiftState)
                }
            }

            is Action.KeyCode -> {
                when (action.code) {
                    KeyEvent.KEYCODE_SHIFT_LEFT -> {
                        updateShiftState()
                        inputView.setShiftState(shiftState)
                    }
                    KEYCODE_DELETE_WORD -> deleteWord()
                    KEYCODE_CURSOR_MODE -> {
                        inputView.enableCursorMode()
                    }
                    else -> {
                        currentInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, action.code))
                        currentInputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, action.code))
                    }
                }
            }

            is Action.Layout -> {
                layoutManager.changeLayout(action.layoutName)
                updateViewLayout()
            }

            is Action.Cursor -> {
                currentInputConnection.getExtractedText(ExtractedTextRequest(), 0).selectionStart.let {
                    currentInputConnection.setSelection(max(0, it - action.dx), max(0, it - action.dx))
                }
            }
        }
    }

    private fun updateViewLayout() {
        inputView.setLayout(layoutManager.currentLayout)
    }

    private fun updateShiftState() {
        shiftState = when (shiftState) {
            ShiftState.Off -> ShiftState.On
            ShiftState.On -> ShiftState.Lock
            ShiftState.Lock -> ShiftState.Off
        }
    }

    private fun deleteWord() {
        currentInputConnection.getTextBeforeCursor(50, 0)
            .split(" ")
            .lastOrNull()?.let {
                currentInputConnection.deleteSurroundingText(it.length + 1, 0)
            }
    }
}