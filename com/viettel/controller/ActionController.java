package com.viettel.controller;

// Created May 5, 2016 4:56:37 PM by quanns2

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.ibm.team.filesystem.client.FileSystemCore;
import com.ibm.team.filesystem.client.IFileContentManager;
import com.ibm.team.filesystem.common.IFileItem;
import com.ibm.team.filesystem.common.IFileItemHandle;
import com.ibm.team.repository.client.IItemManager;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.client.TeamPlatform;
import com.ibm.team.repository.common.*;
import com.ibm.team.repository.common.UUID;
import com.ibm.team.repository.common.internal.util.ItemStore;
import com.ibm.team.scm.client.*;
import com.ibm.team.scm.common.*;
import com.ibm.team.scm.common.dto.IBaselineSetSearchCriteria;
import com.ibm.team.scm.common.dto.IWorkspaceSearchCriteria;
import com.ibm.team.scm.common.internal.BaselineSetHandle;
import com.ibm.team.scm.common.internal.dto.WorkspaceSearchCriteria;
import com.ibm.team.scm.common.internal.impl.BaselineSetImpl;
import com.mchange.v1.util.SimpleMapEntry;
import com.viettel.MapUserCountryServiceImpl;
import com.viettel.bean.*;
import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.gnoc.cr.*;
import com.viettel.gnoc.cr.service.CrOutputForQLTNDTO;
import com.viettel.iim.services.main.IimServices_PortType;
import com.viettel.it.model.CatConfig;
import com.viettel.it.persistence.CatConfigServiceImpl;
import com.viettel.it.persistence.MapUserCountryServiceImpl;
import com.viettel.it.persistence.common.CatCountryServiceImpl;
import com.viettel.it.util.*;
import com.viettel.it.util.LogUtils;
import com.viettel.lazy.LazyAction;
import com.viettel.lazy.LazyActionModule;
import com.viettel.lazy.LazyModule;
import com.viettel.model.*;
import com.viettel.persistence.*;
import com.viettel.util.*;
import com.viettel.util.PasswordEncoder;
import com.viettel.util.Util;
import com.viettel.voffice.Vo2AutoSignSystemImpl;
import com.viettel.voffice.Vo2AutoSignSystemImplService;
import com.viettel.voffice.Vo2AutoSignSystemImplServiceLocator;
import com.viettel.voffice.Vof2EntityUser;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.tabview.Tab;
import org.primefaces.component.tabview.TabView;
import org.primefaces.component.wizard.Wizard;
import org.primefaces.context.RequestContext;
import org.primefaces.event.*;
import org.primefaces.model.*;
import org.primefaces.model.Visibility;
import org.zeroturnaround.exec.ProcessExecutor;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.rpc.ServiceException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

/*import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;*/

/*import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;*/

/**
 * @author quanns2
 */
@ViewScoped
@ManagedBean
public class ActionController implements Serializable {
    // Process for Primefaces columToggler losing effect on filters
    private List<Boolean> columServicesExclusion; //Getters and setters
    private List<Boolean> columServices; //Getters and setters

    public void init() {
        this.columServicesExclusion = new ArrayList<Boolean>() {
            private static final long serialVersionUID = 1L;

            {
                add(Boolean.TRUE);
                add(Boolean.TRUE);
                add(Boolean.TRUE);
                add(Boolean.FALSE);
                add(Boolean.TRUE);
                add(Boolean.FALSE);
                add(Boolean.TRUE);
                add(Boolean.TRUE);
                add(Boolean.FALSE);
                add(Boolean.TRUE);
                add(Boolean.FALSE);
                add(Boolean.TRUE);
            }
        };
        this.columServices = new ArrayList<Boolean>() {
            private static final long serialVersionUID = 1L;

            {
                add(Boolean.TRUE);
                add(Boolean.TRUE);
                add(Boolean.TRUE);
                add(Boolean.FALSE);
                add(Boolean.TRUE);
                add(Boolean.FALSE);
                add(Boolean.TRUE);
                add(Boolean.TRUE);
                add(Boolean.FALSE);
                add(Boolean.TRUE);
                add(Boolean.FALSE);
                add(Boolean.TRUE);
            }
        };
    }

    private static Logger logger = .getLogger(ActionController.class);

    //<editor-fold defaultstate="collapsed" desc="Param">
    private static final long serialVersionUID = 1L;
    @ManagedProperty(value = "#{actionService}")
    ActionService actionService;

    @ManagedProperty(value = "#{actionModuleService}")
    ActionModuleService actionModuleService;

    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    @ManagedProperty(value = "#{checklistService}")
    ChecklistService checklistService;

    public void setChecklistService(ChecklistService checklistService) {
        this.checklistService = checklistService;
    }

    @ManagedProperty(value = "#{actionDetailDatabaseController}")
    ActionDetailDatabaseController actionDetailDatabaseController;

    // thenv_20180618_countryCode_start
    @ManagedProperty(value = "#{mapUserCountryService}")
    private MapUserCountryServiceImpl mapUserCountryService;

    public MapUserCountryServiceImpl getMapUserCountryService() {
        return mapUserCountryService;
    }

    public void setMapUserCountryService(MapUserCountryServiceImpl mapUserCountryService) {
        this.mapUserCountryService = mapUserCountryService;
    }
    // thenv_20180618_countryCode_end

    @ManagedProperty(value = "#{impactProcessService}")
    ImpactProcessService impactProcessService;

    public void setImpactProcessService(ImpactProcessService impactProcessService) {
        this.impactProcessService = impactProcessService;
    }

    public ActionDetailDatabaseController getActionDetailDatabaseController() {
        return actionDetailDatabaseController;
    }

    public void setActionDetailDatabaseController(ActionDetailDatabaseController actionDetailDatabaseController) {
        this.actionDetailDatabaseController = actionDetailDatabaseController;
    }

    @ManagedProperty(value = "#{actionDetailAppController}")
    ActionDetailAppController actionDetailAppController;

    public ActionDetailAppController getActionDetailAppController() {
        return actionDetailAppController;
    }

    public void setActionDetailAppController(ActionDetailAppController actionDetailAppController) {
        this.actionDetailAppController = actionDetailAppController;
    }

    private List<Long> unitId;

    @ManagedProperty(value = "#{verifyController}")
    VerifyController verifyController;

    public void setVerifyController(VerifyController verifyController) {
        this.verifyController = verifyController;
    }

    @ManagedProperty(value = "#{rstKpiService}")
    RstKpiService rstKpiService;

    public void setRstKpiService(RstKpiService rstKpiService) {
        this.rstKpiService = rstKpiService;
    }

    @ManagedProperty(value = "#{rstKpiDbSettingService}")
    RstKpiDbSettingService rstKpiDbSettingService;

    public void setRstKpiDbSettingService(RstKpiDbSettingService rstKpiDbSettingService) {
        this.rstKpiDbSettingService = rstKpiDbSettingService;
    }

    @ManagedProperty(value = "#{testCaseService}")
    TestCaseService testCaseService;

    public void setTestCaseService(TestCaseService testCaseService) {
        this.testCaseService = testCaseService;
    }

    @ManagedProperty(value = "#{testCaseController}")
    TestCaseController testCaseController;

    public void setTestCaseController(TestCaseController testCaseController) {
        this.testCaseController = testCaseController;
    }

    @ManagedProperty(value = "#{actionCustomGroupController}")
    ActionCustomGroupController actionCustomGroupController;

    public void setActionCustomGroupController(ActionCustomGroupController actionCustomGroupController) {
        this.actionCustomGroupController = actionCustomGroupController;
    }

    public void setIimService(IimService iimService) {
        this.iimService = iimService;
    }

    @ManagedProperty(value = "#{iimService}")
    IimService iimService;

    public void setAomClientService(AomClientService aomClientService) {
        this.aomClientService = aomClientService;
    }

    @ManagedProperty(value = "#{aomService}")
    AomClientService aomClientService;

    private Boolean isUctt;

    private LazyDataModel<Action> lazyDataModel;

    private LazyDataModel<ActionModule> lazyModel;

    private LazyDataModel<Module> lazyAppModel;

    private Action selectedObj;
    private ActionModule selectedModuleObj;

    public ActionModule getSelectedModuleObj() {
        return selectedModuleObj;
    }

    public void setSelectedModuleObj(ActionModule selectedModuleObj) {
        this.selectedModuleObj = selectedModuleObj;
    }

    private Action newObj;

    private boolean isEdit;

    private Long searchId;
    private String searchCrNumber;
    private String searchCrName;
    private Long searchActionType;
    private String searchCreatedBy;
    private Date searchCreatedTime;
    private String searchReason;
    private Date searchBeginTime;
    private String searchLocation;
    private Date searchEndTime;
    private String searchPerson;
    private String username = "";
    private String fullname = "";
    private String staffCode = "";
    private Map<Long, String> mapAppGroup;

    private HashMap<Long, HashMap<String, List<Module>>> hmApp = new HashMap<>();
    //    private List<Service> services;
    private List<Database> databases;
    private List<String> ips;
    //    private List<Module> dataTable = new ArrayList<>();
    private List<Module> dataTableFilters = new ArrayList<>();
    private Map<Long, Module> impactModules = new HashMap<>();
    private Map<Long, Module> normalImpactModules = new HashMap<>();

    private Map<Long, Module> exclusionModules = new HashMap<>();

    private Map<Long, Module> normalImpactModulesTemp = new HashMap<>();
    private Map<Long, Module> exclusionModulesTemp = new HashMap<>();

    private Map<Long, Module> testbedImpactModules = new HashMap<>();
    private List<Module> testbedDataTableFilters = new ArrayList<>();

    private List<Module> codeChangeModules;
    private List<Module> selectedCodeChangeModules;
    private List<Module> codeChangeModuleFilters;
    private Module selectedModule;

    //    private HashSet<Long> hmModuleId = new HashSet<>();
    private Long searhUnitId = 0L;
    private String searchGroupName;
    private String searchGroupCode;
    private String searchAppCode;
    private String searchServerIp;
    private String searchAppName;

    private DefaultTreeNode defaultRoot;
    private TreeNode[] listSelectedNodes;

    private DefaultTreeNode cklDefaultRoot;
    private TreeNode[] cklListSelectedNodes;

    private DefaultTreeNode cklDbDefaultRoot;
    private TreeNode[] cklDbListSelectedNodes;

    private List<SelectItem> checkListDbs;
    // private List<Checklist> selectedChecklistDbs;

    private List<SelectItem> listModuleSelected;

    private List<Module> selectedModdules;
    private List<Module> selectedBeforeModdules;

    private QueueChecklist selectedKpiDbSetting;

    private Integer actionType;
    private Integer kbGroup;

    private int counter;
    private boolean viewOnly;
    private boolean createNew;

    // longlt6 add
    private List<Checklist> appKpis;
    private HashMap<Long, HashSet<Long>> selectAppKpiMap = new HashMap<Long, HashSet<Long>>();

    // private HashSet<Long> selectApp =new HashSet<>();
    private String fitterAppName;
    private Long[] fitterKpiIds;
    private String fitterAppIp;

    private List<Vof2EntityUser> vof2EntityUsers;
    private Vof2EntityUser selectedVof2EntityUser;
    private String emailFind;
    private String passSso;
    private ResourceBundle bundle;

    private List<Action> kbUctts;
    private Action selectedKb;

    private String reasonUctt;
    private Date startTimeUctt;
    private Date endTimeUctt;
    private String userRollback;

    private List<SelectItem> moduleActions;
    private List<SelectItem> impactProcesses;
    private List<SelectItem> catCountrys;
    private List<SelectItem> serviceNames;

    public List<SelectItem> getMopService() {
        return mopService;
    }

    public void setMopService(List<SelectItem> mopService) {
        this.mopService = mopService;
    }

    private List<SelectItem> mopService;

    private IimServices_PortType iimServices_portType;

    //    private List<Service> services;
//    private Service[] selectedServices;
    private Database[] selectedDatabases;
    private Integer selectedAction; // 1 - reboot; 2 - shutdown
    private boolean isCheckUcServer;
    //    private String[] selectedIps;
    private String lstIpServer;

    private DualListModel<Module> dualListModel;
    private List<Module> sources;
    private List<Module> targets;
    private List<MdDependent> mdDependents;
    private Map<Long, ModuleDbDr> moduleDbDrMap;

    private Service selectedService;
    private BaseLine selectedBaseLine;
    private List<BaseLine> baseLines;
    private Multimap<Module, CodeChange> codeChanges;
    private String ibmUsername;
    private String ibmPassword;
    private DualListModel<String> dualListChangeFiles;
    private DualListModel<String> dualListRemoveFiles;


    private TreeNode root;
    private TreeNode[] selectedNodes;
    private static final String SUB_PARENT_NODE = "sub_parent";
    private static final String CHILD_NODE = "child";
    private boolean isNextExclude = false;
    // Render tabview edit
    private boolean reqParamater;
    private String tabViewActiveIndex = "0";
    private String msgExcludeUCTT;

    private List<Module> lstModules;

    // Don't load check status service by target module, load with normalImpactModules (use in method loadChild)
    private boolean isFirstHandleChange = false;
    private String keyActionSearch = null;
    // If handle change + add even then set value is true else false
    // If true then call changeOrder in class ActionDetailAppController.java
    private boolean handleChange = false;
    private boolean isLoadTarget = true;

    /*20180702_hoangnd_cau_hinh_user_tac_dong_start*/
    private List<OsAccount> lstOsAccounts;
    /*20180702_hoangnd_cau_hinh_user_tac_dong_end*/

    /*20180727_hoangnd_fix bug chon user tac dong_start*/
    Map<Long, String> mapUsernames = new HashMap<>();
    private String exeImpactStep;
    private String reasonImpactStep;

    /*20200911 ThanhTD*/
    private Integer mopType;

    /*20181023_hoangnd_approval impact step_end*/
    @PostConstruct
    public void onStart() {
        init();
        ExternalContext contextExternal = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest reqExternal = (HttpServletRequest) contextExternal.getRequest();
        String actionIdStr = reqExternal.getParameter("action");
        if (StringUtils.isNotEmpty(actionIdStr)) {
            reqParamater = true;
            tabViewActiveIndex = "1";
        } else {
            reqParamater = false;
            tabViewActiveIndex = "0";
        }

        unitId = new ArrayList<>();
        FacesContext context = FacesContext.getCurrentInstance();
        bundle = context.getApplication().getResourceBundle(context, "msg");

        String viewId = FacesContext.getCurrentInstance().getViewRoot().getViewId();
        isUctt = ("/faces/action/config/uctt.xhtml".equals(viewId)) ? Boolean.TRUE : Boolean.FALSE;

        // ThanhTD - Unikom
        String viewMop = FacesContext.getCurrentInstance().getViewRoot().getViewId();
        if ("/faces/template-mop/index.xhtml".equals(viewMop)) {
            mopType = 1;
        } else {
            mopType = 0;
        }
        
        Set<Long> allImpactMdIds = new HashSet<>(normalImpactModules.keySet());
        allImpactMdIds.addAll(testbedImpactModules.keySet());
        //tuanda38_20180620_start
        if (newObj == null) {
            lazyAppModel = new LazyModule(iimService, new HashMap<>(), allImpactMdIds, unitId, AamConstants.NATION_CODE.VIETNAM);

        } else {
            lazyAppModel = new LazyModule(iimService, new HashMap<>(), allImpactMdIds, unitId, newObj.getCatCountryBO().getCountryCode());
        }
        //tuanda38_20180620_end
        /*username = StringUtils.isEmpty(SessionUtil.getCurrentUsername()) ? "vof_test_tp2" : SessionUtil.getCurrentUsername();*/
        username = StringUtils.isEmpty(SessionUtil.getCurrentUsername()) ? "quanns2" : SessionUtil.getCurrentUsername();
        fullname = StringUtils.isEmpty(SessionUtil.getFullName()) ? "Nguyễn Sĩ Quân" : SessionUtil.getFullName();
        staffCode = StringUtils.isEmpty(SessionUtil.getStaffCode()) ? "168695" : SessionUtil.getStaffCode();
        ibmUsername = username;

        TreeObject parent = new TreeObject(MessageUtil.getResourceBundleMessage("list.app"), null);
        this.defaultRoot = new DefaultTreeNode(parent, null);

        TreeObject cklParent = new TreeObject(MessageUtil.getResourceBundleMessage("list.module"), null);
        this.cklDefaultRoot = new DefaultTreeNode(cklParent, null);

        TreeObject cklDbParent = new TreeObject(MessageUtil.getResourceBundleMessage("list.database"), null);
        this.cklDbDefaultRoot = new DefaultTreeNode(cklDbParent, null);

        clear();
        ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("mop").findComponent("lst").findComponent("objectTable")).setFirst(0);

        // thenv_20180630_hien thi theo CountryCode_start
        Map<String, Object> filters = new HashMap<>();
        List<String> lstCountry = mapUserCountryService.getListCountryForUser();
        filters.put("impactProcess.nationCode", lstCountry);

        // ThanhTD 20200911
        filters.put("mopType", mopType);
        // thenv_20180630_hien thi theo CountryCode_end

        lazyDataModel = new LazyAction(actionService, filters, isUctt ? Arrays.asList(Constant.ACTION_TYPE_KB_UCTT) : Arrays.asList(Constant.ACTION_TYPE_CR_NORMAL, Constant.ACTION_TYPE_CR_UCTT));

        selectedKpiDbSetting = new QueueChecklist();

        actionDetailAppController.viewSelectItems(this, true, false);
        actionDetailDatabaseController.viewSelectItems(this);

