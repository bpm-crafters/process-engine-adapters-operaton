package dev.bpmcrafters.processengineapi.adapter.operaton.remote.testing

import dev.bpmcrafters.processengineapi.adapter.operaton.remote.process.ProcessDefinitionMetaDataResolver

internal object NoOpMetadataResolver : ProcessDefinitionMetaDataResolver {
  override fun getProcessDefinitionKey(processDefinitionId: String?) = null
  override fun getProcessDefinitionVersionTag(processDefinitionId: String?) = null
  override fun getProcessDefinitionId(processDefinitionKey: String, tenantId: String?) = null
}
