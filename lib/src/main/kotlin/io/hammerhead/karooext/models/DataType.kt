/**
 * Copyright (c) 2024 Hammerhead Navigation Inc.
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

@file:Suppress("MemberVisibilityCanBePrivate")

package io.hammerhead.karooext.models

import android.graphics.drawable.Drawable

/**
 * Derived from XML meta-data in [ExtensionInfo].
 */
data class DataType(
    val extension: String,
    val typeId: String,
    val displayName: String,
    val description: String,
    val graphical: Boolean,
    val icon: Drawable,
) {
    companion object {
        /**
         * Heart rate: includes a single required numeric field [Field.HEART_RATE]
         */
        const val HEART_RATE = "TYPE_HEART_RATE_ID"

        /**
         * Power: includes a single required numeric field [Field.POWER]
         */
        const val POWER = "TYPE_POWER_ID"

        /**
         * Raw cadence: includes a single required numeric field [Field.CADENCE]
         */
        const val RAW_CADENCE = "TYPE_CAD_CADENCE_ID"

        /**
         * Cadence: includes a single required numeric field [Field.CADENCE]
         */
        const val CADENCE = "TYPE_CAD_CADENCE_ID"

        /**
         * Raw speed: includes a single required numeric field [Field.SPEED]
         */
        const val RAW_SPEED = "TYPE_SPD_SPEED_ID"

        /**
         * Speed: includes a single required numeric field [Field.SPEED]
         */
        const val SPEED = "TYPE_SPEED_ID"

        /**
         * Radar: includes required field [Field.RADAR_THREAT_LEVEL] and optional fields [Field.RADAR_TARGET_1_RANGE], [Field.RADAR_TARGET_2_RANGE], [Field.RADAR_TARGET_3_RANGE], [Field.RADAR_TARGET_4_RANGE]
         */
        const val RADAR = "TYPE_RADAR_ID"

        /**
         * Tire pressure front: includes a single required numeric field [Field.TIRE_PRESSURE]
         */
        const val TIRE_PRESSURE_FRONT = "TYPE_TIRE_PRESSURE_FRONT_ID"

        /**
         * Tire pressure rear: includes a single required numeric field [Field.TIRE_PRESSURE]
         */
        const val TIRE_PRESSURE_REAR = "TYPE_TIRE_PRESSURE_REAR_ID"

        /**
         * Shifting battery: includes optional fields [Field.SHIFTING_BATTERY_STATUS] [Field.SHIFTING_BATTERY_STATUS_FD] [Field.SHIFTING_BATTERY_STATUS_RD]
         */
        const val SHIFTING_BATTERY = "TYPE_SHIFTING_BATTERY_ID"

        /**
         * Shifting front gear: includes required field [Field.FRONT_GEAR] and optional fields [Field.FRONT_GEAR_TEETH] [Field.FRONT_GEAR_MAX]
         */
        const val SHIFTING_FRONT_GEAR = "TYPE_SHIFTING_FRONT_GEAR_ID"

        /**
         * Shifting rear gear: includes required field [Field.REAR_GEAR] and optional fields [Field.REAR_GEAR_TEETH] [Field.REAR_GEAR_MAX]
         */
        const val SHIFTING_REAR_GEAR = "TYPE_SHIFTING_FRONT_GEAR_ID"

        /**
         * Construct full data type id from extension id and type id.
         */
        fun dataTypeId(extension: String, typeId: String): String {
            return "TYPE_EXT$SEPARATOR$extension$SEPARATOR$typeId"
        }

        /**
         * Parse full data type id string into extension/typeId parts.
         *
         * Also allows safe calling with extension::typeId
         *
         * @return Pair(extension, typeId)
         */
        fun fromDataType(dataTypeId: String): Pair<String, String>? {
            return if (dataTypeId.contains(SEPARATOR)) {
                val start = "TYPE_EXT$SEPARATOR"
                val parts = dataTypeId.removePrefix(start).split(SEPARATOR)
                if (parts.size == 2) {
                    return Pair(parts[0], parts[1])
                } else {
                    null
                }
            } else {
                null
            }
        }

        /**
         * @suppress
         */
        private const val SEPARATOR = "::"
    }
}
