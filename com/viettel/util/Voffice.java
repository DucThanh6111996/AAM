package com.viettel.util;

import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import com.viettel.exception.AppException;
import com.viettel.model.Action;
import com.viettel.security.PassTranformer;
import com.viettel.voffice.*;
import com.viettel.voffice.ws_autosign.util.EncryptionUtils;
import com.viettel.voffice.ws_autosign.util.PassWordUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import javax.xml.rpc.ServiceException;
import java.io.*;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by quanns2 on 2/7/2017.
 */

public class Voffice {
    private static Logger logger = Logger.getLogger(Voffice.class);

    private List<String> mopActions = null;
    private List<String> mopRollBacks = null;
    private String mop = null;
    private List<Integer> kbGroups;

    public static void main(String[] args) {
        String fileName = "C:\\Users\\tuanda38.VIETTELGROUP\\Desktop\\UCTT.docx";
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(fileName);
            outputStream = new FileOutputStream(fileName.replaceAll(".docx$", ".pdf"));

            Document doc = new Document(inputStream);
            doc.save(outputStream, SaveFormat.PDF);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if (outputStream != null)
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
//        Voffice voffice = new Voffice();
//
//        ActionService actionService = new ActionServiceImpl();
//        Action action = null;
//        try {
//            action = actionService.findActionByCode("KB_UCTT_184");
//        } catch (AppException e) {
//            logger.error(e.getMessage(), e);
//        }
//
//        voffice.signVoffice(action, "222222a@", Arrays.asList(1));
    }

