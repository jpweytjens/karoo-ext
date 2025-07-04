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
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.OnStreamState
import io.hammerhead.karooext.models.StreamState

/**
 * A randonneuring-focused data type that calculates average speed including paused time. Perfect
 * for long-distance cycling where accurate average speeds matter for planning. Unlike the default
 * average speed field which excludes paused time, this provides a true average speed since the ride
 * began - essential for randonneuring events.
 */
class Randonneur(
    extension: String,
    private val karooSystem: KarooSystemService,
    private val context: Context,
) : BespokeDataType(extension, "randonneur") {

    companion object {
        private const val PREFS_NAME = "RandonneurPrefs"
        private const val KEY_MIN_SPEED = "min_speed"
        private const val KEY_MAX_SPEED = "max_speed"
        private const val DEFAULT_MIN_SPEED = 15.0
        private const val DEFAULT_MAX_SPEED = 30.0
    }

    private var currentDistance: Double = 0.0 // in meters
    private var currentRideTime: Double = 0.0 // in seconds

    // Configurable speed thresholds (km/h) - loaded from SharedPreferences
    private var minSpeedThreshold: Double = DEFAULT_MIN_SPEED
    private var maxSpeedThreshold: Double = DEFAULT_MAX_SPEED

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    init {
        loadSpeedThresholds()
    }

    override fun getCurrentValue(): Double {
        return if (currentRideTime > 0) {
            // Calculate average speed including paused time: distance / total time
            // Convert from m/s to km/h by multiplying by 3.6
            (currentDistance / currentRideTime) * 3.6
        } else {
            Double.NaN
        }
    }

    override fun getDataType(): String = DataType.Type.AVERAGE_SPEED

    override fun startStream(emitter: Emitter<StreamState>) {
        emitter.onNext(StreamState.Searching)

        karooSystem.connect()

        // Subscribe to real distance data
        val distanceConsumerId = karooSystem.addConsumer<OnStreamState>(
            OnStreamState.StartStreaming(DataType.Type.DISTANCE),
        ) { streamState ->
            val state = streamState.state
            if (state is StreamState.Streaming) {
                currentDistance = state.dataPoint.values[DataType.Field.DISTANCE] ?: 0.0
                updateCalculatedValue(emitter)
            }
        }

        // Subscribe to real ride time data (including paused time)
        val timeConsumerId = karooSystem.addConsumer<OnStreamState>(
            OnStreamState.StartStreaming(DataType.Type.RIDE_TIME),
        ) { streamState ->
            val state = streamState.state
            if (state is StreamState.Streaming) {
                currentRideTime = state.dataPoint.values[DataType.Field.RIDE_TIME] ?: 0.0
                updateCalculatedValue(emitter)
            }
        }

        // Set up cleanup
        emitter.setCancellable {
            karooSystem.removeConsumer(distanceConsumerId)
            karooSystem.removeConsumer(timeConsumerId)
            karooSystem.disconnect()
        }
    }

    private fun updateCalculatedValue(emitter: Emitter<StreamState>) {
        val averageSpeed = getCurrentValue()
        if (!averageSpeed.isNaN()) {
            emitter.onNext(StreamState.Streaming(createDataPoint(averageSpeed)))
        }
    }

    /**
     * Override to provide 2-color scheme: white by default, red when outside speed thresholds.
     */
    override fun getColorForZone(zoneIndex: Int): Int {
        val currentSpeed = getCurrentValue()

        return if (currentSpeed.isNaN() ||
            (currentSpeed >= minSpeedThreshold && currentSpeed <= maxSpeedThreshold)
        ) {
            // White for normal speeds or when no data
            Color.WHITE
        } else {
            // Red for speeds outside the configured range
            Color.RED
        }
    }

    /** Gets formatted display string optimized for randonneuring. */
    fun getFormattedDisplay(): String {
        val speedStr = formatValue(getCurrentValue())
        val timeStr = formatTime(currentRideTime)
        val distanceStr = formatDistance(currentDistance)

        return "$speedStr km/h avg (${distanceStr}km in $timeStr)"
    }

    /** Load speed thresholds from SharedPreferences. */
    private fun loadSpeedThresholds() {
        minSpeedThreshold = prefs.getFloat(KEY_MIN_SPEED, DEFAULT_MIN_SPEED.toFloat()).toDouble()
        maxSpeedThreshold = prefs.getFloat(KEY_MAX_SPEED, DEFAULT_MAX_SPEED.toFloat()).toDouble()
    }

    /** Set speed thresholds for color coding. */
    fun setSpeedThresholds(minSpeed: Double, maxSpeed: Double) {
        minSpeedThreshold = minSpeed
        maxSpeedThreshold = maxSpeed
    }

    /** Get current speed thresholds. */
    fun getSpeedThresholds(): Pair<Double, Double> {
        return Pair(minSpeedThreshold, maxSpeedThreshold)
    }

    private fun formatTime(seconds: Double): String {
        val hours = (seconds / 3600).toInt()
        val minutes = ((seconds % 3600) / 60).toInt()
        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }

    private fun formatDistance(meters: Double): String {
        val km = meters / 1000.0
        return "%.1f".format(km)
    }
}
