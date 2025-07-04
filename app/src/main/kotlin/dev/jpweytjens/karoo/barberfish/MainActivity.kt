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
                    text = "Select which data fields to display in the triple column layout",
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                // Field 1 selector
                DataFieldDropdown(
                    label = "Field 1",
                    selectedField = field1,
                    onFieldSelected = { field1 = it },
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Field 2 selector
                DataFieldDropdown(
                    label = "Field 2",
                    selectedField = field2,
                    onFieldSelected = { field2 = it },
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Field 3 selector
                DataFieldDropdown(
                    label = "Field 3",
                    selectedField = field3,
                    onFieldSelected = { field3 = it },
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = {
                            saveTripleFieldConfig(triplePrefs, field1, field2, field3)
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Save")
                    }

                    Button(
                        onClick = {
                            field1 = DataType.Type.SPEED
                            field2 = DataType.Type.HEART_RATE
                            field3 = DataType.Type.POWER
                            saveTripleFieldConfig(triplePrefs, field1, field2, field3)
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Reset to Default")
                    }
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
}