        // long add
        try {
            this.appKpis = this.rstKpiService.getListByType(1);
            Checklist rstKpi = null;
            for (Checklist kpi : this.appKpis) {
                if (AamConstants.CHECKLIST_CODE.WRITE_LOG.equals(kpi.getCode()))
                    rstKpi = kpi;
                // this.appKpis.remove(kpi);
            }

            if (rstKpi != null)
                this.appKpis.remove(rstKpi);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        Map<String, Object> kbFilters = new HashMap<>();
        kbFilters.put("signStatus", "5");
        kbFilters.put("actionType", String.valueOf(Constant.ACTION_TYPE_KB_UCTT));

        try {
            kbUctts = actionService.findList(kbFilters, new HashMap<String, String>());
            /*List<Action> actions = actionService.findList(kbFilters, new HashMap<>());
			kbUctts = new ArrayList<>();
			for (Action action : actions) {
				kbUctts.add(new SelectItem(action, action.getTdCode()));
			}*/
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }
        //20200201_Quytv7_Tu dong chia tai chay mop start
        catCountrys = new ArrayList<>();
        Map<String, Object> prFilters = new HashMap<>();
        prFilters.put("countryCode-EXAC", lstCountry);
        try {
            List<CatCountryBO> catCountryBOS = new CatCountryServiceImpl().findList(prFilters, new LinkedHashMap<>());
            for (CatCountryBO catCountryBO : catCountryBOS) {
                catCountrys.add(new SelectItem(catCountryBO, catCountryBO.getCountryCode()));
            }
            catCountrys.add(0, new SelectItem(null, MessageUtil.getResourceBundleMessage("common.choose")));
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        //20200201_Quytv7_Tu dong chia tai chay mop end

        impactProcesses = new ArrayList<>();
        prFilters.clear();
        prFilters.put("status", "1");
        // thenv_20180629_dung nationCode thay cho countryCode_start
        prFilters.put("nationCode", lstCountry);
        // thenv_20180629_dung nationCode thay cho countryCode_end
        try {
            List<ImpactProcess> processes = impactProcessService.findList(prFilters, new HashMap<>());
            for (ImpactProcess process : processes) {
                impactProcesses.add(new SelectItem(process, process.getName()));
            }
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }

        /*try {
            services = iimService.findService(newObj.getImpactProcess().getNationCode(), newObj.getImpactProcess().getUnitId());
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }

        try {
            databases = iimService.findDatabases(newObj.getImpactProcess().getNationCode(), newObj.getImpactProcess().getUnitId());
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }*/

        try {
            ips = iimService.findIps(AamConstants.NATION_CODE.VIETNAM, 689L);
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }

        dualListModel = new DualListModel<>();
        dualListRemoveFiles = new DualListModel<>();
        dualListChangeFiles = new DualListModel<>();
        try {
            // Check if exits request paramater
            if (reqParamater) {
                // Get data to Action table by Id
                com.viettel.model.Action action = new ActionServiceImpl().findById(Long.valueOf(actionIdStr));
                // If exist in DB then load tab view edit
                if (action != null) {
                    prepareEdit(action);
                } else { // Don't render tabview edit
                    reqParamater = false;
                }
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        //actionDetailAppController.setActionController(this);
        //actionDetailDatabaseController.setActionController(this);


    }

    /*20180628_hoangnd_cau_hinh_user_tac_dong_start*/

    //20200201_Quytv7_Tu donclg chia tai chay mop start
    public void handleChangeCountry(Action obj) {

        impactProcesses = new ArrayList<>();
        obj.setImpactProcess(null);
        if (obj.getCatCountryBO() != null) {
            Map<String, Object> prFilters = new HashMap<>();
            prFilters.put("status", "1");
            prFilters.put("nationCode", obj.getCatCountryBO().getCountryCode());
            try {
                List<ImpactProcess> processes = impactProcessService.findList(prFilters, new HashMap<>());
                if (processes == null || processes.isEmpty()) {
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            MessageUtil.getResourceBundleMessage("error.not.impact.no.process"), "");
                    FacesContext.getCurrentInstance().addMessage("commonGrowl", msg);

                } else {
                    impactProcesses = new ArrayList<>();
                    for (ImpactProcess process : processes) {
                        impactProcesses.add(new SelectItem(process, process.getName()));
                    }
                    Collections.shuffle(processes);
                    obj.setImpactProcess(processes.get(0));
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            serviceNames = new ArrayList<>();
            try{
                List<Service> lstServices = new IimClientServiceImpl().findServices(obj.getCatCountryBO().getCountryCode(), obj.getCatCountryBO().getUnitId());
                for (Service item: lstServices) {
                    SelectItem selectItem = new SelectItem();
                    selectItem.setLabel(item.getServiceName());
                    selectItem.setValue(item.getServiceName());
                    serviceNames.add(selectItem);
                }
            }catch (Exception ex){
                logger.info(ex.getMessage());
            }
            serviceNames.add(0, new SelectItem(null, MessageUtil.getResourceBundleMessage("common.choose")));
        }
    }

    public void handleChangeImpactProcess(Action obj) {

        try {
            logger.info(obj.getImpactProcess().getName());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    //20200201_Quytv7_Tu dong chia tai chay mop end
    public Module loadUser(Module module) throws Exception {

        String nationCode = AamConstants.VNM;
        if (newObj != null && newObj.getCatCountryBO() != null
                && newObj.getCatCountryBO().getCountryCode() != null
        ) {
            nationCode = newObj.getCatCountryBO().getCountryCode();
        }
        lstOsAccounts = new IimClientServiceImpl().findOsAccount(nationCode, module.getIpServer());
//        for (OsAccount account : lstOsAccounts) {
//            if (account.getUserType() != null && account.getUserType().equals(3)) {
//                module.setUsername(account.getUsername());
//                break;
//            }
//        }

        return module;
    }

    public void onCellEdit(CellEditEvent event) throws Exception {
        String oldValue = (String) event.getOldValue();
        String newValue = (String) event.getNewValue();

        if (newValue != null && !newValue.equals(oldValue)) {
            if (selectedBeforeModdules != null && !selectedBeforeModdules.isEmpty()) {
                String key = lazyAppModel.getRowKey(selectedBeforeModdules.get(0)).toString();
                lazyAppModel.getRowData(key).setUsername(newValue);
            }
        }
    }
    /*20180628_hoangnd_cau_hinh_user_tac_dong_end*/

    public void findVoUserByEmail() {
        ResourceBundle bundle = ResourceBundle.getBundle("config");
        Vo2AutoSignSystemImplService sv = new Vo2AutoSignSystemImplServiceLocator();
        Vo2AutoSignSystemImpl port = null;
        try {
            port = sv.getVo2AutoSignSystemImplPort(new URL(bundle.getString("ws_voffice_url")));

            Vof2EntityUser[] vof2EntityUsers = port.getListVof2UserByMail(new String[]{emailFind.contains("@") ? emailFind : emailFind + "@viettel.com.vn"});
            this.vof2EntityUsers = Arrays.asList(vof2EntityUsers);
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(), e);
        } catch (RemoteException e) {
            logger.error(e.getMessage(), e);
        } catch (ServiceException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void selectUser(int index) {
        switch (index) {
            case 1:
                newObj.setLabelSign1(selectedVof2EntityUser.getJobTile());
                newObj.setUserSign1(selectedVof2EntityUser.getStrEmail());
                newObj.setAdOrgId1(selectedVof2EntityUser.getAdOrgId());
                newObj.setAdOrgName1(selectedVof2EntityUser.getAdOrgName());
                RequestContext.getCurrentInstance().execute("PF('user1Overlay').hide()");
                selectedVof2EntityUser = null;
                vof2EntityUsers = null;
                emailFind = null;
                break;
            case 2:
                newObj.setLabelSign2(selectedVof2EntityUser.getJobTile());
                newObj.setUserSign2(selectedVof2EntityUser.getStrEmail());
                newObj.setAdOrgId2(selectedVof2EntityUser.getAdOrgId());
                newObj.setAdOrgName2(selectedVof2EntityUser.getAdOrgName());
                RequestContext.getCurrentInstance().execute("PF('user2Overlay').hide()");
                selectedVof2EntityUser = null;
                vof2EntityUsers = null;
                emailFind = null;
                break;
            case 3:
                newObj.setLabelSign3(selectedVof2EntityUser.getJobTile());
                newObj.setUserSign3(selectedVof2EntityUser.getStrEmail());
                newObj.setAdOrgId3(selectedVof2EntityUser.getAdOrgId());
                newObj.setAdOrgName3(selectedVof2EntityUser.getAdOrgName());
                RequestContext.getCurrentInstance().execute("PF('user3Overlay').hide()");
                selectedVof2EntityUser = null;
                vof2EntityUsers = null;
                emailFind = null;
                break;
            default:
                break;
        }
    }

    public void onRowSelect(SelectEvent event) {
        FacesMessage msg = new FacesMessage("Car Selected", ((Vof2EntityUser) event.getObject()).getAdOrgName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void loadModuleByActionId(Action action) {
        lazyModel = new LazyActionModule(actionModuleService, action.getId());
    }

    public void prepareRollback(Action action) {
        this.reasonUctt = action.getReasonRollback();
        this.userRollback = action.getExeRollback();
        this.startTimeUctt = action.getStartTimeRollback();
        this.endTimeUctt = action.getEndTimeRollback();
    }

    //20200107_quytv7_bo sung phe duyet tac dong uctt start
    public void prepareApproveImpact(Action action) {
        if (!Util.isNullOrEmpty(action.getReasonImpactUctt())) {
            this.reasonUctt = action.getReasonImpactUctt();
        } else {
            this.reasonUctt = MessageUtil.getResourceBundleMessage("label.impact.type.xlsc");
        }
        if (!Util.isNullOrEmpty(action.getExeImpactUctt())) {
            this.userRollback = action.getReasonImpactUctt();
        } else {
            this.userRollback = action.getCreatedBy();
        }
        if (!Util.isNullOrEmpty(action.getStartTimeImpactUctt())) {
            this.startTimeUctt = action.getStartTimeImpactUctt();
        } else {
            this.startTimeUctt = action.getBeginTime();
        }
        if (!Util.isNullOrEmpty(action.getEndTimeImpactUctt())) {
            this.endTimeUctt = action.getEndTimeImpactUctt();
        } else {
            this.endTimeUctt = action.getEndTime();
        }
    }

    public void prepareImpactStep(Action action) {
        this.exeImpactStep = action.getExeImpactStep();
        this.reasonImpactStep = action.getReasonImpactStep();
    }

    /*20181023_hoangnd_approval impact step_end*/
    public String findAppCode(Long appId) {
        try {
//            return mapApp.get(appId).getAppCode();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);

        }
        return "";
    }

    public String findAppGroupName(Long appId) {
        try {
//            return mapApp.get(appId).getAppGroupName();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);

        }
        return "";
    }

    public void search() {
        ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("mop").findComponent("lst").findComponent("objectTable")).setFirst(0);
        // thenv_20180630_start
        Map<String, Object> filters = new HashMap<>();
        // thenv_20180630_end

        if (StringUtils.isNotEmpty(searchCrNumber))
            filters.put("crNumber", searchCrNumber);

        if (StringUtils.isNotEmpty(searchCrName))
            filters.put("crName", searchCrName);

        if (StringUtils.isNotEmpty(searchCreatedBy))
            filters.put("createdBy", searchCreatedBy);

        if (StringUtils.isNotEmpty(searchLocation))
            filters.put("location", searchLocation);

        if (StringUtils.isNotEmpty(searchPerson))
            filters.put("createdBy", searchPerson);

        lazyDataModel = new LazyAction(actionService, filters, isUctt ? Arrays.asList(Constant.ACTION_TYPE_KB_UCTT) : Arrays.asList(Constant.ACTION_TYPE_CR_NORMAL, Constant.ACTION_TYPE_CR_UCTT));

    }

    public void clearOpt() {
        try {
            int counter = 0;
            for (Module selectedModdule : selectedModdules) {
//                if (impactModules.containsValue(selectedModdule)) {
                if (normalImpactModules.containsValue(selectedModdule)) {
                    counter++;
//                    impactModules.remove(selectedModdule);
                    normalImpactModules.remove(selectedModdule);
//                    hmModuleId.remove(selectedModdule.getModuleId());
                    dataTableFilters.remove(selectedModdule);
//                    impactModules.remove(selectedModdule.getModuleId());
                    normalImpactModules.remove(selectedModdule.getModuleId());
                }
            }

            loadTestbedModule();

            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, counter + " " + MessageUtil.getResourceBundleMessage("module.deleted"), "");
            FacesContext.getCurrentInstance().addMessage("moduleGrowl", msg);

            selectedBeforeModdules = new ArrayList<>();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            // MessageUtil.setErrorMessage(e.toString());
        }
    }

    public void updateAction() {
        if (kbGroup == null || kbGroup < 1) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    MessageUtil.getResourceBundleMessage("mop.db.kbgroup.null"), "");
            FacesContext.getCurrentInstance().addMessage("moduleGrowl", msg);
            return;
        }

        if (selectedModdules == null || selectedModdules.isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    MessageUtil.getResourceBundleMessage("mop.app.select.null"), "");
            FacesContext.getCurrentInstance().addMessage("moduleGrowl", msg);
            return;
        }

        try {
            // this.dataTable.clear();
            // this.hmModuleId.clear();

            for (Module moddule : selectedModdules) {

                if (Arrays.asList(AamConstants.MODULE_GROUP_ACTION.STOP_START, AamConstants.MODULE_GROUP_ACTION.STOP_START_UPCODE, AamConstants.MODULE_GROUP_ACTION.RESTART_STOP_START
                        , AamConstants.MODULE_GROUP_ACTION.RESTART_STOP_START_UPCODE, AamConstants.MODULE_GROUP_ACTION.RESTART,
                        AamConstants.MODULE_GROUP_ACTION.RESTART_UPCODE, AamConstants.MODULE_GROUP_ACTION.START).contains(actionType)) {
                    String clearCache = moddule.getDeleteCache();
                    if (StringUtils.isNotEmpty(clearCache) && !clearCache.equals(Constant.NA_VALUE)) {
                        if (!clearCache.endsWith("work/*") && !clearCache.endsWith("work/Catalina/*")
                                && !clearCache.endsWith("work/Catalina/") && !clearCache.endsWith("work/Catalina")
                                && !clearCache.endsWith("work/Catalina/localhost/*")
                                && !clearCache.endsWith("work/Catalina/localhost/")
                                && !clearCache.endsWith("work/Catalina/localhost")
                                && !clearCache.endsWith("cache/*")) {
                            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    moddule.getModuleName() + ": " + MessageUtil.getResourceBundleMessage("command.clear.cache.invalid") + " " + moddule.getDeleteCache(),
                                    "");
                            FacesContext.getCurrentInstance().addMessage("moduleGrowl", msg);
                            return;
                        }
                    }
                }

                // Cac hanh dong Restart
                if (Arrays.asList(AamConstants.MODULE_GROUP_ACTION.RESTART, AamConstants.MODULE_GROUP_ACTION.RESTART_UPCODE).contains(actionType)) {
                    String restartCmd = moddule.getRestartService();
                    if (restartCmd == null || restartCmd.trim().isEmpty()
                            || restartCmd.trim().toUpperCase().equals(Constant.NA_VALUE)) {
                        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                moddule.getModuleName() + ": " + MessageUtil.getResourceBundleMessage("command.restart.not.exist") + " " + moddule.getModuleName(), "");
                        FacesContext.getCurrentInstance().addMessage("moduleGrowl", msg);
                        return;
                    }
                }

                // Cac hanh dong stop
                if (Arrays.asList(AamConstants.MODULE_GROUP_ACTION.STOP_START, AamConstants.MODULE_GROUP_ACTION.STOP_START_UPCODE, AamConstants.MODULE_GROUP_ACTION.STOP).contains(actionType)) {
                    String stopCmd = moddule.getStopService();
                    if (stopCmd == null || stopCmd.trim().isEmpty() || stopCmd.trim().toUpperCase().equals(Constant.NA_VALUE)) {
                        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                moddule.getModuleName() + ": " + MessageUtil.getResourceBundleMessage("command.stop.app.not.exist") + " " + moddule.getModuleName(),
                                "");
                        FacesContext.getCurrentInstance().addMessage("moduleGrowl", msg);
                        return;
                    }
                }

                // Cac hanh dong start
                if (Arrays.asList(AamConstants.MODULE_GROUP_ACTION.STOP_START, AamConstants.MODULE_GROUP_ACTION.STOP_START_UPCODE, AamConstants.MODULE_GROUP_ACTION.START).contains(actionType)) {
                    String startCmd = moddule.getStartService();
                    if (startCmd == null || startCmd.trim().isEmpty() || startCmd.trim().toUpperCase().equals(Constant.NA_VALUE)) {
                        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, moddule.getModuleName()
                                + ": " + MessageUtil.getResourceBundleMessage("command.start.app.not.exist") + " " + moddule.getDeleteCache(), "");
                        FacesContext.getCurrentInstance().addMessage("moduleGrowl", msg);
                        return;
                    }
                }

            }

            int counter = 0;
            for (Module selectedModdule : selectedModdules) {
//                if (impactModules.containsValue(selectedModdule)) {
                if (normalImpactModules.containsValue(selectedModdule)) {
                    selectedModdule.setActionType(actionType);
                    selectedModdule.setKbGroup(kbGroup);
                    counter++;
                    /*
                     * dataTable.remove(selectedModdule);
                     * hmModuleId.remove(selectedModdule.getAppId());
                     * dataTableFilters.remove(selectedModdule);
                     */
                }
            }

            loadTestbedModule();

            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, counter + " " + MessageUtil.getResourceBundleMessage("module.updated"), "");
            FacesContext.getCurrentInstance().addMessage("moduleGrowl", msg);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            // MessageUtil.setErrorMessage(e.toString());
        }
    }

    public void fitter() {

        this.cklListSelectedNodes = null;
        String newfitterAppName = null;
        if (this.fitterAppName != null)
            newfitterAppName = ReplateVietNameseUtil.removeVietNamese(this.fitterAppName).trim();
        HashSet<Long> set = new HashSet<>();
        if (this.fitterKpiIds != null) {
            for (Long kId : this.fitterKpiIds) {
                set.add(kId);
            }
        }

        try {

            TreeObject treeObject;
            Module parent = new Module();
            this.cklDefaultRoot = new DefaultTreeNode(parent, null);
            List<TreeNode> allSelectNode = new ArrayList<>();

            for (Module app : this.impactModules.values()) {

                if (newfitterAppName != null) {
                    String newAppName = ReplateVietNameseUtil.removeVietNamese(app.getModuleName());
                    if (!newAppName.toUpperCase().contains(newfitterAppName.toUpperCase()))
                        continue;
                }

                if (fitterAppIp != null && !fitterAppIp.trim().isEmpty()) {
                    if (!app.getIpServer().contains(fitterAppIp))
                        continue;
                }

                if ("NEW MODULE IS DETECTED BY AUTO PROCESS".equals(app.getModuleName()))
                    continue;

                treeObject = new TreeObject(app.getModuleName(), app);
                TreeNode parentNode = new DefaultTreeNode(treeObject, this.cklDefaultRoot);

                Boolean hasChecked = false;
                Boolean hasNoChecked = false;
                for (Checklist kpi : appKpis) {
                    if (AamConstants.CHECKLIST_CODE.WRITE_LOG.equals(kpi.getCode()))
                        continue;
                    if (Constant.CODETAPTRUNG_TYPE.equals(app.getModuleTypeCode()) && !AamConstants.CHECKLIST_CODE.DISK.equals(kpi.getCode()))
                        continue;

                    if (!set.isEmpty() && !set.contains(kpi.getId())) {
                        continue;
                    }

                    Checklist checklist = new Checklist();
                    checklist.setId(kpi.getId());
                    checklist.setCode(kpi.getCode());
                    checklist.setName(kpi.getName());
                    checklist.setType(kpi.getType());
                    checklist.setModuleId(app.getModuleId());

                    treeObject = new TreeObject(checklist.getName(), checklist);
                    TreeNode cklNode = new DefaultTreeNode(treeObject, parentNode);

                    if (this.selectAppKpiMap.containsKey(app.getModuleId())
                            && this.selectAppKpiMap.get(app.getModuleId()).contains(kpi.getId())) {
                        cklNode.setSelected(true);
                    }

                    if (cklNode.isSelected()) {
                        allSelectNode.add(cklNode);
                        hasChecked = true;
                    } else {
                        hasNoChecked = true;
                    }
                }

                if (hasChecked && !hasNoChecked) {
                    parentNode.setSelected(true);
                } else if (hasChecked && hasNoChecked) {
                    parentNode.setPartialSelected(true);
                }

                if (parentNode.isSelectable()) {
                    allSelectNode.add(parentNode);
                }
            }
            this.cklListSelectedNodes = new TreeNode[allSelectNode.size()];
            allSelectNode.toArray(cklListSelectedNodes);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), "");
            FacesContext.getCurrentInstance().addMessage("moduleGrowl", msg);
        }
    }

    // end longlt6 add

    private void addChcklistObj(Checklist cObj) {
        if (this.selectAppKpiMap.containsKey(cObj.getModuleId())) {
            this.selectAppKpiMap.get(cObj.getModuleId()).add(cObj.getId());
        } else {
            HashSet<Long> set = new HashSet<>();
            set.add(cObj.getId());
            this.selectAppKpiMap.put(cObj.getModuleId(), set);
        }
    }

    public void onAppNodeSelect(NodeSelectEvent event) {
        TreeNode node = event.getTreeNode();
        if (!(node.getData() instanceof TreeObject)) {
            return;
        }
        TreeObject treeObject = (TreeObject) node.getData();
        if (treeObject.getObj() instanceof Checklist) {
            Checklist cObj = (Checklist) treeObject.getObj();
            this.addChcklistObj(cObj);

        } else if (treeObject.getObj() instanceof Module) {

            // this.selectApp.add(( (ApplicationDetail)
            // treeObject.getObj()).getAppId());
            for (TreeNode child : node.getChildren()) {
                if (!(child.getData() instanceof TreeObject)) {
                    return;
                }
                TreeObject childObject = (TreeObject) child.getData();
                if (!(childObject.getObj() instanceof Checklist)) {
                    return;
                }
                this.addChcklistObj((Checklist) childObject.getObj());
            }
        }
    }

    private void removeChecklistObj(Checklist cObj) {
        if (this.selectAppKpiMap.containsKey(cObj.getModuleId())) {
            this.selectAppKpiMap.get(cObj.getModuleId()).remove(cObj.getId());
            if (this.selectAppKpiMap.get(cObj.getModuleId()).size() == 0) {
                this.selectAppKpiMap.remove(cObj.getModuleId());
            }
        }
    }

    public void onAppNodeUnselect(NodeUnselectEvent event) {
        TreeNode node = event.getTreeNode();
        if (!(node.getData() instanceof TreeObject)) {
            return;
        }
        TreeObject treeObject = (TreeObject) node.getData();
        if (treeObject.getObj() instanceof Checklist) {
            Checklist cObj = (Checklist) treeObject.getObj();
            this.removeChecklistObj(cObj);

        } else if (treeObject.getObj() instanceof Module) {

//			ApplicationDetail app = (ApplicationDetail) treeObject.getObj();
            // if(this.selectApp.contains(app.getAppId()))
            // this.selectApp.remove(app.getAppId());

            for (TreeNode child : node.getChildren()) {
                if (!(child.getData() instanceof TreeObject)) {
                    return;
                }
                TreeObject childObject = (TreeObject) child.getData();
                if (!(childObject.getObj() instanceof Checklist)) {
                    return;
                }
                this.removeChecklistObj((Checklist) childObject.getObj());
            }
        }
    }

    public void createTreeCklApp(TreeNode paNode, List<Module> allApp) {
        try {
            if ( /* !this.selectApp.isEmpty() || */ !this.selectAppKpiMap.isEmpty()) {
                List<Long> moduleNotExsits = new ArrayList<>();
                for (Long aLong : selectAppKpiMap.keySet()) {
                    if (!impactModules.keySet().contains(aLong))
                        moduleNotExsits.add(aLong);
                }

                for (Long moduleNotExsit : moduleNotExsits) {
                    selectAppKpiMap.remove(moduleNotExsit);
                }
            } else {
                if (newObj.getId() != null) {
                    List<Checklist> selectedChecklists = checklistService.findCheckListAppByAction(newObj.getId());
                    this.selectAppKpiMap.clear();

                    for (Checklist obj : selectedChecklists) {
                        if (this.selectAppKpiMap.containsKey(obj.getModuleId())) {
                            this.selectAppKpiMap.get(obj.getModuleId()).add(obj.getId());
                        } else {
                            HashSet<Long> set = new HashSet<>();
                            set.add(obj.getId());
                            this.selectAppKpiMap.put(obj.getModuleId(), set);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        // hmApp.clear();
        List<TreeNode> allSelectNode = new ArrayList<>();
        this.cklListSelectedNodes = null;
        TreeObject treeObject;
        for (Module app : allApp) {
            if ("NEW MODULE IS DETECTED BY AUTO PROCESS".equals(app.getModuleName()))
                continue;

            treeObject = new TreeObject(app.getModuleName(), app);
            TreeNode parentNode = new DefaultTreeNode(treeObject, paNode);

            Boolean hasChecked = false;
            Boolean hasNoChecked = false;
            for (Checklist kpi : appKpis) {
                if (AamConstants.CHECKLIST_CODE.WRITE_LOG.equals(kpi.getCode()))
                    continue;
                if (Constant.CODETAPTRUNG_TYPE.equals(app.getModuleTypeCode()) && !AamConstants.CHECKLIST_CODE.DISK.equals(kpi.getCode()))
                    continue;
                if (app.getOsType() != null && AamConstants.OS_TYPE.WINDOWS == app.getOsType() &&
                        Arrays.asList(AamConstants.CHECKLIST_CODE.IO, AamConstants.CHECKLIST_CODE.CPU_MODULE, AamConstants.CHECKLIST_CODE.RAM_MODULE).contains(kpi.getCode()))
                    continue;

                Checklist checklist = new Checklist();
                checklist.setId(kpi.getId());
                checklist.setCode(kpi.getCode());
                checklist.setName(kpi.getName());
                checklist.setType(kpi.getType());
                checklist.setModuleId(app.getModuleId());

                treeObject = new TreeObject(checklist.getName(), checklist);
                TreeNode cklNode = new DefaultTreeNode(treeObject, parentNode);

                if ((this.selectAppKpiMap.containsKey(app.getModuleId())
                        && this.selectAppKpiMap.get(app.getModuleId()).contains(kpi.getId())) || createNew) {
                    cklNode.setSelected(true);
                }

                if (cklNode.isSelected()) {
                    allSelectNode.add(cklNode);
                    hasChecked = true;
                    if (!(cklNode.getData() instanceof TreeObject)) {
                        return;
                    }
                    TreeObject childObject = (TreeObject) cklNode.getData();
                    if ((childObject.getObj() instanceof Checklist)) {
                        this.addChcklistObj(checklist);
                    }
                } else {
                    hasNoChecked = true;
                }
            }

            if (hasChecked && !hasNoChecked) {
                parentNode.setSelected(true);
            } else if (hasChecked && hasNoChecked) {
                parentNode.setPartialSelected(true);
            }

            if (parentNode.isSelected()) {
                allSelectNode.add(parentNode);
            }
        }

        this.cklListSelectedNodes = new TreeNode[allSelectNode.size()];
        allSelectNode.toArray(cklListSelectedNodes);

        createNew = false;
    }

    public void createTreeCklDb(TreeNode paNode, List<Module> allApp) {
        List<Checklist> selectedChecklists = new ArrayList<>();

        if (newObj.getId() != null) {
            if (cklDbListSelectedNodes != null && cklDbListSelectedNodes.length != 0) {
                selectedChecklists = new ArrayList<>();
                for (TreeNode treeNode : cklDbListSelectedNodes) {
                    if (((TreeObject) treeNode.getData()).getObj() instanceof Checklist) {
                        Checklist checklist = (Checklist) ((TreeObject) treeNode.getData()).getObj();
                        ServiceDatabase database = (ServiceDatabase) ((TreeObject) treeNode.getParent().getData()).getObj();

                        Checklist checklistNew = new Checklist();
                        checklistNew.setId(checklist.getId());
                        checklistNew.setModuleId(database.getServiceDbId());

                        selectedChecklists.add(checklistNew);
                    }
                }
            } else {
                try {
                    selectedChecklists = checklistService.findCheckListDbByAction(newObj.getId());
                } catch (AppException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

//        Set<Checklist> checklists = new HashSet<>();
//        Checklist checklist;

        Map<String, Long> appGroupCodes = new HashMap<>();
        for (Module app : allApp) {
            appGroupCodes.put(app.getServiceCode(), app.getServiceId());
        }

        TreeObject treeObject;
        for (Map.Entry<String, Long> entry : appGroupCodes.entrySet()) {
            try {
//                List<KpiDbSetting> kpiDbSettings = rstKpiDbSettingService.getlistByGroup(entry.getValue());
                List<QueueChecklist> queueChecklists = aomClientService.findChecklistQueues(Arrays.asList(entry.getValue()));

                Multimap<Integer, Long> multimap = HashMultimap.create();

                treeObject = new TreeObject(entry.getKey(), entry.getValue());
                treeObject.setIsLeaf(false);
                TreeNode parentNode = new DefaultTreeNode(treeObject, paNode);
                parentNode.setExpanded(true);

                List<String> serviceQueueIds = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(AppConfig.getInstance().getProperty("aom.queue.ids"));
                for (QueueChecklist queueChecklist : queueChecklists) {
                    if (!serviceQueueIds.contains(queueChecklist.getServiceId().toString()))
                        continue;
                    multimap.put(queueChecklist.getServiceId(), queueChecklist.getQueueId());

//                    checklist = checklistService.findById(kpiDbSetting.getKpiId());
//                    checklists.add(checklist);
                }

//                checklistService.saveOrUpdate(new ArrayList<>(checklists));

                for (Map.Entry<Integer, Collection<Long>> collEntry : multimap.asMap().entrySet()) {
                    TreeObject checklistTreeObject;
                    TreeNode cklparentNode = null;
//                    ServiceDatabase database;
                    for (QueueChecklist queueChecklist : queueChecklists) {
                        if (queueChecklist.getServiceId().equals(collEntry.getKey())) {
//                            database = kpiDb.getServiceDatabase();
//                            database = iimService.findServiceDbById(kpiDb.getViewDbId());

/*                            if (database == null) {
                                continue;
                            } else {*/
                            checklistTreeObject = new TreeObject(MessageUtil.getResourceBundleMessage("aom.queue.name." + queueChecklist.getServiceId()), queueChecklist);
//                            }
                            checklistTreeObject.setIsLeaf(false);
                            cklparentNode = new DefaultTreeNode(checklistTreeObject, parentNode);
                            break;
                        }
                    }

                    Collection<Long> kpis = collEntry.getValue();

                    Integer isCheckAllKpi = 0;

                    for (Long kpi : kpis) {
                        for (QueueChecklist queueChecklist : queueChecklists) {
                            if (!queueChecklist.getAppId().equals(entry.getValue()))
                                continue;

                            if (queueChecklist.getQueueId().equals(kpi) && queueChecklist.getServiceId().equals(collEntry.getKey())) {
//                                Checklist newChecklist = checklistService.findById(kpiDb.getKpiId());

                                TreeObject cklTreeObject = new TreeObject(queueChecklist.getQueueCode(), queueChecklist);
                                cklTreeObject.setExtObj(queueChecklist);
                                cklTreeObject.setIsLeaf(true);
                                TreeNode ckl = new DefaultTreeNode(cklTreeObject, cklparentNode);

                                for (Checklist selectedChecklist : selectedChecklists) {
//                                    if (selectedChecklist.getId().equals(kpi) && selectedChecklist.getModuleId().equals(collEntry.getKey())) {
                                    if (selectedChecklist.getId().equals(kpi)) {
                                        ckl.setSelected(true);
                                    }
                                }

                                if (ckl.isSelected()) {
                                    if (isCheckAllKpi == 0)
                                        isCheckAllKpi = 2;
                                } else {
                                    if (isCheckAllKpi == 2)
                                        isCheckAllKpi = 1;
                                }

                            }
                        }
                    }

                    if (isCheckAllKpi == 2 && cklparentNode != null)
                        cklparentNode.setSelected(true);
                    else if (isCheckAllKpi == 1 && cklparentNode != null)
                        cklparentNode.setPartialSelected(true);
                }
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void changeCheckListDb(ValueChangeEvent event) {
        List<Object> oldValue = (List<Object>) event.getOldValue();
        List<Object> newValue = (List<Object>) event.getNewValue();
//		logger.info(oldValue + "\t" + newValue + "\t" + newValue.getClass().getName());

        if (oldValue == null)
            oldValue = new ArrayList<>();
        if (newValue == null)
            newValue = new ArrayList<>();

        for (Object checklist : newValue) {
            logger.info(checklist);
            if (!oldValue.contains(checklist)) {
                List<TreeNode> treeNodes = cklDefaultRoot.getChildren();
                for (TreeNode treeNode : treeNodes) {
                    List<TreeNode> childNodes = treeNode.getChildren();

                    for (TreeNode childNode : childNodes) {
                        if (((Checklist) ((TreeObject) childNode.getData()).getObj()).getCode().equals(checklist)) {
                            childNode.setSelected(true);
                            childNode.getParent().setPartialSelected(true);
                        }
                    }
                }
            }
        }
    }

    public void chooseOpt() {
        if (kbGroup == null || kbGroup < 1) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    MessageUtil.getResourceBundleMessage("mop.db.kbgroup.null"), "");
            FacesContext.getCurrentInstance().addMessage("moduleGrowl", msg);
            return;
        }

        if (selectedBeforeModdules == null || selectedBeforeModdules.isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    MessageUtil.getResourceBundleMessage("mop.app.select.null"), "");
            FacesContext.getCurrentInstance().addMessage("moduleGrowl", msg);
            return;
        }

        for (Module moddule : selectedBeforeModdules) {

//            if (Arrays.asList(0, 2, 3, 4, 5, 6, 8).contains(actionType)) {
            if (Arrays.asList(AamConstants.MODULE_GROUP_ACTION.STOP_START, AamConstants.MODULE_GROUP_ACTION.STOP_START_UPCODE, AamConstants.MODULE_GROUP_ACTION.RESTART_STOP_START
                    , AamConstants.MODULE_GROUP_ACTION.RESTART_STOP_START_UPCODE, AamConstants.MODULE_GROUP_ACTION.RESTART,
                    AamConstants.MODULE_GROUP_ACTION.RESTART_UPCODE, AamConstants.MODULE_GROUP_ACTION.START).contains(actionType)) {
                String clearCache = moddule.getDeleteCache();
                if (StringUtils.isNotEmpty(clearCache) && !clearCache.equals(Constant.NA_VALUE)) {
                    if (!clearCache.endsWith("work/*") && !clearCache.endsWith("work/Catalina/*")
                            && !clearCache.endsWith("work/Catalina/") && !clearCache.endsWith("work/Catalina")
                            && !clearCache.endsWith("work/Catalina/localhost/*")
                            && !clearCache.endsWith("work/Catalina/localhost/")
                            && !clearCache.endsWith("work/Catalina/localhost")
                            && !clearCache.endsWith("cache/*")) {
                        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                moddule.getModuleName() + ": " + MessageUtil.getResourceBundleMessage("command.clear.cache.invalid") + " " + moddule.getDeleteCache(), "");
                        FacesContext.getCurrentInstance().addMessage("moduleGrowl", msg);
                        return;
                    }
                }
            }

            // Cac hanh dong Restart
            if (Arrays.asList(AamConstants.MODULE_GROUP_ACTION.RESTART, AamConstants.MODULE_GROUP_ACTION.RESTART_UPCODE).contains(actionType)) {
                String restartCmd = moddule.getRestartService();
                if (restartCmd == null || restartCmd.trim().isEmpty()
                        || restartCmd.trim().toUpperCase().equals(Constant.NA_VALUE)) {
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            moddule.getModuleName() + ": " + MessageUtil.getResourceBundleMessage("command.restart.not.exist"), "");
                    FacesContext.getCurrentInstance().addMessage("moduleGrowl", msg);
                    return;
                }
            }

            // Cac hanh dong stop
            if (Arrays.asList(AamConstants.MODULE_GROUP_ACTION.STOP_START, AamConstants.MODULE_GROUP_ACTION.STOP_START_UPCODE, AamConstants.MODULE_GROUP_ACTION.STOP).contains(actionType)) {
                String stopCmd = moddule.getStopService();
                if (stopCmd == null || stopCmd.trim().isEmpty() || stopCmd.trim().toUpperCase().equals(Constant.NA_VALUE)) {
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            moddule.getModuleName() + ": " + MessageUtil.getResourceBundleMessage("command.stop.app.not.exist"),
                            "");
                    FacesContext.getCurrentInstance().addMessage("moduleGrowl", msg);
                    return;
                }
            }

            // Cac hanh dong start
            if (Arrays.asList(AamConstants.MODULE_GROUP_ACTION.STOP_START, AamConstants.MODULE_GROUP_ACTION.STOP_START_UPCODE, AamConstants.MODULE_GROUP_ACTION.START).contains(actionType)) {
                String startCmd = moddule.getStartService();
                if (startCmd == null || startCmd.trim().isEmpty() || startCmd.trim().toUpperCase().equals(Constant.NA_VALUE)) {
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            moddule.getModuleName() + ": " + MessageUtil.getResourceBundleMessage("command.start.app.not.exist"),
                            "");
                    FacesContext.getCurrentInstance().addMessage("moduleGrowl", msg);
                    return;
                }
            }

            if (moddule.getOsType() != null && moddule.getOsType() == AamConstants.OS_TYPE.WINDOWS && !AamConstants.CODETAPTRUNG_TYPE.equals(moddule.getModuleTypeCode())) {
                String statusCmd = moddule.getViewStatus();
                String statusKey = moddule.getStatusSuccessKey();
                if (statusCmd == null || statusCmd.trim().isEmpty() || statusCmd.trim().toUpperCase().equals(Constant.NA_VALUE) || StringUtils.isEmpty(statusKey) || Constant.NA_VALUE.equals(statusKey)) {
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            moddule.getModuleName() + ": " + MessageUtil.getResourceBundleMessage("command.status.app.not.exist"),
                            "");
                    FacesContext.getCurrentInstance().addMessage("moduleGrowl", msg);
                    return;
                }
            }
        }

        int counter = 0;
        for (Module moddule : selectedBeforeModdules) {
//            if (this.impactModules.containsKey(moddule.getModuleId())) {
            if (this.normalImpactModules.containsKey(moddule.getModuleId())) {
                continue; // da add vao bang
            }

            counter++;

            moddule.setActionType(actionType);
            moddule.setKbGroup(kbGroup);
            moddule.setTestbedMode(AamConstants.TESTBED_MODE.NORMAL);
//            this.dataTable.add(moddule);
//            this.impactModules.put(moddule.getModuleId(), moddule);
//            this.impactModules.put(moddule.getModuleId(), moddule);
            this.normalImpactModules.put(moddule.getModuleId(), moddule);
            this.dataTableFilters.add(moddule);
//            this.hmModuleId.add(moddule.getModuleId());
        }

        loadTestbedModule();

        // if (!selectedBeforeModdules.isEmpty()) {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                counter + " " + MessageUtil.getResourceBundleMessage("module.inserted.to.list.impact"), "");
        FacesContext.getCurrentInstance().addMessage("moduleGrowl", msg);
        // }

        selectedBeforeModdules = new ArrayList<>();
    }

    private void loadTestbedModule() {
        List<Integer> groupModuleIds = new ArrayList<>();

        Map<Integer, Integer> actionTypes = new HashMap<>();
        Map<Integer, Integer> kbGroups = new HashMap<>();

//        for (Module module : impactModules.values()) {
        for (Module module : normalImpactModules.values()) {
            if (module.getGroupModuleId() != null && !groupModuleIds.contains(module.getGroupModuleId()) && AamConstants.MODULE_FUNCTION_TYPE.NORMAL.equals(module.getFunctionCode())) {
                groupModuleIds.add(module.getGroupModuleId());
                actionTypes.put(module.getGroupModuleId(), actionType);
                kbGroups.put(module.getGroupModuleId(), kbGroup);
            }
        }
        try {
            testbedImpactModules.clear();
            testbedDataTableFilters.clear();

            if (!groupModuleIds.isEmpty()) {
                List<Long> testbedModuleIds = iimService.findOfflineModuleIds(newObj.getCatCountryBO().getCountryCode(), groupModuleIds, AamConstants.MODULE_FUNCTION_TYPE.TESTBED);

                if (testbedModuleIds != null && !testbedModuleIds.isEmpty()) {
                    testbedModuleIds.removeAll(normalImpactModules.keySet());
//            testbedImpactModules = new HashMap<>();
//            testbedDataTableFilters = new ArrayList<>();

                    if (!testbedModuleIds.isEmpty()) {
                        testbedDataTableFilters = iimService.findModulesByIds(newObj.getCatCountryBO().getCountryCode(), testbedModuleIds);
                        for (Module module : testbedDataTableFilters) {
                            module.setActionType(actionTypes.get(module.getGroupModuleId()));
                            module.setKbGroup(kbGroups.get(module.getGroupModuleId()));
                            module.setTestbedMode(AamConstants.TESTBED_MODE.TESTBED);

                            testbedImpactModules.put(module.getModuleId(), module);
                        }
                    }
                }
            }
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }

        Set<Long> allImpactMdIds = new HashSet<>(normalImpactModules.keySet());
        allImpactMdIds.addAll(testbedImpactModules.keySet());
        lazyAppModel = new LazyModule(iimService, new HashMap<>(), allImpactMdIds, unitId, newObj.getCatCountryBO().getCountryCode());
    }

    public void prepareEdit(Action obj) {
        clear();
/*        try {
            services = iimService.findService(obj.getImpactProcess().getNationCode(), obj.getImpactProcess().getUnitId());
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }*/
        //20200320_Quytv7_fig loi toggle_start
        List<Integer> listHiddenDefault = Arrays.asList(2, 4, 7);
        columnVisible = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            if (listHiddenDefault.contains(i)) {
                columnVisible.add(false);
            } else {
                columnVisible.add(true);
            }
        }
        //20200320_Quytv7_fig loi toggle_end
        //20200203_Quytv7_chia tai chay tien trinh start
        isEdit = true;
        selectedObj = obj;
        if (selectedObj != null && selectedObj.getImpactProcess() != null && selectedObj.getImpactProcess().getNationCode() != null) {
            try {
                selectedObj.setCatCountryBO(new CatCountryServiceImpl().findById(selectedObj.getImpactProcess().getNationCode()));
                if (selectedObj.getCatCountryBO() == null) {
                    selectedObj.setCatCountryBO(new CatCountryServiceImpl().findById("VNM"));
                }
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        } else {
            try {
                selectedObj.setCatCountryBO(new CatCountryServiceImpl().findById("VNM"));
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
        if (selectedObj.getCatCountryBO() != null) {
            Map<String, Object> prFilters = new HashMap<>();
            prFilters.put("status", "1");
            prFilters.put("nationCode", selectedObj.getCatCountryBO().getCountryCode());
            try {
                List<ImpactProcess> processes = impactProcessService.findList(prFilters, new HashMap<>());
                if (processes != null && !processes.isEmpty()) {
                    impactProcesses = new ArrayList<>();
                    for (ImpactProcess process : processes) {
                        impactProcesses.add(new SelectItem(process, process.getName()));
                    }
                }
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
        //20200203_Quytv7_chia tai chay tien trinh end


        try {
            databases = iimService.findDatabases(obj.getCatCountryBO().getCountryCode(), obj.getCatCountryBO().getUnitId());
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }

        /*
         * Wizard wizard = (Wizard)
         * FacesContext.getCurrentInstance().getViewRoot().findComponent("mop").
         * findComponent("lst").findComponent("editwizard");
         * wizard.setStep("common");
         */


//        dataTable.clear();
        dataTableFilters.clear();
        impactModules.clear();
        normalImpactModules.clear();
        testbedImpactModules.clear();
        testbedDataTableFilters.clear();
//        hmModuleId.clear();
        newObj = new Action();
//		newObj.setActionType(Constant.ACTION_TYPE_CR_NORMAL);
        BeanUtilsBean.getInstance().getConvertUtils().register(false, true, 0);
        try {
            BeanUtils.copyProperties(newObj, obj);            // newObj.setPassword("");
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error(e.getMessage(), e);
        }

        if (!username.equals(newObj.getCreatedBy()) || !editable(newObj.getCrState() == null ? null : newObj.getCrState().intValue()) || newObj.getRunStatus() != null) {
            viewOnly = true;
        } else {
            viewOnly = false;
        }

        Map<String, Object> filters = new HashMap<>();
        filters.put("actionId", obj.getId() + "");

        try {
            List<ActionModule> actionModules = actionModuleService.findList(filters, new HashMap<>());
            List<Long> moduleIds = new ArrayList<>();
            Map<Long, Integer> actionTypes = new HashMap<>();
            Map<Long, Integer> kbGroups = new HashMap<>();
            Map<Long, Integer> testbedModes = new HashMap<>();
            /*20180723_hoangnd_chon user tac dong_start*/
            mapUsernames = new HashMap<>();
            /*20180723_hoangnd_chon user tac dong_end*/

            if (actionModules != null && !actionModules.isEmpty()) {
                for (ActionModule actionModule : actionModules) {
                    actionTypes.put(actionModule.getModuleId(), actionModule.getActionType());
                    kbGroups.put(actionModule.getModuleId(), actionModule.getKbGroup());
                    moduleIds.add(actionModule.getModuleId());
                    testbedModes.put(actionModule.getModuleId(), actionModule.getTestbedMode());
                    /*20180723_hoangnd_chon user tac dong_start*/
                    mapUsernames.put(actionModule.getModuleId(), actionModule.getInstalledUser());
                    /*20180723_hoangnd_chon user tac dong_end*/
                }

                List<Module> modules = iimService.findModulesByIds(newObj.getCatCountryBO().getCountryCode(), moduleIds);
                // Load list for tree node by module id
                targets = modules;

                /*20180727_hoangnd_fix bug chon user tac dong_start*/
                modules = reloadUsernames(modules);
                /*20180727_hoangnd_fix bug chon user tac dong_start*/

                for (Module module : modules) {
                    module.setActionType(actionTypes.get(module.getModuleId()));
                    module.setKbGroup(kbGroups.get(module.getModuleId()));
                    if (module.getKbGroup() == null) {
                        module.setKbGroup(1);
                    }

                    if (testbedModes.get(module.getModuleId()) != null && testbedModes.get(module.getModuleId()) == AamConstants.TESTBED_MODE.TESTBED) {
                        module.setTestbedMode(AamConstants.TESTBED_MODE.TESTBED);
                        testbedDataTableFilters.add(module);
                        testbedImpactModules.put(module.getModuleId(), module);
                    } else {
                        module.setTestbedMode(AamConstants.TESTBED_MODE.NORMAL);
                        dataTableFilters.add(module);
                        normalImpactModules.put(module.getModuleId(), module);
                    }
//                    impactModules.put(module.getModuleId(), module);
//                    hmModuleId.add(module.getModuleId());
                }
//                targets = new ArrayList<>();
//                targets.addAll(modules);
//                dualListModel.setSource(modules);
//                root = initCheckboxServices(modules, false);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        actionDetailAppController.setAction(newObj);
        actionDetailAppController.viewSelectItems(this, false, true);
        actionDetailDatabaseController.setAction(newObj);
        actionDetailDatabaseController.viewSelectItems(this);
        testCaseController.setAction(newObj);
        testCaseController.viewSelectItems();
        actionDetailDatabaseController.loadOldDb(this);
        actionCustomGroupController.setActionController(this);
        actionCustomGroupController.viewSelectItems();

        if (newObj.getKbType() != null && newObj.getKbType() >= 2L) {

            if (newObj.getActionRbSd() != null) {
                if (newObj.getActionRbSd().equals(1l)) {
                    selectedAction = 1;// Check reboot
                } else {
                    selectedAction = 2;// Check shutdown
                }
            }
            if (newObj.getIgnoreStopApp() != null) {
                if (newObj.getIgnoreStopApp().equals(1l)) {
                    isCheckUcServer = true;// selected
                } else {
                    isCheckUcServer = false;// unseleced
                }
            }
            List<Service> serviceList = new ArrayList<>();
            List<Database> databaseList = new ArrayList<>();
            List<String> ipServers = new ArrayList<>();
//        List<Module> moduleList = new ArrayList<>();

            List<Long> selectedServiceIds = new ArrayList<>();
            for (ActionItService actionItService : newObj.getActionItServices()) {
                selectedServiceIds.add(actionItService.getServiceId());
            }
            List<Long> selectedDbIds = new ArrayList<>();
            for (ActionDatabase actionDatabase : newObj.getActionDatabases()) {
                selectedDbIds.add(actionDatabase.getDbId());
            }
            List<Long> selectedModuleIds = new ArrayList<>();
            for (ActionModuleUctt actionModuleUctt : newObj.getActionModuleUctts()) {
                selectedModuleIds.add(actionModuleUctt.getModuleId());
            }
            for (ActionServer actionServer : newObj.getActionServers()) {
                ipServers.add(actionServer.getIpServer());
            }


//            for (Service service : services) {
//                if (selectedServiceIds.contains(service.getServiceId())) {
//                    serviceList.add(service);
//                }
//            }

            for (Database database : databases) {
                if (selectedDbIds.contains(database.getDbId())) {
                    databaseList.add(database);
                }
            }

//            selectedServices = serviceList.toArray(new Service[serviceList.size()]);
            selectedDatabases = databaseList.toArray(new Database[databaseList.size()]);

            lstIpServer = Joiner.on(", ").join(ipServers);

        /*try {
            targets = iimService.findModulesByIds(selectedModuleIds);
            dualListModel.setTarget(targets);
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }*/

            sources = new ArrayList<>();
            try {
                Long kbType = AamConstants.KB_TYPE.BD_SERVER;
                if (newObj.getKbType() != null) {
                    kbType = newObj.getKbType();
                }
                if (targets == null || targets.isEmpty()) {
                    targets = iimService.findModules(newObj.getCatCountryBO().getCountryCode(), selectedServiceIds, selectedDbIds, ipServers, kbType);
                }
//            targets = iimService.findModulesByIds(Arrays.asList(17017L, 17019L, 17021L));
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }

            List<Module> excludes = new ArrayList<>();
            for (Module target : targets) {
                if (!selectedModuleIds.contains(target.getModuleId())) {
                    excludes.add(target);
                }
            }
            // anhnt2: Add list exclusion
            loadTarget();
            exclusionModules = new HashMap<>();
            if (dualListModel != null && dualListModel.getSource() != null && dualListModel.getSource().size() > 0) {
                for (Module moduleExculdes : dualListModel.getSource()) {
                    exclusionModules.put(moduleExculdes.getModuleId(), moduleExculdes);
                }
            }

            targets.removeAll(excludes);
            sources.addAll(excludes);

            if (dualListModel != null) {
                dualListModel.setSource(sources);
                dualListModel.setTarget(targets);
            }
            newObj.setKbGroup(1L);

            /*20180727_hoangnd_fix bug chon user tac dong_start*/
            targets = reloadUsernames(targets);
            testbedDataTableFilters = reloadUsernames(testbedDataTableFilters);
            dataTableFilters = reloadUsernames(dataTableFilters);
            /*20180727_hoangnd_fix bug chon user tac dong_start*/
        }
        TabView tabView = (TabView) FacesContext.getCurrentInstance().getViewRoot().findComponent("mop").findComponent("lst");
        Tab tab = (Tab) tabView.findComponent("edittab");
        // tab.setDisabled(true);
        // tab.setDisabled(true);
        tab.setRendered(true);
//		System.out.println(tabView.getActiveIndex());
        tabView.setActiveIndex(1);

        // Don't load check status service by target module, load with normalImpactModules (use in method loadChild)
        isFirstHandleChange = false;
        handleChange = false;
        root = initCheckboxServices(dataTableFilters, false);
//        buildImpactModules(false);

        //HungVC 29/09/2020
        serviceNames = new ArrayList<>();
        try{
            List<Service> lstServices = new IimClientServiceImpl().findServices(obj.getCatCountryBO().getCountryCode(), obj.getCatCountryBO().getUnitId());
            for (Service item: lstServices) {
                SelectItem selectItem = new SelectItem();
                selectItem.setLabel(item.getServiceName());
                selectItem.setValue(item.getServiceName());
                serviceNames.add(selectItem);
            }
        }catch (Exception ex){
            logger.info(ex.getMessage());
        }
        serviceNames.add(0, new SelectItem(null, MessageUtil.getResourceBundleMessage("common.choose")));
    }

    /*20180727_hoangnd_fix bug chon user tac dong_start*/
    public List<Module> reloadUsernames(List<Module> listModule) {
        for (Module module : listModule) {
            if (mapUsernames.get(module.getModuleId()) != null)
                module.setUsername(mapUsernames.get(module.getModuleId()));
        }
        return listModule;
    }
    /*20180727_hoangnd_fix bug chon user tac dong_end*/

    public void clear() {
        if (FacesContext.getCurrentInstance().getViewRoot().findComponent("mop") != null) {
            Wizard wizard = (Wizard) FacesContext.getCurrentInstance().getViewRoot().findComponent("mop").findComponent("lst").findComponent("editwizard");
            wizard.setStep("common");
            counter = 0;

            createNew = false;
            isEdit = false;
            newObj = new Action();
            if (isUctt) {
                newObj.setActionType(Constant.ACTION_TYPE_KB_UCTT);
                //20180717_tudn_start khong cho hien chuc vu khi vua load vao
//                newObj.setLabelSign1(MessageUtil.getResourceBundleMessage("leader.department"));
//                newObj.setLabelSign2(MessageUtil.getResourceBundleMessage("vice.director"));
//                newObj.setLabelSign3(MessageUtil.getResourceBundleMessage("deputy.general.manager"));
                //20180717_tudn_start khong cho hien chuc vu
                newObj.setBeginTime(new Date());
                newObj.setEndTime(new Date());
            } else {
                newObj.setActionType(Constant.ACTION_TYPE_CR_NORMAL);
            }

            newObj.setCrNumber(Constant.CR_DEFAULT);
//        dataTable.clear();
            dataTableFilters.clear();
//        hmModuleId.clear();
            impactModules.clear();
            normalImpactModules.clear();
            // anhnt2 - 20180629
            normalImpactModules.clear();
            exclusionModules.clear();
            keyActionSearch = "";
            dualListModel = new DualListModel<>();
            DataTable dataTableServices = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("mop").findComponent("lst").findComponent("dataTableServices");
            if (dataTableServices != null && !dataTableServices.getFilters().isEmpty()) {
                RequestContext.getCurrentInstance().execute("PF('dataTableServices').clearFilters()");
            }
            DataTable dataTableServicesExclusion = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("mop").findComponent("lst").findComponent("dataTableServicesExclusion");
            if (dataTableServicesExclusion != null && !dataTableServicesExclusion.getFilters().isEmpty()) {
                RequestContext.getCurrentInstance().execute("PF('dataTableServicesExclusion').clearFilters()");
            }

            String folder = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            // String uploadFolder = getBaseTomcatFolder(sourceFolder +
            // File.separator + folder);
            // String uploadFolder = UploadFileUtils.getBaseTomcatFolder(parentPath
            // + File.separator + folder);
            newObj.setSourceDir(folder);

            actionDetailAppController.init();
            actionDetailDatabaseController.init();
            testCaseController.init();
            actionCustomGroupController.init();

            actionDetailAppController.setAction(newObj);
            actionDetailAppController.viewSelectItems(this, true, false);

            /*
             * List<ServiceDb> serviceDbs = new ArrayList<>(); for (TreeNode
             * treeNode : cklDbListSelectedNodes) { if
             * (((TreeObject)treeNode.getData()).getObj() instanceof RstKpi) {
             * RstViewDb rstViewDb = (RstViewDb)
             * ((TreeObject)treeNode.getParent().getData()).getObj(); try {
             * serviceDbs.add(serviceDbService.findById(rstViewDb.getAppGroupDbId())
             * ); } catch (AppException e) { logger.error(e.getMessage(), e); } } }
             */
            actionDetailDatabaseController.setAction(newObj);
            // actionDetailDatabaseController.setListDetailsDb(serviceDbs);
            actionDetailDatabaseController.viewSelectItems(this);

            testCaseController.setAction(newObj);
            testCaseController.viewSelectItems();

            cklListSelectedNodes = new TreeNode[0];
            cklDbListSelectedNodes = new TreeNode[0];

            // this.selectApp.clear();
            this.selectAppKpiMap.clear();
            this.fitterAppName = null;
            this.fitterKpiIds = null;

            kbGroup = 1;

            codeChanges = HashMultimap.create();

            lstIpServer = null;
            selectedDatabases = null;
            targets = null;
            /*20180727_hoangnd_fix bug chon user tac dong_start*/
            mapUsernames = new HashMap<>();
            /*20180727_hoangnd_fix bug chon user tac dong_end*/
        }
    }

    public void preAdd(Long kbGroup) {
        //20200320_Quytv7_fig loi toggle_start
        List<Integer> listHiddenDefault = Arrays.asList(2, 4, 7);
        columnVisible = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            if (listHiddenDefault.contains(i)) {
                columnVisible.add(false);
            } else {
                columnVisible.add(true);
            }
        }
        //20200320_Quytv7_fig loi toggle_end
        if (kbGroup.equals(2L)) {
            if (StringUtils.isEmpty(ibmUsername) || StringUtils.isEmpty(ibmPassword)) {
                RequestContext reqCtx = RequestContext.getCurrentInstance();
                reqCtx.execute("PF('authIbm').show()");
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("devops.ibm.auth.null"), "");
                FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                return;
            } else {
                try {
                    TeamPlatform.startup();
                    IProgressMonitor monitor = new NullProgressMonitor();
                    try {
                        login(monitor, ibmUsername, ibmPassword);
                    } catch (TeamRepositoryException e) {
                        logger.error(e.getMessage() + "\t" + ibmUsername);
                        logger.debug(e.getMessage(), e);
                        RequestContext reqCtx = RequestContext.getCurrentInstance();
                        reqCtx.execute("PF('authIbm').show()");
                        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("devops.ibm.auth.invalid"), "");
                        FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                        return;
                    }
                } finally {
                    TeamPlatform.shutdown();
                }
            }
        }
        viewOnly = false;
        clear();
        newObj.setKbGroup(kbGroup);
        createNew = true;
        if (kbGroup.equals(1L)) {
            newObj.setMaxConcurrent(3);
        }
        if (newObj.getKbType() == null && kbGroup.equals(1L)) {
            selectedAction = 1;
            newObj.setKbType(AamConstants.KB_TYPE.BD_SERVER);
        }
        //20180627_tudn_start them clone UCTT
        String viewId = FacesContext.getCurrentInstance().getViewRoot().getViewId();
        isUctt = ("/faces/action/config/uctt.xhtml".equals(viewId)) ? Boolean.TRUE : Boolean.FALSE;
        //20180627_tudn_end them clone UCTT
        TabView tabView = (TabView) FacesContext.getCurrentInstance().getViewRoot().findComponent("mop").findComponent("lst");
        Tab tab = (Tab) tabView.findComponent("edittab");
        // tab.setDisabled(true);
        tab.setRendered(true);
        tabView.setActiveIndex(1);

        // ThanhTD - process mop type - start
        newObj.setMopType(mopType);
        // Unikom - process mop type - end
    }

    public void exportDT(Action obj) throws FileNotFoundException {
        /*
         * String template = getUploadFolder() + File.separator +
         * "file-template" + File.separator + "TEMPLATE_DT_NEW.docx"; String
         * templateRollBack = getUploadFolder() + File.separator +
         * "file-template" + File.separator + "TEMPLATE_DT_ROLLBACK.docx";
         * String source = getBaseTomcatFolder(DTFolder) + File.separator + new
         * SimpleDateFormat("ddMMyyyyHHmmss").format(new Date());
         *
         * File file1 = new File(source); if (!file1.exists()) file1.mkdir();
         * String zipFile = new DocxUtil().genericDT(obj, template,
         * templateRollBack, source, null);
         */

        String source = UploadFileUtils.getMopFolder(obj);

        String zipFile = DocxUtil.export(obj, null) + ".zip";

        String zipFolder = new File(source).getParent();

        FileOutputStream fos = null;

        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(zipFolder + File.separator + zipFile);
            zos = new ZipOutputStream(fos);
//            byte[] buffer = new byte[1024];
            File fileFolder = new File(source);
            if (fileFolder.exists()) {
                File[] listFile = fileFolder.listFiles();
                for (File file : listFile) {


                    ZipEntry ze = new ZipEntry(file.getName());
                    zos.putNextEntry(ze);

//                    FileInputStream in = new FileInputStream(source + File.separator + file.getName());
//
//                    int len;
//                    while ((len = in.read(buffer)) > 0) {
//                        zos.write(buffer, 0, len);
//                    }
//
//                    in.close();
                    ZipUtils.zipInputStream(source + File.separator + file.getName(), zos);
                }
            }

//            zos.closeEntry();
            zos.close();

            FacesContext facesContext = FacesContext.getCurrentInstance();
            writeExcelToResponse(((HttpServletResponse) facesContext.getExternalContext().getResponse()), zipFile,
                    zipFolder + File.separator + zipFile);
            facesContext.responseComplete();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (zos != null)
                try {
                    zos.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            if (fos != null)
                try {
                    fos.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }

            File file = new File(zipFolder + File.separator + zipFile);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    public void downloadCode(Action obj) throws FileNotFoundException {
        String source = UploadFileUtils.getMopFolder(obj);

        String zipFile = obj.getTdCode() + ".zip";

        String zipFolder = new File(source).getParent();

        FileOutputStream fos = new FileOutputStream(zipFolder + File.separator + zipFile);
        ZipOutputStream zos = new ZipOutputStream(fos);
        byte[] buffer = new byte[1024];

        try {
            String codeFolder = UploadFileUtils.getSourceCodeFolder(obj);
            List<ActionDetailApp> lstUpcode = actionDetailAppController.getActionDetailAppService().findListDetailApp(obj.getId(), Constant.STEP_UPCODE, null, true);
            for (ActionDetailApp detailApp : lstUpcode) {
                File file = new File(codeFolder + File.separator + detailApp.getUploadFilePath());

                ZipEntry ze = new ZipEntry(obj.getCreatedBy() + "_" + obj.getTdCode() + "/source_code/" + file.getName());

                FileInputStream in = null;
                try {
                    zos.putNextEntry(ze);

                    in = new FileInputStream(file);

                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                } catch (ZipException e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    if (in != null)
                        in.close();
                }


            }
            codeFolder = UploadFileUtils.getDatabaseFolder(obj);
            List<ActionDetailDatabase> databases = actionDetailDatabaseController.getActionDetailDatabaseService().findListDetailDb(obj.getId(), null, true, true);
            for (ActionDetailDatabase database : databases) {
                if (StringUtils.isNotEmpty(database.getScriptBackup())) {
                    File file = new File(codeFolder + File.separator + database.getScriptBackup());


                    ZipEntry ze = new ZipEntry(obj.getCreatedBy() + "_" + obj.getTdCode() + "/script_db/" + file.getName());

                    FileInputStream in = null;
                    try {
                        zos.putNextEntry(ze);

                        in = new FileInputStream(file);

                        int len;
                        while ((len = in.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }
                    } catch (ZipException e) {
                        logger.error(e.getMessage(), e);
                    } finally {
                        if (in != null)
                            in.close();
                    }
                }

                if (StringUtils.isNotEmpty(database.getScriptExecute())) {
                    File file = new File(codeFolder + File.separator + database.getScriptExecute());


                    ZipEntry ze = new ZipEntry(obj.getCreatedBy() + "_" + obj.getTdCode() + "/script_db/" + file.getName());
                    FileInputStream in = null;
                    try {
                        zos.putNextEntry(ze);

                        in = new FileInputStream(file);

                        int len;
                        while ((len = in.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }
                    } catch (ZipException e) {
                        logger.error(e.getMessage(), e);
                    } finally {
                        if (in != null)
                            in.close();
                    }
                }

                if (StringUtils.isNotEmpty(database.getRollbackFile())) {
                    File file = new File(codeFolder + File.separator + database.getRollbackFile());


                    ZipEntry ze = new ZipEntry(obj.getCreatedBy() + "_" + obj.getTdCode() + "/script_db/" + file.getName());
                    FileInputStream in = null;
                    try {
                        zos.putNextEntry(ze);

                        in = new FileInputStream(file);

                        int len;
                        while ((len = in.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }
                    } catch (ZipException e) {
                        logger.error(e.getMessage(), e);
                    } finally {
                        if (in != null)
                            in.close();
                    }
                }
            }

            codeFolder = UploadFileUtils.getTestcaseFolder(obj);
            Map<String, Object> filters = new HashMap<>();

            filters.put("actionId", obj.getId() + "");

            try {
                List<TestCase> testCases = testCaseService.findList(filters, new HashMap<String, String>());
                for (TestCase testCase : testCases) {
                    File file = new File(codeFolder + File.separator + testCase.getFileName());


                    ZipEntry ze = new ZipEntry(obj.getCreatedBy() + "_" + obj.getTdCode() + "/test_case/" + file.getName());
                    FileInputStream in = null;
                    try {
                        zos.putNextEntry(ze);

                        in = new FileInputStream(file);

                        int len;
                        while ((len = in.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }
                    } catch (ZipException e) {
                        logger.error(e.getMessage(), e);
                    } finally {
                        if (in != null)
                            in.close();
                    }
                }
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }


            zos.closeEntry();
            zos.close();

            FacesContext facesContext = FacesContext.getCurrentInstance();
            writeExcelToResponse(((HttpServletResponse) facesContext.getExternalContext().getResponse()), zipFile,
                    zipFolder + File.separator + zipFile);
            facesContext.responseComplete();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (zos != null)
                try {
                    zos.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            if (fos != null)
                try {
                    fos.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }

            File file = new File(zipFolder + File.separator + zipFile);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    public boolean editable(Integer crState) {
//		return true;
        if (crState == null)
            return true;
        return Arrays.asList(0, 1, 2, 3, 8).contains(crState);
    }

    /**
     * Load data excludes.
     */
    private List<Module> loadExcludes() {
        List<Module> excludes = new ArrayList<>();
        // •Có module_type thuộc bảng cấu hình EXCLUDE_MODULE_MAINTAIN where status = 0
        try {
            List<String> moduleTypes = actionService.findModuleTypeNotDb();
            if (moduleTypes != null && !moduleTypes.isEmpty()) {
                for (Module target : targets) {
                    if (moduleTypes.contains(target.getModuleTypeCode())) {
                        excludes.add(target);
                    }
                }
            }
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }
        for (Module target : targets) {
            // Module status disable IIM
            if (target.getModuleStatus() == null || !target.getModuleStatus().equals(1)) {
                excludes.add(target);
            } else if (newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_SW_DB)) {
                if (!moduleDbDrMap.keySet().contains(target.getModuleId())) {
                    excludes.add(target);
                }
            } else if (Constant.CODETAPTRUNG_TYPE.equals(target.getModuleTypeCode())) { // •Type = Code tập trung
                excludes.add(target);
            }

            // Comment reson: Chú ý: Riêng trường hợp module đang active mà có lệnh start/stop = N/A thì không cho vào module loại trừ nữa.  Vẫn đẩy vào danh sách Module tác động
//            else {
//                if (StringUtils.isEmpty(target.getStartService()) || StringUtils.isEmpty(target.getStopService()) ||
//                        AamConstants.NA_VALUE.equals(target.getStartService()) || AamConstants.NA_VALUE.equals(target.getStopService())) {
//                    excludes.add(target);
//                }
//            }
        }
        return excludes;
    }

    /**
     * Load list module by ip and dbId.
     */
    private List<Module> loadFindModule() {
        List<Module> lstModule = new ArrayList<>();

        List<Long> databaseIds = new ArrayList<>();
        List<String> ipServers = new ArrayList<>();

        if (selectedDatabases != null
                && (newObj.getKbType().equals(AamConstants.KB_TYPE.UC_SERVER)
                || newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_START)
                || newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_STOP)
                || newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_RESTART_STOP_START)
                || newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_RESTART))) {
            for (Database selectedDatabase : selectedDatabases) {
                databaseIds.add(selectedDatabase.getDbId());
            }
        }

        if (StringUtils.isNotEmpty(lstIpServer))
            ipServers = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(lstIpServer);
        sources = new ArrayList<>();
        try {
            lstModule = iimService.findModules(newObj.getCatCountryBO().getCountryCode(), new ArrayList<>(), databaseIds, ipServers, newObj.getKbType());
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }
        return lstModule;
    }

    /**
     * Load data for target.
     */
    private void loadTarget() {
        // When foreach then only loadTarget() 1 time
        if (isLoadTarget) {
            targets = loadFindModule();
            List<Module> excludes = loadExcludes();
            targets.removeAll(excludes);
            sources.addAll(excludes);
            dualListModel.setSource(sources);
            dualListModel.setTarget(targets);
        }
    }

    public void handleChange() {
        handleChangeToTrue();
        if (StringUtils.isNotEmpty(keyActionSearch)) {
            // Load for input tree search
            searchActionNode();
        } else {
            // Load check status service by target module, don't load by normalImpactModules (use in method loadChild)
            isFirstHandleChange = true;
            load2TableModule();
        }
    }

    private void load2TableModule() {
        normalImpactModules = new HashMap<>();
        List<Long> serviceIds = new ArrayList<>();
        List<Long> databaseIds = new ArrayList<>();
        List<String> ipServers = new ArrayList<>();

//        if (selectedServices != null)
//            for (Service selectedService : selectedServices) {
//                serviceIds.add(selectedService.getServiceId());
//            }

        if (selectedDatabases != null
                && (newObj.getKbType().equals(AamConstants.KB_TYPE.UC_SERVER)
                || newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_STOP)
                || newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_START)
                || newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_RESTART_STOP_START)
                || newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_RESTART))) {
            for (Database selectedDatabase : selectedDatabases) {
                databaseIds.add(selectedDatabase.getDbId());
            }
        } else {
            selectedDatabases = null;
        }

        if (StringUtils.isNotEmpty(lstIpServer))
            ipServers = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(lstIpServer);
        sources = new ArrayList<>();
        try {
            targets = iimService.findModules(newObj.getCatCountryBO().getCountryCode(), serviceIds, databaseIds, ipServers, newObj.getKbType());
//            targets = iimService.findModulesByIds(Arrays.asList(17017L, 17019L, 17021L));
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }

        moduleDbDrMap = new HashMap<>();
        if (newObj.getKbType() != null && newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_SW_DB)) {
            List<Long> moduleIds = new ArrayList<>();
            for (Module target : targets) {
                moduleIds.add(target.getModuleId());
            }
            try {
                ModuleDbDr moduleDbDrTmp = new ModuleDbDr();
                moduleDbDrTmp.setModuleId(737516L);
                moduleDbDrTmp.setDbId(443L);
                moduleDbDrTmp.setFilePath("/u01/test");
                moduleDbDrTmp.setFileName("hibernate.cfg");

                moduleDbDrTmp.setDrFilePath("/u01/test");
                moduleDbDrTmp.setDrFileName("hibernate.cfg_bk");

//                List<ModuleDbDr> moduleDbDrs = iimService.findModuleDbDr(moduleIds, databaseIds);
                List<ModuleDbDr> moduleDbDrs = Arrays.asList(moduleDbDrTmp);
                for (ModuleDbDr moduleDbDr : moduleDbDrs) {
                    moduleDbDrMap.put(moduleDbDr.getModuleId(), moduleDbDr);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        List<Module> excludes = loadExcludes();
        // anhnt2: Add list exclusion
        exclusionModules = new HashMap<>();
        if (excludes != null && excludes.size() > 0) {
            for (Module moduleExculdes : excludes) {
                exclusionModules.put(moduleExculdes.getModuleId(), moduleExculdes);
            }
        }

        targets.removeAll(excludes);
        sources.addAll(excludes);

        dualListModel.setSource(sources);
        dualListModel.setTarget(targets);

        checkModuleDb();

        // For update data table
//        buildImpactModules();
        // For update data tree node (Service)
        if (databaseIds.size() > 0 || ipServers.size() > 0) {
            // Only load list service by ip server/databaseId
            List<Module> mergeTargetSource = new ArrayList<>();
            // List service with check on checkbox
            if (!dualListModel.getTarget().isEmpty())
                mergeTargetSource.addAll(dualListModel.getTarget());
            // List service with not check on checkbox
            if (!dualListModel.getSource().isEmpty())
                mergeTargetSource.addAll(dualListModel.getSource());
            root = initCheckboxServices(mergeTargetSource, true);
            // For update data table
            buildImpactModules(true);
        }
        // If databaseId and ip server don't input then load all list service
        if (databaseIds.size() == 0 && ipServers.size() == 0) {
            root = initCheckboxServices(new ArrayList<>(), false);
            // For update data table
            buildImpactModules(true);
        }
    }

    private boolean checkModuleDb() {
        if (newObj.getKbType().equals(AamConstants.KB_TYPE.BD_SERVER) || newObj.getKbType().equals(AamConstants.KB_TYPE.UC_SERVER)) {
            try {
                List<String> moduleTypes = actionService.findModuleTypeNotDb();

                if (moduleTypes != null && !moduleTypes.isEmpty()) {
                    // End process for check only BD server
                    for (Module module : dualListModel.getTarget()) {
                        if (moduleTypes.contains(module.getModuleTypeCode())) {
                            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(MessageUtil.getResourceBundleMessage("mop.common.uctt.not_support_md"), module.getModuleName(), module.getModuleTypeCode()), "");
                            if (newObj.getKbType().equals(AamConstants.KB_TYPE.UC_SERVER)) {
                                msgExcludeUCTT = msg.getSummary();
                                return false;
                            }
                            FacesContext.getCurrentInstance().addMessage("ucttGrowl", msg);
                            return false;
                        }
                    }
                    for (Module module : dualListModel.getSource()) {
                        if (moduleTypes.contains(module.getModuleTypeCode())) {
                            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(MessageUtil.getResourceBundleMessage("mop.common.uctt.not_support_md"), module.getModuleName(), module.getModuleTypeCode()), "");
                            if (newObj.getKbType().equals(AamConstants.KB_TYPE.UC_SERVER)) {
                                msgExcludeUCTT = msg.getSummary();
                                return false;
                            }
                            FacesContext.getCurrentInstance().addMessage("ucttGrowl", msg);
                            return false;
                        }
                    }

                    // Start process for check when target, source null
                    List<String> ipServers = new ArrayList<>();
                    if (StringUtils.isNotEmpty(lstIpServer))
                        ipServers = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(lstIpServer);
                    List<Module> lstModule = iimService.findModules(newObj.getCatCountryBO().getCountryCode(), new ArrayList<>(), new ArrayList<>(), ipServers, newObj.getKbType());
                    for (Module module : lstModule) {
                        if (moduleTypes.contains(module.getModuleTypeCode())) {
                            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(MessageUtil.getResourceBundleMessage("mop.common.uctt.not_support_md"), module.getModuleName(), module.getModuleTypeCode()), "");
                            if (newObj.getKbType().equals(AamConstants.KB_TYPE.UC_SERVER)) {
                                msgExcludeUCTT = msg.getSummary();
                                return false;
                            }
                            FacesContext.getCurrentInstance().addMessage("ucttGrowl", msg);
                            return false;
                        }
                    }
                }
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return true;
    }

    public static void writeExcelToResponse(HttpServletResponse response, String fileName, String filePatch)
            throws IOException {
        response.setContentType("ZIP/RAR");
        response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        response.setHeader("Pragma", "public");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        FileInputStream inputStream = null;
        try {
            File file = new File(filePatch);
            if (file.exists()) {
                byte bytes[] = new byte[1024];
                inputStream = new FileInputStream(filePatch);
                int count;
                while ((count = inputStream.read(bytes)) != -1) {
                    response.getOutputStream().write(bytes, 0, count);
                }
//                inputStream.close();
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public void syncGnoc(Action action) {
        ResourceBundle bundle = ResourceBundle.getBundle("config");

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
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

    public void duplicate(Action obj, boolean includeFile) {
        clear();
        //20200320_Quytv7_fig loi toggle_start
        List<Integer> listHiddenDefault = Arrays.asList(2, 4, 7);
        columnVisible = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            if (listHiddenDefault.contains(i)) {
                columnVisible.add(false);
            } else {
                columnVisible.add(true);
            }
        }
        //20200320_Quytv7_fig loi toggle_end

        //20180627_tudn_start them clone UCTT
        String viewId = FacesContext.getCurrentInstance().getViewRoot().getViewId();
        isUctt = ("/faces/action/config/uctt.xhtml".equals(viewId)) ? Boolean.TRUE : Boolean.FALSE;
        //20180627_tudn_end them clone UCTT

        isEdit = false;
        selectedObj = obj;
//        dataTable.clear();
        dataTableFilters.clear();
//        hmModuleId.clear();
        impactModules.clear();
        normalImpactModules.clear();
        testbedDataTableFilters.clear();
        testbedImpactModules.clear();
        newObj = new Action();
//		newObj.setActionType(Constant.ACTION_TYPE_CR_NORMAL);

        BeanUtilsBean.getInstance().getConvertUtils().register(false, true, 0);
        //Quytv7_20190919 bo clone action
        try {
            BeanUtils.copyProperties(newObj, obj);
            // newObj.setPassword("");
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error(e.getMessage(), e);
        }
        //Quytv7_20190919 bo clone action

        if (newObj != null && newObj.getCatCountryBO() == null && newObj.getImpactProcess() != null && newObj.getImpactProcess().getNationCode() != null) {
            try {
                newObj.setCatCountryBO(new CatCountryServiceImpl().findById(selectedObj.getImpactProcess().getNationCode()));
                if (newObj.getCatCountryBO() == null) {
                    newObj.setCatCountryBO(new CatCountryServiceImpl().findById("VNM"));
                }
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        } else {
            try {
                newObj.setCatCountryBO(new CatCountryServiceImpl().findById("VNM"));
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }

        viewOnly = false;

        Map<String, Object> filters = new HashMap<>();
        filters.put("actionId", obj.getId() + "");

        try {
            List<ActionModule> actionModules = actionModuleService.findList(filters, new HashMap<String, String>());
            List<Long> moduleIds = new ArrayList<>();
            Map<Long, Integer> actionTypes = new HashMap<>();
            Map<Long, Integer> kbGroups = new HashMap<>();
            Map<Long, Integer> testbedModes = new HashMap<>();
            if (actionModules != null && !actionModules.isEmpty()) {
                for (ActionModule actionModule : actionModules) {
                    actionTypes.put(actionModule.getModuleId(), actionModule.getActionType());
                    kbGroups.put(actionModule.getModuleId(), actionModule.getKbGroup());
                    testbedModes.put(actionModule.getModuleId(), actionModule.getTestbedMode());
                    moduleIds.add(actionModule.getModuleId());
                }

                /*List<ParameterBO> parameterBOS = new ArrayList<>();
                parameterBOS.add(new ParameterBO(null, "serviceId", ",", null, Joiner.on(",").join(moduleIds)));

                RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_MODULE_AAM, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
                IimServices_PortType iimServices_portType = IimClientFactory.create();
                JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request);

                ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
                JsonNode node = preMapper.readTree(jsonData.getDataJson());
                String data = node.get("data").toString();

                ObjectMapper objectMapper = new ObjectMapper();
                dataTable = objectMapper.readValue(data, new TypeReference<List<Module>>() {});*/
//                dataTable = applicationDetailService.search(moduleIds);
//                dataTable = applicationDetailService.search(moduleIds);
                List<Module> modules = iimService.findModulesByIds(newObj.getCatCountryBO().getCountryCode(), moduleIds);
                for (Module module : modules) {
                    module.setActionType(actionTypes.get(module.getModuleId()));
                    module.setKbGroup(kbGroups.get(module.getModuleId()));
                    if (testbedModes.get(module.getModuleId()) == AamConstants.TESTBED_MODE.TESTBED) {
                        testbedDataTableFilters.add(module);
                        testbedImpactModules.put(module.getModuleId(), module);
                    } else {
                        dataTableFilters.add(module);
                        normalImpactModules.put(module.getModuleId(), module);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        actionDetailAppController.setAction(newObj);
        actionDetailAppController.viewSelectItems(this, true, includeFile);
        actionDetailDatabaseController.setAction(newObj);
        if (includeFile) {
//			actionDetailDatabaseController.viewSelectItems(this);
            /*20181018_hoangnd_fix bug clone ActionDetailDatabase_start*/
            actionDetailDatabaseController.cloneOldDb(this);
            /*20181018_hoangnd_fix bug clone ActionDetailDatabase_end*/
        }
        testCaseController.setAction(newObj);
        testCaseController.viewSelectItems();
//		actionDetailDatabaseController.loadOldDb(this);

        actionCustomGroupController.setActionController(this);
        actionCustomGroupController.viewSelectItems();

        newObj.setCreatedBy(username);
        newObj.setFullName(fullname);
        newObj.setStaffCode(staffCode);
        newObj.setCreatedTime(new Date());
        String folder = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        try {
            FileUtils.copyDirectory(new File(UploadFileUtils.getBaseFolder() + File.separator + newObj.getSourceDir() + File.separator + "testcase"), new File(UploadFileUtils.getBaseFolder() + File.separator + folder + File.separator + "testcase"));
        } catch (IOException e) {
            logger.error(e.getMessage());
            logger.debug(e.getMessage(), e);
        }

        if (includeFile) {
            try {
                FileUtils.copyDirectory(new File(UploadFileUtils.getBaseFolder() + File.separator + newObj.getSourceDir() + File.separator + "source_code"), new File(UploadFileUtils.getBaseFolder() + File.separator + folder + File.separator + "source_code"));
            } catch (IOException e) {
                logger.error(e.getMessage());
                logger.debug(e.getMessage(), e);
            }

            try {
                FileUtils.copyDirectory(new File(UploadFileUtils.getBaseFolder() + File.separator + newObj.getSourceDir() + File.separator + "database"), new File(UploadFileUtils.getBaseFolder() + File.separator + folder + File.separator + "database"));
            } catch (IOException e) {
                logger.error(e.getMessage());
                logger.debug(e.getMessage(), e);
            }

            try {
                FileUtils.copyDirectory(new File(UploadFileUtils.getBaseFolder() + File.separator + newObj.getSourceDir() + File.separator + "data_import"), new File(UploadFileUtils.getBaseFolder() + File.separator + folder + File.separator + "data_import"));
            } catch (IOException e) {
                logger.error(e.getMessage());
                logger.debug(e.getMessage(), e);
            }
        }

        newObj.setSourceDir(folder);
        newObj.setId(null);
//		newObj.setCrName("");
        newObj.setCrNumber(Constant.CR_DEFAULT);
        newObj.setRunStatus(null);
        newObj.setLinkCrTime(null);
        newObj.setCrState(null);
        newObj.setSignStatus(null);
        newObj.setVoTextId(null);
        newObj.setAdOrgId1(null);
        newObj.setAdOrgId2(null);
        newObj.setAdOrgId3(null);
        newObj.setAdOrgName1(null);
        newObj.setAdOrgName2(null);
        newObj.setAdOrgName3(null);
        newObj.setApproveUcttBy(null);
        newObj.setApproveRollbackBy(null);
        newObj.setExeRollback(null);
        newObj.setStartTimeRollback(null);
        newObj.setEndTimeRollback(null);
        newObj.setReasonRollback(null);
        newObj.setRunAuto(null);
        newObj.setRunningStatus(null);
        newObj.setExeImpactUctt(null);
        newObj.setReasonImpactUctt(null);
        newObj.setStartTimeImpactUctt(null);
        newObj.setEndTimeImpactUctt(null);
        newObj.setExeImpactStep(null);
        newObj.setReasonImpactStep(null);

        //ThanhTD 20200911 -Sửa chức năng clone MOP
        newObj.setMopType(0);

        if (newObj != null && newObj.getCatCountryBO() == null && newObj.getImpactProcess() != null && newObj.getImpactProcess().getNationCode() != null) {
            try {
                newObj.setCatCountryBO(new CatCountryServiceImpl().findById(newObj.getImpactProcess().getNationCode()));
                if (newObj.getCatCountryBO() == null) {
                    newObj.setCatCountryBO(new CatCountryServiceImpl().findById("VNM"));
                }
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        } else {
            try {
                newObj.setCatCountryBO(new CatCountryServiceImpl().findById("VNM"));
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
        handleChangeCountry(newObj);

        List<Checklist> selectedChecklists = null;
        try {
            selectedChecklists = checklistService.findCheckListAppByAction(obj.getId());

            this.selectAppKpiMap.clear();
            // this.selectApp.clear();

            for (Checklist objCkl : selectedChecklists) {
                if (this.selectAppKpiMap.containsKey(objCkl.getModuleId())) {
                    this.selectAppKpiMap.get(objCkl.getModuleId()).add(objCkl.getId());
                } else {
                    HashSet<Long> set = new HashSet<>();
                    set.add(objCkl.getId());
                    this.selectAppKpiMap.put(objCkl.getModuleId(), set);
                }
            }

        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }

        //Quytv7_20190606_fix loi clone cr bao duong start
        try {
            List<String> ipServers = actionService.findIpReboot(obj.getId());
            lstIpServer = Joiner.on(", ").join(ipServers);
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }
        //Quytv7_20190606_fix loi clone cr bao duong end


        TabView tabView = (TabView) FacesContext.getCurrentInstance().getViewRoot().findComponent("mop").findComponent("lst");
        Tab tab = (Tab) tabView.findComponent("edittab");
        // tab.setDisabled(true);
        tab.setRendered(true);
        tabView.setActiveIndex(1);
    }

    //20180627_tudn_start them clone UCTT
    public void cloneTemplate(Action obj, boolean includeFile) {
        isUctt = true;
        clear();

        isEdit = false;
        selectedObj = obj;
        dataTableFilters.clear();
        impactModules.clear();
        normalImpactModules.clear();
        testbedDataTableFilters.clear();
        testbedImpactModules.clear();
        newObj = new Action();
        FacesMessage msg = null;

        BeanUtilsBean.getInstance().getConvertUtils().register(false, true, 0);
        try {
            BeanUtils.copyProperties(newObj, obj);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error(e.getMessage(), e);
        }

        viewOnly = false;

        Map<String, Object> filters = new HashMap<>();
        filters.put("actionId", obj.getId() + "");

        try {
            List<ActionModule> actionModules = actionModuleService.findList(filters, new HashMap<String, String>());
            List<Long> moduleIds = new ArrayList<>();
            Map<Long, Integer> actionTypes = new HashMap<>();
            Map<Long, Integer> kbGroups = new HashMap<>();
            Map<Long, Integer> testbedModes = new HashMap<>();
            if (actionModules != null && !actionModules.isEmpty()) {
                for (ActionModule actionModule : actionModules) {
                    actionTypes.put(actionModule.getModuleId(), actionModule.getActionType());
                    kbGroups.put(actionModule.getModuleId(), actionModule.getKbGroup());
                    testbedModes.put(actionModule.getModuleId(), actionModule.getTestbedMode());
                    moduleIds.add(actionModule.getModuleId());
                }

                List<Module> modules = iimService.findModulesByIds(newObj.getCatCountryBO().getCountryCode(), moduleIds);
                for (Module module : modules) {
                    module.setActionType(actionTypes.get(module.getModuleId()));
                    module.setKbGroup(kbGroups.get(module.getModuleId()));
                    if (testbedModes.get(module.getModuleId()) == AamConstants.TESTBED_MODE.TESTBED) {
                        testbedDataTableFilters.add(module);
                        testbedImpactModules.put(module.getModuleId(), module);
                    } else {
                        dataTableFilters.add(module);
                        normalImpactModules.put(module.getModuleId(), module);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        actionDetailAppController.setAction(newObj);
        actionDetailAppController.viewSelectItems(this, true, false);
        actionDetailDatabaseController.setAction(newObj);

        testCaseController.setAction(newObj);
        testCaseController.viewSelectItems();

        actionCustomGroupController.setActionController(this);
        actionCustomGroupController.viewSelectItems();

        try {
            Multimap<Long, Long> multimap = HashMultimap.create();
            List<Map.Entry<Long, Long>> kpiDbs = new ArrayList<>();

            for (Entry<Long, HashSet<Long>> set : this.selectAppKpiMap.entrySet()) {
                for (Long kpiId : set.getValue()) {
                    multimap.put(set.getKey(), kpiId);
                }
            }

            if (cklDbListSelectedNodes != null)
                for (TreeNode cklDbListSelectedNode : cklDbListSelectedNodes) {
                    if (((TreeObject) cklDbListSelectedNode.getData()).getIsLeaf()) {
                        kpiDbs.add(new SimpleMapEntry(((QueueChecklist) ((TreeObject) cklDbListSelectedNode.getParent().getData()).getObj()).getQltnDbId(),
                                ((QueueChecklist) ((TreeObject) cklDbListSelectedNode.getData()).getObj()).getQueueId()));
                    }
                }
            List<String> lstIpServers = (lstIpServer == null) ? new ArrayList<>() : Splitter.on(",").trimResults().omitEmptyStrings().splitToList(lstIpServer);
            List<ActionServer> actionServers = new ArrayList<>();
            for (String ip : lstIpServers) {
                ActionServer server = new ActionServer();
                server.setIpServer(ip);
                List<OsAccount> osAccounts = iimService.findOsAccount(newObj.getCatCountryBO().getCountryCode(), ip);
                if (osAccounts != null) {
                    for (OsAccount osAccount : osAccounts) {
                        if (osAccount.getUserType().equals(2)) {
                            server.setMonitorAccount(osAccount.getUsername());
                            continue;
                        }
                    }
                }

                actionServers.add(server);
            }
            ResourceBundle bundle = ResourceBundle.getBundle("config");
            CrForOtherSystemServiceImplService service = new CrForOtherSystemServiceImplServiceLocator();
            CrForOtherSystemService gnocService = null;

            gnocService = service.getCrForOtherSystemServiceImplPort(new URL(bundle.getString("ws_gnoc_new")));

            newObj.setCreatedBy(username);
            newObj.setFullName(fullname);
            newObj.setStaffCode(staffCode);
            newObj.setCreatedTime(new Date());
            newObj.setMaxConcurrent(3);
            newObj.setKbGroup(1L);
            newObj.setActionType(AamConstants.ACTION_TYPE.ACTION_TYPE_CR_UCTT);

            CrDTO crDTO = new CrDTO();

            crDTO.setTitle(newObj.getCrName());
            crDTO.setDescription(newObj.getReason());
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            crDTO.setEarliestStartTime(sdf.format(newObj.getBeginTime()));
            sdf = new SimpleDateFormat("dd/MM/yyyy");
            crDTO.setLatestStartTime(sdf.format(newObj.getBeginTime()) + " 23:59:00");
            crDTO.setChangeOrginator(newObj.getCreatedBy());
            crDTO.setChangeResponsible(newObj.getCreatedBy());
            crDTO.setCountry("VN");
            crDTO.setRegion("KV1");
            List<CrImpactedNodesDTO> nodesDTOS = new ArrayList<>();

            for (Entry<Long, Module> moduleEntry : impactModules.entrySet()) {
                CrImpactedNodesDTO nodesDTO = new CrImpactedNodesDTO();
                nodesDTO.setIp(moduleEntry.getValue().getIpServer());
                nodesDTOS.add(nodesDTO);
            }

            crDTO.setLstNetworkNodeId(nodesDTOS.toArray(new CrImpactedNodesDTO[nodesDTOS.size()]));
            ResultDTO resultDTO = gnocService.createCRTrace(bundle.getString("ws_gnoc_user"), PasswordEncoder.decrypt(bundle.getString("ws_gnoc_pass")), crDTO);

            if (StringUtils.isNotEmpty(resultDTO.getId())) {
                newObj.setCrId(resultDTO.getId());
                newObj.setCrNumber(resultDTO.getMessage());
                newObj.setCrState(201L);
//                            List<String> lstIpServers = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(lstIpServer);

                //20190416_tudn_start import rule config
//                actionService.saveOrUpdateAction(newObj, actionDetailAppController.getListDetailsApp(),
//                        actionDetailDatabaseController.getDetailDatabases(), new ArrayList<>(impactModules.values()), multimap, kpiDbs,
//                        testCaseController.getTestCases(), actionCustomGroupController.getCustomGroups(),
//                        null, selectedDatabases, actionServers, dualListModel.getTarget());
                actionService.saveOrUpdateAction(newObj, actionDetailAppController.getListDetailsApp(),
                        actionDetailDatabaseController.getDetailDatabases(), new ArrayList<>(impactModules.values()), multimap, kpiDbs,
                        testCaseController.getTestCases(), actionCustomGroupController.getCustomGroups(),
                        null, selectedDatabases, actionServers, dualListModel.getTarget(), actionDetailAppController.getRuleConfigList());
                //20190416_tudn_end import rule config

                DocxUtil.export(newObj, resultDTO.getMessage());
                List<Integer> kbGroups = actionService.findKbGroups(newObj.getId());


                String cr_number = newObj.getCrNumber();

                String prefixName = null;
                if (newObj.getActionType().equals(Constant.ACTION_TYPE_CR_NORMAL)) {
                    prefixName = "MOP.CNTT.";
                } else if (newObj.getActionType().equals(Constant.ACTION_TYPE_CR_UCTT)) {
                    prefixName = "MOP.CNTT.";
                } else if (newObj.getActionType().equals(Constant.ACTION_TYPE_KB_UCTT)) {
                    prefixName = "KB.UCTT.";
                }

                String date_time2 = new SimpleDateFormat("ddMMyyyy").format(newObj.getCreatedTime());
                String cr = cr_number.split("_")[cr_number.split("_").length - 1];
                String appName = Util.convertUTF8ToNoSign(new DocxUtil(newObj).getAppGroupName(newObj.getId())).replaceAll("\\?", "");
                String mopAction = prefixName + appName + "_" + cr + "_" + date_time2 + "_tacdong_" + (kbGroups.size() < 2 ? kbGroups.get(0) + ".docx" : ".zip");
                String mopRollBack = prefixName + appName + "_" + cr + "_" + date_time2 + "_rollback_" + (kbGroups.size() < 2 ? kbGroups.get(0) + ".docx" : ".zip");

                resultDTO = gnocService.createCRTraceFileAttach(bundle.getString("ws_gnoc_user"), PasswordEncoder.decrypt(bundle.getString("ws_gnoc_pass")), crDTO.getChangeOrginator(), newObj.getCrId(), Constant.DT_EXECUTE, mopAction, Base64.encodeBase64String(FileUtils.readFileToByteArray(new File(UploadFileUtils.getMopFolder(newObj) + File.separator + mopAction))));
                logger.info(resultDTO.getKey());
                resultDTO = gnocService.createCRTraceFileAttach(bundle.getString("ws_gnoc_user"), PasswordEncoder.decrypt(bundle.getString("ws_gnoc_pass")), crDTO.getChangeOrginator(), newObj.getCrId(), Constant.DT_ROLLBACK, mopRollBack, Base64.encodeBase64String(FileUtils.readFileToByteArray(new File(UploadFileUtils.getMopFolder(newObj) + File.separator + mopRollBack))));
                logger.info(resultDTO.getKey());

                if (testCaseController.getTestCases() != null) {
                    for (TestCase testCase : testCaseController.getTestCases()) {
                        resultDTO = gnocService.createCRTraceFileAttach(bundle.getString("ws_gnoc_user"), PasswordEncoder.decrypt(bundle.getString("ws_gnoc_pass")), crDTO.getChangeOrginator(), newObj.getCrId(), Constant.FORM_TEST_SERVICE, testCase.getFileName(), Base64.encodeBase64String(FileUtils.readFileToByteArray(new File(UploadFileUtils.getTestcaseFolder(newObj) + File.separator + testCase.getFileName()))));
                        logger.info(resultDTO.getKey());
                    }
                }

                msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("create.cr.successful") + ": " + newObj.getCrNumber(), "");
            } else {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, resultDTO.getMessage(), "");
            }

        } catch (SysException | AppException e) {
            logger.error(e.getMessage(), e);
        } catch (RemoteException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (ServiceException e) {
            logger.error(e.getMessage(), e);
        } catch (GeneralSecurityException e) {
            logger.error(e.getMessage(), e);
        }
        TabView tabView = (TabView) FacesContext.getCurrentInstance().getViewRoot().findComponent("mop").findComponent("lst");
        Tab tab = (Tab) tabView.findComponent("edittab");
        tab.setRendered(true);
        tabView.setActiveIndex(1);
    }

    public void cloneTemplateUCTT(Action obj, boolean includeFile) {
        //20200320_Quytv7_fig loi toggle_start
        List<Integer> listHiddenDefault = Arrays.asList(2, 4, 7);
        columnVisible = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            if (listHiddenDefault.contains(i)) {
                columnVisible.add(false);
            } else {
                columnVisible.add(true);
            }
        }
        //20200320_Quytv7_fig loi toggle_end
        isUctt = true;
        clear();

        isEdit = false;
        selectedObj = obj;
        dataTableFilters.clear();
        impactModules.clear();
        normalImpactModules.clear();
        testbedDataTableFilters.clear();
        testbedImpactModules.clear();
        newObj = new Action();

        BeanUtilsBean.getInstance().getConvertUtils().register(false, true, 0);
        try {
            BeanUtils.copyProperties(newObj, obj);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error(e.getMessage(), e);
        }

        viewOnly = false;

        Map<String, Object> filters = new HashMap<>();
        filters.put("actionId", obj.getId() + "");

        try {
            List<ActionModule> actionModules = actionModuleService.findList(filters, new HashMap<String, String>());
            List<Long> moduleIds = new ArrayList<>();
            Map<Long, Integer> actionTypes = new HashMap<>();
            Map<Long, Integer> kbGroups = new HashMap<>();
            Map<Long, Integer> testbedModes = new HashMap<>();
            if (actionModules != null && !actionModules.isEmpty()) {
                for (ActionModule actionModule : actionModules) {
                    actionTypes.put(actionModule.getModuleId(), actionModule.getActionType());
                    kbGroups.put(actionModule.getModuleId(), actionModule.getKbGroup());
                    testbedModes.put(actionModule.getModuleId(), actionModule.getTestbedMode());
                    moduleIds.add(actionModule.getModuleId());
                }
                List<Module> modules = iimService.findModulesByIds(newObj.getCatCountryBO().getCountryCode(), moduleIds);
                for (Module module : modules) {
                    module.setActionType(actionTypes.get(module.getModuleId()));
                    module.setKbGroup(kbGroups.get(module.getModuleId()));
                    if (testbedModes.get(module.getModuleId()) == AamConstants.TESTBED_MODE.TESTBED) {
                        testbedDataTableFilters.add(module);
                        testbedImpactModules.put(module.getModuleId(), module);
                    } else {
                        dataTableFilters.add(module);
                        normalImpactModules.put(module.getModuleId(), module);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        actionDetailAppController.setAction(newObj);
        actionDetailAppController.viewSelectItems(this, true, includeFile);
        actionDetailDatabaseController.setAction(newObj);
        if (includeFile) {
            actionDetailDatabaseController.loadOldDb(this);
        }
        testCaseController.setAction(newObj);
        testCaseController.viewSelectItems();

        actionCustomGroupController.setActionController(this);
        actionCustomGroupController.viewSelectItems();

        newObj.setCreatedBy(username);
        newObj.setFullName(fullname);
        newObj.setStaffCode(staffCode);
        newObj.setCreatedTime(new Date());
        newObj.setActionType(AamConstants.ACTION_TYPE.ACTION_TYPE_KB_UCTT);
        newObj.setKbGroup(0L);
        String folder = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        try {
            FileUtils.copyDirectory(new File(UploadFileUtils.getBaseFolder() + File.separator + newObj.getSourceDir() + File.separator + "testcase"), new File(UploadFileUtils.getBaseFolder() + File.separator + folder + File.separator + "testcase"));
        } catch (IOException e) {
            logger.error(e.getMessage());
            logger.debug(e.getMessage(), e);
        }

        if (includeFile) {
            try {
                FileUtils.copyDirectory(new File(UploadFileUtils.getBaseFolder() + File.separator + newObj.getSourceDir() + File.separator + "source_code"), new File(UploadFileUtils.getBaseFolder() + File.separator + folder + File.separator + "source_code"));
            } catch (IOException e) {
                logger.error(e.getMessage());
                logger.debug(e.getMessage(), e);
            }

            try {
                FileUtils.copyDirectory(new File(UploadFileUtils.getBaseFolder() + File.separator + newObj.getSourceDir() + File.separator + "database"), new File(UploadFileUtils.getBaseFolder() + File.separator + folder + File.separator + "database"));
            } catch (IOException e) {
                logger.error(e.getMessage());
                logger.debug(e.getMessage(), e);
            }

            try {
                FileUtils.copyDirectory(new File(UploadFileUtils.getBaseFolder() + File.separator + newObj.getSourceDir() + File.separator + "data_import"), new File(UploadFileUtils.getBaseFolder() + File.separator + folder + File.separator + "data_import"));
            } catch (IOException e) {
                logger.error(e.getMessage());
                logger.debug(e.getMessage(), e);
            }
        }

        newObj.setSourceDir(folder);
        newObj.setId(null);
        newObj.setCrNumber(Constant.CR_DEFAULT);
        newObj.setRunStatus(null);
        newObj.setLinkCrTime(null);
        newObj.setCrState(null);
        newObj.setSignStatus(null);
        newObj.setVoTextId(null);
        newObj.setRunningStatus(null);

        List<Checklist> selectedChecklists = null;
        try {
            selectedChecklists = checklistService.findCheckListAppByAction(obj.getId());

            this.selectAppKpiMap.clear();

            for (Checklist objCkl : selectedChecklists) {
                if (this.selectAppKpiMap.containsKey(objCkl.getModuleId())) {
                    this.selectAppKpiMap.get(objCkl.getModuleId()).add(objCkl.getId());
                } else {
                    HashSet<Long> set = new HashSet<>();
                    set.add(objCkl.getId());
                    this.selectAppKpiMap.put(objCkl.getModuleId(), set);
                }
            }

        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }
        // Load data for tree node
        root = initCheckboxServices(new ArrayList<>(), false);
        TabView tabView = (TabView) FacesContext.getCurrentInstance().getViewRoot().findComponent("mop").findComponent("lst");
        Tab tab = (Tab) tabView.findComponent("edittab");
        tab.setRendered(true);
        tabView.setActiveIndex(1);
    }

    public void saveOrUpdate(boolean isCheck) {
        Date startTime = new Date();

        if (isCheck && !verifyController.getValid()) {
            RequestContext reqCtx = RequestContext.getCurrentInstance();
            reqCtx.execute("PF('confirmSaveWithowValid').show()");
            return;
        }

        if (verifyController.getValid())
            newObj.setVerifyStatus(Constant.FINISH_SUCCESS_STATUS);
        else
            newObj.setVerifyStatus(Constant.FINISH_FAIL_STATUS);

        Multimap<Long, Long> multimap = HashMultimap.create();
        List<Map.Entry<Long, Long>> kpiDbs = new ArrayList<>();

        for (Entry<Long, HashSet<Long>> set : this.selectAppKpiMap.entrySet()) {
            for (Long kpiId : set.getValue()) {
                multimap.put(set.getKey(), kpiId);
            }
        }

        if (cklDbListSelectedNodes != null)
            for (TreeNode cklDbListSelectedNode : cklDbListSelectedNodes) {
                if (((TreeObject) cklDbListSelectedNode.getData()).getIsLeaf()) {
                    kpiDbs.add(new SimpleMapEntry(((QueueChecklist) ((TreeObject) cklDbListSelectedNode.getParent().getData()).getObj()).getQltnDbId(),
                            ((QueueChecklist) ((TreeObject) cklDbListSelectedNode.getData()).getObj()).getQueueId()));

                }
            }

        FacesMessage msg = null;
        try {
            if (isEdit) {
            }

            selectedObj = new Action();

            BeanUtilsBean.getInstance().getConvertUtils().register(false, true, 0);
            BeanUtils.copyProperties(selectedObj, newObj);

            // anhnt2 - 07/18/2018 - When BD server or UC server then action_rb_sd : reboot = 1, shutdown = 2 else null
            if (newObj.getKbType() != null
                    && (newObj.getKbType().equals(AamConstants.KB_TYPE.BD_SERVER) || newObj.getKbType().equals(AamConstants.KB_TYPE.UC_SERVER))) {
                // Case when service is shutdown
                if (selectedAction != null) {
                    if (selectedAction.equals(2)) {
                        newObj.setActionRbSd(2l);
                        selectedObj.setActionRbSd(2l);
                        //selectedObj.setKbType(AamConstants.KB_TYPE.BD_SERVER_SHUTDOWN);
                    } else {
                        newObj.setActionRbSd(1l);
                        selectedObj.setActionRbSd(1l);
                    }
                }
            }

            // anhnt2 - 07/18/2018 - Only UC server
            if (newObj != null && newObj.getKbType() != null && newObj.getKbType().equals(AamConstants.KB_TYPE.UC_SERVER)) {
                if (isCheckUcServer) {
                    newObj.setIgnoreStopApp(1l);
                    selectedObj.setKbType(AamConstants.KB_TYPE.UC_SERVER);
                } else {
                    newObj.setIgnoreStopApp(0l);
                }
            }
            if (!isEdit) {
                selectedObj.setId(null);
                selectedObj.setCreatedBy(username);
                selectedObj.setFullName(fullname);
                selectedObj.setStaffCode(staffCode);
                selectedObj.setCreatedTime(new Date());
            } else {
                selectedObj.setUpdatedBy(username);
                selectedObj.setUpdatedTime(new Date());
            }
            List<String> lstIpServers = (lstIpServer == null) ? new ArrayList<>() : Splitter.on(",").trimResults().omitEmptyStrings().splitToList(lstIpServer);
            List<ActionServer> actionServers = new ArrayList<>();
            for (String ip : lstIpServers) {
                ActionServer server = new ActionServer();
                server.setIpServer(ip);
                List<OsAccount> osAccounts = iimService.findOsAccount(newObj.getCatCountryBO().getCountryCode(), ip);
                if (osAccounts != null) {
                    for (OsAccount osAccount : osAccounts) {
                        if (osAccount.getUserType().equals(2)) {
                            server.setMonitorAccount(osAccount.getUsername());
                            continue;
                        }
                    }
                }

                actionServers.add(server);
            }

            if (selectedObj.getLinkCrTime() != null) {
                ResourceBundle bundle = ResourceBundle.getBundle("config");
                CrForOtherSystemServiceImplService service = new CrForOtherSystemServiceImplServiceLocator();
                CrForOtherSystemService gnocService = service.getCrForOtherSystemServiceImplPort(new URL(bundle.getString("ws_gnoc_new")));

                CrOutputForQLTNDTO qltndto = GNOCService.getCrByCode(selectedObj.getCrNumber());
                Integer crState = Integer.valueOf(qltndto.getState());
                if (editable(crState)) {
                    if (selectedObj.getActionRbSd() == null) {
                        selectedObj.setActionRbSd(1L);
                    }
                    //20190416_tudn_start import rule config
//                    actionService.saveOrUpdateAction(selectedObj, actionDetailAppController.getListDetailsApp(),
//                            actionDetailDatabaseController.getDetailDatabases(), new ArrayList<>(impactModules.values()), multimap, kpiDbs,
//                            testCaseController.getTestCases(), actionCustomGroupController.getCustomGroups(),
//                            null, selectedDatabases, actionServers, dualListModel.getTarget());
                    actionService.saveOrUpdateAction(selectedObj, actionDetailAppController.getListDetailsApp(),
                            actionDetailDatabaseController.getDetailDatabases(), new ArrayList<>(impactModules.values()), multimap, kpiDbs,
                            testCaseController.getTestCases(), actionCustomGroupController.getCustomGroups(),
                            null, selectedDatabases, actionServers, dualListModel.getTarget(), actionDetailAppController.getRuleConfigList());
                    //20190416_tudn_end import rule config
                    // DocxUtil.export(selectedObj, null);

                    if (selectedObj.getLinkCrTime() != null) {
                        DocxUtil.export(selectedObj, selectedObj.getCrNumber());

                        File mopDir = new File(UploadFileUtils.getMopFolder(selectedObj));
                        logger.info(mopDir);

                        File mopFile = null;
                        File mopRollbackFile = null;
                        File[] files = mopDir.listFiles();
                        if (files != null) {
                            for (File file : files) {
                                if ((file.getPath().endsWith(".docx") && file.getPath().contains("tacdong_")) || file.getPath().endsWith("tacdong_.zip"))
                                    mopFile = file;

                                if ((file.getPath().endsWith(".docx") && file.getPath().contains("rollback_")) || file.getPath().endsWith("rollback_.zip"))
                                    mopRollbackFile = file;
                            }
                        }

                        if (mopFile != null && mopRollbackFile != null) {
                            String mopFileContent = Base64.encodeBase64String(FileUtils.readFileToByteArray(mopFile));
                            String mopRollackContent = Base64.encodeBase64String(FileUtils.readFileToByteArray(mopRollbackFile));

                            List<String> lstImpactIps = new DocxUtil(selectedObj).getListImpactIP(selectedObj.getId());
                            String[] impactIps = lstImpactIps.toArray(new String[lstImpactIps.size()]);

//                            List<Long> appIds = actionService.findListAppIds(selectedObj.getId());
//                            List<String> appGroups = applicationDetailService.findAppGroups(appIds);
//                            List<String> effectIps = applicationDetailService.findListEffectIps(appGroups);
                            List<String> serviceCodes = new ArrayList<>();
                            for (Module module : impactModules.values()) {
                                serviceCodes.add(module.getServiceCode());
                            }

//                            String[] arrayEffectIps = effectIps.toArray(new String[effectIps.size()]);
                            List<String> effectIps = iimService.findAllIpByServices(newObj.getCatCountryBO().getCountryCode(), serviceCodes);
                            String[] arrayEffectService = serviceCodes.toArray(new String[serviceCodes.size()]);
                            String[] arrayEffectIps = effectIps.toArray(new String[effectIps.size()]);

                            ResultDTO resultDTO = gnocService.updateDtInfo(bundle.getString("ws_gnoc_user"),
                                    PasswordEncoder.decrypt(bundle.getString("ws_gnoc_pass")), username, selectedObj.getCrNumber(),
                                    selectedObj.getTdCode(), impactIps, arrayEffectIps, mopFile.getName(), mopFileContent,
                                    mopRollbackFile.getName(), mopRollackContent, arrayEffectService, newObj.getCatCountryBO().getCountryCode());
                            logger.info(resultDTO.getKey() + "\t" + resultDTO.getMessage());
                        }
                    }

                    if (!isEdit) {
                        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("insert.successful"), "");
                    } else {
                        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("update.successful"), "");
                    }
                } else {
                    msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("cr.status") + " " + getCrState(selectedObj), "");
                }
            } else if (
//                    (newObj.getKbType() != null && newObj.getKbType() >= AamConstants.KB_TYPE.UCTT_STOP && newObj.getKbType() <= AamConstants.KB_TYPE.UCTT_RESTART) ||
                    (newObj.getKbGroup() != null && (newObj.getKbGroup().equals(2L)))) {
                ResourceBundle bundle = ResourceBundle.getBundle("config");
                CrForOtherSystemServiceImplService service = new CrForOtherSystemServiceImplServiceLocator();
                CrForOtherSystemService gnocService = null;

                gnocService = service.getCrForOtherSystemServiceImplPort(new URL(bundle.getString("ws_gnoc_new")));

                newObj.setCreatedBy(username);
                newObj.setFullName(fullname);
                newObj.setStaffCode(staffCode);
                newObj.setCreatedTime(new Date());
                newObj.setActionType(AamConstants.ACTION_TYPE.ACTION_TYPE_CR_UCTT);

                CrDTO crDTO = new CrDTO();

                crDTO.setTitle(newObj.getCrName());
                crDTO.setDescription(newObj.getReason());
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                crDTO.setEarliestStartTime(sdf.format(newObj.getBeginTime()));
                sdf = new SimpleDateFormat("dd/MM/yyyy");
                crDTO.setLatestStartTime(sdf.format(newObj.getBeginTime()) + " 23:59:00");
                crDTO.setChangeOrginator(newObj.getCreatedBy());
                crDTO.setChangeResponsible(newObj.getCreatedBy());
                crDTO.setCountry("VN");
                crDTO.setRegion("KV1");
/*
			crDTO.setChangeOrginator("thaoltk");
			crDTO.setChangeResponsible("thaoltk");
*/
                List<CrImpactedNodesDTO> nodesDTOS = new ArrayList<>();

                for (Entry<Long, Module> moduleEntry : impactModules.entrySet()) {
                    CrImpactedNodesDTO nodesDTO = new CrImpactedNodesDTO();
                    nodesDTO.setIp(moduleEntry.getValue().getIpServer());
                    nodesDTOS.add(nodesDTO);
                }

                crDTO.setLstNetworkNodeId(nodesDTOS.toArray(new CrImpactedNodesDTO[nodesDTOS.size()]));
                ResultDTO resultDTO = gnocService.createCRTrace(bundle.getString("ws_gnoc_user"), PasswordEncoder.decrypt(bundle.getString("ws_gnoc_pass")), crDTO);

                if (StringUtils.isNotEmpty(resultDTO.getId())) {
                    newObj.setCrId(resultDTO.getId());
                    newObj.setCrNumber(resultDTO.getMessage());
                    newObj.setCrState(201L);
//                            List<String> lstIpServers = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(lstIpServer);

                    //20190416_tudn_start import rule config
//                    actionService.saveOrUpdateAction(newObj, actionDetailAppController.getListDetailsApp(),
//                            actionDetailDatabaseController.getDetailDatabases(), new ArrayList<>(impactModules.values()), multimap, kpiDbs,
//                            testCaseController.getTestCases(), actionCustomGroupController.getCustomGroups(),
//                            null, selectedDatabases, actionServers, dualListModel.getTarget());
                    actionService.saveOrUpdateAction(newObj, actionDetailAppController.getListDetailsApp(),
                            actionDetailDatabaseController.getDetailDatabases(), new ArrayList<>(impactModules.values()), multimap, kpiDbs,
                            testCaseController.getTestCases(), actionCustomGroupController.getCustomGroups(),
                            null, selectedDatabases, actionServers, dualListModel.getTarget(), actionDetailAppController.getRuleConfigList());
                    //20190416_tudn_end import rule config

                    DocxUtil.export(newObj, resultDTO.getMessage());
                    List<Integer> kbGroups = actionService.findKbGroups(newObj.getId());


                    String cr_number = newObj.getCrNumber();

                    String prefixName = null;
                    if (newObj.getActionType().equals(Constant.ACTION_TYPE_CR_NORMAL)) {
                        prefixName = "MOP.CNTT.";
                    } else if (newObj.getActionType().equals(Constant.ACTION_TYPE_CR_UCTT)) {
                        prefixName = "MOP.CNTT.";
                    } else if (newObj.getActionType().equals(Constant.ACTION_TYPE_KB_UCTT)) {
                        prefixName = "KB.UCTT.";
                    }

                    String date_time2 = new SimpleDateFormat("ddMMyyyy").format(newObj.getCreatedTime());
                    String cr = cr_number.split("_")[cr_number.split("_").length - 1];
                    String appName = Util.convertUTF8ToNoSign(new DocxUtil(newObj).getAppGroupName(newObj.getId())).replaceAll("\\?", "");
                    String mopAction = prefixName + appName + "_" + cr + "_" + date_time2 + "_tacdong_" + (kbGroups.size() < 2 ? kbGroups.get(0) + ".docx" : ".zip");
                    String mopRollBack = prefixName + appName + "_" + cr + "_" + date_time2 + "_rollback_" + (kbGroups.size() < 2 ? kbGroups.get(0) + ".docx" : ".zip");

                    resultDTO = gnocService.createCRTraceFileAttach(bundle.getString("ws_gnoc_user"), PasswordEncoder.decrypt(bundle.getString("ws_gnoc_pass")), crDTO.getChangeOrginator(), newObj.getCrId(), Constant.DT_EXECUTE, mopAction, Base64.encodeBase64String(FileUtils.readFileToByteArray(new File(UploadFileUtils.getMopFolder(newObj) + File.separator + mopAction))));
                    logger.info(resultDTO.getKey());
                    resultDTO = gnocService.createCRTraceFileAttach(bundle.getString("ws_gnoc_user"), PasswordEncoder.decrypt(bundle.getString("ws_gnoc_pass")), crDTO.getChangeOrginator(), newObj.getCrId(), Constant.DT_ROLLBACK, mopRollBack, Base64.encodeBase64String(FileUtils.readFileToByteArray(new File(UploadFileUtils.getMopFolder(newObj) + File.separator + mopRollBack))));
                    logger.info(resultDTO.getKey());

                    if (testCaseController.getTestCases() != null) {
                        for (TestCase testCase : testCaseController.getTestCases()) {
                            resultDTO = gnocService.createCRTraceFileAttach(bundle.getString("ws_gnoc_user"), PasswordEncoder.decrypt(bundle.getString("ws_gnoc_pass")), crDTO.getChangeOrginator(), newObj.getCrId(), Constant.FORM_TEST_SERVICE, testCase.getFileName(), Base64.encodeBase64String(FileUtils.readFileToByteArray(new File(UploadFileUtils.getTestcaseFolder(newObj) + File.separator + testCase.getFileName()))));
                            logger.info(resultDTO.getKey());
                        }
                    }

                    msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("create.cr.successful") + ": " + newObj.getCrNumber(), "");
                } else {
                    msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, resultDTO.getMessage(), "");
                }
            } else {
                //20190416_tudn_start import rule config
//                actionService.saveOrUpdateAction(selectedObj, actionDetailAppController.getListDetailsApp(),
//                        actionDetailDatabaseController.getDetailDatabases(), new ArrayList<>(impactModules.values()), multimap, kpiDbs,
//                        testCaseController.getTestCases(), actionCustomGroupController.getCustomGroups(),
//                        null, selectedDatabases, actionServers, dualListModel.getTarget());

                actionService.saveOrUpdateAction(selectedObj, actionDetailAppController.getListDetailsApp(),
                        actionDetailDatabaseController.getDetailDatabases(), new ArrayList<>(impactModules.values()), multimap, kpiDbs,
                        testCaseController.getTestCases(), actionCustomGroupController.getCustomGroups(),
                        null, selectedDatabases, actionServers, dualListModel.getTarget(), actionDetailAppController.getRuleConfigList());
                //20190416_tudn_end import rule config
                if ((newObj.getKbType() != null
//                        && newObj.getKbType() >= AamConstants.KB_TYPE.UCTT_STOP && newObj.getKbType() <= AamConstants.KB_TYPE.UCTT_RESTART
                )) {
                    if (selectedObj.getId() != null && selectedObj.getCrNumber().equalsIgnoreCase(Constant.CR_DEFAULT)) {
                        selectedObj.setCrNumber("CR_AAM_EMERGENCY_CNTT_" + selectedObj.getId());
                        actionService.updateCrNumber(selectedObj.getId(), selectedObj.getCrNumber());
                    }
                }
                if (!isEdit) {
                    msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("insert.successful"), "");
                } else {
                    msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("update.successful"), "");
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | SysException | AppException e) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("update.failed"), "");
            logger.error(e.getMessage(), e);
        } catch (RemoteException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (ServiceException e) {
            logger.error(e.getMessage(), e);
        } catch (GeneralSecurityException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (msg == null)
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("have.some.error"), "");
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
            newObj = new Action();
//			newObj.setActionType(Constant.ACTION_TYPE_CR_NORMAL);
            isEdit = false;

            clear();
        }

        TabView tabView = (TabView) FacesContext.getCurrentInstance().getViewRoot().findComponent("mop")
                .findComponent("lst");
        Tab tab = (Tab) tabView.findComponent("edittab");
        // tab.setDisabled(true);
        tab.setRendered(false);
        tabView.setActiveIndex(0);

        try {
            LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                    LogUtils.getRemoteIpClient(), LogUtils.getUrl(), this.getClass().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName(), isEdit ? LogUtils.ActionType.UPDATE : LogUtils.ActionType.CREATE,
                    selectedObj.actionLog(), LogUtils.getRequestSessionId());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void closeTab() {
        TabView tabView = (TabView) FacesContext.getCurrentInstance().getViewRoot().findComponent("mop")
                .findComponent("lst");
        Tab tab = (Tab) tabView.findComponent("edittab");
        // tab.setDisabled(true);
        tab.setRendered(false);
        tabView.setActiveIndex(0);
    }

    public void approve() {
        Date startTime = new Date();

        FacesMessage msg = null;
        try {
            selectedObj.setCrState(202L);
            actionService.saveOrUpdate(selectedObj);
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("approval.successful"), "");
        } catch (SysException | AppException e) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("approval.failed"), "");
            logger.error(e.getMessage(), e);
        } finally {
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
        }

        try {
            LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                    LogUtils.getRemoteIpClient(), LogUtils.getUrl(), this.getClass().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName(), LogUtils.ActionType.APPROVE,
                    selectedObj.actionLog(), LogUtils.getRequestSessionId());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
        }
    }

    public void delete() {
        Date startTime = new Date();

        FacesMessage msg = null;
        try {
            actionService.delete(selectedObj);
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("delete.successful"), "");
        } catch (SysException | AppException e) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("delete.failed"), "");
            logger.error(e.getMessage(), e);
        } finally {
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
        }

        try {
            LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                    LogUtils.getRemoteIpClient(), LogUtils.getUrl(), this.getClass().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName(), LogUtils.ActionType.DELETE,
                    selectedObj.actionLog(), LogUtils.getRequestSessionId());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
        }
    }

    public void signVoffice() {
        FacesMessage msg = null;
        try {
            Voffice voffice = new Voffice();

            List<Integer> kbGroups = null;
            try {
                kbGroups = actionService.findKbGroups(selectedObj.getId());
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }

            if (kbGroups == null)
                kbGroups = new ArrayList<>();

            if (!kbGroups.contains(1)) {
                kbGroups.add(1);
            }

            if (kbGroups.contains(null))
                kbGroups.remove(null);

            Collections.sort(kbGroups);

            Long status = voffice.signVoffice(selectedObj, passSso, kbGroups);
            if (status != null && status.equals(1L)) {
                msg = new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("voffice.sign.error.1"), "");
                actionService.updateVofficeStatus(selectedObj.getId(), "-1");
            } else {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("voffice.sign.error." + status), "");
            }
        } catch (Exception e) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("sign.failed"), "");
            logger.error(e.getMessage(), e);
        } finally {
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
        }
    }

    public void confirmCreateCr() {
        FacesMessage msg = null;
        if (selectedKb == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("service.mop.cr_uctt.warning.kb_null"), "");
        } else if (StringUtils.isEmpty(reasonUctt)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("service.mop.cr_uctt.warning.reason_null"), "");
        } else if (startTimeUctt == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("service.mop.cr_uctt.warning.start_time_null"), "");
        } else if (endTimeUctt == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("service.mop.cr_uctt.warning.end_time_null"), "");
        } else if (new DateTime((startTimeUctt)).isBeforeNow()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("service.mop.cr_uctt.warning.start_time.before_now"), "");
        } else if (!new DateTime((endTimeUctt)).isAfter(new DateTime(startTimeUctt))) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("service.mop.cr_uctt.warning.end_time.before_start"), "");
        }

        if (msg == null) {
            RequestContext reqCtx = RequestContext.getCurrentInstance();
            reqCtx.execute("PF('confirmCreateCr').show()");
        } else {
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
        }
    }

    public void confirmApproveRollback() {
        Date startTime = new Date();

        FacesMessage msg = null;
        if (StringUtils.isEmpty(this.reasonUctt)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, this.bundle.getString("service.mop.rollback.warning.reason_null"), "");
        } else if (StringUtils.isEmpty(this.userRollback)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, this.bundle.getString("service.mop.rollback_user.warning.reason_null"), "");
        } else if (this.startTimeUctt == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, this.bundle.getString("service.mop.cr_uctt.warning.start_time_null"), "");
        } else if (this.endTimeUctt == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, this.bundle.getString("service.mop.cr_uctt.warning.end_time_null"), "");
        } else if (new DateTime(this.startTimeUctt).isBeforeNow()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, this.bundle.getString("service.mop.cr_uctt.warning.start_time.before_now"), "");
        } else if (!new DateTime(this.endTimeUctt).isAfter(new DateTime(this.startTimeUctt))) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, this.bundle.getString("service.mop.cr_uctt.warning.end_time.before_start"), "");
        }
        if (msg != null) {
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
        } else {
            try {
                this.actionService.updateRollbackCr(this.selectedObj.getId(), this.username, this.reasonUctt, this.userRollback, this.startTimeUctt, this.endTimeUctt);
                msg = new FacesMessage(FacesMessage.SEVERITY_INFO, this.bundle.getString("service.mop.rollback.approve.success"), "");
                RequestContext reqCtx = RequestContext.getCurrentInstance();
                reqCtx.execute("PF('confirmRollback').hide()");
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), "");
            }
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
        }
    }

    public void confirmApproveImpactUctt() {
        Date startTime = new Date();

        FacesMessage msg = null;
        if (StringUtils.isEmpty(this.reasonUctt)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, this.bundle.getString("service.mop.impact.uctt.warning.reason_null"), "");
        } else if (StringUtils.isEmpty(this.userRollback)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, this.bundle.getString("service.mop.impact.uctt.user.warning.null"), "");
        } else if (this.startTimeUctt == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, this.bundle.getString("service.mop.cr_uctt.warning.start_time_null"), "");
        } else if (this.endTimeUctt == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, this.bundle.getString("service.mop.cr_uctt.warning.end_time_null"), "");
        }
