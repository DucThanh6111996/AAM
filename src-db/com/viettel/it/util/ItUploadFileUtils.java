package com.viettel.it.util;

import com.viettel.it.model.Action;
import com.viettel.util.AppConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by taitd on 6/8/2017.
 */
public class ItUploadFileUtils {
    private static Logger logger = LogManager.getLogger(ItUploadFileUtils.class);

    public static String getBaseFolder() {
        return AppConfig.getInstance().getProperty("data_base_dir");
    }

    public static String getImpactFileFolder() {
        String uploadFolder = getBaseFolder() + File.separator + "filetacdong";
        return uploadFolder;
    }

    public static String getMopFolder(Action action) {
        String uploadFolder = getBaseFolder() + File.separator + "20170608120000" + File.separator + "mop";
        return uploadFolder;
    }

    public static String getSourceCodeFolder(Action action) {
        String uploadFolder = getBaseFolder() + File.separator + "20170608120000" + File.separator + "source_code";
        return uploadFolder;
    }

    public static String getDatabaseFolder(Action action) {
        String uploadFolder = getBaseFolder() + File.separator + "20170608120000" + File.separator + "database";
        return uploadFolder;
    }

    public static String getTestcaseFolder(Action action) {
        String uploadFolder = getBaseFolder() + File.separator + "20170608120000" + File.separator + "testcase";
        return uploadFolder;
    }

    public static String getLogFolder(Action action) {
        String uploadFolder = getBaseFolder() + File.separator + "20170608120000" + File.separator + "logtacdong";
        return uploadFolder;
    }

    public static String getDataImportFolder(Action action) {
        String uploadFolder = getBaseFolder() + File.separator + "20170608120000" + File.separator + "data_import";
        return uploadFolder;
    }

    public static String getDataExportFolder(Action action) {
        String uploadFolder = getBaseFolder() + File.separator +"20170608120000" + File.separator + "data_export";
        return uploadFolder;
    }

    public static String getResourceFolder() {
        ClassLoader classLoader = new ItUploadFileUtils().getClass().getClassLoader();
        File file = new File(classLoader.getResource("hibernate.cfg.xml").getFile());
        String tmp = file.getParentFile().getParentFile().getParentFile() + File.separator + "resources";

        return tmp;
    }

    public static String checkEncoding(String file) {
        String encoding = null;
        InputStream fis = null;
        try {
            byte[] buf = new byte[4096];
            fis = new FileInputStream(file);

            UniversalDetector detector = new UniversalDetector(null);

            int nread;
            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
            detector.dataEnd();

            encoding = detector.getDetectedCharset();
            if (encoding != null) {
                System.out.println("Detected encoding = " + encoding);
            } else {
                System.out.println("No encoding detected.");
            }

            detector.reset();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
        }

        return encoding;
    }
}
