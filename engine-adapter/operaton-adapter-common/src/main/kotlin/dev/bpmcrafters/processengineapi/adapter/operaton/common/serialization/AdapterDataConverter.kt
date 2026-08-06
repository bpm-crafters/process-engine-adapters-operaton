package dev.bpmcrafters.processengineapi.adapter.operaton.common.serialization

interface AdapterDataConverter {

  fun <T : Any> convert(value: Any?, type: Class<T>): T?

}