//        else if (new DateTime(this.startTimeUctt).isBeforeNow()) {
//            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, this.bundle.getString("service.mop.cr_uctt.warning.start_time.before_now"), "");
//        }
        else if (!new DateTime(this.endTimeUctt).isAfter(new DateTime(this.startTimeUctt))) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, this.bundle.getString("service.mop.cr_uctt.warning.end_time.before_start"), "");
        }
        if (msg != null) {
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
        } else {
            try {
                this.actionService.updateImpactUcttCr(this.selectedObj.getId(), this.username, this.reasonUctt, this.userRollback, this.startTimeUctt, this.endTimeUctt);
                msg = new FacesMessage(FacesMessage.SEVERITY_INFO, this.bundle.getString("service.mop.rollback.approve.success"), "");
                RequestContext reqCtx = RequestContext.getCurrentInstance();
                reqCtx.execute("PF('confirmImpactUctt').hide()");
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), "");
            }
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
            try {
                Gson gson = new Gson();
                LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                        LogUtils.getRemoteIpClient(), LogUtils.getUrl(), this.getClass().getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(), LogUtils.ActionType.CREATE,
                        gson.toJson(selectedObj), selectedObj.getId().toString());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

        }
    }

    /*20181023_hoangnd_approval impact step_start*/
    public void confirmApproveImpactStep() {
        Date startTime = new Date();
        FacesMessage msg = null;
        if (StringUtils.isBlank(this.reasonImpactStep)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, this.bundle.getString("service.mop.impact.step.warning.reason_null"), "");
        } else if (StringUtils.isBlank(this.exeImpactStep)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, this.bundle.getString("service.mop.impact.step.warning.user_null"), "");
        }
        try {
            this.actionService.updateImpactStep(this.selectedObj.getId(), this.exeImpactStep, this.reasonImpactStep);
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, this.bundle.getString("service.mop.rollback.approve.success"), "");
            LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                    LogUtils.getRemoteIpClient(), LogUtils.getUrl(), this.getClass().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName(), LogUtils.ActionType.CREATE,
                    selectedObj.actionLog(), LogUtils.getRequestSessionId());
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        RequestContext reqCtx = RequestContext.getCurrentInstance();
        reqCtx.execute("PF('confirmImpactStep').hide()");
        if (msg != null) {
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
        }
    }

    /*20181023_hoangnd_approval impact step_end*/
    public void createCrUctt() {
        Date startTime = new Date();

        FacesMessage msg = null;
        try {
            List<String> ipImpacts = new ArrayList<>();
            clear();

            isEdit = false;
//			selectedObj = obj;
//            dataTable.clear();
            dataTableFilters.clear();
//            hmModuleId.clear();
            impactModules.clear();
            normalImpactModules.clear();
            newObj = new Action();
//		newObj.setActionType(Constant.ACTION_TYPE_CR_NORMAL);

            BeanUtilsBean.getInstance().getConvertUtils().register(false, true, 0);
            try {
                BeanUtils.copyProperties(newObj, selectedKb);
                // newObj.setPassword("");
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.error(e.getMessage(), e);
            }

            viewOnly = false;

            Map<String, Object> filters = new HashMap<>();
            filters.put("actionId", selectedKb.getId() + "");

            try {
                List<ActionModule> actionModules = actionModuleService.findList(filters, new HashMap<String, String>());
                List<Long> moduleIds = new ArrayList<>();
                Map<Long, Integer> actionTypes = new HashMap<>();
                if (actionModules != null && !actionModules.isEmpty()) {
                    for (ActionModule actionModule : actionModules) {
                        actionTypes.put(actionModule.getModuleId(), actionModule.getActionType());
                        moduleIds.add(actionModule.getModuleId());
                    }
//                  dataTable = applicationDetailService.search(moduleIds);
                    getCountryFromProcessImpact(newObj);
                    handleChangeCountry(newObj);
                    List<Module> modules = iimService.findModulesByIds(newObj.getCatCountryBO().getCountryCode(), moduleIds);
                    for (Module module : modules) {
                        module.setActionType(actionTypes.get(module.getModuleId()));
                        dataTableFilters.add(module);
                        module.setTestbedMode(AamConstants.TESTBED_MODE.NORMAL);
                        impactModules.put(module.getModuleId(), module);
                        normalImpactModules.put(module.getModuleId(), module);
//                        hmModuleId.add(module.getModuleId());
                    }
                }
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }

            //fgherherh
            Module parent = new Module();
            this.cklDefaultRoot = new DefaultTreeNode(parent, null);
//            createTreeCklApp(cklDefaultRoot, new ArrayList<>(impactModules.values()));
            createTreeCklApp(cklDefaultRoot, new ArrayList<>(normalImpactModules.values()));

            ServiceDatabase serviceDb = new ServiceDatabase();
            this.cklDbDefaultRoot = new DefaultTreeNode(serviceDb, null);
//            createTreeCklDb(cklDbDefaultRoot, new ArrayList<>(impactModules.values()));
            createTreeCklDb(cklDbDefaultRoot, new ArrayList<>(normalImpactModules.values()));

//            buildListModuleSelected(new ArrayList<>(impactModules.values()));
            buildListModuleSelected(new ArrayList<>(normalImpactModules.values()));
            actionDetailAppController.buildLstAction();

            actionDetailDatabaseController.viewSelectItems(this);

            verifyController.setListModules(this);
            //eththrth


            actionDetailAppController.setAction(newObj);
            actionDetailAppController.viewSelectItems(this, true, true);
            actionDetailAppController.buildLstAction();

            actionDetailDatabaseController.setAction(newObj);
//			actionDetailDatabaseController.viewSelectItems(this);
            actionDetailDatabaseController.loadOldDb(this);
            actionDetailDatabaseController.viewSelectItems(this);

            testCaseController.setAction(newObj);
            testCaseController.viewSelectItems();
//		actionDetailDatabaseController.loadOldDb(this);
            actionCustomGroupController.setActionController(this);

            actionDetailAppController.buildLstAction();

            newObj.setCreatedBy(username);
            newObj.setFullName(fullname);
            newObj.setStaffCode(staffCode);
            newObj.setCreatedTime(new Date());
            newObj.setActionType(Constant.ACTION_TYPE_CR_UCTT);
            newObj.setReason(reasonUctt);
            newObj.setBeginTime(startTimeUctt);
            newObj.setEndTime(endTimeUctt);

            String folder = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            try {
                FileUtils.copyDirectory(new File(UploadFileUtils.getBaseFolder() + File.separator + newObj.getSourceDir() + File.separator + "testcase"), new File(UploadFileUtils.getBaseFolder() + File.separator + folder + File.separator + "testcase"));
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }

            try {
                FileUtils.copyDirectory(new File(UploadFileUtils.getBaseFolder() + File.separator + newObj.getSourceDir() + File.separator + "source_code"), new File(UploadFileUtils.getBaseFolder() + File.separator + folder + File.separator + "source_code"));
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }

            try {
                FileUtils.copyDirectory(new File(UploadFileUtils.getBaseFolder() + File.separator + newObj.getSourceDir() + File.separator + "database"), new File(UploadFileUtils.getBaseFolder() + File.separator + folder + File.separator + "database"));
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }

            try {
                FileUtils.copyDirectory(new File(UploadFileUtils.getBaseFolder() + File.separator + newObj.getSourceDir() + File.separator + "data_import"), new File(UploadFileUtils.getBaseFolder() + File.separator + folder + File.separator + "data_import"));
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }

            newObj.setSourceDir(folder);
            newObj.setId(null);
//		newObj.setCrName("");
            newObj.setCrNumber(Constant.CR_DEFAULT);
            newObj.setRunStatus(null);
            newObj.setLinkCrTime(null);
            newObj.setCrState(null);

            // cklListSelectedNodes.
            Multimap<Long, Long> multimap = HashMultimap.create();
            List<Map.Entry<Long, Long>> kpiDbs = new ArrayList<>();

            for (Entry<Long, HashSet<Long>> set : this.selectAppKpiMap.entrySet()) {
                for (Long kpiId : set.getValue()) {
                    multimap.put(set.getKey(), kpiId);
                }
            }

            if (cklDbListSelectedNodes != null)
                for (TreeNode cklDbListSelectedNode : cklDbListSelectedNodes) {
                    if (((TreeObject) cklDbListSelectedNode.getData()).getObj() instanceof Checklist) {
                        kpiDbs.add(new SimpleMapEntry(((ServiceDatabase) ((TreeObject) cklDbListSelectedNode.getParent().getData()).getObj()).getServiceDbId(),
                                ((Checklist) ((TreeObject) cklDbListSelectedNode.getData()).getObj()).getId()));
                    }
                }


			/*TabView tabView = (TabView) FacesContext.getCurrentInstance().getViewRoot().findComponent("mop").findComponent("lst");
			Tab tab = (Tab) tabView.findComponent("edittab");
			// tab.setDisabled(true);
			tab.setRendered(true);
			tabView.setActiveIndex(1);*/
            logger.info(selectedKb);

            ResourceBundle bundle = ResourceBundle.getBundle("config");
            CrForOtherSystemServiceImplService service = new CrForOtherSystemServiceImplServiceLocator();
            CrForOtherSystemService gnocService = null;

            gnocService = service.getCrForOtherSystemServiceImplPort(new URL(bundle.getString("ws_gnoc_new")));
            CrDTO crDTO = new CrDTO();

            crDTO.setTitle(newObj.getCrName());
            crDTO.setDescription(newObj.getReason());
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            crDTO.setEarliestStartTime(sdf.format(newObj.getBeginTime()));
            sdf = new SimpleDateFormat("dd/MM/yyyy");
            crDTO.setLatestStartTime(sdf.format(newObj.getBeginTime()) + " 23:59:00");
            crDTO.setChangeOrginator(newObj.getCreatedBy());
            crDTO.setChangeResponsible(newObj.getCreatedBy());
            crDTO.setCountry("VN");
            crDTO.setRegion("KV1");
            crDTO.setIsTracingCr("1");

/*
			crDTO.setChangeOrginator("thaoltk");
			crDTO.setChangeResponsible("thaoltk");
*/
            List<CrImpactedNodesDTO> nodesDTOS = new ArrayList<>();
            List<CrAffectedNodesDTO> nodesAffectDTOS = new ArrayList<>();
            CrImpactedNodesDTO nodesDTO;
            CrAffectedNodesDTO nodesAffectDTO;
            List<String> listIpImpact = new ArrayList<>();
            List<String> effectIps = new ArrayList<>();
            //Quytv7_20200423 bo sung truyen ip sang cr cho luong tao mop uctt start
            try {
                listIpImpact = new DocxUtil(selectedKb).getListImpactIP(selectedKb.getId());
                if (listIpImpact != null && !listIpImpact.isEmpty()) {
                    for (String ip : listIpImpact) {
                        nodesDTO = new CrImpactedNodesDTO();
                        nodesDTO.setIp(ip);
                        nodesDTOS.add(nodesDTO);
                    }
                }

                List<Long> appIds = actionService.findListAppIds(selectedKb.getId());
                logger.info("list appIds: " + (appIds == null ? 0 : appIds.size()));
                if (appIds != null && appIds.size() > 0) {
                    List<Service> services = iimService.findServicesByModules(selectedKb.getImpactProcess().getNationCode(), appIds);
                    List<String> appGroups = new ArrayList<>();
                    for (Service service1 : services) {
                        appGroups.add(service1.getServiceCode());
                    }
                    effectIps = iimService.findAllIpByServices(selectedKb.getImpactProcess().getNationCode(), appGroups);
                    if (effectIps != null && !effectIps.isEmpty()) {
                        for (String ip : effectIps) {
                            nodesAffectDTO = new CrAffectedNodesDTO();
                            nodesAffectDTO.setIpIdStr(ip);
                            nodesAffectDTOS.add(nodesAffectDTO);
                        }
                    }
                }
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
            //Quytv7_20200423 bo sung truyen ip sang cr cho luong tao mop uctt end
//            nodesDTO.setIp("1.1.1.1");
//            nodesDTOS.add(nodesDTO);
            crDTO.setLstNetworkNodeId(nodesDTOS.toArray(new CrImpactedNodesDTO[nodesDTOS.size()]));
            crDTO.setLstNetworkNodeIdAffected(nodesAffectDTOS.toArray(new CrAffectedNodesDTO[nodesDTOS.size()]));
            Gson gson = new Gson();
            Util.checkAndPrintObject(logger, "Start create CR trace:", "Title", crDTO.getTitle(), "Description", crDTO.getDescription(),
                    "EarliestStartTime", crDTO.getEarliestStartTime(),
                    "LatestStartTime", crDTO.getLatestStartTime(),
                    "ChangeOrginator", crDTO.getChangeOrginator(),
                    "ChangeResponsible", crDTO.getChangeResponsible(),
                    "Country", crDTO.getCountry(),
                    "Region", crDTO.getRegion(),
                    "IsTracingCr", crDTO.getIsTracingCr(),
                    "ImpactIp", gson.toJson(listIpImpact),
                    "AffectIp", gson.toJson(effectIps)
            );
            ResultDTO resultDTO = gnocService.createCRTrace(bundle.getString("ws_gnoc_user"), PasswordEncoder.decrypt(bundle.getString("ws_gnoc_pass")), crDTO);
            Util.checkAndPrintObject(logger, "End create CR trace:", "ResultDTO", gson.toJson(resultDTO));

            if (StringUtils.isNotEmpty(resultDTO.getId())) {
                newObj.setCrId(resultDTO.getId());
                newObj.setCrNumber(resultDTO.getMessage());
                newObj.setCrState(201L);
                //List<String> lstIpServers = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(lstIpServer);
                List<ActionServer> actionServers = new ArrayList<>();
                if (StringUtils.isNotEmpty(lstIpServer)) {
                    List<String> lstIpServers = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(lstIpServer);
                    for (String ip : lstIpServers) {
                        ActionServer server = new ActionServer();
                        server.setIpServer(ip);
                        List<OsAccount> osAccounts = iimService.findOsAccount(newObj.getCatCountryBO().getCountryCode(), ip);
                        if (osAccounts != null) {
                            for (OsAccount osAccount : osAccounts) {
                                if (osAccount.getUserType().equals(2)) {
                                    server.setMonitorAccount(osAccount.getUsername());
                                    continue;
                                }
                            }
                        }

                        actionServers.add(server);
                    }
                }
//                actionService.saveOrUpdateAction(newObj, actionDetailAppController.getListDetailsApp(),
//                        actionDetailDatabaseController.getDetailDatabases(), new ArrayList<>(impactModules.values()), multimap, kpiDbs,
//                        testCaseController.getTestCases(), actionCustomGroupController.getCustomGroups(),
//                        null, selectedDatabases, actionServers, dualListModel.getTarget());

                //tuanda38_change to normalimpact
                //20190416_tudn_start import rule config
//                actionService.saveOrUpdateAction(newObj, actionDetailAppController.getListDetailsApp(),
//                        actionDetailDatabaseController.getDetailDatabases(), new ArrayList<>(normalImpactModules.values()), multimap, kpiDbs,
//                        testCaseController.getTestCases(), actionCustomGroupController.getCustomGroups(),
//                        null, selectedDatabases, actionServers, dualListModel.getTarget());
                actionService.saveOrUpdateAction(newObj, actionDetailAppController.getListDetailsApp(),
                        actionDetailDatabaseController.getDetailDatabases(), new ArrayList<>(normalImpactModules.values()), multimap, kpiDbs,
                        testCaseController.getTestCases(), actionCustomGroupController.getCustomGroups(),
                        null, selectedDatabases, actionServers, dualListModel.getTarget(), actionDetailAppController.getRuleConfigList());
                //20190416_tudn_end import rule config

                DocxUtil.export(newObj, resultDTO.getMessage());
                List<Integer> kbGroups = actionService.findKbGroups(newObj.getId());

                String cr_number = newObj.getCrNumber();

                String prefixName = "MOP.CNTT.";
                /*if (newObj.getActionType().equals(Constant.ACTION_TYPE_CR_NORMAL)) {
                    prefixName = "MOP.CNTT.";
                } else if (newObj.getActionType().equals(Constant.ACTION_TYPE_CR_UCTT)) {
                    prefixName = "MOP.CNTT.";
                } else if (newObj.getActionType().equals(Constant.ACTION_TYPE_KB_UCTT)) {
                    prefixName = "KB.UCTT.";
                }*/

                String date_time2 = new SimpleDateFormat("ddMMyyyy").format(newObj.getCreatedTime());
                String cr = cr_number.split("_")[cr_number.split("_").length - 1];
                String appName = Util.convertUTF8ToNoSign(new DocxUtil(newObj).getAppGroupName(newObj.getId())).replaceAll("\\?", "");
                String mopAction = prefixName + appName + "_" + cr + "_" + date_time2 + "_tacdong_" + (kbGroups.size() < 2 ? kbGroups.get(0) + ".docx" : ".zip");
                String mopRollBack = prefixName + appName + "_" + cr + "_" + date_time2 + "_rollback_" + (kbGroups.size() < 2 ? kbGroups.get(0) + ".docx" : ".zip");

                resultDTO = gnocService.createCRTraceFileAttach(bundle.getString("ws_gnoc_user"), PasswordEncoder.decrypt(bundle.getString("ws_gnoc_pass")), crDTO.getChangeOrginator(), newObj.getCrId(), Constant.DT_EXECUTE, mopAction, Base64.encodeBase64String(FileUtils.readFileToByteArray(new File(UploadFileUtils.getMopFolder(newObj) + File.separator + mopAction))));
                logger.info(resultDTO.getKey());
                resultDTO = gnocService.createCRTraceFileAttach(bundle.getString("ws_gnoc_user"), PasswordEncoder.decrypt(bundle.getString("ws_gnoc_pass")), crDTO.getChangeOrginator(), newObj.getCrId(), Constant.DT_ROLLBACK, mopRollBack, Base64.encodeBase64String(FileUtils.readFileToByteArray(new File(UploadFileUtils.getMopFolder(newObj) + File.separator + mopRollBack))));
                logger.info(resultDTO.getKey());

                if (testCaseController.getTestCases() != null) {
                    for (TestCase testCase : testCaseController.getTestCases()) {
                        resultDTO = gnocService.createCRTraceFileAttach(bundle.getString("ws_gnoc_user"), PasswordEncoder.decrypt(bundle.getString("ws_gnoc_pass")), crDTO.getChangeOrginator(), newObj.getCrId(), Constant.FORM_TEST_SERVICE, testCase.getFileName(), Base64.encodeBase64String(FileUtils.readFileToByteArray(new File(UploadFileUtils.getTestcaseFolder(newObj) + File.separator + testCase.getFileName()))));
                    }
                }

                msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("create.cr.successful") + ": " + newObj.getCrNumber(), "");
            } else {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, resultDTO.getMessage(), "");
            }
        } catch (Exception e) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("create.failed"), "");
            logger.error(e.getMessage(), e);
        } finally {
            if (msg == null)
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("create.failed"), "");
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
        }

        try {
            LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                    LogUtils.getRemoteIpClient(), LogUtils.getUrl(), this.getClass().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName(), LogUtils.ActionType.APPROVE,
                    selectedObj.actionLog(), LogUtils.getRequestSessionId());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void onSelectKb() {

    }

    public void resetCrUctt() {
        selectedKb = null;
        reasonUctt = "- Request: \n\n" +
                "- What: \n\n" +
                "- Why: \n\n" +
                "- Where: \n\n" +
                "- When: \n\n" +
                "- Who: \n\n" +
                "- How: ";
        startTimeUctt = null;
        endTimeUctt = null;
    }

    public String onFlowProcess(FlowEvent event) {
        try {
            this.fitterAppName = null;
            this.fitterKpiIds = null;

            FacesMessage msg;

            if ("common".equals(event.getOldStep())) {
                /*20180706_hoangnd_check_null_company_start*/
                if (newObj.getCatCountryBO() == null) {
                    msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("error.mop.countryCode.empty"), "");
                    FacesContext.getCurrentInstance().addMessage("commonGrowl", msg);
                    return event.getOldStep();
                }
                if (newObj.getImpactProcess() == null) {
                    msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("error.config.action.company.empty"), "");
                    FacesContext.getCurrentInstance().addMessage("commonGrowl", msg);
                    return event.getOldStep();
                } else if (StringUtils.isEmpty(newObj.getCrNumber())) {
                    /*20180706_hoangnd_check_null_company_end*/
                    msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("service.mop.warning.cr_number_null"), "");
                    FacesContext.getCurrentInstance().addMessage("commonGrowl", msg);
                    return event.getOldStep();
                } else if (StringUtils.isEmpty(newObj.getCrName())) {
                    msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, isUctt ? bundle.getString("service.mop.warning.kb_name_null") : bundle.getString("service.mop.warning.cr_name_null"), "");
                    FacesContext.getCurrentInstance().addMessage("commonGrowl", msg);
                    return event.getOldStep();
                } else if (StringUtils.isEmpty(newObj.getReason())) {
                    msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("service.mop.warning.reason_null"), "");
                    FacesContext.getCurrentInstance().addMessage("commonGrowl", msg);
                    return event.getOldStep();
                } else if (newObj.getBeginTime() == null || newObj.getEndTime() == null) {
                    msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("service.mop.warning.time_null"), "");
                    FacesContext.getCurrentInstance().addMessage("commonGrowl", msg);
                    return event.getOldStep();
                }
                else if (StringUtils.isEmpty(newObj.getService())) {
                    msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("service.mop.warning.service_null"), "");
                    FacesContext.getCurrentInstance().addMessage("commonGrowl", msg);
                    return event.getOldStep();
                }
                if (isUctt) {
                    if (newObj.getUcttType() == null) {
                        msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("service.mop.warning.uctt_kb_null"), "");
                        FacesContext.getCurrentInstance().addMessage("commonGrowl", msg);
                        return event.getOldStep();
                    }

                    //20182806_tudn_start cau hinh mot chu ky
