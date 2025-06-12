package com.example.templateproject.client

import io.micrometer.common.KeyValue
import org.springframework.http.client.observation.ClientHttpObservationDocumentation
import org.springframework.http.client.observation.ClientRequestObservationContext
import org.springframework.http.client.observation.DefaultClientRequestObservationConvention

class CustomClientRequestObservationConvention(private val clientId: String) :
    DefaultClientRequestObservationConvention() {

    override fun clientName(context: ClientRequestObservationContext): KeyValue {
        return if (!clientId.isEmpty())
            KeyValue.of(ClientHttpObservationDocumentation.LowCardinalityKeyNames.CLIENT_NAME, clientId)
        else super.clientName(context)
    }
}
