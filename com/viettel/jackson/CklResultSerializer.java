package com.viettel.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.viettel.bean.ChecklistResult;

import java.io.IOException;

/**
 * @author quanns2
 */
public class CklResultSerializer extends JsonSerializer<ChecklistResult> {
    @Override
    public void serialize(ChecklistResult value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        if (value.getLog() != null)
            jgen.writeStringField("log", value.getLog());
        if (value.getMathOption() != null)
            jgen.writeNumberField("mathOption", value.getMathOption());
        if (value.getOperationData() != null)
            jgen.writeStringField("operationData", value.getOperationData());
        if (value.getStatus() != null)
            jgen.writeNumberField("status", value.getStatus());
        if (value.getThreholdValue() != null)
            jgen.writeStringField("threholdValue", value.getThreholdValue());
        if (value.getTurnId() != null)
            jgen.writeNumberField("turnId", value.getTurnId());
        jgen.writeEndObject();
    }
}
