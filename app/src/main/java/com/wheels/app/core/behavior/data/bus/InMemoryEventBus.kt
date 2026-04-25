package com.wheels.app.core.behavior.data.bus

import android.util.Log
import com.wheels.app.core.behavior.domain.bus.EventBus
import com.wheels.app.core.behavior.domain.event.AppEvent
import com.wheels.app.core.behavior.domain.observer.AppEventSubscriber
import java.util.concurrent.CopyOnWriteArraySet
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@Singleton
class InMemoryEventBus @Inject constructor(
    subscribers: Set<@JvmSuppressWildcards AppEventSubscriber>,
    private val ioDispatcher: CoroutineDispatcher
) : EventBus {

    private val subscribers = CopyOnWriteArraySet<AppEventSubscriber>()

    init {
        // Hilt provides the observer set, and the bus keeps the publish/subscribe
        // relationship explicit to match the Observer pattern in the app layer.
        subscribers.forEach(::subscribe)
    }

    override fun subscribe(subscriber: AppEventSubscriber) {
        subscribers.add(subscriber)
    }

    override suspend fun publish(event: AppEvent) {
        withContext(ioDispatcher) {
            supervisorScope {
                // Fan out the same event to each observer concurrently so
                // tracking, analytics, and notification preparation stay
                // isolated from one another.
                subscribers.map { subscriber ->
                    launch {
                        runCatching {
                            subscriber.update(event)
                        }.onFailure { error ->
                            // Observers are best-effort. One failing subscriber
                            // should not crash the app or prevent the other
                            // observers from running.
                            Log.w(TAG, "App event observer failed", error)
                        }
                    }
                }.joinAll()
            }
        }
    }

    private companion object {
        const val TAG = "InMemoryEventBus"
    }
}
