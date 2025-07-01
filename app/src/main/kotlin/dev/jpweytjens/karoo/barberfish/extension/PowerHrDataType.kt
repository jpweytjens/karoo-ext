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

package dev.jpweytjens.karoo.barberfish.extension

import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.models.DataPoint
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.StreamState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class PowerHrDataType(
    extension: String,
) : DataTypeImpl(extension, "power-hr") {
    override fun startStream(emitter: Emitter<StreamState>) {
        emitter.onNext(StreamState.Searching)

        val job = CoroutineScope(Dispatchers.IO).launch {
            delay(2000) // Simulate connection time

            // Simulate streaming data that alternates between power and heart rate values
            var isPower = true
            repeat(Int.MAX_VALUE) {
                val value = if (isPower) {
                    Random.nextInt(150, 300).toDouble() // Power: 150-300 watts
                } else {
                    Random.nextInt(120, 180).toDouble() // HR: 120-180 bpm
                }

                emitter.onNext(
                    StreamState.Streaming(
                        DataPoint(
                            dataTypeId,
                            values = mapOf(DataType.Field.SINGLE to value),
                        ),
                    ),
                )

                isPower = !isPower // Switch between power and HR
                delay(5000) // Update every 5 seconds
            }
        }

        emitter.setCancellable {
            job.cancel()
        }
    }
}
