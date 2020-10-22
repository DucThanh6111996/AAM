package com.viettel.it.controller;

import com.viettel.common.exception.SysException;
import com.viettel.controller.AppException;
import com.viettel.it.lazy.LazyDataModelBaseNew;
import com.viettel.it.model.*;

import java.io.*;
import java.util.*;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import com.viettel.it.persistence.*;
import com.viettel.it.util.LogUtils;
import com.viettel.it.util.MessageUtil;
import com.viettel.util.SessionUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.model.LazyDataModel;

/**
 *
 * @author taitd
 *
 */
@ViewScoped
@ManagedBean
public class ItUserController implements Serializable {
    private static Logger logger = Logger.getLogger(ItUserController.class);

    @ManagedProperty(value = "#{itUsersServices}")
    private ItUsersServicesImpl itUserService;

    public ItUsersServicesImpl getItUserService() {
        return itUserService;
    }

    public void setItUserService(ItUsersServicesImpl itUserService) {
        this.itUserService = itUserService;
    }

    @ManagedProperty(value = "#{itRoleService}")
    private ItRolesServiceImpl itRolesService;

    public ItRolesServiceImpl getItRolesService() {
        return itRolesService;
    }

    public void setItRolesService(ItRolesServiceImpl itRolesService) {
        this.itRolesService = itRolesService;
    }

    @ManagedProperty(value = "#{itUserRoleService}")
    private ItUserRoleServiceImpl itUserRoleService;

    public ItUserRoleServiceImpl getItUserRoleService() {
        return itUserRoleService;
    }

    public void setItUserRoleService(ItUserRoleServiceImpl itUserRoleService) {
        this.itUserRoleService = itUserRoleService;
    }

    @ManagedProperty(value = "#{itBusGroupService}")
    private ItBusGroupServiceImpl itBusGroupService;

    public ItBusGroupServiceImpl getItBusGroupService() {
        return itBusGroupService;
    }

    public void setItBusGroupService(ItBusGroupServiceImpl itBusGroupService) {
        this.itBusGroupService = itBusGroupService;
    }

    @ManagedProperty(value = "#{itUserBusGroupService}")
    private ItUserBusGroupServiceImpl itUserBusGroupService;

    public ItUserBusGroupServiceImpl getItUserBusGroupService() {
        return itUserBusGroupService;
    }

    public void setItUserBusGroupService(ItUserBusGroupServiceImpl itUserBusGroupService) {
        this.itUserBusGroupService = itUserBusGroupService;
    }

    private LazyDataModel<ItUsers> lazyDataModel;
    private LazyDataModel<ItRoles> lazyDataRole;
    private ItUsers selectedObj = new ItUsers();
    private ItUsers newObj;

    private boolean isEdit;

    private Long searchId;
    private String searchUserName;
    private String searchFullName;
    private String searchEmail;
    private String searchPhone;
    private String searchStaffCode;
    private String searchStatus;

    private String username;
    private String roleString;
    private String businessGroupString;
    private List<ItRoles> listRoles;
    private List<String> listRolesSelected;
    private List<ItBusinessGroup> listBusinessGroups;
    private List<String> listBGSelected;

    private ItUsers selectedUser;

    @PostConstruct
    public void onStart() {
        checkIsAdmin();
        username = SessionUtil.getCurrentUsername() == null ? "N/A" : SessionUtil.getCurrentUsername();
        clear();
        selectedUser = new ItUsers();
        //((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("form").findComponent("objectTable")).setFirst(0);
        Map<String, String> filters = new HashMap<>();
        lazyDataModel = new LazyDataModelBaseNew<>(itUserService, filters, null);
    }

    public void search() {
        //((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("form").findComponent("objectTable")).setFirst(0);
        Map<String, String> filters = new HashMap<>();

        if (StringUtils.isNotEmpty(searchUserName))
            filters.put("userName", searchUserName);
        if (StringUtils.isNotEmpty(searchFullName))
            filters.put("fullName", searchFullName);
        if (StringUtils.isNotEmpty(searchEmail))
            filters.put("email", searchEmail);
        if (StringUtils.isNotEmpty(searchPhone))
            filters.put("phone", searchPhone);
        if (StringUtils.isNotEmpty(searchStaffCode))
            filters.put("staffCode", searchStaffCode);
        if (StringUtils.isNotEmpty(searchStatus))
            filters.put("status", searchStatus);

        lazyDataModel = new LazyDataModelBaseNew<>(itUserService, filters, null);
    }

