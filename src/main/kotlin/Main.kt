package ind.glowingstone.eventbus

import ind.glowingstone.eventbus.EventAnnotations.onEvent

abstract class AbstractEvent {
    abstract val name: String
}


class CustomEvent(input: String) : AbstractEvent() {
    val message: String = input
    override val name: String = "CustomEvent"
}

suspend fun main() {
    val eventBus = EventBus()
    val test = Test()
    //register Instance
    eventBus.register(test)
    val instance1 = eventBus.register(CustomEvent::class) { // This should be called everytime
        println("I will always be here $it")
    }

    val instance2 = eventBus.register(CustomEvent::class) { // This will be deleted after Line 30
        println("I will be deleted ${it.message}")
    }
    val instance3 = eventBus.register(CustomEvent::class) {
        println("I will always be here 2 $it")
    }
    eventBus.callOnce(CustomEvent::class) { // This will only print once
        println("I will only be called once!")
    }
    eventBus.post(CustomEvent("hello, world"))

    println("Unregistered a listener")
    eventBus.unregister(CustomEvent::class, instance2)

    eventBus.post(CustomEvent("hello again, world"))
}

class Test {
    @onEvent(eventType = CustomEvent::class)
    fun newEventHandler(event: CustomEvent) {
        println("112233445566778899 ${event.name}")
    }
}