package com.hackspace.alex.nvwebsocketexample.socket;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

public class NvWebSocket implements ISocket {
    private static final int CONNECTION_TIMEOUT = 3000;

    private WebSocket mWebSocketClient;
    private String mUrl;
    private ISocketListener mSocketListener;
    private Object mLock = new Object();
    private IHttpHeader mIHttpHeader;

    public static interface IHttpHeader {
        Map<String, String> getHeader();
    }

    public NvWebSocket(String url) {
        this(url, null);
    }


    public NvWebSocket(String url, IHttpHeader httpHeader) {
        mUrl = url;
        mIHttpHeader = httpHeader;
    }

    @Override
    public void disconnect() {
        synchronized (mLock) {
            if (isConnected()) mWebSocketClient.disconnect();
        }
    }

    public void setSocketListener(ISocketListener socketListener) {
        mSocketListener = socketListener;
    }

    @Override
    public void sendText(String text) throws CommunicationException {
        synchronized (mLock) {
            if (text.length() < 30 * 1000) { // less then 30Kb

            } else
                Log.d("WebSocket", "RequestApi :" + text.substring(0, 150) + "..." + text.substring(text.length() - 150)); // prevent out of memory
            if (isConnected()) mWebSocketClient.sendText(text);
            else
                throw new CommunicationException(CommunicationException.CommunicationExceptionType.CONNECTION_ESTABLISHMENT);
        }
    }

    @Override
    public void sendBinary(byte[] bytes) throws CommunicationException {
        synchronized (mLock) {
            if (isConnected()) mWebSocketClient.sendBinary(bytes);
            else
                throw new CommunicationException(CommunicationException.CommunicationExceptionType.CONNECTION_ESTABLISHMENT);
        }
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public String getUrl() {
        return mUrl;
    }


    private String getAuthUrl() {
        StringBuilder sb = new StringBuilder(mUrl);
        if (mIHttpHeader != null && mIHttpHeader.getHeader() != null && !mIHttpHeader.getHeader().isEmpty()) {
            sb.append("?");
            Iterator<Map.Entry<String, String>> iter = mIHttpHeader.getHeader().entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> entry = iter.next();
                sb.append(entry.getKey()).append("=").append(entry.getValue());
                if (iter.hasNext()) sb.append("&");
            }
        }
        return sb.toString();
    }

    @Override
    public void connect() throws CommunicationException {
        synchronized (mLock) {
            boolean needToCreateSSLFactory = mUrl.toLowerCase().startsWith("https");
            try {
                mWebSocketClient = NvWebSocketFactory.createWebSocket(getAuthUrl(), mSocketListener);
            } catch (IOException e) {
                throw new CommunicationException("Exception when creating WebSocketClient", e);
            }
            try {
                mWebSocketClient.connect();
            } catch (WebSocketException e) {
                throw new CommunicationException(e);
            }
        }
    }

    public static class NvWebSocketFactory {

        public static WebSocket createWebSocket(String url, ISocketListener socketListener) throws IOException {
            WebSocket ws = new WebSocketFactory().setConnectionTimeout(CONNECTION_TIMEOUT).createSocket(url);
            initListener(ws, socketListener);
            return ws;
        }

        private static void initListener(WebSocket ws, final ISocketListener socketListener) {
            ws.addListener(new WebSocketAdapter() {
                @Override
                public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                    socketListener.onOpen();
                }

                @Override
                public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                    socketListener.onClose();
                }

                @Override
                public void onTextMessage(WebSocket websocket, String text) throws Exception {
                    Log.d("WebSocket", "ResponseApi: " + text);
                    socketListener.onText(text);
                }

                @Override
                public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
                    socketListener.onError(cause);
                }

                @Override
                public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
                    socketListener.onBinary(binary);
                }
            });
        }
    }
}
