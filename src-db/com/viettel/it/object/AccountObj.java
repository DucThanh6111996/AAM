/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.object;

/**
 *
 * @author hienhv4
 */
public class AccountObj {
    private String account;
    private String password;
    private String ip; //Quytv7 them ip tác động

    public AccountObj() {
    }

    public AccountObj(String account, String password) {
        this.account = account;
        this.password = password;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
