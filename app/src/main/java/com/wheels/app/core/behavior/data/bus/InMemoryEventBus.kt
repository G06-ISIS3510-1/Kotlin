package com.wheels.app.core.behavior.data.bus

import com.wheels.app.core.behavior.domain.bus.EventBus
import com.wheels.app.core.behavior.domain.event.AppEvent
import com.wheels.app.core.behavior.domain.observer.AppEventSubscriber
import java.util.concurrent.CopyOnWriteArraySet
import javax.inject.Inject
import javax.inject.Singleton
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
            // Every subscriber receives the same event, but each one decides
            // whether and how to react to it.
            subscribers.forEach { subscriber ->
                subscriber.update(event)
            }
        }
    }
}
