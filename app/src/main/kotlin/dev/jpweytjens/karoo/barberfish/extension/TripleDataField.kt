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
import io.hammerhead.karooext.models.ViewConfig

/**
 * A triple data field that displays three configurable Karoo data fields in equal-width columns.
 * Users can configure which data fields to display through the extension settings.
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
        private const val DEFAULT_FIELD_1 = DataType.Type.SPEED
        private const val DEFAULT_FIELD_2 = DataType.Type.HEART_RATE
        private const val DEFAULT_FIELD_3 = DataType.Type.POWER
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Current values for each field
    private var field1Value: Double = Double.NaN
    private var field2Value: Double = Double.NaN
    private var field3Value: Double = Double.NaN

    // Configured data field types
    private var field1Type: String = DEFAULT_FIELD_1
    private var field2Type: String = DEFAULT_FIELD_2
    private var field3Type: String = DEFAULT_FIELD_3

    init {
        loadFieldConfiguration()
    }

    // Not needed for graphical data types

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        karooSystem.connect()

        // Create RemoteViews for triple layout
        val remoteViews = RemoteViews(context.packageName, R.layout.triple_data_field)

        // Configure as graphical (hide header if desired)
        emitter.onNext(UpdateGraphicConfig(showHeader = false))

        // Subscribe to field 1
        val consumer1 = karooSystem.addConsumer<OnStreamState>(
            OnStreamState.StartStreaming(field1Type),
        ) { streamState ->
            val state = streamState.state
            if (state is StreamState.Streaming) {
                field1Value = state.dataPoint.values[getFieldForType(field1Type)] ?: Double.NaN
                updateTripleView(remoteViews, emitter)
            }
        }

        // Subscribe to field 2
        val consumer2 = karooSystem.addConsumer<OnStreamState>(
            OnStreamState.StartStreaming(field2Type),
        ) { streamState ->
            val state = streamState.state
            if (state is StreamState.Streaming) {
                field2Value = state.dataPoint.values[getFieldForType(field2Type)] ?: Double.NaN
                updateTripleView(remoteViews, emitter)
            }
        }

        // Subscribe to field 3
        val consumer3 = karooSystem.addConsumer<OnStreamState>(
            OnStreamState.StartStreaming(field3Type),
        ) { streamState ->
            val state = streamState.state
            if (state is StreamState.Streaming) {
                field3Value = state.dataPoint.values[getFieldForType(field3Type)] ?: Double.NaN
                updateTripleView(remoteViews, emitter)
            }
        }

        // Set up cleanup
        emitter.setCancellable {
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
        remoteViews.setTextViewText(R.id.field1_value, formatFieldValue(field1Value, field1Type))
        remoteViews.setTextViewText(R.id.field1_label, getFieldLabel(field1Type))
        remoteViews.setTextColor(R.id.field1_value, getFieldColor(field1Value, field1Type))

        // Update field 2
        remoteViews.setTextViewText(R.id.field2_value, formatFieldValue(field2Value, field2Type))
        remoteViews.setTextViewText(R.id.field2_label, getFieldLabel(field2Type))
        remoteViews.setTextColor(R.id.field2_value, getFieldColor(field2Value, field2Type))

        // Update field 3
        remoteViews.setTextViewText(R.id.field3_value, formatFieldValue(field3Value, field3Type))
        remoteViews.setTextViewText(R.id.field3_label, getFieldLabel(field3Type))
        remoteViews.setTextColor(R.id.field3_value, getFieldColor(field3Value, field3Type))

        emitter.updateView(remoteViews)
    }

    private fun formatFieldValue(value: Double, dataType: String): String {
        if (value.isNaN()) return "--"

        return when (dataType) {
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

    private fun getFieldColor(value: Double, dataType: String): Int {
        if (value.isNaN()) return Color.GRAY

        // Use different colors based on data type for better readability
        return when (dataType) {
            DataType.Type.HEART_RATE, DataType.Type.AVERAGE_HR, DataType.Type.MAX_HR -> Color.RED
            DataType.Type.POWER, DataType.Type.AVERAGE_POWER, DataType.Type.MAX_POWER -> Color.YELLOW
            DataType.Type.SPEED, DataType.Type.AVERAGE_SPEED, DataType.Type.MAX_SPEED -> Color.GREEN
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
        field1Type = prefs.getString(KEY_FIELD_1, DEFAULT_FIELD_1) ?: DEFAULT_FIELD_1
        field2Type = prefs.getString(KEY_FIELD_2, DEFAULT_FIELD_2) ?: DEFAULT_FIELD_2
        field3Type = prefs.getString(KEY_FIELD_3, DEFAULT_FIELD_3) ?: DEFAULT_FIELD_3
    }

    /** Get current field configuration. */
    fun getFieldConfiguration(): Triple<String, String, String> {
        return Triple(field1Type, field2Type, field3Type)
    }

    /** Set field configuration. */
    fun setFieldConfiguration(field1: String, field2: String, field3: String) {
        field1Type = field1
        field2Type = field2
        field3Type = field3

        prefs.edit()
            .putString(KEY_FIELD_1, field1)
            .putString(KEY_FIELD_2, field2)
            .putString(KEY_FIELD_3, field3)
            .apply()
    }
}