    public void clear() {
        isEdit = false;
        newObj = new ItUsers();
        selectedUser = new ItUsers();
    }

    public boolean checkIsAdmin(){
        if(new SessionUtil().isItBusinessAdmin()){
            return  true;
        }else
            return false;
    }

    public void view(ItUsers obj) {

        selectedObj = obj;
        roleString = null;
        if(obj.getLstUserRole() != null && obj.getLstUserRole().size() > 0){
            int index = 0;
            for(int i = 0; i < obj.getLstUserRole().size() ; i++){
                if(obj.getLstUserRole().get(i).getRole() != null && index == 0){
                    roleString = obj.getLstUserRole().get(i).getRole().getRoleCode();
                    index ++;
                }
                else if(obj.getLstUserRole().get(i).getRole() != null && index == 1){
                    roleString += ", " + obj.getLstUserRole().get(i).getRole().getRoleCode();
                }
            }
        }
        businessGroupString = null;
        if(obj.getLstUserBusinessGroup() != null && obj.getLstUserBusinessGroup().size() > 0){
            int index = 0;
            for(int i = 0; i < obj.getLstUserBusinessGroup().size() ; i++){
                if(obj.getLstUserBusinessGroup().get(i).getBusinessGroup() != null && index == 0){
                    /*20190115_hoangnd_them thi truong_start*/
                    businessGroupString = obj.getLstUserBusinessGroup().get(i).getBusinessGroup().getBusinessGroupName();
                    /*20190115_hoangnd_them thi truong_end*/
                    index ++;
                }
                else if(obj.getLstUserBusinessGroup().get(i).getBusinessGroup() != null && index == 1){
                    /*20190115_hoangnd_them thi truong_start*/
                    businessGroupString += ", " + obj.getLstUserBusinessGroup().get(i).getBusinessGroup().getBusinessGroupName();
                    /*20190115_hoangnd_them thi truong_end*/
                }
            }
        }

    }

