package com.viettel.util;

import com.viettel.model.Action;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;

/**
 * Created by quan on 6/19/2016.
 */
public class CheckZipUtils {
    private static Logger logger = LogManager.getLogger(CheckZipUtils.class);

    public static Boolean checkUpcodeZip(String file, String upcodeDir, Action action) {
        ZipFile zipFile = null;
        try {
            String uploadFolder = UploadFileUtils.getSourceCodeFolder(action);
            zipFile = new ZipFile(uploadFolder + File.separator + file);

            List<FileHeader> headers = zipFile.getFileHeaders();

            for (FileHeader fileHeader : headers) {
//                System.out.println(fileHeader.getFileName());
                String dirName = FilenameUtils.getName(upcodeDir);
                if (!fileHeader.getFileName().startsWith(dirName + "/") && !fileHeader.getFileName().equals(dirName))
                    return Boolean.FALSE;
            }
        } catch (ZipException e) {
            logger.error(e.getMessage(), e);
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }


}
