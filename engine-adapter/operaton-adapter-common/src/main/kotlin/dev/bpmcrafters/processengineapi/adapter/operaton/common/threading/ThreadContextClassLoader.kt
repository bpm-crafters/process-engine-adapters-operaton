package dev.bpmcrafters.processengineapi.adapter.operaton.common.threading

import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

fun <T> withThreadContextClassLoader(context: Any, action: () -> T): T {
  val contextClassLoader = context.javaClass.classLoader ?: return action()
  val thread = Thread.currentThread()
  val originalContextClassLoader = thread.contextClassLoader
  if (originalContextClassLoader === contextClassLoader) {
    return action()
  }

  thread.contextClassLoader = contextClassLoader
  return try {
    action()
  } finally {
    thread.contextClassLoader = originalContextClassLoader
  }
}

class ThreadContextClassLoaderThreadFactory(
  private val contextClassLoader: ClassLoader,
  private val delegate: ThreadFactory = Executors.defaultThreadFactory(),
) : ThreadFactory {
  override fun newThread(runnable: Runnable): Thread = delegate.newThread(runnable).apply {
    contextClassLoader = this@ThreadContextClassLoaderThreadFactory.contextClassLoader
  }
}
