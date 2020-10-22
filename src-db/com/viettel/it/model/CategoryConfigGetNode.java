package com.viettel.it.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by quytv7 on 1/17/2018.
 */
@Entity
@Table(name = "CATEGORY_CONFIG_GET_NODE")
public class CategoryConfigGetNode implements Serializable {
    private Long id;
    private String configName;
    private String className;
    private String functionName;
    private String description;
    private Long type;
    private String paramName;
    private Long groupId;
    private String regexDefault;

    public CategoryConfigGetNode() {
    }

    @SequenceGenerator(name = "generator", sequenceName = "CCATEGORY_CONFIG_GET_NODE_SEQ", allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "generator")
    @Column(name = "ID", unique = true, nullable = false, precision = 22, scale = 0)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "CONFIG_NAME")
    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    @Column(name = "CLASS_NAME")
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Column(name = "FUNCTION_NAME")
    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    @Column(name = "DESCRIPTION")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "TYPE", nullable = false)
    public Long getType() {
        return type;
    }

    public void setType(Long type) {
        this.type = type;
    }

    @Column(name = "PARAM_NAME", nullable = false, length = 200)
    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    @Column(name = "GROUP_ID", nullable = false)
    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    @Column(name = "REGEX_DEFAULT")
    public String getRegexDefault() {
        return regexDefault;
    }

    public void setRegexDefault(String regexDefault) {
        this.regexDefault = regexDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CategoryConfigGetNode that = (CategoryConfigGetNode) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
