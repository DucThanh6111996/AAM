package com.viettel.it.util;

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

/**
 * @author Nguyễn Xuân Huy <huynx6@viettel.com.vn>
 * @sin Mar 10, 2016
 * @version 1.0
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
     * @return 
     * @see com.jcraft.jsch.UserInfo#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * @param message
     * @return 
     * @see com.jcraft.jsch.UserInfo#getPassphrase(String)
     */
    public String getPassphrase(String message) {
        return passphrase;
    }

    /**
     * @return 
     * @see com.jcraft.jsch.UserInfo#getPassword()
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * @param str
     * @return 
     * @see com.jcraft.jsch.UserInfo#prompt(String)
     */
    public boolean prompt(String str) {
        return false;
    }

    /**
     * @return 
     * @see com.jcraft.jsch.UserInfo#retry()
     */
    public boolean retry() {
        return false;
    }

    /**
     * Sets the name.
     *
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the passphrase.
     *
     * @param passphrase The passphrase to set
     */
    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    /**
     * Sets the password.
     *
     * @param password The password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets the trust.
     *
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
     *
     * @return String
     */
    @Override
    public String getPassphrase() {
        return passphrase;
    }

    /**
     * Returns the keyfile.
     *
     * @return String
     */
    public String getKeyfile() {
        return keyfile;
    }

    /**
     * Sets the keyfile.
     *
     * @param keyfile The keyfile to set
     */
    public void setKeyfile(String keyfile) {
        this.keyfile = keyfile;
    }

    /**
     * @param message
     * @return 
     * @see com.jcraft.jsch.UserInfo#promptPassphrase(String)
     */
    @Override
    public boolean promptPassphrase(String message) {
        return true;
    }

    /**
     * @param passwordPrompt
     * @return 
     * @see com.jcraft.jsch.UserInfo#promptPassword(String)
     */
    @Override
    public boolean promptPassword(String passwordPrompt) {
        //log(passwordPrompt, Project.MSG_DEBUG);
        if (firstTime) {
            firstTime = false;
            return true;
        }
        return firstTime;
    }

    /**
     * @param message
     * @return 
     * @see com.jcraft.jsch.UserInfo#promptYesNo(String)
     */
    @Override
    public boolean promptYesNo(String message) {
        //log(prompt, Project.MSG_DEBUG);
        return trustAllCertificates;
    }

    /**
     * @param message
     * @see com.jcraft.jsch.UserInfo#showMessage(String)
     */
    @Override
    public void showMessage(String message) {
        //log(message, Project.MSG_DEBUG);
    }

    @Override
    public String[] promptKeyboardInteractive(String destination,
            String name, String instruction, String[] prompt, boolean[] echo) {
        if ("Password: ".equals(prompt[0])) {
            String[] response = new String[1];
            response[0] = password;
            return response;
        }
        return null;
    }
}
