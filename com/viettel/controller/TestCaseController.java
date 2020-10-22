package com.viettel.controller;

// Created May 30, 2016 2:10:12 PM by quanns2


import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import com.viettel.exception.AppException;
import com.viettel.it.util.MessageUtil;
import com.viettel.model.Action;
import com.viettel.model.TestCase;
import com.viettel.persistence.TestCaseService;
import com.viettel.util.FileHelper;
import com.viettel.util.UploadFileUtils;
import com.viettel.util.Util;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omnifaces.util.Faces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

/**
 * @author quanns2
 */
@ViewScoped
@ManagedBean
public class TestCaseController implements Serializable {
    private static Logger logger = LogManager.getLogger(TestCaseController.class);

//	static String parentPath = File.separator + "Action_tool";
//	static String testcaseFolder = parentPath + File.separator + "Test_case" + File.separator;

    @ManagedProperty(value = "#{testCaseService}")
    TestCaseService testCaseService;

    public void setTestCaseService(TestCaseService testCaseService) {
        this.testCaseService = testCaseService;
    }

    private TestCase selectedObj;
    private TestCase newObj;

    private boolean isEdit;

    private Long searchId;
    private String searchFileName;
    private Integer searchTestcaseType;
    private Long searchActionId;
    private Date searchDateUpload;

    private Action action;

    private List<TestCase> testCases;

    @PostConstruct
    public void onStart() {
        clear();

        testCases = new ArrayList<>();
    }

    public void handleFileUpload(FileUploadEvent event) {
        logger.info(event.getFile().getFileName() + " is uploaded.");


        UploadedFile file = event.getFile();
        String uploadFolder = UploadFileUtils.getTestcaseFolder(action);
//		String fName = Util.convertUTF8ToNoSign(getNameFile(file.getFileName())) + getFileExtension(file.getFileName());
        FileHelper.uploadFile(uploadFolder, file, event.getFile().getFileName());

        newObj.setFileName(event.getFile().getFileName());
        newObj.setDateUpload(new Date());
    }

    public void downloadFile(TestCase testCase) {
        String uploadFolder = UploadFileUtils.getTestcaseFolder(action);

        File file = new File(uploadFolder + File.separator + testCase.getFileName());

        try {
            Faces.sendFile(file, true);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void viewSelectItems() {
        Map<String, Object> filters = new HashMap<>();

        if (action != null && action.getId() != null) {
            filters.put("actionId", action.getId() + "");

            try {
                testCases = testCaseService.findList(filters, new HashMap<String, String>());
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            testCases = new ArrayList<>();
        }
    }

    public void search() {
        ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("form").findComponent("objectTable"))
                .setFirst(0);

    }

    public void prepareEdit(TestCase obj) {
        isEdit = true;
        selectedObj = obj;
        BeanUtilsBean.getInstance().getConvertUtils().register(false, false, 0);
        try {
            BeanUtils.copyProperties(newObj, obj);
            //newObj.setPassword("");
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void clear() {
        isEdit = false;
        newObj = new TestCase();
    }

    public void init() {
        isEdit = false;
        newObj = new TestCase();
        testCases = new ArrayList<>();
    }

    public void duplicate(TestCase obj) {
        isEdit = false;
        obj.setId(null);
//		selectedObj = obj;
        newObj = new TestCase();
        BeanUtilsBean.getInstance().getConvertUtils().register(false, false, 0);
        try {
            BeanUtils.copyProperties(newObj, obj);
            // newObj.setPassword("");
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void saveOrUpdate() {
        FacesMessage msg = null;
        try {
            if (isEdit) {
                // oldPass = selectedObj.getPassword();
            }

            if (StringUtils.isEmpty(newObj.getFileName())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("test.case.do.not.upload"), "");
                return;
            } else if (StringUtils.isEmpty(newObj.getUserPerform())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("department.execution.do.not.enter"), "");
                return;
            }

            if (!isEdit) {
/*				selectedObj = new TestCase();

				BeanUtilsBean.getInstance().getConvertUtils().register(false, false, 0);
				BeanUtils.copyProperties(selectedObj, newObj);

				selectedObj.setId(null);*/
                testCases.add(newObj);

            } else {
                BeanUtilsBean.getInstance().getConvertUtils().register(false, false, 0);
                try {
                    BeanUtils.copyProperties(selectedObj, newObj);
                    //newObj.setPassword("");
                } catch (IllegalAccessException | InvocationTargetException e) {
                    logger.error(e.getMessage(), e);
                }
            }
//			testCaseService.saveOrUpdate(selectedObj);
            if (!isEdit) {
                msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("insert.successful"), "");
            } else {
                msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("update.successful"), "");
            }


            RequestContext.getCurrentInstance().execute("PF('editDialogTc').hide()");
            newObj = new TestCase();
            isEdit = false;
        } catch (Exception e) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("update.failed"), "");
            logger.error(e.getMessage(), e);
        } finally {
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
        }

    }

    public void delete() {
        FacesMessage msg = null;
        try {
            testCases.remove(selectedObj);
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("delete.successful"), "");
        } catch (Exception e) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("delete.failed"), "");
            logger.error(e.getMessage(), e);
        } finally {
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
        }
    }


    public TestCase getSelectedObj() {
        return selectedObj;
    }

    public void setSelectedObj(TestCase selectedObj) {
        this.selectedObj = selectedObj;
    }

    public TestCase getNewObj() {
        return newObj;
    }

    public void setNewObj(TestCase newObj) {
        this.newObj = newObj;
    }

    public Boolean getIsEdit() {
        return isEdit;
    }

    public void setEdit(boolean isEdit) {
        this.isEdit = isEdit;
    }

    public Long getSearchId() {
        return this.searchId;
    }

    public void setSearchId(Long searchId) {
        this.searchId = searchId;
    }

    public String getSearchFileName() {
        return this.searchFileName;
    }

    public void setSearchFileName(String searchFileName) {
        this.searchFileName = searchFileName;
    }

    public Integer getSearchTestcaseType() {
        return this.searchTestcaseType;
    }

    public void setSearchTestcaseType(Integer searchTestcaseType) {
        this.searchTestcaseType = searchTestcaseType;
    }

    public Long getSearchActionId() {
        return this.searchActionId;
    }

    public void setSearchActionId(Long searchActionId) {
        this.searchActionId = searchActionId;
    }

    public Date getSearchDateUpload() {
        return this.searchDateUpload;
    }

    public void setSearchDateUpload(Date searchDateUpload) {
        this.searchDateUpload = searchDateUpload;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<TestCase> testCases) {
        this.testCases = testCases;
    }
}