//                    if (StringUtils.isEmpty(newObj.getUserSign1()) || StringUtils.isEmpty(newObj.getUserSign2()) || StringUtils.isEmpty(newObj.getUserSign3())) {
//                        msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("service.mop.warning.uctt_sign_null"), "");
//                        FacesContext.getCurrentInstance().addMessage("commonGrowl", msg);
//                        return event.getOldStep();
//                    }
                    if (!StringUtils.isEmpty(newObj.getUserSign1()) || !StringUtils.isEmpty(newObj.getUserSign2()) || !StringUtils.isEmpty(newObj.getUserSign3())) {
                        if ((!StringUtils.isEmpty(newObj.getUserSign1()) && StringUtils.isEmpty(newObj.getLabelSign1()))
                                || (!StringUtils.isEmpty(newObj.getUserSign2()) && StringUtils.isEmpty(newObj.getLabelSign2()))
                                || (!StringUtils.isEmpty(newObj.getUserSign3()) && StringUtils.isEmpty(newObj.getLabelSign3()))) {
                            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("label.position.sign.enter"), "");
                            FacesContext.getCurrentInstance().addMessage("commonGrowl", msg);
                            return event.getOldStep();
                        }
                    } else {
                        msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("service.mop.warning.uctt_sign_null"), "");
                        FacesContext.getCurrentInstance().addMessage("commonGrowl", msg);
                        return event.getOldStep();
                    }
                    if (!StringUtils.isEmpty(newObj.getLabelSign1()) || !StringUtils.isEmpty(newObj.getLabelSign2()) || !StringUtils.isEmpty(newObj.getLabelSign3())) {
                        if ((!StringUtils.isEmpty(newObj.getLabelSign1()) && StringUtils.isEmpty(newObj.getUserSign1()))
                                || (!StringUtils.isEmpty(newObj.getLabelSign2()) && StringUtils.isEmpty(newObj.getUserSign2()))
                                || (!StringUtils.isEmpty(newObj.getLabelSign3()) && StringUtils.isEmpty(newObj.getUserSign3()))) {
                            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("label.sign.position.enter"), "");
                            FacesContext.getCurrentInstance().addMessage("commonGrowl", msg);
                            return event.getOldStep();
                        }
                    }
                    //20182806_tudn_end cau hinh mot chu ky
                }

                Set<Long> allImpactMdIds = new HashSet<>(normalImpactModules.keySet());
                allImpactMdIds.addAll(testbedImpactModules.keySet());
                lazyAppModel = new LazyModule(iimService, new HashMap<>(), allImpactMdIds, unitId, newObj.getCatCountryBO().getCountryCode());

 /*               try {
                    services = iimService.findService(newObj.getImpactProcess().getNationCode(), newObj.getImpactProcess().getUnitId());
                } catch (AppException e) {
                    logger.error(e.getMessage(), e);
                }*/

                try {
                    databases = iimService.findDatabases(newObj.getCatCountryBO().getCountryCode(), newObj.getCatCountryBO().getUnitId());
                } catch (AppException e) {
                    logger.error(e.getMessage(), e);
                }

                // anhnt2 - Load data for tree node
                // Case edit load by list module impact - anhnt2 - 07/23/2018
                List<Module> modules = new ArrayList<>();
                if (normalImpactModules.size() > 0) {
                    for (Module module : normalImpactModules.values()) {
                        modules.add(module);
                    }
                }
                root = initCheckboxServices(modules, false);

