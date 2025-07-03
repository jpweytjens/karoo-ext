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

import android.graphics.Color
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.OnStreamState
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.KarooSystemService

/**
 * A randonneuring-focused data type that calculates average speed including paused time. Perfect
 * for long-distance cycling where accurate average speeds matter for planning. Unlike the default
 * average speed field which excludes paused time, this provides a true average speed since the ride
 * began - essential for randonneuring events.
 */
class Randonneur(
        extension: String,
        private val karooSystem: KarooSystemService,
) : BespokeDataType(extension, "randonneur") {

    private var currentDistance: Double = 0.0 // in meters
    private var currentRideTime: Double = 0.0 // in seconds

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
            OnStreamState.StartStreaming(DataType.Type.DISTANCE)
        ) { streamState ->
            when (streamState.state) {
                is StreamState.Streaming -> {
                    currentDistance = streamState.state.dataPoint.values[DataType.Field.DISTANCE] ?: 0.0
                    updateCalculatedValue(emitter)
                }
                else -> {
                    // Handle other stream states if needed
                }
            }
        }

        // Subscribe to real ride time data (including paused time)
        val timeConsumerId = karooSystem.addConsumer<OnStreamState>(
            OnStreamState.StartStreaming(DataType.Type.RIDE_TIME)
        ) { streamState ->
            when (streamState.state) {
                is StreamState.Streaming -> {
                    currentRideTime = streamState.state.dataPoint.values[DataType.Field.RIDE_TIME] ?: 0.0
                    updateCalculatedValue(emitter)
                }
                else -> {
                    // Handle other stream states if needed
                }
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
     * Override to provide custom zone colors suitable for randonneuring speeds. Randonneuring
     * focuses on sustainable pacing rather than high-intensity zones.
     */
    override fun getColorForZone(zoneIndex: Int): Int {
        return when (zoneIndex) {
            0 -> Color.parseColor("#FF6B6B") // Very slow - Red
            1 -> Color.parseColor("#FFE66D") // Slow - Yellow
            2 -> Color.parseColor("#4ECDC4") // Moderate/Sustainable - Teal
            3 -> Color.parseColor("#45B7D1") // Good pace - Blue
            4 -> Color.parseColor("#96CEB4") // Fast pace - Green
            else -> Color.parseColor("#FFEAA7") // Very fast - Light yellow
        }
    }

    /** Gets formatted display string optimized for randonneuring. */
    fun getFormattedDisplay(): String {
        val speedStr = formatValue(getCurrentValue())
        val timeStr = formatTime(currentRideTime)
        val distanceStr = formatDistance(currentDistance)

        return "$speedStr km/h avg (${distanceStr}km in $timeStr)"
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
