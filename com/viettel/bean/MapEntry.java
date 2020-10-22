package com.viettel.bean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mchange.v2.lang.ObjectUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.Map;

/**
 * @author quanns2
 */
public class MapEntry  implements Serializable {
    private static Logger logger = LogManager.getLogger(MapEntry.class);
    
    Integer key;
    Integer value;

    public MapEntry(){
        super();
    }

    public MapEntry(Integer key, Integer value) {
        this.key = key;
        this.value = value;
    }

    public Integer getKey() {
        return this.key;
    }

    public Integer getValue() {
        return this.value;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public void copy(MapEntry mapEntry) {
        this.value = mapEntry.value;
        this.key = mapEntry.key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MapEntry mapEntry = (MapEntry) o;

        return new EqualsBuilder()
                .append(key, mapEntry.key)
                .append(value, mapEntry.value)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(key)
                .append(value)
                .toHashCode();
    }

/*public boolean equals(Object o) {
        if(!(o instanceof Map.Entry)) {
            return false;
        } else {
            Map.Entry other = (Map.Entry)o;
            return ObjectUtils.eqOrBothNull(this.key, other.getKey()) && ObjectUtils.eqOrBothNull(this.value, other.getValue());
        }
    }

    public int hashCode() {
        return ObjectUtils.hashOrZero(this.key) ^ ObjectUtils.hashOrZero(this.value);
    }*/

    @Override
    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }
}
