package com.viettel.jackson;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.bean.ServiceDatabase;

import java.io.IOException;

/**
 * Created by quanns2 on 4/18/17.
 */
public class DatabaseKeyDeserializer extends KeyDeserializer {

    @Override
    public Object deserializeKey(String s, DeserializationContext deserializationContext) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(s, ServiceDatabase.class);
    }
}
