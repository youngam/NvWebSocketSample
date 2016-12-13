package com.hackspace.alex.nvwebsocketexample.socket;

public interface ISocket {
    String getUrl();
    void connect() throws CommunicationException;
    void disconnect() throws CommunicationException;
    void sendText(String text) throws CommunicationException;
    void sendBinary(byte[] bytes) throws CommunicationException;
    boolean isConnected();
}
