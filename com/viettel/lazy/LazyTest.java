package com.viettel.lazy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.viettel.persistence.TestService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.model.Test;
import com.viettel.persistence.ActionService;

public class LazyTest extends LazyDataModel<Test>  {
    private TestService testService;

    private static Logger logger = LogManager.getLogger(LazyTest.class);
    private Map<String, String> search;

    public LazyTest(TestService testService, Map<String, String> search) {
        this.testService = testService;
        this.search = search;

    }

    @Override
    public void setRowIndex(int rowIndex) {
        if (rowIndex == -1 || getPageSize() == 0) {
            super.setRowIndex(-1);
        } else
            super.setRowIndex(rowIndex % getPageSize());
    }

    @Override
    public Object getRowKey(Test object) {
        return object.getId();
    }

    @Override
    public Test getRowData(String rowKey) {
        Test object = new Test();
        try {
            object = testService.findById(Long.parseLong(rowKey));
        } catch (NumberFormatException e) {
            logger.error(e.getMessage(), e);
        } catch (SysException e) {
            logger.error(e.getMessage(), e);
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }
        return object;
    }

    @Override
    public List<Test> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,Object> filters) {
        List<Test> data = new ArrayList<>();
        int dataSize = 0;

        Map<String, String> order = new HashMap<>();
        if (StringUtils.isEmpty(sortField)) {
            order.put("createdTime", "DESC");
        } else {
            if (sortOrder.equals(SortOrder.ASCENDING))
                order.put(sortField, "ASC");
            else
                order.put(sortField, "DESC");
        }

        if (search != null) {
            for (Entry<String, String> filter : search.entrySet()) {
                filters.put(filter.getKey(), filter.getValue());
            }
        }

        try {
            data = testService.findList(first, pageSize, filters, order);
            dataSize = testService.count(filters);
        } catch (SysException | AppException e) {
            logger.error(e.getMessage(), e);
        }

        this.setRowCount(dataSize);
        return data;
    }

}
