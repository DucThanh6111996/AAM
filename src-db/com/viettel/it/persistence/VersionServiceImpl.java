/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.persistence;

import com.viettel.it.model.Version;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 *
 * @author hienhv4
 */
@Scope("session")
@Service(value = "versionService")
public class VersionServiceImpl extends GenericDaoImplNewV2<Version, Long> implements  Serializable{
    private static final long serialVersionUID = -4109611148855610L;
}
