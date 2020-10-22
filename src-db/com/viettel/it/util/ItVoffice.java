package com.viettel.it.util;

import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import com.viettel.exception.AppException;
import com.viettel.it.model.Action;
import com.viettel.security.PassTranformer;
import com.viettel.util.*;
import com.viettel.voffice.*;
import com.viettel.voffice.ws_autosign.util.EncryptionUtils;
import com.viettel.voffice.ws_autosign.util.PassWordUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import javax.xml.rpc.ServiceException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by taitd on 6/8/2017.
 */
public class ItVoffice {
    private static Logger logger = Logger.getLogger(ItVoffice.class);
    private String mopAction = null;
    private String mopRollBack = null;
    private String mop = null;

    public Long signVoffice(Action action, String password) {
        Long status = null;
        try {
            mop = ItUploadFileUtils.getMopFolder(action);
            new File(mop).mkdirs();

            generateKb(action);
            /*ItDocxUtil.exportUctt(action, null);

            docx2Pdf(mop + File.separator + mopAction);
            docx2Pdf(mop + File.separator + mopRollBack);*/

            status = sign(action, password);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return status;
    }

    private String generateKb(Action action) {
        String mop = ItUploadFileUtils.getMopFolder(action);

        String path = null;
        try {
            WordprocessingMLPackage docx;
            String template = UploadFileUtils.getResourceFolder() + File.separator + "file-template" + File.separator + "TEMPLATE_UCTT.docx";
            docx = WordprocessingMLPackage.load(new File(template));
            replaceText(docx, action);
            docx.save(new File(mop + File.separator + "UCTT.docx"));

            docx2Pdf(mop + File.separator + "UCTT.docx");

            AddSignToPDF.addSignPlaceHolder(mop + File.separator + "UCTT.pdf", MessageUtil.getResourceBundleMessage("vice.director"), "1", -25, -30, 2);
            AddSignToPDF.addSignPlaceHolder(mop + File.separator + "UCTT.pdf", MessageUtil.getResourceBundleMessage("deputy.general.manager"), "2", -50, -30, 1);
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return path;
    }

    private void docx2Pdf(String fileName) throws Exception{
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

        new File(fileName).delete();
    }

//    public static void main(String[] args) {
//        ResourceBundle bundle = ResourceBundle.getBundle("config");
//        Vo2AutoSignSystemImplService sv = new Vo2AutoSignSystemImplServiceLocator();
//        Vo2AutoSignSystemImpl port = null;
//        try {
//            port = sv.getVo2AutoSignSystemImplPort(new URL(bundle.getString("ws_voffice_url")));
////            Vof2EntityUser[] vof2EntityUsers = port.getListVof2UserByMail(new String[]{"Thu1@viettel.com.vn"});
//        } catch (ServiceException e) {
//            logger.error(e.getMessage(), e);
//        } catch (MalformedURLException e) {
//            logger.error(e.getMessage(), e);
//        } catch (RemoteException e) {
//            logger.error(e.getMessage(), e);
//        }
//    }

    private Long sign(Action action, String password){
        Long status = null;
        ResourceBundle bundle = ResourceBundle.getBundle("config");
        Vo2AutoSignSystemImplService sv = new Vo2AutoSignSystemImplServiceLocator();
        Vo2AutoSignSystemImpl port = null;
        try {
            port = sv.getVo2AutoSignSystemImplPort(new URL(bundle.getString("ws_voffice_url")));
            KttsVofficeCommInpuParam param = new KttsVofficeCommInpuParam();

            String email1 = "Thu1@viettel.com.vn";
            String email2 = "thu2@viettel.com.vn";
            String email3 = "thu3@viettel.com.vn";

            String ucttPath = mop + File.separator + "UCTT.pdf";
           /* String kbUcttPath = mop + File.separator + mopAction.replaceAll(".docx$", ".pdf");
            String kbRollbackPath = mop + File.separator + mopRollBack.replaceAll(".docx$", ".pdf");*/

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            param.setCreateDate(sdf.format(action.getActionDetails().get(0).getActionCommands().get(0).getCreateTime()));
            String key = bundle.getString("ws_voffice_key");//lay key trong file config
            PassTranformer.setInputKey(key);//set key

            String appCodeEnc = EncryptionUtils.encrypt(bundle.getString("ws_voffice_user"), EncryptionUtils.getKey());
            String appPassEnc1 = PassWordUtil.getInstance().encrypt(bundle.getString("ws_voffice_pass"));
            String appPassEnc2 = EncryptionUtils.encrypt(appPassEnc1, EncryptionUtils.getKey());

            param.setAppCode(appCodeEnc);
            param.setAppPass(appPassEnc2);
            param.setAccountName("vof_test_tp2");
            String accountPassEnc = PassTranformer.encrypt(password);//ma hoa password
            param.setAccountPass(accountPassEnc);
            param.setTransCode("KB_UCTT_192");
            param.setSender(bundle.getString("ws_voffice_sender"));
            param.setDocTitle("ergerg");
            param.setRegisterNumber("/ĐX-VHKTTC-BOCNTT");
            param.setHinhthucVanban(4L);
            param.setIsCanVanthuXetduyet(true);

            FileAttachTranfer[] fileAttachTranfers = new FileAttachTranfer[1];
            FileAttachTranfer fileAttachTranfer = new FileAttachTranfer();
            fileAttachTranfer.setFileSign(1L);
            fileAttachTranfer.setFileName(FilenameUtils.getName(ucttPath));
            fileAttachTranfer.setAttachBytes(FileUtils.readFileToByteArray(new File(ucttPath)));
            fileAttachTranfer.setPath(ucttPath);
            fileAttachTranfers[0] = fileAttachTranfer;


            param.setLstFileAttach(fileAttachTranfers);
            param.setLstEmail(new String[]{email1, email2, email3});
            param.setMoneyUnitID(1L);
            param.setMoneyTransfer(0L);
            param.setAreaId(917L);
            param.setEmailPublishGroup(email3);

            List<Vof2EntityUser> entityUsers = new ArrayList<>();

            Vof2EntityUser[] vof2EntityUsers = port.getListVof2UserByMail(new String[]{email1});
            Vof2EntityUser entityUser = vof2EntityUsers[0];
            entityUser.setSignImageIndex(null);
            entityUsers.add(entityUser);

            vof2EntityUsers = port.getListVof2UserByMail(new String[]{email2});
            entityUser = vof2EntityUsers[0];
            entityUser.setSignImageIndex(1L);
            entityUsers.add(entityUser);

            vof2EntityUsers = port.getListVof2UserByMail(new String[]{email3});
            entityUser = vof2EntityUsers[0];
            entityUser.setSignImageIndex(2L);
            entityUsers.add(entityUser);

            param.setLstUserVof2(entityUsers.toArray(new Vof2EntityUser[3]));

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

    private void replaceText(WordprocessingMLPackage docx, Action action) {
//        String cr_number = "KB_UCTT_184";

//        String prefixName;
      /*  if (action.getActionType().equals(Constant.ACTION_TYPE_CR_NORMAL)) {
            prefixName = "MOP.CNTT.";
        } else if (action.getActionType().equals(Constant.ACTION_TYPE_CR_UCTT)) {
            prefixName = "MOP.CNTT.";
        } else if (action.getActionType().equals(Constant.ACTION_TYPE_KB_UCTT)) {*/
//            prefixName = "UCTT";
      //  }

//        String date_time2 = new SimpleDateFormat("ddMMyyyy").format(action.getActionDetails().get(0).getActionCommands().get(0).getCreateTime());
//        String cr = cr_number.split("_")[cr_number.split("_").length - 1];
//        String appName = "BANKPLUS";
        mopAction = "KB.UCTT_184_10022017_tacdong_.docx";
        mopRollBack = "KB.UCTT_184_10022017_rollback_.docx";

        DocxUtil.findAndReplace(docx, "{TEN KB}", "Test tạo file 08062017");
        DocxUtil.findAndReplace(docx, "{PURPOSE}", "Ứng cứu thông tin trong trường hợp server down");
        DocxUtil.findAndReplace(docx, "{TEN KB TD}", action.getActionDetails().get(0).getActionCommands().get(0).getCommandDetail().getCommandName());
        DocxUtil.findAndReplace(docx, "{TEN KB ROLLBACK}", action.getActionDetails().get(0).getActionCommands().get(0).getCommandDetail().getCommandName());
        DocxUtil.findAndReplace(docx, "{CREATED_BY}", "-------DUNGPV-----");

        DocxUtil.findAndReplace(docx, "{dd}", new SimpleDateFormat("dd").format(action.getActionDetails().get(0).getActionCommands().get(0).getCreateTime()));
        String month = new SimpleDateFormat("M").format(action.getActionDetails().get(0).getActionCommands().get(0).getCreateTime());
        DocxUtil.findAndReplace(docx, "{MM}", Arrays.asList("1", "2").contains(month) ? 0 + month : month);
        DocxUtil.findAndReplace(docx, "{yyyy}", new SimpleDateFormat("yyyy").format(action.getActionDetails().get(0).getActionCommands().get(0).getCreateTime()));
    }

}
