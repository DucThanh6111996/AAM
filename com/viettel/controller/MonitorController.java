package com.viettel.controller;

// Created May 5, 2016 4:56:37 PM by quanns2

import com.viettel.bean.ChecklistInfo;
import com.viettel.exception.AppException;
import com.viettel.model.Action;
import com.viettel.model.ActionHistory;
import com.viettel.model.ImpactProcess;
import com.viettel.persistence.ActionHistoryService;
import com.viettel.persistence.ActionService;
import com.viettel.persistence.IimService;
import com.viettel.persistence.ImpactProcessService;
import com.viettel.util.AamClientFactory;
import com.viettel.util.SessionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * @author quanns2
 */
@ViewScoped
@ManagedBean
public class MonitorController implements Serializable {
    private static Logger logger = LogManager.getLogger(MonitorController.class);

    public void setIimService(IimService iimService) {
        this.iimService = iimService;
    }

    @ManagedProperty(value = "#{iimService}")
    IimService iimService;

    public void setImpactProcessService(ImpactProcessService impactProcessService) {
        this.impactProcessService = impactProcessService;
    }

    @ManagedProperty(value = "#{impactProcessService}")
    ImpactProcessService impactProcessService;

    @ManagedProperty(value = "#{actionService}")
    ActionService actionService;

    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    @ManagedProperty(value = "#{actionHistoryService}")
    ActionHistoryService actionHistoryService;

    public void setActionHistoryService(ActionHistoryService actionHistoryService) {
        this.actionHistoryService = actionHistoryService;
    }

    private List<ChecklistInfo> processRunnings;
    private List<ChecklistInfo> filterProcessRunnings;

    private List<Action> actions;
    private List<Action> filterActions;

    private String username;

    @PostConstruct
    public void onStart() {
        username = SessionUtil.getCurrentUsername();
//        username = "quanns2";
        reload();
    }

    public void reload() {
        try {
            actions = actionService.findCrByUser(username, new ArrayList<>(), DateTime.now().minusDays(1).toDate(), DateTime.now().plusDays(1).toDate(), true);
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void viewDetail(Action action) {
        try {
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();

            Map<String, Object> filters = new HashMap<>();
            filters.put("action.id", action.getId().toString());
            try {
                List<ActionHistory> histories = actionHistoryService.findList(filters, new HashMap<>());

                if (histories == null || histories.isEmpty()) {
                    externalContext.redirect("execute?action=" + action.getId());
                } else {
                    externalContext.redirect("history?action=" + histories.get(0).getId());
                }
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public List<ChecklistInfo> getProcessRunnings() {
        return processRunnings;
    }

    public void setProcessRunnings(List<ChecklistInfo> processRunnings) {
        this.processRunnings = processRunnings;
    }

    public List<ChecklistInfo> getFilterProcessRunnings() {
        return filterProcessRunnings;
    }

    public void setFilterProcessRunnings(List<ChecklistInfo> filterProcessRunnings) {
        this.filterProcessRunnings = filterProcessRunnings;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public List<Action> getFilterActions() {
        return filterActions;
    }

    public void setFilterActions(List<Action> filterActions) {
        this.filterActions = filterActions;
    }
}
