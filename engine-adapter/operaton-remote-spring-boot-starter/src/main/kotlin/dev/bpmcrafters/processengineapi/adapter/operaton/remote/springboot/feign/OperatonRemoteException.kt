package dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.feign

/**
 * Specific exception wrapping all errors thrown by the engine.
 */
class OperatonRemoteException(message: String, cause: Throwable?)
  : RuntimeException(message, cause)
