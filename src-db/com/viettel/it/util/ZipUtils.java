/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author hienhv4
 */
public class ZipUtils {
    
    public static final String X_CHARFORM_NOHORN = "aaaaaaaaaaaaaaaaaeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyydAAAAAAAAAAAAAAAAAEEEEEEEEEEEIIIIIOOOOOOOOOOOOOOOOOUUUUUUUUUUUYYYYYD";
    public static final String X_CHARFORM_UNICODE = "áàãảạăắằẵẳặâấầẫẩậéèẽẻẹêếềễểệíìĩỉịóòõỏọôốồỗổộơớờỡởợúùũủụưứừữửựýỳỹỷỵđÁÀÃẢẠĂẮẰẴẲẶÂẤẦẪẨẬÉÈẼẺẸÊẾỀỄỂỆÍÌĨỈỊÓÒÕỎỌÔỐỒỖỔỘƠỚỜỠỞỢÚÙŨỦỤƯỨỪỮỬỰÝỲỸỶỴĐ";

    public static void zipFolder(final File folder, final File zipFile) throws IOException {
        zipFolder(folder, new FileOutputStream(zipFile));
    }

    public static void zipFolder(final File folder, final OutputStream outputStream) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            processFolder(folder, zipOutputStream, folder.getPath().length() + 1);
        }
    }

    private static void processFolder(final File folder, final ZipOutputStream zipOutputStream, final int prefixLength)
            throws IOException {
        for (final File file : folder.listFiles()) {
            if (file.isFile()) {
                final ZipEntry zipEntry = new ZipEntry(file.getPath().substring(prefixLength));
                zipOutputStream.putNextEntry(zipEntry);
                try (FileInputStream inputStream = new FileInputStream(file)) {
                    IOUtils.copy(inputStream, zipOutputStream);
                }
                zipOutputStream.closeEntry();
            } else if (file.isDirectory()) {
                processFolder(file, zipOutputStream, prefixLength);
            }
        }
    }
    
    public static String clearHornUnicode(String strSource) {
        try {
            return convertCharForm(strSource, X_CHARFORM_UNICODE, X_CHARFORM_NOHORN);
        } catch (Exception ex) {
            throw ex;
        }
    }

    private static String convertCharForm(String paramString1, String paramString2, String paramString3) {
        if (paramString1 == null) {
            return null;
        }
        int i = paramString1.length();
        int j;
        StringBuilder localStringBuffer = new StringBuilder();
        for (int k = 0; k < i; ++k) {
            char c = paramString1.charAt(k);
            if ((j = paramString2.indexOf(c)) >= 0) {
                localStringBuffer.append(paramString3.charAt(j));
            } else {
                localStringBuffer.append(c);
            }
        }
        return localStringBuffer.toString();
    }
    public static String getSafeFileName(String input) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != '/' && c != '\\' && c != 0) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    //20190416_tudn_start import rule config
    protected static final Logger LOGGER = LoggerFactory.getLogger(ZipUtils.class);
    public static void zipDirectory(File dir, String zipDirName) { //nen file khong mat folder
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            filesListInDir = new ArrayList<String>();
            populateFilesList(dir);
            //now zip files one by one
            //create ZipOutputStream to write to the zip file
            fos = new FileOutputStream(zipDirName);
            zos = new ZipOutputStream(fos);
            for (String filePath : filesListInDir) {
                System.out.println("Zipping " + filePath);
                //for ZipEntry we need to keep only relative file path, so we used substring on absolute path
                ZipEntry ze;
                if (!dir.isFile()) {
                    ze = new ZipEntry(filePath.substring(dir.getParentFile().getAbsolutePath().length() + 1, filePath.length()));
                } else {
//                    ze = new ZipEntry(filePath.substring(dir.getAbsolutePath().length() + 1, filePath.length()));
                    ze = new ZipEntry(filePath.substring(dir.getParentFile().getAbsolutePath().length() + 1, filePath.length()));
                }
                zos.putNextEntry(ze);
                zipInputStream(filePath,zos);
                //read the file and write to ZipOutputStream
//                FileInputStream fis = new FileInputStream(filePath);
//                byte[] buffer = new byte[1024];
//                int len;
//                while ((len = fis.read(buffer)) > 0) {
//                    zos.write(buffer, 0, len);
//                }
//                zos.closeEntry();
//                fis.close();
            }
//            zos.close();
//            fos.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            try {
                filesListInDir.clear();
                if (zos != null) {

                    zos.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    public static void zipInputStream(String filePath, ZipOutputStream zos){
        FileInputStream fis =null;
        try {
             fis = new FileInputStream(filePath);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
            zos.closeEntry();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    static List<String> filesListInDir = new ArrayList<String>();

    public static void populateFilesList(File dir) throws IOException {
        if(!dir.isFile()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                if (file.isFile()) filesListInDir.add(file.getAbsolutePath());
                else populateFilesList(file);
            }
        }else {
            filesListInDir.add(dir.getAbsolutePath());
        }
    }
    //20190416_tudn_end import rule config
}
