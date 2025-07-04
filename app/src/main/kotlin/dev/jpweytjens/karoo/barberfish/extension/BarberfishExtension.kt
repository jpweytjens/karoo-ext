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

import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.KarooExtension

class BarberfishExtension : KarooExtension("barberfish", "1.0") {

    private val karooSystem by lazy { KarooSystemService(this) }

    override val types by lazy {
        listOf(
            Randonneur(extension, karooSystem, this),
            TripleDataField(extension, karooSystem, this),
        )
    }
}
