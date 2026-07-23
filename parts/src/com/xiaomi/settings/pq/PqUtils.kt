/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.pq

import android.os.SystemProperties
import android.util.Log

/**
 * MediaTek MiraVision (PQ) vezérlő — klee (MT6899).
 *
 * A vendor PQ szolgáltatás persist.vendor.sys.pq.* property-kből olvassa a
 * feature-kapcsolókat (a getprop-felderítés igazolta a kulcsokat és a
 * vendor_mtk_pq_prop kontextust). A persist prefix miatt a beállítások
 * bootok közt maguktól megmaradnak — nincs szükség Startup-visszaállításra.
 *
 * Sepolicy előfeltétel a system_app.te-ben:
 *   set_prop(system_app, vendor_mtk_pq_prop)
 *   get_prop(system_app, vendor_mtk_pq_prop)
 *
 * MEMC (MJC) és AI SDR->HDR ezen a SoC-on NINCS licencelve
 * (ro.vendor.pq.mtk_video_transition=0, mtk_ai_sdr_to_hdr_support=0),
 * ezért szándékosan nem szerepelnek.
 */
object PqUtils {

    private const val TAG = "PqUtils"

    // --- támogatottsági flagek (ro, csak olvasás) ---
    private const val RO_SR_SUPPORT = "ro.vendor.pq.mtk_ultra_resolution_support"
    private const val RO_AIPQ_SUPPORT = "ro.vendor.pq.mtk_ai_region_pq_support"
    private const val RO_TDSHP_SUPPORT = "ro.vendor.pq.mtk_disp_tdshp_support"
    private const val RO_VIDEO_HDR_SUPPORT = "ro.vendor.pq.mtk_video_hdr_support"

    // --- vezérlő property-k (vendor oldal: OLVASÁS innen) ---
    const val PROP_SR_EN = "persist.vendor.sys.pq.ultrares.en"
    const val PROP_SR_STRENGTH = "persist.vendor.sys.pq.ultrares.strength"
    const val PROP_AIPQ_EN = "persist.vendor.sys.pq.airegionpq_adaptive.en"
    const val PROP_SHP_EN = "persist.vendor.sys.pq.shp.en"
    const val PROP_SHP_STRENGTH = "persist.vendor.sys.pq.shp.strength"
    const val PROP_HDR_EN = "persist.vendor.sys.pq.hdr.en"

    // --- híd: az ÍRÁS a saját tükör-propokba megy, az init trigger másolja
    // át a vendor propokba (a platform neverallow tiltja az appdomain
    // vendor-set_prop-ját, ez a szabványos kerülőút) ---
    private val MIRROR = mapOf(
        PROP_SR_EN to "persist.pocoparts.pq.sr_en",
        PROP_SR_STRENGTH to "persist.pocoparts.pq.sr_str",
        PROP_AIPQ_EN to "persist.pocoparts.pq.aipq_en",
        PROP_SHP_EN to "persist.pocoparts.pq.shp_en",
        PROP_SHP_STRENGTH to "persist.pocoparts.pq.shp_str",
        PROP_HDR_EN to "persist.pocoparts.pq.hdr_en"
    )

    private fun supportFlag(key: String): Boolean {
        val v = SystemProperties.get(key, "")
        return v == "1" || v.equals("true", ignoreCase = true)
    }

    fun isSrSupported() = supportFlag(RO_SR_SUPPORT)
    fun isAiPqSupported() = supportFlag(RO_AIPQ_SUPPORT)
    fun isSharpnessSupported() = supportFlag(RO_TDSHP_SUPPORT)
    fun isVideoHdrSupported() = supportFlag(RO_VIDEO_HDR_SUPPORT)

    /** Legalább egy PQ-feature elérhető ezen a buildön. */
    fun isAnySupported() =
        isSrSupported() || isAiPqSupported() || isSharpnessSupported() || isVideoHdrSupported()

    fun getBool(prop: String, def: Boolean = false): Boolean =
        SystemProperties.get(prop, if (def) "1" else "0") == "1"

    fun getInt(prop: String, def: Int): Int =
        SystemProperties.get(prop, def.toString()).toIntOrNull() ?: def

    fun setBool(prop: String, value: Boolean): Boolean = setRaw(prop, if (value) "1" else "0")

    fun setInt(prop: String, value: Int): Boolean = setRaw(prop, value.toString())

    private fun setRaw(prop: String, value: String): Boolean = try {
        val mirror = MIRROR[prop] ?: run {
            Log.e(TAG, "no mirror prop for $prop")
            return false
        }
        SystemProperties.set(mirror, value)
        // a tükör-prop visszaolvasása igazolja a saját írást; a vendor
        // oldalt az init trigger tölti át (aszinkron, gyakorlatilag azonnali)
        val ok = SystemProperties.get(mirror, "") == value
        Log.i(TAG, "set $mirror=$value -> ${if (ok) "OK" else "FAILED (pocoparts_prop sepolicy?)"}")
        ok
    } catch (e: Exception) {
        Log.e(TAG, "set $prop=$value threw", e)
        false
    }
}