//                actionDetailAppController.viewSelectItems(this, true, false);
//                actionDetailDatabaseController.viewSelectItems(this);
            }

            switch (event.getNewStep()) {
                case "module":

                    if (newObj.getKbGroup() != null && newObj.getKbGroup().equals(1L)) {
                        if (newObj.getMaxConcurrent() == null) {
                            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("service.mop.warning.concurrent_null"), "");
                            FacesContext.getCurrentInstance().addMessage("commonGrowl", msg);
                            return "uctt";
                        }
                        if (newObj.getKbType().equals(AamConstants.KB_TYPE.BD_SERVER) || newObj.getKbType().equals(AamConstants.KB_TYPE.UC_SERVER)) {
                            if (lstIpServer == null || lstIpServer.isEmpty() || lstIpServer.equals("")) {
                                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("mop.app.bd.server.ip"), "");
                                FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                                return "uctt";
                            }
                            if (StringUtils.isNotEmpty(lstIpServer)) {
                                List<String> ipServers = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(lstIpServer);
                                if (ipServers == null || ipServers.isEmpty()) {
                                    msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("mop.app.bd.server.ip"), "");
                                    FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                                    return "uctt";
                                } else {
                                    for (String ip : ipServers) {
                                        OsAccount osAccount = Util.findOSAccountByIpServer(ip);
                                        if (osAccount == null) {
                                            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("error.osAccount.is.null"), ip);
                                            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                                            return "uctt";
                                        }
                                        if (osAccount != null && (Util.isNullOrEmpty(osAccount.getOsVersion()) || osAccount.getOsVersion().equalsIgnoreCase("N/A"))) {
                                            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("error.osAccount.is.version.null"), osAccount.getUsername());
                                            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                                            return "uctt";
                                        }
                                        if (osAccount != null && (Util.isNullOrEmpty(osAccount.getOsType()))) {
                                            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("error.osAccount.is.osType.null"), osAccount.getUsername());
                                            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                                            return "uctt";
                                        }
                                    }
                                }
                            }
                        }
                        if (newObj.getKbType().equals(AamConstants.KB_TYPE.BD_SERVER)
                                || newObj.getKbType().equals(AamConstants.KB_TYPE.UC_SERVER)) {

                            msgExcludeUCTT = "";
                            if (!checkModuleDb()) {
                                // START Process exclude is exist, display message box
                                if (newObj.getKbType().equals(AamConstants.KB_TYPE.UC_SERVER)) {
                                    if (!isNextExclude) {
                                        RequestContext reqCtx = RequestContext.getCurrentInstance();
                                        reqCtx.execute("PF('confirmNextEclude').show()");
                                        return "uctt";
                                    }
                                } else {
                                    return "uctt";
                                }
                                // End Process exclude is exist, display message box
                            }
                            if (lstIpServer != null && lstIpServer.contains(",")) {
                                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("mop.app.bd.server.multi"), "");
                                FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                                return "uctt";
                            }
                        }
                        // Comment reson: load only module target
