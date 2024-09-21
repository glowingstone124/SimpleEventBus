package ind.glowingstone.eventbus

import kotlin.reflect.KClass

class EventAnnotations {
    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class onEvent(val eventType: KClass<out AbstractEvent>)
}