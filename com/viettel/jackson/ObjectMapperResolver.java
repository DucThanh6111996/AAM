package com.viettel.jackson;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.viettel.bean.MapEntry;
import com.viettel.controller.Module;
import com.viettel.bean.MonitorDatabase;
import com.viettel.bean.ServiceDatabase;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Created by quanns2 on 4/18/17.
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class ObjectMapperResolver implements ContextResolver<ObjectMapper> {

    private final ObjectMapper mapper;

    public ObjectMapperResolver() {
        mapper = new ObjectMapper();
        mapper.registerModule(new GuavaModule());

        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addKeyDeserializer(Module.class, new ModuleKeyDeserializer());
        simpleModule.addKeyDeserializer(ServiceDatabase.class, new DatabaseKeyDeserializer());
        simpleModule.addKeyDeserializer(MonitorDatabase.class, new MonitorDatabaseKeyDeserializer());
        simpleModule.addKeyDeserializer(MapEntry.class, new MapEntryKeyDeserializer());
        mapper.registerModule(simpleModule);

        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
//        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
//        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}

