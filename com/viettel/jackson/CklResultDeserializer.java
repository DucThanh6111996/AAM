package com.viettel.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.*;
import com.viettel.bean.ChecklistResult;

import java.io.IOException;

/**
 * @author quanns2
 */
public class CklResultDeserializer extends JsonDeserializer<ChecklistResult> {
    @Override
    public ChecklistResult deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        ObjectCodec oc = jp.getCodec();

        JsonNode node = oc.readTree(jp);

        ChecklistResult result = new ChecklistResult();
        result.setLog(node.get("log").asText());
        result.setMathOption(node.get("mathOption").asInt());
        result.setOperationData(node.get("operationData").asText());
        result.setStatus(node.get("status").asInt());
        result.setThreholdValue(node.get("threholdValue").asText());
        result.setTurnId(node.get("turnId").asLong());

        return result;
    }
}
