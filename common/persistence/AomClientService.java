package com.viettel.persistence;

import com.viettel.bean.*;
import com.viettel.exception.AppException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by quanns2 on 4/10/17.
 */
public interface AomClientService {
    public List<ServerChecklist> findModulesByIds(List<String> ips) throws AppException;
    public List<ModuleChecklist> findChecklistModulesByIds(List<Long> moduleIds) throws AppException;
    public List<QueueChecklist> findChecklistQueues(List<Long> serviceIds) throws AppException;
    public List<QueueChecklist> findChecklistQueueByIds(List<Long> queueIds) throws AppException;
}
