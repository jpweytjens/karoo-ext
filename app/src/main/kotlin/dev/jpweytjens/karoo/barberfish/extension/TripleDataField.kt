/**
 * Copyright (c) 2025 SRAM LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package dev.jpweytjens.karoo.barberfish.extension

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.widget.RemoteViews
import dev.jpweytjens.karoo.barberfish.R
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.OnStreamState
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.UserProfile
import io.hammerhead.karooext.models.ViewConfig

/**
 * Enhanced triple data field that displays three configurable Karoo data fields in equal-width columns.
 * Uses native Karoo formatting and zone colors for each field.
 * Users can configure data types, smoothing variants, and zone display through the extension settings.
 */
class TripleDataField(
    extension: String,
    private val karooSystem: KarooSystemService,
    private val context: Context,
) : DataTypeImpl(extension, "triple") {

    companion object {
        private const val PREFS_NAME = "TripleDataFieldPrefs"
        private const val KEY_FIELD_1 = "field_1"
        private const val KEY_FIELD_2 = "field_2"
        private const val KEY_FIELD_3 = "field_3"
        private const val KEY_FIELD_1_VARIANT = "field_1_variant"
        private const val KEY_FIELD_2_VARIANT = "field_2_variant"
        private const val KEY_FIELD_3_VARIANT = "field_3_variant"
        private const val KEY_FIELD_1_ZONE_DISPLAY = "field_1_zone_display"
        private const val KEY_FIELD_2_ZONE_DISPLAY = "field_2_zone_display"
        private const val KEY_FIELD_3_ZONE_DISPLAY = "field_3_zone_display"
        private const val DEFAULT_FIELD_1 = DataType.Type.SPEED
        private const val DEFAULT_FIELD_2 = DataType.Type.HEART_RATE
        private const val DEFAULT_FIELD_3 = DataType.Type.POWER
        private const val DEFAULT_VARIANT = "raw"
        private const val DEFAULT_ZONE_DISPLAY = "none"
    }

    enum class SmoothingVariant {
        RAW,
        SMOOTH_3S,
        SMOOTH_5S,
        SMOOTH_10S,
        SMOOTH_30S,
    }

    enum class ZoneDisplay {
        NONE,
        COLOR,
        NUMBER,
        BOTH,
    }

    data class EnhancedFieldConfig(
        val dataType: String,
        val variant: SmoothingVariant,
        val zoneDisplay: ZoneDisplay,
    )

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Current values for each field
    private var field1Value: Double = Double.NaN
    private var field2Value: Double = Double.NaN
    private var field3Value: Double = Double.NaN

    // Current zone values for each field
    private var field1Zone: Int = 0
    private var field2Zone: Int = 0
    private var field3Zone: Int = 0

    // Enhanced field configurations
    private var field1Config: EnhancedFieldConfig = EnhancedFieldConfig(DEFAULT_FIELD_1, SmoothingVariant.RAW, ZoneDisplay.NONE)
    private var field2Config: EnhancedFieldConfig = EnhancedFieldConfig(DEFAULT_FIELD_2, SmoothingVariant.RAW, ZoneDisplay.NONE)
    private var field3Config: EnhancedFieldConfig = EnhancedFieldConfig(DEFAULT_FIELD_3, SmoothingVariant.RAW, ZoneDisplay.NONE)

    // User profile for zone colors
    private var userProfile: UserProfile? = null

    init {
        loadFieldConfiguration()
    }

    // Not needed for graphical data types

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        karooSystem.connect()

        // Create RemoteViews for triple layout
        val remoteViews = RemoteViews(context.packageName, R.layout.triple_data_field)

        // Use native text sizing from ViewConfig
        val valueTextSize = config.textSize.toFloat()
        val labelTextSize = (config.textSize * 0.55f) // ~55% of main text size for labels

        // Apply native text sizes to all value fields
        remoteViews.setTextViewTextSize(R.id.field1_value, android.util.TypedValue.COMPLEX_UNIT_SP, valueTextSize)
        remoteViews.setTextViewTextSize(R.id.field2_value, android.util.TypedValue.COMPLEX_UNIT_SP, valueTextSize)
        remoteViews.setTextViewTextSize(R.id.field3_value, android.util.TypedValue.COMPLEX_UNIT_SP, valueTextSize)

        // Apply native text sizes to all label fields
        remoteViews.setTextViewTextSize(R.id.field1_label, android.util.TypedValue.COMPLEX_UNIT_SP, labelTextSize)
        remoteViews.setTextViewTextSize(R.id.field2_label, android.util.TypedValue.COMPLEX_UNIT_SP, labelTextSize)
        remoteViews.setTextViewTextSize(R.id.field3_label, android.util.TypedValue.COMPLEX_UNIT_SP, labelTextSize)

        // Configure as graphical (hide header if desired)
        emitter.onNext(UpdateGraphicConfig(showHeader = false))

        // Subscribe to user profile for zone colors
        val userProfileConsumer = karooSystem.addConsumer<UserProfile>(
            UserProfile.Params,
        ) { profile ->
            userProfile = profile
            updateTripleView(remoteViews, emitter)
        }

        // Subscribe to field 1
        val field1DataType = getDataTypeForConfig(field1Config)
        val consumer1 = karooSystem.addConsumer<OnStreamState>(
            OnStreamState.StartStreaming(field1DataType),
        ) { streamState ->
            val state = streamState.state
            if (state is StreamState.Streaming) {
                field1Value = state.dataPoint.values[getFieldForType(field1DataType)] ?: Double.NaN
                // Extract zone information if available
                field1Zone = extractZoneFromStreamState(state, field1Config.dataType)
                updateTripleView(remoteViews, emitter)
            }
        }

        // Subscribe to field 2
        val field2DataType = getDataTypeForConfig(field2Config)
        val consumer2 = karooSystem.addConsumer<OnStreamState>(
            OnStreamState.StartStreaming(field2DataType),
        ) { streamState ->
            val state = streamState.state
            if (state is StreamState.Streaming) {
                field2Value = state.dataPoint.values[getFieldForType(field2DataType)] ?: Double.NaN
                field2Zone = extractZoneFromStreamState(state, field2Config.dataType)
                updateTripleView(remoteViews, emitter)
            }
        }

        // Subscribe to field 3
        val field3DataType = getDataTypeForConfig(field3Config)
        val consumer3 = karooSystem.addConsumer<OnStreamState>(
            OnStreamState.StartStreaming(field3DataType),
        ) { streamState ->
            val state = streamState.state
            if (state is StreamState.Streaming) {
                field3Value = state.dataPoint.values[getFieldForType(field3DataType)] ?: Double.NaN
                field3Zone = extractZoneFromStreamState(state, field3Config.dataType)
                updateTripleView(remoteViews, emitter)
            }
        }

        // Set up cleanup
        emitter.setCancellable {
            karooSystem.removeConsumer(userProfileConsumer)
            karooSystem.removeConsumer(consumer1)
            karooSystem.removeConsumer(consumer2)
            karooSystem.removeConsumer(consumer3)
            karooSystem.disconnect()
        }

        // Initial view update
        updateTripleView(remoteViews, emitter)
    }

    private fun updateTripleView(remoteViews: RemoteViews, emitter: ViewEmitter) {
        // Update field 1
        val field1Text = formatFieldValueWithZone(field1Value, field1Zone, field1Config)
        remoteViews.setTextViewText(R.id.field1_value, field1Text)
        remoteViews.setTextViewText(R.id.field1_label, getFieldLabel(field1Config.dataType))
        applyZoneStyling(remoteViews, R.id.field1_value, field1Zone, field1Config)

        // Update field 2
        val field2Text = formatFieldValueWithZone(field2Value, field2Zone, field2Config)
        remoteViews.setTextViewText(R.id.field2_value, field2Text)
        remoteViews.setTextViewText(R.id.field2_label, getFieldLabel(field2Config.dataType))
        applyZoneStyling(remoteViews, R.id.field2_value, field2Zone, field2Config)

        // Update field 3
        val field3Text = formatFieldValueWithZone(field3Value, field3Zone, field3Config)
        remoteViews.setTextViewText(R.id.field3_value, field3Text)
        remoteViews.setTextViewText(R.id.field3_label, getFieldLabel(field3Config.dataType))
        applyZoneStyling(remoteViews, R.id.field3_value, field3Zone, field3Config)

        emitter.updateView(remoteViews)
    }

    private fun applyZoneStyling(remoteViews: RemoteViews, viewId: Int, zone: Int, config: EnhancedFieldConfig) {
        when (config.zoneDisplay) {
            ZoneDisplay.COLOR, ZoneDisplay.BOTH -> {
                if (zone > 0) {
                    // Apply zone background color like native UI
                    val zoneColor = getZoneColor(zone, config.dataType)
                    remoteViews.setInt(viewId, "setBackgroundColor", zoneColor)
                    remoteViews.setTextColor(viewId, android.graphics.Color.WHITE)
                } else {
                    // No zone data - use default styling
                    remoteViews.setInt(viewId, "setBackgroundColor", android.graphics.Color.TRANSPARENT)
                    remoteViews.setTextColor(viewId, android.graphics.Color.WHITE)
                }
            }
            ZoneDisplay.NONE, ZoneDisplay.NUMBER -> {
                // No zone coloring - use default styling
                remoteViews.setInt(viewId, "setBackgroundColor", android.graphics.Color.TRANSPARENT)
                remoteViews.setTextColor(viewId, android.graphics.Color.WHITE)
            }
        }
    }

    private fun formatFieldValueWithZone(value: Double, zone: Int, config: EnhancedFieldConfig): String {
        if (value.isNaN()) return "--"

        val formattedValue = when (config.dataType) {
            DataType.Type.SPEED, DataType.Type.AVERAGE_SPEED, DataType.Type.MAX_SPEED -> {
                "%.1f".format(value)
            }
            DataType.Type.HEART_RATE, DataType.Type.AVERAGE_HR, DataType.Type.MAX_HR -> {
                "%.0f".format(value)
            }
            DataType.Type.POWER, DataType.Type.AVERAGE_POWER, DataType.Type.MAX_POWER -> {
                "%.0f".format(value)
            }
            DataType.Type.CADENCE, DataType.Type.AVERAGE_CADENCE, DataType.Type.MAX_CADENCE -> {
                "%.0f".format(value)
            }
            DataType.Type.DISTANCE -> {
                "%.2f".format(value / 1000.0) // Convert meters to km
            }
            DataType.Type.ELAPSED_TIME, DataType.Type.RIDE_TIME -> {
                formatTime(value)
            }
            else -> "%.1f".format(value)
        }

        // Add zone information if configured
        return when (config.zoneDisplay) {
            ZoneDisplay.NONE -> formattedValue
            ZoneDisplay.COLOR -> formattedValue // Color handled separately
            ZoneDisplay.NUMBER -> if (zone > 0) "$formattedValue Z$zone" else formattedValue
            ZoneDisplay.BOTH -> if (zone > 0) "$formattedValue Z$zone" else formattedValue
        }
    }

    private fun getFieldLabel(dataType: String): String {
        return when (dataType) {
            DataType.Type.SPEED -> "SPD"
            DataType.Type.AVERAGE_SPEED -> "AVG SPD"
            DataType.Type.MAX_SPEED -> "MAX SPD"
            DataType.Type.HEART_RATE -> "HR"
            DataType.Type.AVERAGE_HR -> "AVG HR"
            DataType.Type.MAX_HR -> "MAX HR"
            DataType.Type.POWER -> "PWR"
            DataType.Type.AVERAGE_POWER -> "AVG PWR"
            DataType.Type.MAX_POWER -> "MAX PWR"
            DataType.Type.CADENCE -> "CAD"
            DataType.Type.AVERAGE_CADENCE -> "AVG CAD"
            DataType.Type.MAX_CADENCE -> "MAX CAD"
            DataType.Type.DISTANCE -> "DIST"
            DataType.Type.ELAPSED_TIME -> "TIME"
            DataType.Type.RIDE_TIME -> "RIDE"
            DataType.Type.TEMPERATURE -> "TEMP"
            DataType.Type.ELEVATION_GAIN -> "ELEV+"
            else -> dataType.take(6).uppercase()
        }
    }

    private fun getZoneColor(zone: Int, dataType: String): Int {
        val profile = userProfile
        if (profile == null || zone <= 0) {
            return Color.WHITE
        }

        // Use actual zone colors from user profile
        return when (dataType) {
            DataType.Type.HEART_RATE, DataType.Type.AVERAGE_HR, DataType.Type.MAX_HR -> {
                getZoneColorFromProfile(zone, profile.heartRateZones)
            }
            DataType.Type.POWER, DataType.Type.AVERAGE_POWER, DataType.Type.MAX_POWER -> {
                getZoneColorFromProfile(zone, profile.powerZones)
            }
            else -> Color.WHITE
        }
    }

    private fun getZoneColorFromProfile(zone: Int, zones: List<UserProfile.Zone>): Int {
        // Use predefined zone colors that match Karoo's color scheme
        return when (zone) {
            1 -> Color.parseColor("#808080") // Gray
            2 -> Color.parseColor("#0080FF") // Blue
            3 -> Color.parseColor("#00FF00") // Green
            4 -> Color.parseColor("#FFFF00") // Yellow
            5 -> Color.parseColor("#FF8000") // Orange
            6 -> Color.parseColor("#FF0000") // Red
            7 -> Color.parseColor("#FF00FF") // Magenta
            else -> Color.WHITE
        }
    }

    private fun getFieldForType(dataType: String): String {
        return when (dataType) {
            DataType.Type.SPEED -> DataType.Field.SPEED
            DataType.Type.AVERAGE_SPEED -> DataType.Field.AVERAGE_SPEED
            DataType.Type.MAX_SPEED -> DataType.Field.MAX_SPEED
            DataType.Type.HEART_RATE -> DataType.Field.HEART_RATE
            DataType.Type.AVERAGE_HR -> DataType.Field.AVG_HR
            DataType.Type.MAX_HR -> DataType.Field.MAX_HR
            DataType.Type.POWER -> DataType.Field.POWER
            DataType.Type.AVERAGE_POWER -> DataType.Field.AVERAGE_POWER
            DataType.Type.MAX_POWER -> DataType.Field.MAX_POWER
            DataType.Type.CADENCE -> DataType.Field.CADENCE
            DataType.Type.AVERAGE_CADENCE -> DataType.Field.AVERAGE_CADENCE
            DataType.Type.MAX_CADENCE -> DataType.Field.MAX_CADENCE
            DataType.Type.DISTANCE -> DataType.Field.DISTANCE
            DataType.Type.ELAPSED_TIME -> DataType.Field.ELAPSED_TIME
            DataType.Type.RIDE_TIME -> DataType.Field.RIDE_TIME
            DataType.Type.TEMPERATURE -> DataType.Field.TEMPERATURE
            DataType.Type.ELEVATION_GAIN -> DataType.Field.ELEVATION_GAIN
            else -> DataType.Field.SPEED // Default fallback
        }
    }

    private fun formatTime(seconds: Double): String {
        val hours = (seconds / 3600).toInt()
        val minutes = ((seconds % 3600) / 60).toInt()
        return if (hours > 0) {
            "$hours:${minutes.toString().padStart(2, '0')}"
        } else {
            "$minutes:${(seconds % 60).toInt().toString().padStart(2, '0')}"
        }
    }

    private fun loadFieldConfiguration() {
        field1Config = EnhancedFieldConfig(
            prefs.getString(KEY_FIELD_1, DEFAULT_FIELD_1) ?: DEFAULT_FIELD_1,
            SmoothingVariant.valueOf(prefs.getString(KEY_FIELD_1_VARIANT, DEFAULT_VARIANT)?.uppercase() ?: DEFAULT_VARIANT.uppercase()),
            ZoneDisplay.valueOf(prefs.getString(KEY_FIELD_1_ZONE_DISPLAY, DEFAULT_ZONE_DISPLAY)?.uppercase() ?: DEFAULT_ZONE_DISPLAY.uppercase()),
        )

        field2Config = EnhancedFieldConfig(
            prefs.getString(KEY_FIELD_2, DEFAULT_FIELD_2) ?: DEFAULT_FIELD_2,
            SmoothingVariant.valueOf(prefs.getString(KEY_FIELD_2_VARIANT, DEFAULT_VARIANT)?.uppercase() ?: DEFAULT_VARIANT.uppercase()),
            ZoneDisplay.valueOf(prefs.getString(KEY_FIELD_2_ZONE_DISPLAY, DEFAULT_ZONE_DISPLAY)?.uppercase() ?: DEFAULT_ZONE_DISPLAY.uppercase()),
        )

        field3Config = EnhancedFieldConfig(
            prefs.getString(KEY_FIELD_3, DEFAULT_FIELD_3) ?: DEFAULT_FIELD_3,
            SmoothingVariant.valueOf(prefs.getString(KEY_FIELD_3_VARIANT, DEFAULT_VARIANT)?.uppercase() ?: DEFAULT_VARIANT.uppercase()),
            ZoneDisplay.valueOf(prefs.getString(KEY_FIELD_3_ZONE_DISPLAY, DEFAULT_ZONE_DISPLAY)?.uppercase() ?: DEFAULT_ZONE_DISPLAY.uppercase()),
        )
    }

    private fun getDataTypeForConfig(config: EnhancedFieldConfig): String {
        return when (config.variant) {
            SmoothingVariant.RAW -> config.dataType
            SmoothingVariant.SMOOTH_3S -> getSmoothedDataType(config.dataType, "3s")
            SmoothingVariant.SMOOTH_5S -> getSmoothedDataType(config.dataType, "5s")
            SmoothingVariant.SMOOTH_10S -> getSmoothedDataType(config.dataType, "10s")
            SmoothingVariant.SMOOTH_30S -> getSmoothedDataType(config.dataType, "30s")
        }
    }

    private fun getSmoothedDataType(baseType: String, interval: String): String {
        return when (baseType) {
            DataType.Type.SPEED -> when (interval) {
                "3s" -> DataType.Type.SMOOTHED_3S_AVERAGE_SPEED
                "5s" -> DataType.Type.SMOOTHED_5S_AVERAGE_SPEED
                "10s" -> DataType.Type.SMOOTHED_10S_AVERAGE_SPEED
                else -> baseType
            }
            DataType.Type.POWER -> when (interval) {
                "3s" -> DataType.Type.SMOOTHED_3S_AVERAGE_POWER
                "5s" -> DataType.Type.SMOOTHED_5S_AVERAGE_POWER
                "10s" -> DataType.Type.SMOOTHED_10S_AVERAGE_POWER
                "30s" -> DataType.Type.SMOOTHED_30S_AVERAGE_POWER
                else -> baseType
            }
            DataType.Type.CADENCE -> when (interval) {
                "3s" -> DataType.Type.SMOOTHED_3S_AVERAGE_CADENCE
                "5s" -> DataType.Type.SMOOTHED_5S_AVERAGE_CADENCE
                "10s" -> DataType.Type.SMOOTHED_10S_AVERAGE_CADENCE
                else -> baseType
            }
            else -> baseType
        }
    }

    private fun extractZoneFromStreamState(state: StreamState.Streaming, dataType: String): Int {
        return when (dataType) {
            DataType.Type.HEART_RATE, DataType.Type.AVERAGE_HR, DataType.Type.MAX_HR -> {
                state.dataPoint.values[DataType.Field.HR_ZONE]?.toInt() ?: 0
            }
            DataType.Type.POWER, DataType.Type.AVERAGE_POWER, DataType.Type.MAX_POWER -> {
                state.dataPoint.values[DataType.Field.POWER_ZONE]?.toInt() ?: 0
            }
            else -> 0
        }
    }

    /** Get current field configuration. */
    fun getFieldConfiguration(): Triple<EnhancedFieldConfig, EnhancedFieldConfig, EnhancedFieldConfig> {
        return Triple(field1Config, field2Config, field3Config)
    }

    /** Set field configuration. */
    fun setFieldConfiguration(field1: EnhancedFieldConfig, field2: EnhancedFieldConfig, field3: EnhancedFieldConfig) {
        field1Config = field1
        field2Config = field2
        field3Config = field3

        prefs.edit()
            .putString(KEY_FIELD_1, field1.dataType)
            .putString(KEY_FIELD_1_VARIANT, field1.variant.name.lowercase())
            .putString(KEY_FIELD_1_ZONE_DISPLAY, field1.zoneDisplay.name.lowercase())
            .putString(KEY_FIELD_2, field2.dataType)
            .putString(KEY_FIELD_2_VARIANT, field2.variant.name.lowercase())
            .putString(KEY_FIELD_2_ZONE_DISPLAY, field2.zoneDisplay.name.lowercase())
            .putString(KEY_FIELD_3, field3.dataType)
            .putString(KEY_FIELD_3_VARIANT, field3.variant.name.lowercase())
            .putString(KEY_FIELD_3_ZONE_DISPLAY, field3.zoneDisplay.name.lowercase())
            .apply()
    }

    /** Get available smoothing variants for a data type. */
    fun getAvailableVariants(dataType: String): List<SmoothingVariant> {
        return when (dataType) {
            DataType.Type.SPEED -> listOf(SmoothingVariant.RAW, SmoothingVariant.SMOOTH_3S, SmoothingVariant.SMOOTH_5S, SmoothingVariant.SMOOTH_10S)
            DataType.Type.POWER -> listOf(SmoothingVariant.RAW, SmoothingVariant.SMOOTH_3S, SmoothingVariant.SMOOTH_5S, SmoothingVariant.SMOOTH_10S, SmoothingVariant.SMOOTH_30S)
            DataType.Type.CADENCE -> listOf(SmoothingVariant.RAW, SmoothingVariant.SMOOTH_3S, SmoothingVariant.SMOOTH_5S, SmoothingVariant.SMOOTH_10S)
            DataType.Type.HEART_RATE -> listOf(SmoothingVariant.RAW) // HR typically doesn't need smoothing
            else -> listOf(SmoothingVariant.RAW)
        }
    }

    /** Get available zone display options for a data type. */
    fun getAvailableZoneDisplays(dataType: String): List<ZoneDisplay> {
        return when (dataType) {
            DataType.Type.HEART_RATE, DataType.Type.AVERAGE_HR, DataType.Type.MAX_HR,
            DataType.Type.POWER, DataType.Type.AVERAGE_POWER, DataType.Type.MAX_POWER,
            -> {
                listOf(ZoneDisplay.NONE, ZoneDisplay.COLOR, ZoneDisplay.NUMBER, ZoneDisplay.BOTH)
            }
            else -> listOf(ZoneDisplay.NONE)
        }
    }
}
