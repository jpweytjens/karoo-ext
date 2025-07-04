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

package dev.jpweytjens.karoo.barberfish

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jpweytjens.karoo.barberfish.extension.TripleDataField
import io.hammerhead.karooext.models.DataType

class MainActivity : ComponentActivity() {

    companion object {
        const val PREFS_NAME = "RandonneurPrefs"
        const val KEY_MIN_SPEED = "min_speed"
        const val KEY_MAX_SPEED = "max_speed"
        const val DEFAULT_MIN_SPEED = 15.0
        const val DEFAULT_MAX_SPEED = 30.0

        const val TRIPLE_PREFS_NAME = "TripleDataFieldPrefs"
        const val KEY_FIELD_1 = "field_1"
        const val KEY_FIELD_2 = "field_2"
        const val KEY_FIELD_3 = "field_3"
        const val KEY_FIELD_1_VARIANT = "field_1_variant"
        const val KEY_FIELD_2_VARIANT = "field_2_variant"
        const val KEY_FIELD_3_VARIANT = "field_3_variant"
        const val KEY_FIELD_1_ZONE_DISPLAY = "field_1_zone_display"
        const val KEY_FIELD_2_ZONE_DISPLAY = "field_2_zone_display"
        const val KEY_FIELD_3_ZONE_DISPLAY = "field_3_zone_display"

        val AVAILABLE_DATA_FIELDS = listOf(
            DataType.Type.SPEED to "Current Speed",
            DataType.Type.AVERAGE_SPEED to "Average Speed",
            DataType.Type.MAX_SPEED to "Max Speed",
            DataType.Type.HEART_RATE to "Heart Rate",
            DataType.Type.AVERAGE_HR to "Average HR",
            DataType.Type.MAX_HR to "Max HR",
            DataType.Type.POWER to "Power",
            DataType.Type.AVERAGE_POWER to "Average Power",
            DataType.Type.MAX_POWER to "Max Power",
            DataType.Type.CADENCE to "Cadence",
            DataType.Type.AVERAGE_CADENCE to "Average Cadence",
            DataType.Type.MAX_CADENCE to "Max Cadence",
            DataType.Type.DISTANCE to "Distance",
            DataType.Type.ELAPSED_TIME to "Elapsed Time",
            DataType.Type.RIDE_TIME to "Ride Time",
            DataType.Type.TEMPERATURE to "Temperature",
            DataType.Type.ELEVATION_GAIN to "Elevation Gain",
        )

        val SMOOTHING_VARIANTS = listOf(
            TripleDataField.SmoothingVariant.RAW to "Raw",
            TripleDataField.SmoothingVariant.SMOOTH_3S to "3s Average",
            TripleDataField.SmoothingVariant.SMOOTH_5S to "5s Average",
            TripleDataField.SmoothingVariant.SMOOTH_10S to "10s Average",
            TripleDataField.SmoothingVariant.SMOOTH_30S to "30s Average",
        )

        val ZONE_DISPLAY_OPTIONS = listOf(
            TripleDataField.ZoneDisplay.NONE to "None",
            TripleDataField.ZoneDisplay.COLOR to "Color Only",
            TripleDataField.ZoneDisplay.NUMBER to "Number Only",
            TripleDataField.ZoneDisplay.BOTH to "Color + Number",
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    RandonneurConfigScreen()
                }
            }
        }
    }

    @Composable
    fun RandonneurConfigScreen() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        var minSpeed by remember {
            mutableStateOf(prefs.getFloat(KEY_MIN_SPEED, DEFAULT_MIN_SPEED.toFloat()).toDouble().toString())
        }
        var maxSpeed by remember {
            mutableStateOf(prefs.getFloat(KEY_MAX_SPEED, DEFAULT_MAX_SPEED.toFloat()).toDouble().toString())
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Text(
                text = "Barberfish Extension",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp),
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        text = "Randonneur Speed Thresholds",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )

                    Text(
                        text = "Text color will be red when speed is below minimum or above maximum",
                        modifier = Modifier.padding(bottom = 16.dp),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        TextField(
                            value = minSpeed,
                            onValueChange = { minSpeed = it },
                            label = { Text("Min Speed (km/h)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                        )

                        TextField(
                            value = maxSpeed,
                            onValueChange = { maxSpeed = it },
                            label = { Text("Max Speed (km/h)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = {
                                saveSpeedThresholds(prefs, minSpeed, maxSpeed)
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Save")
                        }

                        Button(
                            onClick = {
                                minSpeed = DEFAULT_MIN_SPEED.toString()
                                maxSpeed = DEFAULT_MAX_SPEED.toString()
                                saveSpeedThresholds(prefs, minSpeed, maxSpeed)
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Reset to Default")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Triple Data Field Configuration
            TripleDataFieldConfig()
        }
    }

    @Composable
    fun TripleDataFieldConfig() {
        val triplePrefs = getSharedPreferences(TRIPLE_PREFS_NAME, Context.MODE_PRIVATE)

        var field1 by remember {
            mutableStateOf(triplePrefs.getString(KEY_FIELD_1, DataType.Type.SPEED) ?: DataType.Type.SPEED)
        }
        var field2 by remember {
            mutableStateOf(triplePrefs.getString(KEY_FIELD_2, DataType.Type.HEART_RATE) ?: DataType.Type.HEART_RATE)
        }
        var field3 by remember {
            mutableStateOf(triplePrefs.getString(KEY_FIELD_3, DataType.Type.POWER) ?: DataType.Type.POWER)
        }

        var field1Variant by remember {
            mutableStateOf(
                TripleDataField.SmoothingVariant.valueOf(
                    triplePrefs.getString(KEY_FIELD_1_VARIANT, "raw")?.uppercase() ?: "RAW",
                ),
            )
        }
        var field2Variant by remember {
            mutableStateOf(
                TripleDataField.SmoothingVariant.valueOf(
                    triplePrefs.getString(KEY_FIELD_2_VARIANT, "raw")?.uppercase() ?: "RAW",
                ),
            )
        }
        var field3Variant by remember {
            mutableStateOf(
                TripleDataField.SmoothingVariant.valueOf(
                    triplePrefs.getString(KEY_FIELD_3_VARIANT, "raw")?.uppercase() ?: "RAW",
                ),
            )
        }

        var field1ZoneDisplay by remember {
            mutableStateOf(
                TripleDataField.ZoneDisplay.valueOf(
                    triplePrefs.getString(KEY_FIELD_1_ZONE_DISPLAY, "none")?.uppercase() ?: "NONE",
                ),
            )
        }
        var field2ZoneDisplay by remember {
            mutableStateOf(
                TripleDataField.ZoneDisplay.valueOf(
                    triplePrefs.getString(KEY_FIELD_2_ZONE_DISPLAY, "none")?.uppercase() ?: "NONE",
                ),
            )
        }
        var field3ZoneDisplay by remember {
            mutableStateOf(
                TripleDataField.ZoneDisplay.valueOf(
                    triplePrefs.getString(KEY_FIELD_3_ZONE_DISPLAY, "none")?.uppercase() ?: "NONE",
                ),
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = "Triple Data Field Configuration",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                Text(
                    text = "Configure data fields, smoothing, and zone display for the triple column layout",
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                // Field 1 configuration
                EnhancedFieldConfig(
                    label = "Field 1",
                    selectedField = field1,
                    selectedVariant = field1Variant,
                    selectedZoneDisplay = field1ZoneDisplay,
                    onFieldSelected = { field1 = it },
                    onVariantSelected = { field1Variant = it },
                    onZoneDisplaySelected = { field1ZoneDisplay = it },
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Field 2 configuration
                EnhancedFieldConfig(
                    label = "Field 2",
                    selectedField = field2,
                    selectedVariant = field2Variant,
                    selectedZoneDisplay = field2ZoneDisplay,
                    onFieldSelected = { field2 = it },
                    onVariantSelected = { field2Variant = it },
                    onZoneDisplaySelected = { field2ZoneDisplay = it },
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Field 3 configuration
                EnhancedFieldConfig(
                    label = "Field 3",
                    selectedField = field3,
                    selectedVariant = field3Variant,
                    selectedZoneDisplay = field3ZoneDisplay,
                    onFieldSelected = { field3 = it },
                    onVariantSelected = { field3Variant = it },
                    onZoneDisplaySelected = { field3ZoneDisplay = it },
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = {
                            saveEnhancedTripleFieldConfig(
                                triplePrefs,
                                field1, field1Variant, field1ZoneDisplay,
                                field2, field2Variant, field2ZoneDisplay,
                                field3, field3Variant, field3ZoneDisplay,
                            )
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Save")
                    }

                    Button(
                        onClick = {
                            field1 = DataType.Type.SPEED
                            field1Variant = TripleDataField.SmoothingVariant.RAW
                            field1ZoneDisplay = TripleDataField.ZoneDisplay.NONE
                            field2 = DataType.Type.HEART_RATE
                            field2Variant = TripleDataField.SmoothingVariant.RAW
                            field2ZoneDisplay = TripleDataField.ZoneDisplay.COLOR
                            field3 = DataType.Type.POWER
                            field3Variant = TripleDataField.SmoothingVariant.RAW
                            field3ZoneDisplay = TripleDataField.ZoneDisplay.COLOR
                            saveEnhancedTripleFieldConfig(
                                triplePrefs,
                                field1, field1Variant, field1ZoneDisplay,
                                field2, field2Variant, field2ZoneDisplay,
                                field3, field3Variant, field3ZoneDisplay,
                            )
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Reset to Default")
                    }
                }
            }
        }
    }

    @Composable
    fun EnhancedFieldConfig(
        label: String,
        selectedField: String,
        selectedVariant: TripleDataField.SmoothingVariant,
        selectedZoneDisplay: TripleDataField.ZoneDisplay,
        onFieldSelected: (String) -> Unit,
        onVariantSelected: (TripleDataField.SmoothingVariant) -> Unit,
        onZoneDisplaySelected: (TripleDataField.ZoneDisplay) -> Unit,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
            ) {
                Text(
                    text = label,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                // Data Type selector
                DataFieldDropdown(
                    label = "Data Type",
                    selectedField = selectedField,
                    onFieldSelected = onFieldSelected,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Smoothing variant selector (only if variants are available)
                val availableVariants = getAvailableVariants(selectedField)
                if (availableVariants.size > 1) {
                    SmoothingVariantDropdown(
                        selectedVariant = selectedVariant,
                        availableVariants = availableVariants,
                        onVariantSelected = onVariantSelected,
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Zone display selector (only if zones are available)
                val availableZoneDisplays = getAvailableZoneDisplays(selectedField)
                if (availableZoneDisplays.size > 1) {
                    ZoneDisplayDropdown(
                        selectedZoneDisplay = selectedZoneDisplay,
                        availableZoneDisplays = availableZoneDisplays,
                        onZoneDisplaySelected = onZoneDisplaySelected,
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DataFieldDropdown(
        label: String,
        selectedField: String,
        onFieldSelected: (String) -> Unit,
    ) {
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            TextField(
                value = AVAILABLE_DATA_FIELDS.find { it.first == selectedField }?.second ?: "Unknown",
                onValueChange = { },
                readOnly = true,
                label = { Text(label) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                AVAILABLE_DATA_FIELDS.forEach { (fieldType, displayName) ->
                    DropdownMenuItem(
                        text = { Text(displayName) },
                        onClick = {
                            onFieldSelected(fieldType)
                            expanded = false
                        },
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SmoothingVariantDropdown(
        selectedVariant: TripleDataField.SmoothingVariant,
        availableVariants: List<TripleDataField.SmoothingVariant>,
        onVariantSelected: (TripleDataField.SmoothingVariant) -> Unit,
    ) {
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            TextField(
                value = SMOOTHING_VARIANTS.find { it.first == selectedVariant }?.second ?: "Raw",
                onValueChange = { },
                readOnly = true,
                label = { Text("Smoothing") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                availableVariants.forEach { variant ->
                    val displayName = SMOOTHING_VARIANTS.find { it.first == variant }?.second ?: variant.name
                    DropdownMenuItem(
                        text = { Text(displayName) },
                        onClick = {
                            onVariantSelected(variant)
                            expanded = false
                        },
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ZoneDisplayDropdown(
        selectedZoneDisplay: TripleDataField.ZoneDisplay,
        availableZoneDisplays: List<TripleDataField.ZoneDisplay>,
        onZoneDisplaySelected: (TripleDataField.ZoneDisplay) -> Unit,
    ) {
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            TextField(
                value = ZONE_DISPLAY_OPTIONS.find { it.first == selectedZoneDisplay }?.second ?: "None",
                onValueChange = { },
                readOnly = true,
                label = { Text("Zone Display") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                availableZoneDisplays.forEach { zoneDisplay ->
                    val displayName = ZONE_DISPLAY_OPTIONS.find { it.first == zoneDisplay }?.second ?: zoneDisplay.name
                    DropdownMenuItem(
                        text = { Text(displayName) },
                        onClick = {
                            onZoneDisplaySelected(zoneDisplay)
                            expanded = false
                        },
                    )
                }
            }
        }
    }

    private fun saveSpeedThresholds(prefs: SharedPreferences, minSpeed: String, maxSpeed: String) {
        try {
            val minValue = minSpeed.toDoubleOrNull() ?: DEFAULT_MIN_SPEED
            val maxValue = maxSpeed.toDoubleOrNull() ?: DEFAULT_MAX_SPEED

            prefs.edit()
                .putFloat(KEY_MIN_SPEED, minValue.toFloat())
                .putFloat(KEY_MAX_SPEED, maxValue.toFloat())
                .apply()
        } catch (e: Exception) {
            // Handle invalid input gracefully
        }
    }

    private fun saveTripleFieldConfig(prefs: SharedPreferences, field1: String, field2: String, field3: String) {
        prefs.edit()
            .putString(KEY_FIELD_1, field1)
            .putString(KEY_FIELD_2, field2)
            .putString(KEY_FIELD_3, field3)
            .apply()
    }

    private fun saveEnhancedTripleFieldConfig(
        prefs: SharedPreferences,
        field1: String,
        field1Variant: TripleDataField.SmoothingVariant,
        field1ZoneDisplay: TripleDataField.ZoneDisplay,
        field2: String,
        field2Variant: TripleDataField.SmoothingVariant,
        field2ZoneDisplay: TripleDataField.ZoneDisplay,
        field3: String,
        field3Variant: TripleDataField.SmoothingVariant,
        field3ZoneDisplay: TripleDataField.ZoneDisplay,
    ) {
        prefs.edit()
            .putString(KEY_FIELD_1, field1)
            .putString(KEY_FIELD_1_VARIANT, field1Variant.name.lowercase())
            .putString(KEY_FIELD_1_ZONE_DISPLAY, field1ZoneDisplay.name.lowercase())
            .putString(KEY_FIELD_2, field2)
            .putString(KEY_FIELD_2_VARIANT, field2Variant.name.lowercase())
            .putString(KEY_FIELD_2_ZONE_DISPLAY, field2ZoneDisplay.name.lowercase())
            .putString(KEY_FIELD_3, field3)
            .putString(KEY_FIELD_3_VARIANT, field3Variant.name.lowercase())
            .putString(KEY_FIELD_3_ZONE_DISPLAY, field3ZoneDisplay.name.lowercase())
            .apply()
    }

    private fun getAvailableVariants(dataType: String): List<TripleDataField.SmoothingVariant> {
        return when (dataType) {
            DataType.Type.SPEED -> listOf(TripleDataField.SmoothingVariant.RAW, TripleDataField.SmoothingVariant.SMOOTH_3S, TripleDataField.SmoothingVariant.SMOOTH_5S, TripleDataField.SmoothingVariant.SMOOTH_10S)
            DataType.Type.POWER -> listOf(TripleDataField.SmoothingVariant.RAW, TripleDataField.SmoothingVariant.SMOOTH_3S, TripleDataField.SmoothingVariant.SMOOTH_5S, TripleDataField.SmoothingVariant.SMOOTH_10S, TripleDataField.SmoothingVariant.SMOOTH_30S)
            DataType.Type.CADENCE -> listOf(TripleDataField.SmoothingVariant.RAW, TripleDataField.SmoothingVariant.SMOOTH_3S, TripleDataField.SmoothingVariant.SMOOTH_5S, TripleDataField.SmoothingVariant.SMOOTH_10S)
            DataType.Type.HEART_RATE -> listOf(TripleDataField.SmoothingVariant.RAW)
            else -> listOf(TripleDataField.SmoothingVariant.RAW)
        }
    }

    private fun getAvailableZoneDisplays(dataType: String): List<TripleDataField.ZoneDisplay> {
        return when (dataType) {
            DataType.Type.HEART_RATE, DataType.Type.AVERAGE_HR, DataType.Type.MAX_HR,
            DataType.Type.POWER, DataType.Type.AVERAGE_POWER, DataType.Type.MAX_POWER,
            -> {
                listOf(TripleDataField.ZoneDisplay.NONE, TripleDataField.ZoneDisplay.COLOR, TripleDataField.ZoneDisplay.NUMBER, TripleDataField.ZoneDisplay.BOTH)
            }
            else -> listOf(TripleDataField.ZoneDisplay.NONE)
        }
    }
}
