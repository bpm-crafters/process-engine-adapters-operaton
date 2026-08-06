package dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.feign

import dev.bpmcrafters.processengineapi.adapter.operaton.remote.springboot.OperatonRemoteAdapterEnabledCondition
import feign.codec.ErrorDecoder
import org.camunda.community.rest.client.EnableCamundaFeignClients
import org.camunda.community.rest.exception.CamundaHttpFeignErrorDecoder
import org.camunda.community.rest.exception.ClientExceptionFactory
import org.camunda.community.rest.variables.ValueMapperConfiguration
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional

@AutoConfiguration
@EnableCamundaFeignClients
@Conditional(OperatonRemoteAdapterEnabledCondition::class)
@ImportAutoConfiguration(ValueMapperConfiguration::class)
class FeignClientAutoConfiguration {

  @Bean
  fun operatonErrorDecoder(): ErrorDecoder {
    return CamundaHttpFeignErrorDecoder(
      httpCodes = listOf(400, 500), // current default
      defaultDecoder = ErrorDecoder.Default(),
      wrapExceptions = true, // wrap exception
      targetExceptionType = OperatonRemoteException::class.java,
      exceptionFactory = object : ClientExceptionFactory<OperatonRemoteException> {
        override fun create(message: String, cause: Throwable?) = OperatonRemoteException(message, cause)
      }
    )
  }

}
