/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.utils

import android.os.IBinder
import android.os.Parcel
import android.os.ServiceManager
import android.os.SystemProperties
import android.util.Log

/**
 * Forces SurfaceFlinger to composite everything on the GPU instead of the
 * hardware composer (HWC).
 *
 * Equivalent to: service call SurfaceFlinger 1008 i32 <1|0>
 *
 * Workaround for washed-out / faded video colors caused by the MiraVision PQ
 * path on the HWC. SurfaceFlinger only accepts this debug transaction from
 * system/graphics/root and forgets it whenever it restarts, so the state is
 * kept in a persist property and re-applied at every boot.
 *
 * Enabled by default: if the property was never set, it is treated as on.
 * Switching it off stores "0", which is respected from then on.
 *
 * The property lives under persist.pocoparts.pq. which is already labeled
 * pocoparts_prop and writable by system_app, so no new sepolicy is needed.
 */
object SfUtils {

    private const val TAG = "SfUtils"

    private const val PROP_GPU_COMP = "persist.pocoparts.pq.gpu_comp"
    private const val SF_SERVICE = "SurfaceFlinger"
    private const val SF_DESCRIPTOR = "android.ui.ISurfaceComposer"
    private const val SF_CODE_DISABLE_HWC = 1008
    private const val DEFAULT_GPU_COMP = true

    fun isGpuCompositionEnabled(): Boolean = SystemProperties.getBoolean(PROP_GPU_COMP, DEFAULT_GPU_COMP)

    /** Applies the setting now and remembers it for the next boots. */
    fun setGpuComposition(enable: Boolean): Boolean {
        if (!transact(enable)) return false
        SystemProperties.set(PROP_GPU_COMP, if (enable) "1" else "0")
        return true
    }

    /** Re-applies the saved state; call once per boot. */
    fun applyOnBoot() {
        if (isGpuCompositionEnabled()) {
            Log.i(TAG, "Re-applying GPU composition")
            transact(true)
        }
    }

    private fun transact(enable: Boolean): Boolean {
        val sf: IBinder = ServiceManager.getService(SF_SERVICE) ?: run {
            Log.e(TAG, "SurfaceFlinger service not found")
            return false
        }
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        return try {
            data.writeInterfaceToken(SF_DESCRIPTOR)
            data.writeInt(if (enable) 1 else 0)
            val ok = sf.transact(SF_CODE_DISABLE_HWC, data, reply, 0)
            Log.i(TAG, "SurfaceFlinger 1008 i32 ${if (enable) 1 else 0} -> $ok")
            ok
        } catch (e: Exception) {
            Log.e(TAG, "SurfaceFlinger transaction failed", e)
            false
        } finally {
            data.recycle()
            reply.recycle()
        }
    }
}
