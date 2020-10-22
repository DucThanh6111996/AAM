package com.viettel.thread;

import com.viettel.controller.ExecuteController;
import com.viettel.model.*;
import com.viettel.persistence.ActionService;
import com.viettel.persistence.ActionServiceImpl;
import com.viettel.util.Constant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author quanns2
 */
public class AutoThread implements Runnable {
    private static Logger logger = LogManager.getLogger(AutoThread.class);
    private Thread thread;

    private Action action;

    public AutoThread(Action action) {
        this.action = action;
    }

    @Override
    public void run() {
        logger.info("Running " +  action.getTdCode());
        ActionService actionService = new ActionServiceImpl();
        try{
            actionService.updateRunStatus(action.getId(),10);
        }catch(Exception ex){
            logger.error(ex.getMessage(),ex);
        }
        action.setTestbedMode(Boolean.FALSE);

        ExecuteController executeController = new ExecuteController();
        executeController.init();
        executeController.setUsername(action.getUserExecute());

        executeController.setSelectedAction(action);
        executeController.subjectSelectionChanged(null);

        action.setTestbedMode(Boolean.FALSE);
        executeController.execute(Constant.EXE_TD);
        try{
            Action actionTemp = actionService.findById(action.getId());
            if(actionTemp.getRunStatus() == 10){
                actionService.updateRunStatus(action.getId(),null);
            }
        }catch(Exception ex){
            logger.error(ex.getMessage(),ex);
        }
        if (executeController.getRunId() != null)
            action.setRunId(executeController.getRunId());
        logger.info("Thread " +  action.getTdCode() + " exiting.");
    }
    public void start () {
        logger.info("Starting " +  action.getCrNumber() + "\t" + action.getTdCode());
        if (thread == null) {
            thread = new Thread(this, action.getTdCode());
            thread.start();
        }
    }
    public static void main(String [] args){
        try {
            ActionService actionService = new ActionServiceImpl();
            actionService.updateRunStatus(63L, 10);
            System.out.println("ok");
        }catch(Exception ex){

        }
    }

}
