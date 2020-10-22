package com.viettel.controller;

import com.viettel.it.util.LanguageBean;
import com.viettel.util.*;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * @author quanns2
 */
@ViewScoped
@ManagedBean
public class UserGuideController {
    private static Logger logger = LogManager.getLogger(UserGuideController.class);

    @ManagedProperty(value = "#{language}")
    LanguageBean languageBean;

    public void setLanguageBean(LanguageBean languageBean) {
        this.languageBean = languageBean;
    }

    private String module;
    private String locate;

    @PostConstruct
    public void onStart() {
        module = AamConstants.SERVICE;
        locate = languageBean.getLocaleCode();
    }

    public void change () {
        logger.info(module + '\t' + locate);
    }

        public void downloadHD(String filename) throws FileNotFoundException {
        logger.info(filename);
//        String template = getUploadFolder() + File.separator + "file-template" + File.separator + filename;
        File guideDir = new File(AppConfig.getInstance().getProperty("aam.guide.path"));
        File file1 = new File(guideDir.getPath() + File.separator + "HDSD_" + filename + "_" + locate + ".pdf");
        if (!file1.exists())
            file1 = new File(guideDir.getPath() + File.separator + "HDSD_" + filename + "_" + "vi" + ".pdf");

        if (!file1.exists())
            return;

        logger.info(file1);
        try {
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();

            IOUtils.copy(new FileInputStream(file1), response.getOutputStream());

            response.setContentType("application/pdf");
            response.addHeader("Content-disposition", "attachment; filename=HDSD_TDTT.pdf");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
        }
    }

    public void handleUpload(FileUploadEvent event) {
        logger.info(module + '\t' + locate);
        UploadedFile file = event.getFile();
        File guideDir = new File(AppConfig.getInstance().getProperty("aam.guide.path"));
        if (!guideDir.exists())
            guideDir.mkdirs();

        File file1 = new File(guideDir.getPath() + File.separator + "HDSD_" + module + "_" + locate + ".pdf");
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file1);
            IOUtils.copy(file.getInputstream(), outputStream);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (outputStream != null)
                try {
                    outputStream.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
        }
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getLocate() {
        return locate;
    }

    public void setLocate(String locate) {
        this.locate = locate;
    }
}
