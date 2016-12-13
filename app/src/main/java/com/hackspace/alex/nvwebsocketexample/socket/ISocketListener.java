package com.hackspace.alex.nvwebsocketexample.socket;

/** Interface definition for a list of Socket callbacks */
public interface ISocketListener {
    /** Called when Text string arrives from Server*/
    void onText(String text);
    /** Called when Binary data arrives from Server*/
    void onBinary(byte[] bytes);
    /** Called when Socket connection is established between client and Server*/
    void onOpen();
    /** Called when Socket connection is closed between client and Server*/
    void onClose();
    /** Called when error occurred in Server-Client communication*/
    void onError(Exception e);
}
