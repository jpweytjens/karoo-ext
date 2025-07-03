/**
 * Copyright (c) 2025 SRAM LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.jpweytjens.karoo.barberfish.extension

import android.graphics.Color
import androidx.annotation.ColorInt
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.models.DataPoint
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.UserProfile

/**
 * Abstract base class for custom data types that provides zone-based color coding
 * and proper handling of missing sensor data (NaN values).
 */
abstract class BespokeDataType(
    extension: String,
    typeId: String,
) : DataTypeImpl(extension, typeId) {

    protected var userProfile: UserProfile? = null
    protected var lastValue: Double = Double.NaN

    /**
     * Determines the appropriate color for a given value based on user profile zones.
     * Returns null if no zone-based coloring should be applied.
     */
    @ColorInt
    protected fun getZoneColor(value: Double, dataType: String): Int? {
        if (value.isNaN()) return Color.GRAY

        val profile = userProfile ?: return null
        val zones = when (dataType) {
            DataType.Type.POWER -> profile.powerZones
            DataType.Type.HEART_RATE -> profile.heartRateZones
            else -> return null
        }

        val zone = zones.find { value >= it.min && value <= it.max }
        return zone?.let { getColorForZone(zones.indexOf(it)) }
    }

    /**
     * Returns the color for a specific zone index.
     * Override this method to customize zone colors.
     */
    @ColorInt
    protected open fun getColorForZone(zoneIndex: Int): Int {
        return when (zoneIndex) {
            0 -> Color.parseColor("#808080") // Zone 1: Gray
            1 -> Color.parseColor("#0000FF") // Zone 2: Blue
            2 -> Color.parseColor("#00FF00") // Zone 3: Green
            3 -> Color.parseColor("#FFFF00") // Zone 4: Yellow
            4 -> Color.parseColor("#FFA500") // Zone 5: Orange
            5 -> Color.parseColor("#FF0000") // Zone 6: Red
            else -> Color.parseColor("#FF00FF") // Zone 7+: Magenta
        }
    }

    /**
     * Formats a value for display, showing "NaN" when no sensor data is available.
     */
    protected fun formatValue(value: Double): String {
        return if (value.isNaN()) "NaN" else value.toInt().toString()
    }

    /**
     * Creates a DataPoint with the given value, handling NaN appropriately.
     */
    protected fun createDataPoint(value: Double): DataPoint {
        lastValue = value
        return DataPoint(
            dataTypeId,
            values = mapOf(DataType.Field.SINGLE to if (value.isNaN()) 0.0 else value),
        )
    }

    /**
     * Override this method to provide the actual sensor data.
     * Return Double.NaN when no data is available.
     */
    protected abstract fun getCurrentValue(): Double

    /**
     * Override this method to specify the data type for zone color coding.
     */
    protected abstract fun getDataType(): String

    /**
     * Updates the user profile when available.
     */
    protected fun updateUserProfile(profile: UserProfile) {
        userProfile = profile
    }

    /**
     * Checks if the current value is within valid sensor range.
     */
    protected fun isValidSensorValue(value: Double): Boolean {
        return !value.isNaN() && value >= 0
    }
}
