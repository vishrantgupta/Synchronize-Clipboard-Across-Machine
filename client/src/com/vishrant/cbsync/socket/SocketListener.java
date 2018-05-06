/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vishrant.cbsync.socket;

/**
 *
 * @author Vishrant
 */
public interface SocketListener {

    void connected();

    void disconnected();

    void connecting();

    void update(SocketConnectionStatus status);

    void update(Object... os);
    
    void message(Object... os);

}
