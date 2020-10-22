package com.viettel.util;

import java.util.List;

/**
 * Created by quytv7 on 9/30/2019.
 */
public class ProcedureDTO {
    private Long procedureId;
    private List<Long> procedureWorkFlowIds;

    public Long getProcedureId() {
        return procedureId;
    }

    public void setProcedureId(Long procedureId) {
        this.procedureId = procedureId;
    }

    public List<Long> getProcedureWorkFlowIds() {
        return procedureWorkFlowIds;
    }

    public void setProcedureWorkFlowIds(List<Long> procedureWorkFlowIds) {
        this.procedureWorkFlowIds = procedureWorkFlowIds;
    }
}
