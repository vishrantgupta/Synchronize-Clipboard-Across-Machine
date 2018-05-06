/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vishrant.cbsync.http;

import com.vishrant.cbsync.common.CommonUtilities;
import javafx.concurrent.Task;

/**
 *
 * @author Vishrant
 */
public class HttpRequestService extends javafx.concurrent.Service<String> {

    private final String url;

    public HttpRequestService(String url) {
        this.url = url;
    }

    @Override
    protected Task<String> createTask() {
        return new Task<String>() {
            @Override
            protected String call() throws Exception {

                CommonUtilities utilities = new CommonUtilities();
                
                if (!utilities.isInternetReachable()) {
                    
//                    failed();
                    
                    updateMessage("Please check your internet connection");
                    updateProgress(10, 10);
                    return null;
                }

                String response = HTTPUtilities.sendGETRequest(url, 5000, 5000);

                updateMessage(response);
                updateProgress(10, 10);

                return null;
            }
        };
    }
}