    public void showMapRole(ItUsers obj) {

        selectedObj = obj;
        listRolesSelected = new ArrayList<String>();
        if(obj.getLstUserRole() != null && obj.getLstUserRole().size() > 0){
            for(int i = 0; i < obj.getLstUserRole().size() ; i++){
                listRolesSelected.add(obj.getLstUserRole().get(i).getId().getRoleId().toString());
            }
        }
        try {
            listRoles = itRolesService.findList();
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void mapRole() {
        FacesMessage msg = null;
        try {
            if(selectedObj.getLstUserRole() != null && selectedObj.getLstUserRole().size() > 0){
                for(int i = 0; i < selectedObj.getLstUserRole().size() ; i++){
                    ItUserRole tmp = new ItUserRole();
                    ItUserRoleId tmpUserRole = new ItUserRoleId();
                    tmpUserRole.setRoleId(selectedObj.getLstUserRole().get(i).getRole().getRoleId());
                    tmpUserRole.setUserId(selectedObj.getUserId());
                    tmp.setId(tmpUserRole);
                    itUserRoleService.delete(tmp);
                }
            }
            if(listRolesSelected.size() > 0){
                for(int i = 0; i < listRolesSelected.size(); i++){
                    ItUserRole tmp = new ItUserRole();
                    ItUserRoleId tmpUserRole = new ItUserRoleId();
                    tmpUserRole.setRoleId(Long.parseLong(listRolesSelected.get(i)));
                    tmpUserRole.setUserId(selectedObj.getUserId());
                    tmp.setId(tmpUserRole);
                    itUserRoleService.saveOrUpdate(tmp);
                }
            }
            Map<String, String> filters = new HashMap<>();
            lazyDataModel = new LazyDataModelBaseNew<>(itUserService, filters, null);
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("mapping.successful"), "");
            RequestContext.getCurrentInstance().execute("PF('mapRoleDialog').hide()");
        } catch (SysException | AppException e) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("mapping.fail"), "");
            logger.error(e.getMessage(), e);
        } finally {
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
        }
    }

    private boolean validateData() {
        boolean val = true;
        // Kiem tra xem cac truong da duoc nhap day du du lieu chua
        if (selectedUser.getUserName() == null
                || selectedUser.getUserName().trim().isEmpty()
                || selectedUser.getFullName() == null
                || selectedUser.getFullName().trim().isEmpty()
                || selectedUser.getStaffCode() == null
                || selectedUser.getPhone() == null
                || selectedUser.getPhone().trim().isEmpty()
                || selectedUser.getEmail() == null
                || selectedUser.getEmail().trim().isEmpty()) {
            MessageUtil.setErrorMessageFromRes("label.error.no.input.value");
            val = false;
        }

        return val;
    }

    public void prepareEditUser(ItUsers users){
        isEdit = true;
        if(users != null){
            selectedUser =users;
        }
    }

    public void saveUser() {
        ItUsers user = new ItUsers();
        try {
            Date startTime = new Date();
            if(isEdit){
                user.setUserId(selectedUser.getUserId());
            }
            if(validateData()){
                user.setUserName(selectedUser.getUserName());
                user.setFullName(selectedUser.getFullName());
                user.setEmail(selectedUser.getEmail());
                user.setPhone(selectedUser.getPhone());
                user.setStaffCode(selectedUser.getStaffCode());
                user.setStatus(1l);
                itUserService.saveOrUpdate(user);


                /*
			    Ghi log tac dong nguoi dung
                */
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItUserController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            (isEdit ? LogUtils.ActionType.UPDATE : LogUtils.ActionType.CREATE),
                            user.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

                MessageUtil.setInfoMessageFromRes("label.action.updateOk");
                RequestContext.getCurrentInstance().execute("PF('dlgUserInfo').hide()");
                clear();
                Map<String, String> filters = new HashMap<>();
                lazyDataModel = new LazyDataModelBaseNew<>(itUserService, filters, null);
            }
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
            MessageUtil.setErrorMessageFromRes("label.action.updateFail");
        }

    }

    public void prepareDel(ItUsers user){
        if(user != null){
            selectedUser = user;
        }
    }

    public void delUser(){
        if (selectedUser != null) {
            try {
                Date startTime = new Date();
                itUserService.delete(selectedUser);

                /*
			    Ghi log tac dong nguoi dung
                */
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItUserController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            LogUtils.ActionType.DELETE, selectedUser.toString(),
                            LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

                selectedUser = new ItUsers();
                MessageUtil.setInfoMessageFromRes("label.action.delelteOk");
            } catch (Exception e) {
                MessageUtil.setErrorMessageFromRes("label.action.deleteFail");
                logger.error(e.getMessage(), e);
            }
        }
    }
    public void showMapBG(ItUsers obj) {

        selectedObj = obj;
        listBGSelected = new ArrayList<String>();
        if(obj.getLstUserBusinessGroup() != null && obj.getLstUserBusinessGroup().size() > 0){
            for(int i = 0; i < obj.getLstUserBusinessGroup().size() ; i++){
                listBGSelected.add(obj.getLstUserBusinessGroup().get(i).getId().getBusinessId().toString());
            }
        }
        /*20190110_hoangnd_them thi truong_start*/
        try {
            listBusinessGroups = itBusGroupService.findLstBusGroup(obj);
            if(CollectionUtils.isNotEmpty(listBusinessGroups)) {
                Collections.sort(listBusinessGroups, new Comparator<ItBusinessGroup>() {
                    @Override
                    public int compare(final ItBusinessGroup object1, final ItBusinessGroup object2) {
                        return object1.getBusinessGroupName().compareTo(object2.getBusinessGroupName());
                    }
                });
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        /*20190110_hoangnd_them thi truong_end*/
    }

    public void mapBG() {
        FacesMessage msg = null;
        try {
            if(selectedObj.getLstUserBusinessGroup() != null && selectedObj.getLstUserBusinessGroup().size() > 0){
                for(int i = 0; i < selectedObj.getLstUserBusinessGroup().size() ; i++){
                    ItUserBusinessGroup tmp = new ItUserBusinessGroup();
                    ItUserBusinessGroupId tmpUserBG = new ItUserBusinessGroupId();
                    tmpUserBG.setBusinessId(selectedObj.getLstUserBusinessGroup().get(i).getBusinessGroup().getBusinessId());
                    tmpUserBG.setUserId(selectedObj.getUserId());
                    tmp.setId(tmpUserBG);
                    itUserBusGroupService.delete(tmp);
                }
            }
            if(listBGSelected.size() > 0){
                for(int i = 0; i < listBGSelected.size(); i++){
                    ItUserBusinessGroup tmp = new ItUserBusinessGroup();
                    ItUserBusinessGroupId tmpUserBG = new ItUserBusinessGroupId();
                    tmpUserBG.setBusinessId(Long.parseLong(listBGSelected.get(i)));
                    tmpUserBG.setUserId(selectedObj.getUserId());
                    tmp.setId(tmpUserBG);
                    itUserBusGroupService.saveOrUpdate(tmp);
                }
            }
            Map<String, String> filters = new HashMap<>();
            lazyDataModel = new LazyDataModelBaseNew<>(itUserService, filters, null);
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("mapping.successful"), "");
            RequestContext.getCurrentInstance().execute("PF('mapBGDialog').hide()");
        } catch (SysException | AppException e) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("mapping.fail"), "");
            logger.error(e.getMessage(), e);
        } finally {
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
        }
    }

    public void cancel(int type){
        Map<String, String> filters = new HashMap<>();
        lazyDataModel = new LazyDataModelBaseNew<>(itUserService, filters, null);
        if(type == 0 ){
            RequestContext.getCurrentInstance().execute("PF('mapRoleDialog').hide()");
        }
        else if (type == 1){
            RequestContext.getCurrentInstance().execute("PF('mapBGDialog').hide()");
        }
    }

    public String displayUserType(ItUsers user) {
        String type = MessageUtil.getResourceBundleMessage("label.user.normal");
        if (checkAdminGroup(user)) {
            type = MessageUtil.getResourceBundleMessage("label.user.admin.group");
        } else if (new SessionUtil().checkRole("")) {
            type = MessageUtil.getResourceBundleMessage("label.user.super.admin");
        }
        return type;
    }

    private boolean checkAdminGroup(ItUsers user) {
        boolean check = false;
        if (user.getLstUserBusinessGroup() != null && !user.getLstUserBusinessGroup().isEmpty()) {
            check = true;
        }
        return check;
    }

    public LazyDataModel<ItUsers> getLazyDataModel() {
        return lazyDataModel;
    }

    public void setLazyDataModel(LazyDataModel<ItUsers> lazyDataModel) {
        this.lazyDataModel = lazyDataModel;
    }

    public ItUsers getSelectedObj() {
        return selectedObj;
    }

    public void setSelectedObj(ItUsers selectedObj) {
        this.selectedObj = selectedObj;
    }

    public ItUsers getNewObj() {
        return newObj;
    }

    public void setNewObj(ItUsers newObj) {
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

    public String getSearchUserName() {
        return searchUserName;
    }

    public void setSearchUserName(String searchUserName) {
        this.searchUserName = searchUserName;
    }

    public String getSearchFullName() {
        return searchFullName;
    }

    public void setSearchFullName(String searchFullName) {
        this.searchFullName = searchFullName;
    }

    public String getSearchEmail() {
        return searchEmail;
    }

    public void setSearchEmail(String searchEmail) {
        this.searchEmail = searchEmail;
    }

    public String getSearchPhone() {
        return searchPhone;
    }

    public void setSearchPhone(String searchPhone) {
        this.searchPhone = searchPhone;
    }

    public String getSearchStaffCode() {
        return searchStaffCode;
    }

    public void setSearchStaffCode(String searchStaffCode) {
        this.searchStaffCode = searchStaffCode;
    }

    public String getSearchStatus() {
        return searchStatus;
    }

    public void setSearchStatus(String searchStatus) {
        this.searchStatus = searchStatus;
    }

    public String getRoleString() {
        return roleString;
    }

    public void setRoleString(String roleString) {
        this.roleString = roleString;
    }

    public String getBusinessGroupString() {
        return businessGroupString;
    }

    public void setBusinessGroupString(String businessGroupString) {
        this.businessGroupString = businessGroupString;
    }

    public List<ItRoles> getListRoles() {
        return listRoles;
    }

    public void setListRoles(List<ItRoles> listRoles) {
        this.listRoles = listRoles;
    }

    public List<String> getListRolesSelected() {
        return listRolesSelected;
    }

    public void setListRolesSelected(List<String> listRolesSelected) {
        this.listRolesSelected = listRolesSelected;
    }

    public List<ItBusinessGroup> getListBusinessGroups() {
        return listBusinessGroups;
    }

    public void setListBusinessGroups(List<ItBusinessGroup> listBusinessGroups) {
        this.listBusinessGroups = listBusinessGroups;
    }

    public List<String> getListBGSelected() {
        return listBGSelected;
    }

    public void setListBGSelected(List<String> listBGSelected) {
        this.listBGSelected = listBGSelected;
    }

    public ItUsers getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(ItUsers selectedUser) {
        this.selectedUser = selectedUser;
    }

}
