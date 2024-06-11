package io.hammerhead.sampleext.extension

import android.content.Context
import io.hammerhead.karooext.extension.KarooExtension
import io.hammerhead.karooext.models.OnStreamState
import io.hammerhead.karooext.models.StreamState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun Context.streamDataFlow(dataTypeId: String): Flow<StreamState> {
    return callbackFlow {
        val listenerId = (this@streamDataFlow as KarooExtension).karooSystem.addConsumer(OnStreamState.StartStreaming(dataTypeId)) { event: OnStreamState ->
            trySendBlocking(event.state)
        }
        awaitClose {
            this@streamDataFlow.karooSystem.removeConsumer(listenerId)
        }
    }
}
