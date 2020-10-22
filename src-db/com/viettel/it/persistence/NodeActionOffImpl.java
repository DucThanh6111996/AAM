/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.persistence;

import com.viettel.it.model.NodeActionOff;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * @author Nguyễn Xuân Huy <huynx6@viettel.com.vn>
 * @sin Oct 19, 2016
 * @version 1.0 
 */
@SuppressWarnings("serial")
@Scope("session")
@Service(value = "nodeActionOffService")
public class NodeActionOffImpl extends GenericDaoImplNewV2<NodeActionOff, Long> implements  Serializable{
    
}
