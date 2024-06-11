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

package io.hammerhead.karooext.models

/**
 * Constants for pre-existing fields used by Karoo.
 */
object Field {
    /**
     * A single value for extensions used in numeric views
     */
    const val SINGLE = "FIELD_SINGLE_ID"

    /**
     * HR in bpm
     */
    const val HEART_RATE = "FIELD_HEART_RATE_ID"

    /**
     * Power in w
     */
    const val POWER = "FIELD_POWER_ID"

    /**
     * Candence in rpm
     */
    const val CADENCE = "FIELD_CADENCE_ID"

    /**
     * Speed in m/s
     */
    const val SPEED = "FIELD_SPEED_ID"

    /**
     * Radar threat level is one of:
     * - 0 - No thread (no RADAR_TARGET_x_RANGE should be present)
     * - 1 - Vehicle approaching (at least one RADAR_TARGET_x_RANGE should be present)
     * - 1 - Vehicle approaching fast (at least one RADAR_TARGET_x_RANGE should be present)
     */
    const val RADAR_THREAT_LEVEL = "FIELD_RADAR_THREAT_LEVEL_ID"

    /**
     * Range in meters of a radar threat 1
     */
    const val RADAR_TARGET_1_RANGE = "FIELD_RADAR_TARGET_1_RANGE_ID"

    /**
     * Range in meters of a radar threat 2
     */
    const val RADAR_TARGET_2_RANGE = "FIELD_RADAR_TARGET_2_RANGE_ID"

    /**
     * Range in meters of a radar threat 3
     */
    const val RADAR_TARGET_3_RANGE = "FIELD_RADAR_TARGET_3_RANGE_ID"

    /**
     * Range in meters of a radar threat 4
     */
    const val RADAR_TARGET_4_RANGE = "FIELD_RADAR_TARGET_4_RANGE_ID"

    /**
     * Tire pressure in bar
     */
    const val TIRE_PRESSURE = "FIELD_TIRE_PRESSURE_ID"

    /**
     * Shifting battery status is one of:
     * - 0 - New
     * - 1 - Good
     * - 2 - OK
     * - 3 - Low
     * - 4 - Critical
     * - 5 - Invalid
     * - 6 - Unrecognized
     */
    const val SHIFTING_BATTERY_STATUS = "FIELD_SHIFTING_BATTERY_STATUS_ID"

    /**
     * Front derailleur battery status.
     *
     * @see [SHIFTING_BATTERY_STATUS]
     */
    const val SHIFTING_BATTERY_STATUS_FD = "FIELD_SHIFTING_BATTERY_STATUS_FRONT_DERAILLEUR_ID"

    /**
     * Rear derailleur battery status.
     *
     * @see [SHIFTING_BATTERY_STATUS]
     */
    const val SHIFTING_BATTERY_STATUS_RD = "FIELD_SHIFTING_BATTERY_STATUS_REAR_DERAILLEUR_ID"

    /**
     * Front gear: current chainring (starts at 1)
     */
    const val FRONT_GEAR = "FIELD_SHIFTING_FRONT_GEAR_ID"

    /**
     * Front gear teeth: number of teeth on current chainring
     */
    const val FRONT_GEAR_TEETH = "FIELD_SHIFTING_FRONT_GEAR_TEETH_ID"

    /**
     * Front gear max: total number of gears on chainring
     */
    const val FRONT_GEAR_MAX = "FIELD_SHIFTING_FRONT_GEAR_MAX_ID"

    /**
     * Rear gear: current cog in cassette (starts at 1)
     */
    const val REAR_GEAR = "FIELD_SHIFTING_REAR_GEAR_ID"

    /**
     * Rear gear teeth: number of teeth on current cog
     */
    const val REAR_GEAR_TEETH = "FIELD_SHIFTING_REAR_GEAR_TEETH_ID"

    /**
     * Rear gear max: total number of cogs in cassette
     */
    const val REAR_GEAR_MAX = "FIELD_SHIFTING_REAR_GEAR_MAX_ID"
}
