package com.viettel.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;


import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import org.primefaces.model.UploadedFile;


public class FileHelper {
    private static Logger logger = LogManager.getLogger(FileHelper.class);


    /**
     * Su dung cho primefaces.
     */
    public static String uploadFile(String folderStore, UploadedFile fileUpload, String fileName) {
        if (fileUpload == null)
            return "FALSE";

        OutputStream outputStream;
        try {
            outputStream = getOutputStream(folderStore, fileName);
            outputStream.write(fileUpload.getContents());
            outputStream.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return "FALSE";
        }

        return "SUCCESS";
    }

    /**
     * Get output stream
     */
    public static OutputStream getOutputStream(String folderStore, String fileName) {
        OutputStream outputStream = null;
        try {
            File fileToCreate = new File(folderStore);
            if (!fileToCreate.exists()) {

                fileToCreate.mkdirs();
            }

            fileToCreate = new File(folderStore, fileName);
            outputStream = new FileOutputStream(fileToCreate);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return outputStream;
    }

    /**
     * Remove file
     */
    public static Boolean removeFile(String folderStore) {
        try {
            File fileToRemove = new File(folderStore);
            if (fileToRemove.exists()) {

                if (fileToRemove.delete()) {

                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * HungVC
     */
    public static String uploadFileCode(String folderStore, File fileUpload, String fileName) {
        if (fileUpload == null)
            return "FALSE";

        OutputStream outputStream;
        try {
            outputStream = getOutputStream(folderStore, fileName);
            FileInputStream fileInputStream = new FileInputStream(fileUpload);
            outputStream.write(fileInputStream.read());
            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return "FALSE";
        }

        return "SUCCESS";
    }
}
