/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.persistence;

import com.viettel.it.model.ServiceTemplateMapping;
import java.io.Serializable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 *
 * @author hienhv4
 */
@Scope("session")
@Service(value = "serviceTemplateMapping")
public class ServiceTemplateMappingServiceImpl extends GenericDaoImplNewV2<ServiceTemplateMapping, String> implements Serializable {

    private static final long serialVersionUID = -4109611148855610L;
}
