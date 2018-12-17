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
import kotlinx.android.synthetic.main.activity_test.*

class TestActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener, OnCheckedChangeListener {

    private lateinit var prefsManager: SharedPreferences
    private lateinit var fontTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        prefsManager = PreferenceManager.getDefaultSharedPreferences(this)

        alignment.setOnSeekBarChangeListener(this)
        alignment.progress = prefsManager.getInt("sudaku_grid_alignment", 1)

        fontSizeBar.progress = prefsManager.getInt("sudaku_font_size", 16) - 10
        fontSizeBar.setOnSeekBarChangeListener(this)

        fontSize.text = prefsManager.getInt("sudaku_font_size", 16).toString()

        extraTolerance.isChecked = prefsManager.getBoolean("sudaku_extra_tolerance", false)
        extraTolerance.setOnCheckedChangeListener(this)

        useCenterDetection.isChecked = prefsManager.getBoolean("sudaku_use_center_detection", false)
        useCenterDetection.setOnCheckedChangeListener(this)
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        when (seekBar!!.id) {
            R.id.alignment -> {
                prefsManager.edit().putInt("sudaku_grid_alignment", seekBar.progress).apply()
            }
            R.id.fontSizeBar -> {
                prefsManager.edit().putInt("sudaku_font_size", seekBar.progress + 10).apply()
                fontTextView.text = (seekBar.progress + 10).toString()
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView!!.id) {
            R.id.extraTolerance -> prefsManager.edit().putBoolean("sudaku_extra_tolerance", isChecked).apply()
            R.id.useCenterDetection -> prefsManager.edit().putBoolean("sudaku_use_center_detection", isChecked).apply()
        }

    }

}
