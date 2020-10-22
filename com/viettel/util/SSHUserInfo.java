package com.viettel.util;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

/**
 * Created by quanns2
 */
public class SSHUserInfo implements UserInfo, UIKeyboardInteractive {

    private String name;
    private String password = null;
    private String keyfile;
    private String passphrase = null;
    private boolean firstTime = true;
    private boolean trustAllCertificates;

    public SSHUserInfo() {
        super();
        this.trustAllCertificates = false;
    }

    public SSHUserInfo(String password, boolean trustAllCertificates) {
        super();
        this.password = password;
        this.trustAllCertificates = trustAllCertificates;
    }

    /**
     * @see UserInfo#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * @see UserInfo#getPassphrase(String)
     */
    public String getPassphrase(String message) {
        return passphrase;
    }

    /**
     * @see UserInfo#getPassword()
     */
    public String getPassword() {
        return password;
    }

    /**
     * @see UserInfo#prompt(String)
     */
    public boolean prompt(String str) {
        return false;
    }

    /**
     * @see UserInfo#retry()
     */
    public boolean retry() {
        return false;
    }

    /**
     * Sets the name.
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the passphrase.
     * @param passphrase The passphrase to set
     */
    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    /**
     * Sets the password.
     * @param password The password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets the trust.
     * @param trust whether to trust or not.
     */
    public void setTrust(boolean trust) {
        this.trustAllCertificates = trust;
    }

    /**
     * @return whether to trust or not.
     */
    public boolean getTrust() {
        return this.trustAllCertificates;
    }

    /**
     * Returns the passphrase.
     * @return String
     */
    public String getPassphrase() {
        return passphrase;
    }

    /**
     * Returns the keyfile.
     * @return String
     */
    public String getKeyfile() {
        return keyfile;
    }

    /**
     * Sets the keyfile.
     * @param keyfile The keyfile to set
     */
    public void setKeyfile(String keyfile) {
        this.keyfile = keyfile;
    }

    /**
     * @see UserInfo#promptPassphrase(String)
     */
    public boolean promptPassphrase(String message) {
        return true;
    }

    /**
     * @see UserInfo#promptPassword(String)
     */
    public boolean promptPassword(String passwordPrompt) {
        //log(passwordPrompt, Project.MSG_DEBUG);
        if (firstTime) {
            firstTime = false;
            return true;
        }
        return firstTime;
    }

    /**
     * @see UserInfo#promptYesNo(String)
     */
    public boolean promptYesNo(String message) {
        //log(prompt, Project.MSG_DEBUG);
        return trustAllCertificates;
    }

    /**
     * @see UserInfo#showMessage(String)
     */
    public void showMessage(String message) {
        //log(message, Project.MSG_DEBUG);
    }
    
    public String[] promptKeyboardInteractive(String destination,
            String name, String instruction, String[] prompt, boolean[] echo) {
        if ("Password: ".equals(prompt[0])){
            String[] response = new String[1];
            response[0] = password;
            return response;
        }
        return null;
    }
}