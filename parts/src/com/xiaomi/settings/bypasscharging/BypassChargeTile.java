package com.xiaomi.settings.bypasscharging;

import android.content.SharedPreferences;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import androidx.preference.PreferenceManager;

import com.xiaomi.settings.R;

public class BypassChargeTile extends TileService
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String PREF_BYPASS_CHARGE = "bypass_charge";

    private ChargeUtils mChargeUtils;
    private SharedPreferences mPrefs;

    @Override
    public void onCreate() {
        super.onCreate();
        mChargeUtils = new ChargeUtils(this);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        mPrefs.registerOnSharedPreferenceChangeListener(this);
        // Sync once when the panel opens (e.g. after a reboot reset the node)
        if (mChargeUtils.isBypassChargeSupported()) {
            mPrefs.edit().putBoolean(PREF_BYPASS_CHARGE,
                    mChargeUtils.isBypassChargeEnabled()).apply();
        }
        updateTile();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
        mPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (PREF_BYPASS_CHARGE.equals(key)) {
            updateTile();
        }
    }

    @Override
    public void onClick() {
        super.onClick();

        if (mChargeUtils.isBypassChargeSupported()) {
            // Same call the Settings toggle uses; it also updates the shared preference
            mChargeUtils.enableBypassCharge(!mPrefs.getBoolean(PREF_BYPASS_CHARGE, false));
        }
        updateTile();
    }

    private void updateTile() {
        Tile tile = getQsTile();
        if (tile == null) return;

        tile.setLabel(getString(R.string.charge_bypass_title));

        if (!mChargeUtils.isBypassChargeSupported()) {
            tile.setState(Tile.STATE_UNAVAILABLE);
            tile.setSubtitle(getString(R.string.charge_bypass_unavailable));
        } else {
            boolean enabled = mPrefs.getBoolean(PREF_BYPASS_CHARGE, false);
            tile.setState(enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
            tile.setSubtitle(null);
        }

        tile.updateTile();
    }
}
