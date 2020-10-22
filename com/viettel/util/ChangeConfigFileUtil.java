package com.viettel.util;

import com.viettel.model.RuleConfig;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class ChangeConfigFileUtil {

    protected static final Logger logger = LoggerFactory.getLogger(ChangeConfigFileUtil.class);

    public static void main(String[] args) {
        try {
            List<RuleConfig> ruleConfigs = new ArrayList<>();
            RuleConfig r = new RuleConfig();
            r.setRuleEdit("RULE_ADD_AFTER_LINE_KEYWORD");
            r.setKeyword("process_socket_ip");
            r.setContent("thenv_");
            ruleConfigs.add(r);

            FileInputStream is = new FileInputStream("D://config.properties");
            System.out.println(ChangeConfigFileUtil.changeConfigByRules(is, ruleConfigs));

            is.close();
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public static String changeConfigByRules(FileInputStream inputStream, List<RuleConfig> ruleConfigs) {
        String str = convertInputStreamToString(inputStream);
        return changeConfigByRules(str, ruleConfigs);
    }

    public static String changeConfigByRule(String str, RuleConfig r) {
        List<RuleConfig> ruleConfigs = new ArrayList<>();
        ruleConfigs.add(r);
        return changeConfigByRules(str, ruleConfigs);
    }

    public static String changeConfigByRules(String str, List<RuleConfig> ruleConfigs) {

        for (RuleConfig r : ruleConfigs) {
            switch (r.getRuleEdit()) {
                case "RULE_ADD_ON_BOTTOM":
                    str = ruleAddOnBottom(str, r);
                    break;
                case "RULE_ADD_ON_HEAD":
                    str = ruleAddOnHead(str, r);
                    break;
                case "RULE_ADD_BEFORE_KEYWORD":
                    str = ruleAddBeforeKeyword(str, r);
                    break;
                case "RULE_ADD_AFTER_KEYWORD":
                    str = ruleAddAfterKeyword(str, r);
                    break;
                case "RULE_REPLACE_KEYWORD":
                    str = ruleReplaceKeyword(str, r);
                    break;
                case "RULE_ADD_BEFORE_LINE_KEYWORD":
                    str = ruleAddBeforeLineKeyword(str, r);
                    break;
                case "RULE_ADD_AFTER_LINE_KEYWORD":
                    str = ruleAddAfterLineKeyword(str, r);
                    break;
                case "RULE_DELETE_KEYWORD":
                    str = ruleDeleteKeyword(str, r);
                    break;
            }
        }
        return str;
    }

    private static String convertInputStreamToString(FileInputStream inputStream) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
            return sb.toString();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return "";
        }
    }

    public static String unzipFileToString(String path, String fileNameZip) {
        FileInputStream fis = null;
        FileInputStream is = null;
        ZipInputStream zis = null;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(path + File.separator + fileNameZip);
            zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            String pathUnzipFile = null;
            while(ze != null){
                String fileName = ze.getName();
                pathUnzipFile = path + File.separator + fileName;
                File newFile = new File(pathUnzipFile);
                System.out.println("Unzipping to "+newFile.getAbsolutePath());
                //create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();
//                FileOutputStream fos = new FileOutputStream(newFile);
//                int len;
//                while ((len = zis.read(buffer)) > 0) {
//                    fos.write(buffer, 0, len);
//                }
//                fos.close();
//                //close this ZipEntry
//                zis.closeEntry();
                zis = unzipFile(zis, pathUnzipFile, buffer);
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
//            zis.close();
//            fis.close();

            // convert file to string
            is = new FileInputStream(pathUnzipFile);
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                    sb.append('\n');
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }

            File delFile = new File(pathUnzipFile);
            FileUtils.forceDelete(delFile);
            return sb.toString();
        } catch (IOException e) {
            logger.error(e.getMessage());
            return "";
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (zis != null) {
                try {
                    zis.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    private static ZipInputStream unzipFile(ZipInputStream zis, String pathUnzipFile, byte[] buffer) {
        FileOutputStream fos = null;
        try {
            File newFile = new File(pathUnzipFile);
            System.out.println("Unzipping to "+newFile.getAbsolutePath());
            //create directories for sub directories in zip
            new File(newFile.getParent()).mkdirs();
            fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            //close this ZipEntry
            zis.closeEntry();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return zis;
    }

    /**
     * Bo sung noi dung can sua vao cuoi file
     */
    private static String ruleAddOnBottom(String str, RuleConfig ruleConfig) {
        if (ruleConfig.getContent() != null && !"".equalsIgnoreCase(ruleConfig.getContent().trim())) {
            str += ruleConfig.getContent();
        }
        return str;
    }

    /**
     * Bo sung noi dung can sua vao dau file cau hinh
     */
    private static String ruleAddOnHead(String str, RuleConfig ruleConfig) {
        if (ruleConfig.getContent() != null && !"".equalsIgnoreCase(ruleConfig.getContent().trim())) {
            str = ruleConfig.getContent() + str;
        }
        return str;
    }

    /**
     * Tim tat ca keyword nhap vao co trong file roi them noi dung can sua truoc keyword do
     */
    private static String ruleAddBeforeKeyword(String str, RuleConfig ruleConfig) {
        if (ruleConfig.getKeyword() != null && !"".equalsIgnoreCase(ruleConfig.getKeyword().trim()) && str.contains(ruleConfig.getKeyword())) {
            str = str.replace(ruleConfig.getKeyword(), ruleConfig.getContent() + ruleConfig.getKeyword());
        }
        return str;
    }

    /**
     * Tim tat ca keyword nhap vao co trong file roi them noi dung can sua sau keyword do
     */
    private static String ruleAddAfterKeyword(String str, RuleConfig ruleConfig) {
        if (ruleConfig.getKeyword() != null && !"".equalsIgnoreCase(ruleConfig.getKeyword().trim()) && str.contains(ruleConfig.getKeyword())) {
            str = str.replace(ruleConfig.getKeyword(),  ruleConfig.getKeyword() + ruleConfig.getContent());
        }
        return str;
    }

    /**
     * Tim tat ca keyword nhap vao co trong file roi thay keyword bang noi dung can sua
     */
    private static String ruleReplaceKeyword(String str, RuleConfig ruleConfig) {
        if (ruleConfig.getKeyword() != null && !"".equalsIgnoreCase(ruleConfig.getKeyword().trim()) && str.contains(ruleConfig.getKeyword())) {
            str = str.replace(ruleConfig.getKeyword(),  ruleConfig.getContent());
        }
        return str;
    }

    /**
     * Tim tat ca keyword nhap vao co trong file roi them noi dung can sua truoc dong co keyword do
     */
    private static String ruleAddBeforeLineKeyword(String str, RuleConfig ruleConfig) {
        if (ruleConfig.getKeyword() != null && !"".equalsIgnoreCase(ruleConfig.getKeyword().trim()) && str.contains(ruleConfig.getKeyword())) {
            String[] strs = str.split("\n", -1);
            StringBuilder result = new StringBuilder();
            for(String s : strs) {
                if(s.contains(ruleConfig.getKeyword())){
                    result.append(ruleConfig.getContent());
                    result.append("\n");
                }
                result.append(s);
                result.append('\n');
            }
            str = result.toString();
        }
        return str;
    }

    /**
     * Tim tat ca keyword nhap vao co trong file roi them noi dung can sua truoc dong co keyword do
     */
    private static String ruleAddAfterLineKeyword(String str, RuleConfig ruleConfig) {
        if (ruleConfig.getKeyword() != null && !"".equalsIgnoreCase(ruleConfig.getKeyword().trim()) && str.contains(ruleConfig.getKeyword())) {
            String[] strs = str.split("\n", -1);
            StringBuilder result = new StringBuilder();
            for(String s : strs) {
                result.append(s);
                result.append('\n');
                if(s.contains(ruleConfig.getKeyword())){
                    result.append(ruleConfig.getContent());
                    result.append("\n");
                }
            }
            str = result.toString();
        }
        return str;
    }

    /**
     * Tim tat ca keyword nhap vao co trong file roi xoa keyword do
     */
    private static String ruleDeleteKeyword(String str, RuleConfig ruleConfig) {
        if (ruleConfig.getKeyword() != null && !"".equalsIgnoreCase(ruleConfig.getKeyword().trim()) && str.contains(ruleConfig.getKeyword())) {
            str = str.replace(ruleConfig.getKeyword(),  "");
        }
        return str;
    }
}
