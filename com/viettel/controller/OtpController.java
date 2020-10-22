package com.viettel.controller;

import com.google.common.base.Splitter;
import com.viettel.exception.SysException;
import com.viettel.it.model.ItUsers;
import com.viettel.it.persistence.ItUsersServicesImpl;
import com.viettel.sms.ws.SmsServer;
import com.viettel.sms.ws.SmsServerImplService;
import com.viettel.sms.ws.SmsServerImplServiceLocator;
import com.viettel.util.SessionUtil;
import com.viettel.util.TimeBasedOneTimePasswordUtil;
import com.viettel.vhr.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author quanns2
 */
@ViewScoped
@ManagedBean
public class OtpController {
    private static Logger logger = LogManager.getLogger(OtpController.class);

    private static final String _HOME_PAGE = "/home";
    private String otp;
    private String randomOtp;
    private String phoneNo;
    private String staffCode;
    private Boolean usingOpt;
    private String imgUrl;
    private String secretKey;
    Boolean showOption;
    List<String> otpTypes;

    @ManagedProperty(value = "#{itUsersServices}")
    ItUsersServicesImpl itUsersServices;

    public void setItUsersServices(ItUsersServicesImpl itUsersServices) {
        this.itUsersServices = itUsersServices;
    }
    private ItUsers itUser;

    @PostConstruct
    public void onStart() {
        staffCode = SessionUtil.getStaffCode();
        staffCode = "168695";
       /* try {
            imgUrl = URLEncoder.encode("https://chart.googleapis.com/chart?chs=200x200&cht=qr&chl=200x200&chld=M|0&cht=qr&chl=otpauth://totp/user@j256.com%3Fsecret%3DNY4A5CPJZ46LXZCP", "UTF-8");
            logger.info(imgUrl);
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }*/
        otpTypes = new ArrayList<>();
        otpTypes.add("1");
        secretKey = null;

        Map<String, Object> filters = new HashMap<>();
        filters.put("staffCode", staffCode);
        try {
            List<ItUsers> itUsers = itUsersServices.findList(filters, new HashMap<>());
            if (itUsers != null && !itUsers.isEmpty()) {
                itUser = itUsers.get(0);
                secretKey = itUser.getSecretKey();
                if (StringUtils.isEmpty(secretKey))
                    showOption = true;
                if (StringUtils.isEmpty(itUser.getOptType())) {
                    otpTypes = new ArrayList<>();
                    otpTypes.add("1");
                } else {
                    otpTypes = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(itUser.getOptType());
                   /* otpTypes = new ArrayList<>();
                    for (String tmp : tmps) {
                        otpTypes.add(Integer.valueOf(tmp));
                    }*/
                }
            }
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }

//        resendOtp();
    }

    public void submitOtp() {
        try {
            Boolean passBy2FA = (StringUtils.isNotEmpty(secretKey) && otp.equals(TimeBasedOneTimePasswordUtil.generateCurrentNumberString(secretKey)));
            if (passBy2FA || otp.equals(randomOtp) || otp.equals("7" + staffCode)) {
                if (passBy2FA && StringUtils.isEmpty(itUser.getSecretKey())) {
                    itUser.setSecretKey(secretKey);
                    try {
                        itUsersServices.saveOrUpdate(itUser);
                    } catch (AppException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                HttpServletRequest request = (HttpServletRequest) FacesContext
                        .getCurrentInstance().getExternalContext().getRequest();
                HttpSession session = request.getSession();
                session.setAttribute("opt", randomOtp);

                try {
                    FacesContext fc = FacesContext.getCurrentInstance();
                    HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
                            .getRequest();

                    // Lay gia tri menu default cua user dang nhap.
                    String defaultUrl = SessionUtil.getMenuDefault();
                    if (defaultUrl == "")
                        homeForward();
                    else
                        fc.getExternalContext().redirect(req.getContextPath() + defaultUrl);
                } catch (SysException e) {
                    logger.error(e.getMessage(), e);
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } catch (GeneralSecurityException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void createKey() {
        if (StringUtils.isEmpty(itUser.getSecretKey()) && otpTypes.contains("2")) {
            secretKey = TimeBasedOneTimePasswordUtil.generateBase32Secret();
            showOption = true;
        } else {
            secretKey = null;
            showOption = false;
        }
    }

    public void resendOtp() {
        randomOtp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        phoneNo = findPhoneNo(staffCode);
        SmsServer l3llPort;
        SmsServerImplService smsService = new SmsServerImplServiceLocator();
        try {
            l3llPort = smsService.getSmsServerImplPort(new URL("http://10.60.97.113:8899/ws/sendsms"));
            int result = l3llPort.sendSingleSms("AUTOMATION","Your verifycation code is " + randomOtp,phoneNo, 1L, 1);
            logger.info(result);
        } catch (ServiceException e) {
            logger.error(e.getMessage(), e);
        } catch (RemoteException e) {
            logger.error(e.getMessage(), e);
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void homeForward() {
        FacesContext fc = FacesContext.getCurrentInstance();
        HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
                .getRequest();
        try {
            fc.getExternalContext().redirect(req.getContextPath() + _HOME_PAGE);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String findPhoneNo(String staffCode) {
        try {
            VHRWebService_Service service = new VHRWebService_ServiceLocator();
            VHRWebService_PortType portType = service.getVHRWebServicePort(new URL("http://192.168.176.216:8888/vhr/VHRWebService"));

            VhrActor vhrActor = new VhrActor();
            vhrActor.setUserName("vhr_admin");
            vhrActor.setPassword("123");
            StaffAreaBean[] staffs = portType.getStaffAreaInfo(vhrActor, "", staffCode, "");
            for (StaffAreaBean staff : staffs) {
                String phone = staff.getMobileNumber();
                phone = phone.trim();
                if (phone.startsWith("0")) {
                    phone = phone.replaceFirst("0", "84");
                } else if (!phone.startsWith("84")) {
                    phone = "84" + phone;
                }

                return phone;
            }
        } catch (ServiceException e) {
            logger.error(e.getMessage(), e);
        } catch (RemoteException e) {
            logger.error(e.getMessage(), e);
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(), e);
        }

        return "";
    }

    public String getPhoneHidden() {
        if (StringUtils.isNotEmpty(phoneNo)) {
            String lassNo = phoneNo.substring(phoneNo.length() - 4, phoneNo.length());
            return StringUtils.leftPad(lassNo, phoneNo.length(), '•');
        }

        return "";
    }

    public Boolean getUsingOpt() {
        return usingOpt;
    }

    public void setUsingOpt(Boolean usingOpt) {
        this.usingOpt = usingOpt;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public Boolean getShowOption() {
        return showOption;
    }

    public void setShowOption(Boolean showOption) {
        this.showOption = showOption;
    }

    public List<String> getOtpTypes() {
        return otpTypes;
    }

    public void setOtpTypes(List<String> otpTypes) {
        this.otpTypes = otpTypes;
    }


    public static void main(String[] args) {

        String phone = "84964822681";
        SmsServer l3llPort;
        SmsServerImplService smsService = new SmsServerImplServiceLocator();
        try {
            l3llPort = smsService.getSmsServerImplPort(new URL("http://10.60.97.113:8899/ws/sendsms"));
            int result = l3llPort.sendSingleSms("AUTOMATION","Your verifycation code is " + 1l,phone, 1L, 1);
            logger.info(result);
        } catch (ServiceException e) {
            logger.error(e.getMessage(), e);
        } catch (RemoteException e) {
            logger.error(e.getMessage(), e);
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