//                        buildImpactModules(false);
                        buildImpactModulesForOnFlow();
                        RequestContext.getCurrentInstance().execute("PF('dataTable').clearFilters()");
                    } else if (newObj.getKbGroup() != null && newObj.getKbGroup().equals(2L)) {
                        buildAutoImpactModules();
                        RequestContext.getCurrentInstance().execute("PF('dataTable').clearFilters()");
                    }

                    unitId.clear();
                    try {
                        List<Unit> units = iimService.findChildrenUnit(newObj.getCatCountryBO().getCountryCode(), newObj.getCatCountryBO().getUnitId());
                        for (Unit unit : units) {
                            unitId.add(unit.getUnitId());
                        }
                    } catch (AppException e) {
                        logger.error(e.getMessage(), e);
                    }

                    moduleActions = new ArrayList<>();
                    if (isUctt) {
                        if (newObj.getUcttType().equals(Constant.UCTT_TYPE_RESTART)) {
                            moduleActions.add(new SelectItem(AamConstants.MODULE_GROUP_ACTION.STOP_START, bundle.getString("service.mop.module.action.0")));
                            moduleActions.add(new SelectItem(AamConstants.MODULE_GROUP_ACTION.RESTART_STOP_START, bundle.getString("service.mop.module.action.3")));
                            moduleActions.add(new SelectItem(AamConstants.MODULE_GROUP_ACTION.RESTART, bundle.getString("service.mop.module.action.5")));
                        } else if (newObj.getUcttType().equals(Constant.UCTT_TYPE_SERVER_DOWN)) {
                            moduleActions.add(new SelectItem(AamConstants.MODULE_GROUP_ACTION.START, bundle.getString("service.mop.module.action.8")));
                        } else {
                            for (int i = 0; i < 10; i++) {
                                moduleActions.add(new SelectItem(i + "", bundle.getString("service.mop.module.action." + i)));
                            }
                        }
                    } else {
                        for (int i = 0; i < 10; i++) {
                            moduleActions.add(new SelectItem(i + "", bundle.getString("service.mop.module.action." + i)));
                        }
                    }
                    break;
                case "checklistapp":
                    if ("module".equals(event.getOldStep())) {
                        impactModules.clear();
                        for (Module module : normalImpactModules.values()) {
                            module.setTestbedMode(AamConstants.TESTBED_MODE.NORMAL);
                        }
                        for (Module module : testbedImpactModules.values()) {
                            module.setTestbedMode(AamConstants.TESTBED_MODE.TESTBED);
                        }
                        impactModules.putAll(normalImpactModules);
                        impactModules.putAll(testbedImpactModules);

                        Module parent = new Module();
                        this.cklDefaultRoot = new DefaultTreeNode(parent, null);
                        createTreeCklApp(cklDefaultRoot, new ArrayList<>(impactModules.values()));

                        ServiceDatabase serviceDb = new ServiceDatabase();
                        this.cklDbDefaultRoot = new DefaultTreeNode(serviceDb, null);
                        createTreeCklDb(cklDbDefaultRoot, new ArrayList<>(impactModules.values()));

                        buildListModuleSelected(new ArrayList<>(impactModules.values()));
                        actionDetailAppController.buildLstAction();

                        actionDetailDatabaseController.viewSelectItems(this);

                        verifyController.setListModules(this);
                    }
                    boolean checkIsServer = false;
                    if (newObj.getKbType() != null
                            && (newObj.getKbType().equals(AamConstants.KB_TYPE.BD_SERVER)
                            || newObj.getKbType().equals(AamConstants.KB_TYPE.UC_SERVER)
                            || newObj.getKbType().equals(AamConstants.KB_TYPE.BD_SERVER_SHUTDOWN))) {
                        checkIsServer = true;
                    }

                    if (!checkIsServer && (impactModules == null || impactModules.isEmpty())) {
                        msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("service.mop.warning.module_null"), "");
                        FacesContext.getCurrentInstance().addMessage("moduleGrowl", msg);
                        return event.getOldStep();
                    }

                    for (Module module : impactModules.values()) {
                        if (!unitId.contains(module.getUnitId())) {
                            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, module.getModuleName() + ": không thuộc thị trường " + newObj.getCatCountryBO().getCountryCode(), "");
                            FacesContext.getCurrentInstance().addMessage("moduleGrowl", msg);
                            return event.getOldStep();
                        }

                        // 20180629_hoangnd_check_null_osType
                        if (module.getOsType() != null && module.getOsType() == AamConstants.OS_TYPE.WINDOWS && !AamConstants.CODETAPTRUNG_TYPE.equals(module.getModuleTypeCode())) {
                            if (StringUtils.isEmpty(module.getViewStatus()) || StringUtils.isEmpty(module.getStatusSuccessKey()) || AamConstants.NA_VALUE.equals(module.getViewStatus()) || AamConstants.NA_VALUE.equals(module.getStatusSuccessKey())) {
                                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        module.getModuleName() + ": lệnh kiểm tra trạng thái là bắt buộc", "");
                                FacesContext.getCurrentInstance().addMessage("moduleGrowl", msg);
                                return event.getOldStep();
                            }
                        }

                        actionType = module.getActionType();
                        if (Arrays.asList(0, 2, 3, 4, 5, 6, 8).contains(actionType)) {
                            String clearCache = module.getDeleteCache();
                            if (StringUtils.isNotEmpty(clearCache) && !clearCache.equals(Constant.NA_VALUE)) {
                                if (!clearCache.endsWith("work/*") && !clearCache.endsWith("work/Catalina/*")
                                        && !clearCache.endsWith("work/Catalina/") && !clearCache.endsWith("work/Catalina")
                                        && !clearCache.endsWith("work/Catalina/localhost/*")
                                        && !clearCache.endsWith("work/Catalina/localhost/")
                                        && !clearCache.endsWith("work/Catalina/localhost")
                                        && !clearCache.endsWith("cache/*")) {
                                    msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                            module.getModuleName() + ": lệnh xóa cache không hợp lệ " + module.getDeleteCache(), "");
                                    FacesContext.getCurrentInstance().addMessage("moduleGrowl", msg);
                                    return event.getOldStep();
                                }
                            }
                        }

                        // Cac hanh dong Restart
                        if (Arrays.asList(5, 6).contains(actionType)) {
                            String restartCmd = module.getRestartService();
                            if (restartCmd == null || restartCmd.trim().isEmpty()
                                    || restartCmd.trim().toUpperCase().equals(Constant.NA_VALUE)) {
                                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        module.getModuleName() + ": " + MessageUtil.getResourceBundleMessage("command.restart.not.exist") + " " + module.getModuleName(), "");
                                FacesContext.getCurrentInstance().addMessage("moduleGrowl", msg);
                                return event.getOldStep();
                            }
                        }

                        // Cac hanh dong stop
                        if (Arrays.asList(0, 2, 9, 3, 4, 10).contains(actionType)) {
                            String stopCmd = module.getStopService();
                            if (stopCmd == null || stopCmd.trim().isEmpty() || stopCmd.trim().toUpperCase().equals(Constant.NA_VALUE)) {
                                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        module.getModuleName() + ": " + MessageUtil.getResourceBundleMessage("command.stop.app.not.exist") + " " + module.getModuleName(),
                                        "");
                                FacesContext.getCurrentInstance().addMessage("moduleGrowl", msg);
                                return event.getOldStep();
                            }
                        }

                        // Cac hanh dong start
                        if (Arrays.asList(0, 2, 8, 3, 4, 10).contains(actionType)) {
                            String startCmd = module.getStartService();
                            if (startCmd == null || startCmd.trim().isEmpty() || startCmd.trim().toUpperCase().equals(Constant.NA_VALUE)) {
                                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        module.getModuleName() + ": " + MessageUtil.getResourceBundleMessage("command.start.app.not.exist") + " " + module.getModuleName(), "");
                                FacesContext.getCurrentInstance().addMessage("moduleGrowl", msg);
                                return event.getOldStep();
                            }
                        }
                    }

                    counter++;
                    // ApplicationDetail parent = new ApplicationDetail();
                    // this.cklDefaultRoot = new DefaultTreeNode(parent, null);
                    // createTreeCklApp(cklDefaultRoot, dataTable);
                    // RequestContext.getCurrentInstance().update(":lst:edit:checklistapp");
                    break;
                case "checklistdb":
                    /*20181101_hoangnd_save all step_start*/
                    if ("checklistapp".equals(event.getOldStep())) {
                        actionDetailAppController.buildChecklistApp();
                    }
                    /*20181101_hoangnd_save all step_end*/
                    // ServiceDb serviceDb = new ServiceDb();
                    // this.cklDbDefaultRoot = new DefaultTreeNode(serviceDb, null);
                    // createTreeCklDb(cklDbDefaultRoot, dataTable);
                    // RequestContext.getCurrentInstance().update(":lst:edit:checklistdb");
                    break;
                case "app":
                    // buildListModuleSelected(dataTable);
                    // actionDetailAppController.buildLstAction();
                    // RequestContext.getCurrentInstance().update(":lst:edit:app");
                    /*20181101_hoangnd_save all step_start*/
                    if ("checklistdb".equals(event.getOldStep())) {
                        actionDetailAppController.buildChecklistDb();
                    }
                    /*20181101_hoangnd_save all step_end*/
                    if (newObj.getKbGroup() != null && newObj.getKbGroup().equals(2L)) {
                        List<ActionDetailApp> actionDetailApps = actionDetailAppController.getLstUpcode();
                        for (ActionDetailApp actionDetailApp : actionDetailApps) {
                            List<CodeChange> codes = new ArrayList<>(codeChanges.get(impactModules.get(actionDetailApp.getModuleId())));
                            CodeChange codeChange = codes.get(0);
                            actionDetailApp.setUpcodePath(codeChange.getUpcodeDir());
                            actionDetailApp.setUploadFilePath(codeChange.getZipCodeFile());
                            List<String> deleteFiles = new ArrayList<>(codeChange.getDeleteFiles());
                            deleteFiles.removeAll(codeChange.getExcludeDeleteFiles());
                            actionDetailApp.setLstFileRemove(Joiner.on(", ").join(deleteFiles));
                        }
                    }
                    break;
                case "db":
                    if ("app".equals(event.getOldStep())) {
                        //01-11-2018 KienPD validate time out start
                        try {
                            logger.info("Start get min max time out");
                            /*ActionCiCdConfig max = new ActionCiCdConfigServiceImpl().getActionCiCdConfig(Constant.TIME_OUT_MAX);
                            ActionCiCdConfig min = new ActionCiCdConfigServiceImpl().getActionCiCdConfig(Constant.TIME_OUT_MIN);
                            if (max != null && StringUtils.isNotEmpty(max.getConfigValue())){
                                actionDetailDatabaseController.setTimeOutMax(Integer.valueOf(max.getConfigValue()));
                                logger.info("Time out max : " + max.getConfigValue());
                            }
                            if (min != null && StringUtils.isNotEmpty(min.getConfigValue())){
                                actionDetailDatabaseController.setTimeOutMin(Integer.valueOf(min.getConfigValue()));
                                logger.info("Time out min : " + min.getConfigValue());
                            }*/
                            Map<String, Object> filters = new LinkedHashMap<>();
                            filters.put("id.propertyKey-EXAC", Constant.TIME_OUT_MAX);
                            List<CatConfig> max = new CatConfigServiceImpl().findList(filters);
                            filters.put("id.propertyKey-EXAC", Constant.TIME_OUT_MIN);
                            List<CatConfig> min = new CatConfigServiceImpl().findList(filters);
                            if (CollectionUtils.isNotEmpty(max) && max.get(0).getPropertyValue() != null) {
                                actionDetailDatabaseController.setTimeOutMax(Integer.valueOf(max.get(0).getPropertyValue()));
                                logger.info("Time out max : " + max.get(0).getPropertyValue());
                            }
                            if (CollectionUtils.isNotEmpty(min) && min.get(0).getPropertyValue() != null) {
                                actionDetailDatabaseController.setTimeOutMin(Integer.valueOf(min.get(0).getPropertyValue()));
                                logger.info("Time out min : " + min.get(0).getPropertyValue());
                            }
                            logger.info("End get min max time out");
                        } catch (AppException e) {
                            logger.error(e.getMessage(), e);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                        //01-11-2018 KienPD validate time out end
                        List<ActionDetailApp> detailApps = actionDetailAppController.getLstUpcode();
                        for (ActionDetailApp detailApp : detailApps) {
                            if (newObj.getMopType()==0 && detailApp.getUploadFilePath() == null) {
                                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("file.code.have.not.uploaded.yet"), "");
                                FacesContext.getCurrentInstance().addMessage("tdAppGrowl", msg);
                                return event.getOldStep();
                            }

                            if (newObj.getMopType()==0 && detailApp.getUpcodePath() == null) {
                                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("folder.up.code.have.not.selected.yet"), "");
                                FacesContext.getCurrentInstance().addMessage("tdAppGrowl", msg);
                                return event.getOldStep();
                            }
                        }

                        detailApps = actionDetailAppController.getLstUpcodeStart();
                        for (ActionDetailApp detailApp : detailApps) {
                            if (detailApp.getUploadFilePath() == null) {
                                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("file.code.have.not.uploaded.yet"), "");
                                FacesContext.getCurrentInstance().addMessage("tdAppGrowl", msg);
                                return event.getOldStep();
                            }

                            if (detailApp.getUpcodePath() == null) {
                                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("folder.up.code.have.not.selected.yet"), "");
                                FacesContext.getCurrentInstance().addMessage("tdAppGrowl", msg);
                                return event.getOldStep();
                            }
                        }
                    }
                    break;
                case "testcase":
                    List<ActionCustomGroup> customGroups = actionCustomGroupController.getCustomGroups();
                    for (ActionCustomGroup customGroup : customGroups) {
                        Set<ActionCustomAction> actions = customGroup.getActionCustomActions();
                        for (ActionCustomAction action : actions) {
                            if (action.getType().equals(0) && (action.getModuleAction() >= 100 && action.getModuleAction() <= 109)) {
                                if (StringUtils.isEmpty(action.getUpcodePath())) {
                                    msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("folder.up.code.have.not.selected.yet"), "");
                                    FacesContext.getCurrentInstance().addMessage("tdAppGrowl", msg);
                                    return event.getOldStep();
                                } else if (StringUtils.isEmpty(action.getUploadCodePath())) {
                                    msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("file.code.have.not.uploaded.yet"), "");
                                    FacesContext.getCurrentInstance().addMessage("tdAppGrowl", msg);
                                    return event.getOldStep();
                                }
                            }
                        }
                    }

                    if ("advance".equals(event.getOldStep())) {
                        boolean result = actionCustomGroupController.verifyAction();

                        if (!result) {
                            return "advance";
                        }
                    }
                    break;
                case "verify":
                    verifyController.loadVerifyDb();
                    verifyController.loadChecklist();
                    verifyController.prepareValidate();
                    break;
                case "advance":
                    actionCustomGroupController.setActionController(this);
                    actionCustomGroupController.loadAppGroup();
                    actionCustomGroupController.createConfirmGroup();
                    actionCustomGroupController.createFlow();
                    actionCustomGroupController.createFlowRollback();
                    break;
                default:
                    break;
            }

            return event.getNewStep();
        } finally {
            isNextExclude = false;
            RequestContext.getCurrentInstance().execute("PF('blockUiWizard').unblock()");
        }
    }

    public List<Service> completeService(String query) {
        List<Service> services = null;
        try {
            services = iimService.findService(newObj.getCatCountryBO().getCountryCode(), newObj.getCatCountryBO().getUnitId());
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }
        List<Service> filteredServices = new ArrayList<>();

        if (services != null)
            for (int i = 0; i < services.size(); i++) {
                Service service = services.get(i);
                if (service.getServiceName().toLowerCase().contains(query.toLowerCase()) || service.getServiceCode().toLowerCase().contains(query.toLowerCase())) {
                    filteredServices.add(service);
                }
            }

        return filteredServices;
    }

    public void changeService(SelectEvent event) {
        if (selectedService == null) {
            dualListModel = new DualListModel<>();
        } else {
            try {
                dualListModel = new DualListModel<>();
                targets = iimService.findModules(newObj.getCatCountryBO().getCountryCode(), Arrays.asList(selectedService.getServiceId()), new ArrayList<>(), new ArrayList<>(), 9l);
                dualListModel.setTarget(targets);

                baseLines = new ArrayList<>();
                if (StringUtils.isNotEmpty(selectedService.getProjectArea()) && StringUtils.isNotEmpty(selectedService.getStream())) {
                    try {
                        TeamPlatform.startup();
                        IProgressMonitor monitor = new NullProgressMonitor();
                        ITeamRepository repository = login(monitor, ibmUsername, ibmPassword);
                        baseLines = findBaseline(repository, monitor, selectedService.getProjectArea(), selectedService.getStream());
                    } finally {
                        TeamPlatform.shutdown();
                    }
                }
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            } catch (TeamRepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void buildCode() {
        logger.info("=====================");
        try {
            TeamPlatform.startup();
            IProgressMonitor monitor = new NullProgressMonitor();
            ITeamRepository repository;
            try {
                repository = login(monitor, ibmUsername, ibmPassword);

                if (selectedBaseLine == null) {
                    IWorkspaceConnection workspace = findStream(repository, monitor, selectedService.getProjectArea(), selectedService.getStream());

                    downloadTree(repository, workspace, monitor);
                } else {
                    logger.info("++++++++++++");

                    checkoutSnapshot(repository, monitor, selectedService.getProjectArea(), selectedService.getStream());
                }

//                newObj.setSourceDir("20171102075258");

                build();
            } catch (TeamRepositoryException e) {
                logger.error(e.getMessage(), e);
            }
//            baseLines = findBaseline(repository, monitor, selectedService.getProjectArea(), selectedService.getStream());
        } finally {
            TeamPlatform.shutdown();
        }
    }

    public void checkoutSnapshot(ITeamRepository repo, IProgressMonitor monitor, String projectName, String stream) throws TeamRepositoryException {
        IItemManager iItemManager = repo.itemManager();
        IWorkspaceManager wm = SCMPlatform.getWorkspaceManager(repo);

        logger.info(selectedBaseLine.getUuid());
        IItemHandle baselineHandle = IBaseline.ITEM_TYPE.createItemHandle(UUID.valueOf(selectedBaseLine.getUuid()), null);
        IItem iBaseline = iItemManager.fetchCompleteItem(baselineHandle, IItemManager.DEFAULT, monitor);
        BaselineSetImpl baselineSet = (BaselineSetImpl) ItemStore.getImmutableItem(iBaseline);

        for (Object o : baselineSet.getBaselines()) {
            IBaselineHandle iBaselineHandle = (IBaselineHandle) o;
            IItem bl = iItemManager.fetchCompleteItem(iBaselineHandle, IItemManager.DEFAULT, monitor);
            IBaseline baseline = (IBaseline) ItemStore.getImmutableItem(bl);

            IComponentHandle iComponentHandle = baseline.getComponent();
            IItem cpItem = iItemManager.fetchCompleteItem(iComponentHandle, IItemManager.DEFAULT, monitor);
            IComponent iComponent = (IComponent) ItemStore.getImmutableItem(cpItem);
            logger.info(iComponent.getName());

            IBaselineConnection iBaselineConnection = wm.getBaselineConnection(baseline, monitor);

            downloadComponentFileTree(repo, null, null, iBaselineConnection.configuration(), null, iComponent.getName() + File.separator, monitor);
        }
    }

    public void build() {
        codeChanges = HashMultimap.create();
        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_FAST);
        parameters.setIncludeRootFolder(true);

        String rootDir = AppConfig.getInstance().getProperty("checkout_code_dir") + File.separator + newObj.getSourceDir() + File.separator + selectedService.getStream();
        File rootFile = new File(rootDir);
        //20181023_tudn_start load pass security
        Map<String, String> mapConfigSecurity = SecurityService.getConfigSecurity();
        //20181023_tudn_end load pass security

        try {
            File[] components = rootFile.listFiles((FileFilter) FileFilterUtils.directoryFileFilter());
            for (File component : components) {
                File antBuild = new File(component + File.separator + "build_production.xml");
                if (antBuild.exists()) {
                    try {
                        String output = new ProcessExecutor().command("ant", "-f", antBuild.getPath())
                                .readOutput(true).execute().outputUTF8();
                        logger.info(output);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }

            Multimap<String, Module> moduleGroups = HashMultimap.create();
            for (Module module : targets) {
                moduleGroups.put(module.getGroupModuleName(), module);
            }

            Set<Long> changeCodeModuleIds = new HashSet<>();
            for (File component : components) {
                File productionCode = new File(component + File.separator + "production");
                if (productionCode.exists() && productionCode.isDirectory()) {
                    File[] codeGroups = productionCode.listFiles((FileFilter) FileFilterUtils.directoryFileFilter());

                    for (File codeGroup : codeGroups) {
                        Collection<Module> modules = moduleGroups.get(codeGroup.getName());
                        for (Module module : modules) {
                            String executePath = module.getExecutePath();
                            String codeDir = "";
                            LogOs logOs = null;
                            try {
                                List<LogOs> sourceCodePaths = iimService.findMdPath(newObj.getCatCountryBO().getCountryCode(), AamConstants.MD_PATH.SOURCE_CODE, module.getModuleId());
                                if (sourceCodePaths.size() == 1) {
                                    codeDir = sourceCodePaths.get(0).getLink();
                                    logOs = sourceCodePaths.get(0);
                                } else {
                                    continue;
                                }
                            } catch (AppException e) {
                                logger.error(e.getMessage(), e);
                            }

                            if (logOs == null)
                                continue;

                            logger.info(module.getModuleCode() + "\t" + codeGroup);
                            File tempFile = null;
                            try {
                                tempFile = File.createTempFile("aam", "dev");
                                //20181023_tudn_start load pass security
//                                FileUtils.write(tempFile, PasswordEncoderQltn.decrypt(module.getPassword()), Boolean.FALSE);
                                String passBackup = "";
                                try {
                                    passBackup = PasswordEncoderQltn.decrypt(module.getPassword());
                                } catch (Exception e) {
                                    logger.error(e.getMessage(), e);
                                    passBackup = module.getPassword();
                                }
                                ResultGetAccount resultGetAccount = SecurityService.getPassSecurity(module.getIpServer(), SessionUtil.getCurrentUsername()
                                        , module.getUsername(), null, null, null, null, module.getModuleId().toString()
                                        , passBackup, mapConfigSecurity, null);

                                if (!resultGetAccount.getResultStatus()) {
                                    MessageUtil.setErrorMessage(resultGetAccount.getResultMessage());
                                    return;
                                } else {
                                    passBackup = resultGetAccount.getResult();
                                }

                                FileUtils.write(tempFile, passBackup, Boolean.FALSE);
                                //20181023_tudn_end load pass security
                                if (StringUtils.isNotEmpty(codeDir)) {
                                    executePath += executePath.endsWith("/") ? "" : "/";
                                    codeDir = codeDir.trim();
                                    codeDir += codeDir.endsWith("/") ? "" : "/";

                                    CodeChange codeChange = new CodeChange();
                                    codeChange.setExcludeChangeFiles(new ArrayList<>());
                                    codeChange.setExcludeDeleteFiles(new ArrayList<>());

                                    if (StringUtils.isEmpty(logOs.getFileName()))
                                        codeChange.setDir(true);
                                    else
                                        codeChange.setDir(false);

                                    String diff = new ProcessExecutor().command("aamrsync", tempFile.getPath(), codeGroup.getPath() + "/" + (StringUtils.isEmpty(logOs.getFileName()) ? "" : logOs.getFileName()), module.getUsername() + "@" + module.getIpServer() + ":" + codeDir + (StringUtils.isEmpty(logOs.getFileName()) ? "" : logOs.getFileName()))
                                            .readOutput(true).execute().outputUTF8();

                                    logger.info(diff);
                                    if (diff.contains("does not exist") || diff.contains("o such file or directory") || diff.contains("rsync error:")) {
                                        //TODO
                                        logger.error(tempFile.getPath() + " " + codeGroup.getPath() + "/" + (StringUtils.isEmpty(logOs.getFileName()) ? "" : logOs.getFileName()) + " " + module.getUsername() + "@" + module.getIpServer() + ":" + codeDir + (StringUtils.isEmpty(logOs.getFileName()) ? "" : logOs.getFileName()));
                                        continue;
                                    }

                                    List<String> deleteFiles = new ArrayList<>();
                                    List<String> updateFiles = new ArrayList<>();

                                    List<String> lines = Splitter.on("\n").trimResults().splitToList(diff);
                                    for (String line : lines) {
                                        if (line.length() == 0)
                                            break;
                                        if (!line.contains("sending incremental file list") && !"./".equals(line)) {
                                            if (line.endsWith("/"))
                                                continue;
                                            if (line.startsWith("deleting ")) {
                                                deleteFiles.add(line.replaceAll("^deleting ", ""));
                                            } else {
                                                updateFiles.add(line);
                                            }
                                        }
                                    }

                                    logger.info(updateFiles);
                                    logger.info(deleteFiles);
                                    //                                updateFiles = Arrays.asList("WEB-INF/classes/com/viettel/filter/JSF2Filter.class");

                                    Multimap<Integer, String> dirLevels = HashMultimap.create();
                                    for (String updateFile : updateFiles) {
                                        dirLevels.put(StringUtils.countMatches(updateFile, "/"), updateFile);
                                    }

                                    for (String deleteFile : deleteFiles) {
                                        dirLevels.put(StringUtils.countMatches(deleteFile, "/"), deleteFile);
                                    }

                                    Integer minLevel;
                                    List<Integer> keys = new ArrayList<>(dirLevels.keySet());
                                    Collections.sort(keys);
                                    minLevel = keys.get(0);
                                    Collections.reverse(keys);
                                    String upcodeDir = null;
                                    for (Integer key : keys) {
                                        Collection<String> files = dirLevels.get(key);
                                        for (String file : files) {
                                            String parent = FilenameUtils.getPath(file);

                                            Boolean isParent = true;
                                            for (String updateFile : updateFiles) {
                                                if (!updateFile.startsWith(parent)) {
                                                    isParent = false;
                                                    continue;
                                                }
                                            }

                                            for (String deleteFile : deleteFiles) {
                                                if (!deleteFile.startsWith(parent)) {
                                                    isParent = false;
                                                    continue;
                                                }
                                            }

                                            if (isParent) {
                                                upcodeDir = parent;
                                                break;
                                            }
                                        }

                                        if (upcodeDir != null)
                                            break;
                                    }

                                    if (upcodeDir == null) {
                                        Collection<String> files = dirLevels.get(minLevel);
                                        for (int i = minLevel; i > 0; i--) {
                                            for (String file : files) {
                                                String tmpFile = file;
                                                for (int j = 0; j <= minLevel - i; j++) {
                                                    tmpFile = FilenameUtils.getPathNoEndSeparator(tmpFile);
                                                }

                                                Boolean isParent = true;
                                                for (String updateFile : updateFiles) {
                                                    if (!updateFile.startsWith(tmpFile)) {
                                                        isParent = false;
                                                        continue;
                                                    }
                                                }

                                                for (String deleteFile : deleteFiles) {
                                                    if (!deleteFile.startsWith(tmpFile)) {
                                                        isParent = false;
                                                        continue;
                                                    }
                                                }

                                                if (isParent) {
                                                    upcodeDir = tmpFile;
                                                    break;
                                                }
                                            }
                                            if (upcodeDir != null)
                                                break;
                                        }
                                    }

                                    String parentUpcodeDir = codeDir.replaceAll("^" + executePath, "").replaceAll("/$", "");

                                    String upcodePath = null;
                                    if (upcodeDir == null) {
                                        upcodePath = parentUpcodeDir;
                                    } else {
                                        upcodePath = parentUpcodeDir + File.separator + upcodeDir;
                                    }

                                    String zipFilename = Util.convertUTF8ToNoSign(module.getModuleCode() + "_" + upcodePath.replaceAll("\\.\\.", "").replaceAll("/", "_") + ".zip");

                                    String uploadFolder = UploadFileUtils.getSourceCodeFolder(newObj);

                                    String folderName = (StringUtils.isEmpty(upcodeDir) ? FilenameUtils.getName(parentUpcodeDir) : FilenameUtils.getName(upcodeDir));
                                    logger.info(uploadFolder);
                                    File rootCodeDir = new File(uploadFolder + File.separator + System.currentTimeMillis() + "_" + module.getModuleId() + File.separator + folderName);

                                    if (StringUtils.isEmpty(logOs.getFileName())) {
                                        ZipFile zipFile = new ZipFile(uploadFolder + File.separator + zipFilename);

                                        for (String updateFile : updateFiles) {
                                            if (upcodeDir == null) {
                                                File codeFile = new File(codeGroup + File.separator + updateFile);
                                                File target = new File(rootCodeDir + File.separator + updateFile);

                                                FileUtils.copyFile(codeFile, target);
                                            } else {
                                                File codeFile = new File(codeGroup + File.separator + updateFile);
                                                File target = new File(rootCodeDir + File.separator + updateFile.replaceAll("^" + upcodeDir, ""));

                                                FileUtils.copyFile(codeFile, target);
                                            }
                                        }
                                        zipFile.addFolder(rootCodeDir, parameters);
                                    } else {
                                        upcodePath += (upcodePath.endsWith("/") ? "" : "/") + logOs.getFileName();
                                        ZipFile zipFile = new ZipFile(uploadFolder + File.separator + zipFilename);

                                        for (String updateFile : updateFiles) {
                                            if (upcodeDir == null) {
                                                File codeFile = new File(codeGroup + File.separator + updateFile);
                                                FileUtils.copyFile(codeFile, new File(rootCodeDir + File.separator + updateFile));
                                                zipFile.addFile(codeFile, parameters);
                                            } else {
                                                File codeFile = new File(codeGroup + File.separator + updateFile);
                                                FileUtils.copyFile(codeFile, new File(rootCodeDir + File.separator + updateFile.replaceAll("^" + upcodeDir, "")));
                                                zipFile.addFile(codeFile, parameters);
                                            }
                                        }
                                        //                                    zipFile.addFolder(rootCodeDir, parameters);
                                    }

                                    logger.info(upcodeDir);

                                    if (rootCodeDir != null && rootCodeDir.getParentFile().exists())
                                        FileUtils.forceDelete(rootCodeDir.getParentFile());

                                    upcodePath = upcodePath.replaceAll("/$", "");
                                    List<String> newDeleteFiles = new ArrayList<>();
                                    if (deleteFiles != null) {
                                        for (String deleteFile : deleteFiles) {
                                            newDeleteFiles.add(upcodePath + "/" + deleteFile);
                                        }
                                    }

                                    codeChange.setUpcodeDir(upcodePath);
                                    codeChange.setZipCodeFile(zipFilename);
                                    codeChange.setChangeFiles(updateFiles);
                                    codeChange.setDeleteFiles(newDeleteFiles);
                                    codeChanges.put(module, codeChange);
                                    changeCodeModuleIds.add(module.getModuleId());
                                }

                            } catch (IOException e) {
                                logger.error(e.getMessage(), e);
                            } catch (InterruptedException e) {
                                logger.error(e.getMessage(), e);
                            } catch (TimeoutException e) {
                                logger.error(e.getMessage(), e);
                            } catch (net.lingala.zip4j.exception.ZipException e) {
                                logger.error(e.getMessage(), e);
                            } finally {
                                if (tempFile != null && tempFile.exists()) {
                                    try {
                                        FileUtils.forceDelete(tempFile);
                                    } catch (IOException e) {
                                        logger.error(e.getMessage(), e);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            try {
                if (changeCodeModuleIds != null && !changeCodeModuleIds.isEmpty()) {
                    List<MdDependent> mdDependents = iimService.findMdDependent(newObj.getCatCountryBO().getCountryCode(), new ArrayList<>(changeCodeModuleIds), AamConstants.MD_DEPENDENT.CODE);
                    codeChangeModules = new ArrayList<>(codeChanges.keySet());
                    codeChangeModuleFilters = new ArrayList<>(codeChanges.keySet());
                }
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }
        } finally {
            if (rootDir != null)
                try {
                    FileUtils.forceDelete(rootFile.getParentFile());
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
        }
    }

    public void excludeChangeCodeFiles(Module module) {
        selectedModule = module;
        if (codeChanges != null && codeChanges.get(module) != null) {
            List<CodeChange> tmpCodeChanges = new ArrayList<>(codeChanges.get(module));
            if (!tmpCodeChanges.isEmpty()) {
                CodeChange codeChange = tmpCodeChanges.get(0);

                dualListChangeFiles = new DualListModel<>();
                dualListRemoveFiles = new DualListModel<>();

                dualListChangeFiles.setTarget(codeChange.getChangeFiles());
                dualListChangeFiles.setSource(codeChange.getExcludeChangeFiles());

                dualListRemoveFiles.setTarget(codeChange.getDeleteFiles());
                dualListRemoveFiles.setSource(codeChange.getExcludeDeleteFiles());
            }
        }
    }

    public void saveChangeCodeFiles() {
        if (codeChanges != null && codeChanges.get(selectedModule) != null) {
            List<CodeChange> tmpCodeChanges = new ArrayList<>(codeChanges.get(selectedModule));
            if (!tmpCodeChanges.isEmpty()) {
                CodeChange codeChange = tmpCodeChanges.get(0);

                codeChange.setChangeFiles(dualListChangeFiles.getTarget());
                codeChange.setExcludeChangeFiles(dualListChangeFiles.getSource());

                codeChange.setDeleteFiles(dualListRemoveFiles.getTarget());
                codeChange.setExcludeDeleteFiles(dualListRemoveFiles.getSource());
            }
        }
    }


    private void buildImpactModulesForOnFlow() {
        sources = dualListModel.getSource();
        targets = dualListModel.getTarget();
        /*20180727_hoangnd_fix bug chon user tac dong_start*/
        targets = reloadUsernames(targets);
        /*20180727_hoangnd_fix bug chon user tac dong_end*/
        List<Long> moduleIds = new ArrayList<>();
        //anhnt_20180808_fix_start
        List<Module> moduleList = new ArrayList<>();
        if (isEdit) {
            for (Module moduleTarget : normalImpactModules.values()) {
                moduleList.add(moduleTarget);
            }
        } else {
            for (Module moduleTarget : targets) {
                moduleList.add(moduleTarget);
            }
        }

        if (moduleList != null) {
            for (Module target : moduleList) {
                if (StringUtils.isEmpty(target.getStartService()) || StringUtils.isEmpty(target.getStopService())) {
                } else {
                    moduleIds.add(target.getModuleId());
                }
            }
            //anhnt_20180808_fix_end
            Set<Long> impactModuleIds = new HashSet<>(moduleIds);
            mdDependents = new ArrayList<>();
            List<MdDependent> startRestartMdDependents = null;
            try {
                if (newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_STOP) || newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_RESTART_STOP_START)
                        || newObj.getKbType().equals(AamConstants.KB_TYPE.BD_SERVICE) || newObj.getKbType().equals(AamConstants.KB_TYPE.BD_SERVER)
                        || newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_SW_DB) || newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_SW_MODULE)) {
                    if (moduleIds.size() > 0) {
                        mdDependents = iimService.findMdDependent(newObj.getCatCountryBO().getCountryCode(), moduleIds, AamConstants.MD_DEPENDENT.STOP);
                    }
                } else if (newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_START)) {
                    if (moduleIds.size() > 0) {
                        mdDependents = iimService.findMdDependent(newObj.getCatCountryBO().getCountryCode(), moduleIds, AamConstants.MD_DEPENDENT.START);
                    }
                } else if (newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_RESTART)) {
                    if (moduleIds.size() > 0) {
                        mdDependents = iimService.findMdDependent(newObj.getCatCountryBO().getCountryCode(), moduleIds, AamConstants.MD_DEPENDENT.RESTART);
                    }
                }

                if (newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_START) || newObj.getKbType().equals(AamConstants.KB_TYPE.BD_SERVER)) {
                    if (moduleIds.size() > 0) {
                        startRestartMdDependents = iimService.findMdDependent(newObj.getCatCountryBO().getCountryCode(), moduleIds, AamConstants.MD_DEPENDENT.START_RESTART);
                    }
                }

                for (MdDependent mdDependent : mdDependents) {
                    impactModuleIds.add(mdDependent.getMdId());
                }

                logger.info(impactModuleIds);
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }

            try {
//                impactModules = new HashMap<>();
                normalImpactModules = new HashMap<>();
                List<Module> modules;
                if (impactModuleIds != null && !impactModuleIds.isEmpty()) {

                    modules = iimService.findModulesByIds(newObj.getCatCountryBO().getCountryCode(), new ArrayList<>(impactModuleIds));
                    /*20180727_hoangnd_fix bug chon user tac dong_start*/
                    modules = reloadUsernames(modules);
                    /*20180727_hoangnd_fix bug chon user tac dong_end*/
                    List<Integer> groupModuleIds = new ArrayList<>();
                    for (Module module : modules) {
                        if (newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_STOP))
                            module.setActionType(AamConstants.MODULE_GROUP_ACTION.STOP);
                        else if (newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_START))
                            module.setActionType(AamConstants.MODULE_GROUP_ACTION.START);
                        else if (newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_RESTART))
                            module.setActionType(AamConstants.MODULE_GROUP_ACTION.RESTART);
                        else if (newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_RESTART_STOP_START))
                            module.setActionType(AamConstants.MODULE_GROUP_ACTION.RESTART_STOP_START);

                        else if (newObj.getKbType().equals(AamConstants.KB_TYPE.BD_SERVICE))
                            module.setActionType(AamConstants.MODULE_GROUP_ACTION.STOP_START);
                        else if (newObj.getKbType().equals(AamConstants.KB_TYPE.BD_SERVER))
                            module.setActionType(AamConstants.MODULE_GROUP_ACTION.STOP_START);
                        else if (newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_SW_MODULE)) {
                            module.setActionType(AamConstants.MODULE_GROUP_ACTION.STOP);
                            groupModuleIds.add(module.getGroupModuleId());
                        } else if (newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_SW_DB)) {
                            if (moduleIds.contains(module.getModuleId())) {
                                if (StringUtils.isEmpty(module.getStartService()) || StringUtils.isEmpty(module.getStopService()) ||
                                        AamConstants.NA_VALUE.equals(module.getStartService()) || AamConstants.NA_VALUE.equals(module.getStopService())) {
                                    module.setActionType(AamConstants.MODULE_GROUP_ACTION.SWICH_DR);
                                } else {
                                    module.setActionType(AamConstants.MODULE_GROUP_ACTION.SWICH_DR_STOP_START);
                                }
                            } else {
                                module.setActionType(AamConstants.MODULE_GROUP_ACTION.STOP_START);
                            }
//                            groupModuleIds.add(module.getGroupModuleId());
                        } else if (newObj.getKbType().equals(AamConstants.KB_TYPE.UC_SERVER)) {
                            if (isCheckUcServer) {
                                module.setActionType(AamConstants.MODULE_GROUP_ACTION.START);
                            } else {
                                module.setActionType(AamConstants.MODULE_GROUP_ACTION.STOP_START);
                            }
                        }


//                    impactModules.put(module.getModuleId(), module);
                        normalImpactModules.put(module.getModuleId(), module);
                    }

                    if (!groupModuleIds.isEmpty()) {
                        List<Long> moduleOfflineIds = iimService.findOfflineModuleIds(newObj.getCatCountryBO().getCountryCode(), groupModuleIds, AamConstants.MODULE_FUNCTION_TYPE.BACKUP_OFFLINE);

                        List<Module> moduleOfflines = iimService.findModulesByIds(newObj.getCatCountryBO().getCountryCode(), moduleOfflineIds);

                        for (Module moduleOffline : moduleOfflines) {
                            moduleOffline.setActionType(AamConstants.MODULE_GROUP_ACTION.START);
//                        impactModules.put(moduleOffline.getModuleId(), moduleOffline);
                            normalImpactModules.put(moduleOffline.getModuleId(), moduleOffline);
                        }
                    }
                }

                if (startRestartMdDependents != null && !startRestartMdDependents.isEmpty()) {
                    List<Long> mdIds = new ArrayList<>();
                    for (MdDependent startRestartMdDependent : startRestartMdDependents) {
                        if (!normalImpactModules.keySet().contains(startRestartMdDependent.getDependentId()))
                            mdIds.add(startRestartMdDependent.getDependentId());
                    }

                    if (!mdIds.isEmpty()) {
                        List<Module> restartModules = iimService.findModulesByIds(newObj.getCatCountryBO().getCountryCode(), mdIds);
                        for (Module restartModule : restartModules) {
                            restartModule.setActionType(AamConstants.MODULE_GROUP_ACTION.RESTART);
                            restartModule.setKbGroup(2);
                            normalImpactModules.put(restartModule.getModuleId(), restartModule);
                        }
                    }
                }

//                dataTableFilters = new ArrayList<>(impactModules.values());
                dataTableFilters = new ArrayList<>(normalImpactModules.values());
                /*20180727_hoangnd_fix bug chon user tac dong_start*/
                dataTableFilters = reloadUsernames(dataTableFilters);
                /*20180727_hoangnd_fix bug chon user tac dong_end*/
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void buildImpactModules(boolean isHandleChange) {
        sources = dualListModel.getSource();
        targets = dualListModel.getTarget();
        /*20180727_hoangnd_fix bug chon user tac dong_start*/
        targets = reloadUsernames(targets);
        /*20180727_hoangnd_fix bug chon user tac dong_end*/
        List<Long> moduleIds = new ArrayList<>();
        if (targets != null && isSelectedTreeNode(root, isHandleChange)) {
            for (Module target : lstModules) {
                if (StringUtils.isEmpty(target.getStartService()) || StringUtils.isEmpty(target.getStopService())) {
                } else {
                    moduleIds.add(target.getModuleId());
                }
            }
            Set<Long> impactModuleIds = new HashSet<>(moduleIds);
            mdDependents = new ArrayList<>();
            List<MdDependent> startRestartMdDependents = null;
            try {
                if (newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_STOP) || newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_RESTART_STOP_START)
                        || newObj.getKbType().equals(AamConstants.KB_TYPE.BD_SERVICE) || newObj.getKbType().equals(AamConstants.KB_TYPE.BD_SERVER)
                        || newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_SW_DB) || newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_SW_MODULE)) {
                    if (moduleIds.size() > 0) {
                        mdDependents = iimService.findMdDependent(newObj.getCatCountryBO().getCountryCode(), moduleIds, AamConstants.MD_DEPENDENT.STOP);
                    }
                } else if (newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_START)) {
                    if (moduleIds.size() > 0) {
                        mdDependents = iimService.findMdDependent(newObj.getCatCountryBO().getCountryCode(), moduleIds, AamConstants.MD_DEPENDENT.START);
                    }
                } else if (newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_RESTART)) {
                    if (moduleIds.size() > 0) {
                        mdDependents = iimService.findMdDependent(newObj.getCatCountryBO().getCountryCode(), moduleIds, AamConstants.MD_DEPENDENT.RESTART);
                    }
                }

                if (newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_START) || newObj.getKbType().equals(AamConstants.KB_TYPE.BD_SERVER)) {
                    if (moduleIds.size() > 0) {
                        startRestartMdDependents = iimService.findMdDependent(newObj.getCatCountryBO().getCountryCode(), moduleIds, AamConstants.MD_DEPENDENT.START_RESTART);
                    }
                }

                for (MdDependent mdDependent : mdDependents) {
                    impactModuleIds.add(mdDependent.getMdId());
                }

                logger.info(impactModuleIds);
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }

            try {
//                impactModules = new HashMap<>();
                normalImpactModules = new HashMap<>();
                List<Module> modules;
                if (impactModuleIds != null && !impactModuleIds.isEmpty()) {

                    modules = iimService.findModulesByIds(newObj.getCatCountryBO().getCountryCode(), new ArrayList<>(impactModuleIds));
                    /*20180727_hoangnd_fix bug chon user tac dong_start*/
                    modules = reloadUsernames(modules);
                    /*20180727_hoangnd_fix bug chon user tac dong_end*/
                    List<Integer> groupModuleIds = new ArrayList<>();
                    for (Module module : modules) {
                        if (newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_STOP))
                            module.setActionType(AamConstants.MODULE_GROUP_ACTION.STOP);
                        else if (newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_START))
                            module.setActionType(AamConstants.MODULE_GROUP_ACTION.START);
                        else if (newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_RESTART))
                            module.setActionType(AamConstants.MODULE_GROUP_ACTION.RESTART);
                        else if (newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_RESTART_STOP_START))
                            module.setActionType(AamConstants.MODULE_GROUP_ACTION.RESTART_STOP_START);

                        else if (newObj.getKbType().equals(AamConstants.KB_TYPE.BD_SERVICE))
                            module.setActionType(AamConstants.MODULE_GROUP_ACTION.STOP_START);
                        else if (newObj.getKbType().equals(AamConstants.KB_TYPE.BD_SERVER))
                            module.setActionType(AamConstants.MODULE_GROUP_ACTION.STOP_START);
                        else if (newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_SW_MODULE)) {
                            module.setActionType(AamConstants.MODULE_GROUP_ACTION.STOP);
                            groupModuleIds.add(module.getGroupModuleId());
                        } else if (newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_SW_DB)) {
                            if (moduleIds.contains(module.getModuleId())) {
                                if (StringUtils.isEmpty(module.getStartService()) || StringUtils.isEmpty(module.getStopService()) ||
                                        AamConstants.NA_VALUE.equals(module.getStartService()) || AamConstants.NA_VALUE.equals(module.getStopService())) {
                                    module.setActionType(AamConstants.MODULE_GROUP_ACTION.SWICH_DR);
                                } else {
                                    module.setActionType(AamConstants.MODULE_GROUP_ACTION.SWICH_DR_STOP_START);
                                }
                            } else {
                                module.setActionType(AamConstants.MODULE_GROUP_ACTION.STOP_START);
                            }
//                            groupModuleIds.add(module.getGroupModuleId());
                        } else if (newObj.getKbType().equals(AamConstants.KB_TYPE.UC_SERVER)) {
                            if (isCheckUcServer) {
                                module.setActionType(AamConstants.MODULE_GROUP_ACTION.START);
                            } else {
                                module.setActionType(AamConstants.MODULE_GROUP_ACTION.STOP_START);
                            }
                        }


//                    impactModules.put(module.getModuleId(), module);
                        normalImpactModules.put(module.getModuleId(), module);
                    }

                    if (!groupModuleIds.isEmpty()) {
                        List<Long> moduleOfflineIds = iimService.findOfflineModuleIds(newObj.getCatCountryBO().getCountryCode(), groupModuleIds, AamConstants.MODULE_FUNCTION_TYPE.BACKUP_OFFLINE);

                        List<Module> moduleOfflines = iimService.findModulesByIds(newObj.getCatCountryBO().getCountryCode(), moduleOfflineIds);

                        for (Module moduleOffline : moduleOfflines) {
                            moduleOffline.setActionType(AamConstants.MODULE_GROUP_ACTION.START);
//                        impactModules.put(moduleOffline.getModuleId(), moduleOffline);
                            normalImpactModules.put(moduleOffline.getModuleId(), moduleOffline);
                        }
                    }
                }

                if (startRestartMdDependents != null && !startRestartMdDependents.isEmpty()) {
                    List<Long> mdIds = new ArrayList<>();
                    for (MdDependent startRestartMdDependent : startRestartMdDependents) {
                        if (!normalImpactModules.keySet().contains(startRestartMdDependent.getDependentId()))
                            mdIds.add(startRestartMdDependent.getDependentId());
                    }

                    if (!mdIds.isEmpty()) {
                        List<Module> restartModules = iimService.findModulesByIds(newObj.getCatCountryBO().getCountryCode(), mdIds);
                        for (Module restartModule : restartModules) {
                            restartModule.setActionType(AamConstants.MODULE_GROUP_ACTION.RESTART);
                            restartModule.setKbGroup(2);
                            normalImpactModules.put(restartModule.getModuleId(), restartModule);
                        }
                    }
                }

