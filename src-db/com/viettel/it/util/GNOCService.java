package com.viettel.it.util;

//import com.sun.xml.internal.ws.client.BindingProviderProperties;

import com.viettel.gnoc.cr.service.CrForOtherSystemService;
import com.viettel.gnoc.cr.service.CrForOtherSystemServiceImplService;
import com.viettel.gnoc.cr.service.CrOutputForQLTNDTO;
import com.viettel.util.PasswordEncoder;
import com.viettel.util.SessionUtil;
import com.viettel.util.SessionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

//import javax.xml.ws.BindingProvider;

public class GNOCService {

    public SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    protected final static Logger LOGGER = LoggerFactory.getLogger(GNOCService.class);
    protected static CrForOtherSystemService service;
    static ResourceBundle bundle = ResourceBundle.getBundle("config");
    static final int TIME_OUT = 30000;

    enum StateGnoc {
        DRAFT(0), OPEN(1), QUEUE(2), COORDINATED(3), EVALUATED(4), APPROVED(5), ACCEPTED(6), RESOLVED(7), INCOMPLETED(8), CLOSED(9);
        public Integer value;

        private StateGnoc(Integer value) {
            this.value = value;
        }

    }

    public static Boolean isCanUpdateDT(String crNumber) {
        try {
            CrOutputForQLTNDTO cr = getCrByCode(crNumber);
            if (cr.getState() == null)
                return false;
            Integer state = Integer.valueOf(cr.getState());
            return state < StateGnoc.EVALUATED.value;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;

    }


    public static Boolean isCanExecute(String crNumber) {
        try {
            CrOutputForQLTNDTO cr = getCrByCode(crNumber);
            if (cr.getState() == null)
                return false;

            if (!cr.getUserExecute().equalsIgnoreCase(SessionWrapper.getCurrentUsername()))
                return null;
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date startTime = df.parse(cr.getImpactStartTime());
            Date endTime = df.parse(cr.getImpactEndTime());
            Date curTime = new Date();

            LOGGER.info("CR: " + crNumber + ". START TIME: " + startTime.toString() + ". END TIME: " + endTime.toString()
                    + ". CUR TIME: " + curTime.toString());

            if (curTime.after(startTime) && curTime.before(endTime)) {
                Integer state = Integer.valueOf(cr.getState());
                LOGGER.info("state: " + state + "/getUserExecute: " + cr.getUserExecute() + "---" + SessionUtil.getCurrentUsername());
                LOGGER.info("state.equals(StateGnoc.ACCEPTED.value): " + state.equals(StateGnoc.ACCEPTED.value));
                LOGGER.info("cr.getUserExecute().equalsIgnoreCase(SessionUtil.getCurrentUsername()): " + cr.getUserExecute().equalsIgnoreCase(SessionUtil.getCurrentUsername()));
                return (state.equals(StateGnoc.ACCEPTED.value) && cr.getUserExecute().equalsIgnoreCase(SessionUtil.getCurrentUsername()));
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;

    }

    public static CrOutputForQLTNDTO getCrByCode(String crNumber) {
        try {
            service = new CrForOtherSystemServiceImplService(new URL(bundle.getString("ws_gnoc_new"))).getCrForOtherSystemServiceImplPort();
            CrOutputForQLTNDTO cr = service.getCrByCode(bundle.getString("ws_gnoc_user"), PasswordEncoder.decrypt(bundle.getString("ws_gnoc_pass")), crNumber);
            return cr;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    public static void main(String[] args) {
        GNOCService gnocService = new GNOCService();
        boolean rs = gnocService.isCanExecute("CR_AUTO");
        System.out.println(rs);
    }
}
