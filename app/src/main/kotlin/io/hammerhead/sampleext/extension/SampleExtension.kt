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

package io.hammerhead.sampleext.extension

import io.hammerhead.karooext.extension.KarooExtension
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.models.Device
import io.hammerhead.karooext.models.DeviceEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SampleExtension : KarooExtension("sample", "1.0") {
    override val types by lazy {
        listOf(
            PowerHrDataType(extension),
            CustomSpeedDataType(extension),
        )
    }

    override fun startScan(emitter: Emitter<Device>) {
        // Find a new static HR source every 5 seconds
        val job = CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            repeat(Int.MAX_VALUE) {
                emitter.onNext(StaticHrSource(extension, 100 + it * 10).source)
                delay(5000)
            }
        }
        emitter.setCancellable {
            job.cancel()
        }
    }

    override fun connectDevice(uid: String, emitter: Emitter<DeviceEvent>) {
        StaticHrSource.fromUid(extension, uid)?.connect(emitter)
    }
}
