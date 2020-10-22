package com.viettel.thread;

import com.viettel.gnoc.cr.service.CrOutputForQLTNDTO;
import com.viettel.it.util.GNOCService;
import com.viettel.model.Action;
import com.viettel.persistence.ActionService;
import com.viettel.persistence.ActionServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;
import java.util.ResourceBundle;

/**
 * @author quanns2
 */
public class ReSyncGnocCrJob implements Job {
    private Logger logger = LogManager.getLogger(ReSyncGnocCrJob.class);

    public void execute(JobExecutionContext context) throws JobExecutionException {
        ResourceBundle bundle = ResourceBundle.getBundle("config");
        ActionService actionService = new ActionServiceImpl();
        List<Action> actions;

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        try {
            actions = actionService.findCrUnclosed();
            if (actions != null) {
                for (Action action : actions) {
                    try {
                        CrOutputForQLTNDTO qltndto = GNOCService.getCrByCode(action.getCrNumber());
                        if ("OK".equals(qltndto.getResultCode())) {
                            try {
                                Long crState = Long.valueOf(qltndto.getState());
                                DateTime startDateTime = DateTime.parse(qltndto.getImpactStartTime(), dateTimeFormatter);
                                DateTime endDateTime = DateTime.parse(qltndto.getImpactEndTime(), dateTimeFormatter);
                                if ((!action.getCrState().equals(crState)) || (!startDateTime.isEqual(action.getBeginTime().getTime())) || (!endDateTime.isEqual(action.getEndTime().getTime()))) {
                                    this.logger.info(action.getId() + "\t" + action.getCrNumber() + ":\t" + action.getCrState() + ":" + crState + "\t" + action.getBeginTime() + ":" + startDateTime + "\t" + action.getEndTime() + ":" + endDateTime);
                                    actionService.updateCrFromGnoc(action.getId(), startDateTime.toDate(), endDateTime.toDate(), crState);
                                }
                            } catch (NumberFormatException e) {
                                logger.error(e.getMessage() + "\t" + action.getCrNumber(), e);
                            }
                        } else if (StringUtils.isEmpty(action.getUserExecute())) {
                            String userExecute;
                            CrOutputForQLTNDTO crOutputForQLTNDTO = GNOCService.getCrByCode(action.getCrNumber());
                            if (crOutputForQLTNDTO != null && "OK".equals(crOutputForQLTNDTO.getResultCode())) {
                                userExecute = crOutputForQLTNDTO.getUserExecute();

                                actionService.updateCr(action.getId(), action.getCrNumber(), action.getCrName(), action.getBeginTime(), action.getEndTime(), action.getCrState(), userExecute);
                            }
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
