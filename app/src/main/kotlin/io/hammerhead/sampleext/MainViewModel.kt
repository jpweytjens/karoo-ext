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

package io.hammerhead.sampleext

import androidx.lifecycle.ViewModel
import io.hammerhead.karooext.models.RideState
import io.hammerhead.karooext.models.StreamState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MainData(
    val connected: Boolean = false,
    val power: StreamState? = null,
    val rideState: RideState? = null,
)

class MainViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(MainData())
    val state: StateFlow<MainData> = mutableState.asStateFlow()

    fun updateConnected(connected: Boolean) {
        mutableState.update { it.copy(connected = connected) }
    }

    fun updatePower(power: StreamState) {
        mutableState.update { it.copy(power = power) }
    }

    fun updateRideState(rideState: RideState) {
        mutableState.update { it.copy(rideState = rideState) }
    }
}
