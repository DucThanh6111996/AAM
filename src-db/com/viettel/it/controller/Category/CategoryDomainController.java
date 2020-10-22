package com.viettel.it.controller.Category;


import com.viettel.it.lazy.LazyDataModelBaseNew;
import com.viettel.it.model.CategoryDomain;
import com.viettel.it.model.CategoryGroupDomain;
import com.viettel.it.persistence.Category.CategoryDomainServiceImpl;
import com.viettel.it.persistence.Category.CategoryGroupDomainServiceImpl;
import com.viettel.it.util.Config;
import com.viettel.it.util.LogUtils;
import com.viettel.it.util.MessageUtil;
import com.viettel.util.SessionUtil;
import com.viettel.util.SessionWrapper;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.primefaces.context.RequestContext;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.StreamedContent;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * Created by quytv7 on 1/17/2018.
 */
@ViewScoped
@ManagedBean
public class CategoryDomainController {
    //<editor-fold defaultstate="collapsed" desc="Param">
    protected static final Logger logger = Logger.getLogger(CategoryDomainController.class);
    private LazyDataModel<CategoryDomain> lazyDataModel;
    private CategoryDomainServiceImpl categoryDomainService;
    private String logAction = "";
    private String className = CategoryDomainController.class.getName();
    private CategoryDomain categoryDomain;
    private List<CategoryDomain> categoryDomains;
    private StreamedContent resultImport;
    private static String patternDate = "dd/MM/yyyy HH:mm:ss";
    private List<CategoryGroupDomain> categoryGroupDomains;
    private boolean isUpdate = false;

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Set&Get">

    public boolean isUpdate() {
        return isUpdate;
    }

    public void setUpdate(boolean update) {
        isUpdate = update;
    }

    public List<CategoryGroupDomain> getCategoryGroupDomains() {
        return categoryGroupDomains;
    }

    public void setCategoryGroupDomains(List<CategoryGroupDomain> categoryGroupDomains) {
        this.categoryGroupDomains = categoryGroupDomains;
    }

    public LazyDataModel<CategoryDomain> getLazyDataModel() {
        return lazyDataModel;
    }

    public void setLazyDataModel(LazyDataModel<CategoryDomain> lazyDataModel) {
        this.lazyDataModel = lazyDataModel;
    }

    public CategoryDomainServiceImpl getCategoryDomainService() {
        return categoryDomainService;
    }

    public void setCategoryDomainService(CategoryDomainServiceImpl categoryDomainService) {
        this.categoryDomainService = categoryDomainService;
    }

    public String getLogAction() {
        return logAction;
    }

