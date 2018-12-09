package org.klava.sudaku

import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.CompoundButton
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView

class TestActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener, OnCheckedChangeListener {

    private lateinit var prefsManager: SharedPreferences
    private lateinit var fontTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        prefsManager = PreferenceManager.getDefaultSharedPreferences(this)

        findViewById<SeekBar>(R.id.alignment).apply {
            progress = prefsManager.getInt("sudaku_grid_alignment", 1)
            setOnSeekBarChangeListener(this@TestActivity)
        }

        findViewById<SeekBar>(R.id.fontSizeBar).apply {
            progress = prefsManager.getInt("sudaku_font_size", 16) - 10
            setOnSeekBarChangeListener(this@TestActivity)
        }

        fontTextView = findViewById<TextView>(R.id.fontSize).apply {
            text = prefsManager.getInt("sudaku_font_size", 16).toString()
        }

        findViewById<Switch>(R.id.extraTolerance).apply {
            isChecked = prefsManager.getBoolean("sudaku_extra_tolerance", false)
            setOnCheckedChangeListener(this@TestActivity)
        }
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        when (seekBar!!.id) {
            R.id.alignment -> {
                prefsManager.edit().putInt("sudaku_grid_alignment", seekBar.progress).commit()
            }
            R.id.fontSizeBar -> {
                prefsManager.edit().putInt("sudaku_font_size", seekBar.progress + 10).commit()
                fontTextView.text = (seekBar.progress + 10).toString()
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        prefsManager.edit().putBoolean("sudaku_extra_tolerance", isChecked).commit()
    }

}