    public Long signVoffice(Action action, String password, List<Integer> kbGroups) {
        Long status = null;
        this.kbGroups = kbGroups;
        this.mopActions = new ArrayList<>();
        this.mopRollBacks = new ArrayList<>();
        try {
            mop = UploadFileUtils.getMopFolder(action);
            new File(mop).mkdirs();

            generateKb(action);
            DocxUtil.exportUctt(action, null);

            for (String mopAction : mopActions) {
                docx2Pdf(mop + File.separator + mopAction);
            }
            for (String mopRollBack : mopRollBacks) {
                docx2Pdf(mop + File.separator + mopRollBack);
            }

            status = sign(action, password);
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return status;
    }

    private Long sign(Action action, String password) {
        Long status = null;
        ResourceBundle bundle = ResourceBundle.getBundle("config");
        Vo2AutoSignSystemImplService sv = new Vo2AutoSignSystemImplServiceLocator();
        Vo2AutoSignSystemImpl port = null;
        try {
            port = sv.getVo2AutoSignSystemImplPort(new URL(bundle.getString("ws_voffice_url")));
            KttsVofficeCommInpuParam param = new KttsVofficeCommInpuParam();

            String email1 = action.getUserSign1();
            String email2 = action.getUserSign2();
            String email3 = action.getUserSign3();

            String ucttPath = mop + File.separator + "UCTT.pdf";

            List<String> kbUcttPaths = new ArrayList<>();
            List<String> kbRollbackPaths = new ArrayList<>();
            for (String mopAction : mopActions) {
                String kbUcttPath = mop + File.separator + mopAction.replaceAll(".docx$", ".pdf");
                kbUcttPaths.add(kbUcttPath);
            }
            for (String mopRollBack : mopRollBacks) {
                String kbRollbackPath = mop + File.separator + mopRollBack.replaceAll(".docx$", ".pdf");
                kbRollbackPaths.add(kbRollbackPath);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            param.setCreateDate(sdf.format(action.getCreatedTime()));
            String key = bundle.getString("ws_voffice_key");//lay key trong file config
            PassTranformer.setInputKey(key);//set key

            String appCodeEnc = EncryptionUtils.encrypt(bundle.getString("ws_voffice_user"), EncryptionUtils.getKey());
            String appPassEnc1 = PassWordUtil.getInstance().encrypt(PasswordEncoder.decrypt(bundle.getString("ws_voffice_pass")));
            String appPassEnc2 = EncryptionUtils.encrypt(appPassEnc1, EncryptionUtils.getKey());

            param.setAppCode(appCodeEnc);
            param.setAppPass(appPassEnc2);
            param.setAccountName(action.getCreatedBy());
            String accountPassEnc = PassTranformer.encrypt(password);//ma hoa password
            param.setAccountPass(accountPassEnc);
            param.setTransCode(action.getTdCode());
            param.setSender(bundle.getString("ws_voffice_sender"));
            param.setDocTitle(action.getCrName());
            param.setRegisterNumber("/ĐX-VHKTTC-BOCNTT");
            param.setHinhthucVanban(4L);
            param.setIsCanVanthuXetduyet(true);

            FileAttachTranfer[] fileAttachTranfers = new FileAttachTranfer[1 + kbUcttPaths.size() + kbRollbackPaths.size()];
            FileAttachTranfer fileAttachTranfer = new FileAttachTranfer();
            fileAttachTranfer.setFileSign(1L);
            fileAttachTranfer.setFileName(FilenameUtils.getName(ucttPath));
            fileAttachTranfer.setAttachBytes(FileUtils.readFileToByteArray(new File(ucttPath)));
            fileAttachTranfer.setPath(ucttPath);
            fileAttachTranfers[0] = fileAttachTranfer;

            Integer fileIndex = 0;

            for (String kbUcttPath : kbUcttPaths) {
                fileIndex ++;
                fileAttachTranfer = new FileAttachTranfer();
                fileAttachTranfer.setFileSign(2L);
                fileAttachTranfer.setFileName(FilenameUtils.getName(kbUcttPath));
                fileAttachTranfer.setAttachBytes(FileUtils.readFileToByteArray(new File(kbUcttPath)));
                fileAttachTranfer.setPath(kbUcttPath);
                fileAttachTranfers[fileIndex] = fileAttachTranfer;
            }

            for (String kbRollbackPath : kbRollbackPaths) {
                fileIndex ++;
                fileAttachTranfer = new FileAttachTranfer();
                fileAttachTranfer.setFileSign(2L);
                fileAttachTranfer.setFileName(FilenameUtils.getName(kbRollbackPath));
                fileAttachTranfer.setAttachBytes(FileUtils.readFileToByteArray(new File(kbRollbackPath)));
                fileAttachTranfer.setPath(kbRollbackPath);
                fileAttachTranfers[fileIndex] = fileAttachTranfer;
            }

            param.setLstFileAttach(fileAttachTranfers);
            param.setLstEmail(new String[]{email1, email2, email3});
            param.setMoneyUnitID(1L);
            param.setMoneyTransfer(0L);
            param.setAreaId(917L);
            param.setEmailPublishGroup(email3);

            List<Vof2EntityUser> entityUsers = new ArrayList<>();

            Vof2EntityUser[] vof2EntityUsers;
            Vof2EntityUser entityUser;

            if(email1 != null && email1.isEmpty()) {
                vof2EntityUsers = port.getListVof2UserByMail(new String[]{email1});
                entityUser = vof2EntityUsers[0];
                entityUser.setSignImageIndex(null);
                entityUsers.add(entityUser);
            }

            if(email2 != null && email2.isEmpty()){
                vof2EntityUsers = port.getListVof2UserByMail(new String[]{email2});
                entityUser = vof2EntityUsers[0];
                entityUser.setSignImageIndex(1L);
                entityUsers.add(entityUser);
            }

            if(email3 != null && !email3.isEmpty()) {
                vof2EntityUsers = port.getListVof2UserByMail(new String[]{email3});
                entityUser = vof2EntityUsers[0];
                entityUser.setSignImageIndex(2L);
                entityUsers.add(entityUser);
            }

            param.setLstUserVof2(entityUsers.toArray(new Vof2EntityUser[entityUsers.size()]));

            status = port.vo2RegDigitalDocByEmail(param);
            logger.info(status);
        } catch (ServiceException e) {
            logger.error(e.getMessage(), e);
        } catch (RemoteException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return status;
    }

    private String generateKb(Action action) {
        String mop = UploadFileUtils.getMopFolder(action);

        String path = null;
        try {
            WordprocessingMLPackage docx;
            String template = UploadFileUtils.getResourceFolder() + File.separator + "file-template" + File.separator + "TEMPLATE_UCTT.docx";
            docx = WordprocessingMLPackage.load(new File(template));
            replaceText(docx, action);
            docx.save(new File(mop + File.separator + "UCTT.docx"));

            docx2Pdf(mop + File.separator + "UCTT.docx");

            AddSignToPDF.addSignPlaceHolder(mop + File.separator + "UCTT.pdf", "P.GIÁM ĐỐC", "1", -25, -30, 2);
//            AddSignToPDF.addSignPlaceHolder(mop + File.separator + "UCTT.pdf", MessageUtil.getResourceBundleMessage("vice.director"), "1", -25, -30, 2);
            AddSignToPDF.addSignPlaceHolder(mop + File.separator + "UCTT.pdf", "P.TỔNG GIÁM ĐỐC", "2", -50, -30, 1);
//            AddSignToPDF.addSignPlaceHolder(mop + File.separator + "UCTT.pdf", MessageUtil.getResourceBundleMessage("deputy.general.manager"), "2", -50, -30, 1);
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return path;
    }

    private void docx2Pdf(String fileName) throws Exception {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(fileName);
            outputStream = new FileOutputStream(fileName.replaceAll(".docx$", ".pdf"));

            Document doc = new Document(inputStream);
            doc.save(outputStream, SaveFormat.PDF);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (inputStream != null)
                inputStream.close();
            if (outputStream != null)
                outputStream.close();
        }

//        new File(fileName).delete();
    }

    private void replaceText(WordprocessingMLPackage docx, Action action) {
        String cr_number = action.getTdCode();

        String prefixName = null;
        if (action.getActionType().equals(Constant.ACTION_TYPE_CR_NORMAL)) {
            prefixName = "MOP.CNTT.";
        } else if (action.getActionType().equals(Constant.ACTION_TYPE_CR_UCTT)) {
            prefixName = "MOP.CNTT.";
        } else if (action.getActionType().equals(Constant.ACTION_TYPE_KB_UCTT)) {
            prefixName = "KB.UCTT.";
        }

        String date_time2 = new SimpleDateFormat("ddMMyyyy").format(action.getCreatedTime());
        String cr = cr_number.split("_")[cr_number.split("_").length - 1];
        String appName = Util.convertUTF8ToNoSign(new DocxUtil(action).getAppGroupName(action.getId())).replaceAll("\\?", "");

        for (Integer kbGroup : kbGroups) {
            String mopAction = prefixName + appName + "_" + cr + "_" + date_time2 + "_tacdong_" + kbGroup + ".docx";
            String mopRollBack = prefixName + appName + "_" + cr + "_" + date_time2 + "_rollback_" + kbGroup + ".docx";
            //tuanda38_20180801_fix sign_start
            mopActions.add(mopAction);
            mopRollBacks.add(mopRollBack);

            DocxUtil.findAndReplace(docx, "{TEN KB}", action.getCrName());
            DocxUtil.findAndReplace(docx, "{PURPOSE}", action.getReason());
            DocxUtil.findAndReplace(docx, "{TEN KB TD}", mopAction.replaceAll(".docx$", ".pdf"));
            DocxUtil.findAndReplace(docx, "{TEN KB ROLLBACK}", mopRollBack.replaceAll(".docx$", ".pdf"));
            DocxUtil.findAndReplace(docx, "{CREATED_BY}", action.getFullName().substring(action.getFullName().lastIndexOf(' ')));

            DocxUtil.findAndReplace(docx, "{dd}", new SimpleDateFormat("dd").format(action.getCreatedTime()));
            String month = new SimpleDateFormat("M").format(action.getCreatedTime());
            DocxUtil.findAndReplace(docx, "{MM}", Arrays.asList("1", "2").contains(month) ? 0 + month : month);
            DocxUtil.findAndReplace(docx, "{yyyy}", new SimpleDateFormat("yyyy").format(action.getCreatedTime()));
        }
    }
}
