/* =========================================================================

    Compass Navigator
    Copyright (C) 2019,2022 Draekko, Benoit Touchette

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.

   ========================================================================= */

package com.draekko.compassnavigator;

import android.content.SharedPreferences;

public class Settings {

    public static final String ENABLE_NIGHTMODE_KEY = "enable_night_mode";
    public static final String ENABLE_ALTROSE_KEY = "enable_alternate_rose";
    public static final String ENABLE_GPSDECL_KEY = "enable_gps_declination";
    public static final String BEARING_DIRECTION_KEY = "enable_bearing_direction";
    public static final String SHOW_CALIBRAION_KEY = "show_calibration_dialog";
    public static final String ENABLE_MANDECL_KEY = "enable_manual_declination";
    public static final String MANDECL_VALUE_KEY = "manual_declination_value";
    public static final String AUTOUPDECL_VALUE_KEY = "auto_update_declination_value";
    
    private static boolean enableNightMode;
    private static boolean enableAltRose;
    private static boolean enableGpsDeclination;
    private static boolean enableManualDeclination;
    private static int bearignDirection;
    private static boolean showCalibration;
    private static float manualDeclinationValue;
    private static boolean autoUpManDeclinationValue;

    public void load(SharedPreferences prefs) {
        enableNightMode = prefs.getBoolean(ENABLE_NIGHTMODE_KEY, false);
        enableAltRose = prefs.getBoolean(ENABLE_ALTROSE_KEY, false);
        enableGpsDeclination = prefs.getBoolean(ENABLE_GPSDECL_KEY, true);
        enableManualDeclination = prefs.getBoolean(ENABLE_MANDECL_KEY, false);
        showCalibration = prefs.getBoolean(SHOW_CALIBRAION_KEY, true);
        bearignDirection = prefs.getInt(BEARING_DIRECTION_KEY, 0);
        manualDeclinationValue = prefs.getFloat(MANDECL_VALUE_KEY, 0.0f);
        autoUpManDeclinationValue = prefs.getBoolean(AUTOUPDECL_VALUE_KEY, false);
    }

    public void save(SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        save(editor);
        editor.commit();
    }

    public void saveDeferred(SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        save(editor);
        editor.apply();
    }

    public void save(SharedPreferences.Editor editor) {
        editor.putBoolean(ENABLE_NIGHTMODE_KEY, enableNightMode);
        editor.putBoolean(ENABLE_ALTROSE_KEY, enableAltRose);
        editor.putBoolean(ENABLE_GPSDECL_KEY, enableGpsDeclination);
        editor.putBoolean(ENABLE_MANDECL_KEY, enableManualDeclination);
        editor.putBoolean(SHOW_CALIBRAION_KEY, showCalibration);
        editor.putBoolean(AUTOUPDECL_VALUE_KEY, autoUpManDeclinationValue);
        editor.putInt(BEARING_DIRECTION_KEY, bearignDirection);
        editor.putFloat(MANDECL_VALUE_KEY, manualDeclinationValue);
    }

    public static boolean getAutoUpdateManualDeclination() {
        return autoUpManDeclinationValue;
    }

    public static void setAutoUpdateManualDeclination(boolean value) {
        autoUpManDeclinationValue = value;
    }

    public static boolean getEnableAltRose() {
        return enableAltRose;
    }

    public static void setEnableAltRose(boolean value) {
        enableAltRose = value;
    }

    public static boolean getEnableNightMode() {
        return enableNightMode;
    }

    public static void setEnableNightMode(boolean value) {
        enableNightMode = value;
    }

    public static boolean getEnableGpsDeclination() {
        return enableGpsDeclination;
    }

    public static void setEnableGpsDeclination(boolean value) {
        enableGpsDeclination = value;
    }

    public static int getBearingDirection() {
        return bearignDirection;
    }

    public static void setBearingDirection(int value) {
        bearignDirection = value;
    }

    public static boolean getShowCalibration() {
        return showCalibration;
    }

    public static void setShowCalibration(boolean value) {
        showCalibration = value;
    }

    public static boolean getEnableManualDeclination() {
        return enableManualDeclination;
    }

    public static void setEnableManualDeclination(boolean value) {
        enableManualDeclination = value;
    }

    public static float getManualDeclinationValue() {
        return manualDeclinationValue;
    }

    public static void setManualDeclinationValue(float value) {
        manualDeclinationValue = value;
    }
}