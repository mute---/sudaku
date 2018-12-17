package org.klava.sudaku

import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.*
import android.widget.CompoundButton.OnCheckedChangeListener
import kotlinx.android.synthetic.main.activity_test.*

class TestActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener, OnCheckedChangeListener, AdapterView.OnItemSelectedListener {
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

        ArrayAdapter.createFromResource(this, R.array.touch_zones, R.layout.support_simple_spinner_dropdown_item)
            .also {
                it.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                zonesSpinner.adapter = it
            }
        zonesSpinner.onItemSelectedListener = this
        zonesSpinner.setSelection(prefsManager.getInt("sudaku_touch_zones_variant", 0))
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

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        prefsManager.edit().putInt("sudaku_touch_zones_variant", position).apply()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

}
