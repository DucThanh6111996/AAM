package com.viettel.util;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.rits.cloning.Cloner;
import com.viettel.controller.Module;
import com.viettel.bean.OsAccount;
import com.viettel.bean.ServiceDatabase;
import com.viettel.bean.Unit;
import com.viettel.controller.IimClientService;
import com.viettel.controller.IimClientServiceImpl;
import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.it.util.MessageUtil;
import com.viettel.model.*;
import com.viettel.persistence.*;
import com.viettel.webservice.MopFileResult;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.docx4j.TextUtils;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.XPathBinderAssociationIsPartialException;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.*;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

public class DocxUtil {
    private static Logger logger = LogManager.getLogger(DocxUtil.class);
    private static String COMMAND_COLOR = "red";
    /*
     * processing: -read template -find positon table -create table -add table
     * to docx save file
     */
//    private static String columnNetworkNode = MessageUtil.getResourceBundleMessage("columnNetworkNode");//"STT : Tên node tác động/IP/module : Khu vực phục vụ : Tên node mạng/IP/module liên quan : Khu vực phục vụ(node ảnh hưởng)";
//    private static String columnBackupDatabase = MessageUtil.getResourceBundleMessage("columnBackupDatabase");//"STT :Node mạng : Mô tả  : Lệnh thực hiện : Kết quả mong muốn : Thời gian thực hiện (Phút) : Kết quả : Mục RollBack : Ghi chú";
//    private static String columnStopAppTomcat = MessageUtil.getResourceBundleMessage("columnStopAppTomcat");//"STT :Node mạng : Mô tả  : Lệnh thực hiện : Kết quả mong muốn : Thời gian thực hiện (Phút) : Kết quả : Mục RollBack : Ghi chú";
//    private static String columnCheckKpi = MessageUtil.getResourceBundleMessage("columnCheckKpi");//"Mã dịch vụ : Tên dịch vụ : Chuỗi kết nối : Acount : Mã tiêu chí(Là tham số đầu vào API) : Câu lệnh : Ngưỡng : Đối số so sánh (>, <, = …): Kết quả thực tế : đánh giá (OK/NOK)";
//    // private static String columnBackupCode =
//    // "TT : Node : Module : Port : Command : Description : Th�?i gian thực hiện (phút) : Kết quả thực hiện. (Ngư�?i T/H đi�?n thông tin) : Sai khác lệnh, kết quả khi thực hiện. (Ngư�?i T/H đi�?n thông tin)";
//    private static String columnUpcode = MessageUtil.getResourceBundleMessage("columnUpcode");//"STT :Node mạng : Mô tả  : Lệnh thực hiện : Kết quả mong muốn : Thời gian thực hiện (Phút) : Kết quả : Mục RollBack : Ghi chú";
//    private static String columnChangeConfigDB = MessageUtil.getResourceBundleMessage("columnChangeConfigDB");//"STT :Node mạng : Mô tả  : Lệnh thực hiện : Kết quả mong muốn : Thời gian thực hiện (Phút) : Kết quả : Mục RollBack : Ghi chú";
//    private static String columnCheckList = MessageUtil.getResourceBundleMessage("columnCheckList");//"TT :D/s Node mạng : Mô tả  : Lệnh thực hiện : Tham số : Kết quả mong muốn : Thời gian thực hiện (Phút) : Kết quả :Mục Rollback tương ứng: Ghi chú";
//
//    private static String posNetworkNode = MessageUtil.getResourceBundleMessage("posNetworkNode");//"DANH SÁCH NODE MẠNG PHỤC VỤ TÁC ĐỘNG";
//    private static String posCheckStatusTomcat = MessageUtil.getResourceBundleMessage("posCheckStatusTomcat");//"KIỂM TRA HỆ THốNG TRƯỚC THAY ĐỔI";
//    private static String posStopAppTomcat = MessageUtil.getResourceBundleMessage("posStopAppTomcat");//"DỪNG ỨNG DỤNG";
//    private static String posBackupDB = "BACKUP DATABASE";
//    private static String posBackupCode = MessageUtil.getResourceBundleMessage("posBackupCode");//"BACKUP CODE VÀ CẤU HÌNH";
//    private static String posUpcode = "UPCODE";
//    private static String posChangeConfigDB = MessageUtil.getResourceBundleMessage("posChangeConfigDB");//"THAY ĐỔI DATABASE";
//    private static String posClearCache = MessageUtil.getResourceBundleMessage("posClearCache");//"XÓA CACHE ỨNG DỤNG";
//    private static String posStartApp = MessageUtil.getResourceBundleMessage("posStartApp");//"BẢNG START ỨNG DỤNG";
//    private static String posUpcodeStopStartApp = MessageUtil.getResourceBundleMessage("posUpcodeStopStartApp");//"BẢNG UPCODE + STOP/START ỨNG DỤNG";
//    private static String posRestartApp = MessageUtil.getResourceBundleMessage("posRestartApp");//"BẢNG RESTART(STOP/START) ỨNG DỤNG";
//    private static String posRestartAppCmd = MessageUtil.getResourceBundleMessage("posRestartAppCmd");//"BẢNG RESTART ỨNG DỤNG";
//    private static String posRollBackData = "ROLLBACK DATABASE";
//    private static String posRollBackCode = "ROLLBACK CODE";
//    private static String posCheckLast = MessageUtil.getResourceBundleMessage("posCheckLast");//"KIỂM TRA HỆ THỐNG SAU THAY ĐỔI";
//    private static String posCheckList = MessageUtil.getResourceBundleMessage("posCheckList");//"CHECKLIST SAU TÁC ĐỘNG";
//    private static String posCheckListDb = "CHECKLIST DATABASE";
//
//    private static String afterCheckStatus = "{AFTER CHECK STATUS}";
//    private static String afterStopApp = "{AFTER STOP APP}";
//    private static String afterBackupApp = "{AFTER BACKUP APP}";
//    private static String afterBackupDb = "{AFTER BACKUP DB}";
//    private static String afterUpcode = "{AFTER UPCODE}";
//    private static String afterTdDb = "{AFTER TD DB}";
//    private static String afterClearCache = "{AFTER CLEAR CACHE}";
//    private static String afterRestartApp = "{AFTER RESTART APP}";
//    private static String afterStartApp = "{AFTER START APP}";
//
//    private static String tdStopApp = "{TD STOP APP}";
//    private static String tdBackupApp = "{TD BACKUP APP}";
//    private static String tdBackupDb = "{TD BACKUP DB}";
//    private static String tdUpcode = "{TD UPCODE}";
//    private static String tdTdDb = "{TD TD DB}";
//    private static String tdClearCache = "{TD CLEAR CACHE}";
//    private static String tdRestartApp = "{TD RESTART APP}";
//    private static String tdStartApp = "{TD START APP}";
//
//    private static String afterRollbackCheckStatus = "{AFTER ROLLBACK CHECK STATUS}";
//    private static String afterRollbackStopApp = "{AFTER ROLLBACK STOP APP}";
//    private static String afterRollbackCode = "{AFTER ROLLBACK CODE}";
//    private static String afterRollbackDb = "{AFTER ROLLBACK DATABASE}";
//    private static String afterRollbackClearCache = "{AFTER ROLLBACK CLEAR CACHE}";
//    private static String afterRollbackRestartApp = "{AFTER ROLLBACK RESTART APP}";
//    private static String afterRollbackStartApp = "{AFTER ROLLBACK START APP}";
//
//    private static String rollbackCheckStatus = "{ROLLBACK CHECK STATUS}";
//    private static String rollbackStopApp = "{ROLLBACK STOP APP}";
//    private static String rollbackCode = "{ROLLBACK CODE}";
//    private static String rollbackDb = "{ROLLBACK DATABASE}";
//    private static String rollbackClearCache = "{ROLLBACK CLEAR CACHE}";
//    private static String rollbackRestartApp = "{ROLLBACK RESTART APP}";
//    private static String rollbackStartApp = "{ROLLBACK START APP}";
//
//    private static String labelStopApp = MessageUtil.getResourceBundleMessage("stop.app");//"Dừng ứng dụng";
//    private static String labelBackupApp = MessageUtil.getResourceBundleMessage("backup.code.and.configuration");//"Backup code và cấu hình";
//    private static String labelBackupDb = "Backup Database";
//    private static String labelUpcode = MessageUtil.getResourceBundleMessage("upcode.new");//"Upcode mới";
//    private static String labelTdDb = MessageUtil.getResourceBundleMessage("execute.edit.db");//"Thực hiện thay đổi DB";
//    private static String labelClearCache = MessageUtil.getResourceBundleMessage("clear.cache.app");//"Xoá cache ứng dụng";
//    private static String labelRestartApp = MessageUtil.getResourceBundleMessage("restart.app");//"Restart ứng dụng";
//    private static String labelStartApp = MessageUtil.getResourceBundleMessage("start.app.again");//"Bật lại ứng dụng";
//
//    private static String labelRollbackStopApp = MessageUtil.getResourceBundleMessage("stop.app");//"Dừng ứng dụng";
//    private static String labelRollbackCode = MessageUtil.getResourceBundleMessage("rollback.code.and.configuration");//"Rollback code và cấu hình";
//    private static String labelRollbackDb = "Rollback Database";
//    private static String labelRollbackClearCache = MessageUtil.getResourceBundleMessage("clear.cache.app");//"Xoá cache ứng dụng";
//    private static String labelRollbackRestartApp = MessageUtil.getResourceBundleMessage("restart.app");//"Restart ứng dụng";
//    private static String labelRollbackStartApp = MessageUtil.getResourceBundleMessage("start.app.again");//"Bật lại ứng dụng";

    //tuanda38_20180720
    private static String columnNetworkNode = "No. : Node name impact/IP/module : Service area : Node name/IP/module related : Service area (node affect)";//"STT : Tên node tác động/IP/module : Khu vực phục vụ : Tên node mạng/IP/module liên quan : Khu vực phục vụ(node ảnh hưởng)";
    private static String columnBackupDatabase = "No. :Order : Node : Description : Command execute : Desired Result : Execute time (minute) : Result : Item rollback : Note";//"STT :Node mạng : Mô tả  : Lệnh thực hiện : Kết quả mong muốn : Thời gian thực hiện (Phút) : Kết quả : Mục RollBack : Ghi chú";
    private static String columnStopAppTomcat = "No. :Order : Node : Description : Command execute : Desired Result : Execute time (minute) : Result : Item rollback : Note";//"STT :Node mạng : Mô tả  : Lệnh thực hiện : Kết quả mong muốn : Thời gian thực hiện (Phút) : Kết quả : Mục RollBack : Ghi chú";
    private static String columnCheckKpi = "Service code : Service name : Connection string : Account : Criteria Id (As API input parameter) : Command : Threshold : Comparative argument (>, <, = ...) : Actual result : Assess (OK/NOK)";//"Mã dịch vụ : Tên dịch vụ : Chuỗi kết nối : Acount : Mã tiêu chí(Là tham số đầu vào API) : Câu lệnh : Ngưỡng : Đối số so sánh (>, <, = …): Kết quả thực tế : đánh giá (OK/NOK)";
    // private static String columnBackupCode =
    // "TT : Node : Module : Port : Command : Description : Th�?i gian thực hiện (phút) : Kết quả thực hiện. (Ngư�?i T/H đi�?n thông tin) : Sai khác lệnh, kết quả khi thực hiện. (Ngư�?i T/H đi�?n thông tin)";
    private static String columnUpcode = "No. :Order : Node : Description : Command execute : Desired Result : Execute time (minute) : Result : Item rollback : Note";//"STT :Node mạng : Mô tả  : Lệnh thực hiện : Kết quả mong muốn : Thời gian thực hiện (Phút) : Kết quả : Mục RollBack : Ghi chú";
    private static String columnChangeConfigDB = "No. :Order : Node : Description : Command execute : Desired Result : Execute time (minute) : Result : Item rollback : Note";//"STT :Node mạng : Mô tả  : Lệnh thực hiện : Kết quả mong muốn : Thời gian thực hiện (Phút) : Kết quả : Mục RollBack : Ghi chú";
    private static String columnCheckList = "No. : List node : Description : Command : Parameters : Desired Result : Execute time (minute) : Result : Item rollback : Note";//"TT :D/s Node mạng : Mô tả  : Lệnh thực hiện : Tham số : Kết quả mong muốn : Thời gian thực hiện (Phút) : Kết quả :Mục Rollback tương ứng: Ghi chú";

    private static String posNetworkNode = "CHANGED/AFFECTED NODES LIST";//"DANH SÁCH NODE MẠNG PHỤC VỤ TÁC ĐỘNG";
    private static String posCheckStatusTomcat = "CHECK BEFORE CHANGE";//"KIỂM TRA HỆ THốNG TRƯỚC THAY ĐỔI";
    private static String posStopAppTomcat = "STOP APPLICATION";//"DỪNG ỨNG DỤNG";
    private static String posBackupDB = "BACKUP DATABASE";
    private static String posBackupCode = "BACKUP CODE AND CONFIGURATION";//"BACKUP CODE VÀ CẤU HÌNH";
    private static String posUpcode = "UPCODE";
    private static String posChangeConfigDB = "DATABASE CHANGING";//"THAY ĐỔI DATABASE";
    private static String posClearCache = "CLEAR APPLICATION CACHE";//"XÓA CACHE ỨNG DỤNG";
    private static String posStartApp = "APPLICATION START TABLE";//"BẢNG START ỨNG DỤNG";
    private static String posUpcodeStopStartApp = "APPLICATION UPCODE + STOP/START TABLE";//"BẢNG UPCODE + STOP/START ỨNG DỤNG";
    private static String posRestartApp = "APPLICATION RESTART (STOP/START) TABLE";//"BẢNG RESTART(STOP/START) ỨNG DỤNG";
    private static String posRestartAppCmd = "APPLICATION RESTART TABLE";//"BẢNG RESTART ỨNG DỤNG";
    private static String posRollBackData = "ROLLBACK DATABASE";
    private static String posRollBackCode = "ROLLBACK CODE";
    private static String posCheckLast = "CHECK SYSTEMS AFTER CHANGE";//"KIỂM TRA HỆ THỐNG SAU THAY ĐỔI";
    private static String posCheckList = "CHECKLIST AFTER CHANGE";//"CHECKLIST SAU TÁC ĐỘNG";
    private static String posCheckListDb = "CHECKLIST DATABASE";

    private static String afterCheckStatus = "{AFTER CHECK STATUS}";
    private static String afterStopApp = "{AFTER STOP APP}";
    private static String afterBackupApp = "{AFTER BACKUP APP}";
    private static String afterBackupDb = "{AFTER BACKUP DB}";
    private static String afterUpcode = "{AFTER UPCODE}";
    private static String afterTdDb = "{AFTER TD DB}";
    private static String afterClearCache = "{AFTER CLEAR CACHE}";
    private static String afterRestartApp = "{AFTER RESTART APP}";
    private static String afterStartApp = "{AFTER START APP}";

    private static String tdStopApp = "{TD STOP APP}";
    private static String tdBackupApp = "{TD BACKUP APP}";
    private static String tdBackupDb = "{TD BACKUP DB}";
    private static String tdUpcode = "{TD UPCODE}";
    private static String tdTdDb = "{TD TD DB}";
    private static String tdClearCache = "{TD CLEAR CACHE}";
    private static String tdRestartApp = "{TD RESTART APP}";
    private static String tdStartApp = "{TD START APP}";

    private static String afterRollbackCheckStatus = "{AFTER ROLLBACK CHECK STATUS}";
    private static String afterRollbackStopApp = "{AFTER ROLLBACK STOP APP}";
    private static String afterRollbackCode = "{AFTER ROLLBACK CODE}";
    private static String afterRollbackDb = "{AFTER ROLLBACK DATABASE}";
    private static String afterRollbackClearCache = "{AFTER ROLLBACK CLEAR CACHE}";
    private static String afterRollbackRestartApp = "{AFTER ROLLBACK RESTART APP}";
    private static String afterRollbackStartApp = "{AFTER ROLLBACK START APP}";

    private static String rollbackCheckStatus = "{ROLLBACK CHECK STATUS}";
    private static String rollbackStopApp = "{ROLLBACK STOP APP}";
    private static String rollbackCode = "{ROLLBACK CODE}";
    private static String rollbackDb = "{ROLLBACK DATABASE}";
    private static String rollbackClearCache = "{ROLLBACK CLEAR CACHE}";
    private static String rollbackRestartApp = "{ROLLBACK RESTART APP}";
    private static String rollbackStartApp = "{ROLLBACK START APP}";

    private static String labelStopApp = "Stop app";//"Dừng ứng dụng";
    private static String labelBackupApp = "Backup code and configuration";//"Backup code và cấu hình";
    private static String labelBackupDb = "Backup Database";
    private static String labelUpcode = "Upcode new";//"Upcode mới";
    private static String labelTdDb = "Execute edit DB";//"Thực hiện thay đổi DB";
    private static String labelClearCache = "clear cache app";//"Xoá cache ứng dụng";
    private static String labelRestartApp = "Restart app";//"Restart ứng dụng";
    private static String labelStartApp = "Start app again";//"Bật lại ứng dụng";

    private static String labelRollbackStopApp = "Stop app";//"Dừng ứng dụng";
    private static String labelRollbackCode = "Rollback code and configuration";//"Rollback code và cấu hình";
    private static String labelRollbackDb = "Rollback Database";
    private static String labelRollbackClearCache = "clear cache app";//"Xoá cache ứng dụng";
    private static String labelRollbackRestartApp = "Restart app";//"Restart ứng dụng";
    private static String labelRollbackStartApp = "Start app again";//"Bật lại ứng dụng";
    //tuanda38_20180720

    private Map<Integer, String> groupActionLabels;
    private Map<Integer, Integer> indexMap;
    private Integer tdIndex;
    private Integer rbIndex;

    private static IimService iimService = new IimServiceImpl();

    private static final String CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    private Action action;
    private Long actionType;
    private String prefixName;

    private Integer kbGroup;
    private Integer minKbGroup;
    private Boolean includeTestBed;
    private final Integer DEFAULT_KB_GROUP = 1;
    private List<Long> moduleIds;

    private String impactDesc = "";
    private String rollbackDesc = "";

    public static void main(String[] args) {

        /*ActionService actionService = new ActionServiceImpl();

        String impactDesc = "";
        String rollbackDesc = "";
        try {
            Action action = actionService.findById(5186L);

            List<Integer> kbGroups = actionService.findKbGroups(action.getId());
            Collections.sort(kbGroups);

            logger.info(kbGroups);

            String mopDir = UploadFileUtils.getMopFolder(action);
            String template = UploadFileUtils.getResourceFolder() + File.separator + "file-template" + File.separator + "TEMPLATE_DT_NEW.docx";
            String templateRollBack = UploadFileUtils.getResourceFolder() + File.separator + "file-template" + File.separator + "TEMPLATE_DT_ROLLBACK.docx";

            String subTemplate = UploadFileUtils.getResourceFolder() + File.separator + "file-template" + File.separator + "SUB_TEMPLATE_DT_NEW.docx";
            String subTemplateRollBack = UploadFileUtils.getResourceFolder() + File.separator + "file-template" + File.separator + "SUB_TEMPLATE_DT_ROLLBACK.docx";

            if (kbGroups.size() > 1) {
                impactDesc += "Tác động tiếp theo kịch bản";
                rollbackDesc += "Rollback tiếp theo kịch bản";
                for (Integer kbGroup : kbGroups) {
                    if (kbGroup == 1)
                        continue;

                    impactDesc += "\n" + new DocxUtil().fileName(action, false, kbGroup, action.getCrNumber());
                    rollbackDesc += "\n"  + new DocxUtil().fileName(action, true, kbGroup, action.getCrNumber());;
                }
            }

            logger.info(impactDesc);

            File file1 = new File(mopDir);
            FileUtils.deleteQuietly(file1);
            file1.mkdir();

            ZipFile zipKbFile = null;
            ZipFile zipRollbackFile;

            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_FAST);
            parameters.setIncludeRootFolder(false);

            for (Integer kbGroup : kbGroups) {
                ActionModuleService moduleService = new ActionModuleServiceImpl();
                List<Long> moduleIds = moduleService.findListModuleId(action.getId(), kbGroup, false);
                String filename = new DocxUtil(kbGroup, true, moduleIds, kbGroup == 1 ? impactDesc : "", kbGroup == 1 ? rollbackDesc : "").genericDT(action, kbGroup == 1 ? template : subTemplate, kbGroup == 1 ? templateRollBack : subTemplateRollBack, mopDir, null, action.getCrNumber());

                if (kbGroups.size() > 1) {

                    try {
                        zipKbFile = new ZipFile(mopDir + File.separator + filename + "_tacdong_.zip");
                        zipRollbackFile = new ZipFile(mopDir + File.separator + filename + "_rollback_.zip");

                        File kb = new File(mopDir + File.separator + filename + "_tacdong_" + (kbGroup != 1 ? kbGroup : "") + ".docx");
                        File rollback = new File(mopDir + File.separator + filename + "_rollback_" + (kbGroup != 1 ? kbGroup : "") + ".docx");

                        zipKbFile.addFile(kb, parameters);
                        zipRollbackFile.addFile(rollback, parameters);

                        FileUtils.forceDelete(kb);
                        FileUtils.forceDelete(rollback);
                    } catch (ZipException e) {
                        logger.error(e.getMessage(), e);
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }
                    logger.info(zipKbFile);
                }
            }
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }*/

        List<String> ips = new DocxUtil().getListImpactIP(5557L);
        System.out.println(ips);
    }

    public DocxUtil() {
        this(1, 1, true, null, "", "");
    }

    public DocxUtil(Action action) {
        this(1, 1, true, null, "", "");
        this.action = action;
    }

    public DocxUtil(Integer kbGroup, Integer minKbGroup, Boolean includeTestBed, List<Long> moduleIds, String impactDesc, String rollbackDesc) {
        this.minKbGroup = minKbGroup;
        this.kbGroup = kbGroup;
        this.includeTestBed = includeTestBed;
        this.moduleIds = moduleIds;
        this.impactDesc = impactDesc;
        this.rollbackDesc = rollbackDesc;
    }

    public static String export(Action action, String crNumber) {
        ActionService actionService = new ActionServiceImpl();

        String impactDesc = "";
        String rollbackDesc = "";

        String filename = "";
        try {
            List<Integer> kbGroups = actionService.findKbGroups(action.getId());
            Collections.sort(kbGroups);
            Integer minKbGroup = kbGroups.get(0);

            logger.info(kbGroups);

            String mopDir = UploadFileUtils.getMopFolder(action);
            String template = UploadFileUtils.getResourceFolder() + File.separator + "file-template" + File.separator + "TEMPLATE_DT_NEW_EN.docx";
            String templateRollBack = UploadFileUtils.getResourceFolder() + File.separator + "file-template" + File.separator + "TEMPLATE_DT_ROLLBACK_EN.docx";
//            String subTemplate = UploadFileUtils.getResourceFolder() + File.separator + "file-template" + File.separator + "SUB_TEMPLATE_DT_NEW_EN.docx";
//            String subTemplateRollBack = UploadFileUtils.getResourceFolder() + File.separator + "file-template" + File.separator + "SUB_TEMPLATE_DT_ROLLBACK_EN.docx";
            String subTemplate = UploadFileUtils.getResourceFolder() + File.separator + "file-template" + File.separator + "TEMPLATE_DT_NEW_EN.docx";
            String subTemplateRollBack = UploadFileUtils.getResourceFolder() + File.separator + "file-template" + File.separator + "TEMPLATE_DT_ROLLBACK_EN.docx";

            if (kbGroups.size() > 1) {
                impactDesc += "Impact next MOP: ";
                rollbackDesc += "Rollback next MOP: ";
                for (Integer kbGroup : kbGroups) {
                    if (kbGroup == minKbGroup)
                        continue;

                    impactDesc += "" + new DocxUtil().fileName(action, false, kbGroup, crNumber);
                    rollbackDesc += "" + new DocxUtil().fileName(action, true, kbGroup, crNumber);
                }
            }

            logger.info(impactDesc);

            File file1 = new File(mopDir);
            FileUtils.deleteQuietly(file1);
            file1.mkdir();

            ZipFile zipKbFile = null;
            ZipFile zipRollbackFile;

            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_FAST);
            parameters.setIncludeRootFolder(false);

            for (Integer kbGroup : kbGroups) {
                ActionModuleService moduleService = new ActionModuleServiceImpl();
                List<Long> moduleIds = moduleService.findListModuleId(action.getId(), kbGroup, false);
                filename = new DocxUtil(kbGroup, minKbGroup, true, moduleIds, kbGroup == minKbGroup ? impactDesc : "", kbGroup == minKbGroup ? rollbackDesc : "").genericDT(action, kbGroup == minKbGroup ? template : subTemplate, kbGroup == minKbGroup ? templateRollBack : subTemplateRollBack, mopDir, null, crNumber);
                if (kbGroups.size() > 1) {

                    try {
                        zipKbFile = new ZipFile(mopDir + File.separator + filename + "_tacdong_.zip");
                        zipRollbackFile = new ZipFile(mopDir + File.separator + filename + "_rollback_.zip");

                        File kb = new File(mopDir + File.separator + filename + "_tacdong_" + kbGroup + ".docx");
                        File rollback = new File(mopDir + File.separator + filename + "_rollback_" + kbGroup + ".docx");

                        zipKbFile.addFile(kb, parameters);
                        zipRollbackFile.addFile(rollback, parameters);

                        FileUtils.forceDelete(kb);
                        FileUtils.forceDelete(rollback);
                    } catch (ZipException e) {
                        logger.error(e.getMessage(), e);
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }
                    logger.info(zipKbFile);
                }
            }
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }

        return filename;
    }

    /*public static String export(Action action, String crNumber) {
        String mopDir = UploadFileUtils.getMopFolder(action);
        String template = UploadFileUtils.getResourceFolder() + File.separator + "file-template" + File.separator + "TEMPLATE_DT_NEW.docx";
        String templateRollBack = UploadFileUtils.getResourceFolder() + File.separator + "file-template" + File.separator + "TEMPLATE_DT_ROLLBACK.docx";

        File file1 = new File(mopDir);
        FileUtils.deleteQuietly(file1);
        file1.mkdir();

        String zipFile = new DocxUtil().genericDT(action, template, templateRollBack, mopDir, null, crNumber);

        return zipFile;
    }*/

    public static String exportUctt(Action action, String crNumber) {
        String mopDir = UploadFileUtils.getMopFolder(action);
        logger.info(mopDir);
        String template = UploadFileUtils.getResourceFolder() + File.separator + "file-template" + File.separator + "TEMPLATE_DT_NEW_EN.docx";
        String templateRollBack = UploadFileUtils.getResourceFolder() + File.separator + "file-template" + File.separator + "TEMPLATE_DT_ROLLBACK_EN.docx";

        File file1 = new File(mopDir);
//		FileUtils.deleteQuietly(file1);
        file1.mkdir();

        String zipFile = new DocxUtil().genericDT(action, template, templateRollBack, mopDir, null, crNumber);

        return zipFile;
    }

    public static InputStream mergeDocx(final LinkedHashMap<String, InputStream> mapFile,
                                        LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, String>>> tables) throws Docx4JException, IOException, JAXBException {

        WordprocessingMLPackage target = null;
        final File generated = File.createTempFile("generate", ".docx");

        int chunkId = 0;

        for (Map.Entry<String, InputStream> entry : mapFile.entrySet()) {
            InputStream is = entry.getValue();
            if (is != null) {
                if (target == null) {
                    // Copy first (master) document
                    OutputStream os = new FileOutputStream(generated);
                    os.write(IOUtils.toByteArray(is));
                    os.close();

                    target = WordprocessingMLPackage.load(generated);
                    Tbl table = createTable(target, columnNetworkNode, tables.get(""), -1, false, false);
                    target.getMainDocumentPart().addObject(table);
                } else {

                    // Attach the others (Alternative input parts)
                    insertDocx(target.getMainDocumentPart(), IOUtils.toByteArray(is), chunkId++);
                    // insert table
                    Tbl table = null;
                    switch (entry.getKey()) {

                        case "templateDT2":
                            table = createTable(target, "", tables.get(""), -1, false, false);
                            break;
                        default:
                            break;
                    }

                    target.getMainDocumentPart().addObject(table);
                }
            }

        }

        if (target != null) {
            target.save(generated);
            return new FileInputStream(generated);
        } else {
            return null;
        }
    }

    private static void insertDocx(MainDocumentPart main, byte[] bytes, int chunkId) {
        try {
            AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(new PartName("/part" + chunkId + ".docx"));
            afiPart.setContentType(new ContentType(CONTENT_TYPE));
            afiPart.setBinaryData(bytes);
            Relationship altChunkRel = main.addTargetPart(afiPart);

            CTAltChunk chunk = Context.getWmlObjectFactory().createCTAltChunk();
            chunk.setId(altChunkRel.getId());

            main.addObject(chunk);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void saveFile(InputStream inputStream, String fileName) {
        OutputStream outputStream = null;
        try {
            // read this file into InputStream

            // write the inputStream to a FileOutputStream
            File f = new File(fileName);
            if (!f.exists())
                f.createNewFile();
            outputStream = new FileOutputStream(new File(fileName));

            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }

            System.out.println("Done!");

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (outputStream != null) {
                try {
                    // outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }

            }
        }
    }

    static Tbl createTable(WordprocessingMLPackage target, String colums, LinkedHashMap<String, LinkedHashMap<String, String>> data, int collColor, boolean checkBackground, boolean isFixWidth) {
        String columsLst[] = colums.split(":");
        LinkedHashMap<String, String> dataColumns = new LinkedHashMap<>();
        for (String colum : columsLst) {
            dataColumns.put(colum.trim(), colum.trim());
        }

        ObjectFactory factory = Context.getWmlObjectFactory();
        Tbl table = factory.createTbl();
        addBorders(table);
        Tr tableRow;
        // create header

        if (dataColumns.size() > 0) {
            tableRow = addTableRow(target, factory, dataColumns, true, 18, 2500, collColor, false, true, isFixWidth);
            table.getContent().add(tableRow);
        }
        // add data
        if (data != null) {
            boolean background = false;
            String lastModule = "";
            for (Map.Entry<String, LinkedHashMap<String, String>> entry : data.entrySet()) {
                if (checkBackground) {
                    if (!lastModule.equals(entry.getValue().get("2"))) {
                        background = background ? false : true;
                    }
                    lastModule = entry.getValue().get("2");
                }

                tableRow = addTableRow(target, factory, entry.getValue(), false, 18, 4000, collColor, background, false, isFixWidth);
                table.getContent().add(tableRow);
            }
        }
        return table;
    }

    static Tbl createTable(WordprocessingMLPackage target, String colums, LinkedHashMap<String, LinkedHashMap<String, String>> data, boolean isfixWidth) {
        String columsLst[] = colums.split(":");
        LinkedHashMap<String, String> dataColumns = new LinkedHashMap<>();
        for (String colum : columsLst) {
            dataColumns.put(colum.trim(), colum.trim());
        }

        ObjectFactory factory = Context.getWmlObjectFactory();
        Tbl table = factory.createTbl();
        addBorders(table);
        Tr tableRow;
        // create header
//		int i = 0;
        if (dataColumns.size() > 0) {
/*			i++;
            if (i == 6)
				tableRow = addTableRow(target, factory, dataColumns, true, 18, 8000, isfixWidth);*/
//			else
            tableRow = addTableRow(target, factory, dataColumns, true, 18, 3500, isfixWidth);
            table.getContent().add(tableRow);
        }
        // add data
//		int j = 0;
        if (data != null)
            for (Map.Entry<String, LinkedHashMap<String, String>> entry : data.entrySet()) {
/*				j++;
                if (j == 6)
					tableRow = addTableRow(target, factory, entry.getValue(), false, 18, 8000, isfixWidth);
				else*/
                tableRow = addTableRow(target, factory, entry.getValue(), false, 18, 3500, isfixWidth);
                table.getContent().add(tableRow);
            }
        return table;
    }

    // add row
    private static Tr addTableRow(WordprocessingMLPackage wordMLPackage, ObjectFactory factory, LinkedHashMap<String, String> data, boolean bold, int fontSize,
                                  int width, int collColor, boolean background, boolean header, boolean isFixWidth) {
        Tr tableRow = factory.createTr();

        if (data == null || data.size() < 1)
            return null;
        int index = 0;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            addTableCell(wordMLPackage, factory, tableRow, entry.getValue(), bold, fontSize, width, entry.getKey().equals(collColor + "") ? COMMAND_COLOR : null, background, header, isFixWidth && index == 1);
            index++;
        }

        return tableRow;
    }


    private static void addTableCell(WordprocessingMLPackage wordMLPackage, ObjectFactory factory, Tr tableRow, String content, boolean bold, int fontSize,
                                     int width, String color, boolean background, boolean header, boolean isFixWidth) {
        Tc tableCell = factory.createTc();
        P paragraph = factory.createP();
        Text text;
        R run = factory.createR();
        String[] arrContent = content.split("\n");
        for (String str : arrContent) {
            text = factory.createText();
            text.setValue(str);
            Br br = factory.createBr();
            run.getContent().add(br);
            run.getContent().add(text);
        }
        // text.setValue(content);
        // run.getContent().add(text);
        paragraph.getContent().add(run);
        RPr runProperties = factory.createRPr();
        if (bold) {
            addBoldStyle(runProperties);
        }
//		if (width > 0 && isFixWidth) {
        setCellWidth(tableCell, width, isFixWidth);
//		}

        if (fontSize > 0) {
            setFontSize(runProperties, fontSize);
        }

        if (StringUtils.isNotEmpty(color)) {
            Color color1 = new Color();
            color1.setVal(color);
            runProperties.setColor(color1);
        }

        run.setRPr(runProperties);
        tableCell.getContent().add(paragraph);

        if (header) {
            TcPr tcPr = new TcPr();
            CTShd ctShd = new CTShd();
            ctShd.setFill("BFBFBF");
            tcPr.setShd(ctShd);
            tableCell.setTcPr(tcPr);
        } else if (background) {
            TcPr tcPr = new TcPr();
            CTShd ctShd = new CTShd();
            ctShd.setFill("99ccff");
            tcPr.setShd(ctShd);
            tableCell.setTcPr(tcPr);
        }
        tableRow.getContent().add(tableCell);
    }

    // add row
    private static Tr addTableRow(WordprocessingMLPackage wordMLPackage, ObjectFactory factory, LinkedHashMap<String, String> data, boolean bold, int fontSize,
                                  int width, boolean isFixWidth) {
        Tr tableRow = factory.createTr();

        if (data == null || data.size() < 1)
            return null;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            addTableCell(wordMLPackage, factory, tableRow, entry.getValue(), bold, fontSize, width, isFixWidth);
        }

        return tableRow;
    }

    // add cell
    private static void addTableCell(WordprocessingMLPackage wordMLPackage, ObjectFactory factory, Tr tableRow, String content, boolean bold, int fontSize,
                                     int width, boolean isFixWidth) {
        Tc tableCell = factory.createTc();
        P paragraph = factory.createP();
        Text text;
        R run = factory.createR();
        String[] arrContent = content.split("\n");
        for (String str : arrContent) {
            text = factory.createText();
            text.setValue(str);
            Br br = factory.createBr();
            run.getContent().add(br);
            run.getContent().add(text);
        }
        // text.setValue(content);
        // run.getContent().add(text);
        paragraph.getContent().add(run);
        RPr runProperties = factory.createRPr();
        if (bold) {
            addBoldStyle(runProperties);
        }
        setCellNoWrap(tableCell);
        if (width > 0 && isFixWidth) {
            setCellWidth(tableCell, width, isFixWidth);
        }

        if (fontSize > 0) {
            setFontSize(runProperties, fontSize);
        }

        run.setRPr(runProperties);
        tableCell.getContent().add(paragraph);
        tableRow.getContent().add(tableCell);
    }

    // create table border
    private static void addBorders(Tbl table) {
        table.setTblPr(new TblPr());
        CTBorder border = new CTBorder();
        border.setColor("auto");
        border.setSz(new BigInteger("4"));
        border.setSpace(new BigInteger("0"));
        border.setVal(STBorder.SINGLE);

        TblBorders borders = new TblBorders();
        borders.setBottom(border);
        borders.setLeft(border);
        borders.setRight(border);
        borders.setTop(border);
        borders.setInsideH(border);
        borders.setInsideV(border);
        table.getTblPr().setTblBorders(borders);
    }

    // set bold style
    private static void addBoldStyle(RPr runProperties) {
        BooleanDefaultTrue b = new BooleanDefaultTrue();
        b.setVal(true);
        runProperties.setB(b);

/*		Color color = new Color();
        color.setVal("blue");
		runProperties.setColor(color);*/
    }

    // set fontsize
    private static void setFontSize(RPr runProperties, int fontSize) {
        HpsMeasure size = new HpsMeasure();
        size.setVal(new BigInteger("" + fontSize));
        runProperties.setSz(size);
        runProperties.setSzCs(size);
    }

    // set cell widt
    private static void setCellWidth(Tc tableCell, int width) {
        TcPr tableCellProperties = new TcPr();
        TblWidth tableWidth = new TblWidth();
        tableWidth.setW(BigInteger.valueOf(width));

        tableCellProperties.setTcW(tableWidth);
        tableCell.setTcPr(tableCellProperties);
    }

    // set cell widt
    private static void setCellWidth(Tc tableCell, int width, boolean isfixWidth) {
        if (width > 0) {
            TcPr tableCellProperties = tableCell.getTcPr();
            if (tableCellProperties == null) {
                tableCellProperties = Context.getWmlObjectFactory().createTcPr();
                tableCell.setTcPr(tableCellProperties);
            }

            TblWidth tableWidth = Context.getWmlObjectFactory().createTblWidth();
            tableWidth.setType("dxa");
            tableWidth.setW(BigInteger.valueOf(4788));
            tableCellProperties.setTcW(tableWidth);

//			logger.info(tableCell.getTcPr().getTcW().getW());
        }
    }

    private static void setCellNoWrap(Tc tableCell) {
        TcPr tableCellProperties = tableCell.getTcPr();
        if (tableCellProperties == null) {
            tableCellProperties = new TcPr();
            tableCell.setTcPr(tableCellProperties);
        }
        BooleanDefaultTrue b = new BooleanDefaultTrue();
        b.setVal(true);
        tableCellProperties.setNoWrap(b);
    }

    // insert table after or before text start,
    static void insertTable(WordprocessingMLPackage pkg, boolean before, String postionText, Tbl table) throws Exception {
        Body b = pkg.getMainDocumentPart().getJaxbElement().getBody();
        int addPoint, count = 0;
        List<Integer> addPoints = new ArrayList<Integer>();
        for (Object o : b.getEGBlockLevelElts()) {
//			addPoint = -1;
            if (o instanceof P && getElementText(o).endsWith(postionText)) {
                addPoint = count + 1;
                addPoints.add(addPoint);
                // break;

            }
            count++;
        }
        int c = 0;
        int k;
        for (Integer i : addPoints) {
            k = i;
            if (k != -1) {
                if (before)
                    k = k + c - 1;
                b.getEGBlockLevelElts().add(k, table);
                c++;
            } else {
                // didn't find paragraph to insert
            }
        }
    }

    static String getElementText(Object jaxbElem) throws Exception {
        StringWriter sw = new StringWriter();
        TextUtils.extractText(jaxbElem, sw);
        return sw.toString();
    }

    // insert table after text end

    // find and replace text start

	/*private static XWPFDocument replaceText(File file, Map<String, String> findTexts) {
        File result = new File("D:\\template.docx");
		FileInputStream is = null;
		XWPFDocument doc = null;
		FileOutputStream os = null;
		int count = 0;
		try {
			for (Map.Entry<String, String> findText : findTexts.entrySet()) {
				if (count == 0)
					is = new FileInputStream(file);
				else
					is = new FileInputStream(result);
				count++;
				doc = new XWPFDocument(OPCPackage.open(is));
				for (XWPFParagraph p : doc.getParagraphs()) {
					String text = p.getParagraphText();
					if (text != null && text.contains(findText.getKey())) {
						text = text.replace(findText.getKey(), findText.getValue());
						//
						List<XWPFRun> runs = p.getRuns();
						for (int i = runs.size() - 1; i > 0; i--) {
							p.removeRun(i);
						}
						XWPFRun run = runs.get(0);
						run.setText(text, 0);
					}
					os = new FileOutputStream(result);
					doc.write(os);
					os.close();
				}

			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (os != null)
				try {
					os.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
		}
		return doc;
	}*/

    public static WordprocessingMLPackage replateTextInTable(String findText, String replaceText, WordprocessingMLPackage doc) {
        List<Object> tables = getAllElementFromObject(doc.getMainDocumentPart(), Tbl.class);

        Tbl tempTable = (Tbl) tables.get(0);
        List<Object> rows = getAllElementFromObject(tempTable, Tr.class);
        Tr templateRow = (Tr) rows.get(1);
        Tr workingRow = (Tr) XmlUtils.deepCopy(templateRow);
        List<Object> textElements = getAllElementFromObject(workingRow, Text.class);
        for (Object object : textElements) {
            Text text = (Text) object;
//			System.err.println(text.getValue());
            if (text.getValue().equals(findText)) {
                text.setValue(replaceText);

            }
        }
        tempTable.getContent().remove(templateRow);
        tempTable.getContent().add(workingRow);
        return doc;
    }

    public static WordprocessingMLPackage replateText(String findText, String replaceText, WordprocessingMLPackage doc) {
        MainDocumentPart documentPart = doc.getMainDocumentPart();
        HashMap<String, String> map = new HashMap<>();
        map.put(findText, replaceText);
        try {
            documentPart.variableReplace(map);
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            logger.error(e.getMessage(), e);
        } catch (Docx4JException e) {
            // TODO Auto-generated catch block
            logger.error(e.getMessage(), e);
        }

        return doc;
    }

    public static WordprocessingMLPackage findAndReplace(WordprocessingMLPackage doc, String toFind, String replacer) {
        /*String xpath = "//w:r[w:t[contains(text(),'" + toFind.replaceAll("}", "").replaceAll("\\{", "") + "')]]";
        MainDocumentPart documentPart = doc.getMainDocumentPart();
		List<Object> list = null;
		try {
			list = documentPart.getJAXBNodesViaXPath(xpath, true);
		} catch (JAXBException e) {
			logger.error(e.getMessage(), e);
		} catch (XPathBinderAssociationIsPartialException e) {
			logger.error(e.getMessage(), e);
		} catch (NullPointerException e) {
			logger.error(toFind + "\t" + replacer);
			logger.error(e.getMessage(), e);
			return doc;
		}

		for (Object obj : list) {
			org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
			((R) obj).getContent().clear();

			((R) obj).getContent().add(getFindingText(replacer));
		}

		return doc;*/
        List<Object> paragraphs = getAllElementFromObject(doc.getMainDocumentPart(), P.class);
        for (Object par : paragraphs) {
            P p = (P) par;
            List<Object> texts = getAllElementFromObject(p, Text.class);
            for (Object text : texts) {
                Text t = (Text) text;
                if (t.getValue().contains(toFind)) {
                    t.setValue(t.getValue().replace(toFind, replacer));
                }
            }

        }
        return doc;
    }

    public static WordprocessingMLPackage findAndReplaceNew(WordprocessingMLPackage doc, String toFind, String replacer) {
        String xpath = "//w:r[w:t[contains(text(),'auditFindings')]]";
        MainDocumentPart documentPart = doc.getMainDocumentPart();
        List<Object> list = null;
        Text text;
        try {
            list = documentPart.getJAXBNodesViaXPath(xpath, true);

            for (Object obj : list) {

                text = (Text) ((JAXBElement) obj).getValue();

                String textValue = text.getValue();

                if (textValue.contains(toFind)) {
//					textValue = "your own string";

                    text.setValue(replacer);
                }
//now you have a String - textValue... just modify it


            }
        } catch (JAXBException e) {
            logger.error(e.getMessage(), e);
        } catch (XPathBinderAssociationIsPartialException e) {
            logger.error(e.getMessage(), e);
        }

        return doc;
    }

    public static WordprocessingMLPackage findAndReplaceTC(WordprocessingMLPackage doc, String toFind, String replacer) {
        String xpath = "//w:r[w:t[contains(text(),'" + toFind + "')]]";
        MainDocumentPart documentPart = doc.getMainDocumentPart();
        List<Object> list = null;
        try {
            list = documentPart.getJAXBNodesViaXPath(xpath, false);
        } catch (JAXBException e) {
            logger.error(e.getMessage(), e);
        } catch (XPathBinderAssociationIsPartialException e) {
            logger.error(e.getMessage(), e);
        } /*catch (NullPointerException e) {
            logger.debug(e.getMessage(), e);
			logger.error(toFind + ":\t" + replacer);
			return doc;
		}*/

        for (Object obj : list) {
//			org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
            ((R) obj).getContent().clear();

            List<String> tcs = Splitter.on("\n").omitEmptyStrings().trimResults().splitToList(replacer);
            for (String tc : tcs) {
                ((R) obj).getContent().add(getFindingText(tc));
            }

        }

        return doc;
    }

    private static P getFindingText(String textReplace) {
        org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
        org.docx4j.wml.P p = factory.createP();
        org.docx4j.wml.Text t = factory.createText();
        t.setValue("	" + textReplace);
        org.docx4j.wml.R run = factory.createR();

        RPr runProperties = factory.createRPr();
        RFonts rFonts = new RFonts();
        rFonts.setAscii("Times New Roman");
        rFonts.setEastAsia("Times New Roman");
        rFonts.setCs("Times New Roman");
        rFonts.setHAnsi("Times New Roman");
        runProperties.setRFonts(rFonts);
        setFontSize(runProperties, 24);
        run.getContent().add(t);
        run.setRPr(runProperties);
        p.getContent().add(run);

        return p;
    }

    private static List<Object> getAllElementFromObject(Object obj, Class<?> toSearch) {
        List<Object> result = new ArrayList<>();
        if (obj instanceof JAXBElement)
            obj = ((JAXBElement<?>) obj).getValue();

        if (obj.getClass().equals(toSearch))
            result.add(obj);
        else if (obj instanceof ContentAccessor) {
            List<?> children = ((ContentAccessor) obj).getContent();
            for (Object child : children) {
                result.addAll(getAllElementFromObject(child, toSearch));
            }

        }
        return result;
    }

    int totalTime = 0;
    int totalTimeRollback = 0;

    public String getFileName(Action action) {
        String appName = Util.convertUTF8ToNoSign(getAppGroupName(action.getId())).replaceAll("\\?", "");
        String cr_number;
        if (StringUtils.isEmpty(action.getCrNumber())) {
            if (this.actionType.equals(Constant.ACTION_TYPE_KB_UCTT))
                cr_number = action.getTdCode();
            else
                cr_number = action.getCrNumber();
        } else {
            cr_number = action.getCrNumber();
        }
        String cr = cr_number.split("_")[cr_number.split("_").length - 1];
        String date_time2 = new SimpleDateFormat("ddMMyyyy").format(action.getCreatedTime());

        String fileName = prefixName + appName + "_" + cr + "_" + date_time2;

        return fileName;
    }

    private String fileName(Action action, Boolean isRollback, Integer kbGroup, String crNumber) {
        this.actionType = action.getActionType();

        if (this.actionType.equals(Constant.ACTION_TYPE_CR_NORMAL)) {
            prefixName = "MOP.CNTT.";
        } else if (this.actionType.equals(Constant.ACTION_TYPE_CR_UCTT)) {
            prefixName = "MOP.CNTT.";
        } else if (this.actionType.equals(Constant.ACTION_TYPE_KB_UCTT)) {
            prefixName = "KB.UCTT.";
        }

        groupActionLabels = new HashMap<>();
        indexMap = new HashMap<>();
        tdIndex = 1;
        rbIndex = 1;

        totalTime = 0;
        totalTimeRollback = 0;
        this.action = action;
        long actionId = action.getId();
        /*String cr_name = action.getCrName();
        String user = action.getCreatedBy();
        String date_time = new SimpleDateFormat("dd/MM/yyyy").format(action.getCreatedTime());*/
        String cr_number;
        if (StringUtils.isEmpty(crNumber)) {
            if (this.actionType.equals(Constant.ACTION_TYPE_KB_UCTT))
                cr_number = action.getTdCode();
            else
                cr_number = action.getCrNumber();
        } else {
            cr_number = crNumber;
        }
        String date_time2 = new SimpleDateFormat("ddMMyyyy").format(action.getCreatedTime());
        String cr = cr_number.split("_")[cr_number.split("_").length - 1];
//        String reason = action.getReason();
        String appName = Util.convertUTF8ToNoSign(getAppGroupName(actionId)).replaceAll("\\?", "");
        String mopAction = prefixName + appName + "_" + cr + "_" + date_time2 + "_tacdong_" + (kbGroup == null ? 1 : kbGroup) + ".docx";
        String mopRollBack = prefixName + appName + "_" + cr + "_" + date_time2 + "_rollback_" + (kbGroup == null ? 1 : kbGroup) + ".docx";

        if (isRollback)
            return mopRollBack;
        else
            return mopAction;
    }

    public String genericDT(Action action, String template, String templateRollBack, String source, MopFileResult fileResult, String crNumber) {
        this.actionType = action.getActionType();

        if (this.actionType.equals(Constant.ACTION_TYPE_CR_NORMAL)) {
            prefixName = "MOP.CNTT.";
        } else if (this.actionType.equals(Constant.ACTION_TYPE_CR_UCTT)) {
            prefixName = "MOP.CNTT.";
        } else if (this.actionType.equals(Constant.ACTION_TYPE_KB_UCTT)) {
            prefixName = "KB.UCTT.";
        }

        groupActionLabels = new HashMap<>();
        indexMap = new HashMap<>();
        tdIndex = 1;
        rbIndex = 1;

        totalTime = 0;
        totalTimeRollback = 0;
        this.action = action;
        long actionId = action.getId();
        String cr_name = action.getCrName();
        String user = action.getCreatedBy();
        String date_time = new SimpleDateFormat("dd/MM/yyyy").format(action.getCreatedTime());
        String cr_number;
        if (StringUtils.isEmpty(crNumber)) {
            if (this.actionType.equals(Constant.ACTION_TYPE_KB_UCTT))
                cr_number = action.getTdCode();
            else
                cr_number = action.getCrNumber();
        } else {
            cr_number = crNumber;
        }
        String date_time2 = new SimpleDateFormat("ddMMyyyy").format(action.getCreatedTime());
        String cr = cr_number.split("_")[cr_number.split("_").length - 1];
        String reason = action.getReason();
        String appName = Util.convertUTF8ToNoSign(getAppGroupName(actionId)).replaceAll("\\?", "").replaceAll("/", "");
        String mopAction = prefixName + appName + "_" + cr + "_" + date_time2 + "_tacdong_" + (kbGroup == null ? 1 : kbGroup) + ".docx";
        String mopRollBack = prefixName + appName + "_" + cr + "_" + date_time2 + "_rollback_" + (kbGroup == null ? 1 : kbGroup) + ".docx";

        if (fileResult != null) {
            fileResult.setMopFile(mopAction);
            fileResult.setMopRollbackFile(mopRollBack);
        }

        TestCaseServiceImpl testCaseServiceImpl = new TestCaseServiceImpl();
//        Map<String, String> mapTestCase = testCaseServiceImpl.getMapTestCaseData(actionId);
        Map<String, String> mapTestCase = new HashMap<>();
        List<TestCase> objects = testCaseServiceImpl.getMapTestCaseData(actionId);

        if (objects != null) {
            int i = 0;
            for (TestCase testCase : objects) {
                int tesCaseType = testCase.getTestcaseType();
                switch (tesCaseType) {
                    case 1:
                        mapTestCase.put("" + i++, "- " + MessageUtil.getResourceBundleMessage("department") + " " + testCase.getUserPerform() + " " + MessageUtil.getResourceBundleMessage("executing.test.follow.testcase") + ": " + FilenameUtils.getName(testCase.getFileName()));
                        break;
                    case 2:
                        mapTestCase.put("" + i++, "- " + MessageUtil.getResourceBundleMessage("department") + " " + testCase.getUserPerform() + " " + MessageUtil.getResourceBundleMessage("executing.test.follow.testcase") + ": " + FilenameUtils.getName(testCase.getFileName()));
                        break;
                    case 3:
                        mapTestCase.put("" + i++, "- " + MessageUtil.getResourceBundleMessage("department") + " " + testCase.getUserPerform() + " " + MessageUtil.getResourceBundleMessage("executing.test.follow.testcase") + ": " + FilenameUtils.getName(testCase.getFileName()));
                        break;
                    case 4:
                        mapTestCase.put("" + i++, "- " + MessageUtil.getResourceBundleMessage("department") + " " + testCase.getUserPerform() + " " + MessageUtil.getResourceBundleMessage("executing.test.follow.testcase") + ": " + FilenameUtils.getName(testCase.getFileName()));
                        break;
                    default:
                        break;
                }
            }
        }

        String testCase = "";
        for (String dv : mapTestCase.values()) {
            testCase += "\r\n" + dv + "\r\n";
        }

        File file = new File(source);
        if (!file.exists())
            file.mkdirs();

        // build du lieu bang node mang
        LinkedHashMap<String, LinkedHashMap<String, String>> dataIpTable = getTabelNode(actionId);

        // build du lieu check list truoc tac dong
        LinkedHashMap<String, LinkedHashMap<String, String>> dataCheckList = getTabelCheckList(actionId, cr_number);

        // build du lieu check list truoc tac dong
        LinkedHashMap<String, LinkedHashMap<String, String>> dataCheckListRollBack = getTabelCheckListRollBack(actionId, cr_number);

        // build du lieu check list truoc tac dong
        LinkedHashMap<String, LinkedHashMap<String, String>> dataCheckListLast = getTabelCheckListLast(actionId, cr_number);

        // build du lieu check list kpi
        LinkedHashMap<String, LinkedHashMap<String, String>> dataCheckListKpi = getTabelCheckListKpi(actionId);

        // build du lieu bang stop
        LinkedHashMap<String, LinkedHashMap<String, String>> dataAfterCheckStatus = getTableCustom(Constant.SUB_STEP_CHECK_STATUS, actionId, mopRollBack, cr, date_time2);

        LinkedHashMap<String, LinkedHashMap<String, String>> dataAfterCheckStatusRb = getTableCustomRb(Constant.ROLLBACK_STEP_CHECK_STATUS, actionId, mopRollBack, cr, date_time2);

        indexMap.put(Constant.SUB_STEP_STOP_APP, tdIndex++);
        // build du lieu bang stop
        LinkedHashMap<String, LinkedHashMap<String, String>> dataStop = getTableStopApp(actionId, mopRollBack);

        // build du lieu bang stop
        LinkedHashMap<String, LinkedHashMap<String, String>> dataAfterStop = getTableCustom(Constant.SUB_STEP_STOP_APP, actionId, mopRollBack, cr, date_time2);

        indexMap.put(Constant.SUB_STEP_BACKUP_APP, tdIndex++);
        // build du lieu bang backup code
        LinkedHashMap<String, LinkedHashMap<String, String>> dataBackupCode = getTableBackupCode(actionId, date_time2, mopRollBack, cr);

        LinkedHashMap<String, LinkedHashMap<String, String>> dataAfterBackupApp = getTableCustom(Constant.SUB_STEP_BACKUP_APP, actionId, mopRollBack, cr, date_time2);

        indexMap.put(Constant.ROLLBACK_STEP_STOP_APP, rbIndex++);
        // build du lieu bang stop
        LinkedHashMap<String, LinkedHashMap<String, String>> dataStopRb = getTableStopAppRb(actionId, mopRollBack);
        LinkedHashMap<String, LinkedHashMap<String, String>> dataAfterStopRb = getTableCustomRb(Constant.ROLLBACK_STEP_STOP_APP, actionId, mopRollBack, cr, date_time2);

        indexMap.put(Constant.SUB_STEP_BACKUP_DB, tdIndex++);
        // build du lieu bang backup database
        LinkedHashMap<String, LinkedHashMap<String, String>> dataBackupDb = getTableBackupDb(actionId, mopRollBack);

        LinkedHashMap<String, LinkedHashMap<String, String>> dataAfterBackupDb = getTableCustom(Constant.SUB_STEP_BACKUP_DB, actionId, mopRollBack, cr, date_time2);

        indexMap.put(Constant.SUB_STEP_UPCODE, tdIndex++);
        // build du lieu bang up code
        LinkedHashMap<String, LinkedHashMap<String, String>> dataUpCode = getTableUpCode(actionId, cr_number, mopRollBack, cr);

        LinkedHashMap<String, LinkedHashMap<String, String>> dataAfterUpCode = getTableCustom(Constant.SUB_STEP_UPCODE, actionId, mopRollBack, cr, date_time2);


        indexMap.put(Constant.SUB_STEP_TD_DB, tdIndex++);
        // build du lieu thay doi cau hinh db
        LinkedHashMap<String, LinkedHashMap<String, String>> dataConfigDb = getTableConfigDb(actionId, mopRollBack);

        LinkedHashMap<String, LinkedHashMap<String, String>> dataAfterConfigDb = getTableCustom(Constant.SUB_STEP_TD_DB, actionId, mopRollBack, cr, date_time2);

        indexMap.put(Constant.ROLLBACK_STEP_SOURCE_CODE, rbIndex++);
        LinkedHashMap<String, LinkedHashMap<String, String>> dataRollbackCode = getTableRollbackCode(actionId, date_time2, cr);
        LinkedHashMap<String, LinkedHashMap<String, String>> dataAfterRollbackCode = getTableCustomRb(Constant.ROLLBACK_STEP_SOURCE_CODE, actionId, mopRollBack, cr, date_time2);

        indexMap.put(Constant.ROLLBACK_STEP_DB, rbIndex++);
        // build du lieu bang rollback database
        LinkedHashMap<String, LinkedHashMap<String, String>> dataRollbackDb = getTableRollBackDb(actionId);
        LinkedHashMap<String, LinkedHashMap<String, String>> dataAfterRollbackDb = getTableCustomRb(Constant.ROLLBACK_STEP_DB, actionId, mopRollBack, cr, date_time2);

        // buid du lieu clear cache
        indexMap.put(Constant.SUB_STEP_CLEARCACHE, tdIndex++);
        indexMap.put(Constant.ROLLBACK_STEP_CLEARCACHE, rbIndex++);
        LinkedHashMap<String, LinkedHashMap<String, String>> dataClearCache = getTableClearCache(actionId);

        LinkedHashMap<String, LinkedHashMap<String, String>> dataAfterClearCache = getTableCustom(Constant.SUB_STEP_CLEARCACHE, actionId, mopRollBack, cr, date_time2);
        LinkedHashMap<String, LinkedHashMap<String, String>> dataAfterClearCacheRb = getTableCustomRb(Constant.ROLLBACK_STEP_CLEARCACHE, actionId, mopRollBack, cr, date_time2);

        indexMap.put(Constant.SUB_STEP_RESTART_APP, tdIndex++);
        // build du lieu bang Restart
        MutableInt index = new MutableInt();
        LinkedHashMap<String, LinkedHashMap<String, String>> dataRestart = getTableRestart(actionId, mopRollBack, cr, date_time2, index);
        LinkedHashMap<String, LinkedHashMap<String, String>> dataRestartCmd = getTableRestartCmd(actionId, mopRollBack, cr, date_time2, index);

        LinkedHashMap<String, LinkedHashMap<String, String>> dataAfterRestart = getTableCustom(Constant.SUB_STEP_RESTART_APP, actionId, mopRollBack, cr, date_time2);

        indexMap.put(Constant.SUB_STEP_START_APP, tdIndex++);
        // build du lieu bang start
        LinkedHashMap<String, LinkedHashMap<String, String>> dataStart = getTableStart(actionId, mopRollBack, cr, date_time2);

        LinkedHashMap<String, LinkedHashMap<String, String>> dataUpCodeStopStart = getTableUpCodeStopStart(actionId, cr_number, mopRollBack, cr, date_time2);

        LinkedHashMap<String, LinkedHashMap<String, String>> dataAfterStart = getTableCustom(Constant.SUB_STEP_START_APP, actionId, mopRollBack, cr, date_time2);

        // build du lieu bang RestartRb
        index = new MutableInt();
        indexMap.put(Constant.ROLLBACK_STEP_RESTART_APP, rbIndex++);
        LinkedHashMap<String, LinkedHashMap<String, String>> dataRestartRb = getTableRestartRb(actionId, mopRollBack, cr, date_time2, index);
        LinkedHashMap<String, LinkedHashMap<String, String>> dataRestartCmdRb = getTableRestartCmdRb(actionId, mopRollBack, cr, date_time2, index);

        LinkedHashMap<String, LinkedHashMap<String, String>> dataAfterRestartRb = getTableCustomRb(Constant.ROLLBACK_STEP_RESTART_APP, actionId, mopRollBack, cr, date_time2);

        indexMap.put(Constant.ROLLBACK_STEP_START_APP, rbIndex++);
        // build du lieu bang startrb
        LinkedHashMap<String, LinkedHashMap<String, String>> dataStartRb = getTableStartRb(actionId, mopRollBack, cr, date_time2);
        LinkedHashMap<String, LinkedHashMap<String, String>> dataAfterStartRb = getTableCustomRb(Constant.ROLLBACK_STEP_START_APP, actionId, mopRollBack, cr, date_time2);

//		LinkedHashMap<String, LinkedHashMap<String, String>> dataUpCodeStopStartRb = getTableUpCodeStopStartRb(actionId, cr_number, mopRollBack, cr, date_time2);

        LinkedHashMap<String, LinkedHashMap<String, String>> dataCheckLast = getTabelCheckLast(actionId, cr_number, mopRollBack);
        WordprocessingMLPackage do1c;
        WordprocessingMLPackage do2c;
        String fileName = prefixName + appName + "_" + cr + "_" + date_time2;
        // test
        try {
            do1c = WordprocessingMLPackage.load(new File(template));

//			do2c = WordprocessingMLPackage.load(new File(templateRollBack));
            replateTextInTable("{CR_NAME}", cr_name, do1c);
            replateTextInTable("{USER}", user, do1c);
            replateTextInTable("{CR_NUMBER}", cr_number, do1c);
            replateTextInTable("{REASON}", reason, do1c);
            replateTextInTable("{APP_NAME}", appName, do1c);
            replateTextInTable("{DATE_TIME}", date_time, do1c);
            replateTextInTable("{TOTAL_TIME}", totalTime + "", do1c);
            findAndReplace(do1c, "{ROLLBACK}", mopRollBack);
            findAndReplace(do1c, "{TEMP}", prefixName + appName + "_" + cr);
//            if (kbGroup == 1) {
            findAndReplaceTC(do1c, "{SUB MOP}", impactDesc);
//            }
            findAndReplaceTC(do1c, "{TESTCASE}", testCase);
            findAndReplace(do1c, "{CHECKLISTDB}", appName + "/Checklist DB");
            if (this.actionType.equals(Constant.ACTION_TYPE_KB_UCTT)) {
                findAndReplace(do1c, "{MOP}", "KB");
                findAndReplace(do1c, "{CHANGE_HEADER}", MessageUtil.getResourceBundleMessage("doc.header.execute.mop"));
            } else {
                findAndReplace(do1c, "{MOP}", "MOP");
                findAndReplace(do1c, "{CHANGE_HEADER}", MessageUtil.getResourceBundleMessage("doc.header.execute.change"));
            }

            do2c = WordprocessingMLPackage.load(new File(templateRollBack));
            replateTextInTable("{CR_NAME}", cr_name, do2c);
            replateTextInTable("{USER}", user, do2c);
            replateTextInTable("{CR_NUMBER}", cr_number, do2c);
            replateTextInTable("{REASON}", reason, do2c);
            replateTextInTable("{APP_NAME}", appName, do2c);
            replateTextInTable("{DATE_TIME}", date_time, do2c);
            replateTextInTable("{TOTAL_TIME}", totalTimeRollback + "", do2c);
//            if (kbGroup == 1) {
            findAndReplaceTC(do2c, "{SUB MOP ROLLBACK}", rollbackDesc);
//            }
            findAndReplace(do2c, "{TESTCASE}", testCase);

            findAndReplace(do2c, "{TEMP}", prefixName + appName + "_" + cr);
            findAndReplace(do2c, "{CHECKLISTDB}", appName + "/Checklist DB");

            if (this.actionType.equals(Constant.ACTION_TYPE_KB_UCTT)) {
                findAndReplace(do2c, "{MOP}", "KB");
                findAndReplace(do2c, "{CHANGE_HEADER}", MessageUtil.getResourceBundleMessage("execute.script"));
            } else {
                findAndReplace(do2c, "{MOP}", "MOP");
                findAndReplace(do2c, "{CHANGE_HEADER}", MessageUtil.getResourceBundleMessage("execute.edit"));
            }

            Tbl tableNode = DocxUtil.createTable(do1c, DocxUtil.columnNetworkNode, dataIpTable, -1, false, false);
            Tbl tableNodeRb = DocxUtil.createTable(do2c, DocxUtil.columnNetworkNode, dataIpTable, -1, false, false);
            Tbl tableCheckList = DocxUtil.createTable(do1c, DocxUtil.columnCheckList, dataCheckList, -1, false, false);
            // Tbl tableCheckListRb = DocxUtil.createTable(do2c,
            // DocxUtil.columnCheckList, dataCheckList);
            // rollback
            Tbl tableCheckListRollBack = DocxUtil.createTable(do2c, DocxUtil.columnCheckList, dataCheckListRollBack, -1, false, false);

            Tbl tableCheckListLast = DocxUtil.createTable(do1c, DocxUtil.columnCheckList, dataCheckListLast, -1, false, false);
            Tbl tableCheckListLastRb = DocxUtil.createTable(do2c, DocxUtil.columnCheckList, dataCheckListLast, -1, false, false);

            Tbl tableAfterCheckStatus = dataAfterCheckStatus == null ? null : DocxUtil.createTable(do1c, DocxUtil.columnStopAppTomcat, dataAfterCheckStatus, 4, true, false);
            Tbl tableAfterCheckStatuRb = dataAfterCheckStatusRb == null ? null : DocxUtil.createTable(do1c, DocxUtil.columnStopAppTomcat, dataAfterCheckStatusRb, 4, true, false);


            Tbl tableStop = DocxUtil.createTable(do1c, DocxUtil.columnStopAppTomcat, dataStop, 4, true, false);
            Tbl tableStopRb = DocxUtil.createTable(do2c, DocxUtil.columnStopAppTomcat, dataStopRb, 4, true, false);
            Tbl tableAfterStop = dataAfterStop == null ? null : DocxUtil.createTable(do1c, DocxUtil.columnStopAppTomcat, dataAfterStop, 4, true, false);
            Tbl tableAfterStopRb = dataAfterStopRb == null ? null : DocxUtil.createTable(do1c, DocxUtil.columnStopAppTomcat, dataAfterStopRb, 4, true, false);

            //
            Tbl tableBkDb = DocxUtil.createTable(do1c, DocxUtil.columnBackupDatabase, dataBackupDb, -1, false, true);
            Tbl tableAfterBkDb = dataAfterBackupDb == null ? null : DocxUtil.createTable(do1c, DocxUtil.columnStopAppTomcat, dataAfterBackupDb, 4, true, false);

            Tbl tableBkCode = DocxUtil.createTable(do1c, DocxUtil.columnStopAppTomcat, dataBackupCode, 4, true, false);
            Tbl tableAfterBkCode = dataAfterBackupApp == null ? null : DocxUtil.createTable(do1c, DocxUtil.columnStopAppTomcat, dataAfterBackupApp, 4, true, false);

            Tbl tableConfigDb = DocxUtil.createTable(do1c, DocxUtil.columnChangeConfigDB, dataConfigDb, -1, false, true);
            Tbl tableAfterConfigDb = dataAfterConfigDb == null ? null : DocxUtil.createTable(do1c, DocxUtil.columnStopAppTomcat, dataAfterConfigDb, 4, true, false);
            Tbl tableAfterRollbackDb = dataAfterRollbackDb == null ? null : DocxUtil.createTable(do1c, DocxUtil.columnStopAppTomcat, dataAfterRollbackDb, 4, true, false);

            Tbl tableRollbackDb = DocxUtil.createTable(do2c, DocxUtil.columnChangeConfigDB, dataRollbackDb, -1, false, true);
            Tbl tableUpCode = DocxUtil.createTable(do1c, DocxUtil.columnUpcode, dataUpCode, 4, true, false);
            Tbl tableAfterUpCode = dataAfterUpCode == null ? null : DocxUtil.createTable(do1c, DocxUtil.columnStopAppTomcat, dataAfterUpCode, 4, true, false);
            Tbl tableAfterRollbackCode = dataAfterRollbackCode == null ? null : DocxUtil.createTable(do1c, DocxUtil.columnStopAppTomcat, dataAfterRollbackCode, 4, true, false);

            Tbl tableRollbackCode = DocxUtil.createTable(do2c, DocxUtil.columnUpcode, dataRollbackCode, 4, true, false);
            Tbl tableClearCache = DocxUtil.createTable(do1c, DocxUtil.columnUpcode, dataClearCache, 4, true, false);
            Tbl tableAfterClearCache = dataAfterClearCache == null ? null : DocxUtil.createTable(do1c, DocxUtil.columnStopAppTomcat, dataAfterClearCache, 4, true, false);
//			Tbl tableAfterClearCache = dataAfter == null ? null : DocxUtil.createTable(do1c, DocxUtil.columnStopAppTomcat, dataAfterClearCache, 4, true, false);
            Tbl tableClearCacheRb = DocxUtil.createTable(do2c, DocxUtil.columnUpcode, dataClearCache, 4, true, false);

            Tbl tableAfterClearCacheRb = dataAfterClearCacheRb == null ? null : DocxUtil.createTable(do1c, DocxUtil.columnStopAppTomcat, dataAfterClearCacheRb, 4, true, false);

            Tbl tableRestart = DocxUtil.createTable(do1c, DocxUtil.columnUpcode, dataRestart, 4, true, false);
            Tbl tableRestartCmd = DocxUtil.createTable(do1c, DocxUtil.columnUpcode, dataRestartCmd, 4, true, false);
            Tbl tableAfterRestart = dataAfterRestart == null ? null : DocxUtil.createTable(do1c, DocxUtil.columnStopAppTomcat, dataAfterRestart, 4, true, false);
            Tbl tableAfterRestartRb = dataAfterRestartRb == null ? null : DocxUtil.createTable(do1c, DocxUtil.columnStopAppTomcat, dataAfterRestartRb, 4, true, false);

            Tbl tableStart = DocxUtil.createTable(do1c, DocxUtil.columnUpcode, dataStart, 4, true, false);
            Tbl tableUpcodeStopStart = dataUpCodeStopStart.isEmpty() ? null : DocxUtil.createTable(do1c, DocxUtil.columnUpcode, dataUpCodeStopStart, 4, true, false);
            Tbl tableAfterStart = dataAfterStart == null ? null : DocxUtil.createTable(do1c, DocxUtil.columnStopAppTomcat, dataAfterStart, 4, true, false);
            Tbl tableAfterStartRb = dataAfterStartRb == null ? null : DocxUtil.createTable(do1c, DocxUtil.columnStopAppTomcat, dataAfterStartRb, 4, true, false);

            Tbl tableRestartRb = DocxUtil.createTable(do2c, DocxUtil.columnUpcode, dataRestartRb, 4, true, false);
            Tbl tableRestartCmdRb = DocxUtil.createTable(do2c, DocxUtil.columnUpcode, dataRestartCmdRb, 4, true, false);
            Tbl tableStartRb = DocxUtil.createTable(do2c, DocxUtil.columnUpcode, dataStartRb, 4, true, false);
            Tbl tableCheckLast = DocxUtil.createTable(do1c, DocxUtil.columnCheckList, dataCheckLast, -1, false, false);
            Tbl tableCheckKpi = DocxUtil.createTable(do1c, DocxUtil.columnCheckKpi, dataCheckListKpi, true);
            Tbl tableCheckLastRb = DocxUtil.createTable(do2c, DocxUtil.columnCheckList, dataCheckLast, -1, false, false);
            Tbl tableCheckKpiRb = DocxUtil.createTable(do2c, DocxUtil.columnCheckKpi, dataCheckListKpi, true);
            insertTable(do1c, false, posNetworkNode, tableNode);
            // findAndReplace(do1c, posNetworkNode, "");
            insertTable(do1c, false, posCheckStatusTomcat, tableCheckList);
            // findAndReplace(do1c, posCheckStatusTomcat, "");
            // insertTable(do1c, true, posCheckList, tableCheckList);
            int tdIndex = 1;
            if (tableAfterCheckStatus != null) {
                insertTable(do1c, false, afterCheckStatus, tableAfterCheckStatus);
                findAndReplace(do1c, afterCheckStatus, "2.2." + tdIndex++ + ". " + prefixName + appName + "_" + cr + " - " + groupActionLabels.get(Constant.SUB_STEP_CHECK_STATUS));
            } else {
                findAndReplace(do1c, afterCheckStatus, "");
            }

            findAndReplace(do1c, tdStopApp, "2.2." + tdIndex++ + ". " + prefixName + appName + "_" + cr + " - " + labelStopApp);

            insertTable(do1c, false, posStopAppTomcat, tableStop);
            if (tableAfterStop != null) {
                insertTable(do1c, false, afterStopApp, tableAfterStop);
                findAndReplace(do1c, afterStopApp, "2.2." + tdIndex++ + ". " + prefixName + appName + "_" + cr + " - " + groupActionLabels.get(Constant.SUB_STEP_STOP_APP));
            } else {
                findAndReplace(do1c, afterStopApp, "");
            }

            findAndReplace(do1c, tdBackupApp, "2.2." + tdIndex++ + ". " + prefixName + appName + "_" + cr + " - " + labelBackupApp);

            // findAndReplace(do1c, posStopAppTomcat, "");
            insertTable(do1c, false, posBackupCode, tableBkCode);

            if (tableAfterBkCode != null) {
                insertTable(do1c, false, afterBackupApp, tableAfterBkCode);
                findAndReplace(do1c, afterBackupApp, "2.2." + tdIndex++ + ". " + prefixName + appName + "_" + cr + " - " + groupActionLabels.get(Constant.SUB_STEP_BACKUP_APP));
            } else {
                findAndReplace(do1c, afterBackupApp, "");
            }

            findAndReplace(do1c, tdBackupDb, "2.2." + tdIndex++ + ". " + prefixName + appName + "_" + cr + " - " + labelBackupDb);
            // findAndReplace(do1c, posBackupCode, "");
            insertTable(do1c, false, posBackupDB, tableBkDb);
            // findAndReplace(do1c, posBackupDB, "");

            if (tableAfterBkDb != null) {
                insertTable(do1c, false, afterBackupDb, tableAfterBkDb);
                findAndReplace(do1c, afterBackupDb, "2.2." + tdIndex++ + ". " + prefixName + appName + "_" + cr + " - " + groupActionLabels.get(Constant.SUB_STEP_BACKUP_DB));
            } else {
                findAndReplace(do1c, afterBackupDb, "");
            }

            findAndReplace(do1c, tdUpcode, "2.2." + tdIndex++ + ". " + prefixName + appName + "_" + cr + " - " + labelUpcode);
            //
            insertTable(do1c, false, posUpcode, tableUpCode);
            // findAndReplace(do1c, posUpcode, "");
            if (tableAfterUpCode != null) {
                insertTable(do1c, false, afterUpcode, tableAfterUpCode);
                findAndReplace(do1c, afterUpcode, "2.2." + tdIndex++ + ". " + prefixName + appName + "_" + cr + " - " + groupActionLabels.get(Constant.SUB_STEP_UPCODE));
            } else {
                findAndReplace(do1c, afterUpcode, "");
            }

            findAndReplace(do1c, tdTdDb, "2.2." + tdIndex++ + ". " + prefixName + appName + "_" + cr + " - " + labelTdDb);
            //
            insertTable(do1c, false, posChangeConfigDB, tableConfigDb);
            // findAndReplace(do1c, posChangeConfigDB, "");

            if (tableAfterConfigDb != null) {
                insertTable(do1c, false, afterTdDb, tableAfterConfigDb);
                findAndReplace(do1c, afterTdDb, "2.2." + tdIndex++ + ". " + prefixName + appName + "_" + cr + " - " + groupActionLabels.get(Constant.SUB_STEP_TD_DB));
            } else {
                findAndReplace(do1c, afterTdDb, "");
            }

            findAndReplace(do1c, tdClearCache, "2.2." + tdIndex++ + ". " + prefixName + appName + "_" + cr + " - " + labelClearCache);
            insertTable(do1c, false, posClearCache, tableClearCache);

            if (tableAfterClearCache != null) {
                insertTable(do1c, false, afterClearCache, tableAfterClearCache);
                findAndReplace(do1c, afterClearCache, "2.2." + tdIndex++ + ". " + prefixName + appName + "_" + cr + " - " + groupActionLabels.get(Constant.SUB_STEP_CLEARCACHE));
            } else {
                findAndReplace(do1c, afterClearCache, "");
            }
            // findAndReplace(do1c, posClearCache, "");
            // insertTable(do1c, true, posRollBackCode, tableRollbackCode);
            // insertTable(do1c, true, posRollBackData, tableRollbackDb);
            findAndReplace(do1c, tdRestartApp, "2.2." + tdIndex++ + ". " + prefixName + appName + "_" + cr + " - " + labelRestartApp);
            insertTable(do1c, false, posRestartApp, tableRestart);
            insertTable(do1c, false, posRestartAppCmd, tableRestartCmd);

            if (tableAfterRestart != null) {
                insertTable(do1c, false, afterRestartApp, tableAfterRestart);
                findAndReplace(do1c, afterRestartApp, "2.2." + tdIndex++ + ". " + prefixName + appName + "_" + cr + " - " + groupActionLabels.get(Constant.SUB_STEP_RESTART_APP));
            } else {
                findAndReplace(do1c, afterRestartApp, "");
            }
            findAndReplace(do1c, tdStartApp, "2.2." + tdIndex++ + ". " + prefixName + appName + "_" + cr + " - " + labelStartApp);
            // findAndReplace(do1c, posRestartApp, "");
            insertTable(do1c, false, posStartApp, tableStart);
            if (tableUpcodeStopStart != null) {
                insertTable(do1c, false, posUpcodeStopStartApp, tableUpcodeStopStart);
            } else {
                findAndReplace(do1c, posUpcodeStopStartApp, "");
            }

            if (tableAfterStart != null) {
                insertTable(do1c, false, afterStartApp, tableAfterStart);
                findAndReplace(do1c, afterStartApp, "2.2." + tdIndex + ". " + prefixName + appName + "_" + cr + " - " + groupActionLabels.get(Constant.SUB_STEP_START_APP));
            } else {
                findAndReplace(do1c, afterStartApp, "");
            }

            // findAndReplace(do1c, posStartApp, "");
            insertTable(do1c, false, posCheckList, tableCheckListLast);
            // findAndReplace(do1c, posCheckList, "");
            insertTable(do1c, false, posCheckLast, tableCheckLast);
            // findAndReplace(do1c, posCheckLast, "");
            insertTable(do1c, false, posCheckListDb, tableCheckKpi);
            // findAndReplace(do1c, posCheckListDb, "");
            do1c.save(new File(source + File.separator + mopAction));

            // /////////////////////////////////////////////////////
            insertTable(do2c, false, posNetworkNode, tableNodeRb);
            // findAndReplace(do2c, posNetworkNode, "");
            insertTable(do2c, false, posCheckStatusTomcat, tableCheckListRollBack);
            // findAndReplace(do2c, posCheckStatusTomcat, "");
            // insertTable(do1c, true, posCheckList, tableCheckList);

            if (tableAfterCheckStatuRb != null) {
                insertTable(do2c, false, afterRollbackCheckStatus, tableAfterCheckStatuRb);
                findAndReplace(do2c, afterRollbackCheckStatus, "2.2." + indexMap.get(Constant.ROLLBACK_CUSTOM_STEP_CHECK_STATUS) + ". " + prefixName + appName + "_" + cr + " - " + groupActionLabels.get(Constant.ROLLBACK_STEP_CHECK_STATUS));
            } else {
                findAndReplace(do2c, afterRollbackCheckStatus, "");
            }

            findAndReplace(do2c, rollbackStopApp, "2.2." + indexMap.get(Constant.ROLLBACK_STEP_STOP_APP) + ". " + prefixName + appName + "_" + cr + " - " + labelRollbackStopApp);
            insertTable(do2c, false, posStopAppTomcat, tableStopRb);
            if (tableAfterStopRb != null) {
                insertTable(do2c, false, afterRollbackStopApp, tableAfterStopRb);
                findAndReplace(do2c, afterRollbackStopApp, "2.2." + indexMap.get(Constant.ROLLBACK_CUSTOM_STEP_STOP_APP) + ". " + prefixName + appName + "_" + cr + " - " + groupActionLabels.get(Constant.ROLLBACK_STEP_STOP_APP));
            } else {
                findAndReplace(do2c, afterRollbackStopApp, "");
            }
            // findAndReplace(do2c, posStopAppTomcat, "");

            // findAndReplace(do2c, posClearCache, "");
            findAndReplace(do2c, rollbackCode, "2.2." + indexMap.get(Constant.ROLLBACK_STEP_SOURCE_CODE) + ". " + prefixName + appName + "_" + cr + " - " + labelRollbackCode);
            insertTable(do2c, false, posRollBackCode, tableRollbackCode);
            if (tableAfterRollbackCode != null) {
                insertTable(do2c, false, afterRollbackCode, tableAfterRollbackCode);
                findAndReplace(do2c, afterRollbackCode, "2.2." + indexMap.get(Constant.ROLLBACK_CUSTOM_STEP_UPCODE) + ". " + prefixName + appName + "_" + cr + " - " + groupActionLabels.get(Constant.ROLLBACK_STEP_SOURCE_CODE));
            } else {
                findAndReplace(do2c, afterRollbackCode, "");
            }

            // findAndReplace(do2c, posRollBackCode, "");
            findAndReplace(do2c, rollbackDb, "2.2." + indexMap.get(Constant.ROLLBACK_STEP_DB) + ". " + prefixName + appName + "_" + cr + " - " + labelRollbackDb);
            insertTable(do2c, false, posRollBackData, tableRollbackDb);
            if (tableAfterRollbackDb != null) {
                insertTable(do2c, false, afterRollbackDb, tableAfterRollbackDb);
                findAndReplace(do2c, afterRollbackDb, "2.2." + indexMap.get(Constant.ROLLBACK_CUSTOM_STEP_TD_DB) + ". " + prefixName + appName + "_" + cr + " - " + groupActionLabels.get(Constant.ROLLBACK_STEP_DB));
            } else {
                findAndReplace(do2c, afterRollbackDb, "");
            }


            findAndReplace(do2c, rollbackClearCache, "2.2." + indexMap.get(Constant.ROLLBACK_STEP_CLEARCACHE) + ". " + prefixName + appName + "_" + cr + " - " + labelRollbackClearCache);
            insertTable(do2c, false, posClearCache, tableClearCacheRb);
            if (tableAfterClearCache != null) {
                insertTable(do2c, false, afterRollbackClearCache, tableAfterClearCacheRb);
                findAndReplace(do2c, afterRollbackClearCache, "2.2." + indexMap.get(Constant.ROLLBACK_CUSTOM_STEP_CLEARCACHE) + ". " + prefixName + appName + "_" + cr + " - " + groupActionLabels.get(Constant.ROLLBACK_STEP_CLEARCACHE));
            } else {
                findAndReplace(do2c, afterRollbackClearCache, "");
            }

            findAndReplace(do2c, rollbackRestartApp, "2.2." + indexMap.get(Constant.ROLLBACK_STEP_RESTART_APP) + ". " + prefixName + appName + "_" + cr + " - " + labelRollbackRestartApp);
            insertTable(do2c, false, posRestartApp, tableRestartRb);
            insertTable(do2c, false, posRestartAppCmd, tableRestartCmdRb);
            if (tableAfterRestartRb != null) {
                insertTable(do2c, false, afterRollbackRestartApp, tableAfterRestartRb);
                findAndReplace(do2c, afterRollbackRestartApp, "2.2." + indexMap.get(Constant.ROLLBACK_CUSTOM_STEP_RESTART_APP) + ". " + prefixName + appName + "_" + cr + " - " + groupActionLabels.get(Constant.ROLLBACK_STEP_RESTART_APP));
            } else {
                findAndReplace(do2c, afterRollbackRestartApp, "");
            }

            // findAndReplace(do2c, posRestartApp, "");
            findAndReplace(do2c, rollbackStartApp, "2.2." + indexMap.get(Constant.ROLLBACK_STEP_START_APP) + ". " + prefixName + appName + "_" + cr + " - " + labelRollbackStartApp);
            insertTable(do2c, false, posStartApp, tableStartRb);
            // findAndReplace(do2c, posStartApp, "");
            if (tableAfterStartRb != null) {
                insertTable(do2c, false, afterRollbackStartApp, tableAfterStartRb);
                findAndReplace(do2c, afterRollbackStartApp, "2.2." + indexMap.get(Constant.ROLLBACK_CUSTOM_STEP_START_APP) + ". " + prefixName + appName + "_" + cr + " - " + groupActionLabels.get(Constant.ROLLBACK_STEP_START_APP));
            } else {
                findAndReplace(do2c, afterRollbackStartApp, "");
            }

            insertTable(do2c, false, posCheckList, tableCheckListLastRb);
            // findAndReplace(do2c, posCheckList, "");
            insertTable(do2c, false, posCheckLast, tableCheckLastRb);
            // findAndReplace(do2c, posCheckLast, "");
            insertTable(do2c, false, posCheckListDb, tableCheckKpiRb);
            // findAndReplace(do2c, posCheckListDb, "");

            do2c.save(new File(source + File.separator + mopRollBack));

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return fileName;
    }

    public List<String> getListImpactIP(Long actionId) {
        Set<String> ips = new HashSet<>();
        ActionModuleServiceImpl service = new ActionModuleServiceImpl();
        ActionDetailDatabaseServiceImpl serviceDetailDb = new ActionDetailDatabaseServiceImpl();
        try {
            List<ActionDetailDatabase> listDetails = serviceDetailDb.findListDetailDb(actionId, null, true, false);
            List<Long> ids = service.findListModuleId(actionId, null, false);
            List<Module> lstApp = iimService.findModulesByIds(action.getImpactProcess().getNationCode(), ids);
            if (lstApp != null) {

                for (Module applicationDetail : lstApp) {
                    if (!ips.contains(applicationDetail.getIpServer()))
                        ips.add(applicationDetail.getIpServer());
                }
            }

            if (listDetails != null) {
                for (ActionDetailDatabase db : listDetails) {
                    ServiceDatabase serviceDatabase = iimService.findServiceDbById(action.getImpactProcess().getNationCode(), db.getAppDbId());
                    if (serviceDatabase != null && serviceDatabase.getIpVirtual() != null)
                        ips.addAll(Splitter.on(",").trimResults().omitEmptyStrings().splitToList(serviceDatabase.getIpVirtual()));
                }
            }

            //tuanda38 - 20180619 - start
            Action selectedAction = new ActionServiceImpl().findById(actionId);
            String cyberArk = selectedAction.getImpactProcess().getCyberArkIps();
//            String cyberArk = AppConfig.getInstance().getProperty("cyber_ark_ip");
            //tuanda38 - 20180619 - end
            if (StringUtils.isNotEmpty(cyberArk)) {
                ips.addAll(Splitter.on(",").trimResults().omitEmptyStrings().splitToList(cyberArk));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<>(ips);
    }

    public List<String> getListImpactIPRoot(Long actionId) {
        Set<String> ips = new HashSet<>();
        ActionModuleServiceImpl service = new ActionModuleServiceImpl();
        ActionDetailDatabaseServiceImpl serviceDetailDb = new ActionDetailDatabaseServiceImpl();
        try {
            List<ActionDetailDatabase> listDetails = serviceDetailDb.findListDetailDb(actionId, null, true, false);
            List<Long> ids = service.findListModuleId(actionId, null, false);
            List<Module> lstApp = iimService.findModulesByIds(action.getImpactProcess().getNationCode(), ids);
            if (lstApp != null) {

                for (Module applicationDetail : lstApp) {
                    if (!ips.contains(applicationDetail.getIpServer()) && applicationDetail.getUsername().equalsIgnoreCase("root")) {
                        ips.add(applicationDetail.getIpServer());
                    }
                }
            }

            if (listDetails != null) {
                for (ActionDetailDatabase db : listDetails) {
                    ServiceDatabase serviceDatabase = iimService.findServiceDbById(action.getImpactProcess().getNationCode(), db.getAppDbId());
                    if (serviceDatabase != null && serviceDatabase.getIpVirtual() != null)
                        ips.addAll(Splitter.on(",").trimResults().omitEmptyStrings().splitToList(serviceDatabase.getIpVirtual()));
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<>(ips);
    }

    public String getAppGroupName(Long actionId) {
        String app_Group_name = "";
        ActionModuleServiceImpl service = new ActionModuleServiceImpl();
        try {
            List<Long> ids = service.findListModuleId(actionId, null, false);
            if (ids != null && ids.size() > 0) {
                List<Module> lstApp = iimService.findModulesByIds(action.getImpactProcess().getNationCode(), ids);
                if (lstApp != null) {

                    for (Module applicationDetail : lstApp) {
                        if (!app_Group_name.contains(applicationDetail.getServiceName()))
                            app_Group_name += applicationDetail.getServiceName() + ",";
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        if (app_Group_name.length() > 2)
            app_Group_name = app_Group_name.substring(0, app_Group_name.length() - 1);
        return app_Group_name;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getTabelNode(Long actionId) {
        LinkedHashMap<String, LinkedHashMap<String, String>> data = new LinkedHashMap<>();
        LinkedHashMap<String, String> rowData;

//        String cyber = AppConfig.getInstance().getProperty("cyber_ark_ip");
        List<String> ips = getListImpactIP(actionId);

/*        if (StringUtils.isNotEmpty(cyber)) {
            List<String> cyberIps = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(cyber);
            ips.addAll(cyberIps);
        }*/
        int i = 0;
        for (String ip : ips) {
            if (data.containsKey(ip))
                rowData = data.get(ip);
            else {
                i++;
                rowData = new LinkedHashMap<>();

            }

            rowData.put("1", i + "");
            rowData.put("2", ip);
            rowData.put("3", MessageUtil.getResourceBundleMessage("all.of.country"));
            rowData.put("4", "");
            rowData.put("5", "");
            data.put(ip, rowData);
        }

        return data;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getTabelCheckList(Long actionId, String cr_number) {
        LinkedHashMap<String, LinkedHashMap<String, String>> data = new LinkedHashMap<>();
        LinkedHashMap<String, String> rowData;
        ActionModuleServiceImpl service = new ActionModuleServiceImpl();
        ActionModuleChecklistServiceImpl serviceCheckList = new ActionModuleChecklistServiceImpl();
        Map<Long, String> dataCheckList = buildMapCheckListApp();
        Map<Long, List<Long>> mapCheckList;

        // tạo row 1

        List<String> ips = getListImpactIP(actionId);
        List<String> ipsRoot = getListImpactIPRoot(actionId);
        String listIp = "";
        String listIpRoot = "";
        String listIpNoRoot = "";
        for (String ip : ips) {
            if (ipsRoot != null && ipsRoot.contains(ip)) {
                listIpRoot += ip + "\r\n";
            } else {
                listIpNoRoot += ip + "\r\n";
            }
            listIp += ip + "\r\n";
        }
        rowData = new LinkedHashMap<>();
        rowData.put("1", "1");
        rowData.put("2", listIp);
        rowData.put("3", MessageUtil.getResourceBundleMessage("call.to.fo.monitor"));
        rowData.put("4", MessageUtil.getResourceBundleMessage("call.to.fo.monitor"));
        rowData.put("5", "0462500008");
        rowData.put("6", MessageUtil.getResourceBundleMessage("called.to.fo"));
        rowData.put("7", "10");
        rowData.put("8", "");
        rowData.put("9", "");
        data.put("1", rowData);
        totalTime += 10;
        rowData = new LinkedHashMap<>();
        rowData.put("1", "2");
        rowData.put("2", listIp);
        rowData.put("3", MessageUtil.getResourceBundleMessage("notice.execution.request.monitoring.service") + " " + getAppGroupName(actionId));
        rowData.put("4", MessageUtil.getResourceBundleMessage("notice.execution.request.monitoring.service") + " " + getAppGroupName(actionId));
        rowData.put("5", Constant.NA_VALUE);
        rowData.put("6", MessageUtil.getResourceBundleMessage("fo.agrees.monitor.and.make.implementation"));
        rowData.put("7", "2");
        rowData.put("8", "");
        rowData.put("9", "");
        data.put("2", rowData);
        totalTime += 2;
        rowData = new LinkedHashMap<>();
        rowData.put("1", "3");
        rowData.put("2", listIp);
//		rowData.put("3", "Download code theo file \"HD download source code tac dong.docx\" trên GNOC, giải nén và đặt vào thư mục dưới: " + "\r\n" + "Tạo thư mục trên local có tên là định dạng :" + "\r\n" + "\"" + cr_number + "\"");
        rowData.put("3", MessageUtil.getResourceBundleMessage("download.code.from.cr.and.insert.to.folder.below") + ": " + "\r\n" + MessageUtil.getResourceBundleMessage("create.folder.on.local.have.name.same.format") + ":" + "\r\n" + "\"" + cr_number + "\"");
        rowData.put("4", MessageUtil.getResourceBundleMessage("create.folder.on.local.have.name.same.format") + " " + "\r\n" + "\"" + cr_number + "\"" + MessageUtil.getResourceBundleMessage("download.code.from.cr.to.local.and.insert.to.folder.upper") + "");
        rowData.put("5", cr_number);
        rowData.put("6", MessageUtil.getResourceBundleMessage("folder.created") + "\r\n" + " " + MessageUtil.getResourceBundleMessage("download.code.to.folder.upper"));
        rowData.put("7", "1");
        rowData.put("8", "");
        rowData.put("9", "");
        data.put("3", rowData);
        totalTime += 1;

        rowData = new LinkedHashMap<>();
        rowData.put("1", "4");
        rowData.put("2", listIp);
        rowData.put("3", MessageUtil.getResourceBundleMessage("create.a.local.directory.as.a.subdirectory.of.the.directory.you.just.created") + ": " + "\r\n" + "\"" + cr_number + "\\" + "logs\"");
        rowData.put("4", MessageUtil.getResourceBundleMessage("create.a.local.directory.as.a.subdirectory.of.the.directory.you.just.created") + ": " + "\r\n" + "\"" + cr_number + "\\" + "logs\"" + "\r\n"
                + MessageUtil.getResourceBundleMessage("configuration.save.log.session.ssh.to.folder.log.upper"));
        rowData.put("5", "Log");
        rowData.put("6", MessageUtil.getResourceBundleMessage("folder.created") + "\r\n" + " " + MessageUtil.getResourceBundleMessage("configuration.save.log.session.ssh.to.folder.log.successful"));
        rowData.put("7", "10");
        rowData.put("8", "");
        rowData.put("9", "");
        data.put("4", rowData);
        totalTime += 10;
        rowData = new LinkedHashMap<>();
        rowData.put("1", "5");
        rowData.put("2", listIp);
        rowData.put("3", MessageUtil.getResourceBundleMessage("login.to.GatePro"));
        rowData.put("4", MessageUtil.getResourceBundleMessage("login.to.GatePro"));
        rowData.put("5", Constant.NA_VALUE);
        rowData.put("6", MessageUtil.getResourceBundleMessage("GatePro.runed"));
        rowData.put("7", "1");
        rowData.put("8", "");
        rowData.put("9", "");
        data.put("5", rowData);
        totalTime += 1;
        rowData = new LinkedHashMap<>();
        rowData.put("1", "6");
        rowData.put("2", listIp);
        rowData.put("3", MessageUtil.getResourceBundleMessage("select.node"));
        rowData.put("4", MessageUtil.getResourceBundleMessage("select.node"));
        rowData.put("5", listIp);
        rowData.put("6", MessageUtil.getResourceBundleMessage("node.have.found"));
        rowData.put("7", "1");
        rowData.put("8", "");
        rowData.put("9", "");
        data.put("6", rowData);
        totalTime += 1;
        rowData = new LinkedHashMap<>();
        rowData.put("1", "7");
        rowData.put("2", listIp);
        rowData.put("3", MessageUtil.getResourceBundleMessage("open.firewall.for.node"));
        rowData.put("4", MessageUtil.getResourceBundleMessage("select.node.for.open.firewall.in.cr"));
        rowData.put("5", cr_number);
        rowData.put("6", MessageUtil.getResourceBundleMessage("open.firewall.for.node.in.cr"));
        rowData.put("7", "10");
        rowData.put("8", "");
        rowData.put("9", "");
        data.put("7", rowData);
        totalTime += 10;
        rowData = new LinkedHashMap<>();
        rowData.put("1", "8");
        rowData.put("2", listIpNoRoot);
        rowData.put("3", MessageUtil.getResourceBundleMessage("create.session.ssh.to.server.app"));
        rowData.put("4", MessageUtil.getResourceBundleMessage("create.session.ssh.on.tool.secure.crt.for.execute.ssh.to.server"));
        rowData.put("5", listIpNoRoot);
        rowData.put("6", MessageUtil.getResourceBundleMessage("create.session.connect.to.server.app.successful"));
        rowData.put("7", "10");
        rowData.put("8", "");
        rowData.put("9", "");
        data.put("8", rowData);
        totalTime += 10;
        if (!Util.isNullOrEmpty(listIpRoot)) {
            rowData = new LinkedHashMap<>();
            rowData.put("1", "9");
            rowData.put("2", listIpRoot);
            rowData.put("3", MessageUtil.getResourceBundleMessage("create.session.ssh.to.server.app") + "\r\n After run cmd login with account root: su -");
            rowData.put("4", MessageUtil.getResourceBundleMessage("create.session.ssh.on.tool.secure.crt.for.execute.ssh.to.server"));
            rowData.put("5", listIpRoot);
            rowData.put("6", MessageUtil.getResourceBundleMessage("create.session.connect.to.server.app.successful"));
            rowData.put("7", "10");
            rowData.put("8", "");
            rowData.put("9", "");
            data.put("9", rowData);
            totalTime += 10;
        }

        if (this.actionType.equals(Constant.ACTION_TYPE_CR_NORMAL)) {
            try {
                Map<Long, Long> mapIds = service.findMapActionModuleId(actionId);
                List<Long> lstId = service.findListActionModuleId(actionId);
                List<Long> ids = service.findListModuleId(actionId, null, false);
                if (ids != null && ids.size() > 0) {
                    List<Module> modules = iimService.findModulesByIds(action.getImpactProcess().getNationCode(), ids);
                    Map<Long, Module> mapApp = new HashMap<>();
                    for (Module module : modules) {
                        mapApp.put(module.getModuleId(), module);
                    }
                    mapCheckList = serviceCheckList.getMapCheckList(lstId);

                    ActionDetailAppService detailAppService = new ActionDetailAppServiceImpl();
                    RstKpiService kpiService = new RstKpiDaoImpl();
                    List<Long> rstKpiIds = kpiService.getKpiByCode(Arrays.asList("LIVE_OR_DIE", "CPU_MODULE", "RAM_MODULE", "CHECK_ERORR_LOG"));
                    int i = 8;
                    if (!Util.isNullOrEmpty(listIpRoot)) {
                        i = 9;
                    }
                    if (mapCheckList != null)

                        for (Long acModuleId : mapCheckList.keySet()) {
                            Long appId = mapIds.get(acModuleId);
                            Module module = mapApp.get(appId);
                            String checkList = "";
                            i++;
                            // du lieu cho 1 row
                            List<Long> lstCheckList = mapCheckList.get(acModuleId);

                            List<ActionDetailApp> moduleStop = detailAppService.findListDetailApp(actionId, Constant.STEP_STOP, module.getModuleId(), null, false);
                            List<ActionDetailApp> moduleStart = detailAppService.findListDetailApp(actionId, Constant.STEP_START, module.getModuleId(), null, false);

                            // lay danh sach check list
                            for (Long checkListId : lstCheckList) {
                                if (dataCheckList.containsKey(checkListId)) {
                                    if (moduleStop.isEmpty() && !moduleStart.isEmpty() && rstKpiIds.contains(checkListId))
                                        continue;
                                    checkList += "- " + dataCheckList.get(checkListId) + "\r\n";
                                }
                            }

                            if (StringUtils.isEmpty(checkList))
                                continue;

                            Unit unit = iimService.findUnit(action.getImpactProcess().getNationCode(), module.getUnitId());

                            rowData = new LinkedHashMap<>();
                            rowData.put("1", i + "");
                            rowData.put("2", module.getModuleCode());
                            rowData.put("3", MessageUtil.getResourceBundleMessage("using.tool.audit.checklist.compare.criteria.in.parameter.category.with.module.code.in.checklist"));
                            String action = MessageUtil.getResourceBundleMessage("accessing.to.link.tool.check.list") + ": "
                                    + "http://10.60.5.133:8866/checklist/tdtt/tdtt-checklist: " + MessageUtil.getResourceBundleMessage("with.path") + " UD_CNTT/" + unit.getUnitName() + "/"
                                    + module.getServiceCode() + "/Checklist App";
                            rowData.put("4", action);
                            rowData.put("5", checkList);
                            rowData.put("6", "OK" + "\r\n" + MessageUtil.getResourceBundleMessage("the.result.of.the.criteria.corresponding.to.the.module.code.is.ok"));
                            rowData.put("7", "2");
                            rowData.put("8", "");
                            rowData.put("9", "");
                            totalTime += 2;
                            data.put(i + "", rowData);

                        }
                }

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        return data;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getTabelCheckListRollBack(Long actionId, String cr_number) {
        LinkedHashMap<String, LinkedHashMap<String, String>> data = new LinkedHashMap<>();
        LinkedHashMap<String, String> rowData;
        new ActionModuleServiceImpl();
        new ActionModuleChecklistServiceImpl();
        buildMapCheckListApp();
        new HashMap<>();

        // tạo row 1

        List<String> ips = getListImpactIP(actionId);
        List<String> ipsRoot = getListImpactIPRoot(actionId);
        String listIp = "";
        String listIpRoot = "";
        String listIpNoRoot = "";
        for (String ip : ips) {
            if (ipsRoot != null && ipsRoot.contains(ip)) {
                listIpRoot += ip + "\r\n";
            } else {
                listIpNoRoot += ip + "\r\n";
            }
            listIp += ip + "\r\n";
        }
        rowData = new LinkedHashMap<>();
        rowData.put("1", "1");
        rowData.put("2", listIp);
        rowData.put("3", MessageUtil.getResourceBundleMessage("call.to.fo.monitor"));
        rowData.put("4", MessageUtil.getResourceBundleMessage("call.to.fo.monitor"));
        rowData.put("5", "0462500008");
        rowData.put("6", MessageUtil.getResourceBundleMessage("called.to.fo"));
        rowData.put("7", "10");
        rowData.put("8", "");
        rowData.put("9", "");
        data.put("1", rowData);
        totalTimeRollback += 10;
        rowData = new LinkedHashMap<>();
        rowData.put("1", "2");
        rowData.put("2", listIp);
        rowData.put("3", MessageUtil.getResourceBundleMessage("notice.execution.request.monitoring.service") + " " + getAppGroupName(actionId));
        rowData.put("4", MessageUtil.getResourceBundleMessage("notice.execution.request.monitoring.service") + " " + getAppGroupName(actionId));
        rowData.put("5", Constant.NA_VALUE);
        rowData.put("6", MessageUtil.getResourceBundleMessage("fo.agrees.monitor.and.make.implementation"));
        rowData.put("7", "2");
        rowData.put("8", "");
        rowData.put("9", "");
        data.put("2", rowData);
        totalTimeRollback += 2;
        rowData = new LinkedHashMap<>();
        rowData.put("1", "3");
        rowData.put("2", listIp);
        rowData.put("3", MessageUtil.getResourceBundleMessage("download.code.from.cr.and.insert.to.folder.below") + ": " + "\r\n" + "Tạo thư mục trên local có tên là định dạng :" + "\r\n" + "\""
                + cr_number + "\"");
        rowData.put("4", MessageUtil.getResourceBundleMessage("create.folder.on.local.have.name.same.format") + " " + "\r\n" + "\"" + cr_number + "\"" + MessageUtil.getResourceBundleMessage("download.code.from.cr.to.local.and.insert.to.folder.upper"));
        rowData.put("5", cr_number);
        rowData.put("6", MessageUtil.getResourceBundleMessage("folder.created") + "\r\n" + " " + MessageUtil.getResourceBundleMessage("download.code.to.folder.upper"));
        rowData.put("7", "1");
        rowData.put("8", "");
        rowData.put("9", "");
        data.put("3", rowData);
        totalTimeRollback += 1;
        rowData = new LinkedHashMap<>();
        rowData.put("1", "4");
        rowData.put("2", listIp);
        rowData.put("3", MessageUtil.getResourceBundleMessage("create.a.local.directory.as.a.subdirectory.of.the.directory.you.just.created") + ": " + "\r\n" + "\"" + cr_number + "\\" + "rollback\"");
        rowData.put("4", MessageUtil.getResourceBundleMessage("create.a.local.directory.as.a.subdirectory.of.the.directory.you.just.created") + ": " + "\r\n" + "\"" + cr_number + "\\" + "rollback\"" + "\r\n"
                + MessageUtil.getResourceBundleMessage("configuration.save.log.session.ssh.to.folder.log.upper"));
        rowData.put("5", "Log");
        rowData.put("6", MessageUtil.getResourceBundleMessage("folder.created") + "\r\n" + " " + MessageUtil.getResourceBundleMessage("configuration.save.log.session.ssh.to.folder.log.successful"));
        rowData.put("7", "10");
        rowData.put("8", "");
        rowData.put("9", "");
        data.put("4", rowData);
        totalTimeRollback += 10;
        rowData = new LinkedHashMap<>();
        rowData.put("1", "5");
        rowData.put("2", listIp);
        rowData.put("3", MessageUtil.getResourceBundleMessage("login.to.GatePro"));
        rowData.put("4", MessageUtil.getResourceBundleMessage("login.to.GatePro"));
        rowData.put("5", Constant.NA_VALUE);
        rowData.put("6", MessageUtil.getResourceBundleMessage("GatePro.runed"));
        rowData.put("7", "1");
        rowData.put("8", "");
        rowData.put("9", "");
        data.put("5", rowData);
        totalTimeRollback += 1;
        rowData = new LinkedHashMap<>();
        rowData.put("1", "6");
        rowData.put("2", listIp);
        rowData.put("3", MessageUtil.getResourceBundleMessage("select.node"));
        rowData.put("4", MessageUtil.getResourceBundleMessage("select.node"));
        rowData.put("5", listIp);
        rowData.put("6", MessageUtil.getResourceBundleMessage("node.have.found"));
        rowData.put("7", "1");
        rowData.put("8", "");
        rowData.put("9", "");
        data.put("6", rowData);
        totalTimeRollback += 1;
        rowData = new LinkedHashMap<>();
        rowData.put("1", "7");
        rowData.put("2", listIp);
        rowData.put("3", MessageUtil.getResourceBundleMessage("open.firewall.for.node"));
        rowData.put("4", MessageUtil.getResourceBundleMessage("select.node.for.open.firewall.in.cr"));
        rowData.put("5", cr_number);
        rowData.put("6", MessageUtil.getResourceBundleMessage("open.firewall.for.node.in.cr"));
        rowData.put("7", "10");
        rowData.put("8", "");
        rowData.put("9", "");
        data.put("7", rowData);
        totalTimeRollback += 10;
        rowData = new LinkedHashMap<>();
        rowData.put("1", "8");
        rowData.put("2", listIpNoRoot);
        rowData.put("3", MessageUtil.getResourceBundleMessage("create.session.ssh.to.server.app"));
        rowData.put("4", MessageUtil.getResourceBundleMessage("create.session.ssh.on.tool.secure.crt.for.execute.ssh.to.server"));
        rowData.put("5", listIpNoRoot);
        rowData.put("6", MessageUtil.getResourceBundleMessage("create.session.connect.to.server.app.successful"));
        rowData.put("7", "10");
        rowData.put("8", "");
        rowData.put("9", "");
        data.put("8", rowData);
        totalTimeRollback += 10;

        if (!Util.isNullOrEmpty(listIpRoot)) {
            rowData = new LinkedHashMap<>();
            rowData.put("1", "9");
            rowData.put("2", listIpRoot);
            rowData.put("3", MessageUtil.getResourceBundleMessage("create.session.ssh.to.server.app") + "\r\n After run cmd login with account root: su -");
            rowData.put("4", MessageUtil.getResourceBundleMessage("create.session.ssh.on.tool.secure.crt.for.execute.ssh.to.server"));
            rowData.put("5", listIpRoot);
            rowData.put("6", MessageUtil.getResourceBundleMessage("create.session.connect.to.server.app.successful"));
            rowData.put("7", "10");
            rowData.put("8", "");
            rowData.put("9", "");
            data.put("9", rowData);
            totalTimeRollback += 10;
        }
        return data;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getTabelCheckListLast(Long actionId, String cr_number) {
        LinkedHashMap<String, LinkedHashMap<String, String>> data = new LinkedHashMap<>();
        LinkedHashMap<String, String> rowData;
        RstKpiService kpiService = new RstKpiDaoImpl();
        ActionModuleServiceImpl service = new ActionModuleServiceImpl();
        ActionModuleChecklistServiceImpl serviceCheckList = new ActionModuleChecklistServiceImpl();
        Map<Long, String> dataCheckList = buildMapCheckListApp();
        Map<Long, List<Long>> mapCheckList;

        ActionDetailAppService detailAppService = new ActionDetailAppServiceImpl();
        List<Long> rstKpiIds = null;
        try {
            rstKpiIds = kpiService.getKpiByCode(Arrays.asList("LIVE_OR_DIE", "CPU_MODULE", "RAM_MODULE", "CHECK_ERORR_LOG"));
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }

        // tạo row 1

        try {
            Map<Long, Long> mapIds = service.findMapActionModuleId(actionId);
            List<Long> lstId = service.findListActionModuleId(actionId);
            List<Long> ids = service.findListModuleId(actionId, null, false);
            List<Module> modules = iimService.findModulesByIds(action.getImpactProcess().getNationCode(), ids);
            Map<Long, Module> mapApp = new HashMap<>();
            for (Module module : modules) {
                mapApp.put(module.getModuleId(), module);
            }
            mapCheckList = serviceCheckList.getMapCheckList(lstId);
            int i = 0;
            if (mapCheckList != null)

                for (Long acModuleId : mapCheckList.keySet()) {
                    Long appId = mapIds.get(acModuleId);
                    Module module = mapApp.get(appId);
                    String checkList = "";
                    i++;
                    // du lieu cho 1 row
                    List<Long> lstCheckList = mapCheckList.get(acModuleId);
                    // lay danh sach check list
                    List<ActionDetailApp> moduleStop = detailAppService.findListDetailApp(actionId, Constant.STEP_STOP, module.getModuleId(), null, false);
                    List<ActionDetailApp> moduleStart = detailAppService.findListDetailApp(actionId, Constant.STEP_START, module.getModuleId(), null, false);

                    for (Long checkListId : lstCheckList) {
                        if (dataCheckList.containsKey(checkListId)) {
                            if (!moduleStop.isEmpty() && moduleStart.isEmpty() && rstKpiIds != null && rstKpiIds.contains(checkListId))
                                continue;

                            checkList += "- " + dataCheckList.get(checkListId) + "\r\n";
                        }
                    }

                    if (StringUtils.isEmpty(checkList))
                        continue;

                    Unit unit = iimService.findUnit(action.getImpactProcess().getNationCode(), module.getUnitId());

                    rowData = new LinkedHashMap<>();
                    rowData.put("1", i + "");
                    rowData.put("2", module.getModuleCode());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("using.tool.audit.checklist.compare.criteria.in.parameter.category.with.module.code.in.checklist"));
                    String action = MessageUtil.getResourceBundleMessage("accessing.to.link.tool.check.list") + ": "
                            + "http://10.60.5.133:8866/checklist/tdtt/tdtt-checklist: " + MessageUtil.getResourceBundleMessage("with.path") + " UD_CNTT/" + unit.getUnitName() + "/"
                            + module.getServiceCode() + "/Checklist App";
                    rowData.put("4", action);
                    rowData.put("5", checkList);
                    rowData.put("6", "OK" + "\r\n" + MessageUtil.getResourceBundleMessage("the.result.of.the.criteria.corresponding.to.the.module.code.is.ok"));
                    rowData.put("7", "2");
                    rowData.put("8", "");
                    rowData.put("9", "");
                    totalTimeRollback += 2;
                    totalTime += 2;
                    data.put(i + "", rowData);

                }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return data;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getTabelCheckListKpi(Long actionId) {
        LinkedHashMap<String, LinkedHashMap<String, String>> data = new LinkedHashMap<>();
        LinkedHashMap<String, String> rowData = null;
        RstKpiDbSettingDaoImpl kpisetting = new RstKpiDbSettingDaoImpl();
        ActionDbChecklistServiceImpl serviceCheckList = new ActionDbChecklistServiceImpl();
        Map<Long, String> dataCheckList = buildMapCheckListDb();
        Map<Long, List<Long>> mapCheckList = new HashMap<>();

        // tạo row 1

        try {
            int i = 0;
            mapCheckList = serviceCheckList.getMapCheckList(actionId);
            KpiDbSetting rst;
            if (mapCheckList != null)

                for (Long appDbId : mapCheckList.keySet()) {
                    ServiceDatabase seDb = iimService.findServiceDbById(action.getImpactProcess().getNationCode(), appDbId);

                    String checkList;

                    // du lieu cho 1 row
                    List<Long> lstCheckList = mapCheckList.get(appDbId);
                    // lay danh sach check list
                    for (Long checkListId : lstCheckList) {
                        i++;
                        rst = kpisetting.findbyKpiId(checkListId, appDbId);
                        checkList = dataCheckList.get(checkListId);
                        rowData = new LinkedHashMap<>();

                        rowData.put("1", seDb.getServiceCode() == null ? "" : seDb.getServiceCode());

                        rowData.put("2", seDb.getServiceName() == null ? "" : seDb.getServiceName());

                        rowData.put("3", seDb.getUrl() == null ? "" : seDb.getUrl());
                        rowData.put("4", seDb.getUsername());
                        rowData.put("5", checkList);
                        if (rst != null) {
                            rowData.put("6", rst.getSqlCommand());
                            rowData.put("7", rst.getDefaultvalue() + "");
//                            rowData.put("8", rst.getMathOptionStr());
                            rowData.put("8", MessageUtil.getResourceBundleMessage("checklist.operation." + rst.getMathOption()));
                        } else {
                            rowData.put("6", "");
                            rowData.put("7", "");
                            rowData.put("8", "");
                        }
                        rowData.put("9", "");
                        rowData.put("10", "");

                        data.put(i + "", rowData);

                    }

                }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return data;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getTabelCheckLast(Long actionId, String cr_number, String mopRollBack) {
        LinkedHashMap<String, LinkedHashMap<String, String>> data = new LinkedHashMap<>();
        LinkedHashMap<String, String> rowData;
//		TestCaseServiceImpl testCase = new TestCaseServiceImpl();
//		Map<String, String> mapTestCase = testCase.getMapTestCase(actionId);
        // tạo row 1

        List<String> ips = getListImpactIP(actionId);
        String listIp = "";
        for (String ip : ips) {
            listIp += ip + "\r\n";
        }
        rowData = new LinkedHashMap<>();
        rowData.put("1", "1");
        rowData.put("2", listIp);
        rowData.put("3", MessageUtil.getResourceBundleMessage("call.to.fo.monitor"));
        rowData.put("4", MessageUtil.getResourceBundleMessage("call.to.fo.monitor"));
        rowData.put("5", "0462500008");
        rowData.put("6", MessageUtil.getResourceBundleMessage("called.to.fo"));
        rowData.put("7", "10");
        rowData.put("8", "");
        rowData.put("9", "");
        rowData.put("10", "");
        data.put("1", rowData);

        rowData = new LinkedHashMap<>();
        rowData.put("1", "2");
        rowData.put("2", listIp);
        rowData.put("3", MessageUtil.getResourceBundleMessage("notice.execution.request.monitoring.service") + " " + getAppGroupName(actionId));
        rowData.put("4", MessageUtil.getResourceBundleMessage("notice.execution.request.monitoring.service") + " " + getAppGroupName(actionId));
        rowData.put("5", Constant.NA_VALUE);
        rowData.put("6", MessageUtil.getResourceBundleMessage("fo.agrees.monitor.and.make.implementation"));
        rowData.put("7", "2");
        rowData.put("8", "");
        rowData.put("9", "");
        rowData.put("10", "");
        data.put("2", rowData);
        int i = 3;

        rowData = new LinkedHashMap<>();
        rowData.put("1", i + "");
        rowData.put("2", listIp);
        rowData.put("3", MessageUtil.getResourceBundleMessage("test.service.again.after.edit"));
        rowData.put("4", MessageUtil.getResourceBundleMessage("call.to.department.test.follow.form.test.in.items") + " 2.5");
        rowData.put("5", Constant.NA_VALUE);
        rowData.put("6", MessageUtil.getResourceBundleMessage("response.of.department.follow.form.test.regulations"));
        rowData.put("7", "10");
        rowData.put("8", "");
        rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.4 => " + MessageUtil.getResourceBundleMessage("Clear.cache.if.have") + "\r\n"
                + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.1 => " + MessageUtil.getResourceBundleMessage("restart.module.again") + "\r\n"
                + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                + "B7: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n" + "\r\n"
                + "B8: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback")
                + "B9: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
        rowData.put("10", "");
        data.put("3", rowData);
//		i++;
        totalTimeRollback += 10;
        totalTime += 10;
        return data;
    }

    public Map<Long, String> buildMapCheckListApp() {
        Map<Long, String> data = new HashMap<>();
        ChecklistServiceImpl srImpl = new ChecklistServiceImpl();
        List<Checklist> lst = null;
        try {
            lst = srImpl.findList();
            if (lst != null)
                for (Checklist checklist : lst) {
                    if (checklist.getType() == 1)
                        data.put(checklist.getId(), checklist.getName());
                }
        } catch (SysException | AppException e) {
            // TODO Auto-generated catch block
            logger.error(e.getMessage(), e);
        }
        return data;
    }

    public Map<Long, String> buildMapCheckListDb() {
        Map<Long, String> data = new HashMap<>();
        ChecklistServiceImpl srImpl = new ChecklistServiceImpl();
        List<Checklist> lst = null;
        try {
            lst = srImpl.findList();
            if (lst != null)
                for (Checklist checklist : lst) {
                    if (checklist.getType() == 2)
                        data.put(checklist.getId(), checklist.getName());
                }
        } catch (SysException | AppException e) {
            // TODO Auto-generated catch block
            logger.error(e.getMessage(), e);
        }
        return data;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getTableStopApp(Long actionId, String mopRollBack) {
        LinkedHashMap<String, LinkedHashMap<String, String>> data = new LinkedHashMap<>();
        LinkedHashMap<String, String> rowData = null;
        ActionDetailAppServiceImpl serviceDetailApp = new ActionDetailAppServiceImpl();
        ActionDetailDatabaseServiceImpl serviceDetailDb = new ActionDetailDatabaseServiceImpl();
        ActionModuleService serviceApp = new ActionModuleServiceImpl();

        try {

//			List<Long> ids = service.findListModuleId(actionId);
            Map<Long, ActionModule> mapApp = serviceApp.getMapObjByListAppId(actionId);
            List<ActionDetailApp> listDetails = serviceDetailApp.findListDetailApp(actionId, Constant.STEP_STOP, moduleIds, false);

            int total = 0;
            Map<Long, Integer> moduleStart = new HashMap<>();
            for (ActionDetailApp actionDetailApp : listDetails) {
                Long appId = actionDetailApp.getModuleId();
                ActionModule module = mapApp.get(appId);
//				String key = module.getKeyword();
                String link = module.getLogLink();
//				String key_status_start = module.getKeyStatusStart();

                //
                if (StringUtils.isNotEmpty(link) && !link.trim().equals(Constant.NA_VALUE)) {
                    moduleStart.put(actionDetailApp.getModuleId(), 4);
                } else {
                    moduleStart.put(actionDetailApp.getModuleId(), 2);
                }

                total += moduleStart.get(actionDetailApp.getModuleId());
            }

            Integer currentIndex = indexMap.get(Constant.SUB_STEP_STOP_APP);

            int i = 0;
            int j = 0;
            String des = "";

            if (j == 1) {
                des = MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
//								+ "B1: Thực hiện  bước 3.2.5.2.1 đến bước 3.2.5.2." + (j - 1) * 5 + " => bật lại module" + "\r\n"
                        + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2." + getIndexRollbackStop(listDetails, moduleStart, j)
                        + " " + MessageUtil.getResourceBundleMessage("to.step") + " 2.2.5.2." + total + " => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                        + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                        + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                        + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback");
            }
            List<ActionDetailDatabase> listDetailDbs = serviceDetailDb.findListDetailDb(actionId, kbGroup, true, false);
            if (listDetailDbs != null) {

                for (ActionDetailDatabase actionDetailDb : listDetailDbs) {
                    if (!actionDetailDb.getType().equals(Constant.TD_DB_TYPE_STOP_START))
                        continue;


                    ServiceDatabase module = iimService.findServiceDbById(action.getImpactProcess().getNationCode(), actionDetailDb.getAppDbId());
                    String file = FilenameUtils.getName(actionDetailDb.getScriptExecute());
                    i++;
                    // du lieu cho 1 row

                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2." + currentIndex + "." + i + "");
                    rowData.put("1.1", actionDetailDb.getActionOrder() + "");
                    rowData.put("2", module.getIpVirtual() + "@" + module.getUsername() + "\r\n" + "URL : " + module.getUrl());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("run.script.turn.off.flag"));
                    String action;
                    if (actionDetailDb.getTypeImport() == null)
                        rowData.put("4", MessageUtil.getResourceBundleMessage("need.not.execute"));
                    else {
                        if (actionDetailDb.getTypeImport() == 1L)
                            if (file != null)
                                action = MessageUtil.getResourceBundleMessage("execute.commands.sql.in.the.file") + " " + file;
                            else
                                action = MessageUtil.getResourceBundleMessage("need.not.execute");
                        else {
                            action = "-- " + MessageUtil.getResourceBundleMessage("execute.commands.follow.script") + ": " + "\r\n" + actionDetailDb.getScriptText();
                        }
                        rowData.put("4", action);
                    }
                    rowData.put("6", MessageUtil.getResourceBundleMessage("run.script.successful.have.not.error"));
                    rowData.put("7", "5");
                    rowData.put("8", "");
                    rowData.put("9", des);
                    rowData.put("10", "");
                    data.put(module.getDbCode() + i++, rowData);
                    totalTime += 5;
                }
            }

            if (listDetails != null) {

                for (ActionDetailApp actionDetailApp : listDetails) {
                    Long appId = actionDetailApp.getModuleId();
                    ActionModule module = mapApp.get(appId);

                    boolean codetaptrung = false;
                    if (module.getAppTypeCode() != null && module.getAppTypeCode().equals(Constant.CODETAPTRUNG_TYPE)) {
                        codetaptrung = true;
                    }

                    if (AamConstants.OS_TYPE.WINDOWS != module.getOsType()) {
                        // cd vào thu muc cai dat
                        i++;
                        j++;
                        if (j == 1)
                            des = MessageUtil.getResourceBundleMessage("need.not.execute");
                        else {
                            des = MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
//								+ "B1: Thực hiện  bước 3.2.5.2.1 đến bước 3.2.5.2." + (j - 1) * 5 + " => bật lại module" + "\r\n"
                                    + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2." + getIndexRollbackStop(listDetails, moduleStart, j)
                                    + " " + MessageUtil.getResourceBundleMessage("to.step") + " 2.2.5.2." + total + " => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                    + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                    + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                    + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback");
                        }
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + i + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("forward.to.folder.install"));
                        rowData.put("4", "cd " + module.getPath() + ";");

                        rowData.put("5", MessageUtil.getResourceBundleMessage("moved.to.app.directory.success.have.prompt.when.command.end"));
                        rowData.put("6", "1");

                        rowData.put("8", "");
                        rowData.put("9", des);
                        rowData.put("10", "");
                        data.put(i + "", rowData);
                        totalTime += 1;
                        // dung module
                    }

                    i++;
                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2." + currentIndex + "." + i + "");
                    rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                    rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("stop.module"));

                    String stopCmd;
                    String expect;
                    if (StringUtils.isNotEmpty(module.getViewStatus()) && StringUtils.isNotEmpty(module.getKeyStatusStart()) && !module.getViewStatus().toUpperCase().equals(Constant.NA_VALUE)) {
                        stopCmd = module.getStopService();
                        rowData.put("4", stopCmd);
                        expect = "1. " + MessageUtil.getResourceBundleMessage("moved.to.app.directory.success.have.prompt.when.command.end") + "\n"
                                + "2. " + MessageUtil.getResourceBundleMessage("if.after.desired.time.execute.command.without.command.prompt.press.ctrl.c");
                    } else {
                        stopCmd = codetaptrung ? module.getStopService() : module.getStopService() + ";" + "\r\n" + "\r\n" + "ps -ef | grep \"" + LinuxParser.getFullPath(module.getPath()) + "\" | grep -v grep; " + "\r\n" + "kill -9 PID;";
                        rowData.put("4", stopCmd);
                        expect = "1. " + MessageUtil.getResourceBundleMessage("moved.to.app.directory.success.have.prompt.when.command.end") + "\n"
                                + "2. " + MessageUtil.getResourceBundleMessage("if.after.desired.time.execute.command.without.command.prompt.press.ctrl.c");
                        if (!codetaptrung) {
                            expect += ", " + MessageUtil.getResourceBundleMessage("execute.command.get.PID.of.process") + "\r\n"
                                    + MessageUtil.getResourceBundleMessage("example.get.PID.red.shown.below") + " (PID = 20894)"
                                    + "\r\n"
                                    + "$ ps -ef | grep /u01/BCCS_CODINH/CM_APP/jdk1.6.0_26/ | grep -v grep;"
                                    + "\r\n "
                                    + "5503      20894     1  2 Apr14 ?        03:12:52 "
                                    + "\r\n"
                                    + "/u01/BCCS_CODINH/CM_APP/jdk1.6.0_26/bin/java -"
                                    + "\r\n"
                                    + "3. " + MessageUtil.getResourceBundleMessage("pid.process.have.still.stop.process.using.command.kill.9.pid");

                        }
                    }
                    rowData.put("5", expect);
                    rowData.put("6", "3");
                    rowData.put("8", "");
                    rowData.put("9", des);
                    rowData.put("10", "");
                    data.put(i + "", rowData);
                    totalTime += 3;

                    if (!codetaptrung) {
                        if (StringUtils.isNotEmpty(module.getViewStatus()) && StringUtils.isNotEmpty(module.getKeyStatusStart()) && !module.getViewStatus().toUpperCase().equals(Constant.NA_VALUE)) {
                            i++;
                            rowData = new LinkedHashMap<>();
                            rowData.put("1", "2.2." + currentIndex + "." + i + "");
                            rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                            rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                            rowData.put("3", MessageUtil.getResourceBundleMessage("check.module.has.stopped.altogether"));
                            rowData.put("4", module.getViewStatus());
                            rowData.put("5", MessageUtil.getResourceBundleMessage("have.not.keyword") + " \"" + module.getKeyStatusStart()
                                    + "\" " + MessageUtil.getResourceBundleMessage("appears.in.begin.line.has.command.prompt.after.end.command.process.has.been.stopped"));
                            rowData.put("6", "1");

                            rowData.put("8", "");
                            rowData.put("9", des);
                            rowData.put("10", "");
                            data.put(i + "", rowData);
                            totalTime += 1;
                        } else {
                            i++;
                            rowData = new LinkedHashMap<>();
                            rowData.put("1", "2.2." + currentIndex + "." + i + "");
                            rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                            rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                            rowData.put("3", MessageUtil.getResourceBundleMessage("check.module.has.stopped.altogether"));
                            rowData.put("4", "ps  -ef | grep \"" + LinuxParser.getFullPath(module.getPath()) + "\" | grep -v grep");
                            rowData.put("5", MessageUtil.getResourceBundleMessage("no.process.line.app.appears.there.is.command.prompt.after.end.command.process.has.been.stopped"));
                            rowData.put("6", "1");

                            rowData.put("8", "");
                            rowData.put("9", des);
                            rowData.put("10", "");
                            data.put(i + "", rowData);
                            totalTime += 1;
                        }
                    }
                }
            }


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return data;
    }

    public String rollbackDetail(Integer step, String mopRollBack) {
        step += 20;
        String des = "";
        switch (step) {
            case Constant.CUSTOM_STEP_CHECK_STATUS:
                break;
            case Constant.CUSTOM_STEP_STOP_APP:
                des = MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                        + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                        + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                        + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                        + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback");
                break;
            case Constant.CUSTOM_STEP_BACKUP_APP:
                des = MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                        + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                        + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                        + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                        + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback");
                break;
            case Constant.CUSTOM_STEP_BACKUP_DB:
                des = MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                        + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                        + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                        + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                        + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback");
                break;
            case Constant.CUSTOM_STEP_UPCODE:
                des = MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                        + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                        + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                        + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2=> " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                        + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                        + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                        + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback");
                break;
            case Constant.CUSTOM_STEP_TD_DB:
                des = MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                        + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                        + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                        + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                        + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                        + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback");
                break;
            case Constant.CUSTOM_STEP_CLEARCACHE:
                break;
            case Constant.CUSTOM_STEP_RESTART_APP:
                des = MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                        + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                        + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                        + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.4 => " + MessageUtil.getResourceBundleMessage("Clear.cache.if.have") + "\r\n"
                        + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.1 => " + MessageUtil.getResourceBundleMessage("restart.module.again") + "\r\n"
                        + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                        + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                        + "B7: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                        + "B8: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback");
                break;
            case Constant.CUSTOM_STEP_START_APP:
                des = MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                        + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.1 => " + MessageUtil.getResourceBundleMessage("stop.modules") + "\r\n"
                        + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                        + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                        + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.4 => " + MessageUtil.getResourceBundleMessage("Clear.cache.if.have") + "\r\n"
                        + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.1 => " + MessageUtil.getResourceBundleMessage("restart.module.again") + "\r\n"
                        + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                        + "B7: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                        + "B8: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                        + "B9: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback");
                break;
            default:
                break;
        }

        return des;
    }


    public LinkedHashMap<String, LinkedHashMap<String, String>> getTableCustom(Integer position, Long actionId, String mopRollBack, String crNumber, String date) {
        LinkedHashMap<String, LinkedHashMap<String, String>> data = new LinkedHashMap<>();
        LinkedHashMap<String, String> rowData;

        ActionCustomActionService actionService = new ActionCustomActionServiceImpl();
        ActionCustomGroupService groupService = new ActionCustomGroupServiceImpl();
        ActionModuleService serviceApp = new ActionModuleServiceImpl();

        List<ActionCustomAction> customActions = new ArrayList<>();

        Map<String, Object> filters = new HashMap<>();
        filters.put("actionId", actionId);
        filters.put("afterGroup", position);
        filters.put("kbGroup", kbGroup);
        try {
            List<ActionCustomGroup> customGroups = groupService.findList(filters, new HashMap<>());
            if (customGroups != null && !customGroups.isEmpty()) {
                for (ActionCustomGroup customGroup : customGroups) {
                    groupActionLabels.put(position, customGroup.getName());
                }

                filters = new HashMap<>();
                filters.put("actionCustomGroup.id", customGroups.get(0).getId());

                Map<String, String> orders = new HashMap<>();
                orders.put("priority", "ASC");
                customActions = actionService.findList(filters, orders);

                List<ActionCustomAction> rollbackCodeTests = new ArrayList<>();

                for (ActionCustomAction customAction : customActions) {
                    if (customAction.getType() == 0) {
                        Cloner cloner = new Cloner();
                        ActionCustomAction rollbackTest = cloner.deepClone(customAction);
                        rollbackTest.setPriority(customAction.getRollbackTestPriority());
//						rollbackTest.setModuleAction(12);
                        /*if (customAction.getModuleAction() == 100)
                            rollbackTest.setModuleAction(200);
						else if (customAction.getModuleAction() == 101)
							rollbackTest.setModuleAction(201);
						else if (customAction.getModuleAction() == 102)
							rollbackTest.setModuleAction(202);*/

                        if (customAction.getModuleAction() == Constant.SPECIAL_UPCODETEST_RESTART_STOP_START)
                            rollbackTest.setModuleAction(Constant.SPECIAL_ROLLBACKTEST_RESTART_STOP_START);
                        else if (customAction.getModuleAction() == Constant.SPECIAL_UPCODETEST_RESTART)
                            rollbackTest.setModuleAction(Constant.SPECIAL_ROLLBACKTEST_RESTART);
                        else if (customAction.getModuleAction() == Constant.SPECIAL_UPCODETEST_START)
                            rollbackTest.setModuleAction(Constant.SPECIAL_ROLLBACKTEST_STOP);
                        else if (customAction.getModuleAction() == Constant.SPECIAL_UPCODETEST_STOP_START)
                            rollbackTest.setModuleAction(Constant.SPECIAL_ROLLBACKTEST_STOP_START);
                        else if (customAction.getModuleAction() == Constant.SPECIAL_RESTART_STOP_START)
                            rollbackTest.setModuleAction(Constant.SPECIAL_ROLLBACK_RESTART_STOP_START);
                        else if (customAction.getModuleAction() == Constant.SPECIAL_RESTART)
                            rollbackTest.setModuleAction(Constant.SPECIAL_ROLLBACK_RESTART);
                        else if (customAction.getModuleAction() == Constant.SPECIAL_START)
                            rollbackTest.setModuleAction(Constant.SPECIAL_ROLLBACK_STOP);

                        rollbackCodeTests.add(rollbackTest);
                    }
                }

                customActions.addAll(rollbackCodeTests);
            }
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }

        if (customActions.isEmpty())
            return null;
        Integer currentIndex = tdIndex++;
        indexMap.put(position + 200, currentIndex);

        try {
            Map<Long, ActionModule> mapApp = serviceApp.getMapObjByListAppId(actionId);
            int i = 0;
            int j = 0;
            if (customActions != null)

                for (ActionCustomAction customAction : customActions) {
                    switch (customAction.getType()) {
                        case 0:
                            Long appId = customAction.getModuleId();
                            ActionModule module = mapApp.get(appId);
                            String key = module.getKeyword();
                            String link = module.getLogLink();

                            boolean codetaptrung = false;
                            if (module.getAppTypeCode() != null && module.getAppTypeCode().equals(Constant.CODETAPTRUNG_TYPE)) {
                                codetaptrung = true;
                            }

                            appId = customAction.getModuleId();
                            module = mapApp.get(appId);
                            String file = FilenameUtils.getName(customAction.getUploadCodePath());

                            MutableInt index = new MutableInt(i);
                            switch (customAction.getModuleAction()) {
                                case Constant.SPECIAL_UPCODETEST_RESTART_STOP_START:
                                    upcodeTestDt(position, currentIndex, index, customAction, module, data, mopRollBack, date, crNumber, file);
                                    stopCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    startCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    break;
                                case Constant.SPECIAL_UPCODETEST_RESTART:
                                    upcodeTestDt(position, currentIndex, index, customAction, module, data, mopRollBack, date, crNumber, file);
                                    restartCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    break;
                                case Constant.SPECIAL_UPCODETEST_START:
                                    upcodeTestDt(position, currentIndex, index, customAction, module, data, mopRollBack, date, crNumber, file);
                                    startCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    break;
                                case Constant.SPECIAL_UPCODETEST_STOP_START:
                                    stopCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    upcodeTestDt(position, currentIndex, index, customAction, module, data, mopRollBack, date, crNumber, file);
                                    startCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    break;
                                case Constant.SPECIAL_RESTART_STOP_START:
                                case Constant.SPECIAL_ROLLBACK_RESTART_STOP_START:
                                    stopCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    startCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    break;
                                case Constant.SPECIAL_RESTART:
                                case Constant.SPECIAL_ROLLBACK_RESTART:
                                    restartCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    break;
                                case Constant.SPECIAL_START:
                                    startCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    break;
                                case Constant.SPECIAL_ROLLBACKTEST_RESTART_STOP_START:
                                    rollbackUpcodeTestDt(position, currentIndex, index, customAction, module, data, mopRollBack, date, crNumber, file);
                                    stopCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    startCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    break;
                                case Constant.SPECIAL_ROLLBACKTEST_RESTART:
                                    rollbackUpcodeTestDt(position, currentIndex, index, customAction, module, data, mopRollBack, date, crNumber, file);
                                    restartCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    break;
                                case Constant.SPECIAL_ROLLBACKTEST_STOP:
                                    rollbackUpcodeTestDt(position, currentIndex, index, customAction, module, data, mopRollBack, date, crNumber, file);
                                    stopCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    break;
                                case Constant.SPECIAL_ROLLBACKTEST_STOP_START:
                                    stopCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    rollbackUpcodeTestDt(position, currentIndex, index, customAction, module, data, mopRollBack, date, crNumber, file);
                                    startCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    break;
                                case Constant.SPECIAL_ROLLBACK_STOP:
                                    stopCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    break;
                                default:
                                    break;
                            }
                            i = index.getValue();
                            break;
                        case 1:
                            ActionModuleServiceImpl service = new ActionModuleServiceImpl();
                            switch (customAction.getDbAction()) {
                                case 0:
                                    List<Long> ids = service.findListModuleId(actionId, null, false);
                                    List<Integer> listIds = new ArrayList<>();
                                    if (ids != null)
                                        for (Long id : ids) {
                                            listIds.add(id.intValue());
                                        }
                                    ServiceDatabase db = iimService.findServiceDbById(action.getImpactProcess().getNationCode(), customAction.getDbId());
/*											Module app = serviceApp.getObjByAppId(db.getAppId().longValue());
                                            if (app == null)
												app = new Module();*/
                                    file = FilenameUtils.getName(customAction.getDbScriptFile());
                                    i++;
                                    // du lieu cho 1 row

                                    rowData = new LinkedHashMap<>();
                                    rowData.put("1", "2.2." + currentIndex + "." + i + "");
                                    rowData.put("1.1", customAction.getPriority() + "");
                                    rowData.put("2", db.getIpVirtual() + "@" + db.getUsername() + "\r\n" + "URL : " + db.getUrl());
                                    rowData.put("3", MessageUtil.getResourceBundleMessage("run.script.configuration.db"));
                                    String action = MessageUtil.getResourceBundleMessage("execute.commands.sql.in.the.file") + " " + file;

                                    rowData.put("4", action);

                                    rowData.put("6", MessageUtil.getResourceBundleMessage("run.script.successful.have.not.error"));
                                    rowData.put("7", "5");
                                    rowData.put("8", "");
                                    rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                            + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                            + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                            + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                            + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                            + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5=> " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                                    rowData.put("10", "");
                                    data.put(db.getDbCode() + System.currentTimeMillis(), rowData);
                                    totalTime += 5;

                                    break;
                                case 1:
                                    db = iimService.findServiceDbById(this.action.getImpactProcess().getNationCode(), customAction.getDbId());
/*											Module app = serviceApp.getObjByAppId(db.getAppId().longValue());
                                            if (app == null)
												app = new Module();*/
//									file = FilenameUtils.getName(customAction.getDbScriptFile());
                                    i++;
                                    // du lieu cho 1 row

                                    rowData = new LinkedHashMap<>();
                                    rowData.put("1", "2.2." + currentIndex + "." + i + "");
                                    rowData.put("1.1", customAction.getPriority() + "");
                                    rowData.put("2", db.getIpVirtual() + "@" + db.getUsername() + "\r\n" + "URL : " + db.getUrl());
                                    rowData.put("3", MessageUtil.getResourceBundleMessage("import.data.to.db"));

                                    action = MessageUtil.getResourceBundleMessage("import.file.using.tool.import") + " " + customAction.getImportDataFile()
                                            + " " + MessageUtil.getResourceBundleMessage("to.db") + "\n"
                                            + MessageUtil.getResourceBundleMessage("command.import")
                                            + ": " + customAction.getSqlImport() + "\nSparator: " + customAction.getSeparator();

                                    rowData.put("4", action);

                                    rowData.put("6", MessageUtil.getResourceBundleMessage("import.successful"));
                                    rowData.put("7", "5");
                                    rowData.put("8", "");
                                    rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                            + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                            + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                            + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                            + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                            + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                                    rowData.put("10", "");
                                    data.put(db.getDbCode() + System.currentTimeMillis(), rowData);
                                    totalTime += 5;
                                    break;
                                case 2:
                                    ids = service.findListModuleId(actionId, null, false);
                                    listIds = new ArrayList<>();
                                    if (ids != null)
                                        for (Long id : ids) {
                                            listIds.add(id.intValue());
                                        }

                                    db = iimService.findServiceDbById(this.action.getImpactProcess().getNationCode(), customAction.getDbId());
//									file = FilenameUtils.getName(customAction.getDbScriptFile());
                                    i++;
                                    // du lieu cho 1 row

                                    rowData = new LinkedHashMap<>();
                                    rowData.put("1", "2.2." + currentIndex + "." + i + "");
                                    rowData.put("1.1", customAction.getPriority() + "");
                                    rowData.put("2", db.getIpVirtual() + "@" + db.getUsername() + "\r\n" + "URL : " + db.getUrl());
                                    rowData.put("3", MessageUtil.getResourceBundleMessage("run.command.export.data"));
//									action = "";

                                    action = MessageUtil.getResourceBundleMessage("export.data.follow.command") + "\n" + customAction.getExportStatement();
                                    if (StringUtils.isNotEmpty(customAction.getExportCount())) {
                                        action += "\n\n" + MessageUtil.getResourceBundleMessage("run.command.count.number.record") + "\n" + customAction.getExportCount();
                                    }

                                    rowData.put("4", action);

                                    rowData.put("6", MessageUtil.getResourceBundleMessage("export.data.successful"));
                                    rowData.put("7", "5");
                                    rowData.put("8", "");
                                    rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                            + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                            + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                            + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                            + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                            + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                                    rowData.put("10", "");
                                    data.put(db.getDbCode() + System.currentTimeMillis(), rowData);
                                    totalTime += 5;
                                    break;
                                default:
                                    break;
                            }

                            break;
                        case 2:
                            i++;
                            ActionDtFileService dtFileService = new ActionDtFileServiceImpl();
                            ActionDtFile dtFile = dtFileService.findById(customAction.getFileId());
                            rowData = new LinkedHashMap<>();
                            rowData.put("1", "2.2." + currentIndex + "." + i + "");
                            rowData.put("1.1", customAction.getPriority() + "");
                            rowData.put("2", Constant.NA_VALUE);
                            rowData.put("3", dtFile.getImpactDescription());
                            rowData.put("4", MessageUtil.getResourceBundleMessage("execute.follow.the.file") + " " + dtFile.getImpactFile());

                            rowData.put("6", MessageUtil.getResourceBundleMessage("execute.successful"));
                            rowData.put("7", dtFile.getImpactTime() + "");
                            rowData.put("8", "");
                            rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                    + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                    + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                                    + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                    + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                    + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                    + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                            rowData.put("10", "");
                            data.put(i + "", rowData);
                            totalTime += 1;
                            break;
                        case 3:
                            i++;
                            rowData = new LinkedHashMap<>();
                            rowData.put("1", "2.2." + currentIndex + "." + i + "");
                            rowData.put("1.1", customAction.getPriority() + "");
                            rowData.put("2", Constant.NA_VALUE);
                            rowData.put("3", MessageUtil.getResourceBundleMessage("pause.waiting.for.execute.continue"));
                            rowData.put("4", MessageUtil.getResourceBundleMessage("pause"));

                            rowData.put("6", customAction.getWaitReason());
                            rowData.put("7", "1");
                            rowData.put("8", "");
                            rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                    + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                    + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                                    + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                    + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                    + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                    + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                            rowData.put("10", "");
                            data.put(i + "", rowData);
                            totalTime += 1;
                            break;
                        case 4:
                            ActionServerService actionServerService = new ActionServerServiceImpl();
                            filters = new HashMap<>();
                            filters.put("actionId", actionId);
                            List<ActionServer> actionServers = actionServerService.findList(filters, new HashMap<>());

                            for (ActionServer actionServer : actionServers) {
                                // anhnt2
                                OsAccount osAccount = findOSAccountByIpServer(actionServer.getIpServer());
                                i++;
                                rowData = new LinkedHashMap<>();
                                rowData.put("1", "2.2." + currentIndex + "." + i + "");
                                rowData.put("1.1", customAction.getPriority() + "");
                                rowData.put("2", actionServer.getMonitorAccount() + "@" + actionServer.getIpServer());
                                rowData.put("3", MessageUtil.getResourceBundleMessage("root.su"));
                                rowData.put("4", "su -");

                                rowData.put("6", MessageUtil.getResourceBundleMessage("root.su.success"));
                                rowData.put("7", "1");
                                rowData.put("8", "");
                                rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                        + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                        + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                                        + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                        + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                        + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                        + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                                rowData.put("10", "");
                                data.put(i + "", rowData);
                                totalTime += 1;

                                i++;
                                rowData = new LinkedHashMap<>();
                                rowData.put("1", "2.2." + currentIndex + "." + i + "");
                                rowData.put("1.1", customAction.getPriority() + "");
                                rowData.put("2", "root@" + actionServer.getIpServer());
                                rowData.put("3", MessageUtil.getResourceBundleMessage("iptables.save"));
                                //rowData.put("4", "/etc/init.d/iptables save");
                                rowData.put("4", "cp /etc/sysconfig/iptables /etc/sysconfig/iptables_bk_$date" + "\r\n"
                                        + "iptables-save > /etc/sysconfig/iptables");

                                rowData.put("6", MessageUtil.getResourceBundleMessage("iptables.save.success"));
                                rowData.put("7", "1");
                                rowData.put("8", "");
                                rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                        + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                        + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                                        + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                        + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                        + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                        + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                                rowData.put("10", "");
                                data.put(i + "", rowData);
                                totalTime += 1;

                                i++;
                                rowData = new LinkedHashMap<>();
                                rowData.put("1", "2.2." + currentIndex + "." + i + "");
                                rowData.put("1.1", customAction.getPriority() + "");
                                rowData.put("2", "root@" + actionServer.getIpServer());
                                rowData.put("3", MessageUtil.getResourceBundleMessage("service.show"));
                                // anhnt2
                                Double osVersion = 7d;
                                /*20190125_hoangnd_xuat file DT theo lenh moi_start*/
//                                if (osAccount != null && osAccount.getOsVersion() != null && Double.valueOf(osAccount.getOsVersion()).compareTo(osVersion) >= 0) {
                                rowData.put("4", "systemctl list-units --type service --all 2>/dev/null| grep -w running | awk '{print $1}';\n" +
                                        "service --status-all 2>/dev/null | grep -w \"running...\" | awk '{print $1}' | grep -v \"rpc.\\|automount\\|hald\\|daemon\\|logd\\|filebeat\";\n" +
                                        "for l in $(printf %b 'haldaemon iptables \\nfilebeat'); do [[ `service $l status 2>/dev/null| grep \"running\\|Table: filter\\|Active: active\"| wc -l` -ge 1 ]] && echo $l; done");
//                                } else {
////                                    rowData.put("4", "service --status-all 2>&1 |egrep -w \"running...\" | awk '{print $1}' | grep -v \"\\.\" ");
//                                    rowData.put("4", "service --status-all 2>/dev/null | grep -w \"running...\" | awk '{print $1}' | grep -v \"rpc.\\|automount\\|hald\\|daemon\\|logd\\|filebeat\";\n" +
//                                            "for l in $(printf %b 'haldaemon iptables \\nfilebeat'); do [[ `service $l status 2>/dev/null| grep \"running\\|Table: filter\\|Active: active\"| wc -l` -ge 1 ]] && echo $l; done");
//                                }
                                /*20190125_hoangnd_xuat file DT theo lenh moi_end*/

                                rowData.put("6", MessageUtil.getResourceBundleMessage("service.success"));
                                rowData.put("7", "1");
                                rowData.put("8", "");
                                rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                        + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                        + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                                        + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                        + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                        + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                        + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                                rowData.put("10", "");
                                data.put(i + "", rowData);
                                totalTime += 1;

                                i++;
                                rowData = new LinkedHashMap<>();
                                rowData.put("1", "2.2." + currentIndex + "." + i + "");
                                rowData.put("1.1", customAction.getPriority() + "");
                                rowData.put("2", "root@" + actionServer.getIpServer());
                                rowData.put("3", MessageUtil.getResourceBundleMessage("ip.show"));
                                rowData.put("4", "ip a | egrep \"inet\" | egrep -v \"link|host\"");

                                rowData.put("6", MessageUtil.getResourceBundleMessage("ip.show.success"));
                                rowData.put("7", "1");
                                rowData.put("8", "");
                                rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                        + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                        + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                                        + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                        + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                        + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                        + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                                rowData.put("10", "");
                                data.put(i + "", rowData);
                                totalTime += 1;

                                i++;
                                rowData = new LinkedHashMap<>();
                                rowData.put("1", "2.2." + currentIndex + "." + i + "");
                                rowData.put("1.1", customAction.getPriority() + "");
                                rowData.put("2", "root@" + actionServer.getIpServer());
                                rowData.put("3", MessageUtil.getResourceBundleMessage("route.show"));
                                rowData.put("4", "ip route show");

                                rowData.put("6", MessageUtil.getResourceBundleMessage("ip.route.success"));
                                rowData.put("7", "1");
                                rowData.put("8", "");
                                rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                        + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                        + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                                        + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                        + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                        + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                        + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                                rowData.put("10", "");
                                data.put(i + "", rowData);
                                totalTime += 1;

                                i++;
                                rowData = new LinkedHashMap<>();
                                rowData.put("1", "2.2." + currentIndex + "." + i + "");
                                rowData.put("1.1", customAction.getPriority() + "");
                                rowData.put("2", "root@" + actionServer.getIpServer());
                                rowData.put("3", MessageUtil.getResourceBundleMessage("mount.show"));
                                rowData.put("4", "mount |egrep -v \"iso9660|gvfs-fuse-daemon|nfsd\"");

                                rowData.put("6", MessageUtil.getResourceBundleMessage("mount.success"));
                                rowData.put("7", "1");
                                rowData.put("8", "");
                                rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                        + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                        + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                                        + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                        + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                        + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                        + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                                rowData.put("10", "");
                                data.put(i + "", rowData);
                                totalTime += 1;


                                i++;
                                rowData = new LinkedHashMap<>();
                                rowData.put("1", "2.2." + currentIndex + "." + i + "");
                                rowData.put("1.1", customAction.getPriority() + "");
                                rowData.put("2", "root@" + actionServer.getIpServer());
                                if (action.getActionRbSd().equals(2l)) {
                                    rowData.put("3", MessageUtil.getResourceBundleMessage("root.shutdown"));
                                    if (osAccount != null && osAccount.getOsType().equals(AamConstants.OS_TYPE.WINDOWS)) {
                                        rowData.put("4", "cmd.exe /c shutdown /s");
                                    } else {
                                        rowData.put("4", "shutdown -h now");
                                    }
                                    rowData.put("6", MessageUtil.getResourceBundleMessage("root.shutdown.success"));

                                } else {
                                    rowData.put("3", MessageUtil.getResourceBundleMessage("root.reboot"));
                                    if (osAccount != null && osAccount.getOsType().equals(AamConstants.OS_TYPE.WINDOWS)) {
                                        rowData.put("4", "cmd.exe /c shutdown /r");
                                    } else {
                                        rowData.put("4", "reboot");
                                    }
                                    rowData.put("6", MessageUtil.getResourceBundleMessage("root.reboot.success"));
                                }


                                rowData.put("7", "1");
                                rowData.put("8", "");
                                rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                        + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                        + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                                        + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                        + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                        + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                        + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                                rowData.put("10", "");
                                data.put(i + "", rowData);
                                totalTime += 1;

                                i++;
                                rowData = new LinkedHashMap<>();
                                rowData.put("1", "2.2." + currentIndex + "." + i + "");
                                rowData.put("1.1", customAction.getPriority() + "");
                                rowData.put("2", "root@" + actionServer.getIpServer());
                                rowData.put("3", MessageUtil.getResourceBundleMessage("service.show"));
                                // anhnt2
                                /*20190125_hoangnd_xuat file DT theo lenh moi_start*/
//                                if (osAccount != null && osAccount.getOsVersion() != null && Double.valueOf(osAccount.getOsVersion()).compareTo(osVersion) >= 0) {
                                rowData.put("4", "systemctl list-units --type service --all 2>/dev/null| grep -w running | awk '{print $1}';\n" +
                                        "service --status-all 2>/dev/null | grep -w \"running...\" | awk '{print $1}' | grep -v \"rpc.\\|automount\\|hald\\|daemon\\|logd\\|filebeat\";\n" +
                                        "for l in $(printf %b 'haldaemon iptables \\nfilebeat'); do [[ `service $l status 2>/dev/null| grep \"running\\|Table: filter\\|Active: active\"| wc -l` -ge 1 ]] && echo $l; done");
//                                } else {
////                                    rowData.put("4", "service --status-all 2>&1 |egrep -w \"running...\" | awk '{print $1}' | grep -v \"\\.\" ");
//                                    rowData.put("4", "service --status-all 2>/dev/null | grep -w \"running...\" | awk '{print $1}' | grep -v \"rpc.\\|automount\\|hald\\|daemon\\|logd\\|filebeat\";\n" +
//                                            "for l in $(printf %b 'haldaemon iptables \\nfilebeat'); do [[ `service $l status 2>/dev/null| grep \"running\\|Table: filter\\|Active: active\"| wc -l` -ge 1 ]] && echo $l; done");
//                                }
                                /*20190125_hoangnd_xuat file DT theo lenh moi_end*/

                                rowData.put("6", MessageUtil.getResourceBundleMessage("service.success.after"));
                                rowData.put("7", "1");
                                rowData.put("8", "");
                                rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                        + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                        + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                                        + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                        + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                        + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                        + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                                rowData.put("10", "");
                                data.put(i + "", rowData);
                                totalTime += 1;

                                i++;
                                rowData = new LinkedHashMap<>();
                                rowData.put("1", "2.2." + currentIndex + "." + i + "");
                                rowData.put("1.1", customAction.getPriority() + "");
                                rowData.put("2", "root@" + actionServer.getIpServer());
                                rowData.put("3", MessageUtil.getResourceBundleMessage("ip.show"));
                                rowData.put("4", "ip a | egrep \"inet\" | egrep -v \"link|host\"");

                                rowData.put("6", MessageUtil.getResourceBundleMessage("ip.show.success.after"));
                                rowData.put("7", "1");
                                rowData.put("8", "");
                                rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                        + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                        + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                                        + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                        + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                        + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                        + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                                rowData.put("10", "");
                                data.put(i + "", rowData);
                                totalTime += 1;

                                i++;
                                rowData = new LinkedHashMap<>();
                                rowData.put("1", "2.2." + currentIndex + "." + i + "");
                                rowData.put("1.1", customAction.getPriority() + "");
                                rowData.put("2", "root@" + actionServer.getIpServer());
                                rowData.put("3", MessageUtil.getResourceBundleMessage("route.show"));
                                rowData.put("4", "ip route show");

                                rowData.put("6", MessageUtil.getResourceBundleMessage("ip.route.success.after"));
                                rowData.put("7", "1");
                                rowData.put("8", "");
                                rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                        + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                        + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                                        + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                        + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                        + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                        + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                                rowData.put("10", "");
                                data.put(i + "", rowData);
                                totalTime += 1;

                                i++;
                                rowData = new LinkedHashMap<>();
                                rowData.put("1", "2.2." + currentIndex + "." + i + "");
                                rowData.put("1.1", customAction.getPriority() + "");
                                rowData.put("2", "root@" + actionServer.getIpServer());
                                rowData.put("3", MessageUtil.getResourceBundleMessage("mount.show"));
                                rowData.put("4", "mount |egrep -v \"iso9660|gvfs-fuse-daemon|nfsd\"");

                                rowData.put("6", MessageUtil.getResourceBundleMessage("mount.success.after"));
                                rowData.put("7", "1");
                                rowData.put("8", "");
                                rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                        + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                        + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                                        + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                        + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                        + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                        + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                                rowData.put("10", "");
                                data.put(i + "", rowData);
                                totalTime += 1;
                            }
                            break;
                        default:
                            break;
                    }

                }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return data;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getTableCustomRb(Integer position, Long actionId, String mopRollBack, String crNumber, String date) {
        LinkedHashMap<String, LinkedHashMap<String, String>> data = new LinkedHashMap<>();
        LinkedHashMap<String, String> rowData;

        ActionCustomActionService actionService = new ActionCustomActionServiceImpl();
        ActionCustomGroupService groupService = new ActionCustomGroupServiceImpl();
        ActionModuleService serviceApp = new ActionModuleServiceImpl();

        List<ActionCustomAction> customActionRbs = new ArrayList<>();
        List<ActionCustomAction> rollbackCodeTests = new ArrayList<>();

        Map<String, Object> filters = new HashMap<>();
        filters.put("actionId", actionId);
        filters.put("rollbackAfter", position);
        filters.put("kbGroup", kbGroup);
        try {
            List<ActionCustomGroup> customGroups = groupService.findList(filters, new HashMap<String, String>());
            if (customGroups != null && !customGroups.isEmpty()) {
                for (ActionCustomGroup customGroup : customGroups) {
                    groupActionLabels.put(position, customGroup.getName());
                }

                filters = new HashMap<>();
                filters.put("actionCustomGroup.id", customGroups.get(0).getId());

                Map<String, String> orders = new HashMap<>();
                orders.put("priority", "DESC");
                List<ActionCustomAction> customActions = actionService.findList(filters, orders);

                for (ActionCustomAction customAction : customActions) {
                    if (customAction.getType() == 0) {
                        Cloner cloner = new Cloner();
                        ActionCustomAction rollbackTest = cloner.deepClone(customAction);
                        rollbackTest.setPriority(customAction.getRollbackTestPriority());
//						rollbackTest.setModuleAction(12);
                        /*if (customAction.getModuleAction() == 100)
                            rollbackTest.setModuleAction(200);
						else if (customAction.getModuleAction() == 101)
							rollbackTest.setModuleAction(201);
						else if (customAction.getModuleAction() == 102)
							rollbackTest.setModuleAction(202);*/

                        if (customAction.getModuleAction() == Constant.SPECIAL_UPCODETEST_RESTART_STOP_START)
                            rollbackTest.setModuleAction(Constant.SPECIAL_ROLLBACKTEST_RESTART_STOP_START);
                        else if (customAction.getModuleAction() == Constant.SPECIAL_UPCODETEST_RESTART)
                            rollbackTest.setModuleAction(Constant.SPECIAL_ROLLBACKTEST_RESTART);
                        else if (customAction.getModuleAction() == Constant.SPECIAL_UPCODETEST_START)
                            rollbackTest.setModuleAction(Constant.SPECIAL_ROLLBACKTEST_STOP);
                        else if (customAction.getModuleAction() == Constant.SPECIAL_UPCODETEST_STOP_START)
                            rollbackTest.setModuleAction(Constant.SPECIAL_ROLLBACKTEST_STOP_START);
                        else if (customAction.getModuleAction() == Constant.SPECIAL_RESTART_STOP_START)
                            rollbackTest.setModuleAction(Constant.SPECIAL_ROLLBACK_RESTART_STOP_START);
                        else if (customAction.getModuleAction() == Constant.SPECIAL_RESTART)
                            rollbackTest.setModuleAction(Constant.SPECIAL_ROLLBACK_RESTART);
                        else if (customAction.getModuleAction() == Constant.SPECIAL_START)
                            rollbackTest.setModuleAction(Constant.SPECIAL_ROLLBACK_STOP);

                        customActionRbs.add(rollbackTest);
                    } else {
                        customActionRbs.add(customAction);
                    }
                }

//				customActionRbs.addAll(rollbackCodeTests);
            }
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }

        if (customActionRbs.isEmpty())
            return null;
        Integer currentIndex = rbIndex++;
        Integer rollbackPos = 0;
        switch (position) {
            case Constant.ROLLBACK_STEP_CHECK_STATUS:
                rollbackPos = Constant.ROLLBACK_CUSTOM_STEP_CHECK_STATUS;
                break;
            case Constant.ROLLBACK_STEP_STOP_APP:
                rollbackPos = Constant.ROLLBACK_CUSTOM_STEP_STOP_APP;
                break;
            case Constant.ROLLBACK_STEP_SOURCE_CODE:
                rollbackPos = Constant.ROLLBACK_CUSTOM_STEP_UPCODE;
                break;
            case Constant.ROLLBACK_STEP_DB:
                rollbackPos = Constant.ROLLBACK_CUSTOM_STEP_TD_DB;
                break;
            case Constant.ROLLBACK_STEP_CLEARCACHE:
                rollbackPos = Constant.ROLLBACK_CUSTOM_STEP_CLEARCACHE;
                break;
            case Constant.ROLLBACK_STEP_RESTART_APP:
                rollbackPos = Constant.ROLLBACK_CUSTOM_STEP_RESTART_APP;
                break;
            case Constant.ROLLBACK_STEP_START_APP:
                rollbackPos = Constant.ROLLBACK_CUSTOM_STEP_START_APP;
                break;
            default:
                break;
        }
        indexMap.put(rollbackPos, currentIndex);

        try {
            Map<Long, ActionModule> mapApp = serviceApp.getMapObjByListAppId(actionId);
            int i = 0;
            int j = 0;
            if (rollbackCodeTests != null)

                for (ActionCustomAction customAction : customActionRbs) {
                    switch (customAction.getType()) {
                        case 0:
                            Long appId = customAction.getModuleId();
                            ActionModule module = mapApp.get(appId);
                            String key = module.getKeyword();
                            String link = module.getLogLink();

                            boolean codetaptrung = false;
                            if (module.getAppTypeCode() != null && module.getAppTypeCode().equals(Constant.CODETAPTRUNG_TYPE)) {
                                codetaptrung = true;
                            }

                            appId = customAction.getModuleId();
                            module = mapApp.get(appId);
                            String file = FilenameUtils.getName(customAction.getUploadCodePath());

                            MutableInt index = new MutableInt(i);
                            switch (customAction.getModuleAction()) {
                                case Constant.SPECIAL_UPCODETEST_RESTART_STOP_START:
                                    upcodeTestDt(position, currentIndex, index, customAction, module, data, mopRollBack, date, crNumber, file);
                                    stopCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    startCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    break;
                                case Constant.SPECIAL_UPCODETEST_RESTART:
                                    upcodeTestDt(position, currentIndex, index, customAction, module, data, mopRollBack, date, crNumber, file);
                                    restartCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    break;
                                case Constant.SPECIAL_UPCODETEST_START:
                                    upcodeTestDt(position, currentIndex, index, customAction, module, data, mopRollBack, date, crNumber, file);
                                    startCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    break;
                                case Constant.SPECIAL_UPCODETEST_STOP_START:
                                    stopCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    upcodeTestDt(position, currentIndex, index, customAction, module, data, mopRollBack, date, crNumber, file);
                                    startCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    break;
                                case Constant.SPECIAL_RESTART_STOP_START:
                                case Constant.SPECIAL_ROLLBACK_RESTART_STOP_START:
                                    stopCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    startCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    break;
                                case Constant.SPECIAL_RESTART:
                                case Constant.SPECIAL_ROLLBACK_RESTART:
                                    restartCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    break;
                                case Constant.SPECIAL_START:
                                    startCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    break;
                                case Constant.SPECIAL_ROLLBACKTEST_RESTART_STOP_START:
                                    rollbackUpcodeTestDt(position, currentIndex, index, customAction, module, data, mopRollBack, date, crNumber, file);
                                    stopCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    startCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    break;
                                case Constant.SPECIAL_ROLLBACKTEST_RESTART:
                                    rollbackUpcodeTestDt(position, currentIndex, index, customAction, module, data, mopRollBack, date, crNumber, file);
                                    restartCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    break;
                                case Constant.SPECIAL_ROLLBACKTEST_STOP:
                                    rollbackUpcodeTestDt(position, currentIndex, index, customAction, module, data, mopRollBack, date, crNumber, file);
                                    stopCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    break;
                                case Constant.SPECIAL_ROLLBACKTEST_STOP_START:
                                    stopCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    rollbackUpcodeTestDt(position, currentIndex, index, customAction, module, data, mopRollBack, date, crNumber, file);
                                    startCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    break;
                                case Constant.SPECIAL_ROLLBACK_STOP:
                                    stopCustomDt(position, data, mopRollBack, currentIndex, module, index, j, link, key, date, crNumber, codetaptrung, customAction);
                                    break;
                                default:
                                    break;
                            }
                            i = index.getValue();
                            break;
                        case 1:
                            ActionModuleServiceImpl service = new ActionModuleServiceImpl();
                            switch (customAction.getDbAction()) {
                                case 0:
                                    List<Long> ids = service.findListModuleId(actionId, null, false);
                                    List<Integer> listIds = new ArrayList<>();
                                    if (ids != null)
                                        for (Long id : ids) {
                                            listIds.add(id.intValue());
                                        }
                                    ServiceDatabase db = iimService.findServiceDbById(action.getImpactProcess().getNationCode(), customAction.getDbId());
/*											Module app = serviceApp.getObjByAppId(db.getAppId().longValue());
                                            if (app == null)
												app = new Module();*/
                                    file = FilenameUtils.getName(customAction.getDbScriptFile());
                                    i++;
                                    // du lieu cho 1 row

                                    rowData = new LinkedHashMap<>();
                                    rowData.put("1", "2.2." + currentIndex + "." + i + "");
                                    rowData.put("1.1", customAction.getPriority() + "");
                                    rowData.put("2", db.getIpVirtual() + "@" + db.getUsername() + "\r\n" + "URL : " + db.getUrl());
                                    rowData.put("3", MessageUtil.getResourceBundleMessage("run.script.configuration.db"));

                                    String action = MessageUtil.getResourceBundleMessage("execute.commands.sql.in.the.file") + " " + file;

                                    rowData.put("4", action);

                                    rowData.put("6", MessageUtil.getResourceBundleMessage("run.script.successful.have.not.error"));

                                    rowData.put("7", "5");
                                    rowData.put("8", "");
                                    rowData.put("9", "");
                                    rowData.put("10", "");
                                    data.put(db.getDbCode() + System.currentTimeMillis(), rowData);
                                    totalTime += 5;

                                    break;
                                case 1:
                                    db = iimService.findServiceDbById(this.action.getImpactProcess().getNationCode(), customAction.getDbId());
/*											Module app = serviceApp.getObjByAppId(db.getAppId().longValue());
                                            if (app == null)
												app = new Module();*/
//									file = FilenameUtils.getName(customAction.getDbScriptFile());
                                    i++;
                                    // du lieu cho 1 row

                                    rowData = new LinkedHashMap<>();
                                    rowData.put("1", "2.2." + currentIndex + "." + i + "");
                                    rowData.put("1.1", customAction.getPriority() + "");
                                    rowData.put("2", db.getIpVirtual() + "@" + db.getUsername() + "\r\n" + "URL : " + db.getUrl());
                                    rowData.put("3", MessageUtil.getResourceBundleMessage("import.data.to.db"));

                                    action = MessageUtil.getResourceBundleMessage("import.file.using.tool.import") + " " + customAction.getImportDataFile()
                                            + " " + MessageUtil.getResourceBundleMessage("to.db") + "\n" + MessageUtil.getResourceBundleMessage("command.import") + ": " + customAction.getSqlImport() + "\nSparator: " + customAction.getSeparator();

                                    rowData.put("4", action);

                                    rowData.put("6", MessageUtil.getResourceBundleMessage("import.successful"));
                                    rowData.put("7", "5");
                                    rowData.put("8", "");
                                    rowData.put("9", "");
                                    rowData.put("10", "");
                                    data.put(db.getDbCode() + System.currentTimeMillis(), rowData);
                                    totalTime += 5;
                                    break;
                                case 2:
                                    ids = service.findListModuleId(actionId, null, false);
                                    listIds = new ArrayList<>();
                                    if (ids != null)
                                        for (Long id : ids) {
                                            listIds.add(id.intValue());
                                        }

                                    db = iimService.findServiceDbById(this.action.getImpactProcess().getNationCode(), customAction.getDbId());
//									file = FilenameUtils.getName(customAction.getDbScriptFile());
                                    i++;
                                    // du lieu cho 1 row

                                    rowData = new LinkedHashMap<>();
                                    rowData.put("1", "2.2." + currentIndex + "." + i + "");
                                    rowData.put("1.1", customAction.getPriority() + "");
                                    rowData.put("2", db.getIpVirtual() + "@" + db.getUsername() + "\r\n" + "URL : " + db.getUrl());
                                    rowData.put("3", MessageUtil.getResourceBundleMessage("run.command.export.data"));

                                    action = MessageUtil.getResourceBundleMessage("export.data.follow.command") + "\n" + customAction.getExportStatement();
                                    if (StringUtils.isNotEmpty(customAction.getExportCount())) {
                                        action += "\n\n" + MessageUtil.getResourceBundleMessage("run.command.count.number.record") + "\n" + customAction.getExportCount();
                                    }

                                    rowData.put("4", action);

                                    rowData.put("6", MessageUtil.getResourceBundleMessage("export.data.successful"));
                                    rowData.put("7", "5");
                                    rowData.put("8", "");
                                    rowData.put("9", "");
                                    rowData.put("10", "");
                                    data.put(db.getDbCode() + System.currentTimeMillis(), rowData);
                                    totalTime += 5;
                                    break;
                                default:
                                    break;
                            }

                            break;
                        case 2:
                            i++;
                            ActionDtFileService dtFileService = new ActionDtFileServiceImpl();
                            ActionDtFile dtFile = dtFileService.findById(customAction.getFileId());
                            rowData = new LinkedHashMap<>();
                            if (StringUtils.isNotEmpty(dtFile.getRollbackFile())) {
                                rowData.put("1", "2.2." + currentIndex + "." + i + "");
                                rowData.put("1.1", customAction.getPriority() + "");
                                rowData.put("2", Constant.NA_VALUE);
                                rowData.put("3", dtFile.getRollbackDescription());
                                rowData.put("4", MessageUtil.getResourceBundleMessage("execute.follow.the.file") + " " + dtFile.getRollbackFile());

                                rowData.put("6", MessageUtil.getResourceBundleMessage("execute.successful"));
                                rowData.put("7", dtFile.getImpactTime() + "");
                                rowData.put("8", "");
                                rowData.put("9", "");
                                rowData.put("10", "");
                                data.put(i + "", rowData);
                                totalTime += 1;
                            }
                            break;
                        case 3:
                            i++;
                            rowData = new LinkedHashMap<>();
                            rowData.put("1", "2.2." + currentIndex + "." + i + "");
                            rowData.put("1.1", customAction.getPriority() + "");
                            rowData.put("2", Constant.NA_VALUE);
                            rowData.put("3", MessageUtil.getResourceBundleMessage("pause.waiting.for.execute.continue"));
                            rowData.put("4", MessageUtil.getResourceBundleMessage("pause"));

                            rowData.put("6", customAction.getWaitReason());
                            rowData.put("7", "1");
                            rowData.put("8", "");
                            rowData.put("9", "");
                            rowData.put("10", "");
                            data.put(i + "", rowData);
                            totalTime += 1;
                            break;
                        default:
                            break;
                    }

                }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return data;
    }

    private void restartCustomDt(Integer position, LinkedHashMap<String, LinkedHashMap<String, String>> data, String mopRollBack, Integer currentIndex, ActionModule module, MutableInt i, Integer j, String link, String key, String date, String crNumber, boolean codetaptrung, ActionCustomAction customAction) {
        LinkedHashMap<String, String> rowData;
//		String des;

        i.increment();
        rowData = new LinkedHashMap<>();
        rowData.put("1", "2.2." + currentIndex + "." + i + "");
        rowData.put("1.1", customAction.getPriority() + "");
        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
        rowData.put("3", MessageUtil.getResourceBundleMessage("forward.to.folder.install"));
        rowData.put("4", "cd " + module.getPath());

        rowData.put("6", MessageUtil.getResourceBundleMessage("moved.to.app.directory.success.have.prompt.when.command.end"));
        rowData.put("7", "1");
        rowData.put("8", "");
        rowData.put("9", "");
        rowData.put("10", "");
        data.put(i + "", rowData);
        totalTime += 1;


        i.increment();
        String startCmd = module.getRestartService();
        String startComment = "1. " + MessageUtil.getResourceBundleMessage("app.restarted.successful.have.prompt.in.start.line.when.end.command") + ".\n";
        if (!codetaptrung) {
            if (StringUtils.isNotEmpty(link) && StringUtils.isNotEmpty(key) && !link.trim().equals(Constant.NA_VALUE) && !key.trim().equals(Constant.NA_VALUE)) {
                startCmd += ";\n" + "tail -500f " + link + " | grep --color " + "\"" + key + "\";\n"
                        + "cat " + link + " | grep -a --color " + "\"" + key + "\"";
                startComment += "2. " + MessageUtil.getResourceBundleMessage("find.keyword.in.log.command.response.execute.command.3.when.keyword.not.found") + "\n" +
                        "3. " + MessageUtil.getResourceBundleMessage("find.keyword.in.log.command.response.execute.1.time.again.after.2.minute.when.keyword.not.found") + "\n";
            } else {
                startCmd += ";\n" + "ps -ef | grep \"" + LinuxParser.getFullPath(module.getPath()) + "\" | grep -v grep;";
                startComment += "2. " + MessageUtil.getResourceBundleMessage("have.pid.of.process.response.if.process.have.not.pid.wait.2.minute.and.then.execute.1.time.again") + "\n";
            }
        }
        rowData = new LinkedHashMap<>();
        rowData.put("1", "2.2." + currentIndex + "." + i + "");
        rowData.put("1.1", customAction.getPriority() + "");
        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
        rowData.put("3", "Restart module");
        rowData.put("4", startCmd);

        rowData.put("6", startComment);
        rowData.put("7", "3");
        rowData.put("8", "");
        rowData.put("9", rollbackDetail(position, mopRollBack));
        rowData.put("10", "");
        data.put(i + "", rowData);
        totalTime += 3;
    }

    private void stopCustomDt(Integer position, LinkedHashMap<String, LinkedHashMap<String, String>> data, String mopRollBack, Integer currentIndex, ActionModule module, MutableInt i, Integer j, String link, String key, String date, String crNumber, boolean codetaptrung, ActionCustomAction customAction) {
        LinkedHashMap<String, String> rowData;
        String des;

        i.increment();
//		j++;
        des = rollbackDetail(position, mopRollBack);
        rowData = new LinkedHashMap<>();
        rowData.put("1", "2.2." + currentIndex + "." + i + "");
        rowData.put("1.1", customAction.getPriority() + "");
        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
        rowData.put("3", MessageUtil.getResourceBundleMessage("forward.to.folder.install"));
        rowData.put("4", "cd " + module.getPath() + ";");

        rowData.put("5", MessageUtil.getResourceBundleMessage("moved.to.app.directory.success.have.prompt.when.command.end"));
        rowData.put("6", "1");

        rowData.put("8", "");
        rowData.put("9", des);
        rowData.put("10", "");
        data.put(i + "", rowData);
        totalTime += 1;
        // dung module

        i.increment();
        rowData = new LinkedHashMap<>();
        rowData.put("1", "2.2." + currentIndex + "." + i + "");
        rowData.put("1.1", customAction.getPriority() + "");
        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
        rowData.put("3", MessageUtil.getResourceBundleMessage("stop.module"));
        String stopCmd = codetaptrung ? module.getStopService() : module.getStopService() + ";" + "\r\n" + "\r\n" + "ps -ef | grep \"" + LinuxParser.getFullPath(module.getPath()) + "\" | grep -v grep; " + "\r\n" + "kill -9 PID;";
        rowData.put("4", stopCmd);
        String expect = "1. " + MessageUtil.getResourceBundleMessage("stop.app.success.have.command.prompt.when.command.end") + "\n"
                + "2. " + MessageUtil.getResourceBundleMessage("if.after.desired.time.execute.command.without.command.prompt.press.ctrl.c");
        if (!codetaptrung) {
            expect += ", " + MessageUtil.getResourceBundleMessage("execute.command.get.PID.of.process") + "\r\n"
                    + MessageUtil.getResourceBundleMessage("example.get.PID.red.shown.below") + " (PID = 20894)"
                    + "\r\n"
                    + "$ ps -ef | grep /u01/BCCS_CODINH/CM_APP/jdk1.6.0_26/ | grep -v grep;"
                    + "\r\n "
                    + "5503      20894     1  2 Apr14 ?        03:12:52 "
                    + "\r\n"
                    + "/u01/BCCS_CODINH/CM_APP/jdk1.6.0_26/bin/java -"
                    + "\r\n"
                    + "3. " + MessageUtil.getResourceBundleMessage("pid.process.have.still.stop.process.using.command.kill.9.pid");
        }
        rowData.put("5", expect);
        rowData.put("6", "3");
        rowData.put("8", "");
        rowData.put("9", des);
        rowData.put("10", "");
        data.put(i + "", rowData);
        totalTime += 3;

        if (!codetaptrung) {
            i.increment();
            rowData = new LinkedHashMap<>();
            rowData.put("1", "2.2." + currentIndex + "." + i + "");
            rowData.put("1.1", customAction.getPriority() + "");
            rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
            rowData.put("3", MessageUtil.getResourceBundleMessage("check.module.has.stopped.altogether"));
            rowData.put("4", "ps  -ef | grep \"" + LinuxParser.getFullPath(module.getPath()) + "\" | grep -v grep");
            rowData.put("5", MessageUtil.getResourceBundleMessage("no.process.line.app.appears.there.is.command.prompt.after.end.command.process.has.been.stopped"));
            rowData.put("6", "1");

            rowData.put("8", "");
            rowData.put("9", des);
            rowData.put("10", "");
            data.put(i + "", rowData);
            totalTime += 1;
        }
    }

    private void startCustomDt(Integer position, LinkedHashMap<String, LinkedHashMap<String, String>> data, String mopRollBack, Integer currentIndex, ActionModule module, MutableInt i, Integer j, String link, String key, String date, String crNumber, boolean codetaptrung, ActionCustomAction customAction) {
        LinkedHashMap<String, String> rowData;
        String des;
//		j++;
        des = rollbackDetail(position, mopRollBack);
        i.increment();
        rowData = new LinkedHashMap<>();
        rowData.put("1", "2.2." + currentIndex + "." + i + "");
        rowData.put("1.1", customAction.getPriority() + "");
        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
        rowData.put("3", MessageUtil.getResourceBundleMessage("forward.to.folder.install"));
        rowData.put("4", "cd " + module.getPath() + ";");

        rowData.put("6", MessageUtil.getResourceBundleMessage("moved.to.app.directory.success.have.prompt.when.command.end"));
        rowData.put("7", "1");
        rowData.put("8", "");
        rowData.put("9", des);
        rowData.put("10", "2.2.8." + i + "");
        data.put(i + "", rowData);
        totalTime += 1;

        if (StringUtils.isNotEmpty(link) && !link.trim().equals(Constant.NA_VALUE)) {

            i.increment();
            rowData = new LinkedHashMap<>();
            rowData.put("1", "2.2." + currentIndex + "." + i + "");
            rowData.put("1.1", customAction.getPriority() + "");
            rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
            rowData.put("3", MessageUtil.getResourceBundleMessage("backup.file.log.start.current"));
            rowData.put("4", "cp " + link + " " + link + "bk_" + crNumber + "_" + date + ";\n"
                    + "zip -m " + link + "bk_" + crNumber + "_" + date + ".zip " + link + "bk_" + crNumber + "_" + date);

            rowData.put("6", "1. " + MessageUtil.getResourceBundleMessage("backup.file.log.successful.have.prompt.in.start.line.when.end.command") + ".\n" +
                    "2. " + MessageUtil.getResourceBundleMessage("zip.and.delete.file.log.after.backup.successful.have.prompt.in.start.line.when.end.command") + ".\n");
            rowData.put("7", "2");
            rowData.put("8", "");
            rowData.put("9", des);
            rowData.put("10", "");
            data.put(i + "", rowData);
            totalTime += 2;
            i.increment();
            rowData = new LinkedHashMap<>();
            rowData.put("1", "2.2." + currentIndex + "." + i + "");
            rowData.put("1.1", customAction.getPriority() + "");
            rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
            rowData.put("3", MessageUtil.getResourceBundleMessage("before.enable.module.has.made.empty.log.file"));
            rowData.put("4", "> " + link);

            rowData.put("6", MessageUtil.getResourceBundleMessage("has.made.empty.log.file.successful.have.prompt.in.start.line.when.end.command") + ".");
            rowData.put("7", "1");
            rowData.put("8", "");
            rowData.put("9", des);
            rowData.put("10", "");
            data.put(i + "", rowData);
            totalTime += 1;
        }
        i.increment();
        rowData = new LinkedHashMap<>();
        rowData.put("1", "2.2." + currentIndex + "." + i + "");
        rowData.put("1.1", customAction.getPriority() + "");
        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
        rowData.put("3", MessageUtil.getResourceBundleMessage("execute.start.module"));
        String startCmd = module.getStartService();
        String startComment = "1. " + MessageUtil.getResourceBundleMessage("app.started.successful.have.prompt.in.start.line.when.end.command") + ".\n";
        if (!codetaptrung) {
            if (StringUtils.isNotEmpty(link) && StringUtils.isNotEmpty(key) && !link.trim().equals(Constant.NA_VALUE) && !key.trim().equals(Constant.NA_VALUE)) {
                startCmd += ";\n" + "tail -500f " + link + " | grep --color " + "\"" + key + "\";\n"
                        + "cat " + link + " | grep -a --color " + "\"" + key + "\"";
                startComment += "2. " + MessageUtil.getResourceBundleMessage("find.keyword.in.log.command.response.execute.command.3.when.keyword.not.found") + "\n" +
                        "3. " + MessageUtil.getResourceBundleMessage("find.keyword.in.log.command.response.execute.1.time.again.after.2.minute.when.keyword.not.found") + "\n";
            } else {
                startCmd += ";\n" + "ps -ef | grep \"" + LinuxParser.getFullPath(module.getPath()) + "\" | grep -v grep;";
                startComment += "2. " + MessageUtil.getResourceBundleMessage("have.pid.of.process.response.if.process.have.not.pid.wait.2.minute.and.then.execute.1.time.again") + "\n";
            }
        }
        rowData.put("4", startCmd);
        rowData.put("6", startComment);
        rowData.put("7", "1");
        rowData.put("8", "");
        rowData.put("9", des);
        rowData.put("10", "");
        data.put(i + "", rowData);
        totalTime += 1;
    }

    private void rollbackUpcodeTestDt(Integer position, Integer currentIndex, MutableInt i, ActionCustomAction customAction, ActionModule module, LinkedHashMap<String, LinkedHashMap<String, String>> data, String mopRollBack, String date, String crNumber, String file) {
        i.increment();
        LinkedHashMap<String, String> rowData;
        rowData = new LinkedHashMap<>();
        rowData.put("1", "2.2." + currentIndex + "." + i + "");
        rowData.put("1.1", customAction.getPriority() + "");
        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
        rowData.put("3", MessageUtil.getResourceBundleMessage("forward.to.folder.install"));
        rowData.put("4", "cd " + module.getPath() + ";");

        rowData.put("6", MessageUtil.getResourceBundleMessage("moved.to.app.directory.success.have.prompt.when.command.end"));
        rowData.put("7", "1");
        rowData.put("8", "");
        rowData.put("9", rollbackDetail(position, mopRollBack));
        rowData.put("10", "");
        data.put(i + "", rowData);
        totalTimeRollback += 1;

        i.increment();
        rowData = new LinkedHashMap<>();
        rowData.put("1", "2.2." + currentIndex + "." + i + "");
        rowData.put("1.1", customAction.getPriority() + "");
        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
        rowData.put("3", MessageUtil.getResourceBundleMessage("backup.is.the.code.fail.configuration"));
        rowData.put("4", "mv " + customAction.getUpcodePath() + " backup_toantrinh/" + customAction.getUpcodePath().replaceAll("\\.\\./", "") + "_fail_" + date
                + "_" + crNumber + "_test");

        rowData.put("6", MessageUtil.getResourceBundleMessage("move.to.folder.code.in.local.successful.have.prompt.in.start.line.when.end.command"));
        rowData.put("7", "1");
        rowData.put("8", "");
        rowData.put("9", rollbackDetail(position, mopRollBack));
        rowData.put("10", "");
        data.put(i + "", rowData);
        totalTimeRollback += 1;
        i.increment();
        rowData = new LinkedHashMap<>();
        rowData.put("1", "2.2." + currentIndex + "." + i + "");
        rowData.put("1.1", customAction.getPriority() + "");
        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
        rowData.put("3", MessageUtil.getResourceBundleMessage("rollback.code"));
        rowData.put("4", "cp -r  backup_toantrinh/" + customAction.getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date + "_" + crNumber + "_test" + " "
                + customAction.getUpcodePath());

        rowData.put("6", MessageUtil.getResourceBundleMessage("rollback.folder.code.config.successful.have.prompt.in.start.line.when.end.command"));
        rowData.put("7", "2");
        rowData.put("8", "");
        rowData.put("9", rollbackDetail(position, mopRollBack));
        rowData.put("10", "");
        data.put(i + "", rowData);
        totalTimeRollback += 2;
        //Quytv720190917_bo sung them lenh diff -rq start
        i.increment();
        rowData = new LinkedHashMap<>();
        rowData.put("1", "2.2." + currentIndex + "." + i + "");
        rowData.put("1.1", customAction.getPriority() + "");
        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
        rowData.put("3", MessageUtil.getResourceBundleMessage("compare.code"));
        rowData.put("4", "diff -rq backup_toantrinh/" + customAction.getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date + "_" + crNumber + "_test" + " "
                + customAction.getUpcodePath());

        rowData.put("6", MessageUtil.getResourceBundleMessage("compare.code.result"));
        rowData.put("7", "2");
        rowData.put("8", "");
        rowData.put("9", rollbackDetail(position, mopRollBack));
        rowData.put("10", "");
        data.put(i + "", rowData);
        totalTimeRollback += 1;
        //Quytv720190917_bo sung them lenh diff -rq end
        i.increment();
        rowData = new LinkedHashMap<>();
        rowData.put("1", "2.2." + currentIndex + "." + i + "");
        rowData.put("1.1", customAction.getPriority() + "");
        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
        rowData.put("3", MessageUtil.getResourceBundleMessage("check.for.existence.of.directory.code.after.rollback"));
        rowData.put("4", "cd " + customAction.getUpcodePath());

        rowData.put("6", MessageUtil.getResourceBundleMessage("have.prompt.in.start.line.when.end.command"));
        rowData.put("7", "1");
        rowData.put("8", "");
        rowData.put("9", rollbackDetail(position, mopRollBack));
        rowData.put("10", "");
        data.put(i + "", rowData);
        totalTimeRollback += 1;
    }

    private void upcodeTestDt(Integer position, Integer currentIndex, MutableInt i, ActionCustomAction customAction, ActionModule module, LinkedHashMap<String, LinkedHashMap<String, String>> data, String mopRollBack, String date, String crNumber, String file) {
        // cd vào thu muc cai dat 3.2.5.
        i.increment();
        LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
        rowData.put("1", "2.2." + currentIndex + "." + i + "");
        rowData.put("1.1", customAction.getPriority() + "");
        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
        rowData.put("3", MessageUtil.getResourceBundleMessage("move.to.application.directory"));
        rowData.put("4", "cd " + module.getPath());

        rowData.put("6", MessageUtil.getResourceBundleMessage("moved.to.app.directory.success.have.prompt.when.command.end"));
        rowData.put("7", "1");
        rowData.put("8", "");
        rowData.put("9", rollbackDetail(position, mopRollBack));
        rowData.put("10", "");
        data.put(i + "", rowData);
        totalTime += 1;

        i.increment();
        rowData = new LinkedHashMap<>();
        rowData.put("1", "2.2." + currentIndex + "." + i + "");
        rowData.put("1.1", customAction.getPriority() + "");
        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
        rowData.put("3", MessageUtil.getResourceBundleMessage("create.folders") + " backup_toantrinh");
        rowData.put("4", "mkdir -p backup_toantrinh/" + FilenameUtils.getPathNoEndSeparator(customAction.getUpcodePath()).replaceAll("\\.\\.", ""));

        rowData.put("6", MessageUtil.getResourceBundleMessage("have.prompt.in.start.line.when.end.command"));
        rowData.put("7", "1");
        rowData.put("8", "");
        rowData.put("9", rollbackDetail(position, mopRollBack));
        rowData.put("10", "");
        data.put(i + "", rowData);
        totalTime += 1;

        i.increment();
        rowData = new LinkedHashMap<>();
        rowData.put("1", "2.2." + currentIndex + "." + i + "");
        rowData.put("1.1", customAction.getPriority() + "");
        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
        rowData.put("3", MessageUtil.getResourceBundleMessage("backup.code.configuration"));
        rowData.put("4", "cp -r " + customAction.getUpcodePath() + " backup_toantrinh/" + customAction.getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date + "_" + crNumber + (customAction.getModuleAction() == 11 ? "_test" : ""));
        rowData.put("5", MessageUtil.getResourceBundleMessage("backup.folder.code.config.successful.have.prompt.in.start.line.when.end.command"));
        rowData.put("6", "2");

        rowData.put("8", "");
        rowData.put("9", rollbackDetail(position, mopRollBack));
        rowData.put("10", "");
        data.put(i + "", rowData);
        totalTime += 2;

        //Quytv720190917_bo sung them lenh diff -rq start
        i.increment();
        rowData = new LinkedHashMap<>();
        rowData.put("1", "2.2." + currentIndex + "." + i + "");
        rowData.put("1.1", customAction.getPriority() + "");
        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
        rowData.put("3", MessageUtil.getResourceBundleMessage("compare.code"));
        rowData.put("4", "diff -rq " + customAction.getUpcodePath() + " backup_toantrinh/" + customAction.getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date + "_" + crNumber + (customAction.getModuleAction() == 11 ? "_test" : ""));
        rowData.put("5", MessageUtil.getResourceBundleMessage("compare.code.result"));
        rowData.put("6", "2");

        rowData.put("8", "");
        rowData.put("9", rollbackDetail(position, mopRollBack));
        rowData.put("10", "");
        data.put(i + "", rowData);
        totalTime += 1;
        //Quytv720190917_bo sung them lenh diff -rq end


        i.increment();
        rowData = new LinkedHashMap<>();
        rowData.put("1", "2.2." + currentIndex + "." + i + "");
        rowData.put("1.1", customAction.getPriority() + "");
        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
        rowData.put("3", MessageUtil.getResourceBundleMessage("check.the.existence.of.the.backup.directory"));
        rowData.put("4", "cd " + "backup_toantrinh/" + customAction.getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date + "_" + crNumber + (customAction.getModuleAction() == 11 ? "_test" : ""));
        rowData.put("5", MessageUtil.getResourceBundleMessage("have.prompt.in.start.line.when.end.command"));
        rowData.put("6", "1");

        rowData.put("8", "");
        rowData.put("9", rollbackDetail(position, mopRollBack));
        rowData.put("10", "");
        data.put(i + "", rowData);
        totalTime += 1;

        i.increment();
        rowData = new LinkedHashMap<>();
        rowData.put("1", "2.2." + currentIndex + "." + i + "");
        rowData.put("1.1", customAction.getPriority() + "");
        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
        rowData.put("3", MessageUtil.getResourceBundleMessage("switch.to.sftp.mode.press.alt.p.key.combination.then.go.to.installation.directory"));
        rowData.put("4", "cd " + module.getPath());

        rowData.put("6", MessageUtil.getResourceBundleMessage("moved.to.app.directory.success.have.prompt.when.command.end"));
        rowData.put("7", "1");
        rowData.put("8", "");
        rowData.put("9", rollbackDetail(position, mopRollBack));
        rowData.put("10", "");
        data.put(i + "", rowData);
        totalTime += 1;

        i.increment();
        rowData = new LinkedHashMap<>();
        rowData.put("1", "2.2." + currentIndex + "." + i + "");
        rowData.put("1.1", customAction.getPriority() + "");
        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
        rowData.put("3", MessageUtil.getResourceBundleMessage("switch.to.sftp.mode.press.alt.p.key.combination.and.then.transfer.to.the.original.preparation.directory") + ".\n" +
                MessageUtil.getResourceBundleMessage("attention") + ": ~ " + MessageUtil.getResourceBundleMessage("is.the.relative.path.to.the.local") + "\n");
        rowData.put("4", "lcd " + "\"~\\" + crNumber + "\\" + action.getCreatedBy() + "_" + action.getTdCode() + "\\source_code" + "\"");

        rowData.put("6", MessageUtil.getResourceBundleMessage("move.to.folder.code.in.local.successful.have.prompt.in.start.line.when.end.command"));
        rowData.put("7", "1");
        rowData.put("8", "");
        rowData.put("9", rollbackDetail(position, mopRollBack));
        rowData.put("10", "");
        data.put(i + "", rowData);
        totalTime += 1;

        i.increment();
        rowData = new LinkedHashMap<>();
        rowData.put("1", "2.2." + currentIndex + "." + i + "");
        rowData.put("1.1", customAction.getPriority() + "");
        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
        rowData.put("3", MessageUtil.getResourceBundleMessage("upload.code.config.with.sftp.session.on.tool.secure.crt"));
        if (!Util.isNullOrEmpty(module.getInstalledUser()) && module.getInstalledUser().equalsIgnoreCase("root")) {
            rowData.put("4", "put " + file + "\n" +
                    "cd backup_toantrinh_upload_file" + "\n" +
                    "mv 'backup_toantrinh_upload_file' '" + module.getPath() + "'" + "\n");
        } else {
            rowData.put("4", "put " + file);
        }

        rowData.put("6", MessageUtil.getResourceBundleMessage("push.file") + " " + file + " "
                + MessageUtil.getResourceBundleMessage("to.the.application.directory.successful.the.files.pushed.up.to.100.percent.example.below") + "\r\n"
                + "sftp> put thread_node2.zip Uploading agent.cfg to /u02/app/interchg/interchange_node1/process/module/InterChangeProcess.jar "
                + "\r\n" + "100% 137KB    137KB/s 00:00:00");
        rowData.put("7", "1");
        rowData.put("8", "");
        rowData.put("9", rollbackDetail(position, mopRollBack));
        rowData.put("10", "");
        data.put(i + "", rowData);
        totalTime += 1;

        if (StringUtils.isNotEmpty(customAction.getLstFileRemove())) {
            i.increment();
            rowData = new LinkedHashMap<>();
            rowData.put("1", "2.2." + currentIndex + "." + i + "");
            rowData.put("1.1", customAction.getPriority() + "");
            rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
            rowData.put("3", MessageUtil.getResourceBundleMessage("delete.file.code"));
            rowData.put("4", "rm -rf " + customAction.getLstFileRemove().replaceAll(",", ""));

            rowData.put("6", MessageUtil.getResourceBundleMessage("have.prompt.in.start.line.when.end.command"));
            rowData.put("7", "1");
            rowData.put("8", "");
            rowData.put("9", rollbackDetail(position, mopRollBack));
            rowData.put("10", "");
            data.put(i + "", rowData);
            totalTime += 1;
        }

        i.increment();
        rowData = new LinkedHashMap<>();
        rowData.put("1", "2.2." + currentIndex + "." + i + "");
        rowData.put("1.1", customAction.getPriority() + "");
        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
        rowData.put("3", MessageUtil.getResourceBundleMessage("switch.to.ssh.session.mode") + " " + MessageUtil.getResourceBundleMessage("override.code.config.over.current"));
        if (file != null && file.length() > 4)
            rowData.put("4", "unzip -o '" + file + "'" + (StringUtils.isNotEmpty(FilenameUtils.getPathNoEndSeparator(customAction.getUpcodePath())) ? " -d " + FilenameUtils.getPathNoEndSeparator(customAction.getUpcodePath()) : "") + ";" + "\r\n" + "mv '" + file + "' 'backup_toantrinh/" + FilenameUtils.getPath(customAction.getUpcodePath().replaceAll("\\.\\./", "")) + file.substring(0, file.length() - 4)
                    + "_" + crNumber + (customAction.getModuleAction() == 11 ? "_test" : "") + "_" + new SimpleDateFormat("ddMMyyyHHmmss").format(new Date()) + "." + FilenameUtils.getExtension(file) + "';");
        else
            rowData.put("4", "");

        rowData.put("6", "1." + MessageUtil.getResourceBundleMessage("execute.extract.overwrites.new.code.files.in.the.current.code.directory") + "\r\n"
                + MessageUtil.getResourceBundleMessage("have.prompt.in.start.line.when.end.command") + "\r\n"
                + "Ví dụ:" + "\r\n"
                + "$ unzip -o 'thread_node1.zip'" + "\r\n" + "Archive:  thread_node1.zip" + "\r\n"
                + "inflating: etc/app.conf" + "\r\n" + "2. " + MessageUtil.getResourceBundleMessage("have.prompt.in.start.line.when.end.command"));
        rowData.put("7", "1");
        rowData.put("8", "");
        rowData.put("9", rollbackDetail(position, mopRollBack));
        rowData.put("10", "");
        data.put(i + "", rowData);
        totalTime += 1;
    }

    private int getIndexRollbackStop(List<ActionDetailApp> listDetails, Map<Long, Integer> moduleStart, int failModuleIndex) {
        int index = 1;
        for (int i = 0; i < listDetails.size() - failModuleIndex + 1; i++) {
            index += moduleStart.get(listDetails.get(i).getModuleId());
        }

        return index;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getTableStopAppRb(Long actionId, String mopRollBack) {
        LinkedHashMap<String, LinkedHashMap<String, String>> data = new LinkedHashMap<>();
        LinkedHashMap<String, String> rowData = null;
        ActionDetailAppServiceImpl serviceDetailApp = new ActionDetailAppServiceImpl();
        ActionDetailDatabaseServiceImpl serviceDetailDb = new ActionDetailDatabaseServiceImpl();
        ActionModuleService serviceApp = new ActionModuleServiceImpl();

        try {
            Map<Long, ActionModule> mapApp = serviceApp.getMapObjByListAppId(actionId);
            List<ActionDetailApp> listDetails = serviceDetailApp.findListDetailApp(actionId, "5", false, moduleIds, false);

            int i = 0;

            List<ActionDetailDatabase> listDetailDbs = serviceDetailDb.findListDetailDb(actionId, kbGroup, true, false);
            if (listDetailDbs != null) {

                for (ActionDetailDatabase actionDetailDb : listDetailDbs) {
                    if (!actionDetailDb.getType().equals(Constant.TD_DB_TYPE_STOP_START))
                        continue;

                    ServiceDatabase module = iimService.findServiceDbById(action.getImpactProcess().getNationCode(), actionDetailDb.getAppDbId());
                    String file = FilenameUtils.getName(actionDetailDb.getScriptExecute());
                    i++;
                    // du lieu cho 1 row

                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2." + 1 + "." + i + "");
                    rowData.put("1.1", actionDetailDb.getActionOrder() + "");
                    rowData.put("2", module.getIpVirtual() + "@" + module.getUsername() + "\r\n" + "URL : " + module.getUrl());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("run.script.turn.off.flag"));
                    String action;
                    if (actionDetailDb.getTypeImport() == null)
                        rowData.put("4", MessageUtil.getResourceBundleMessage("need.not.execute"));
                    else {
                        if (actionDetailDb.getTypeImport() == 1L)
                            if (file != null)
                                action = MessageUtil.getResourceBundleMessage("execute.commands.sql.in.the.file") + " " + file;
                            else
                                action = MessageUtil.getResourceBundleMessage("need.not.execute");
                        else {
                            action = "-- " + MessageUtil.getResourceBundleMessage("execute.commands.follow.script") + ": " + "\r\n" + actionDetailDb.getScriptText();
                        }
                        rowData.put("4", action);
                    }
                    rowData.put("6", MessageUtil.getResourceBundleMessage("run.script.successful.have.not.error"));
                    rowData.put("7", "5");
                    rowData.put("8", "");
                    rowData.put("9", "");
                    rowData.put("10", "");
                    data.put(module.getDbCode() + i++, rowData);
                    totalTime += 5;
                }
            }

            if (listDetails != null) {
                // buoc thuc hien 3.2.1
                for (ActionDetailApp actionDetailApp : listDetails) {
                    Long appId = actionDetailApp.getModuleId();
                    ActionModule module = mapApp.get(appId);

                    boolean codetaptrung = false;
                    if (module.getAppTypeCode() != null && module.getAppTypeCode().equals(Constant.CODETAPTRUNG_TYPE)) {
                        codetaptrung = true;
                    }

                    if (AamConstants.OS_TYPE.WINDOWS != module.getOsType()) {
                        // cd vào thu muc cai dat
                        i++;
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "3.2.1." + i + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("forward.to.folder.install"));
                        rowData.put("4", "cd " + module.getPath() + ";");
                        rowData.put("5", MessageUtil.getResourceBundleMessage("moved.to.app.directory.success.have.prompt.when.command.end"));
                        rowData.put("6", "1");

                        rowData.put("8", "");
                        rowData.put("9", "");
                        rowData.put("10", "");
                        data.put(i + "", rowData);
                        totalTimeRollback += 1;
                        // dung module
                    }

                    i++;
                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2.1." + i + "");
                    rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                    rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("stop.module"));
                    String stopCmd;
                    String expect;
                    if (StringUtils.isNotEmpty(module.getViewStatus()) && StringUtils.isNotEmpty(module.getKeyStatusStart()) && !module.getViewStatus().toUpperCase().equals(Constant.NA_VALUE)) {
                        stopCmd = module.getStopService();
                        rowData.put("4", stopCmd);
                        expect = "1. " + MessageUtil.getResourceBundleMessage("stop.app.success.have.command.prompt.when.command.end") + "\n"
                                + "2. " + MessageUtil.getResourceBundleMessage("if.after.desired.time.execute.command.without.command.prompt.press.ctrl.c");
                    } else {
                        stopCmd = codetaptrung ? module.getStopService() : module.getStopService() + ";" + "\r\n" + "\r\n" + "ps -ef | grep \"" + LinuxParser.getFullPath(module.getPath()) + "\" | grep -v grep; " + "\r\n" + "kill -9 PID;";
                        rowData.put("4", stopCmd);
                        expect = "1. " + MessageUtil.getResourceBundleMessage("stop.app.success.have.command.prompt.when.command.end") + "\n"
                                + "2. " + MessageUtil.getResourceBundleMessage("if.after.desired.time.execute.command.without.command.prompt.press.ctrl.c");

                        if (!codetaptrung) {
                            expect += ", " + MessageUtil.getResourceBundleMessage("execute.command.get.PID.of.process") + "\r\n"
                                    + MessageUtil.getResourceBundleMessage("example.get.PID.red.shown.below") + " (PID = 20894)"
                                    + "\r\n"
                                    + "$ ps -ef | grep /u01/BCCS_CODINH/CM_APP/jdk1.6.0_26/ | grep -v grep;"
                                    + "\r\n "
                                    + "5503      20894     1  2 Apr14 ?        03:12:52 "
                                    + "\r\n"
                                    + "/u01/BCCS_CODINH/CM_APP/jdk1.6.0_26/bin/java -"
                                    + "\r\n"
                                    + "3. " + MessageUtil.getResourceBundleMessage("pid.process.have.still.stop.process.using.command.kill.9.pid");
                        }
                    }
                    rowData.put("5", expect);
                    rowData.put("6", "3");

                    rowData.put("8", "");
                    rowData.put("9", "");
                    rowData.put("10", "");
                    data.put(i + "", rowData);
                    totalTimeRollback += 3;

                    if (!codetaptrung) {
                        if (StringUtils.isNotEmpty(module.getViewStatus()) && StringUtils.isNotEmpty(module.getKeyStatusStart()) && !module.getViewStatus().toUpperCase().equals(Constant.NA_VALUE)) {
                            i++;
                            rowData = new LinkedHashMap<>();
                            rowData.put("1", "2.2.1." + i + "");
                            rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                            rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                            rowData.put("3", MessageUtil.getResourceBundleMessage("check.module.has.stopped.altogether"));
                            rowData.put("4", module.getViewStatus());
                            rowData.put("5", MessageUtil.getResourceBundleMessage("have.not.keyword") + " \"" + module.getKeyStatusStart()
                                    + "\" " + MessageUtil.getResourceBundleMessage("appears.in.begin.line.has.command.prompt.after.end.command.process.has.been.stopped"));
                            rowData.put("6", "1");

                            rowData.put("8", "");
                            rowData.put("9", "");
                            rowData.put("10", "");
                            data.put(i + "", rowData);
                            totalTime += 1;
                        } else {
                            i++;
                            rowData = new LinkedHashMap<>();
                            rowData.put("1", "2.2.1." + i + "");
                            rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                            rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                            rowData.put("3", MessageUtil.getResourceBundleMessage("check.module.has.stopped.altogether"));
                            rowData.put("4", "ps  -ef | grep \"" + LinuxParser.getFullPath(module.getPath()) + "\" | grep -v grep;");
                            rowData.put("5", MessageUtil.getResourceBundleMessage("no.process.line.app.appears.there.is.command.prompt.after.end.command.process.has.been.stopped"));
                            rowData.put("6", "1");

                            rowData.put("8", "");
                            rowData.put("9", "");
                            rowData.put("10", "");
                            data.put(i + "", rowData);
                            totalTimeRollback += 1;
                        }
                    }
                }
            }


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return data;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getTableBackupCode(Long actionId, String dateTime, String mopRollBack, String crNumber) {
        LinkedHashMap<String, LinkedHashMap<String, String>> data = new LinkedHashMap<>();
        LinkedHashMap<String, String> rowData = null;
        ActionModuleService serviceApp = new ActionModuleServiceImpl();
        ActionDetailAppServiceImpl serviceDetailApp = new ActionDetailAppServiceImpl();

        try {

//			List<Long> ids = service.findListModuleId(actionId);
            Map<Long, ActionModule> mapApp = serviceApp.getMapObjByListAppId(actionId);
            List<ActionDetailApp> listDetails = serviceDetailApp.findListDetailApp(actionId, "2", moduleIds, false);

            Integer currentIndex = indexMap.get(Constant.SUB_STEP_BACKUP_APP);

            int i = 0;
            if (listDetails != null)

                for (ActionDetailApp actionDetailApp : listDetails) {
                    Long appId = actionDetailApp.getModuleId();
                    ActionModule module = mapApp.get(appId);
                    String upcodePath = actionDetailApp.getUpcodePath();

                    if (AamConstants.OS_TYPE.WINDOWS != module.getOsType()) {
                        // cd vào thu muc cai dat
                        i++;
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + i + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("forward.to.folder.install"));
                        rowData.put("4", "cd " + module.getPath() + ";");
                        rowData.put("5", MessageUtil.getResourceBundleMessage("moved.to.app.directory.success.have.prompt.when.command.end"));
                        rowData.put("6", "1");

                        rowData.put("8", "");
                        rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                        rowData.put("10", "");
                        data.put(i + "", rowData);
                        totalTime += 1;
                        // dung module
                    }

                    // cd vào thu muc cai dat 2.2.7.
                    i++;
                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2." + currentIndex + "." + i + "");
                    rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                    rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("create.folders") + " backup_toantrinh");

                    String cmd = AamConstants.OS_TYPE.WINDOWS == module.getOsType() ? "cmd /C mkdir \"" + module.getPath() + "\\backup_toantrinh\\" + FilenameUtils.getPathNoEndSeparator(upcodePath).replaceAll("\\.\\.\\\\", "") + "\""
                            : "mkdir -p backup_toantrinh/" + FilenameUtils.getPathNoEndSeparator(upcodePath).replaceAll("\\.\\.", "");
                    rowData.put("4", cmd);

                    rowData.put("6", MessageUtil.getResourceBundleMessage("have.prompt.in.start.line.when.end.command"));
                    rowData.put("7", "1");
                    rowData.put("8", "");
                    rowData.put("9", "");
                    rowData.put("10", "");
                    data.put(i + "", rowData);
                    totalTime += 1;

                    i++;
                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2." + currentIndex + "." + i + "");
                    rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                    rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("backup.code.configuration"));
                    if (AamConstants.OS_TYPE.WINDOWS == module.getOsType()) {
                        String copyCmd = "cmd /C xcopy /E /Y /I";
                        if (actionDetailApp.getFile() != null && actionDetailApp.getFile()) {
                            copyCmd = "cmd /C echo F | xcopy /Y";
                        }
                        cmd = copyCmd + " \"" + module.getPath() + "\\" + upcodePath + "\" \"" + module.getPath() + "\\backup_toantrinh\\" + upcodePath.replaceAll("\\.\\.\\\\", "") + "_bk" + dateTime + "_" + crNumber + "\"";
                    } else {
                        cmd = "cp -r " + upcodePath + " backup_toantrinh/" + upcodePath.replaceAll("\\.\\./", "") + "_bk" + dateTime + "_" + crNumber;
                    }
                    rowData.put("4", cmd);
                    rowData.put("5", MessageUtil.getResourceBundleMessage("backup.folder.code.config.successful.have.prompt.in.start.line.when.end.command"));
                    rowData.put("6", "2");

                    rowData.put("8", "");
                    rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                            + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                            + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                            + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                            + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                    rowData.put("10", "");

                    data.put(i + "", rowData);
                    totalTime += 2;

                    //Quytv720190917_bo sung them lenh diff -rq start
                    if (AamConstants.OS_TYPE.WINDOWS != module.getOsType()) {
                        i++;
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + i + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("compare.code"));
                        cmd = "diff -rq " + upcodePath + " backup_toantrinh/" + upcodePath.replaceAll("\\.\\./", "") + "_bk" + dateTime + "_" + crNumber;
                        rowData.put("4", cmd);
                        rowData.put("5", MessageUtil.getResourceBundleMessage("compare.code.result"));
                        rowData.put("6", "2");

                        rowData.put("8", "");
                        rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                        rowData.put("10", "");

                        data.put(i + "", rowData);
                        totalTime += 1;
                    }
                    //Quytv720190917_bo sung them lenh diff -rq end


                    if (AamConstants.OS_TYPE.WINDOWS != module.getOsType()) {
                        i++;
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + i + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("check.the.existence.of") + " " + (actionDetailApp.getFile() != null && actionDetailApp.getFile() ? "file" : MessageUtil.getResourceBundleMessage("folder")) + " backup");
//					rowData.put("4", "du -b " + upcodePath + " backup_toantrinh/" + upcodePath + "_bk" + dateTime + "_" + crNumber);
                        rowData.put("4", (actionDetailApp.getFile() != null && actionDetailApp.getFile() ? "ls -l " : "cd ") + "backup_toantrinh/" + upcodePath.replaceAll("\\.\\./", "") + "_bk" + dateTime + "_" + crNumber);
//					rowData.put("5", ". Kiểm tra lại file cấu hình hiện tại và file backup có dung lượng bằng nhau, ví dụ:" + "\r\n" + "$ du -b etc backup_toantrinh/etc_bk13062016" + "\r\n" + "132    etc" + "\r\n" + "132    backup_toantrinh/etc_bk13062016");
                        rowData.put("5", MessageUtil.getResourceBundleMessage("have.prompt.in.start.line.when.end.command"));
                        rowData.put("6", "1");

                        rowData.put("8", "");
                        rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                        rowData.put("10", "");
                        data.put(i + "", rowData);
                        totalTime += 1;
                    }
                }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return data;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getTableBackupDb(Long actionId, String mopRollBack) {
        LinkedHashMap<String, LinkedHashMap<String, String>> data = new LinkedHashMap<>();
        LinkedHashMap<String, String> rowData;
//		ActionModuleServiceImpl service = new ActionModuleServiceImpl();
        ActionDetailDatabaseServiceImpl serviceDetailDb = new ActionDetailDatabaseServiceImpl();
//		ApplicationDetailServiceImpl serviceApp = new ApplicationDetailServiceImpl();
        try {
            List<ActionDetailDatabase> listDetails = serviceDetailDb.findListDetailDb(actionId, kbGroup, true, false);

            Integer currentIndex = indexMap.get(Constant.SUB_STEP_BACKUP_DB);
            int i = 0;
            int index = 0;
            if (listDetails != null)

                for (ActionDetailDatabase actionDetailDb : listDetails) {
                    if (actionDetailDb.getType().equals(Constant.TD_DB_TYPE_STOP_START))
                        continue;

                    ServiceDatabase module = iimService.findServiceDbById(action.getImpactProcess().getNationCode(), actionDetailDb.getAppDbId());
//					Module app = serviceApp.getObjByAppId(module.getAppId().longValue());
//					if (app == null)
//						app = new Module();
                    String file = FilenameUtils.getName(actionDetailDb.getScriptBackup());
                    i++;
                    // du lieu cho 1 row
                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2." + currentIndex + "." + i + "");
                    rowData.put("1.1", actionDetailDb.getActionOrder() + "");
                    rowData.put("2", module.getIpVirtual() + "@" + module.getUsername() + "\r\n" + "URL : " + module.getUrl());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("backup.data.in.the.db"));
                    String action;
                    if (actionDetailDb.getTypeImport() == null)
                        rowData.put("4", MessageUtil.getResourceBundleMessage("need.not.execute"));
                    else {
                        if (actionDetailDb.getTypeImport() == 1L)
                            if (file != null)
                                action = MessageUtil.getResourceBundleMessage("execute.commands.sql.in.the.file") + " " + file;
                            else
                                action = MessageUtil.getResourceBundleMessage("need.not.execute");
                        else {
                            action = "-- " + MessageUtil.getResourceBundleMessage("execute.commands.follow.script") + ": " + "\r\n" + actionDetailDb.getBackupText();
                        }
                        rowData.put("4", action);
                    }
//					String des = "";
                    // if(des==null)

                    rowData.put("5", "1. " + MessageUtil.getResourceBundleMessage("run.script.successful.have.not.error"));
                    rowData.put("6", "5");
                    rowData.put("8", "");
                    rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                            + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                            + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                            + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                            + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                    rowData.put("10", "");
                    data.put(module.getDbCode() + index++, rowData);
                    totalTime += 5;
                }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return data;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getTableRollBackDb(Long actionId) {
        LinkedHashMap<String, LinkedHashMap<String, String>> data = new LinkedHashMap<>();
        LinkedHashMap<String, String> rowData = null;
        ActionModuleServiceImpl service = new ActionModuleServiceImpl();
        ActionDetailDatabaseServiceImpl serviceDetailDb = new ActionDetailDatabaseServiceImpl();
        try {
            List<Long> ids = service.findListModuleId(actionId, null, false);
            List<Integer> listIds = new ArrayList<>();
            if (ids != null)
                for (Long id : ids) {
                    listIds.add(id.intValue());
                }

            List<ActionDetailDatabase> listDetails = serviceDetailDb.findListDetailDb(actionId, kbGroup, false, false);

            int i = 0;
            int index = 0;
            if (listDetails != null) {

                for (ActionDetailDatabase actionDetailDb : listDetails) {
                    if (actionDetailDb.getType().equals(Constant.TD_DB_TYPE_STOP_START))
                        continue;

                    ServiceDatabase module = iimService.findServiceDbById(action.getImpactProcess().getNationCode(), actionDetailDb.getAppDbId());
                    String file = FilenameUtils.getName(actionDetailDb.getRollbackFile());
                    i++;
                    // du lieu cho 1 row 2.2.3.

                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2.3." + i + "");
                    rowData.put("2", module.getIpVirtual() + "@" + module.getUsername() + "\r\n" + "URL : " + module.getUrl());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("run.script.rollback.the.db"));
                    String action;
                    if (actionDetailDb.getTypeImport() == null)
                        rowData.put("4", MessageUtil.getResourceBundleMessage("need.not.execute"));
                    else {
                        if (actionDetailDb.getTypeImport() == 1L)
                            if (file != null)
                                action = MessageUtil.getResourceBundleMessage("execute.commands.sql.in.the.file") + " " + file;
                            else
                                action = MessageUtil.getResourceBundleMessage("need.not.execute");
                        else {
                            action = "-- " + MessageUtil.getResourceBundleMessage("execute.commands.follow.script") + ": " + "\r\n" + actionDetailDb.getRollbackText();
                        }
                        rowData.put("4", action);
                    }
//					String des = "";
                    // if(des==null)
                    // des = "Backup bảng";

                    rowData.put("5", MessageUtil.getResourceBundleMessage("run.script.successful.have.not.error"));
                    rowData.put("6", "5");
                    rowData.put("8", "");
                    rowData.put("9", "");
                    rowData.put("10", "");
                    data.put(module.getDbCode() + index++, rowData);
                    totalTimeRollback += 5;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return data;
    }

    // check list DB

    public LinkedHashMap<String, LinkedHashMap<String, String>> getTableConfigDb(Long actionId, String mopRollBack) {
        LinkedHashMap<String, LinkedHashMap<String, String>> data = new LinkedHashMap<>();
        LinkedHashMap<String, String> rowData;
//		ActionModuleServiceImpl service = new ActionModuleServiceImpl();
        ActionDetailDatabaseServiceImpl serviceDetailDb = new ActionDetailDatabaseServiceImpl();
        try {

			/*List<Long> ids = service.findListModuleId(actionId);
            List<Integer> listIds = new ArrayList<>();
			if (ids != null)
				for (Long id : ids) {
					listIds.add(id.intValue());
				}*/

            List<ActionDetailDatabase> listDetails = serviceDetailDb.findListDetailDb(actionId, kbGroup, true, false);

            Integer currentIndex = indexMap.get(Constant.SUB_STEP_TD_DB);
            int i = 0;
            int index = 0;
            if (listDetails != null) {

                for (ActionDetailDatabase actionDetailDb : listDetails) {
                    if (actionDetailDb.getType().equals(Constant.TD_DB_TYPE_STOP_START))
                        continue;

                    ServiceDatabase module = iimService.findServiceDbById(action.getImpactProcess().getNationCode(), actionDetailDb.getAppDbId());
                    String file = FilenameUtils.getName(actionDetailDb.getScriptExecute());
                    i++;
                    // du lieu cho 1 row

                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2." + currentIndex + "." + i + "");
                    rowData.put("1.1", actionDetailDb.getActionOrder() + "");
                    rowData.put("2", module.getIpVirtual() + "@" + module.getUsername() + "\r\n" + "URL : " + module.getUrl());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("run.script.configuration.the.db"));
                    String action;
                    if (actionDetailDb.getTypeImport() == null)
                        rowData.put("4", MessageUtil.getResourceBundleMessage("need.not.execute"));
                    else {
                        if (actionDetailDb.getTypeImport() == 1L)
                            if (file != null)
                                action = MessageUtil.getResourceBundleMessage("execute.commands.sql.in.the.file") + " " + file;
                            else
                                action = MessageUtil.getResourceBundleMessage("need.not.execute");
                        else {
                            action = "-- " + MessageUtil.getResourceBundleMessage("execute.commands.follow.script") + ": " + "\r\n" + actionDetailDb.getScriptText();
                        }
                        rowData.put("4", action);
                    }
                    rowData.put("6", MessageUtil.getResourceBundleMessage("run.script.successful.have.not.error"));
                    rowData.put("7", "5");
                    rowData.put("8", "");
                    rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                            + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                            + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                            + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                            + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                            + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                    rowData.put("10", "");
                    data.put(module.getDbCode() + index++, rowData);
                    totalTime += 5;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return data;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getTableUpCode(Long actionId, String cr_number, String mopRollBack, String cr) {
        LinkedHashMap<String, LinkedHashMap<String, String>> data = new LinkedHashMap<>();
        LinkedHashMap<String, String> rowData = null;
        ActionModuleService serviceApp = new ActionModuleServiceImpl();
        ActionDetailAppServiceImpl serviceDetailApp = new ActionDetailAppServiceImpl();

        try {
            Map<Long, ActionModule> mapApp = serviceApp.getMapObjByListAppId(actionId);
            List<ActionDetailApp> listDetails = serviceDetailApp.findListDetailApp(actionId, "2", moduleIds, false);

            Integer currentIndex = indexMap.get(Constant.SUB_STEP_UPCODE);
            int i = 0;
            if (listDetails != null)

                for (ActionDetailApp actionDetailApp : listDetails) {
                    Long appId = actionDetailApp.getModuleId();
                    ActionModule module = mapApp.get(appId);
                    String file = FilenameUtils.getName(actionDetailApp.getUploadFilePath());

                    if (AamConstants.OS_TYPE.WINDOWS != module.getOsType()) {

                        // cd vào thu muc cai dat 3.2.5.
                        i++;
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + i + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("move.to.application.directory"));
                        rowData.put("4", "cd " + module.getPath());

                        rowData.put("6", MessageUtil.getResourceBundleMessage("moved.to.app.directory.success.have.prompt.when.command.end"));
                        rowData.put("7", "1");
                        rowData.put("8", "");
                        rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                                + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                        rowData.put("10", "");
                        data.put(i + "", rowData);
                        totalTime += 1;


                        i++;
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + i + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("switch.to.sftp.mode.press.alt.p.key.combination.then.go.to.installation.directory"));
                        rowData.put("4", "cd " + module.getPath());

                        rowData.put("6", MessageUtil.getResourceBundleMessage("moved.to.app.directory.success.have.prompt.when.command.end"));
                        rowData.put("7", "1");
                        rowData.put("8", "");
                        rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                                + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                        rowData.put("10", "");
                        data.put(i + "", rowData);
                        totalTime += 1;

                        i++;
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + i + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("switch.to.sftp.mode.press.alt.p.key.combination.and.then.transfer.to.the.original.preparation.directory") + ".\n" +
                                MessageUtil.getResourceBundleMessage("attention") + ": ~ " + MessageUtil.getResourceBundleMessage("is.the.relative.path.to.the.local") + "\n");
                        rowData.put("4", "lcd " + "\"~\\" + cr_number + "\\" + action.getCreatedBy() + "_" + action.getTdCode() + "\\source_code" + "\"");

                        rowData.put("6", MessageUtil.getResourceBundleMessage("move.to.folder.code.in.local.successful.have.prompt.in.start.line.when.end.command"));
                        rowData.put("7", "1");
                        rowData.put("8", "");
                        rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                                + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                        rowData.put("10", "");
                        data.put(i + "", rowData);
                        totalTime += 1;


                        i++;
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + i + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("upload.code.config.with.sftp.session.on.tool.secure.crt"));

                        if (!Util.isNullOrEmpty(module.getInstalledUser()) && module.getInstalledUser().equalsIgnoreCase("root")) {
                            rowData.put("4", "put " + file + "\n" +
                                    "cd backup_toantrinh_upload_file" + "\n" +
                                    "mv 'backup_toantrinh_upload_file' '" + module.getPath() + "'" + "\n");
                        } else {
                            rowData.put("4", "put " + file);
                        }

                        rowData.put("6", MessageUtil.getResourceBundleMessage("push.file") + " " + file
                                + " " + MessageUtil.getResourceBundleMessage("to.the.application.directory.successful.the.files.pushed.up.to.100.percent.example.below") + "\r\n"
                                + "sftp> put thread_node2.zip Uploading agent.cfg to /u02/app/interchg/interchange_node1/process/module/InterChangeProcess.jar "
                                + "\r\n" + "100% 137KB    137KB/s 00:00:00");
                        rowData.put("7", "1");
                        rowData.put("8", "");
                        rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                                + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                        rowData.put("10", "");
                        data.put(i + "", rowData);
                        totalTime += 1;
                    }

                    if (AamConstants.OS_TYPE.WINDOWS == module.getOsType()) {
                        i++;
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + i + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("upload.code.config.with.sftp.session.on.tool.secure.crt"));
                        rowData.put("4", "copy file " + file + " " + MessageUtil.getResourceBundleMessage("copy.file.intofolder") + " " + module.getPath());

                        rowData.put("6", "");
                        rowData.put("7", "1");
                        rowData.put("8", "");
                        rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                                + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                        rowData.put("10", "");
                        data.put(i + "", rowData);
                        totalTime += 1;
                    }

                    if (StringUtils.isNotEmpty(actionDetailApp.getLstFileRemove())) {
                        i++;
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + i + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("delete.file.code"));
                        String cmd;
                        if (AamConstants.OS_TYPE.WINDOWS == module.getOsType()) {
                            List<String> removes = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(actionDetailApp.getLstFileRemove().replaceAll("'", ""));
                            List<String> deletes = new ArrayList<>();
                            for (String remove : removes) {
                                deletes.add("\"" + module.getPath() + "\\" + remove + "\"");
                            }

                            cmd = "cmd /C del /F /Q /S " + Joiner.on(" ").join(deletes);
                        } else {
                            cmd = "rm -rf " + actionDetailApp.getLstFileRemove().replaceAll(",", "");
                        }
                        rowData.put("4", cmd);

                        rowData.put("6", MessageUtil.getResourceBundleMessage("have.prompt.in.start.line.when.end.command"));
                        rowData.put("7", "1");
                        rowData.put("8", "");
                        rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                                + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                        rowData.put("10", "");
                        data.put(i + "", rowData);
                        totalTime += 1;
                    }

//					if (actionDetailApp.getFile() == null || !actionDetailApp.getFile()) {
                    i++;
                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2." + currentIndex + "." + i + "");
                    rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                    rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("switch.to.ssh.session.mode") + " " + MessageUtil.getResourceBundleMessage("override.code.config.over.current"));

                    String cmd;
                    if (AamConstants.OS_TYPE.WINDOWS == module.getOsType()) {
                        String upcodeFullPath = module.getPath() + "\\" + actionDetailApp.getUpcodePath();
                        if (actionDetailApp.getFile() != null && actionDetailApp.getFile()) {
                            upcodeFullPath = upcodeFullPath.replaceAll("\\\\[^\\\\]*$", "");
                        } else {
                            upcodeFullPath = upcodeFullPath.replaceAll("\\\\$", "").replaceAll("\\\\[^\\\\]*$", "");
                        }

                        cmd = "\"C:\\Program Files\\WinRAR\\winrar\" x -o \"" + module.getPath() + "\\" + actionDetailApp.getUploadFilePath() + "\" *.* \"" + upcodeFullPath + "\";\r\n";
                        cmd += "\"C:\\Program Files (x86)\\WinRAR\\winrar\" x -o \"" + module.getPath() + "\\" + actionDetailApp.getUploadFilePath() + "\" *.* \"" + upcodeFullPath + "\";\r\n";
                        cmd += "cmd /C move /Y \"" + module.getPath() + "\\" + actionDetailApp.getUploadFilePath() + "\" \"" + module.getPath() + "\\" + "backup_toantrinh\\\\" + FilenameUtils.getPath(actionDetailApp.getUploadFilePath()).replaceAll("\\.\\.\\\\", "") + file.substring(0, file.length() - 4)
                                + "_" + cr + "_" + new SimpleDateFormat("ddMMyyyHHmmss").format(new Date()) + "." + FilenameUtils.getExtension(file) + "\"";
                    } else {
                        cmd = "unzip -o '" + file + "'" + (StringUtils.isNotEmpty(FilenameUtils.getPathNoEndSeparator(actionDetailApp.getUpcodePath())) ? " -d " + FilenameUtils.getPathNoEndSeparator(actionDetailApp.getUpcodePath()) : "") + ";" + "\r\n" + "mv '" + file + "' 'backup_toantrinh/" + FilenameUtils.getPath(actionDetailApp.getUpcodePath().replaceAll("\\.\\./", "")) + file.substring(0, file.length() - 4)
                                + "_" + cr + "_" + new SimpleDateFormat("ddMMyyyHHmmss").format(new Date()) + "." + FilenameUtils.getExtension(file) + "';";
                    }
                    if (file != null && file.length() > 4)
                        rowData.put("4", cmd);
                    else
                        rowData.put("4", "");

                    rowData.put("6", "1." + MessageUtil.getResourceBundleMessage("execute.extract.overwrites.new.code.files.in.the.current.code.directory") + "\r\n"
                            + MessageUtil.getResourceBundleMessage("have.prompt.in.start.line.when.end.command") + "\r\n"
                            + (AamConstants.OS_TYPE.WINDOWS == module.getOsType() ? MessageUtil.getResourceBundleMessage("windows.winrar.unzip") : MessageUtil.getResourceBundleMessage("example") + ":" + "\r\n"
                            + "$ unzip -o 'thread_node1.zip'" + "\r\n" + "Archive:  thread_node1.zip" + "\r\n"
                            + "inflating: etc/app.conf" + "\r\n" + "2. " + MessageUtil.getResourceBundleMessage("have.prompt.in.start.line.when.end.command")));
                    rowData.put("7", "1");
                    rowData.put("8", "");
                    rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                            + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                            + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                            + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                            + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                            + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                            + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                    rowData.put("10", "");
                    data.put(i + "", rowData);
                    totalTime += 1;
//					}
                    /*if (actionDetailApp.getCheckCmd() != null && !actionDetailApp.getCheckCmd().isEmpty()) {
                        i++;
						rowData = new LinkedHashMap<>();
						rowData.put("1", "2.2." + currentIndex + "." + i + "");
						rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
						rowData.put("3", "Chạy lệnh so sánh");
						rowData.put("4", actionDetailApp.getCheckCmd());

						rowData.put("6", actionDetailApp.getCheckCmdResult());
						rowData.put("7", "1");
						rowData.put("8", "");
						rowData.put("9", "Thực hiện rollback tuần tự các bước trong file \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
								+ "B1: Thực hiện bước 2.2.3 => Rollback Database" + "\r\n" + "B2: Thực hiện bước 2.2.2 => Rollback Code/config" + "\r\n"

								+ "B3: Thực hiện  bước 2.2.5.2 => bật lại module" + "\r\n" + "B4: Thực hiện bước 2.3 => kiểm tra ứng dụng sau start" + "\r\n"
								+ "B5: Thực hiện bước 2.4 => thông báo các đơn vị kiểm tra dịch vụ test rollback" + "\r\n"
								+ "B6: Thực hiện bước 2.5=> Thực hiện test sau rollback");
						rowData.put("10", "");
						data.put(i + "", rowData);
						totalTime += 1;
					}*/

                }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return data;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getTableUpCodeStopStart(Long actionId, String cr_number, String mopRollBack, String crNumber, String date) {
        LinkedHashMap<String, LinkedHashMap<String, String>> data = new LinkedHashMap<>();
        LinkedHashMap<String, String> rowData = null;
        ActionDetailAppServiceImpl serviceDetailApp = new ActionDetailAppServiceImpl();
        ActionModuleService serviceApp = new ActionModuleServiceImpl();

        try {
            Map<Long, ActionModule> mapApp = serviceApp.getMapObjByListAppId(actionId);
            List<ActionDetailApp> listDetails = serviceDetailApp.findListDetailApp(actionId, Constant.STEP_UPCODE_STOP_START, moduleIds, false);

            String key;
            String link;
            String des = "";

            Integer currentIndex = indexMap.get(Constant.SUB_STEP_UPCODE);
            int i = 0;
            if (listDetails != null)

                for (ActionDetailApp actionDetailApp : listDetails) {
                    Long appId = actionDetailApp.getModuleId();
                    ActionModule module = mapApp.get(appId);
                    String file = FilenameUtils.getName(actionDetailApp.getUploadFilePath());

                    key = module.getKeyword();
                    link = module.getLogLink();

                    boolean codetaptrung = false;
                    if (module.getAppTypeCode() != null && module.getAppTypeCode().equals(Constant.CODETAPTRUNG_TYPE)) {
                        codetaptrung = true;
                    }

                    if (AamConstants.OS_TYPE.WINDOWS != module.getOsType()) {
                        // cd vào thu muc cai dat 2.2.5.
                        i++;
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + i + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("move.to.application.directory"));
                        rowData.put("4", "cd " + module.getPath());

                        rowData.put("6", MessageUtil.getResourceBundleMessage("moved.to.app.directory.success.have.prompt.when.command.end"));
                        rowData.put("7", "1");
                        rowData.put("8", "");
                        rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                                + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                        rowData.put("10", "");
                        data.put(i + "", rowData);
                        totalTime += 1;
                    }

                    i++;
                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2." + currentIndex + "." + i + "");
                    rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                    rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("switch.to.sftp.mode.press.alt.p.key.combination.then.go.to.installation.directory"));
                    rowData.put("4", "cd " + module.getPath());

                    rowData.put("6", MessageUtil.getResourceBundleMessage("moved.to.app.directory.success.have.prompt.when.command.end"));
                    rowData.put("7", "1");
                    rowData.put("8", "");
                    rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                            + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                            + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                            + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                            + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                            + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                            + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                    rowData.put("10", "");
                    data.put(i + "", rowData);
                    totalTime += 1;

                    i++;
                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2." + currentIndex + "." + i + "");
                    rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                    rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("switch.to.sftp.mode.press.alt.p.key.combination.and.then.transfer.to.the.original.preparation.directory") + ".\n" +
                            MessageUtil.getResourceBundleMessage("attention") + ": ~ " + MessageUtil.getResourceBundleMessage("is.the.relative.path.to.the.local") + "\n");
                    rowData.put("4", "lcd " + "\"~\\" + cr_number + "\\" + action.getCreatedBy() + "_" + action.getTdCode() + "\\source_code" + "\"");

                    rowData.put("6", MessageUtil.getResourceBundleMessage("move.to.folder.code.in.local.successful.have.prompt.in.start.line.when.end.command"));
                    rowData.put("7", "1");
                    rowData.put("8", "");
                    rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                            + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                            + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                            + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                            + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                            + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                            + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                    rowData.put("10", "");
                    data.put(i + "", rowData);
                    totalTime += 1;

                    i++;
                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2." + currentIndex + "." + i + "");
                    rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                    rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("upload.code.config.with.sftp.session.on.tool.secure.crt"));
                    if (!Util.isNullOrEmpty(module.getInstalledUser()) && module.getInstalledUser().equalsIgnoreCase("root")) {
                        rowData.put("4", "put " + file + "\n" +
                                "cd backup_toantrinh_upload_file" + "\n" +
                                "mv 'backup_toantrinh_upload_file' '" + module.getPath() + "'" + "\n");
                    } else {
                        rowData.put("4", "put " + file);
                    }

                    rowData.put("6", MessageUtil.getResourceBundleMessage("push.file") + " " + file + " "
                            + MessageUtil.getResourceBundleMessage("to.the.application.directory.successful.the.files.pushed.up.to.100.percent.example.below") + "\r\n"
                            + "sftp> put thread_node2.zip Uploading agent.cfg to /u02/app/interchg/interchange_node1/process/module/InterChangeProcess.jar "
                            + "\r\n" + "100% 137KB    137KB/s 00:00:00");
                    rowData.put("7", "1");
                    rowData.put("8", "");
                    rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                            + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                            + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                            + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                            + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                            + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                            + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                    rowData.put("10", "");
                    data.put(i + "", rowData);
                    totalTime += 1;

                    if (StringUtils.isNotEmpty(actionDetailApp.getLstFileRemove())) {
                        i++;
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + i + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("delete.file.code"));
                        rowData.put("4", "rm -rf " + actionDetailApp.getLstFileRemove().replaceAll(",", ""));

                        rowData.put("6", MessageUtil.getResourceBundleMessage("have.prompt.in.start.line.when.end.command"));
                        rowData.put("7", "1");
                        rowData.put("8", "");
                        rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                                + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                        rowData.put("10", "");
                        data.put(i + "", rowData);
                        totalTime += 1;
                    }

                    i++;
                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2." + currentIndex + "." + i + "");
                    rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                    rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("switch.to.ssh.session.mode") + " " + MessageUtil.getResourceBundleMessage("override.code.config.over.current"));
                    if (file != null && file.length() > 4)
                        rowData.put("4", "unzip -o '" + file + "'" + (StringUtils.isNotEmpty(FilenameUtils.getPathNoEndSeparator(actionDetailApp.getUpcodePath())) ? " -d " + FilenameUtils.getPathNoEndSeparator(actionDetailApp.getUpcodePath()) : "") + ";" + "\r\n" + "mv '" + file + "' 'backup_toantrinh/" + FilenameUtils.getPath(actionDetailApp.getUpcodePath().replaceAll("\\.\\./", "")) + file.substring(0, file.length() - 4)
                                + "_" + crNumber + "_" + new SimpleDateFormat("ddMMyyyHHmmss").format(new Date()) + "." + FilenameUtils.getExtension(file) + "';");
                    else
                        rowData.put("4", "");

                    rowData.put("6", "1." + MessageUtil.getResourceBundleMessage("execute.extract.overwrites.new.code.files.in.the.current.code.directory") + "\r\n"
                            + MessageUtil.getResourceBundleMessage("have.prompt.in.start.line.when.end.command") + "\r\n"
                            + MessageUtil.getResourceBundleMessage("example") + ":" + "\r\n"
                            + "$ unzip -o 'thread_node1.zip'" + "\r\n" + "Archive:  thread_node1.zip" + "\r\n"
                            + "inflating: etc/app.conf" + "\r\n" + "2. " + MessageUtil.getResourceBundleMessage("have.prompt.in.start.line.when.end.command"));
                    rowData.put("7", "1");
                    rowData.put("8", "");
                    rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                            + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                            + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                            + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                            + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                            + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                            + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                    rowData.put("10", "");
                    data.put(i + "", rowData);
                    totalTime += 1;
                    if (actionDetailApp.getCheckCmd() != null && !actionDetailApp.getCheckCmd().isEmpty()) {
                        i++;
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + i + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("run.the.compare.command"));
                        rowData.put("4", actionDetailApp.getCheckCmd());

                        rowData.put("6", actionDetailApp.getCheckCmdResult());
                        rowData.put("7", "1");
                        rowData.put("8", "");
                        rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                                + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                        rowData.put("10", "");
                        data.put(i + "", rowData);
                        totalTime += 1;
                    }

                    i++;
                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2." + currentIndex + "." + i + "");
                    rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                    rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("stop.module"));

                    String stopCmd;
                    String expect;
                    if (StringUtils.isNotEmpty(module.getViewStatus()) && StringUtils.isNotEmpty(module.getKeyStatusStart()) && !module.getViewStatus().toUpperCase().equals(Constant.NA_VALUE)) {
                        stopCmd = module.getStopService();
                        rowData.put("4", stopCmd);
                        expect = "1. " + MessageUtil.getResourceBundleMessage("stop.app.success.have.command.prompt.when.command.end") + "\n"
                                + "2. " + MessageUtil.getResourceBundleMessage("if.after.desired.time.execute.command.without.command.prompt.press.ctrl.c");
                    } else {
                        stopCmd = codetaptrung ? module.getStopService() : module.getStopService() + ";" + "\r\n" + "\r\n" + "ps -ef | grep \"" + LinuxParser.getFullPath(module.getPath()) + "\" | grep -v grep; " + "\r\n" + "kill -9 PID;";
                        rowData.put("4", stopCmd);
                        expect = "1. " + MessageUtil.getResourceBundleMessage("stop.app.success.have.command.prompt.when.command.end") + "\n"
                                + "2. " + MessageUtil.getResourceBundleMessage("if.after.desired.time.execute.command.without.command.prompt.press.ctrl.c");
                        if (!codetaptrung) {
                            expect += ", " + MessageUtil.getResourceBundleMessage("execute.command.get.PID.of.process") + "\r\n"
                                    + MessageUtil.getResourceBundleMessage("example.get.PID.red.shown.below") + " (PID = 20894)"
                                    + "\r\n"
                                    + "$ ps -ef | grep /u01/BCCS_CODINH/CM_APP/jdk1.6.0_26/ | grep -v grep;"
                                    + "\r\n "
                                    + "5503      20894     1  2 Apr14 ?        03:12:52 "
                                    + "\r\n"
                                    + "/u01/BCCS_CODINH/CM_APP/jdk1.6.0_26/bin/java -"
                                    + "\r\n"
                                    + "3. " + MessageUtil.getResourceBundleMessage("pid.process.have.still.stop.process.using.command.kill.9.pid");

                        }
                    }
                    rowData.put("5", expect);
                    rowData.put("6", "3");
                    rowData.put("8", "");
                    rowData.put("9", des);
                    rowData.put("10", "");
                    data.put(i + "", rowData);
                    totalTime += 3;

                    if (!codetaptrung) {
                        if (StringUtils.isNotEmpty(module.getViewStatus()) && StringUtils.isNotEmpty(module.getKeyStatusStart()) && !module.getViewStatus().toUpperCase().equals(Constant.NA_VALUE)) {
                            i++;
                            rowData = new LinkedHashMap<>();
                            rowData.put("1", "2.2." + currentIndex + "." + i + "");
                            rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                            rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                            rowData.put("3", MessageUtil.getResourceBundleMessage("check.module.has.stopped.altogether"));
                            rowData.put("4", module.getViewStatus());
                            rowData.put("5", MessageUtil.getResourceBundleMessage("have.not.keyword") + " \"" + module.getKeyStatusStart()
                                    + "\" " + MessageUtil.getResourceBundleMessage("appears.in.begin.line.has.command.prompt.after.end.command.process.has.been.stopped"));
                            rowData.put("6", "1");

                            rowData.put("8", "");
                            rowData.put("9", des);
                            rowData.put("10", "");
                            data.put(i + "", rowData);
                            totalTime += 1;
                        } else {
                            i++;
                            rowData = new LinkedHashMap<>();
                            rowData.put("1", "2.2." + currentIndex + "." + i + "");
                            rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                            rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                            rowData.put("3", MessageUtil.getResourceBundleMessage("check.module.has.stopped.altogether"));
                            rowData.put("4", "ps -ef | grep \"" + LinuxParser.getFullPath(module.getPath()) + "\" | grep -v grep");
                            rowData.put("5", MessageUtil.getResourceBundleMessage("no.process.line.app.appears.there.is.command.prompt.after.end.command.process.has.been.stopped"));
                            rowData.put("6", "1");

                            rowData.put("8", "");
                            rowData.put("9", des);
                            rowData.put("10", "");
                            data.put(i + "", rowData);
                            totalTime += 1;
                        }
                    }

                    if (StringUtils.isNotEmpty(link) && !link.trim().equals(Constant.NA_VALUE)) {

                        i++;
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + i + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("backup.file.log.start.current"));
                        rowData.put("4", "cp " + link + " " + link + "bk_" + crNumber + "_" + date + ";\n"
                                + "zip -m " + link + "bk_" + crNumber + "_" + date + ".zip " + link + "bk_" + crNumber + "_" + date);

                        rowData.put("6", "1. " + MessageUtil.getResourceBundleMessage("backup.file.log.successful.have.prompt.in.start.line.when.end.command") + ".\n" +
                                "2. " + MessageUtil.getResourceBundleMessage("zip.and.delete.file.log.after.backup.successful.have.prompt.in.start.line.when.end.command") + ".\n");
                        rowData.put("7", "2");
                        rowData.put("8", "");
                        rowData.put("9", des);
                        rowData.put("10", "");
                        data.put(i + "", rowData);
                        totalTime += 2;
                        i++;
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + i + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("before.enable.module.has.made.empty.log.file"));
                        rowData.put("4", "> " + link);

                        rowData.put("6", MessageUtil.getResourceBundleMessage("has.made.empty.log.file.successful.have.prompt.in.start.line.when.end.command") + ".");
                        rowData.put("7", "1");
                        rowData.put("8", "");
                        rowData.put("9", des);
                        rowData.put("10", "");
                        data.put(i + "", rowData);
                        totalTime += 1;
                    }
                    i++;
                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2." + currentIndex + "." + i + "");
                    rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                    rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("execute.start.module"));
                    String startCmd = module.getStartService();
                    String startComment = "1. " + MessageUtil.getResourceBundleMessage("app.started.successful.have.prompt.in.start.line.when.end.command") + ".\n";
                    if (!codetaptrung) {
                        if (StringUtils.isNotEmpty(link) && StringUtils.isNotEmpty(key) && !link.trim().equals(Constant.NA_VALUE) && !key.trim().equals(Constant.NA_VALUE)) {
                            startCmd += ";\n" + "tail -500f " + link + " | grep --color " + "\"" + key + "\";\n"
                                    + "cat " + link + " | grep -a --color " + "\"" + key + "\"";
                            startComment += "2. " + MessageUtil.getResourceBundleMessage("find.keyword.in.log.command.response.execute.command.3.when.keyword.not.found") + "\n" +
                                    "3. " + MessageUtil.getResourceBundleMessage("find.keyword.in.log.command.response.execute.1.time.again.after.2.minute.when.keyword.not.found") + "\n";
                        } else if (StringUtils.isNotEmpty(module.getViewStatus()) && StringUtils.isNotEmpty(module.getKeyStatusStart()) && !module.getViewStatus().toUpperCase().equals(Constant.NA_VALUE)) {
                            startCmd += ";\n" + module.getViewStatus() + ";";
                            startComment += "2. " + MessageUtil.getResourceBundleMessage("have.keyword") + " \"" + module.getKeyStatusStart()
                                    + "\" " + MessageUtil.getResourceBundleMessage("in.the.result.returned") + "," + MessageUtil.getResourceBundleMessage("execute.1.times.again.after.2.minute.when.keyword.not.found") + "\n";
                        } else {
                            startCmd += ";\n" + "ps -ef | grep \"" + LinuxParser.getFullPath(module.getPath()) + "\" | grep -v grep;";
                            startComment += "2. " + MessageUtil.getResourceBundleMessage("have.pid.of.process.response.if.process.have.not.pid.wait.2.minute.and.then.execute.1.time.again") + "\n";
                        }
                    }
                    rowData.put("4", startCmd);
                    rowData.put("6", startComment);
                    rowData.put("7", "1");
                    rowData.put("8", "");
                    rowData.put("9", des);
                    rowData.put("10", "");
                    data.put(i + "", rowData);
                    totalTime += 1;
                }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return data;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getTableUpCodeStopStartRb(Long actionId, String cr_number, String mopRollBack, String crNumber, String date) {
        LinkedHashMap<String, LinkedHashMap<String, String>> data = new LinkedHashMap<>();
        LinkedHashMap<String, String> rowData = null;
        ActionModuleService serviceApp = new ActionModuleServiceImpl();
        ActionDetailAppServiceImpl serviceDetailApp = new ActionDetailAppServiceImpl();

        try {
            Map<Long, ActionModule> mapApp = serviceApp.getMapObjByListAppId(actionId);
            List<ActionDetailApp> listDetails = serviceDetailApp.findListDetailApp(actionId, Constant.STEP_UPCODE_STOP_START, moduleIds, false);

            String key;
            String link;
            String des = "";

            Integer currentIndex = indexMap.get(Constant.SUB_STEP_UPCODE);
            int i = 0;
            if (listDetails != null)

                for (ActionDetailApp actionDetailApp : listDetails) {
                    Long appId = actionDetailApp.getModuleId();
                    ActionModule module = mapApp.get(appId);
                    String file = FilenameUtils.getName(actionDetailApp.getUploadFilePath());

                    key = module.getKeyword();
                    link = module.getLogLink();

                    boolean codetaptrung = false;
                    if (module.getAppTypeCode() != null && module.getAppTypeCode().equals(Constant.CODETAPTRUNG_TYPE)) {
                        codetaptrung = true;
                    }

                    if (AamConstants.OS_TYPE.WINDOWS != module.getOsType()) {
                        // cd vào thu muc cai dat 3.2.5.
                        i++;
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + i + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("move.to.application.directory"));
                        rowData.put("4", "cd " + module.getPath());

                        rowData.put("6", MessageUtil.getResourceBundleMessage("moved.to.app.directory.success.have.prompt.when.command.end"));
                        rowData.put("7", "1");
                        rowData.put("8", "");
                        rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                                + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                        rowData.put("10", "");
                        data.put(i + "", rowData);
                        totalTime += 1;
                    }

                    i++;
                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2." + currentIndex + "." + i + "");
                    rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                    rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("switch.to.sftp.mode.press.alt.p.key.combination.then.go.to.installation.directory"));
                    rowData.put("4", "cd " + module.getPath());

                    rowData.put("6", MessageUtil.getResourceBundleMessage("moved.to.app.directory.success.have.prompt.when.command.end"));
                    rowData.put("7", "1");
                    rowData.put("8", "");
                    rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                            + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                            + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                            + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                            + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                            + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                            + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                    rowData.put("10", "");
                    data.put(i + "", rowData);
                    totalTime += 1;

                    i++;
                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2." + currentIndex + "." + i + "");
                    rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                    rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("switch.to.sftp.mode.press.alt.p.key.combination.and.then.transfer.to.the.original.preparation.directory") + ".\n" +
                            MessageUtil.getResourceBundleMessage("attention") + ": ~ " + MessageUtil.getResourceBundleMessage("is.the.relative.path.to.the.local") + "\n");
                    rowData.put("4", "lcd " + "\"~\\" + cr_number + "\\" + action.getCreatedBy() + "_" + action.getTdCode() + "\\source_code" + "\"");

                    rowData.put("6", MessageUtil.getResourceBundleMessage("moved.to.app.directory.success.have.prompt.when.command.end"));
                    rowData.put("7", "1");
                    rowData.put("8", "");
                    rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                            + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                            + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                            + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                            + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                            + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                            + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                    rowData.put("10", "");
                    data.put(i + "", rowData);
                    totalTime += 1;

                    i++;
                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2." + currentIndex + "." + i + "");
                    rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                    rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("upload.code.config.with.sftp.session.on.tool.secure.crt"));
                    if (!Util.isNullOrEmpty(module.getInstalledUser()) && module.getInstalledUser().equalsIgnoreCase("root")) {
                        rowData.put("4", "put " + file + "\n" +
                                "cd backup_toantrinh_upload_file" + "\n" +
                                "mv 'backup_toantrinh_upload_file' '" + module.getPath() + "'" + "\n");
                    } else {
                        rowData.put("4", "put " + file);
                    }

                    rowData.put("6", MessageUtil.getResourceBundleMessage("push.file") + " " + file
                            + " " + MessageUtil.getResourceBundleMessage("to.the.application.directory.successful.the.files.pushed.up.to.100.percent.example.below") + "\r\n"
                            + "sftp> put thread_node2.zip Uploading agent.cfg to /u02/app/interchg/interchange_node1/process/module/InterChangeProcess.jar "
                            + "\r\n" + "100% 137KB    137KB/s 00:00:00");
                    rowData.put("7", "1");
                    rowData.put("8", "");
                    rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                            + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                            + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                            + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                            + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                            + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                            + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                    rowData.put("10", "");
                    data.put(i + "", rowData);
                    totalTime += 1;

                    if (StringUtils.isNotEmpty(actionDetailApp.getLstFileRemove())) {
                        i++;
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + i + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("delete.file.code"));
                        rowData.put("4", "rm -rf " + actionDetailApp.getLstFileRemove().replaceAll(",", ""));

                        rowData.put("6", MessageUtil.getResourceBundleMessage("have.prompt.in.start.line.when.end.command"));
                        rowData.put("7", "1");
                        rowData.put("8", "");
                        rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                                + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                        rowData.put("10", "");
                        data.put(i + "", rowData);
                        totalTime += 1;
                    }

                    i++;
                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2." + currentIndex + "." + i + "");
                    rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                    rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("switch.to.ssh.session.mode") + " " + MessageUtil.getResourceBundleMessage("override.code.config.over.current"));
                    if (file != null && file.length() > 4)
                        rowData.put("4", "unzip -o '" + file + "'" + (StringUtils.isNotEmpty(FilenameUtils.getPathNoEndSeparator(actionDetailApp.getUpcodePath())) ? " -d " + FilenameUtils.getPathNoEndSeparator(actionDetailApp.getUpcodePath()) : "") + ";" + "\r\n" + "mv '" + file + "' 'backup_toantrinh/" + FilenameUtils.getPath(actionDetailApp.getUpcodePath().replaceAll("\\.\\./", "")) + file.substring(0, file.length() - 4)
                                + "_" + crNumber + "_" + new SimpleDateFormat("ddMMyyyHHmmss").format(new Date()) + "." + FilenameUtils.getExtension(file) + "';");
                    else
                        rowData.put("4", "");

                    rowData.put("6", "1." + MessageUtil.getResourceBundleMessage("execute.extract.overwrites.new.code.files.in.the.current.code.directory") + "\r\n"
                            + MessageUtil.getResourceBundleMessage("have.prompt.in.start.line.when.end.command") + "\r\n"
                            + MessageUtil.getResourceBundleMessage("example") + ":" + "\r\n"
                            + "$ unzip -o 'thread_node1.zip'" + "\r\n" + "Archive:  thread_node1.zip" + "\r\n"
                            + "inflating: etc/app.conf" + "\r\n" + "2. " + MessageUtil.getResourceBundleMessage("have.prompt.in.start.line.when.end.command"));
                    rowData.put("7", "1");
                    rowData.put("8", "");
                    rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                            + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                            + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                            + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                            + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                            + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                            + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                    rowData.put("10", "");
                    data.put(i + "", rowData);
                    totalTime += 1;
                    if (actionDetailApp.getCheckCmd() != null && !actionDetailApp.getCheckCmd().isEmpty()) {
                        i++;
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + i + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("run.the.compare.command"));
                        rowData.put("4", actionDetailApp.getCheckCmd());

                        rowData.put("6", actionDetailApp.getCheckCmdResult());
                        rowData.put("7", "1");
                        rowData.put("8", "");
                        rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                                + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                        rowData.put("10", "");
                        data.put(i + "", rowData);
                        totalTime += 1;
                    }

                    i++;
                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2." + currentIndex + "." + i + "");
                    rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                    rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("stop.module"));

                    String stopCmd;
                    String expect;
                    if (StringUtils.isNotEmpty(module.getViewStatus()) && StringUtils.isNotEmpty(module.getKeyStatusStart()) && !module.getViewStatus().toUpperCase().equals(Constant.NA_VALUE)) {
                        stopCmd = module.getStopService();
                        rowData.put("4", stopCmd);
                        expect = "1. " + MessageUtil.getResourceBundleMessage("stop.app.success.have.command.prompt.when.command.end") + "\n"
                                + "2. " + MessageUtil.getResourceBundleMessage("if.after.desired.time.execute.command.without.command.prompt.press.ctrl.c");
                    } else {
                        stopCmd = codetaptrung ? module.getStopService() : module.getStopService() + ";" + "\r\n" + "\r\n" + "ps -ef | grep \"" + LinuxParser.getFullPath(module.getPath()) + "\" | grep -v grep; " + "\r\n" + "kill -9 PID;";
                        rowData.put("4", stopCmd);
                        expect = "1. " + MessageUtil.getResourceBundleMessage("stop.app.success.have.command.prompt.when.command.end") + "\n"
                                + "2. " + MessageUtil.getResourceBundleMessage("if.after.desired.time.execute.command.without.command.prompt.press.ctrl.c");
                        if (!codetaptrung) {
                            expect += ", " + MessageUtil.getResourceBundleMessage("execute.command.get.PID.of.process") + "\r\n"
                                    + MessageUtil.getResourceBundleMessage("example.get.PID.red.shown.below") + " (PID = 20894)"
                                    + "\r\n"
                                    + "$ ps -ef | grep /u01/BCCS_CODINH/CM_APP/jdk1.6.0_26/ | grep -v grep;"
                                    + "\r\n "
                                    + "5503      20894     1  2 Apr14 ?        03:12:52 "
                                    + "\r\n"
                                    + "/u01/BCCS_CODINH/CM_APP/jdk1.6.0_26/bin/java -"
                                    + "\r\n"
                                    + "3. " + MessageUtil.getResourceBundleMessage("pid.process.have.still.stop.process.using.command.kill.9.pid");

                        }
                    }
                    rowData.put("5", expect);
                    rowData.put("6", "3");
                    rowData.put("8", "");
                    rowData.put("9", des);
                    rowData.put("10", "");
                    data.put(i + "", rowData);
                    totalTime += 3;

                    if (!codetaptrung) {
                        if (StringUtils.isNotEmpty(module.getViewStatus()) && StringUtils.isNotEmpty(module.getKeyStatusStart()) && !module.getViewStatus().toUpperCase().equals(Constant.NA_VALUE)) {
                            i++;
                            rowData = new LinkedHashMap<>();
                            rowData.put("1", "2.2." + currentIndex + "." + i + "");
                            rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                            rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                            rowData.put("3", MessageUtil.getResourceBundleMessage("check.module.has.stopped.altogether"));
                            rowData.put("4", module.getViewStatus());
                            rowData.put("5", MessageUtil.getResourceBundleMessage("have.not.keyword") + " \""
                                    + module.getKeyStatusStart() + "\" " + MessageUtil.getResourceBundleMessage("appears.in.begin.line.has.command.prompt.after.end.command.process.has.been.stopped"));
                            rowData.put("6", "1");

                            rowData.put("8", "");
                            rowData.put("9", des);
                            rowData.put("10", "");
                            data.put(i + "", rowData);
                            totalTime += 1;
                        } else {
                            i++;
                            rowData = new LinkedHashMap<>();
                            rowData.put("1", "2.2." + currentIndex + "." + i + "");
                            rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                            rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                            rowData.put("3", MessageUtil.getResourceBundleMessage("check.module.has.stopped.altogether"));
                            rowData.put("4", "ps -ef | grep \"" + LinuxParser.getFullPath(module.getPath()) + "\" | grep -v grep");
                            rowData.put("5", MessageUtil.getResourceBundleMessage("no.process.line.app.appears.there.is.command.prompt.after.end.command.process.has.been.stopped"));
                            rowData.put("6", "1");

                            rowData.put("8", "");
                            rowData.put("9", des);
                            rowData.put("10", "");
                            data.put(i + "", rowData);
                            totalTime += 1;
                        }
                    }

                    if (StringUtils.isNotEmpty(link) && !link.trim().equals(Constant.NA_VALUE)) {

                        i++;
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + i + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("backup.file.log.start.current"));
                        rowData.put("4", "cp " + link + " " + link + "bk_" + crNumber + "_" + date + ";\n"
                                + "zip -m " + link + "bk_" + crNumber + "_" + date + ".zip " + link + "bk_" + crNumber + "_" + date);

                        rowData.put("6", "1. " + MessageUtil.getResourceBundleMessage("backup.file.log.successful.have.prompt.in.start.line.when.end.command") + ".\n" +
                                "2. " + MessageUtil.getResourceBundleMessage("zip.and.delete.file.log.after.backup.successful.have.prompt.in.start.line.when.end.command") + ".\n");
                        rowData.put("7", "2");
                        rowData.put("8", "");
                        rowData.put("9", des);
                        rowData.put("10", "");
                        data.put(i + "", rowData);
                        totalTime += 2;
                        i++;
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + i + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("before.enable.module.has.made.empty.log.file"));
                        rowData.put("4", "> " + link);

                        rowData.put("6", MessageUtil.getResourceBundleMessage("has.made.empty.log.file.successful.have.prompt.in.start.line.when.end.command") + ".");
                        rowData.put("7", "1");
                        rowData.put("8", "");
                        rowData.put("9", des);
                        rowData.put("10", "");
                        data.put(i + "", rowData);
                        totalTime += 1;
                    }
                    i++;
                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2." + currentIndex + "." + i + "");
                    rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                    rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("execute.start.module"));
                    String startCmd = module.getStartService();
                    String startComment = "1. " + MessageUtil.getResourceBundleMessage("app.started.successful.have.prompt.in.start.line.when.end.command") + ".\n";
                    if (!codetaptrung) {
                        if (StringUtils.isNotEmpty(link) && StringUtils.isNotEmpty(key) && !link.trim().equals(Constant.NA_VALUE) && !key.trim().equals(Constant.NA_VALUE)) {
                            startCmd += ";\n" + "tail -500f " + link + " | grep --color " + "\"" + key + "\";\n"
                                    + "cat " + link + " | grep -a --color " + "\"" + key + "\"";
                            startComment += "2. " + MessageUtil.getResourceBundleMessage("find.keyword.in.log.command.response.execute.command.3.when.keyword.not.found") + "\n" +
                                    "3. " + MessageUtil.getResourceBundleMessage("find.keyword.in.log.command.response.execute.1.time.again.after.2.minute.when.keyword.not.found") + "\n";
                        } else if (StringUtils.isNotEmpty(module.getViewStatus()) && StringUtils.isNotEmpty(module.getKeyStatusStart()) && !module.getViewStatus().toUpperCase().equals(Constant.NA_VALUE)) {
                            startCmd += ";\n" + module.getViewStatus() + ";";
                            startComment += "2. " + MessageUtil.getResourceBundleMessage("have.keyword") + " \"" + module.getKeyStatusStart()
                                    + "\" " + MessageUtil.getResourceBundleMessage("in.the.result.returned") + ", " + MessageUtil.getResourceBundleMessage("execute.1.times.again.after.2.minute.when.keyword.not.found") + "\n";
                        } else {
                            startCmd += ";\n" + "ps -ef | grep \"" + LinuxParser.getFullPath(module.getPath()) + "\" | grep -v grep;";
                            startComment += "2. " + MessageUtil.getResourceBundleMessage("have.pid.of.process.response.if.process.have.not.pid.wait.2.minute.and.then.execute.1.time.again") + "\n";
                        }
                    }
                    rowData.put("4", startCmd);
                    rowData.put("6", startComment);
                    rowData.put("7", "1");
                    rowData.put("8", "");
                    rowData.put("9", des);
                    rowData.put("10", "");
                    data.put(i + "", rowData);
                    totalTime += 1;
                }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return data;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getTableRollbackCode(Long actionId, String date_time2, String crNumber) {
        LinkedHashMap<String, LinkedHashMap<String, String>> data = new LinkedHashMap<>();
        LinkedHashMap<String, String> rowData = null;
        ActionDetailAppServiceImpl serviceDetailApp = new ActionDetailAppServiceImpl();
        ActionModuleService serviceApp = new ActionModuleServiceImpl();

        try {
            Map<Long, ActionModule> mapApp = serviceApp.getMapObjByListAppId(actionId);
            List<ActionDetailApp> listDetails = serviceDetailApp.findListDetailApp(actionId, "2", moduleIds, false);

            int i = 0;
            if (listDetails != null)

                for (ActionDetailApp actionDetailApp : listDetails) {
                    Long appId = actionDetailApp.getModuleId();
                    ActionModule module = mapApp.get(appId);

                    if (AamConstants.OS_TYPE.WINDOWS != module.getOsType()) {
                        // cd vào thu muc cai dat 3.2.2
                        i++;
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2.2." + i + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("forward.to.folder.install"));
                        rowData.put("4", "cd " + module.getPath() + ";");

                        rowData.put("6", MessageUtil.getResourceBundleMessage("moved.to.app.directory.success.have.prompt.when.command.end"));
                        rowData.put("7", "1");
                        rowData.put("8", "");
                        rowData.put("9", "");
                        rowData.put("10", "");
                        data.put(i + "", rowData);
                        totalTimeRollback += 1;
                    }

                    i++;
                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2.2." + i + "");
                    rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                    rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                    rowData.put("3", "Backup lại code sai/cấu hình");
                    String cmd = AamConstants.OS_TYPE.WINDOWS == module.getOsType() ? "cmd /C move /Y \"" + module.getPath() + "\\" + actionDetailApp.getUpcodePath() + "\" \"" + module.getPath() + "\\backup_toantrinh\\" + actionDetailApp.getUpcodePath().replaceAll("\\.\\.\\\\", "") + "_fail_" + date_time2 + "_" + crNumber
                            : "mv " + actionDetailApp.getUpcodePath() + " backup_toantrinh/" + actionDetailApp.getUpcodePath().replaceAll("\\.\\./", "") + "_fail_" + date_time2
                            + "_" + crNumber;
                    rowData.put("4", cmd);

                    rowData.put("6", MessageUtil.getResourceBundleMessage("move.to.folder.code.in.local.successful.have.prompt.in.start.line.when.end.command"));
                    rowData.put("7", "1");
                    rowData.put("8", "");
                    rowData.put("9", "");
                    rowData.put("10", "");
                    data.put(i + "", rowData);
                    totalTimeRollback += 1;
                    i++;
                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2.2." + i + "");
                    rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                    rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("rollback.code"));
                    if (AamConstants.OS_TYPE.WINDOWS == module.getOsType()) {
                        String copyCmd = "cmd /C xcopy /E /Y /I";
                        if (actionDetailApp.getFile() != null && actionDetailApp.getFile()) {
                            copyCmd = "cmd /C echo F | xcopy /Y";
                        }
                        cmd = copyCmd + " \"" + module.getPath() + "\\backup_toantrinh\\" + actionDetailApp.getUpcodePath().replaceAll("\\.\\.\\\\", "") + "_bk" + date_time2 + "_" + crNumber + "\"" + " \"" + module.getPath() + "\\" + actionDetailApp.getUpcodePath() + "\"";
                    } else {
                        cmd = "cp -r  backup_toantrinh/" + actionDetailApp.getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date_time2 + "_" + crNumber + " "
                                + actionDetailApp.getUpcodePath();
                    }
                    rowData.put("4", cmd);

                    rowData.put("6", MessageUtil.getResourceBundleMessage("rollback.folder.code.config.successful.have.prompt.in.start.line.when.end.command"));
                    rowData.put("7", "2");
                    rowData.put("8", "");
                    rowData.put("9", "");
                    rowData.put("10", "");
                    data.put(i + "", rowData);
                    totalTimeRollback += 2;

                    //Quytv720190917_bo sung them lenh diff -rq start
                    if (AamConstants.OS_TYPE.WINDOWS != module.getOsType()) {
                        i++;
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2.2." + i + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("compare.code"));
                        cmd = "diff -rq backup_toantrinh/" + actionDetailApp.getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date_time2 + "_" + crNumber + " "
                                + actionDetailApp.getUpcodePath();
                        rowData.put("4", cmd);

                        rowData.put("6", MessageUtil.getResourceBundleMessage("compare.code.result"));
                        rowData.put("7", "2");
                        rowData.put("8", "");
                        rowData.put("9", "");
                        rowData.put("10", "");
                        data.put(i + "", rowData);
                        totalTimeRollback += 1;
                    }
                    //Quytv720190917_bo sung them lenh diff -rq end

                    if (AamConstants.OS_TYPE.WINDOWS != module.getOsType()) {
                        i++;
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2.2." + i + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
//					rowData.put("3", "So sanh kiểm tra dung lượng trước và sau backup");
                        rowData.put("3", MessageUtil.getResourceBundleMessage("check.for.existence.of.directory.code.after.rollback"));
//					rowData.put("4", "du -b " + actionDetailApp.getUpcodePath() + " backup_toantrinh/" + actionDetailApp.getUpcodePath() + "_bk" + date_time2 + "_" + crNumber);
//					rowData.put("4", "cd " + "backup_toantrinh/" + actionDetailApp.getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date_time2 + "_" + crNumber);
                        rowData.put("4", (actionDetailApp.getFile() != null && actionDetailApp.getFile() ? "ls -l " : "cd ") + actionDetailApp.getUpcodePath());

                        rowData.put("6", MessageUtil.getResourceBundleMessage("have.prompt.in.start.line.when.end.command"));
//					rowData.put("6", "3. Kiểm tra lại file cấu hình hiện tại và file backup có dung lượng bằng nhau, ví dụ:" + "\r\n" + "$ du -b etc backup_toantrinh/etc_bk13062016" + "\r\n" + "132    etc" + "\r\n" + "132    backup_toantrinh/etc_bk13062016");
                        rowData.put("7", "1");
                        rowData.put("8", "");
                        rowData.put("9", "");
                        rowData.put("10", "");
                        data.put(i + "", rowData);
                        totalTimeRollback += 1;
                    }

                }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return data;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getTableClearCache(Long actionId) {
        LinkedHashMap<String, LinkedHashMap<String, String>> data = new LinkedHashMap<>();
        LinkedHashMap<String, String> rowData = null;
        ActionDetailAppServiceImpl serviceDetailApp = new ActionDetailAppServiceImpl();
        ActionModuleService serviceApp = new ActionModuleServiceImpl();

        try {
            Map<Long, ActionModule> mapApp = serviceApp.getMapObjByListAppId(actionId);
            List<ActionDetailApp> listDetails = serviceDetailApp.findListDetailApp(actionId, "3", moduleIds, false);

            Integer currentIndex = indexMap.get(Constant.SUB_STEP_CLEARCACHE);
            int i = 0;
            if (listDetails != null)

                for (ActionDetailApp actionDetailApp : listDetails) {
                    Long appId = actionDetailApp.getModuleId();
                    ActionModule module = mapApp.get(appId);

                    i++;
                    // du lieu cho 1 row

                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2." + currentIndex + "." + i + "");
                    rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                    rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("clear.cache.app"));
                    if (module.getDeleteCache() != null) {
                        String action = "cd " + module.getPath() + ";" + "\r\n " + module.getDeleteCache();
                        rowData.put("4", action);

                        rowData.put("6", MessageUtil.getResourceBundleMessage("have.prompt.in.start.line.when.end.command"));
                        rowData.put("7", "1");
                        rowData.put("8", "");
                        rowData.put("9", "");
                        rowData.put("10", "");
                        data.put(module.getAppCode(), rowData);
                        totalTime += 1;
                        totalTimeRollback += 1;
                    } else {
                        String action = MessageUtil.getResourceBundleMessage("need.not.execute");
                        rowData.put("4", action);

                        rowData.put("6", "");
                        rowData.put("7", "");
                        rowData.put("8", "");
                        rowData.put("9", "");
                        rowData.put("10", "");
                        data.put(module.getAppCode(), rowData);
                    }
                }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return data;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getTableRestart(Long actionId, String mopRollBack, String crNumber, String date, MutableInt index) {
        LinkedHashMap<String, LinkedHashMap<String, String>> data = new LinkedHashMap<>();
        LinkedHashMap<String, String> rowData = null;
        ActionDetailAppServiceImpl serviceDetailApp = new ActionDetailAppServiceImpl();
        ActionModuleService serviceApp = new ActionModuleServiceImpl();

        try {
            Map<Long, ActionModule> mapApp = serviceApp.getMapObjByListAppId(actionId);
            List<ActionDetailApp> listDetails = serviceDetailApp.findListDetailApp(actionId, "4", moduleIds, false);
            String key;
            String link;
//			int i = 0;

            Integer currentIndex = indexMap.get(Constant.SUB_STEP_RESTART_APP);
            if (listDetails != null)

                for (ActionDetailApp actionDetailApp : listDetails) {
                    Long appId = actionDetailApp.getModuleId();
                    ActionModule module = mapApp.get(appId);
                    key = module.getKeyword();
                    link = module.getLogLink();
//					String key_status_start = module.getKeyStatusStart();

                    boolean codetaptrung = false;
                    if (module.getAppTypeCode() != null && module.getAppTypeCode().equals(Constant.CODETAPTRUNG_TYPE)) {
                        codetaptrung = true;
                    }

                    if (AamConstants.OS_TYPE.WINDOWS != module.getOsType()) {
                        // cd vào thu muc cai dat 3.2.7.
                        index.increment();
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + index + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("forward.to.folder.install"));
                        rowData.put("4", "cd " + module.getPath() + ";");

                        rowData.put("6", MessageUtil.getResourceBundleMessage("moved.to.app.directory.success.have.prompt.when.command.end"));
                        rowData.put("7", "1");
                        rowData.put("8", "");
                        rowData.put("9", "");
                        rowData.put("10", "");
                        data.put(index + "", rowData);
                        totalTime += 1;
                    }

                    index.increment();
                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2." + currentIndex + "." + index + "");
                    rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                    rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("stop.module"));
//					rowData.put("4", codetaptrung ? module.getStopService() : module.getStopService() + ";\r\n" + "ps  -ef | grep \"" + LinuxParser.getFullPath(module.getPath()) + "\" | grep -v grep; " + "\r\n" + "kill -9 PID;");

                    String stopCmd;
                    String expect;
                    if (StringUtils.isNotEmpty(module.getViewStatus()) && StringUtils.isNotEmpty(module.getKeyStatusStart()) && !module.getViewStatus().toUpperCase().equals(Constant.NA_VALUE)) {
                        stopCmd = module.getStopService();
                        rowData.put("4", stopCmd);
                        expect = "1. " + MessageUtil.getResourceBundleMessage("stop.app.success.have.command.prompt.when.command.end") + "\n"
                                + "2. " + MessageUtil.getResourceBundleMessage("if.after.desired.time.execute.command.without.command.prompt.press.ctrl.c");
                    } else {
                        stopCmd = codetaptrung ? module.getStopService() : module.getStopService() + ";" + "\r\n" + "\r\n" + "ps -ef | grep \"" + LinuxParser.getFullPath(module.getPath()) + "\" | grep -v grep; " + "\r\n" + "kill -9 PID;";
                        rowData.put("4", stopCmd);
                        expect = "1. " + MessageUtil.getResourceBundleMessage("stop.app.success.have.command.prompt.when.command.end") + "\n"
                                + "2. " + MessageUtil.getResourceBundleMessage("if.after.desired.time.execute.command.without.command.prompt.press.ctrl.c");
                        if (!codetaptrung) {
                            expect += ", " + MessageUtil.getResourceBundleMessage("execute.command.get.PID.of.process") + "\r\n"
                                    + MessageUtil.getResourceBundleMessage("example.get.PID.red.shown.below") + " (PID = 20894)"
                                    + "\r\n"
                                    + "$ ps -ef | grep /u01/BCCS_CODINH/CM_APP/jdk1.6.0_26/ | grep -v grep;"
                                    + "\r\n "
                                    + "5503      20894     1  2 Apr14 ?        03:12:52 "
                                    + "\r\n"
                                    + "/u01/BCCS_CODINH/CM_APP/jdk1.6.0_26/bin/java -"
                                    + "\r\n"
                                    + "3. " + MessageUtil.getResourceBundleMessage("pid.process.have.still.stop.process.using.command.kill.9.pid");

                        }
                    }
                    rowData.put("6", expect);
                    rowData.put("7", "3");
                    rowData.put("8", "");
                    rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                            + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                            + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                            + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.4 => " + MessageUtil.getResourceBundleMessage("Clear.cache.if.have") + "\r\n"
                            + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.1 => " + MessageUtil.getResourceBundleMessage("restart.module.again") + "\r\n"
                            + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                            + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                            + "B7: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                            + "B8: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                    rowData.put("10", "");
                    data.put(index + "", rowData);
                    totalTime += 3;

                    if (!codetaptrung) {
                        if (StringUtils.isNotEmpty(module.getViewStatus()) && StringUtils.isNotEmpty(module.getKeyStatusStart()) && !module.getViewStatus().toUpperCase().equals(Constant.NA_VALUE)) {
                            index.increment();
                            rowData = new LinkedHashMap<>();
                            rowData.put("1", "2.2." + currentIndex + "." + index + "");
                            rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                            rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                            rowData.put("3", MessageUtil.getResourceBundleMessage("check.module.has.stopped.altogether"));
                            rowData.put("4", module.getViewStatus());

                            rowData.put("6",
                                    MessageUtil.getResourceBundleMessage("have.not.keyword") + " \"" + module.getKeyStatusStart()
                                            + "\" " + MessageUtil.getResourceBundleMessage("appears.in.begin.line.has.command.prompt.after.end.command.process.has.been.stopped"));
                            rowData.put("7", "1");
                            rowData.put("8", "");
                            rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                    + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                    + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                                    + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.4 => " + MessageUtil.getResourceBundleMessage("Clear.cache.if.have") + "\r\n"
                                    + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.1 => " + MessageUtil.getResourceBundleMessage("restart.module.again") + "\r\n"
                                    + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                    + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                    + "B7: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                    + "B8: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                            rowData.put("10", "");
                            data.put(index + "", rowData);
                            totalTime += 1;
                        } else {
                            index.increment();
                            rowData = new LinkedHashMap<>();
                            rowData.put("1", "2.2." + currentIndex + "." + index + "");
                            rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                            rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                            rowData.put("3", MessageUtil.getResourceBundleMessage("check.module.has.stopped.altogether"));
                            rowData.put("4", "ps  -ef | grep \"" + LinuxParser.getFullPath(module.getPath()) + "\" | grep -v grep");

                            rowData.put("6", MessageUtil.getResourceBundleMessage("no.process.line.app.appears.there.is.command.prompt.after.end.command.process.has.been.stopped"));
                            rowData.put("7", "1");
                            rowData.put("8", "");
                            rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                    + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                    + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                                    + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.4 => " + MessageUtil.getResourceBundleMessage("Clear.cache.if.have") + "\r\n"
                                    + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.1 => " + MessageUtil.getResourceBundleMessage("restart.module.again") + "\r\n"
                                    + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                    + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                    + "B7: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                    + "B8: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                            rowData.put("10", "");
                            data.put(index + "", rowData);
                            totalTime += 1;
                        }
                    }

                    if (StringUtils.isNotEmpty(link) && !link.trim().equals(Constant.NA_VALUE)) {
                        index.increment();
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + index + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("backup.file.log.start.current"));
                        String cmd;
                        if (AamConstants.OS_TYPE.WINDOWS == module.getOsType()) {
                            cmd = "cmd /C echo F | xcopy /Y \"" + link + "\" \"" + link + "bk_" + crNumber + "_" + date + "\"";
                        } else {
                            cmd = "cp " + link + " " + link + "bk_" + crNumber + "_" + date + ";\n"
                                    + "zip -m " + link + "bk_" + crNumber + "_" + date + ".zip " + link + "bk_" + crNumber + "_" + date;
                        }
                        rowData.put("4", cmd);

                        rowData.put("6", "1. " + MessageUtil.getResourceBundleMessage("backup.file.log.successful.have.prompt.in.start.line.when.end.command") + ".\n" +
                                "2. " + MessageUtil.getResourceBundleMessage("zip.and.delete.file.log.after.backup.successful.have.prompt.in.start.line.when.end.command") + ".\n");
                        rowData.put("7", "2");
                        rowData.put("8", "");
                        rowData.put("9", "");
                        rowData.put("10", "");
                        data.put(index + "", rowData);
                        totalTime += 2;
                        index.increment();
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + index + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("before.enable.module.has.made.empty.log.file"));
                        if (AamConstants.OS_TYPE.WINDOWS == module.getOsType()) {
                            cmd = "cmd /C type NUL > \"" + link + "\"";
                        } else {
                            cmd = "> " + link;
                        }
                        rowData.put("4", cmd);

                        rowData.put("6", MessageUtil.getResourceBundleMessage("has.made.empty.log.file.successful.have.prompt.in.start.line.when.end.command") + ".");
                        rowData.put("7", "1");
                        rowData.put("8", "");
                        rowData.put("9", "");
                        rowData.put("10", "");
                        data.put(index + "", rowData);
                        totalTime += 1;
                    }

                    index.increment();
                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2." + currentIndex + "." + index + "");
                    rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                    rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("execute.start.module"));
                    String startCmd = module.getStartService();
                    String startComment = "1. " + MessageUtil.getResourceBundleMessage("app.started.successful.have.prompt.in.start.line.when.end.command") + ".\n";
                    if (!codetaptrung) {
                        if (StringUtils.isNotEmpty(link) && StringUtils.isNotEmpty(key) && !link.trim().equals(Constant.NA_VALUE) && !key.trim().equals(Constant.NA_VALUE)) {
                            if (AamConstants.OS_TYPE.WINDOWS == module.getOsType()) {
                                startCmd += ";\ncmd /C find /i " + "\"" + key + "\" \"" + link + "\"";
                            } else {
                                startCmd += ";\n" + "tail -500f " + link + " | grep --color " + "\"" + key + "\";\n"
                                        + "cat " + link + " | grep -a --color " + "\"" + key + "\"";
                                startComment += "2. " + MessageUtil.getResourceBundleMessage("find.keyword.in.log.command.response.execute.command.3.when.keyword.not.found") + "\n" +
                                        "3. " + MessageUtil.getResourceBundleMessage("find.keyword.in.log.command.response.execute.1.time.again.after.2.minute.when.keyword.not.found") + "\n";
                            }
                        } else if (StringUtils.isNotEmpty(module.getViewStatus()) && StringUtils.isNotEmpty(module.getKeyStatusStart()) && !module.getViewStatus().toUpperCase().equals(Constant.NA_VALUE)) {
                            startCmd += ";\n" + module.getViewStatus() + ";";
                            startComment += "2. " + MessageUtil.getResourceBundleMessage("have.keyword") + " \"" + module.getKeyStatusStart() + "\" "
                                    + MessageUtil.getResourceBundleMessage("in.the.result.returned") + ", " + MessageUtil.getResourceBundleMessage("execute.1.times.again.after.2.minute.when.keyword.not.found") + "\n";
                        } else {
                            startCmd += ";\n" + "ps -ef | grep \"" + LinuxParser.getFullPath(module.getPath()) + "\" | grep -v grep;";
                            startComment += "2. " + MessageUtil.getResourceBundleMessage("have.pid.of.process.response.if.process.have.not.pid.wait.2.minute.and.then.execute.1.time.again") + "\n";
                        }
                    }
                    rowData.put("4", startCmd);
                    rowData.put("6", startComment);
                    rowData.put("7", "1");
                    rowData.put("8", "");
                    rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                            + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                            + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                            + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.4 => " + MessageUtil.getResourceBundleMessage("Clear.cache.if.have") + "\r\n"
                            + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.1 => " + MessageUtil.getResourceBundleMessage("restart.module.again") + "\r\n"
                            + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                            + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                            + "B7: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                            + "B8: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                    rowData.put("10", "");
                    data.put(index + "", rowData);
                    totalTime += 1;
                }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return data;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getTableRestartCmd(Long actionId, String mopRollBack, String crNumber, String date, MutableInt index) {
        LinkedHashMap<String, LinkedHashMap<String, String>> data = new LinkedHashMap<>();
        LinkedHashMap<String, String> rowData = null;
        ActionDetailAppServiceImpl serviceDetailApp = new ActionDetailAppServiceImpl();
        ActionModuleService serviceApp = new ActionModuleServiceImpl();

        try {
            Map<Long, ActionModule> mapApp = serviceApp.getMapObjByListAppId(actionId);
            List<ActionDetailApp> listDetails = serviceDetailApp.findListDetailApp(actionId, "6", moduleIds, false);
            String key;
            String link;

            Integer currentIndex = indexMap.get(Constant.SUB_STEP_RESTART_APP);
            if (listDetails != null)

                for (ActionDetailApp actionDetailApp : listDetails) {
                    Long appId = actionDetailApp.getModuleId();
                    ActionModule module = mapApp.get(appId);

                    key = module.getKeyword();
                    link = module.getLogLink();

                    boolean codetaptrung = false;
                    if (module.getAppTypeCode() != null && module.getAppTypeCode().equals(Constant.CODETAPTRUNG_TYPE)) {
                        codetaptrung = true;
                    }
                    if (AamConstants.OS_TYPE.WINDOWS != module.getOsType()) {
                        // cd vào thu muc cai dat 3.2.7.
                        index.increment();
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + index + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("forward.to.folder.install"));
                        rowData.put("4", "cd " + module.getPath());

                        rowData.put("6", MessageUtil.getResourceBundleMessage("moved.to.app.directory.success.have.prompt.when.command.end"));
                        rowData.put("7", "1");
                        rowData.put("8", "");
                        rowData.put("9", "");
                        rowData.put("10", "");
                        data.put(index + "", rowData);
                        totalTime += 1;
                    }


                    index.increment();
                    String startCmd = module.getRestartService();
                    String startComment = "1. " + MessageUtil.getResourceBundleMessage("app.restarted.successful.have.prompt.in.start.line.when.end.command") + ".\n";
                    if (!codetaptrung) {
                        if (StringUtils.isNotEmpty(link) && StringUtils.isNotEmpty(key) && !link.trim().equals(Constant.NA_VALUE) && !key.trim().equals(Constant.NA_VALUE)) {
                            if (AamConstants.OS_TYPE.WINDOWS == module.getOsType()) {
                                startCmd += ";\ncmd /C find /i " + "\"" + key + "\" \"" + link + "\"";
                            } else {
                                startCmd += ";\n" + "tail -500f " + link + " | grep --color " + "\"" + key + "\";\n"
                                        + "cat " + link + " | grep -a --color " + "\"" + key + "\"";
                                startComment += "2. " + MessageUtil.getResourceBundleMessage("find.keyword.in.log.command.response.execute.command.3.when.keyword.not.found") + "\n" +
                                        "3. " + MessageUtil.getResourceBundleMessage("find.keyword.in.log.command.response.execute.1.time.again.after.2.minute.when.keyword.not.found") + "\n";
                            }
                        } else if (StringUtils.isNotEmpty(module.getViewStatus()) && StringUtils.isNotEmpty(module.getKeyStatusStart()) && !module.getViewStatus().toUpperCase().equals(Constant.NA_VALUE)) {
                            startCmd += ";\n" + module.getViewStatus() + ";";
                            startComment += "2. " + MessageUtil.getResourceBundleMessage("have.keyword") + " \"" + module.getKeyStatusStart() + "\" " + MessageUtil.getResourceBundleMessage("in.the.result.returned") + ", "
                                    + MessageUtil.getResourceBundleMessage("execute.1.times.again.after.2.minute.when.keyword.not.found") + "\n";
                        } else {
                            startCmd += ";\n" + "ps -ef | grep \"" + LinuxParser.getFullPath(module.getPath()) + "\" | grep -v grep;";
                            startComment += "2. " + MessageUtil.getResourceBundleMessage("have.pid.of.process.response.if.process.have.not.pid.wait.2.minute.and.then.execute.1.time.again") + "\n";
                        }
                    }
                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2." + currentIndex + "." + index + "");
                    rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                    rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                    rowData.put("3", "Restart module");
                    rowData.put("4", startCmd);

                    rowData.put("6", startComment);
                    rowData.put("7", "3");
                    rowData.put("8", "");
                    rowData.put("9", MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                            + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                            + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                            + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.4 => " + MessageUtil.getResourceBundleMessage("Clear.cache.if.have") + "\r\n"
                            + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.1 => " + MessageUtil.getResourceBundleMessage("restart.module.again") + "\r\n"
                            + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                            + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                            + "B7: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                            + "B8: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback"));
                    rowData.put("10", "");
                    data.put(index + "", rowData);
                    totalTime += 3;
                }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return data;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getTableRestartRb(Long actionId, String mopRollBack, String crNumber, String date, MutableInt index) {
        LinkedHashMap<String, LinkedHashMap<String, String>> data = new LinkedHashMap<>();
        LinkedHashMap<String, String> rowData = null;
        ActionDetailAppServiceImpl serviceDetailApp = new ActionDetailAppServiceImpl();
        ActionModuleService serviceApp = new ActionModuleServiceImpl();

        try {
//			List<Long> ids = service.findListModuleId(actionId);
            Map<Long, ActionModule> mapApp = serviceApp.getMapObjByListAppId(actionId);
            List<ActionDetailApp> listDetails = serviceDetailApp.findListDetailApp(actionId, "4", moduleIds, false);

            Integer currentIndex = indexMap.get(Constant.ROLLBACK_STEP_RESTART_APP);
//			int i = 0;
            if (listDetails != null)

                for (ActionDetailApp actionDetailApp : listDetails) {
                    Long appId = actionDetailApp.getModuleId();
                    ActionModule module = mapApp.get(appId);
                    String key = module.getKeyword();
                    String link = module.getLogLink();
//					String key_status_start = module.getKeyStatusStart();
                    boolean codetaptrung = false;
                    if (module.getAppTypeCode() != null && module.getAppTypeCode().equals(Constant.CODETAPTRUNG_TYPE)) {
                        codetaptrung = true;
                    }

                    if (AamConstants.OS_TYPE.WINDOWS != module.getOsType()) {
                        // cd vào thu muc cai dat 3.2.5.1
                        index.increment();
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + index + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("forward.to.folder.install"));
                        rowData.put("4", "cd " + module.getPath() + ";");

                        rowData.put("6", MessageUtil.getResourceBundleMessage("moved.to.app.directory.success.have.prompt.when.command.end"));
                        rowData.put("7", "1");
                        rowData.put("8", "");
                        rowData.put("9", "");
                        rowData.put("10", "");
                        data.put(index + "", rowData);
                        totalTimeRollback += 1;
                    }


                    index.increment();
                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2." + currentIndex + "." + index + "");
                    rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                    rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("stop.module"));


//                    rowData.put("4", module.getStopService() + ";\r\n" + "ps  -ef | grep \"" + LinuxParser.getFullPath(module.getPath()) + "\" | grep -v grep;" + "\r\n" + "kill -9 PID;");

                    String stopCmd;
                    String expect;
                    if (StringUtils.isNotEmpty(module.getViewStatus()) && StringUtils.isNotEmpty(module.getKeyStatusStart()) && !module.getViewStatus().toUpperCase().equals(Constant.NA_VALUE)) {
                        stopCmd = module.getStopService();
                        rowData.put("4", stopCmd);
                        expect = "1. " + MessageUtil.getResourceBundleMessage("stop.app.success.have.command.prompt.when.command.end") + "\n"
                                + "2. " + MessageUtil.getResourceBundleMessage("if.after.desired.time.execute.command.without.command.prompt.press.ctrl.c");
                    } else {
                        stopCmd = codetaptrung ? module.getStopService() : module.getStopService() + ";" + "\r\n" + "\r\n" + "ps -ef | grep \"" + LinuxParser.getFullPath(module.getPath()) + "\" | grep -v grep; " + "\r\n" + "kill -9 PID;";
                        rowData.put("4", stopCmd);
                        expect = "1. " + MessageUtil.getResourceBundleMessage("stop.app.success.have.command.prompt.when.command.end")
                                + "\r\n"
                                + "2. " + MessageUtil.getResourceBundleMessage("if.after.desired.time.execute.command.without.command.prompt.press.ctrl.c") + ", " + MessageUtil.getResourceBundleMessage("execute.command.get.PID.of.process")
                                + "\r\n"
                                + MessageUtil.getResourceBundleMessage("example.get.PID.red.shown.below") + " (PID = 20894)"
                                + "\r\n"
                                + "$ ps -ef | grep /u01/BCCS_CODINH/CM_APP/jdk1.6.0_26/ | grep -v grep;"
                                + "\r\n"
                                + "5503      20894     1  2 Apr14 ?        03:12:52 /u01/BCCS_CODINH/CM_APP/jdk1.6.0_26/bin/java "
                                + "\r\n"
                                + "3. " + MessageUtil.getResourceBundleMessage("pid.process.have.still.stop.process.using.command.kill.9.pid");
                    }

                    rowData.put("6", expect);
                    rowData.put("7", "2");
                    rowData.put("8", "");
                    rowData.put("9", "");
                    rowData.put("10", "");
                    data.put(index + "", rowData);
                    totalTimeRollback += 3;
                    if (!codetaptrung) {
                        if (StringUtils.isNotEmpty(module.getViewStatus()) && StringUtils.isNotEmpty(module.getKeyStatusStart()) && !module.getViewStatus().toUpperCase().equals(Constant.NA_VALUE)) {
                            index.increment();
                            rowData = new LinkedHashMap<>();
                            rowData.put("1", "2.2." + currentIndex + "." + index + "");
                            rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                            rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                            rowData.put("3", MessageUtil.getResourceBundleMessage("check.module.has.stopped.altogether"));
                            rowData.put("4", module.getViewStatus());

                            rowData.put("6", MessageUtil.getResourceBundleMessage("have.not.keyword") + " \"" + module.getKeyStatusStart()
                                    + "\" " + MessageUtil.getResourceBundleMessage("appears.in.begin.line.has.command.prompt.after.end.command.process.has.been.stopped"));
                            rowData.put("7", "1");
                            rowData.put("8", "");
                            rowData.put("9", "");
                            rowData.put("10", "");
                            data.put(index + "", rowData);
                            totalTimeRollback += 1;
                        } else {
                            index.increment();
                            rowData = new LinkedHashMap<>();
                            rowData.put("1", "2.2." + currentIndex + "." + index + "");
                            rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                            rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                            rowData.put("3", MessageUtil.getResourceBundleMessage("check.module.has.stopped.altogether"));
                            rowData.put("4", "ps  -ef | grep \"" + LinuxParser.getFullPath(module.getPath()) + "\" | grep -v grep");

                            rowData.put("6", MessageUtil.getResourceBundleMessage("no.process.line.app.appears.there.is.command.prompt.after.end.command.process.has.been.stopped"));
                            rowData.put("7", "1");
                            rowData.put("8", "");
                            rowData.put("9", "");
                            rowData.put("10", "");
                            data.put(index + "", rowData);
                            totalTimeRollback += 1;
                        }
                    }
                    if (StringUtils.isNotEmpty(link) && !link.trim().equals(Constant.NA_VALUE)) {

                        index.increment();
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + index + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("backup.file.log.start.current"));
                        String cmd;
                        if (AamConstants.OS_TYPE.WINDOWS == module.getOsType()) {
                            cmd = "cmd /C echo F | xcopy /Y \"" + link + "\" \"" + link + "bk_rollback_" + crNumber + "_" + date + "\"";
                        } else {
                            cmd = "cp " + link + " " + link + "bk_" + crNumber + "_" + date + ";\n"
                                    + "zip -m " + link + "bk_" + crNumber + "_" + date + ".zip " + link + "bk_rollback_" + crNumber + "_" + date;
                        }
                        rowData.put("4", cmd);

                        rowData.put("6", "1. " + MessageUtil.getResourceBundleMessage("backup.file.log.successful.have.prompt.in.start.line.when.end.command") + ".\n" +
                                "2. " + MessageUtil.getResourceBundleMessage("zip.and.delete.file.log.after.backup.successful.have.prompt.in.start.line.when.end.command") + ".\n");
                        rowData.put("7", "2");
                        rowData.put("8", "");

                        rowData.put("9", "");
                        rowData.put("10", "");
                        data.put(index + "", rowData);
                        totalTimeRollback += 2;
                        index.increment();
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + index + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("before.enable.module.has.made.empty.log.file"));
                        if (AamConstants.OS_TYPE.WINDOWS == module.getOsType()) {
                            cmd = "cmd /C type NUL > \"" + link + "\"";
                        } else {
                            cmd = "> " + link;
                        }
                        rowData.put("4", cmd);

                        rowData.put("6", MessageUtil.getResourceBundleMessage("has.made.empty.log.file.successful.have.prompt.in.start.line.when.end.command") + ".");
                        rowData.put("7", "1");
                        rowData.put("8", "");

                        rowData.put("9", "");
                        rowData.put("10", "");
                        data.put(index + "", rowData);
                        totalTimeRollback += 1;
                        index.increment();

                    }
                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2." + currentIndex + "." + index + "");
                    rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                    rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("execute.start.module"));
                    String startCmd = module.getStartService();
                    String startComment = "1. " + MessageUtil.getResourceBundleMessage("app.started.successful.have.prompt.in.start.line.when.end.command") + ".\n";
                    if (StringUtils.isNotEmpty(link) && StringUtils.isNotEmpty(key) && !link.trim().equals(Constant.NA_VALUE) && !key.trim().equals(Constant.NA_VALUE)) {
                        if (AamConstants.OS_TYPE.WINDOWS == module.getOsType()) {
                            startCmd += ";\ncmd /C find /i " + "\"" + key + "\" \"" + link + "\"";
                        } else {
                            startCmd += ";\n" + "tail -500f " + link + " | grep --color " + "\"" + key + "\";\n"
                                    + "cat " + link + " | grep -a --color " + "\"" + key + "\"";
                            startComment += "2. " + MessageUtil.getResourceBundleMessage("find.keyword.in.log.command.response.execute.command.3.when.keyword.not.found") + "\n" +
                                    "3. " + MessageUtil.getResourceBundleMessage("find.keyword.in.log.command.response.execute.1.time.again.after.2.minute.when.keyword.not.found") + "\n";
                        }
                    } else {
                        startCmd += ";\n" + "ps -ef | grep \"" + LinuxParser.getFullPath(module.getPath()) + "\" | grep -v grep;";
                        startComment += "2. " + MessageUtil.getResourceBundleMessage("have.pid.of.process.response.if.process.have.not.pid.wait.2.minute.and.then.execute.1.time.again") + "\n";
                    }
                    rowData.put("4", startCmd);
                    rowData.put("6", startComment);
                    rowData.put("7", "1");
                    rowData.put("7", "1");
                    rowData.put("8", "");
                    rowData.put("9", "");
                    rowData.put("10", "");
                    data.put(index + "", rowData);
                    totalTimeRollback += 1;
                }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return data;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getTableRestartCmdRb(Long actionId, String mopRollBack, String crNumber, String date, MutableInt index) {
        LinkedHashMap<String, LinkedHashMap<String, String>> data = new LinkedHashMap<>();
        LinkedHashMap<String, String> rowData = null;
        ActionDetailAppServiceImpl serviceDetailApp = new ActionDetailAppServiceImpl();
        ActionModuleService serviceApp = new ActionModuleServiceImpl();

        try {
            Map<Long, ActionModule> mapApp = serviceApp.getMapObjByListAppId(actionId);
            List<ActionDetailApp> listDetails = serviceDetailApp.findListDetailApp(actionId, "6", moduleIds, false);

//			int i = 0;
            if (listDetails != null)

                for (ActionDetailApp actionDetailApp : listDetails) {
                    Long appId = actionDetailApp.getModuleId();
                    ActionModule module = mapApp.get(appId);
                    String key = module.getKeyword();
                    String link = module.getLogLink();

                    boolean codetaptrung = false;
                    if (module.getAppTypeCode() != null && module.getAppTypeCode().equals(Constant.CODETAPTRUNG_TYPE)) {
                        codetaptrung = true;
                    }

                    if (AamConstants.OS_TYPE.WINDOWS != module.getOsType()) {
                        // cd vào thu muc cai dat 3.2.5.1
                        index.increment();
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2.5.1." + index + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("forward.to.folder.install"));
                        rowData.put("4", "cd " + module.getPath() + ";");

                        rowData.put("6", MessageUtil.getResourceBundleMessage("moved.to.app.directory.success.have.prompt.when.command.end"));
                        rowData.put("7", "1");
                        rowData.put("8", "");
                        rowData.put("9", "");
                        rowData.put("10", "");
                        data.put(index + "", rowData);
                        totalTimeRollback += 1;
                    }

                    index.increment();
                    String startCmd = module.getRestartService();
                    String startComment = "1. " + MessageUtil.getResourceBundleMessage("app.restarted.successful.have.prompt.in.start.line.when.end.command") + ".\n";
                    if (!codetaptrung) {
                        if (StringUtils.isNotEmpty(link) && StringUtils.isNotEmpty(key) && !link.trim().equals(Constant.NA_VALUE) && !key.trim().equals(Constant.NA_VALUE)) {
                            startCmd += ";\n" + "tail -500f " + link + " | grep --color " + "\"" + key + "\";\n"
                                    + "cat " + link + " | grep -a --color " + "\"" + key + "\"";
                            startComment += "2. " + MessageUtil.getResourceBundleMessage("find.keyword.in.log.command.response.execute.command.3.when.keyword.not.found") + "\n" +
                                    "3. " + MessageUtil.getResourceBundleMessage("find.keyword.in.log.command.response.execute.1.time.again.after.2.minute.when.keyword.not.found") + "\n";
                        } else if (StringUtils.isNotEmpty(module.getViewStatus()) && StringUtils.isNotEmpty(module.getKeyStatusStart()) && !module.getViewStatus().toUpperCase().equals(Constant.NA_VALUE)) {
                            startCmd += ";\n" + module.getViewStatus() + ";";
                            startComment += "2. " + MessageUtil.getResourceBundleMessage("have.keyword") + " \"" + module.getKeyStatusStart()
                                    + "\" " + MessageUtil.getResourceBundleMessage("in.the.result.returned") + ", "
                                    + MessageUtil.getResourceBundleMessage("execute.1.times.again.after.2.minute.when.keyword.not.found") + "\n";
                        } else {
                            startCmd += ";\n" + "ps -ef | grep \"" + LinuxParser.getFullPath(module.getPath()) + "\" | grep -v grep;";
                            startComment += "2. " + MessageUtil.getResourceBundleMessage("have.pid.of.process.response.if.process.have.not.pid.wait.2.minute.and.then.execute.1.time.again") + "\n";
                        }
                    }
                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2.5.1." + index + "");
                    rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                    rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                    rowData.put("3", "Restart module");
                    rowData.put("4", startCmd);

                    rowData.put("6", startComment);
                    rowData.put("7", "2");
                    rowData.put("8", "");
                    rowData.put("9", "");
                    rowData.put("10", "");
                    data.put(index + "", rowData);
                    totalTimeRollback += 3;
                }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return data;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getTableStart(Long actionId, String mopRollBack, String crNumber, String date) {
        LinkedHashMap<String, LinkedHashMap<String, String>> data = new LinkedHashMap<>();
        LinkedHashMap<String, String> rowData = null;
        ActionDetailAppServiceImpl serviceDetailApp = new ActionDetailAppServiceImpl();
        ActionDetailDatabaseServiceImpl serviceDetailDb = new ActionDetailDatabaseServiceImpl();
        ActionModuleService serviceApp = new ActionModuleServiceImpl();

        try {

            Map<Long, ActionModule> mapApp = serviceApp.getMapObjByListAppId(actionId);
            List<ActionDetailApp> listDetails = serviceDetailApp.findListDetailApp(actionId, "5", moduleIds, false);
            String key;
            String link;
            String des = "";
            int j = 0;
            int i = 0;
            Integer currentIndex = indexMap.get(Constant.SUB_STEP_START_APP);


            if (listDetails != null)

                for (ActionDetailApp actionDetailApp : listDetails) {
                    Long appId = actionDetailApp.getModuleId();
                    ActionModule module = mapApp.get(appId);
                    key = module.getKeyword();
                    link = module.getLogLink();
//					String key_status_start = module.getKeyStatusStart();

                    boolean codetaptrung = false;
                    if (module.getAppTypeCode() != null && module.getAppTypeCode().equals(Constant.CODETAPTRUNG_TYPE)) {
                        codetaptrung = true;
                    }

                    if (AamConstants.OS_TYPE.WINDOWS != module.getOsType()) {
                        // cd vào thu muc cai dat 2.2.8.
                        j++;
                        if (j == 1)
                            des = MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                    + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                    + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                                    + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.4 => " + MessageUtil.getResourceBundleMessage("Clear.cache.if.have") + "\r\n"
                                    + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.1 => " + MessageUtil.getResourceBundleMessage("restart.module.again") + "\r\n"
                                    + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                    + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                    + "B7: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                    + "B8: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback");

                        else
                            des = MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                                    + "B1: " + MessageUtil.getResourceBundleMessage("stop.the.modules.that.have.started") + ": " + MessageUtil.getResourceBundleMessage("made.from.the.step") + "2.2.1.1" + "=>" + "2.2.1." + (j - 1) * 3 + "\r\n"
                                    + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                                    + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                                    + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.4 => " + MessageUtil.getResourceBundleMessage("Clear.cache.if.have") + "\r\n"
                                    + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.1 => " + MessageUtil.getResourceBundleMessage("restart.module.again") + "\r\n"
                                    + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                                    + "B7: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                                    + "B8: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                                    + "B9: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback");
                        i++;
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + i + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("forward.to.folder.install"));
                        rowData.put("4", "cd " + module.getPath() + ";");

                        rowData.put("6", MessageUtil.getResourceBundleMessage("moved.to.app.directory.success.have.prompt.when.command.end"));
                        rowData.put("7", "1");
                        rowData.put("8", "");
                        rowData.put("9", des);
                        rowData.put("10", "2.2.8." + i + "");
                        data.put(i + "", rowData);
                        totalTime += 1;
                    }

                    if (StringUtils.isNotEmpty(link) && !link.trim().equals(Constant.NA_VALUE)) {

                        i++;
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + i + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("backup.file.log.start.current"));

                        String cmd;
                        if (AamConstants.OS_TYPE.WINDOWS == module.getOsType()) {
                            cmd = "cmd /C echo F | xcopy /Y \"" + link + "\" \"" + link + "bk_" + crNumber + "_" + date + "\"";
                        } else {
                            cmd = "cp " + link + " " + link + "bk_" + crNumber + "_" + date + ";\n"
                                    + "zip -m " + link + "bk_" + crNumber + "_" + date + ".zip " + link + "bk_" + crNumber + "_" + date;
                        }
                        rowData.put("4", cmd);

                        rowData.put("6", "1. " + MessageUtil.getResourceBundleMessage("backup.file.log.successful.have.prompt.in.start.line.when.end.command") + ".\n" +
                                "2. " + MessageUtil.getResourceBundleMessage("zip.and.delete.file.log.after.backup.successful.have.prompt.in.start.line.when.end.command") + ".\n");
                        rowData.put("7", "2");
                        rowData.put("8", "");
                        rowData.put("9", des);
                        rowData.put("10", "");
                        data.put(i + "", rowData);
                        totalTime += 2;
                        i++;
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + i + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("before.enable.module.has.made.empty.log.file"));
                        if (AamConstants.OS_TYPE.WINDOWS == module.getOsType()) {
                            cmd = "cmd /C type NUL > \"" + link + "\"";
                        } else {
                            cmd = "> " + link;
                        }
                        rowData.put("4", cmd);

                        rowData.put("6", MessageUtil.getResourceBundleMessage("has.made.empty.log.file.successful.have.prompt.in.start.line.when.end.command") + ".");
                        rowData.put("7", "1");
                        rowData.put("8", "");
                        rowData.put("9", des);
                        rowData.put("10", "");
                        data.put(i + "", rowData);
                        totalTime += 1;
                    }
                    i++;
                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2." + currentIndex + "." + i + "");
                    rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                    rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("execute.start.module"));
                    String startCmd = module.getStartService();
                    String startComment = "1. " + MessageUtil.getResourceBundleMessage("app.started.successful.have.prompt.in.start.line.when.end.command") + ".\n";
                    if (!codetaptrung) {
                        if (StringUtils.isNotEmpty(link) && StringUtils.isNotEmpty(key) && !link.trim().equals(Constant.NA_VALUE) && !key.trim().equals(Constant.NA_VALUE)) {
                            if (AamConstants.OS_TYPE.WINDOWS == module.getOsType()) {
                                startCmd += ";\ncmd /C find /i " + "\"" + key + "\" \"" + link + "\"";
                            } else {
                                startCmd += ";\n" + "tail -500f " + link + " | grep --color " + "\"" + key + "\";\n"
                                        + "cat " + link + " | grep -a --color " + "\"" + key + "\"";
                                startComment += "2. " + MessageUtil.getResourceBundleMessage("find.keyword.in.log.command.response.execute.command.3.when.keyword.not.found") + "\n" +
                                        "3. " + MessageUtil.getResourceBundleMessage("find.keyword.in.log.command.response.execute.1.time.again.after.2.minute.when.keyword.not.found") + "\n";
                            }
                        } else if (StringUtils.isNotEmpty(module.getViewStatus()) && StringUtils.isNotEmpty(module.getKeyStatusStart()) && !module.getViewStatus().toUpperCase().equals(Constant.NA_VALUE)) {
                            startCmd += ";\n" + module.getViewStatus() + ";";
                            startComment += "2. " + MessageUtil.getResourceBundleMessage("have.keyword") + " \"" + module.getKeyStatusStart()
                                    + "\" " + MessageUtil.getResourceBundleMessage("in.the.result.returned") + ", " + MessageUtil.getResourceBundleMessage("execute.1.times.again.after.2.minute.when.keyword.not.found") + "\n";
                        } else {
                            startCmd += ";\n" + "ps -ef | grep \"" + LinuxParser.getFullPath(module.getPath()) + "\" | grep -v grep;";
                            startComment += "2. " + MessageUtil.getResourceBundleMessage("have.pid.of.process.response.if.process.have.not.pid.wait.2.minute.and.then.execute.1.time.again") + "\n";
                        }
                    }
                    rowData.put("4", startCmd);
                    rowData.put("6", startComment);
                    rowData.put("7", "1");
                    rowData.put("8", "");
                    rowData.put("9", des);
                    rowData.put("10", "");
                    data.put(i + "", rowData);
                    totalTime += 1;
                }

            List<ActionDetailDatabase> listDetailDbs = serviceDetailDb.findListDetailDb(actionId, kbGroup, false, false);
            if (listDetailDbs != null) {

                for (ActionDetailDatabase actionDetailDb : listDetailDbs) {
                    if (!actionDetailDb.getType().equals(Constant.TD_DB_TYPE_STOP_START))
                        continue;

                    ServiceDatabase module = iimService.findServiceDbById(action.getImpactProcess().getNationCode(), actionDetailDb.getAppDbId());
                    String file = FilenameUtils.getName(actionDetailDb.getRollbackFile());
                    i++;
                    // du lieu cho 1 row 3.2.3.

                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2." + currentIndex + "." + i + "");
                    rowData.put("1.1", actionDetailDb.getActionOrder() + "");
                    rowData.put("2", module.getIpVirtual() + "@" + module.getUsername() + "\r\n" + "URL : " + module.getUrl());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("run.script.start.flag"));
                    String action;
                    if (actionDetailDb.getTypeImport() == null)
                        rowData.put("4", MessageUtil.getResourceBundleMessage("need.not.execute"));
                    else {
                        if (actionDetailDb.getTypeImport() == 1L)
                            if (file != null)
                                action = MessageUtil.getResourceBundleMessage("execute.commands.sql.in.the.file") + " " + file;
                            else
                                action = MessageUtil.getResourceBundleMessage("need.not.execute");
                        else {
                            action = "-- " + MessageUtil.getResourceBundleMessage("execute.commands.follow.script") + ": " + "\r\n" + actionDetailDb.getRollbackText();
                        }
                        rowData.put("4", action);
                    }
                    des = MessageUtil.getResourceBundleMessage("rollback.sequence.by.step.in.the.file") + " \"" + "\r\n" + mopRollBack + "\"" + "\r\n"
                            + "B1: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.3 => Rollback Database" + "\r\n"
                            + "B2: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.2 => Rollback Code/config" + "\r\n"
                            + "B3: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.4 => " + MessageUtil.getResourceBundleMessage("Clear.cache.if.have") + "\r\n"
                            + "B4: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.1 => " + MessageUtil.getResourceBundleMessage("restart.module.again") + "\r\n"
                            + "B5: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.2.5.2 => " + MessageUtil.getResourceBundleMessage("start.module.again") + "\r\n"
                            + "B6: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.3 => " + MessageUtil.getResourceBundleMessage("check.app.after.start") + "\r\n"
                            + "B7: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.4 => " + MessageUtil.getResourceBundleMessage("notice.to.the.department.check.service.test.rollback") + "\r\n"
                            + "B8: " + MessageUtil.getResourceBundleMessage("execute.step") + " 2.5 => " + MessageUtil.getResourceBundleMessage("execute.test.after.rollback");
                    // if(des==null)
                    // des = "Backup bảng";

                    rowData.put("5", MessageUtil.getResourceBundleMessage("run.script.successful.have.not.error"));
                    rowData.put("6", "5");
                    rowData.put("8", "");
                    rowData.put("9", des);
                    rowData.put("10", "");
                    data.put(module.getDbCode() + i++, rowData);
                    totalTimeRollback += 5;
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return data;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getTableStartRb(Long actionId, String mopRollBack, String crNumber, String date) {
        LinkedHashMap<String, LinkedHashMap<String, String>> data = new LinkedHashMap<>();
        LinkedHashMap<String, String> rowData = null;
        ActionDetailAppServiceImpl serviceDetailApp = new ActionDetailAppServiceImpl();
        ActionDetailDatabaseServiceImpl serviceDetailDb = new ActionDetailDatabaseServiceImpl();
        ActionModuleService serviceApp = new ActionModuleServiceImpl();

        try {

            Map<Long, ActionModule> mapApp = serviceApp.getMapObjByListAppId(actionId);
            List<ActionDetailApp> listDetails = serviceDetailApp.findListDetailApp(actionId, "1", false, moduleIds, false);

            Integer currentIndex = indexMap.get(Constant.ROLLBACK_STEP_START_APP);
            int i = 0;


            if (listDetails != null)

                for (ActionDetailApp actionDetailApp : listDetails) {
                    Long appId = actionDetailApp.getModuleId();
                    ActionModule module = mapApp.get(appId);
                    String key = module.getKeyword();
                    String link = module.getLogLink();
//					String key_status_start = module.getKeyStatusStart();

                    boolean codetaptrung = false;
                    if (module.getAppTypeCode() != null && module.getAppTypeCode().equals(Constant.CODETAPTRUNG_TYPE)) {
                        codetaptrung = true;
                    }

                    if (AamConstants.OS_TYPE.WINDOWS != module.getOsType()) {
                        // cd vào thu muc cai dat 3.2.5.2.
                        i++;
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + i + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("forward.to.folder.install"));
                        rowData.put("4", "cd " + module.getPath() + ";");

                        rowData.put("6", MessageUtil.getResourceBundleMessage("moved.to.app.directory.success.have.prompt.when.command.end"));
                        rowData.put("7", "1");
                        rowData.put("8", "");
                        rowData.put("9", "");
                        rowData.put("10", "");
                        data.put(i + "", rowData);
                        totalTimeRollback += 1;
                    }

                    if (StringUtils.isNotEmpty(link) && !link.trim().equals(Constant.NA_VALUE)) {

                        i++;
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + i + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("backup.file.log.start.current"));

                        String cmd;
                        if (AamConstants.OS_TYPE.WINDOWS == module.getOsType()) {
                            cmd = "cmd /C echo F | xcopy /Y \"" + link + "\" \"" + link + "bk_rollback_" + crNumber + "_" + date + "\"";
                        } else {
                            cmd = "cp " + link + " " + link + "bk_" + crNumber + "_" + date + ";\n"
                                    + "zip -m " + link + "bk_" + crNumber + "_" + date + ".zip " + link + "bk_rollback_" + crNumber + "_" + date;
                        }
                        rowData.put("4", cmd);

                        rowData.put("6", "1. " + MessageUtil.getResourceBundleMessage("backup.file.log.successful.have.prompt.in.start.line.when.end.command") + ".\n" +
                                "2. " + MessageUtil.getResourceBundleMessage("zip.and.delete.file.log.after.backup.successful.have.prompt.in.start.line.when.end.command") + ".\n");
                        rowData.put("7", "2");
                        rowData.put("8", "");
                        rowData.put("9", "");
                        rowData.put("10", "");
                        data.put(i + "", rowData);
                        totalTimeRollback += 2;
                        i++;
                        rowData = new LinkedHashMap<>();
                        rowData.put("1", "2.2." + currentIndex + "." + i + "");
                        rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                        rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                        rowData.put("3", MessageUtil.getResourceBundleMessage("before.enable.module.has.made.empty.log.file"));
                        if (AamConstants.OS_TYPE.WINDOWS == module.getOsType()) {
                            cmd = "cmd /C type NUL > \"" + link + "\"";
                        } else {
                            cmd = "> " + link;
                        }
                        rowData.put("4", cmd);

                        rowData.put("6", MessageUtil.getResourceBundleMessage("has.made.empty.log.file.successful.have.prompt.in.start.line.when.end.command") + ".");
                        rowData.put("7", "1");
                        rowData.put("8", "");
                        rowData.put("9", "");
                        rowData.put("10", "");
                        data.put(i + "", rowData);
                        totalTimeRollback += 1;
                    }
                    i++;
                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2." + currentIndex + "." + i + "");
                    rowData.put("1.1", actionDetailApp.getModuleOrder() + "");
                    rowData.put("2", module.getIpServer() + "@" + module.getInstalledUser() + "\r\n" + module.getAppCode());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("execute.start.module"));
                    String startCmd = module.getStartService();
                    String startComment = "1. " + MessageUtil.getResourceBundleMessage("app.started.successful.have.prompt.in.start.line.when.end.command") + ".\n";
                    if (!codetaptrung) {
                        if (StringUtils.isNotEmpty(link) && StringUtils.isNotEmpty(key) && !link.trim().equals(Constant.NA_VALUE) && !key.trim().equals(Constant.NA_VALUE)) {
                            if (AamConstants.OS_TYPE.WINDOWS == module.getOsType()) {
                                startCmd += ";\ncmd /C find /i " + "\"" + key + "\" \"" + link + "\"";
                            } else {
                                startCmd += ";\n" + "tail -500f " + link + " | grep --color " + "\"" + key + "\";\n"
                                        + "cat " + link + " | grep -a --color " + "\"" + key + "\"";
                                startComment += "2. " + MessageUtil.getResourceBundleMessage("find.keyword.in.log.command.response.execute.command.3.when.keyword.not.found") + "\n" +
                                        "3. " + MessageUtil.getResourceBundleMessage("find.keyword.in.log.command.response.execute.1.time.again.after.2.minute.when.keyword.not.found") + "\n";
                            }
                        } else if (StringUtils.isNotEmpty(module.getViewStatus()) && StringUtils.isNotEmpty(module.getKeyStatusStart()) && !module.getViewStatus().toUpperCase().equals(Constant.NA_VALUE)) {
                            startCmd += ";\n" + module.getViewStatus() + ";";
                            startComment += "2. " + MessageUtil.getResourceBundleMessage("have.keyword") + " \"" + module.getKeyStatusStart()
                                    + "\" " + MessageUtil.getResourceBundleMessage("in.the.result.returned") + ", " + MessageUtil.getResourceBundleMessage("execute.1.times.again.after.2.minute.when.keyword.not.found") + "\n";
                        } else {
                            startCmd += ";\n" + "ps -ef | grep \"" + LinuxParser.getFullPath(module.getPath()) + "\" | grep -v grep;";
                            startComment += "2. " + MessageUtil.getResourceBundleMessage("have.pid.of.process.response.if.process.have.not.pid.wait.2.minute.and.then.execute.1.time.again") + "\n";
                        }
                    }
                    rowData.put("4", startCmd);
                    rowData.put("6", startComment);
                    rowData.put("7", "1");
                    rowData.put("8", "");
                    rowData.put("9", "");
                    rowData.put("10", "");
                    data.put(i + "", rowData);
                    totalTimeRollback += 1;
                }

            List<ActionDetailDatabase> listDetailDbs = serviceDetailDb.findListDetailDb(actionId, kbGroup, false, false);
            if (listDetailDbs != null) {

                for (ActionDetailDatabase actionDetailDb : listDetailDbs) {
                    if (!actionDetailDb.getType().equals(Constant.TD_DB_TYPE_STOP_START))
                        continue;

                    ServiceDatabase module = iimService.findServiceDbById(action.getImpactProcess().getNationCode(), actionDetailDb.getAppDbId());
                    String file = FilenameUtils.getName(actionDetailDb.getRollbackFile());
                    i++;
                    // du lieu cho 1 row 3.2.3.

                    rowData = new LinkedHashMap<>();
                    rowData.put("1", "2.2." + currentIndex + "." + i + "");
                    rowData.put("1.1", actionDetailDb.getActionOrder() + "");
                    rowData.put("2", module.getIpVirtual() + "@" + module.getUsername() + "\r\n" + "URL : " + module.getUrl());
                    rowData.put("3", MessageUtil.getResourceBundleMessage("run.script.start.flag"));
                    String action;
                    if (actionDetailDb.getTypeImport() == null)
                        rowData.put("4", MessageUtil.getResourceBundleMessage("need.not.execute"));
                    else {
                        if (actionDetailDb.getTypeImport() == 1L)
                            if (file != null)
                                action = MessageUtil.getResourceBundleMessage("execute.commands.sql.in.the.file") + " " + file;
                            else
                                action = MessageUtil.getResourceBundleMessage("need.not.execute");
                        else {
                            action = "-- " + MessageUtil.getResourceBundleMessage("execute.commands.follow.script") + ": " + "\r\n" + actionDetailDb.getRollbackText();
                        }
                        rowData.put("4", action);
                    }

                    rowData.put("5", MessageUtil.getResourceBundleMessage("run.script.successful.have.not.error"));
                    rowData.put("6", "5");
                    rowData.put("8", "");
                    rowData.put("9", "");
                    rowData.put("10", "");
                    data.put(module.getDbCode() + i++, rowData);
                    totalTimeRollback += 5;
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return data;
    }

    public static String getDt(Module app, String action, ActionDetailApp detail) {
        String result = "";
        switch (action) {
            case AamConstants.RUN_STEP.STEP_BACKUP:
                if (StringUtils.isNotEmpty(detail.getUpcodePath())) {
                    // 20180629_hoangnd_check_null_osType
                    if (app.getOsType() != null && AamConstants.OS_TYPE.WINDOWS == app.getOsType()) {
                        String date_time2 = new SimpleDateFormat("ddMMyyyy").format(new Date());
//                        String cr = exeObject.getAction().getCrNumber().split("_")[exeObject.getAction().getCrNumber().split("_").length - 1];

                        String copyCmd = "cmd /C xcopy /E /Y /I";
                        if (detail.getFile() != null && detail.getFile()) {
                            copyCmd = "cmd /C echo F | xcopy /Y";
                        }

                        result = copyCmd + " \"" + app.getExecutePath() + "\\" + detail.getUpcodePath() + "\" \"" + app.getExecutePath() + "\\backup_toantrinh\\" + detail.getUpcodePath().replaceAll("\\.\\.\\\\", "") + "_bk" + date_time2 + "\"";
                    } else {
                        result = "1. cd " + app.getExecutePath() + ";" + "\r\n "
                                + "2. cp -r " + detail.getUpcodePath() + " backup_toantrinh/" + detail.getUpcodePath().replaceAll("\\.\\./", "")
                                + "_bk" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + "\r\n" + "3. du -sh " + detail.getUpcodePath() + ";";
                    }
                }
                break;
            case AamConstants.RUN_STEP.STEP_STOP:
                // 20180629_hoangnd_check_null_osType
                if (app.getOsType() != null && AamConstants.OS_TYPE.WINDOWS == app.getOsType()) {
                    result = "1. " + app.getStopService() + ";\r\n2. " + app.getViewStatus();
                } else {
                    result = "1. cd " + app.getExecutePath() + ";" + "\n " + "2. " + app.getStopService() + ";" + "\r\n" + "3. ps -ef | grep " + app.getExecutePath() + ";";
                }
                break;
            case AamConstants.RUN_STEP.STEP_UPCODE:
                /*
                 * result = "1. cd " + module.getPath() + "/" +
                 * actionDetailApp.getUpcodePath() + ";" + "\r\n " + "2. Up file  "
                 * + file + " lên server" + "\r\n" + "3. unzip " + file;
                 */
                break;
            case AamConstants.RUN_STEP.STEP_CLEARCACHE:
                // 20180629_hoangnd_check_null_osType
                if (app.getOsType() != null && AamConstants.OS_TYPE.WINDOWS == app.getOsType()) {
                    result = app.getDeleteCache();
                } else {
                    result = "1. cd " + app.getExecutePath() + ";" + "\r\n " + "2. " + app.getDeleteCache();
                }
                break;
            case AamConstants.RUN_STEP.STEP_RESTART:
                // 20180629_hoangnd_check_null_osType
                if (app.getOsType() != null && AamConstants.OS_TYPE.WINDOWS == app.getOsType()) {
                    result = "1. " + app.getStopService() + ";\r\n2. " + app.getViewStatus() + ";\r\n3. " + app.getStartService() + ";";
                } else {
                    result = "1. cd " + app.getExecutePath() + ";" + "\r\n " + "2. " + app.getStopService() + "; " + "\r\n" + "3. " + "ps -ef|grep " + app.getExecutePath() + "\r\n"
                            + "4. " + app.getStartService() + "; " + "\r\n" + "6. " + "ps -ef|grep " + app.getExecutePath();
                }
                break;
            case AamConstants.RUN_STEP.STEP_START:
                // 20180629_hoangnd_check_null_osType
                if (app.getOsType() != null && AamConstants.OS_TYPE.WINDOWS == app.getOsType()) {
                    result = "1. " + app.getStartService() + ";\r\n2. " + app.getViewStatus();
                } else {
                    result = "1. cd " + app.getExecutePath() + ";" + "\r\n " + "2. " + app.getStartService() + "; " + "\r\n" + "4. " + "ps -ef | grep " + app.getExecutePath();
                }
                break;
            case AamConstants.RUN_STEP.STEP_RESTART_CMD:
                // 20180629_hoangnd_check_null_osType
                if (app.getOsType() != null && AamConstants.OS_TYPE.WINDOWS == app.getOsType()) {
                    result = "1. " + app.getRestartService() + ";\r\n2. " + app.getViewStatus();
                } else {
                    result = "1. cd " + app.getExecutePath() + ";" + "\r\n " + "2. " + app.getRestartService() + ";";
                }
                break;
            default:
                break;
        }

        return result;
    }

//    public static void main(String[] args) throws Exception {

    // String str ="D:\\test\\test.txt";
    // System.out.println(FilenameUtils.getName(str));

    // WordprocessingMLPackage doc = WordprocessingMLPackage.load(new
    // java.io.File("D:/TEMPLATE_DT.docx"));
    // doc = new DocxUtil().findAndReplace(doc, "{TESTDMM}", "TEST");
    // SaveToZipFile saver = new SaveToZipFile(doc);
    // saver.save("D:/DT_test.docx");

    // MainDocumentPart documentPart = do1c.getMainDocumentPart();
    // // documentPart.variableReplace(arg0);
    // LinkedHashMap<String, LinkedHashMap<String, String>> data = new
    // LinkedHashMap<>();
    // LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
    // rowData.put("1", "1");
    // rowData.put("2", "Hệ thống cơ điện");
    // rowData.put("3", "10.10.0.0");
    // data.put("1", rowData);
    // Tbl table = DocxUtil.createTable(do1c, DocxUtil.columnNetworkNode,
    // data);
    // insertTable(do1c, true, "Bảng: Danh sách node mạng tác động", table);
    // do1c.save(new File("D:/quannt.docx"));
    //
    // DocxUtil t = new DocxUtil();
    // t.genericDT(1L, "D:/TEMPLATE_DT.docx", "D:/");

//    }

    // thay the nhieu ky tu trong path
    //
    public static String processPath(String path, String find, String replace) {
        String result = "";
        try {

            boolean flag = true;

            while (flag) {
                path = path.replace(find, replace);
                flag = path.contains(find);
            }
            result = path;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    private OsAccount findOSAccountByIpServer(String ipServer) {
        OsAccount monitorAccount = null;
        try {
            IimClientService iimClientService = new IimClientServiceImpl();
            List<OsAccount> osAccounts = iimClientService.findOsAccount(AamConstants.NATION_CODE.VIETNAM, ipServer);
            for (OsAccount osAccount : osAccounts) {
//                if (osAccount.getUserType().equals(2)) {
                monitorAccount = osAccount;
//                }
            }
            return monitorAccount;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
