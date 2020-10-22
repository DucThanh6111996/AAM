package com.viettel.it.util;

import com.viettel.it.model.LogActionBO;
import com.viettel.it.persistence.common.LogActionServiceImpl;
import com.viettel.util.SessionUtil;
import org.apache.log4j.Logger;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by hanhnv68 on 8/28/2017.
 */
public class LogUtils {
    private static final Logger logger = Logger.getLogger("log.action");
    public static final String appCode = "TDTT";

    public enum ResultCode {

        SUCCESS,
        FAIL
    }

    public enum ErrorLevel {

        LOW,
        NORMAL,
        HIGHT
    }

    public enum ActionType {

        APPROVE,
        IMPACT,
        CREATE,
        UPDATE,
        DELETE,
        VIEW
    }

    /*
     * Log đăng nhập (thành công và không thành công)
     1. Thời gian
     2. Nguồn (ID/IP) đăng nhập
     3. Nội dung
     4. Kết quả
     */
    public static void log1(Date date, String ip, String content, ResultCode result) {
        try {
            if (logger != null) {
                StringBuilder info = new StringBuilder();

                info.append(date).append("||");
                info.append(ip).append("||");
                info.append(content).append("||");
                info.append(result);
                if (result.equals(ResultCode.SUCCESS)) {
                    logger.info(info.toString().replace("null", "N/A"));
                } else {
                    logger.error(info.toString().replace("null", "N/A"));
                }
            }
        } catch (Exception e) {
            if (logger != null) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /*
     * Log lỗi của ứng dụng (trong quá trình thực thi ứng dụng để để hỗ trợ sửa lỗi)
     1. Thời gian
     2. Nguồn bị lỗi.
     3. Mã (ID) lỗi: mã lỗi gắn với loại lỗi để dễ kiểm soát) (VD: SQL-00001)
     4. Nội dung: mô tả lỗi, nguyên nhân lỗi: Chỉ nguyên nhân gây lỗi (nếu có), cách khắc phục lỗi
     5. Mức lỗi
     */
    public static void log9(Date date, String sourceError, String ErrorCode, String content, ErrorLevel errorLevel, Throwable exception) {
        try {
            if (logger != null) {
                StringBuilder info = new StringBuilder();

                info.append(date).append("|");
                info.append(sourceError).append("|");
                info.append(ErrorCode).append("|");
                info.append(content).append("|");
                info.append(errorLevel);
                if (null != exception) {
                    logger.info(info.toString().replace("null", "N/A"), exception);
                } else {
                    logger.info(info.toString().replace("null", "N/A"));
                }
            }
        } catch (Exception e) {
            if (logger != null) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /*
     * Log chức năng, nghiệp vụ: ghi log tác động, thay đổi, giao dịch, xem, báo cáo liên quan đến tài khoản, thông tin khách hàng, thông tin nhân sự, đầu tư, tác động đến phần tử mạng lưới, chi thức, tiền, tài sản, hàng hóa,..
     1. Mã ứng dụng
     2. Thời gian bắt đầu
     3. Thời gian kết thúc
     4. Địa chỉ IP máy tác động
     5. Mã chức năng
     6. Loại tác động: thêm, cập nhật, sửa, xóa, view
     7. Mô tả tác động
     */
    public static void log10(
            String appCode,
            Date startTime,
            Date endTime,
            String ip,
            String functionCode,
            ActionType actionType,
            String content,
            Throwable exception) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:S");
        String dateStartStr = formatter.format(startTime);
        String dateEndStr = formatter.format(endTime);
        try {
            if (logger != null) {
                StringBuilder info = new StringBuilder();

                info.append(appCode).append("|");
                info.append(dateStartStr).append("|");
                info.append(dateEndStr).append("|");
                info.append(ip).append("|");
                info.append(functionCode).append("|");
                info.append(actionType).append("|");
                info.append(content);
                if (null != exception) {
                    logger.info(info.toString().replace("null", "N/A"), exception);
                } else {
                    logger.info(info.toString().replace("null", "N/A"));
                }
            }
        } catch (Exception e) {
            if (logger != null) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * @param appCode      -- Ma ung dung
     * @param startTime    -- Thoi gian bat dau
     * @param endTime      -- Thoi gian xu ly xong action
     * @param user         -- Nguoi dung tac dong
     * @param ip           -- Ip nguoi dung tac dong
     * @param linkWeb      -- Link web request
     * @param className    -- Class xu ly su kien
     * @param actionMethod -- Ham xu ly su kien
     * @param actionType   -- Loai hanh dong
     * @param content      -- Mo ta chi tiet hanh dong
     * @param requestId    -- Request session id
     */
    public static void logAction(
            String appCode,
            Date startTime,
            Date endTime,
            String user,
            String ip,
            String linkWeb,
            String className,
            String actionMethod,
            ActionType actionType,
            String content,
            String requestId) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:S");
        String dateStartStr = formatter.format(startTime);
        String dateEndStr = formatter.format(endTime);
        try {
            StringBuilder info = new StringBuilder();

            info.append(appCode).append("|");
            info.append(dateStartStr).append("|");
            info.append(dateEndStr).append("|");
            info.append(user).append("|");
            info.append(ip).append("|");
            info.append(linkWeb).append("|");
            info.append(className).append("|");
            info.append(actionMethod).append("|");
            info.append(actionType).append("|");
            info.append(content).append("|");
            info.append(requestId);

            logger.info(info.toString().replace("null", "N/A"));
            //20181606_tudn_start them ghi log
            //write db
            LogActionBO logBO = new LogActionBO();
            logBO.setAppCode(appCode);
            logBO.setStartTime(startTime);
            logBO.setEndTime(endTime);
            logBO.setUser(user);
            logBO.setIp(ip);
            logBO.setLinkWeb(linkWeb);
            logBO.setClassName(className);
            logBO.setActionMethod(actionMethod);
            logBO.setActionType(actionType.toString());
            logBO.setContent(content);
            logBO.setRequestId(requestId);
            logBO.setCreateDate(new Date());
            Long id = new LogActionServiceImpl().save(logBO);
            //20181606_tudn_end them ghi log
        } catch (Exception e) {
            if (logger != null) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Ham lay thong tin request session id
     *
     * @return
     */
    public static String getRequestSessionId() {
        try {
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            if (request != null) {
                return request.getRequestedSessionId();
            } else {
                return "N/A";
            }
        } catch (Exception ex) {
            logger.debug(ex.getMessage(), ex);
        }
        return "N/A";
//        return "test";
    }

    /**
     * Ham lay thong tin IP nguoi dung
     *
     * @return
     */
    public static String getRemoteIpClient() {
        try {
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            if (request != null) {
                return request.getRemoteAddr();
            } else {
                return "N/A";
            }
        } catch (Exception ex) {
            logger.debug(ex.getMessage(), ex);
        }
        return "N/A";
    }

    /**
     * Ham lay thong tin IP nguoi dung
     *
     * @return
     */
    public String getRemoteIpClient1() {
//        return "test";
//        System.out.println(LogUtils.class.getMethods()[0].getName());
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        if (request != null) {
            return request.getRemoteAddr();
        } else {
            return "N/A";
        }
    }

    /**
     * Ham lay thong tin LinkWeb
     *
     * @return
     */
    public static String getUrl() {
//        return "test";
        try {
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            if (request != null) {
                return request.getScheme() + "://" +   // "http" + "://
                        request.getServerName() +       // "myhost"
                        ":" +                           // ":"
                        request.getServerPort() +       // "8080"
                        request.getRequestURI();      // "lastname=Fox&age=30"
            } else {
                return "N/A";
            }
        } catch (Exception ex) {
            logger.debug(ex.getMessage(), ex);
        }
        return "N/A";
    }

    //    public static void main(String args[]) {
//        try {
//            String data = "export tdht/dfkghf from ata";
//            System.out.println(data.replaceFirst("export\\s+(.+\\/)(\\S)+", "export xxx/xxx"));
//            LogUtils t = new LogUtils();
//            System.out.println(t.getRemoteIpClient1());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
    public static void writelog(Date StartTime,
                                String className, String actionMethod, String actionType,
                                String content) {
        try {
            if (logger != null) {
                StringBuilder info = new StringBuilder();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                info.append(appCode).append("|");
                info.append(dateFormat.format(StartTime)).append("|");
                info.append(dateFormat.format(new Date())).append("|");
                info.append(SessionUtil.getCurrentUsername()).append("|");
                info.append(getRemoteIpClient()).append("|");
                info.append(getUrl()).append("|");
                info.append(className).append("|");
                info.append(actionMethod).append("|");
                info.append(actionType).append("|");
                info.append(content).append("|");
                try {
                    info.append(SessionUtil.getCurrentSession().getId()).append("|");
                } catch (Exception e) {
                    if (logger != null) {
                        logger.debug(e.getMessage(), e);
                    }
                }
                if (logger != null) {
                    logger.info(info.toString().replace("null", "N/A").replace("Null", "N/A"));
                }
            }
        } catch (Exception e) {
            if (logger != null) {
                logger.debug(e.getMessage(), e);
            }
        }
    }

    public static String addContent(String content, String addCont) {
        try {
            if ("".equals(content)) {
                return addCont;
            } else {
                return content + ", " + (addCont == null ? "null" : addCont);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return content + ", " + ex.getMessage();
        }
    }

    //20181023_tudn_start load pass security
    public static void writeLogSecurity(String appCode,Date startTime,Date endTime,String user,String ip,String linkWeb,
            String className,String actionMethod,String actionType,String content,String requestId,String detailResult, String result
            ) {
        try {
            LogActionBO logBO = new LogActionBO();
            logBO.setAppCode(appCode);
            logBO.setStartTime(startTime);
            logBO.setEndTime(endTime);
            logBO.setUser(user);
            logBO.setIp(ip);
            logBO.setLinkWeb(linkWeb);
            logBO.setClassName(className);
            logBO.setActionMethod(actionMethod);
            logBO.setActionType(actionType.toString());
            logBO.setContent(content);
            logBO.setRequestId(requestId);
            logBO.setCreateDate(new Date());
            logBO.setDetailResult(detailResult);
            logBO.setResult(result);
            Long id = new LogActionServiceImpl().save(logBO);
        } catch (Exception e) {
            if (logger != null) {
                logger.error(e.getMessage(), e);
            }
        }
    }
    //20181023_tudn_end load pass security
}
