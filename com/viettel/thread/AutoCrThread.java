package com.viettel.thread;


import com.viettel.it.controller.DrawTopoStatusExecController;
import com.viettel.it.model.FlowRunAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by VTN-PTPM-NV55 on 12/20/2018.
 */
public class AutoCrThread implements Runnable {

    private static Logger logger = LogManager.getLogger(AutoThread.class);
    private Thread thread;

    private FlowRunAction flowRunAction;

    public AutoCrThread(FlowRunAction flowRunAction) {
        this.flowRunAction = flowRunAction;
    }

    @Override
    public void run() {
        logger.info("Start Auto Running for flowRunId: " +  flowRunAction.getFlowRunId() + ", crNumber:" + flowRunAction.getCrNumber());
        DrawTopoStatusExecController drawTopoStatusExecController = new DrawTopoStatusExecController();
        drawTopoStatusExecController.initAutoMode();
        drawTopoStatusExecController.setSelectedFlowRunAction(flowRunAction);
        drawTopoStatusExecController.onSaveAccountNode();
        logger.info(" End Auto Running for flowRunId: " +  flowRunAction.getFlowRunId() + ", crNumber:" + flowRunAction.getCrNumber());
    }

    public void start () {
        if (thread == null) {
            thread = new Thread(this, flowRunAction.getFlowRunId()+"");
            thread.start();
        }
    }
}
