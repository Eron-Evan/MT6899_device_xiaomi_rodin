/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.pq

import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity
import com.xiaomi.settings.CustomSeekBarPreference
import com.xiaomi.settings.R

class VisualEnhancementActivity : CollapsingToolbarBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager
            .beginTransaction()
            .replace(
                com.android.settingslib.collapsingtoolbar.R.id.content_frame,
                VisualEnhancementFragment(), "visual_enhancement"
            )
            .commit()
    }
}

class VisualEnhancementFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.visual_enhancement_settings, rootKey)

        // --- Super Resolution ---
        bindSwitch("pq_sr_enable", PqUtils.PROP_SR_EN, PqUtils.isSrSupported())
        bindSlider("pq_sr_strength", PqUtils.PROP_SR_STRENGTH, PqUtils.isSrSupported(), 5)

        // --- AI Region PQ ---
        bindSwitch("pq_aipq_enable", PqUtils.PROP_AIPQ_EN, PqUtils.isAiPqSupported())

        // --- Sharpness ---
        bindSwitch("pq_shp_enable", PqUtils.PROP_SHP_EN, PqUtils.isSharpnessSupported())
        bindSlider("pq_shp_strength", PqUtils.PROP_SHP_STRENGTH, PqUtils.isSharpnessSupported(), 4)

        // --- Video HDR ---
        bindSwitch("pq_hdr_enable", PqUtils.PROP_HDR_EN, PqUtils.isVideoHdrSupported())
    }

    /** Kapcsoló <-> property: UI a property aktuális értékéből indul. */
    private fun bindSwitch(key: String, prop: String, supported: Boolean) {
        val pref = findPreference<SwitchPreferenceCompat>(key) ?: return
        if (!supported) {
            pref.isEnabled = false
            pref.setSummary(R.string.pq_unsupported)
            return
        }
        pref.isChecked = PqUtils.getBool(prop)
        pref.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                val ok = PqUtils.setBool(prop, newValue as Boolean)
                if (!ok) toastFail()
                ok
            }
    }

    /** Csúszka <-> property. */
    private fun bindSlider(key: String, prop: String, supported: Boolean, def: Int) {
        val pref = findPreference<CustomSeekBarPreference>(key) ?: return
        if (!supported) {
            pref.isEnabled = false
            return
        }
        pref.setValue(PqUtils.getInt(prop, def))
        pref.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                val ok = PqUtils.setInt(prop, newValue as Int)
                if (!ok) toastFail()
                ok
            }
    }

    private fun toastFail() {
        Toast.makeText(requireContext(), R.string.pq_write_failed, Toast.LENGTH_SHORT).show()
    }
}