//                dataTableFilters = new ArrayList<>(impactModules.values());
                dataTableFilters = new ArrayList<>(normalImpactModules.values());
                /*20180727_hoangnd_fix bug chon user tac dong_start*/
                dataTableFilters = reloadUsernames(dataTableFilters);
                /*20180727_hoangnd_fix bug chon user tac dong_end*/
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void buildAutoImpactModules() {
        sources = dualListModel.getSource();
        targets = dualListModel.getTarget();

        for (Module module : targets) {
            Collection<CodeChange> codes = codeChanges.get(module);
            if (!codes.isEmpty()) {
                String uploadFolder = UploadFileUtils.getSourceCodeFolder(newObj);
                for (CodeChange code : codes) {
                    try {
                        if (!code.getExcludeChangeFiles().isEmpty()) {
                            List<FileHeader> rmFileHeaders = new ArrayList<>();
                            String upcodeDir = code.getUpcodeDir().replaceAll("/$", "");

                            ZipFile zipFile = new ZipFile(uploadFolder + File.separator + code.getZipCodeFile());
                            List<FileHeader> fileHeaders = zipFile.getFileHeaders();
                            for (FileHeader fileHeader : fileHeaders) {
                                for (String s : code.getExcludeChangeFiles()) {
                                    if (fileHeader.getFileName().equals(FilenameUtils.getName(upcodeDir) + "/" + s)) {
                                        logger.info(fileHeader.getFileName());
                                        rmFileHeaders.add(fileHeader);
                                    }
                                }
                            }

                            for (FileHeader rmFileHeader : rmFileHeaders) {
                                zipFile.removeFile(rmFileHeader);
                            }
                        }
                    } catch (net.lingala.zip4j.exception.ZipException e) {
                        logger.error(e.getMessage(), e);
                    }
                }

                if (StringUtils.isEmpty(module.getStartService()) || StringUtils.isEmpty(module.getStopService()) ||
                        AamConstants.NA_VALUE.equals(module.getStartService()) || AamConstants.NA_VALUE.equals(module.getStopService())) {
                    module.setActionType(AamConstants.MODULE_GROUP_ACTION.UPCODE);
                } else {
                    module.setActionType(AamConstants.MODULE_GROUP_ACTION.STOP_START_UPCODE);
                }
//                impactModules.put(module.getModuleId(), module);
                normalImpactModules.put(module.getModuleId(), module);
            }
        }
    }

    public void addChilds(TreeNode rootNode, Module module) {
        rootNode.getChildren().clear();

        ImpactProcess impactProcess = newObj.getImpactProcess();

        Client client = AamClientFactory.create(impactProcess.getUsername(), impactProcess.getPassword());
        String rootDir = ((TreeObject) rootNode.getData()).getObj().toString();

        WebTarget webTarget = client.target(impactProcess.getLink()).path("execute").path("lsdir").queryParam("curDir", rootDir).queryParam("countryCode", newObj.getCatCountryBO().getCountryCode()).queryParam("userTd", SessionUtil.getCurrentUsername());
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = null;
        try {
            response = builder.post(Entity.json(module));
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

        if (response == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Not connected to server", "Not connected to server");
            if (msg != null) {

                FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                RequestContext.getCurrentInstance().update("insertEditForm:designGrowl");
                RequestContext.getCurrentInstance().execute("PF('editDialogApp').hide()");
            }
            return;
        }
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            List<Map.Entry<String, Boolean>> childFiles = response.readEntity(new GenericType<List<Entry<String, Boolean>>>() {
            });

            TreeNode childNode;
            TreeObject treeObject;
            for (Map.Entry<String, Boolean> childFile : childFiles) {
                treeObject = new TreeObject(FilenameUtils.getName(childFile.getKey()), childFile.getKey());
                treeObject.setIsDir(childFile.getValue());
                childNode = new DefaultTreeNode(treeObject, rootNode);

                if (childFile.getValue()) {
                    treeObject = new TreeObject("unknown", "unknown");
                    treeObject.setIsDir(Boolean.FALSE);
                    new DefaultTreeNode(treeObject, childNode);
                }
            }
        } else {
            try {
                String message = response.readEntity(new GenericType<>(String.class));
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message);
                if (msg != null) {

                    FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                    RequestContext.getCurrentInstance().update("insertEditForm:designGrowl");
                    RequestContext.getCurrentInstance().execute("PF('editDialogApp').hide()");
                }
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    //20190416_tudn_start import rule config
    public GetFileFromServer getFileToServer(Module module, String path, String filename, Action action) {
        GetFileFromServer result = new GetFileFromServer();
        result.setOk(false);
        FileOutputStream fos = null;
        File fileOutResult = null;
        try {
            ImpactProcess impactProcess = newObj.getImpactProcess();

            Client client = AamClientFactory.create(impactProcess.getUsername(), impactProcess.getPassword());

            WebTarget webTarget = client.target(impactProcess.getLink()).path("execute").path("readFileStream").queryParam("path", path).queryParam("fileName", filename).queryParam("countryCode", newObj.getCatCountryBO().getCountryCode()).queryParam("userTd", SessionUtil.getCurrentUsername());
            Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_OCTET_STREAM);
//            Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
            Response response = builder.post(Entity.json(module));

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                byte[] content = response.readEntity(byte[].class);
                if (content == null) {
                    return result;
                } else {
                    result.setOk(true);
                    result.setInputStream(new ByteArrayInputStream(content));
                    result.setContentFile(new String(content));
                    String pathZip = UploadFileUtils.getSourceCodeFolder(action) + File.separator + "file_backup" + File.separator;
                    //tao folder tam de chua cac file ve sau
                    FileUtils.forceMkdir(new File(pathZip));
                    fos = new FileOutputStream(new File(pathZip + filename));
                    fos.write(content);
                    fos.flush();
//                    fos.close();
                    fileOutResult = new File(pathZip + filename);
                    filename = filename.substring(0, filename.lastIndexOf(".")) + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".zip";
                    ZipUtils.zipDirectory(fileOutResult, fileOutResult.getParent() + File.separator + filename);
                    result.setBackupFileName(filename);
//                    FileUtils.forceDelete(fileOutResult);
                    return result;
                }
            } else {
                try {
                    String message = response.readEntity(new GenericType<>(String.class));
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message);
                    if (msg != null) {
                        FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                        RequestContext.getCurrentInstance().update("insertEditForm:designGrowl");
                    }
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (fos != null)
                    fos.close();

                if (fileOutResult != null)
                    FileUtils.forceDelete(fileOutResult);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return result;

    }
    //20190416_tudn_end import rule config

    public String getCrState(Action action) {

        String state = "";
        if (action != null && action.getCrState() != null)
            state = bundle.getString("gnoc.cr.status." + action.getCrState());

        return state;
    }

    public String actionTypeStr(Integer actionType) {
        String result = "";
        if (actionType == null)
            actionType = 0;
        switch (actionType) {
            case 0:
                result = "Stop/Start";
                break;
            case 1:
                result = "Upcode";
                break;
            case 2:
                result = "Stop/Start + Upcode";
                break;
            case 3:
                result = "Restart(Stop/Start)";
                break;
            case 4:
                result = "Restart(Stop/Start) + Upcode";
                break;
            case 5:
                result = "Restart";
                break;
            case 6:
                result = "Restart + Upcode";
                break;
            case 7:
                result = "Checkapp";
                break;
            case 8:
                result = "Start";
                break;
            case 9:
                result = "Stop";
                break;
            case 10:
                result = "Upcode + Stop/Start(Special)";
                break;
            case 11:
                result = "Switch DB DR";
                break;
            case 12:
                result = "Switch DB DR + Stop/Start";
                break;
            default:
                break;
        }

        return result;
    }

    private void buildListModuleSelected(List<Module> dataTable) {
        listModuleSelected = new ArrayList<>();
        List<ActionDetailApp> listApp = new ArrayList<>();
        if (dataTable != null) {
            for (Module module : dataTable) {
                listModuleSelected.add(new SelectItem(module.getModuleId(), module.getModuleName()));
                listApp.add(new ActionDetailApp(null, module.getModuleId()));
            }
        }
//		actionDetailAppController.setListDetailsApp(listApp);
    }

    public void clickNode(TreeObject obj) {
        if (obj.getExtObj() != null && obj.getExtObj() instanceof QueueChecklist) {
            selectedKpiDbSetting = (QueueChecklist) obj.getExtObj();
        } else {
            selectedKpiDbSetting = new QueueChecklist();
        }
    }

    public boolean isRender(TreeObject obj) {
        if (obj.getExtObj() != null && obj.getExtObj() instanceof QueueChecklist) {
            return true;
        } else {
            return false;
        }
    }

    public Integer convertOperator(String advance) {
        return TextUtils.convertOperator(advance);
    }

    public Float criticalAlarm(String timeMonitor) {
        return TextUtils.getCriticalAlarm(timeMonitor);
    }

    public LazyDataModel<Action> getLazyDataModel() {
        return lazyDataModel;
    }

    public void setLazyDataModel(LazyDataModel<Action> lazyDataModel) {
        this.lazyDataModel = lazyDataModel;
    }

    public Action getSelectedObj() {
        return selectedObj;
    }

    public void setSelectedObj(Action selectedObj) {
        this.selectedObj = selectedObj;
    }

    public Action getNewObj() {
        return newObj;
    }

    public void setNewObj(Action newObj) {
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

    public String getSearchCrNumber() {
        return this.searchCrNumber;
    }

    public void setSearchCrNumber(String searchCrNumber) {
        this.searchCrNumber = searchCrNumber;
    }

    public Long getSearchActionType() {
        return this.searchActionType;
    }

    public void setSearchActionType(Long searchActionType) {
        this.searchActionType = searchActionType;
    }

    public String getSearchCreatedBy() {
        return this.searchCreatedBy;
    }

    public void setSearchCreatedBy(String searchCreatedBy) {
        this.searchCreatedBy = searchCreatedBy;
    }

    public Date getSearchCreatedTime() {
        return this.searchCreatedTime;
    }

    public void setSearchCreatedTime(Date searchCreatedTime) {
        this.searchCreatedTime = searchCreatedTime;
    }

    public String getSearchReason() {
        return this.searchReason;
    }

    public void setSearchReason(String searchReason) {
        this.searchReason = searchReason;
    }

    public Date getSearchBeginTime() {
        return this.searchBeginTime;
    }

    public void setSearchBeginTime(Date searchBeginTime) {
        this.searchBeginTime = searchBeginTime;
    }

    public String getSearchLocation() {
        return this.searchLocation;
    }

    public void setSearchLocation(String searchLocation) {
        this.searchLocation = searchLocation;
    }

    public Date getSearchEndTime() {
        return this.searchEndTime;
    }

    public void setSearchEndTime(Date searchEndTime) {
        this.searchEndTime = searchEndTime;
    }

    public String getSearchPerson() {
        return this.searchPerson;
    }

    public void setSearchPerson(String searchPerson) {
        this.searchPerson = searchPerson;
    }

    public ActionService getActionService() {
        return actionService;
    }

    public ActionModuleService getActionModuleService() {
        return actionModuleService;
    }

    public LazyDataModel<ActionModule> getLazyModel() {
        return lazyModel;
    }

    public void setActionModuleService(ActionModuleService actionModuleService) {
        this.actionModuleService = actionModuleService;
    }

    public void setLazyModel(LazyDataModel<ActionModule> lazyModel) {
        this.lazyModel = lazyModel;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setMapAppGroup(Map<Long, String> mapAppGroup) {
        this.mapAppGroup = mapAppGroup;
    }

    public Long getSearhUnitId() {
        return searhUnitId;
    }

    public void setSearhUnitId(Long searhUnitId) {
        this.searhUnitId = searhUnitId;
    }

    public String getSearchGroupName() {
        return searchGroupName;
    }

    public void setSearchGroupName(String searchGroupName) {
        this.searchGroupName = searchGroupName;
    }

    public String getSearchGroupCode() {
        return searchGroupCode;
    }

    public void setSearchGroupCode(String searchGroupCode) {
        this.searchGroupCode = searchGroupCode;
    }

    public String getSearchAppCode() {
        return searchAppCode;
    }

    public void setSearchAppCode(String searchAppCode) {
        this.searchAppCode = searchAppCode;
    }

    public String getSearchServerIp() {
        return searchServerIp;
    }

    public void setSearchServerIp(String searchServerIp) {
        this.searchServerIp = searchServerIp;
    }

    public String getSearchAppName() {
        return searchAppName;
    }

    public void setSearchAppName(String searchAppName) {
        this.searchAppName = searchAppName;
    }

//    public List<Service> getServices() {
//        return services;
//    }
//
//    public void setServices(List<Service> services) {
//        this.services = services;
//    }

/*
    public HashSet<Long> getHmModuleId() {
        return hmModuleId;
    }

    public void setHmModuleId(HashSet<Long> hmModuleId) {
        this.hmModuleId = hmModuleId;
    }
*/

    public DefaultTreeNode getDefaultRoot() {
        return defaultRoot;
    }

    public void setDefaultRoot(DefaultTreeNode defaultRoot) {
        this.defaultRoot = defaultRoot;
    }

    public HashMap<Long, HashMap<String, List<Module>>> getHmApp() {
        return hmApp;
    }

    public void setHmApp(HashMap<Long, HashMap<String, List<Module>>> hmApp) {
        this.hmApp = hmApp;
    }

    public TreeNode[] getListSelectedNodes() {
        return listSelectedNodes;
    }

    public void setListSelectedNodes(TreeNode[] listSelectedNodes) {
        this.listSelectedNodes = listSelectedNodes;
    }

/*
    public List<Module> getDataTable() {
        return dataTable;
    }

    public void setDataTable(List<Module> dataTable) {
        this.dataTable = dataTable;
    }
*/

    public DefaultTreeNode getCklDefaultRoot() {
        return cklDefaultRoot;
    }

    public void setCklDefaultRoot(DefaultTreeNode cklDefaultRoot) {
        this.cklDefaultRoot = cklDefaultRoot;
    }

    public TreeNode[] getCklListSelectedNodes() {
        return cklListSelectedNodes;
    }

    public void setCklListSelectedNodes(TreeNode[] cklListSelectedNodes) {
        this.cklListSelectedNodes = cklListSelectedNodes;
    }

    /*
     * public List<SelectItem> getCheckListApps() { return checkListApps; }
     *
     * public void setCheckListApps(List<SelectItem> checkListApps) {
     * this.checkListApps = checkListApps; }
     */

    /*
     * public List<Checklist> getSelectedChecklistApps() { return
     * selectedChecklistApps; }
     *
     * public void setSelectedChecklistApps(List<Checklist>
     * selectedChecklistApps) { this.selectedChecklistApps =
     * selectedChecklistApps; }
     */

    public DefaultTreeNode getCklDbDefaultRoot() {
        return cklDbDefaultRoot;
    }

    public void setCklDbDefaultRoot(DefaultTreeNode cklDbDefaultRoot) {
        this.cklDbDefaultRoot = cklDbDefaultRoot;
    }

    public TreeNode[] getCklDbListSelectedNodes() {
        return cklDbListSelectedNodes;
    }

    public void setCklDbListSelectedNodes(TreeNode[] cklDbListSelectedNodes) {
        this.cklDbListSelectedNodes = cklDbListSelectedNodes;
    }

    public List<SelectItem> getCheckListDbs() {
        return checkListDbs;
    }

    public void setCheckListDbs(List<SelectItem> checkListDbs) {
        this.checkListDbs = checkListDbs;
    }

    /*
     * public List<Checklist> getSelectedChecklistDbs() { return
     * selectedChecklistDbs; }
     *
     * public void setSelectedChecklistDbs(List<Checklist> selectedChecklistDbs)
     * { this.selectedChecklistDbs = selectedChecklistDbs; }
     */

    public List<SelectItem> getListModuleSelected() {
        return listModuleSelected;
    }

    public void setListModuleSelected(List<SelectItem> listModuleSelected) {
        this.listModuleSelected = listModuleSelected;
    }

    public List<Module> getSelectedModdules() {
        return selectedModdules;
    }

    public void setSelectedModdules(List<Module> selectedModdules) {
        this.selectedModdules = selectedModdules;
    }

    public QueueChecklist getSelectedKpiDbSetting() {
        return selectedKpiDbSetting;
    }

    public void setSelectedKpiDbSetting(QueueChecklist selectedKpiDbSetting) {
        this.selectedKpiDbSetting = selectedKpiDbSetting;
    }

    public LazyDataModel<Module> getLazyAppModel() {
        return lazyAppModel;
    }

    public void setLazyAppModel(LazyDataModel<Module> lazyAppModel) {
        this.lazyAppModel = lazyAppModel;
    }

    public List<Module> getSelectedBeforeModdules() {
        return selectedBeforeModdules;
    }

    public void setSelectedBeforeModdules(List<Module> selectedBeforeModdules) {
        this.selectedBeforeModdules = selectedBeforeModdules;
    }

    public Integer getActionType() {
        return actionType;
    }

    public void setActionType(Integer actionType) {
        this.actionType = actionType;
    }

    public String getSearchCrName() {
        return searchCrName;
    }

    public void setSearchCrName(String searchCrName) {
        this.searchCrName = searchCrName;
    }

    public boolean isViewOnly() {
        if ("quytv7".equals(username) || "anttt2".equals(username))
            return false;
        return viewOnly;
    }

    public boolean isBdUctt() {
        return newObj.getKbType() != null && newObj.getKbType() >= AamConstants.KB_TYPE.BD_SERVICE;
    }

    public void setViewOnly(boolean viewOnly) {
        this.viewOnly = viewOnly;
    }

    public List<Module> getDataTableFilters() {
        return dataTableFilters;
    }

    public void setDataTableFilters(List<Module> dataTableFilters) {
        this.dataTableFilters = dataTableFilters;
    }

    /*
     * public Checklist[] getSelectedChecklistApps() { return
     * selectedChecklistApps; }
     *
     * public void setSelectedChecklistApps(Checklist[] selectedChecklistApps) {
     * this.selectedChecklistApps = selectedChecklistApps; }
     */

    public List<Checklist> getAppKpis() {
        return appKpis;
    }

    public String getFitterAppName() {
        return fitterAppName;
    }

    public void setFitterAppName(String fitterAppName) {
        this.fitterAppName = fitterAppName;
    }

    public Long[] getFitterKpiIds() {
        return fitterKpiIds;
    }

    public void setFitterKpiIds(Long[] fitterKpiIds) {
        this.fitterKpiIds = fitterKpiIds;
    }

    public String getFitterAppIp() {
        return fitterAppIp;
    }

    public void setFitterAppIp(String fitterAppIp) {
        this.fitterAppIp = fitterAppIp;
    }

    public HashMap<Long, HashSet<Long>> getSelectAppKpiMap() {
        return selectAppKpiMap;
    }

    public Boolean getUctt() {
        return isUctt;
    }

    public void setUctt(Boolean uctt) {
        isUctt = uctt;
    }

    public List<Vof2EntityUser> getVof2EntityUsers() {
        return vof2EntityUsers;
    }

    public void setVof2EntityUsers(List<Vof2EntityUser> vof2EntityUsers) {
        this.vof2EntityUsers = vof2EntityUsers;
    }

    public Vof2EntityUser getSelectedVof2EntityUser() {
        return selectedVof2EntityUser;
    }

    public void setSelectedVof2EntityUser(Vof2EntityUser selectedVof2EntityUser) {
        this.selectedVof2EntityUser = selectedVof2EntityUser;
    }

    public String getEmailFind() {
        return emailFind;
    }

    public void setEmailFind(String emailFind) {
        this.emailFind = emailFind;
    }

    public String getPassSso() {
        return passSso;
    }

    public void setPassSso(String passSso) {
        this.passSso = passSso;
    }

    public List<Action> getKbUctts() {
        return kbUctts;
    }

    public void setKbUctts(List<Action> kbUctts) {
        this.kbUctts = kbUctts;
    }

    public Action getSelectedKb() {
        return selectedKb;
    }

    public void setSelectedKb(Action selectedKb) {
        this.selectedKb = selectedKb;
    }

    public String getReasonUctt() {
        return reasonUctt;
    }

    public void setReasonUctt(String reasonUctt) {
        this.reasonUctt = reasonUctt;
    }

    public Date getStartTimeUctt() {
        return startTimeUctt;
    }

    public void setStartTimeUctt(Date startTimeUctt) {
        this.startTimeUctt = startTimeUctt;
    }

    public Date getEndTimeUctt() {
        return endTimeUctt;
    }

    public void setEndTimeUctt(Date endTimeUctt) {
        this.endTimeUctt = endTimeUctt;
    }

    public List<SelectItem> getModuleActions() {
        return moduleActions;
    }

    public void setModuleActions(List<SelectItem> moduleActions) {
        this.moduleActions = moduleActions;
    }

    public String getUserRollback() {
        return userRollback;
    }

    public void setUserRollback(String userRollback) {
        this.userRollback = userRollback;
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    public Map<Long, Module> getImpactModules() {
        return impactModules;
    }

    public List<Module> getLstImpactModules() {
        /*List<Module> modules = new ArrayList<>();
        for (Module module : impactModules.values()) {
            if (module.getTe)
        }*/
        return new ArrayList<>(impactModules.values());
    }

    public List<Module> getLstNormalImpactModules() {
        if (targets != null && targets.size() > 0) {
            // Black text
            for (Module module : targets) {
                if (normalImpactModules.get(module.getModuleId()) != null) {
                    normalImpactModules.get(module.getModuleId()).setTypeModule("0");
                }
            }
        }
        if (sources != null && sources.size() > 0) {
            // Red text
            for (Module module : sources) {
                if (normalImpactModules.get(module.getModuleId()) != null) {
                    normalImpactModules.get(module.getModuleId()).setTypeModule("1");
                }
            }
        }
        return new ArrayList<>(normalImpactModules.values());
    }

    public List<Module> getLstExclusionModules() {
        return new ArrayList<>(exclusionModules.values());
    }

    public List<Module> getLstTestBedImpactModules() {
        return new ArrayList<>(testbedImpactModules.values());
    }

    public void setImpactModules(Map<Long, Module> impactModules) {
        this.impactModules = impactModules;
    }

    public List<SelectItem> getImpactProcesses() {
        return impactProcesses;
    }

    public void setImpactProcesses(List<SelectItem> impactProcesses) {
        this.impactProcesses = impactProcesses;
    }

//    public Service[] getSelectedServices() {
//        return selectedServices;
//    }

    public List<Database> getDatabases() {
        return databases;
    }

    public void setDatabases(List<Database> databases) {
        this.databases = databases;
    }

//    public void setSelectedServices(Service[] selectedServices) {
//        this.selectedServices = selectedServices;
//    }

    public Database[] getSelectedDatabases() {
        return selectedDatabases;
    }

    public Integer getSelectedAction() {
        return selectedAction;
    }

    public void setSelectedAction(Integer selectedAction) {
        this.selectedAction = selectedAction;
    }

    public boolean getCheckUcServer() {
        return isCheckUcServer;
    }

    public void setCheckUcServer(boolean checkUcServer) {
        isCheckUcServer = checkUcServer;
    }

    public void setSelectedDatabases(Database[] selectedDatabases) {
        this.selectedDatabases = selectedDatabases;
    }

    public DualListModel<Module> getDualListModel() {
        return dualListModel;
    }

    public void setDualListModel(DualListModel<Module> dualListModel) {
        this.dualListModel = dualListModel;
    }

    public List<Module> getSources() {
        return sources;
    }

    public void setSources(List<Module> sources) {
        this.sources = sources;
    }

    public List<Module> getTargets() {
        return targets;
    }

    public void setTargets(List<Module> targets) {
        this.targets = targets;
    }

    public Date getEndTimeWrapper() {
        if (newObj.getEndTime() == null)
            return null;

        DateTime dateTime = new DateTime(newObj.getEndTime());
        //tuanda38_20180618_start
//        dateTime = dateTime.plusHours(SessionUtil.getDiffZone());
        dateTime = dateTime.plusMinutes((int) (SessionUtil.getDiffZone() * 60));
        //tuanda38_20180618_end

        return dateTime.toDate();
    }

    public void setEndTimeWrapper(Date endTime) {
        DateTime dateTime = new DateTime(endTime);
        //tuanda38_20180618_start
//        dateTime = dateTime.minusHours(SessionUtil.getDiffZone());
        dateTime = dateTime.minusMinutes((int) (SessionUtil.getDiffZone() * 60));
        //tuanda38_20180618_end
        newObj.setEndTime(dateTime.toDate());
    }

    public Date getBeginTimeWrapper() {
        if (newObj.getBeginTime() == null)
            return null;

        DateTime dateTime = new DateTime(newObj.getBeginTime());
        //tuanda38_20180618_start
//        dateTime = dateTime.plusHours(SessionUtil.getDiffZone());
        dateTime = dateTime.plusMinutes((int) (SessionUtil.getDiffZone() * 60));
        //tuanda38_20180618_end

        return dateTime.toDate();
    }

    public void setBeginTimeWrapper(Date beginTime) {
        DateTime dateTime = new DateTime(beginTime);
        //tuanda38_20180618_start
        dateTime = dateTime.minusMinutes((int) (SessionUtil.getDiffZone() * 60));
        //tuanda38_20180618_end
        newObj.setBeginTime(dateTime.toDate());
    }

    public List<String> getIps() {
        return ips;
    }

    public void setIps(List<String> ips) {
        this.ips = ips;
    }

/*    public String[] getSelectedIps() {
        return selectedIps;
    }

    public void setSelectedIps(String[] selectedIps) {
        this.selectedIps = selectedIps;
    }*/


    public String getLstIpServer() {
        return lstIpServer;
    }

    public void setLstIpServer(String lstIpServer) {
        this.lstIpServer = lstIpServer;
    }

    public List<MdDependent> getMdDependents() {
        return mdDependents;
    }

    public Map<Long, ModuleDbDr> getModuleDbDrMap() {
        return moduleDbDrMap;
    }

    public void setModuleDbDrMap(Map<Long, ModuleDbDr> moduleDbDrMap) {
        this.moduleDbDrMap = moduleDbDrMap;
    }

    public Service getSelectedService() {
        return selectedService;
    }

    public void setSelectedService(Service selectedService) {
        this.selectedService = selectedService;
    }

    public Integer getKbGroup() {
        return kbGroup;
    }

    public void setKbGroup(Integer kbGroup) {
        this.kbGroup = kbGroup;
    }

    public Map<Long, Module> getTestbedImpactModules() {
        return testbedImpactModules;
    }

    public void setTestbedImpactModules(Map<Long, Module> testbedImpactModules) {
        this.testbedImpactModules = testbedImpactModules;
    }

    public List<Module> getTestbedDataTableFilters() {
        return testbedDataTableFilters;
    }

    public void setTestbedDataTableFilters(List<Module> testbedDataTableFilters) {
        this.testbedDataTableFilters = testbedDataTableFilters;
    }

    public BaseLine getSelectedBaseLine() {
        return selectedBaseLine;
    }

    public void setSelectedBaseLine(BaseLine selectedBaseLine) {
        this.selectedBaseLine = selectedBaseLine;
    }

    public List<BaseLine> getBaseLines() {
        return baseLines;
    }

    public void setBaseLines(List<BaseLine> baseLines) {
        this.baseLines = baseLines;
    }

    public Multimap<Module, CodeChange> getCodeChanges() {
        return codeChanges;
    }

    public void setCodeChanges(Multimap<Module, CodeChange> codeChanges) {
        this.codeChanges = codeChanges;
    }

    public String getIbmUsername() {
        return ibmUsername;
    }

    public void setIbmUsername(String ibmUsername) {
        this.ibmUsername = ibmUsername;
    }

    public String getIbmPassword() {
        return ibmPassword;
    }

    public void setIbmPassword(String ibmPassword) {
        this.ibmPassword = ibmPassword;
    }

    public List<Module> getCodeChangeModules() {
        return codeChangeModules;
    }

    public void setCodeChangeModules(List<Module> codeChangeModules) {
        this.codeChangeModules = codeChangeModules;
    }

    public List<Module> getCodeChangeModuleFilters() {
        return codeChangeModuleFilters;
    }

    public void setCodeChangeModuleFilters(List<Module> codeChangeModuleFilters) {
        this.codeChangeModuleFilters = codeChangeModuleFilters;
    }

    public List<Module> getSelectedCodeChangeModules() {
        return selectedCodeChangeModules;
    }

    public void setSelectedCodeChangeModules(List<Module> selectedCodeChangeModules) {
        this.selectedCodeChangeModules = selectedCodeChangeModules;
    }

    public DualListModel<String> getDualListChangeFiles() {
        return dualListChangeFiles;
    }

    public void setDualListChangeFiles(DualListModel<String> dualListChangeFiles) {
        this.dualListChangeFiles = dualListChangeFiles;
    }

    public DualListModel<String> getDualListRemoveFiles() {
        return dualListRemoveFiles;
    }

    public void setDualListRemoveFiles(DualListModel<String> dualListRemoveFiles) {
        this.dualListRemoveFiles = dualListRemoveFiles;
    }

    public Module getSelectedModule() {
        return selectedModule;
    }

    public void setSelectedModule(Module selectedModule) {
        this.selectedModule = selectedModule;
    }

    public ITeamRepository login(IProgressMonitor monitor, String username, String password) throws TeamRepositoryException {
        ITeamRepository repository = TeamPlatform.getTeamRepositoryService().getTeamRepository("https://10.30.9.130:9443/ccm");
        repository.registerLoginHandler((ITeamRepository.ILoginHandler) repository1 -> new ITeamRepository.ILoginHandler.ILoginInfo() {
            public String getUserId() {
                return username;
            }

            public String getPassword() {
                return password;
            }
        });
        repository.login(monitor);
        return repository;
    }

    public IWorkspaceHandle findStreamhandle(ITeamRepository repo, IProgressMonitor monitor, String projectName, String stream) throws TeamRepositoryException, ItemNotFoundException, ComponentNotInWorkspaceException {
        IWorkspaceManager wm = SCMPlatform.getWorkspaceManager(repo);
        IItemManager iItemManager = repo.itemManager();
        IWorkspaceSearchCriteria wsSearchCriteria = WorkspaceSearchCriteria.FACTORY.newInstance();

        wsSearchCriteria.setKind(IWorkspaceSearchCriteria.STREAMS);

        wsSearchCriteria.setPartialOwnerNameIgnoreCase(projectName);

        List<IWorkspaceHandle> workspaceHandles = wm.findWorkspaces(wsSearchCriteria, Integer.MAX_VALUE, monitor);


        IWorkspaceHandle iWorkspaceHandle = null;
        for (IWorkspaceHandle workspaceHandle : workspaceHandles) {
            IWorkspace iWorkspace = (IWorkspace) iItemManager.fetchCompleteItem(workspaceHandle, Integer.MAX_VALUE, monitor);
            if (stream.equalsIgnoreCase(iWorkspace.getName())) {
                iWorkspaceHandle = workspaceHandle;
            }
        }

        return iWorkspaceHandle;
    }

    public IWorkspaceConnection findStream(ITeamRepository repo, IProgressMonitor monitor, String projectName, String stream) {
        IWorkspaceManager wm = SCMPlatform.getWorkspaceManager(repo);
        IWorkspaceHandle iWorkspaceHandle;
        IWorkspaceConnection workspaceConnection = null;
        try {
            iWorkspaceHandle = findStreamhandle(repo, monitor, projectName, stream);
            workspaceConnection = wm.getWorkspaceConnection(iWorkspaceHandle, monitor);
        } catch (TeamRepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        return workspaceConnection;
    }

    public List<BaseLine> findBaseline(ITeamRepository repo, IProgressMonitor monitor, String projectName, String stream) {
        List<BaseLine> streamBaselines = new ArrayList<>();

        IWorkspaceManager wm = SCMPlatform.getWorkspaceManager(repo);
        IItemManager iItemManager = repo.itemManager();

        IWorkspaceHandle iWorkspaceHandle;
        try {
            iWorkspaceHandle = findStreamhandle(repo, monitor, projectName, stream);
            if (iWorkspaceHandle == null)
                return streamBaselines;

            IBaselineSetSearchCriteria searchCriteria = IBaselineSetSearchCriteria.FACTORY.newInstance();

            searchCriteria.setOwnerWorkspaceOptional(iWorkspaceHandle);

            List<BaselineSetHandle> baseLines = wm.findBaselineSets(searchCriteria, Integer.MAX_VALUE, null);

            for (BaselineSetHandle baseLine : baseLines) {
                IItem iBaseline = iItemManager.fetchCompleteItem(baseLine, IItemManager.DEFAULT, null);

                BaselineSetImpl baselineSet = (BaselineSetImpl) ItemStore.getImmutableItem(iBaseline);
                BaseLine streamBaseLine = new BaseLine();
                streamBaseLine.setCreatedDate(baselineSet.getCreationDate());
                streamBaseLine.setName(baselineSet.getName());
                streamBaseLine.setUuid(baselineSet.getItemId().getUuidValue());
                streamBaselines.add(streamBaseLine);
//                logger.info(baselineSet.getName() + "\t" + baselineSet.getCreationDate() + "\t" + baselineSet.getItemId());
            }
        } catch (TeamRepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        return streamBaselines;
    }

    public void downloadTree(ITeamRepository repo, IWorkspaceConnection workspace, IProgressMonitor monitor) throws TeamRepositoryException {
        List components = repo.itemManager().fetchCompleteItems(workspace.getComponents(), IItemManager.DEFAULT, null);
        for (Iterator<IComponent> it = components.iterator(); it.hasNext(); ) {
            IComponent component = it.next();
            monitor.subTask(component.getName());
            downloadComponentFileTree(repo, workspace, component, workspace.configuration(component), null, component.getName() + File.separator, monitor);
        }
    }

    private void downloadComponentFileTree(ITeamRepository repo, IWorkspaceConnection workspace,
                                           IComponent component, IConfiguration configuration, IFolderHandle parent, String indent, IProgressMonitor monitor) throws TeamRepositoryException {
        String rootDir = AppConfig.getInstance().getProperty("checkout_code_dir") + File.separator + newObj.getSourceDir() + File.separator + selectedService.getStream();
        Map<String, IVersionableHandle> children;
        if (parent == null) {
            children = configuration.childEntriesForRoot(null);
        } else {
            children = configuration.childEntries(parent, null);
        }
        if (children != null) {
            for (Map.Entry<String, IVersionableHandle> entry : children.entrySet()) {
                File file = new File(rootDir + File.separator + indent + entry.getKey());
                if (entry.getValue() instanceof IFolderHandle) {
                    if (!file.exists())
                        file.mkdirs();
                    downloadComponentFileTree(repo, workspace, component, configuration, (IFolderHandle) entry.getValue(), indent + entry.getKey() + File.separator, monitor);
                } else {
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }

                    IFileContentManager contentManager = FileSystemCore.getContentManager(repo);

                    IFileItemHandle iFileItemHandle = (IFileItemHandle) entry.getValue();
                    IFileItem fileItem = (IFileItem) SCMPlatform.getWorkspaceManager(repo).versionableManager().fetchCompleteState(iFileItemHandle, null);
                    OutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(file);
                    } catch (FileNotFoundException e) {
                        logger.error(e.getMessage(), e);
                    }
                    contentManager.retrieveContent(iFileItemHandle, fileItem.getContent(), outputStream, monitor);

                    try {
                        if (outputStream != null)
                            outputStream.close();
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * Get list data service fill tree node.
     *
     * @param selectedModule List module after filter by service/ip/db (use for case edit)
     * @param isHandleChange use for when service, ip, db on event change
     * @return TreeNode
     */
    public TreeNode initCheckboxServices(List<Module> selectedModule, boolean isHandleChange) {
        // Load data to service
        List<Service> services = new ArrayList<>();
        try {
            // Load all data when ip/DBId don't input
            if (!StringUtils.isNotEmpty(lstIpServer)
                    && (selectedDatabases == null || selectedDatabases.length == 0)) {
                // Case edit load by list module impact - anhnt2 - 07/23/2018
                boolean isLoadByModuleIds = false;
                if (isEdit && newObj.getKbType() != null
                        && (!newObj.getKbType().equals(AamConstants.KB_TYPE.BD_SERVER) && !newObj.getKbType().equals(AamConstants.KB_TYPE.UC_SERVER))) {
                    isLoadByModuleIds = true;
                }
                if (isLoadByModuleIds) {
                    List<Long> lstLongModuleId = new ArrayList<>();
                    // Get list moduleId
                    for (Module module : selectedModule) {
                        lstLongModuleId.add(module.getModuleId());
                    }
                    if (lstLongModuleId.size() > 0) {
                        dualListModel.setTarget(selectedModule);
                        services = iimService.findServicesByModules(newObj.getCatCountryBO().getCountryCode(), lstLongModuleId);
                    }
                } else {
                    // Process for case initialize
                    if (newObj.getCatCountryBO() != null) {
                        services = iimService.findService(newObj.getCatCountryBO().getCountryCode(), newObj.getCatCountryBO().getUnitId());
                    } else {
                        services = iimService.findService(AamConstants.NATION_CODE.VIETNAM, 689L);
                    }
                    // Reset Db and Ip
                    newObj.setActionDatabases(new HashSet<>());
                    newObj.setActionServers(new HashSet<>());
                }
            } else {

                // Process for event handle change
                if (isEdit) {
                    selectedModule = loadFindModule();
                }
                if (isHandleChange || isEdit) {
                    List<Long> lstLongModuleId = new ArrayList<>();
                    // Get list moduleId
                    for (Module module : selectedModule) {
                        lstLongModuleId.add(module.getModuleId());
                    }
                    // Find service by moduleId
                    if (lstLongModuleId.size() > 0) {
                        services = iimService.findServicesByModules(newObj.getCatCountryBO().getCountryCode(), lstLongModuleId);
                    }
                } else {
                    // Process for case initialize
                    if (newObj.getCatCountryBO() != null) {
                        services = iimService.findService(newObj.getCatCountryBO().getCountryCode(), newObj.getCatCountryBO().getUnitId());
                    } else {
                        services = iimService.findService(AamConstants.NATION_CODE.VIETNAM, 689L);
                    }
                }
            }

        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }
        // Fill data in tree node (Parent)
        TreeNode root = new CheckboxTreeNode(null, null);
        Module moduleEmpty = null;
        Set<Long> serviceIds = new HashSet<>();
        for (Module moduleSelected : selectedModule) {
            serviceIds.add(moduleSelected.getServiceId());
        }
        for (Service service : services) {
            Module module = new Module();
            module.setServiceId(service.getServiceId());
            module.setServiceCode(service.getServiceCode());
            module.setServiceName(service.getServiceName());
            module.setUnitId(service.getUnitId());
            // Init is parent is 0
            service.setServiceStatus(0l);
            // Create root
            TreeNode parent = new CheckboxTreeNode(module, root);
            // Create sub root (Because: need icon expand)
            TreeNode sub_parent = new CheckboxTreeNode(moduleEmpty, parent);

            // Process when edit (Expand list tree node)
            if (!serviceIds.isEmpty()) {
                // When foreach then only loadTarget() 1 time
                int iNumberLoad = 0;
                for (Long serviceId : serviceIds) {
                    if (iNumberLoad == 0) {
                        isLoadTarget = true;
                    } else {
                        isLoadTarget = false;
                    }
                    if (module.getServiceId().equals(serviceId)) {
                        expandCheckboxService(parent, true);
                    }
                    iNumberLoad++;
                }
                isLoadTarget = true;
            }
        }
        return root;
    }

    /**
     * Get value selected/input to form.
     */
    private void loadSelectedForm(Module module, List<Long> selectedServiceIds, List<Long> selectedDbIds, List<String> ipServers) {
        // Get service id is selected
        selectedServiceIds.add(module.getServiceId());

        // Get list database id is selected
        if (selectedDatabases != null) {
            for (Database selectedDatabase : selectedDatabases) {
                selectedDbIds.add(selectedDatabase.getDbId());
            }
        }
//        for (ActionDatabase actionDatabase : newObj.getActionDatabases()) {
//            selectedDbIds.add(actionDatabase.getDbId());
//        }

        // Get list server ip
        if (StringUtils.isNotEmpty(lstIpServer)) {
            List<String> ipServersSub = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(lstIpServer);
            ipServers.addAll(ipServersSub);
        }

//        for (ActionServer actionServer : newObj.getActionServers()) {
//            ipServers.add(actionServer.getIpServer());
//        }
    }

    /**
     * Load data for sub parent by serviceId.
     */
    private void loadSubParent(List<Module> lstModuleFindModules, Module module, TreeNode rootNode, boolean isSelected) {
        // Load data for group module
        Map<Set<String>, Set<String>> mapGroupModule = new HashMap<>();
        for (Module moduleFindModules : lstModuleFindModules) {
            // Màn hình ở tab [Kịch bản UCTT]: Cây dịch vụ vẫn chưa load đúng theo điều kiện tìm kiếm
            // http://10.61.2.190/mantisbt/view.php?id=44
            // if tree exits search
            if (StringUtils.isNotEmpty(keyActionSearch)) {
                // When root contain search key, load all - 07/20/2018
                boolean isRootContainsSearchKey = false;
                if ((StringUtils.isNotEmpty(moduleFindModules.getServiceName()) && moduleFindModules.getServiceName().toLowerCase().trim().contains(keyActionSearch.toLowerCase().trim()))
                        || (StringUtils.isNotEmpty(moduleFindModules.getServiceCode()) && moduleFindModules.getServiceCode().toLowerCase().trim().contains(keyActionSearch.toLowerCase().trim()))) {
                    isRootContainsSearchKey = true;
                }
                if (isRootContainsSearchKey || (StringUtils.isNotEmpty(moduleFindModules.getModuleCode()) && moduleFindModules.getModuleCode().toLowerCase().trim().contains(keyActionSearch.toLowerCase().trim()))
                        || (StringUtils.isNotEmpty(moduleFindModules.getModuleName()) && moduleFindModules.getModuleName().toLowerCase().trim().contains(keyActionSearch.toLowerCase().trim()))) {
                    Set<String> groupModuleCode = new HashSet<>();
                    Set<String> groupModuleName = new HashSet<>();
                    groupModuleCode.add(moduleFindModules.getGroupModuleCode());
                    groupModuleName.add(moduleFindModules.getGroupModuleName());
                    mapGroupModule.put(groupModuleCode, groupModuleName);
                }
            } else {
                Set<String> groupModuleCode = new HashSet<>();
                Set<String> groupModuleName = new HashSet<>();
                groupModuleCode.add(moduleFindModules.getGroupModuleCode());
                groupModuleName.add(moduleFindModules.getGroupModuleName());
                mapGroupModule.put(groupModuleCode, groupModuleName);
            }
        }

        Module moduleEmpty = null;
        for (Map.Entry<Set<String>, Set<String>> entry : mapGroupModule.entrySet()) {
            Module moduleGroupModule = new Module();
            Set<String> groupModuleCode = entry.getKey();
            Set<String> groupModuleName = entry.getValue();
            // Set again service id for case sub parent
            moduleGroupModule.setServiceId(module.getServiceId());
            moduleGroupModule.setGroupModuleCode(String.join("", groupModuleCode));
            moduleGroupModule.setGroupModuleName(String.join("", groupModuleName));
            TreeNode sub_parent = new CheckboxTreeNode(SUB_PARENT_NODE, moduleGroupModule, rootNode);
            TreeNode child = new CheckboxTreeNode(moduleEmpty, sub_parent);
            if (isSelected) {
                child.setSelected(true);
            }
        }
    }

    /**
     * Load data for child with groupModuleId.
     */
    private void loadChild(List<Module> lstModuleFindModules, Module module, TreeNode rootNode, boolean isSelected) {
        // In case search tree, reload 2 table (List module impaction, List module exclusion)  by key search
        if (StringUtils.isNotEmpty(keyActionSearch)) {
            for (Module moduleExclusion : exclusionModules.values()) {
                for (Module moduleExclusionTemp : lstModuleFindModules) {
                    if (moduleExclusion.equals(moduleExclusionTemp))
                        exclusionModulesTemp.put(moduleExclusionTemp.getModuleId(), moduleExclusionTemp);
                }
            }
        }
        boolean rootNodeSelect = rootNode.isSelected();
        // Load data for module
        HashMap<String, List<Module>> hashMapGroupModule = new HashMap<>();
        for (Module moduleFindModules : lstModuleFindModules) {
            // Màn hình ở tab [Kịch bản UCTT]: Cây dịch vụ vẫn chưa load đúng theo điều kiện tìm kiếm
            // http://10.61.2.190/mantisbt/view.php?id=44
            if (StringUtils.isNotEmpty(keyActionSearch)) {
                // When root contain search key, load all - 07/20/2018
                boolean isRootContainsSearchKey = false;
                if ((StringUtils.isNotEmpty(moduleFindModules.getServiceName()) && moduleFindModules.getServiceName().toLowerCase().trim().contains(keyActionSearch.toLowerCase().trim()))
                        || (StringUtils.isNotEmpty(moduleFindModules.getServiceCode()) && moduleFindModules.getServiceCode().toLowerCase().trim().contains(keyActionSearch.toLowerCase().trim()))) {
                    isRootContainsSearchKey = true;
                }
                // When sub parent or child
                if (isRootContainsSearchKey || (StringUtils.isNotEmpty(moduleFindModules.getModuleCode()) && moduleFindModules.getModuleCode().toLowerCase().trim().contains(keyActionSearch.toLowerCase().trim()))
                        || (StringUtils.isNotEmpty(moduleFindModules.getModuleName()) && moduleFindModules.getModuleName().toLowerCase().trim().contains(keyActionSearch.toLowerCase().trim()))) {

                    if (!hashMapGroupModule.containsKey(moduleFindModules.getGroupModuleCode())) {
                        List<Module> lstModuleSub = new ArrayList<>();
                        lstModuleSub.add(moduleFindModules);
                        hashMapGroupModule.put(moduleFindModules.getGroupModuleCode(), lstModuleSub);
                    } else {
                        hashMapGroupModule.get(moduleFindModules.getGroupModuleCode()).add(moduleFindModules);
                    }
                }
            } else {
                if (!hashMapGroupModule.containsKey(moduleFindModules.getGroupModuleCode())) {
                    List<Module> lstModuleSub = new ArrayList<>();
                    lstModuleSub.add(moduleFindModules);
                    hashMapGroupModule.put(moduleFindModules.getGroupModuleCode(), lstModuleSub);
                } else {
//                if (!moduleFindModules.getModuleId().equals(moduleFindModules.getModuleId())) {
                    hashMapGroupModule.get(moduleFindModules.getGroupModuleCode()).add(moduleFindModules);
//                }
                }
            }
        }

        // Group by groupModuleId
        for (Map.Entry<String, List<Module>> entry : hashMapGroupModule.entrySet()) {
            String groupmoduleId = entry.getKey();
            List<Module> listModule = entry.getValue();
            if (module.getGroupModuleCode().equals(groupmoduleId)
                    // Process for case group code is null
                    || (module.getGroupModuleCode().equals("null") && groupmoduleId == null)) {
                for (Module mapModule : listModule) {
                    TreeNode child = new CheckboxTreeNode(CHILD_NODE, mapModule, rootNode);
                    // Don't load check status service by target module, load with normalImpactModules (use in method loadChild)
                    if (!isFirstHandleChange) {
                        isSelected = false;
                        for (Module moduleTarget : normalImpactModules.values()) {
                            if (moduleTarget.equals(mapModule)) {
                                isSelected = true;
                                break;
                            }
                        }
                    } else {
                        // Set select for target
                        for (Module moduleTarget : targets) {
                            if (moduleTarget.equals(mapModule)) {
                                isSelected = true;
                                break;
                            }
                        }
                        // Set select for target
                        if (!rootNodeSelect) {
                            for (Module moduleSources : sources) {
                                if (moduleSources.equals(mapModule)) {
                                    isSelected = false;
                                    break;
                                }
                            }
                        }
                    }
                    if (isSelected) {
                        child.setSelected(true);
                    }
                    if (!rootNodeSelect && !isSelected) {
                        child.setSelected(false);
                    }
                    // In case search tree, reload 2 table (List module impaction, List module exclusion)  by key search
                    if (StringUtils.isNotEmpty(keyActionSearch)) {
                        if (isSelected) {
                            Module moduleChild = (Module) child.getData();
                            normalImpactModulesTemp.put(moduleChild.getModuleId(), moduleChild);
                        }
                    }

                    // Process case edit
                    if (isEdit)
                        loadSelectWhenClickEdit(mapModule, child);
                }
            }
        }
    }

    /**
     * When click edit, load status checkbox for tree node.
     */
    private void loadSelectWhenClickEdit(Module mapModule, TreeNode child) {
        if (normalImpactModules != null && normalImpactModules.size() > 0) {
            for (Iterator<Map.Entry<Long, Module>> it = normalImpactModules.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<Long, Module> entryNormalImpactModules = it.next();
                Module modueNormalImpactModules = entryNormalImpactModules.getValue();
                if (mapModule.getModuleId() != null
                        && mapModule.getModuleId().equals(modueNormalImpactModules.getModuleId())) {
                    child.setSelected(true);
                }
            }
        }
    }

    /**
     * Load data when expand groupModuleCode/Module.
     *
     * @param rootNode
     * @param isCallToInit true: case when edit, false: case when click expand
     * @return TreeNode
     */
    public TreeNode expandCheckboxService(TreeNode rootNode, Boolean isCallToInit) {
        // Check is selected
        boolean isSelected = false;
        if (rootNode.isSelected()) {
            isSelected = true;
        }
        // Check is root
        boolean isRoot = false;

        // Clear item child
        rootNode.getChildren().clear();

        // Get data module
        Module module = (Module) rootNode.getData();

        if (module.getServiceCode() != null && !module.getServiceCode().equals("")
                && !module.getServiceCode().isEmpty()) {
            isRoot = true;
        }
        List<Long> selectedServiceIds = new ArrayList<>();
        List<Long> selectedDbIds = new ArrayList<>();
        List<String> ipServers = new ArrayList<>();
        loadSelectedForm(module, selectedServiceIds, selectedDbIds, ipServers);
        Long kbType = AamConstants.KB_TYPE.BD_SERVER;
        if (newObj.getKbType() != null) {
            kbType = newObj.getKbType();
        }
        if ((selectedDatabases != null && selectedDatabases.length > 0)
                || (lstIpServer != null && !lstIpServer.isEmpty() && !lstIpServer.equals(""))) {
            // Load data for target
            loadTarget();
        }
        List<Module> lstModuleFindModules = new ArrayList<>();
        for (Module moduleTarget : dualListModel.getTarget()) {
            if (module.getServiceId().equals(moduleTarget.getServiceId())) {
                lstModuleFindModules.add(moduleTarget);
            }
        }
        for (Module moduleSource : dualListModel.getSource()) {
            if (module.getServiceId().equals(moduleSource.getServiceId())) {
                lstModuleFindModules.add(moduleSource);
            }
        }
        if (isCallToInit) {
            // For case when search by ip/db. List service only list target and list source
//                lstModuleFindModules = new ArrayList<>();
//                for (Module moduleTarget: dualListModel.getTarget()) {
//                    if (module.getServiceId().equals(moduleTarget.getServiceId())) {
//                        lstModuleFindModules.add(moduleTarget);
//                    }
//                }
//                for (Module moduleSource: dualListModel.getSource()) {
//                    if (module.getServiceId().equals(moduleSource.getServiceId())) {
//                        lstModuleFindModules.add(moduleSource);
//                    }
//                }
//                if (dualListModel != null) {
//                    lstModuleFindModules = new ArrayList<>();
//                    if (!dualListModel.getTarget().isEmpty())
//                        lstModuleFindModules.addAll(dualListModel.getTarget());
//                    if (!dualListModel.getSource().isEmpty())
//                        lstModuleFindModules.addAll(dualListModel.getSource());
//                }

            if (module.getGroupModuleCode() != null
                    && !module.getGroupModuleCode().isEmpty()
                    && !module.getGroupModuleCode().equals("")) {
                // In case search tree, reload 2 table (List module impaction, List module exclusion)  by key search
                if (StringUtils.isNotEmpty(keyActionSearch)) {
                    normalImpactModulesTemp = new HashMap<>();
                    exclusionModulesTemp = new HashMap<>();
                }
                // Load data for module (child)
                loadChild(lstModuleFindModules, module, rootNode, isSelected);

                // In case search tree, reload 2 table (List module impaction, List module exclusion)  by key search
                if (StringUtils.isNotEmpty(keyActionSearch)) {
                    normalImpactModules.putAll(normalImpactModulesTemp);
                    exclusionModules.putAll(exclusionModulesTemp);
                }
            } else {
                // Load data for group module (sub parent)
                loadSubParent(lstModuleFindModules, module, rootNode, isSelected);
                // Load data for module (child)
                List<TreeNode> childTreeNodes = rootNode.getChildren();

                // In case search tree, reload 2 table (List module impaction, List module exclusion)  by key search
                if (StringUtils.isNotEmpty(keyActionSearch)) {
                    normalImpactModulesTemp = new HashMap<>();
                    exclusionModulesTemp = new HashMap<>();
                }
                for (TreeNode childTreeNode : childTreeNodes) {
                    childTreeNode.getChildren().clear();
                    Module groupModule = (Module) childTreeNode.getData();
                    loadChild(lstModuleFindModules, groupModule, childTreeNode, isSelected);
                    childTreeNode.setExpanded(true);
                }

                // In case search tree, reload 2 table (List module impaction, List module exclusion)  by key search
                if (StringUtils.isNotEmpty(keyActionSearch)) {
                    normalImpactModules.putAll(normalImpactModulesTemp);
                    exclusionModules.putAll(exclusionModulesTemp);
                }
                if (rootNode.isSelected()) {
                    rootNode.setExpanded(true);
                }
            }
        } else {
            // Get data for case initial
            // Comment "if (lstModuleFindModules.isEmpty())" reason: when click expand don't load data
//            if (lstModuleFindModules.isEmpty()) {
            try {
                lstModuleFindModules = iimService.findModules(newObj.getCatCountryBO().getCountryCode(), selectedServiceIds, selectedDbIds, ipServers, kbType);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
//            }
            // Case sub parent
            if (isRoot) {
                // Load data for group module (sub parent)
                loadSubParent(lstModuleFindModules, module, rootNode, isSelected);
            }
            // Case child
            else {

                // In case search tree, reload 2 table (List module impaction, List module exclusion)  by key search
                if (StringUtils.isNotEmpty(keyActionSearch)) {
                    normalImpactModulesTemp = new HashMap<>();
                    exclusionModulesTemp = new HashMap<>();
                }

                // Load data for module (child)
                loadChild(lstModuleFindModules, module, rootNode, isSelected);

                // In case search tree, reload 2 table (List module impaction, List module exclusion)  by key search
                if (StringUtils.isNotEmpty(keyActionSearch)) {
                    normalImpactModules.putAll(normalImpactModulesTemp);
                    exclusionModules.putAll(exclusionModulesTemp);
                }
            }
        }
        return root;
    }

    /**
     * Load data by ip server and databaseId.
     *
     * @param isHasFilterByIpDB
     * @param moduleFindModules
     */
    private void loadHasFilterByIpDB(boolean isHasFilterByIpDB, Module moduleFindModules) {
        // Select check node when key action search is exits, only get module contain keyActionSearch. (anhnt2 - 07/23/2018)
        boolean isAdd = true;
        if (StringUtils.isNotEmpty(keyActionSearch)) {
            isAdd = false;
            if ((StringUtils.isNotEmpty(moduleFindModules.getServiceCode()) && moduleFindModules.getServiceCode().toLowerCase().trim().contains(keyActionSearch.toLowerCase().trim()))
                    || (StringUtils.isNotEmpty(moduleFindModules.getServiceName()) && moduleFindModules.getServiceName().toLowerCase().trim().contains(keyActionSearch.toLowerCase().trim()))) {
                isAdd = true;
            }
            if ((StringUtils.isNotEmpty(moduleFindModules.getGroupModuleCode()) && moduleFindModules.getGroupModuleCode().toLowerCase().trim().contains(keyActionSearch.toLowerCase().trim()))
                    || (StringUtils.isNotEmpty(moduleFindModules.getGroupModuleName()) && moduleFindModules.getGroupModuleName().toLowerCase().trim().contains(keyActionSearch.toLowerCase().trim()))) {
                isAdd = true;
            }
            if ((StringUtils.isNotEmpty(moduleFindModules.getModuleCode()) && moduleFindModules.getModuleCode().toLowerCase().trim().contains(keyActionSearch.toLowerCase().trim()))
                    || (StringUtils.isNotEmpty(moduleFindModules.getModuleName()) && moduleFindModules.getModuleName().toLowerCase().trim().contains(keyActionSearch.toLowerCase().trim()))) {
                isAdd = true;
            }
        }
        if (isHasFilterByIpDB) {
            if (dualListModel != null) {
                if (dualListModel.getTarget() != null) {
                    for (Module moduleTarget : dualListModel.getTarget()) {
                        if (moduleTarget.getModuleId().equals(moduleFindModules.getModuleId())) {
                            if (isAdd) {
                                normalImpactModules.put(moduleFindModules.getModuleId(), moduleFindModules);
                            }
                        }
                    }
                }
                if (dualListModel.getSource() != null) {
                    for (Module moduleSoruce : dualListModel.getSource()) {
                        if (moduleSoruce.getModuleId().equals(moduleFindModules.getModuleId())) {
                            if (isAdd) {
                                normalImpactModules.put(moduleFindModules.getModuleId(), moduleFindModules);
                            }
                        }
                    }
                }
            }
//            if (targets != null && !targets.isEmpty()) {
//                for (Module moduleTarget: targets) {
//                    if (moduleTarget.getModuleId().equals(moduleFindModules.getModuleId())) {
//                        normalImpactModules.put(moduleFindModules.getModuleId(), moduleFindModules);
//                    }
//                }
//            }
        } else {
            if (isAdd) {
                normalImpactModules.put(moduleFindModules.getModuleId(), moduleFindModules);
            }
        }
    }

    /**
     * Event on node select.
     *
     * @param event
     */
    public void onNodeSelect(NodeSelectEvent event) {
        try {
            handleChangeToTrue();
            TreeNode treeNode = event.getTreeNode();
            // Check is root
            boolean isModule = false;

            // Get data module
            Module module = (Module) treeNode.getData();
            if (module.getModuleId() != null) {
                isModule = true;
            }
            if (normalImpactModules == null) {
                normalImpactModules = new HashMap<>();
            }
            if (isModule) {
                List<Module> lstModule = new ArrayList<>();
                lstModule.add(module);
                normalImpactModules.put(module.getModuleId(), module);
                //            dualListModel.setSource(lstModule);
                //            dualListModel.setTarget(lstModule);
                //            buildImpactModules();
            } else {
                // Check is root
                boolean isRoot = false;

                if (module.getServiceCode() != null && !module.getServiceCode().equals("")
                        && !module.getServiceCode().isEmpty()) {
                    isRoot = true;
                }
                List<Long> selectedServiceIds = new ArrayList<>();
                List<Long> selectedDbIds = new ArrayList<>();
                List<String> ipServers = new ArrayList<>();
                loadSelectedForm(module, selectedServiceIds, selectedDbIds, ipServers);
                Long kbType = AamConstants.KB_TYPE.BD_SERVER;
                if (newObj.getKbType() != null) {
                    kbType = newObj.getKbType();
                }
                List<Module> lstModuleFindModules = iimService.findModules(newObj.getCatCountryBO().getCountryCode(), selectedServiceIds, selectedDbIds, ipServers, kbType);
                // Process for case filter by ip/DB
                boolean isHasFilterByIpDB = false;
                if ((selectedDatabases != null && selectedDatabases.length > 0)
                        || (lstIpServer != null && !lstIpServer.isEmpty() && !lstIpServer.equals(""))) {
                    isHasFilterByIpDB = true;
                    // Load data for target
                    loadTarget();
                }
                if (isRoot) {
                    for (Module moduleFindModules : lstModuleFindModules) {
                        loadHasFilterByIpDB(isHasFilterByIpDB, moduleFindModules);
                    }

                    // START When only the parent is selected, the data of the module will not be selected, because the data only loads when expanded --> When the parent is selected then all child of parent will automatically expanded.
                    // Expand of parent
                    treeNode.setExpanded(true);
                    expandCheckboxService(treeNode, false);
                    if (treeNode.getChildren() != null) {
                        List<TreeNode> lstTreeNode = treeNode.getChildren();
                        // When foreach then only loadTarget() 1 time
                        int iNumberLoad = 0;
                        for (TreeNode nodeSub : lstTreeNode) {
                            if (iNumberLoad == 0) {
                                isLoadTarget = true;
                            } else {
                                isLoadTarget = false;
                            }
                            iNumberLoad++;
                            // Expand of sub parent
                            nodeSub.setExpanded(true);
                            expandCheckboxService(nodeSub, false);
                        }
                        isLoadTarget = true;
                    }
                    // START When only the parent is selected, the data of the module will not be selected, because the data only loads when expanded --> When the parent is selected then all child of parent will automatically expanded.
                } else {
                    for (Module moduleFindModules : lstModuleFindModules) {
                        if (moduleFindModules.getGroupModuleCode().equals(module.getGroupModuleCode())) {
                            // If filter by ip server/databaseId
                            loadHasFilterByIpDB(isHasFilterByIpDB, moduleFindModules);
                        }
                    }
                    // START When only the parent is selected, the data of the module will not be selected, because the data only loads when expanded --> When the parent is selected then all child of parent will automatically expanded.
                    // Expand of sub parent
                    treeNode.setExpanded(true);
                    expandCheckboxService(treeNode, false);
                    // START When only the parent is selected, the data of the module will not be selected, because the data only loads when expanded --> When the parent is selected then all child of parent will automatically expanded.

                }
            }
            List<Module> lstModule = new ArrayList<>();
            for (Iterator<Map.Entry<Long, Module>> it = normalImpactModules.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<Long, Module> entry = it.next();
                lstModule.add(entry.getValue());
            }
            //lstModules.addAll(lstModule);
            dualListModel.setTarget(lstModule);
            //        getLstNormalImpactModules();
            //        buildImpactModules();
            //RequestContext.getCurrentInstance().execute("PF('dataTable').clearFilters()");
            //RequestContext.getCurrentInstance().execute("PF('blockUiWizard').unblock()");
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Event remove item when unselect.
     *
     * @param event
     */
    public void onNodeUnselect(NodeUnselectEvent event) {
        handleChangeToTrue();
        TreeNode treeNode = event.getTreeNode();

        // Get data module
        Module module = (Module) treeNode.getData();
        if (normalImpactModules != null && normalImpactModules.size() > 0) {
            for (Iterator<Map.Entry<Long, Module>> it = normalImpactModules.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<Long, Module> entry = it.next();
                if (module.getModuleId() != null) {
                    // Remove for case moduleId
                    if (entry.getKey().equals(module.getModuleId())) {
                        treeNode.setSelected(false);
                        it.remove();
                        if (dualListModel != null && dualListModel.getTarget() != null) {
                            dualListModel.getTarget().remove(module);
                        }
                    }
                } else if (module.getServiceCode() != null && !module.getServiceCode().equals("")
                        && !module.getServiceCode().isEmpty()) {
                    // Remove for case serviceId
                    Module mapModule = entry.getValue();
                    if (mapModule.getServiceCode().equals(module.getServiceCode())) {
                        treeNode.setSelected(false);
                        it.remove();
                        if (dualListModel != null && dualListModel.getTarget() != null) {
                            dualListModel.getTarget().remove(mapModule);
                        }
                    }
                } else {
                    // Remove for case modelGroup
                    Module mapModule = entry.getValue();
                    if (mapModule.getGroupModuleCode() != null && mapModule.getGroupModuleCode().equals(module.getGroupModuleCode())) {
                        treeNode.setSelected(false);
                        it.remove();
                        if (dualListModel != null && dualListModel.getTarget() != null) {
                            dualListModel.getTarget().remove(mapModule);
                        }
                    }
                }
            }
        }
        // Don't load check status service by target module, load with normalImpactModules (use in method loadChild)
        isFirstHandleChange = false;
    }

    /**
     * Event on node expand.
     *
     * @param event
     */
    public void onNodeExpand(NodeExpandEvent event) {
        boolean isCallToInit = false;
        // If has ip/dbId
        if ((selectedDatabases != null && selectedDatabases.length > 0)
                || (lstIpServer != null && !lstIpServer.isEmpty() && !lstIpServer.equals(""))) {
            isCallToInit = true;
        }
        root = expandCheckboxService(event.getTreeNode(), isCallToInit);

        // Don't load check status service by target module, load with normalImpactModules (use in method loadChild)
        isFirstHandleChange = false;
    }

    /**
     * Check in tree node exist element is selected.
     *
     * @param treeNode
     */
    private boolean isSelectedTreeNode(TreeNode treeNode, boolean isHandleChange) {
        // Edit reason: list service when click next button to ModuleTD incorrect
        lstModules = new ArrayList<>();
        if (treeNode != null) {
            List<TreeNode> lstTreeNodeParent = treeNode.getChildren();
            if (lstTreeNodeParent != null && lstTreeNodeParent.size() > 0) {
                for (TreeNode treeNodeParent : lstTreeNodeParent) {
                    boolean isSelectParent = false;
                    if (treeNodeParent.isSelected()) {
                        isSelectParent = true;
//                        Module module = (Module) treeNodeParent.getData();
//                        lstModules.add(module);
                    }
                    List<TreeNode> lstTreeNodeChild = treeNodeParent.getChildren();
                    if (lstTreeNodeChild != null && lstTreeNodeChild.size() > 0) {
                        for (TreeNode treeNodeChild : lstTreeNodeChild) {
                            boolean isSelectChild = false;
                            if (treeNodeChild.isSelected()) {
                                isSelectChild = true;
//                                Module module = (Module) treeNodeChild.getData();
//                                lstModules.add(module);
                            }

                            List<TreeNode> lstTreeNodeChildSub = treeNodeChild.getChildren();
                            if (lstTreeNodeChildSub != null && lstTreeNodeChildSub.size() > 0) {
                                for (TreeNode treeNodeChildSub : lstTreeNodeChildSub) {
                                    if (isSelectParent || isSelectChild || treeNodeChildSub.isSelected()) {
                                        Module module = (Module) treeNodeChildSub.getData();
                                        // If event is handle change then process exclude module
                                        if (isHandleChange) {
                                            // Check is exclude module then don't select
                                            boolean isExclude = false;
                                            if (module != null) {
                                                // check list exclude is exist
                                                if (sources != null && sources.size() > 0) {
                                                    for (Module moduleSources : sources) {
                                                        if (moduleSources.equals(module)) {
                                                            isExclude = true;
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                            // Add module when not exclude
                                            if (!isExclude) {
                                                lstModules.add(module);
                                            }
                                        } else {
                                            if (module != null) {
                                                lstModules.add(module);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
        if (lstModules.size() > 0)
            return true;
//        if (treeNode != null) {
//            if (treeNode.isSelected()) {
//                return true;
//            }
//            List<TreeNode> lstTreeNode = treeNode.getChildren();
//            if (lstTreeNode != null && lstTreeNode.size() > 0) {
//                for (TreeNode treeNodeChild: lstTreeNode) {
//                    if (treeNodeChild.isSelected()) {
//                        return true;
//                    }
//                    boolean isSelectedChild = isSelectedTreeNode(treeNodeChild);
//                    if (isSelectedChild)
//                        return true;
//                }
//
//            }
//        }
        return false;
    }

    /**
     * If confirm next exclude is "yes" -> continue flow process.
     */
    public void changeNextExclude() {
        isNextExclude = true;
        RequestContext reqCtx = RequestContext.getCurrentInstance();
        reqCtx.execute("PF('editwizard').next()");
    }

    /**
     * Filter on tree node.
     */
    public void searchActionNode() {
        try {
            if (StringUtils.isNotEmpty(keyActionSearch)) {
                List<Long> databaseIds = new ArrayList<>();
                if (selectedDatabases != null
                        && (newObj.getKbType().equals(AamConstants.KB_TYPE.UC_SERVER)
                        || newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_STOP)
                        || newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_START)
                        || newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_RESTART_STOP_START)
                        || newObj.getKbType().equals(AamConstants.KB_TYPE.UCTT_RESTART))) {
                    for (Database selectedDatabase : selectedDatabases) {
                        databaseIds.add(selectedDatabase.getDbId());
                    }
                }
                Long unitId = newObj.getCatCountryBO().getUnitId() == null ? 0 : newObj.getCatCountryBO().getUnitId();

                List<Service> services = iimService.findFilterModule(newObj.getCatCountryBO().getCountryCode(), keyActionSearch.trim().toLowerCase(), unitId, lstIpServer, databaseIds);
                // clear list impact module and list exclusion module
                if (services == null || services.size() == 0) {
                    normalImpactModules = new HashMap<>();
                    exclusionModules = new HashMap<>();
                } else {
                    // Load data for target
                    loadTarget();
                    for (Module module : dualListModel.getTarget()) {
                        normalImpactModules.put(module.getModuleId(), module);
                    }
                    for (Module module : dualListModel.getSource()) {
                        exclusionModules.put(module.getModuleId(), module);
                    }
                }
                // Fill data in tree node (Parent)
                root = new CheckboxTreeNode(null, null);
                Module moduleEmpty = null;
                for (Service service : services) {
                    Module module = new Module();
                    module.setServiceId(service.getServiceId());
                    module.setServiceCode(service.getServiceCode());
                    module.setServiceName(service.getServiceName());
                    module.setUnitId(service.getUnitId());
                    // Init is parent is 0
                    service.setServiceStatus(0l);
                    // Create root
                    TreeNode parent = new CheckboxTreeNode(module, root);
                    expandCheckboxService(parent, true);
                    // Create sub root (Because: need icon expand)
                    TreeNode sub_parent = new CheckboxTreeNode(moduleEmpty, parent);

                }
                // For update data table
                buildImpactModules(true);
            } else {
                handleChange();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void getCountryFromProcessImpact(Action action) {
        if (action != null && action.getCatCountryBO() == null && action.getImpactProcess() != null && action.getImpactProcess().getNationCode() != null) {
            try {
                action.setCatCountryBO(new CatCountryServiceImpl().findById(action.getImpactProcess().getNationCode()));
                if (action.getCatCountryBO() == null) {
                    action.setCatCountryBO(new CatCountryServiceImpl().findById("VNM"));
                }
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        } else {
            try {
                action.setCatCountryBO(new CatCountryServiceImpl().findById("VNM"));
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    //hoangnd_start
    public void validCommon() {

        if (newObj == null || newObj.getCatCountryBO() == null) {
            MessageUtil.setErrorMessageFromRes("error.config.action.company.empty");
            return;
        }
    }

    //hoangnd_end
    public void handleChangeToTrue() {
        handleChange = true;
    }

    // Process for Primefaces columToggler losing effect on filters
    public void onToggleServices(ToggleEvent e) {
        this.columServices.set((Integer) e.getData(), e.getVisibility() == Visibility.VISIBLE);
    }

    // Process for Primefaces columToggler losing effect on filters
    public void onToggleServicesExclusion(ToggleEvent e) {
        this.columServicesExclusion.set((Integer) e.getData(), e.getVisibility() == Visibility.VISIBLE);
    }

    public List<Boolean> getColumServicesExclusion() {
        return columServicesExclusion;
    }

    public void setColumServicesExclusion(List<Boolean> columServicesExclusion) {
        this.columServicesExclusion = columServicesExclusion;
    }

    public List<Boolean> getColumServices() {
        return columServices;
    }

    public void setColumServices(List<Boolean> columServices) {
        this.columServices = columServices;
    }

    public TreeNode getRoot() {
        return root;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }

    public TreeNode[] getSelectedNodes() {
        return selectedNodes;
    }

    public void setSelectedNodes(TreeNode[] selectedNodes) {
        this.selectedNodes = selectedNodes;
    }

    public boolean isReqParamater() {
        return reqParamater;
    }

    public void setReqParamater(boolean reqParamater) {
        this.reqParamater = reqParamater;
    }

    public String getMsgExcludeUCTT() {
        return msgExcludeUCTT;
    }

    public void setMsgExcludeUCTT(String msgExcludeUCTT) {
        this.msgExcludeUCTT = msgExcludeUCTT;
    }

    public String getKeyActionSearch() {
        return keyActionSearch;
    }

    public void setKeyActionSearch(String keyActionSearch) {
        this.keyActionSearch = keyActionSearch;
    }

    public String getTabViewActiveIndex() {
        return tabViewActiveIndex;
    }

    public void setTabViewActiveIndex(String tabViewActiveIndex) {
        this.tabViewActiveIndex = tabViewActiveIndex;
    }

    public boolean isHandleChange() {
        return handleChange;
    }

    public void setHandleChange(boolean handleChange) {
        this.handleChange = handleChange;
    }

    public boolean isLoadTarget() {
        return isLoadTarget;
    }

    public void setLoadTarget(boolean loadTarget) {
        isLoadTarget = loadTarget;
    }

    /*20180702_hoangnd_cau_hinh_user_tac_dong_start*/
    public List<OsAccount> getLstOsAccounts() {
        return lstOsAccounts;
    }

    public void setLstOsAccounts(List<OsAccount> lstOsAccounts) {
        this.lstOsAccounts = lstOsAccounts;
    }

    /*20180702_hoangnd_cau_hinh_user_tac_dong_end*/
    /*20181023_hoangnd_approval impact step_start*/
    public String getExeImpactStep() {
        return exeImpactStep;
    }

    public void setExeImpactStep(String exeImpactStep) {
        this.exeImpactStep = exeImpactStep;
    }

    public String getReasonImpactStep() {
        return reasonImpactStep;
    }

    public void setReasonImpactStep(String reasonImpactStep) {
        this.reasonImpactStep = reasonImpactStep;
    }
    /*20181023_hoangnd_approval impact step_end*/

    public List<SelectItem> getCatCountrys() {
        return catCountrys;
    }

    public void setCatCountrys(List<SelectItem> catCountrys) {
        this.catCountrys = catCountrys;
    }

    List<Boolean> columnVisible = new ArrayList<>();

    public void onToggler(ToggleEvent e) {
        this.columnVisible.set((Integer) e.getData(), e.getVisibility() == Visibility.VISIBLE);
    }

    public void setColumnVisible(List<Boolean> columnVisible) {
        this.columnVisible = columnVisible;
    }

    public List<Boolean> getColumnVisible() {
        return columnVisible;
    }

    public List<SelectItem> getServiceNames() {
        return serviceNames;
    }

}
