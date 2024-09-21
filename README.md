# Simple EventBUS

This is a simple eventbus implementation written in kotlin.

## Functions

Support Listener register, unregister and callOnce listeners.

Using Kotlin 2.0.20 and Kotlin-Reflection, Kotlin-Coroutines.

## Usage

To create a event, create a KClass and implement `AbstractEvent`

To create a listener, you can choose two method:

`Lambda Callback`
```Kotlin
val eventBus = EventBus()
val instance1 = eventBus.register(CustomEvent::class) { // This should be called everytime
    println("I will always be here $it")
}
```
`Annotationed Functions`
```Kotlin
class Test {
    @onEvent(eventType = CustomEvent::class)
    fun newEventHandler(event: CustomEvent) {
        println("112233445566778899 ${event.name}")
    }
}
//Main
val test = Test()
//register Instance
eventBus.register(test)
```
`Call once`
```Kotlin
eventBus.callOnce(CustomEvent::class) { // This will only print once
        println("I will only be called once!")
}
```

