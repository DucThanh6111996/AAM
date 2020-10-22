package com.viettel.controller;

import com.viettel.bean.*;
import java.util.List;


/**
 * Created by quanns2 on 4/10/17.
 */
public interface AomClientService {
    public List<ServerChecklist> findModulesByIds(List<String> ips) throws AppException;
    public List<ModuleChecklist> findChecklistModulesByIds(List<Long> moduleIds) throws AppException;
    public List<QueueChecklist> findChecklistQueues(List<Long> serviceIds) throws AppException;
    public List<QueueChecklist> findChecklistQueueByIds(List<Long> queueIds) throws AppException;
}
