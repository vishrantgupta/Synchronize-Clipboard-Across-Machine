/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vishrant.cbsync.common;

/**
 *
 * @author Vishrant
 */
public class AppConstant {

    public static final String APP_URL = "https://vishrant-clipboard-sync.herokuapp.com";
    public static final String REQUEST_TOKEN = APP_URL + "/requestToken";

    public static final int DEFAULT_SYNC_TIME = 500; // 500 milliseconds
    public static final int LOCK_TIMEOUT = 1000; // 1000 milliseconds
    public static final int RESPONSE_TIMEOUT = 10000;

}
