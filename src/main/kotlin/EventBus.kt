package ind.glowingstone.eventbus

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import kotlin.reflect.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.isAccessible

class EventBus {
    private val listeners = ConcurrentHashMap<KClass<out AbstractEvent>, MutableSet<WrappedListener>>()
    val event_list = LinkedBlockingQueue<AbstractEvent>(30)
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    data class WrappedListener(
        val listener: (AbstractEvent) -> Unit,
        val hashCode: Int,
        val callOnce: Boolean = false,
        var haveCalled: Boolean = false,
    )

    fun <T : AbstractEvent> register(eventType: KClass<T>, listener: (event: T) -> Unit): WrappedListener {
        val wrappedListener = WrappedListener({ event -> listener(event as T) }, listener.hashCode(), false, false)
        listeners.computeIfAbsent(eventType) { mutableSetOf() }.add(wrappedListener)
        return wrappedListener
    }

    fun <T : AbstractEvent> callOnce(eventType: KClass<T>, listener: (event: T) -> Unit): WrappedListener {
        val wrappedListener = WrappedListener({ event ->
            listener(event as T)
        }, listener.hashCode(), true, false)
        listeners.computeIfAbsent(eventType) { mutableSetOf() }.add(wrappedListener)
        return wrappedListener
    }

    suspend fun post(event: AbstractEvent) {
        val eventType = event::class
        event_list.add(event)
        listeners[eventType]?.forEach { wrappedListener ->
            if (wrappedListener.haveCalled && wrappedListener.callOnce) {
                unregister(eventType, wrappedListener)
                return
            }
            withContext(scope.coroutineContext) {
                wrappedListener.listener.invoke(event)
            }
            wrappedListener.haveCalled = true
        }
    }

    fun <T : AbstractEvent> unregister(eventType: KClass<T>, listener: WrappedListener) {
        listeners[eventType]?.removeIf { wrappedListener ->
            wrappedListener.listener == listener.listener
        }
    }
    fun register(listenerInstance: Any) {

        val methodsWithAnnotation = listenerInstance::class.declaredFunctions.filter { function ->
            function.findAnnotation<EventAnnotations.onEvent>() != null
        }

        methodsWithAnnotation.forEach { method ->
            val annotation = method.findAnnotation<EventAnnotations.onEvent>()!!
            val eventType = annotation.eventType
            method.isAccessible = true

            val wrappedListener = WrappedListener(
                { event ->
                    if (event::class == eventType) {
                        method.call(listenerInstance, event)
                    } else {
                        throw IllegalArgumentException("Event type mismatch: expected ${eventType.simpleName}, but got ${event::class.simpleName}")
                    }
                },
                method.hashCode(),
                false,
                false
            )

            listeners.computeIfAbsent(eventType) { mutableSetOf() }.add(wrappedListener)
        }
    }


}