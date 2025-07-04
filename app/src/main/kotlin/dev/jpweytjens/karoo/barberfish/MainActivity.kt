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
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
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
            HammerheadTheme {
                BarberfishConfigScreen()
            }
        }
    }

    @Composable
    fun HammerheadTheme(content: @Composable () -> Unit) {
        MaterialTheme(
            colorScheme = MaterialTheme.colorScheme.copy(
                background = Color.White,
                surface = Color.White,
                onBackground = Color.Black,
                onSurface = Color.Black,
                primary = Color(0xFF4A90E2), // Hammerhead blue
                onPrimary = Color.White,
            ),
            content = content,
        )
    }

    @Composable
    fun BarberfishConfigScreen() {
        val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White,
        ) {
            Column {
                // Header with back button and title
                HeaderBar(
                    title = "BARBERFISH",
                    onBackPressed = { backPressedDispatcher?.onBackPressed() },
                )

                // Scrollable content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    // Randonneur Settings
                    RandonneurSection()

                    HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                    // Triple Data Field Settings
                    TripleDataFieldSection()
                }
            }
        }
    }

    @Composable
    fun HeaderBar(
        title: String,
        onBackPressed: () -> Unit,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Back button (gray circle like native)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFB0B0B0))
                    .clickable { onBackPressed() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Title
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
            )
        }
    }

    @Composable
    fun RandonneurSection() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var minSpeed by remember {
            mutableStateOf(prefs.getFloat(KEY_MIN_SPEED, DEFAULT_MIN_SPEED.toFloat()).toString())
        }
        var maxSpeed by remember {
            mutableStateOf(prefs.getFloat(KEY_MAX_SPEED, DEFAULT_MAX_SPEED.toFloat()).toString())
        }

        SettingsSection(title = "Randonneur Average Speed") {
            SettingsItem(
                title = "Min Speed Threshold",
                subtitle = "Speed below this will show in red",
                trailing = {
                    NativeTextField(
                        value = minSpeed,
                        onValueChange = {
                            minSpeed = it
                            saveSpeedThresholds(prefs, minSpeed, maxSpeed)
                        },
                        suffix = "km/h",
                        keyboardType = KeyboardType.Number,
                    )
                },
            )

            SettingsItem(
                title = "Max Speed Threshold",
                subtitle = "Speed above this will show in red",
                trailing = {
                    NativeTextField(
                        value = maxSpeed,
                        onValueChange = {
                            maxSpeed = it
                            saveSpeedThresholds(prefs, minSpeed, maxSpeed)
                        },
                        suffix = "km/h",
                        keyboardType = KeyboardType.Number,
                    )
                },
            )
        }
    }

    @Composable
    fun TripleDataFieldSection() {
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

        SettingsSection(title = "Triple Data Field") {
            // Field 1
            NativeFieldConfiguration(
                label = "Field 1",
                selectedField = field1,
                selectedVariant = field1Variant,
                selectedZoneDisplay = field1ZoneDisplay,
                onFieldSelected = { field1 = it },
                onVariantSelected = { field1Variant = it },
                onZoneDisplaySelected = { field1ZoneDisplay = it },
                onSave = {
                    saveEnhancedTripleFieldConfig(
                        triplePrefs,
                        field1, field1Variant, field1ZoneDisplay,
                        field2, field2Variant, field2ZoneDisplay,
                        field3, field3Variant, field3ZoneDisplay,
                    )
                },
            )

            // Field 2
            NativeFieldConfiguration(
                label = "Field 2",
                selectedField = field2,
                selectedVariant = field2Variant,
                selectedZoneDisplay = field2ZoneDisplay,
                onFieldSelected = { field2 = it },
                onVariantSelected = { field2Variant = it },
                onZoneDisplaySelected = { field2ZoneDisplay = it },
                onSave = {
                    saveEnhancedTripleFieldConfig(
                        triplePrefs,
                        field1, field1Variant, field1ZoneDisplay,
                        field2, field2Variant, field2ZoneDisplay,
                        field3, field3Variant, field3ZoneDisplay,
                    )
                },
            )

            // Field 3
            NativeFieldConfiguration(
                label = "Field 3",
                selectedField = field3,
                selectedVariant = field3Variant,
                selectedZoneDisplay = field3ZoneDisplay,
                onFieldSelected = { field3 = it },
                onVariantSelected = { field3Variant = it },
                onZoneDisplaySelected = { field3ZoneDisplay = it },
                onSave = {
                    saveEnhancedTripleFieldConfig(
                        triplePrefs,
                        field1, field1Variant, field1ZoneDisplay,
                        field2, field2Variant, field2ZoneDisplay,
                        field3, field3Variant, field3ZoneDisplay,
                    )
                },
            )
        }
    }

    @Composable
    fun SettingsSection(
        title: String,
        content: @Composable () -> Unit,
    ) {
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )
            content()
        }
    }

    @Composable
    fun SettingsItem(
        title: String,
        subtitle: String? = null,
        trailing: @Composable (() -> Unit)? = null,
        onClick: (() -> Unit)? = null,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .let { if (onClick != null) it.clickable { onClick() } else it }
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Normal,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }

            if (trailing != null) {
                trailing()
            }
        }
    }

    @Composable
    fun NativeTextField(
        value: String,
        onValueChange: (String) -> Unit,
        suffix: String? = null,
        keyboardType: KeyboardType = KeyboardType.Text,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = Color.Black,
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color(0xFF4A90E2),
                    unfocusedIndicatorColor = Color.Gray,
                ),
                modifier = Modifier.width(80.dp),
            )
            if (suffix != null) {
                Text(
                    text = suffix,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
    }

    @Composable
    fun NativeFieldConfiguration(
        label: String,
        selectedField: String,
        selectedVariant: TripleDataField.SmoothingVariant,
        selectedZoneDisplay: TripleDataField.ZoneDisplay,
        onFieldSelected: (String) -> Unit,
        onVariantSelected: (TripleDataField.SmoothingVariant) -> Unit,
        onZoneDisplaySelected: (TripleDataField.ZoneDisplay) -> Unit,
        onSave: () -> Unit,
    ) {
        SettingsItem(
            title = label,
            subtitle = AVAILABLE_DATA_FIELDS.find { it.first == selectedField }?.second ?: "Unknown",
            trailing = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Configure",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(start = 8.dp),
                )
            },
            onClick = {
                // TODO: Navigate to detailed configuration screen
                // For now, just save current configuration
                onSave()
            },
        )
    }

    // Utility functions
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
}
