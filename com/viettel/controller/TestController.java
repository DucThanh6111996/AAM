package com.viettel.controller;

import com.viettel.exception.AppException;
import com.viettel.lazy.LazyTest;

import com.viettel.model.Test;
import com.viettel.persistence.TestService;
import com.viettel.util.Constant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.component.tabview.Tab;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.LazyDataModel;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ViewScoped
@ManagedBean
public class TestController implements Serializable {
    private static Logger logger = LogManager.getLogger(TestController.class);


    @ManagedProperty(value = "#{testService}")
    private TestService testService;


    public boolean isEditBtnDisabled() {
        return editBtnDisabled;
    }

    public void setEditBtnDisabled(boolean editBtnDisabled) {
        this.editBtnDisabled = editBtnDisabled;
    }

    private boolean editBtnDisabled;


    private Test selectedObj;


    public Test getSelectedObj() {
        return selectedObj;
    }

    public void setSelectedObj(Test selectedObj) {
        this.selectedObj = selectedObj;
    }

    private String testString;


    public String getTestString() {
        return testString;
    }

    public void setTestString(String testString) {
        this.testString = testString;
    }

    public TestService getTestService() {
        return testService;
    }

    public void setTestService(TestService testService) {
        this.testService = testService;
    }

    private LazyDataModel<Test> lazyDataModel;

    public LazyDataModel<Test> getLazyDataModel() {
        return lazyDataModel;
    }


    private Boolean isUctt;


    @PostConstruct
    public void onStart() {

        setEditBtnDisabled(true);
        Map<String, String> filters = new HashMap<>();
        lazyDataModel = new LazyTest(testService, filters);


    }


    public void onRowSelect(SelectEvent event) {
//        FacesMessage msg = new FacesMessage("Obj Selected", Long.toString(((Test) event.getObject()).getId()));
//        FacesContext.getCurrentInstance().addMessage(null, msg);

        setEditBtnDisabled(false);

    }

    public void onRowUnselect(UnselectEvent event) {
//        FacesMessage msg = new FacesMessage("Obj Unselected", Long.toString(((Test) event.getObject()).getId()));
//        FacesContext.getCurrentInstance().addMessage(null, msg);


        setEditBtnDisabled(true);


    }

    public void preAdd() {
//        testService.save();


        TabView tabView = (TabView) FacesContext.getCurrentInstance().getViewRoot().findComponent("mop").findComponent("lst");
        Tab tab = (Tab) tabView.findComponent("addTab");
        // tab.setDisabled(true);
        tab.setRendered(true);
        tabView.setActiveIndex(1);

    }

    public void preEdit() {
//        testService.save();


        TabView tabView = (TabView) FacesContext.getCurrentInstance().getViewRoot().findComponent("mop").findComponent("lst");
        Tab tab = (Tab) tabView.findComponent("editTab");
        // tab.setDisabled(true);
        tab.setRendered(true);
        tabView.setActiveIndex(1);

        setTestString(selectedObj.getTestString());

    }

    public void add() {
//        testService.save();
        Test testObj = new Test();
        FacesMessage message = null;

        testObj.setTestString(getTestString());
        testObj.setCreatedTime(new Date());
        boolean success = false;
        try {
            testService.save(testObj);
            message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Create New Obj Success !");
            success = true;
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
            message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error while Creating New Obj !");
        }

        FacesContext.getCurrentInstance().addMessage(null, message);

        if (success) {
            TabView tabView = (TabView) FacesContext.getCurrentInstance().getViewRoot().findComponent("mop").findComponent("lst");
            Tab tab = (Tab) tabView.findComponent("addTab");
            // tab.setDisabled(true);

            tab.setRendered(false);
            tabView.setActiveIndex(0);

            setTestString(null);
        }

    }


    public void edit() {
//        testService.save();
        FacesMessage message = null;

        selectedObj.setTestString(testString);
        boolean succcess = false;
        try {
            testService.saveOrUpdate(selectedObj);
            message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Edit Obj Success !");
            succcess = true;
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
            message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error while Edit Obj !");
        }

        if (succcess) {
            FacesContext.getCurrentInstance().addMessage(null, message);

            TabView tabView = (TabView) FacesContext.getCurrentInstance().getViewRoot().findComponent("mop").findComponent("lst");
            Tab tab = (Tab) tabView.findComponent("editTab");
            // tab.setDisabled(true);
            tab.setRendered(false);
            tabView.setActiveIndex(0);tabView.setActiveIndex(0);

            setTestString(null);
        }


    }


    public void delete() {
//        testService.save();
        FacesMessage message = null;


        try {
            testService.delete(selectedObj);
            message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Delete Obj Success !");
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
            message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error while Delete Obj !");
        }

        FacesContext.getCurrentInstance().addMessage(null, message);


    }

}