    public void setLogAction(String logAction) {
        this.logAction = logAction;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public CategoryDomain getCategoryDomain() {
        return categoryDomain;
    }

    public void setCategoryDomain(CategoryDomain categoryDomain) {
        this.categoryDomain = categoryDomain;
    }

    public List<CategoryDomain> getCategoryDomains() {
        return categoryDomains;
    }

    public void setCategoryDomains(List<CategoryDomain> categoryDomains) {
        this.categoryDomains = categoryDomains;
    }

    public StreamedContent getResultImport() {
        return resultImport;
    }

    public void setResultImport(StreamedContent resultImport) {
        this.resultImport = resultImport;
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Main">
    @PostConstruct
    public void onStart() {
        try {
            categoryDomainService = new CategoryDomainServiceImpl();
            LinkedHashMap<String, String> orders = new LinkedHashMap<>();
            orders.put("systemType", "ASC");
            orders.put("groupDomain.groupName", "ASC");
            orders.put("updateTime", "DESC");

            lazyDataModel = new LazyDataModelBaseNew<>(categoryDomainService, null, orders);
            logAction = LogUtils.addContent("", "Login Function");
            LinkedHashMap<String, String> order = new LinkedHashMap<>();
            order.put("groupName", "ASC");
            HashMap<String, Object> filters = new HashMap<>();
            filters.put("systemType", MessageUtil.getResourceBundleMessage("label.category.domain.system.type.nocpro"));
            categoryGroupDomains = new CategoryGroupDomainServiceImpl().findList(filters, order);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        LogUtils.writelog(new Date(), className, new Object() {
        }.getClass().getEnclosingMethod().getName(), LogUtils.ActionType.VIEW.name(), logAction);
    }

    public void preAddCategory() {
        try {
            isUpdate = false;
            categoryDomain = new CategoryDomain();
            LinkedHashMap<String, String> orders = new LinkedHashMap<>();
            orders.put("groupName", "ASC");
            HashMap<String, Object> filters = new HashMap<>();
            filters.put("systemType", MessageUtil.getResourceBundleMessage("label.category.domain.system.type.nocpro"));
            categoryGroupDomains = new CategoryGroupDomainServiceImpl().findList(filters, orders);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public void preUpdateCategory(CategoryDomain categoryDomainOld) {
        isUpdate = true;
        categoryDomain = categoryDomainOld;
        afterSystemTypeSelected();
    }
    public void preDeleteCategory() {
        if (categoryDomains != null && categoryDomains.size() > 0) {
            RequestContext.getCurrentInstance().execute("PF('comfirmDeleteCategory').show()");
        } else {
            MessageUtil.setInfoMessageFromRes("label.form.choose.one");
        }
    }
    public void afterSystemTypeSelected() {
        try {
            if (!isNullOrEmpty(categoryDomain.getSystemType())) {
                LinkedHashMap<String, String> orders = new LinkedHashMap<>();
                orders.put("groupName", "ASC");
                HashMap<String, Object> filters = new HashMap<>();
                filters.put("systemType", categoryDomain.getSystemType().toLowerCase());
                categoryGroupDomains = new CategoryGroupDomainServiceImpl().findList(filters, orders);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public void onSaveOrUpdateCategory() {
        try {
            boolean checkError = false;
            if (isNullOrEmpty(categoryDomain.getSystemType())) {
                checkError = true;
                MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"), MessageUtil.getResourceBundleMessage("label.category.domain.system.type")));
            }
            if (categoryDomain.getGroupDomain() == null) {
                checkError = true;
                MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"), MessageUtil.getResourceBundleMessage("label.category.domain.groupName")));
            }
            if (isNullOrEmpty(categoryDomain.getDomainCode())) {
                checkError = true;
                MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"), MessageUtil.getResourceBundleMessage("label.category.domain.domainCode")));
            }else{
                if(categoryDomain.getDomainCode().length() > 200){
                    checkError = true;
                    MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.maxLength"), MessageUtil.getResourceBundleMessage("label.category.domain.domainCode"), "200"));
                }
            }
            if (checkError) {
                return;
            } else {
                Date startTime = new Date();
                categoryDomain.setUpdateTime(new Date());
                categoryDomain.setCreateUser(SessionWrapper.getCurrentUsername());
                if (categoryDomain.getId() == null) {
                    HashMap<String, Object> filters = new HashMap<>();
                    filters.put("systemType", categoryDomain.getSystemType().toLowerCase());
                    filters.put("groupDomain.id", categoryDomain.getGroupDomain().getId());
                    filters.put("domainCode", categoryDomain.getDomainCode().toLowerCase());
                    List<CategoryDomain> listExist = categoryDomainService.findList(filters);
                    if (listExist.size() > 0) {
                        MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.exist"), MessageUtil.getResourceBundleMessage("label.category.domain.domainCode")));
                        return;
                    }
                }

                categoryDomainService.saveOrUpdate(categoryDomain);
                //20180620_tudn_start ghi log DB
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), CategoryDomainController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            LogUtils.ActionType.UPDATE,
                            categoryDomain.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                //20180620_tudn_end ghi log DB
                MessageUtil.setInfoMessageFromRes("info.save.success");
                RequestContext.getCurrentInstance().execute("PF('addCategory').hide()");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            MessageUtil.setErrorMessageFromRes("error.save.unsuccess");
        }
    }
    public void deleteCategory() {
        try {
            Date startTime = new Date();
            if (categoryDomains != null && categoryDomains.size() > 0) {
                categoryDomainService.delete(categoryDomains);
            }
            //20180620_tudn_start ghi log DB
            try {
                LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                        LogUtils.getRemoteIpClient(), LogUtils.getUrl(), CategoryDomainController.class.getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(),
                        LogUtils.ActionType.DELETE,
                        categoryDomains.toString(), LogUtils.getRequestSessionId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            //20180620_tudn_end ghi log DB
            MessageUtil.setInfoMessageFromRes("label.action.delelteOk");
            RequestContext.getCurrentInstance().execute("PF('comfirmDeleteCategory').hide()");
            if (categoryDomains != null) {
                categoryDomains.clear();
            }
        } catch (Exception ex) {
            MessageUtil.setInfoMessageFromRes("label.action.deleteFail");
            logger.error(ex.getMessage(), ex);
        }
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Import">
   /* public void onImport(FileUploadEvent event) {
        List<String> listNodeCode = new ArrayList<>();
        List<CategoryStationFestival> dataInput = new ArrayList<>();
        List<CategoryStationFestival> dataResult = new ArrayList<>();
        Workbook wb = null;
        boolean checkImport = true;
        try {
            wb = WorkbookFactory.create(event.getFile().getInputstream());
            Sheet sheet = wb.getSheetAt(0);
            Row rowHeader = sheet.getRow(4);

            boolean check = checkHeader(rowHeader);
            int rowNum = sheet.getLastRowNum();
            CategoryStationFestival categoryStationFestival_;
            if (check) {
                for (int i = 5; i <= rowNum; i++) {
                    categoryStationFestival_ = new CategoryStationFestival();
                    String err = checkRow(sheet, i, categoryStationFestival_);
                    if (err != null) {
                        //Truong hop row co du lieu
                        if ("".equals(err)) {
                            categoryStationFestival_.setResult("OK");
                            if (!listNodeCode.contains(categoryStationFestival_.getNodeb().toLowerCase())) {
                                dataResult.add(categoryStationFestival_);
                                listNodeCode.add(categoryStationFestival_.getNodeb().toLowerCase());
                            }
                        } else {
                            checkImport = false;
                            categoryStationFestival_.setResult("NOK");
                        }
                        categoryStationFestival_.setResultDetail(err);
                        dataInput.add(categoryStationFestival_);
                    }
                }
            } else {
                MessageUtil.setErrorMessage(MessageUtil.getResourceBundleMessage("message.invalid.header"));
                return;
            }
            if (dataResult.size() > 0 && listNodeCode.size() > 0) {
                HashMap<String, Object> filters = new HashMap<>();
                filters.put("nodeb-EXAC_IGNORE_CASE", listNodeCode);
                List<CategoryStationFestival> listExist = categoryStationFestivalService.findList(filters);
                HashMap<String, CategoryStationFestival> mapExist = new HashMap<>();
                if (listExist.size() > 0) {
                    for (CategoryStationFestival categoryStationFestival1 : listExist) {
                        mapExist.put(categoryStationFestival1.getNodeb().toLowerCase(), categoryStationFestival1);
                    }
                }
                for (CategoryStationFestival bo : dataResult) {
                    bo.setUpdateTime(new Date());
                    bo.setCreateUser(SessionWrapper.getCurrentUsername());
                    if (mapExist.containsKey(bo.getNodeb().toLowerCase())) {
                        bo.setId(mapExist.get(bo.getNodeb().toLowerCase()).getId());
                    }
                }
                categoryStationFestivalService.saveOrUpdate(dataResult);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            MessageUtil.setErrorMessageFromRes("meassage.import.fail");
            return;
        } finally {
            if (wb != null) {
                try {
                    wb.close();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        try {
            ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance()
                    .getExternalContext().getContext();
            String pathTemplate = ctx.getRealPath("/")
                    + File.separator + "templates" + File.separator + "import" + File.separator + "Template_import_catagory_station_festival_result.xlsx";
            InputStream fileTemplate = new FileInputStream(pathTemplate);

            Workbook workbook = WorkbookFactory.create(fileTemplate);
            File fileExport = exportFileResult(workbook, dataInput, 5, "Category_NodeB_Festival.xlsx");

            resultImport = new DefaultStreamedContent(new FileInputStream(fileExport), ".xlsx", fileExport.getName());
            if(checkImport){
                MessageUtil.setInfoMessageFromRes("meassage.import.success");
            }else{
                MessageUtil.setErrorMessageFromRes("meassage.import.fail");
            }
            RequestContext.getCurrentInstance().execute("PF('importDialog').hide();");
            RequestContext.getCurrentInstance().execute("PF('resultImportDialog').show();");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            MessageUtil.setErrorMessageFromRes("meassage.import.fail");
        }
    }

    public boolean checkHeader(Row rowHeader) {
        boolean check = true;
        int i = 0;
        String header1 = rowHeader.getCell(i) != null && getCellValue(rowHeader.getCell(i)) != null ? getCellValue(rowHeader.getCell(i)).trim().toLowerCase() : "";
        i++;
        String header2 = rowHeader.getCell(i) != null && getCellValue(rowHeader.getCell(i)) != null ? getCellValue(rowHeader.getCell(i)).trim().toLowerCase() : "";
        i++;
        String header3 = rowHeader.getCell(i) != null && getCellValue(rowHeader.getCell(i)) != null ? getCellValue(rowHeader.getCell(i)).trim().toLowerCase() : "";
        i++;
        String header4 = rowHeader.getCell(i) != null && getCellValue(rowHeader.getCell(i)) != null ? getCellValue(rowHeader.getCell(i)).trim().toLowerCase() : "";
//        i++;
        if (!"no.".equals(header1)
                || !"nodeb".equals(header2)
                || !("start time\n" +
                "(dd/mm/yyyy hh:mm:ss)").equals(header3)
                || !("end time\n" +
                "(dd/mm/yyyy hh:mm:ss)").equals(header4)) {
            check = false;
        }
        return check;
    }

    public String checkRow(Sheet sheet, int i, CategoryStationFestival categoryStationFestival_) {
        String err = "";
        Row row = sheet.getRow(i);
        if (row != null) {
            int j = 0;
//            String STT = getCellValue(row.getCell(j));
            j++;
            String nodeB = getCellValue(row.getCell(j));
            j++;
            String startTime = getCellValue(row.getCell(j));
            j++;
            String endTime = getCellValue(row.getCell(j));

            if (!isNullOrEmpty(nodeB)
                    || !isNullOrEmpty(startTime)
                    || !isNullOrEmpty(endTime)) {
                if (isNullOrEmpty(nodeB)) {
                    err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                            MessageUtil.getResourceBundleMessage("label.NodeB")) + "\n";
                } else {
                    categoryStationFestival_.setNodeb(nodeB);
                }
                if (isNullOrEmpty(startTime)) {
                    err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                            MessageUtil.getResourceBundleMessage("label.startTime")) + "\n";
                } else {
                    try {
                        categoryStationFestival_.setStartTime(DateTimeUtils.convertStringToDate(startTime, patternDate));
                    } catch (Exception ex) {
                        err += MessageFormat.format(MessageUtil.getResourceBundleMessage("meassage.fail.invalid.date"),
                                MessageUtil.getResourceBundleMessage("label.startTime")) + "\n";
                        logger.error(ex.getMessage(), ex);
                    }
                }
                if (isNullOrEmpty(endTime)) {
                    err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                            MessageUtil.getResourceBundleMessage("label.endTime")) + "\n";
                } else {
                    try {
                        categoryStationFestival_.setEndTime(DateTimeUtils.convertStringToDate(endTime, patternDate));
                    } catch (Exception ex) {
                        err += MessageFormat.format(MessageUtil.getResourceBundleMessage("meassage.fail.invalid.date"),
                                MessageUtil.getResourceBundleMessage("label.endTime")) + "\n";
                        logger.error(ex.getMessage(), ex);
                    }
                }
                if (isNullOrEmpty(err)) {
                    if (categoryStationFestival_.getStartTime().getTime() >= categoryStationFestival_.getEndTime().getTime()) {
                        err += MessageUtil.getResourceBundleMessage("message.startTime.endTime") + "\n";
                    }
                }

                return err;
            } else {
                return null;
            }
        }
        return null;
    }

    public StreamedContent onDownloadTemplate() {
        try {
            InputStream stream = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/templates/import/Template_import_catagory_station_festival.xlsx");
            return new DefaultStreamedContent(stream, "application/xls", "Template_import_catagory_station_festival.xlsx");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public StreamedContent getImportResult() {
        return resultImport;
    }

    private String getCellValue(Cell cell) {
        String result = "";
        if (cell == null) {
            return result;
        }
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                result = cell.getStringCellValue();
                break;
            case Cell.CELL_TYPE_FORMULA:
                result = cell.getStringCellValue();
                break;

            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    result = cell.getDateCellValue().toString();
                } else {
                    Double number = cell.getNumericCellValue();
                    if (Math.round(number) == number) {
                        result = Long.toString(Math.round(number));
                    } else {
                        result = Double.toString(cell.getNumericCellValue());
                    }
                }
                break;

            case Cell.CELL_TYPE_BLANK:
                result = "";
                break;

            case Cell.CELL_TYPE_BOOLEAN:
                result = Boolean.toString(cell.getBooleanCellValue());
                break;
        }
        return result.trim();
    }*/
    //20180124 Quyvt7 import end

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Common">
    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static String getFolderSave() {
        String pathOut;
        ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance()
                .getExternalContext().getContext();
        pathOut = ctx.getRealPath("/") + Config.PATH_OUT;
        File folderOut = new File(pathOut);
        if (!folderOut.exists()) {
            folderOut.mkdirs();
        }
        return pathOut;
    }

    //</editor-fold>
}
