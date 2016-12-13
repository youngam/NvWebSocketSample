package com.hackspace.alex.nvwebsocketexample;

import static com.hackspace.alex.nvwebsocketexample.utils.BinaryUtils.fromByteArray;
import static com.hackspace.alex.nvwebsocketexample.utils.BinaryUtils.intToBytes;
import static com.hackspace.alex.nvwebsocketexample.utils.BinaryUtils.toPrimitives;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hackspace.alex.nvwebsocketexample.socket.ISocketListener;
import com.hackspace.alex.nvwebsocketexample.socket.NvWebSocket;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG = "WebSocket";
    private NvWebSocket mWebSocket;
    private TextView mResponseTextView;
    private EditText mServerUrlEditText;
    private ImageView  mResponseImageView;
    private Button  mConnectButton;
    private final String DEFAULT_SERVER_URL = "192.168.43.44:8001";

    private static final String testRequest = "{\"method\":\"user.uploadMedia\",\"request\":{},\"ruid\":1}";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final EditText inputEditText = (EditText) findViewById(R.id.input_edit_text);
        mResponseTextView = (TextView) findViewById(R.id.server_response_text_view);
        final Button sendTextButton = (Button) findViewById(R.id.send_text_button);
        final Button sendBinaryButton = (Button) findViewById(R.id.send_binary_button);
        final ImageView requestImageView = (ImageView) findViewById(R.id.request_image_view);
        mResponseImageView = (ImageView) findViewById(R.id.response_image_view);
        mServerUrlEditText = (EditText) findViewById(R.id.server_ws_url_editText);
        mConnectButton = (Button) findViewById(R.id.connect_button);

        mServerUrlEditText.setText(DEFAULT_SERVER_URL);
        mConnectButton.setOnClickListener(v ->
                connectToServer(mServerUrlEditText.getText().toString().trim()));

        sendBinaryButton.setOnClickListener(view -> {
            BitmapDrawable drawable = (BitmapDrawable) requestImageView.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            sendBinaryToServer(getBytesFromBitmap(bitmap));
        });

        sendTextButton.setOnClickListener(view -> sendMessageToServer(inputEditText.getText().toString()));
    }

    private void initWebSocket(String url) {
        mWebSocket = new NvWebSocket("ws://" + url);
        mWebSocket.setSocketListener(new ISocketListener() {
            @Override
            public void onText(String text) {
                String serverResponse = "server response:" + text;
                Log.d(LOG_TAG, serverResponse);
                runOnUiThread(() -> mResponseTextView.setText(serverResponse));
            }

            @Override
            public void onBinary(byte[] bytes) {
                String bytesStringResp = Arrays.toString(bytes);
                Log.d(LOG_TAG, "onBinary " + bytesStringResp);
                Toast.makeText(MainActivity.this, "onBinary " , Toast.LENGTH_SHORT).show();
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                runOnUiThread(() -> mResponseImageView.setImageBitmap(bitmap));
            }

            @Override
            public void onOpen() {
                Log.d(LOG_TAG, "connection open");
            }

            @Override
            public void onClose() {
                Log.d(LOG_TAG, "Socket closed");
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                Log.d(LOG_TAG, "Error " + e);
            }
        });
    }

    private void sendMessageToServer(String message) {
        doAsync(() -> {
            mWebSocket.sendText(message);
            return true;
        });
    }

    private void sendBinaryToServer(byte[] photoBinary) {
        byte[] bytesRequest = makeBinaryRequest(photoBinary);
        Log.d(LOG_TAG, "sendBinary " + Arrays.toString(bytesRequest));

        doAsync(() -> {
            mWebSocket.sendBinary(bytesRequest);
            return true;
        });
    }

    private void parseBinaryRequest(byte[] bytesRequest) {
        List<Byte> bytesList = new ArrayList<>();

        for(byte b : bytesRequest) {
            bytesList.add(b);
        }

        //first two bytes - size of json content
        List<Byte> bytes = new ArrayList<>(bytesList.subList(0, 2));
        //remove this bytes after getting
        bytesList.subList(0, 2).clear();

        int jsonLength = fromByteArray(toPrimitives(bytes));
        Log.d(LOG_TAG, "parsed json length = " + jsonLength);

        List<Byte> jsonBinary = new ArrayList<>(bytesList.subList(0, jsonLength));
        //remove this bytes after getting
        bytesList.subList(0, jsonLength).clear();

        String jsonString = new String(toPrimitives(jsonBinary), StandardCharsets.UTF_8);
        Log.d(LOG_TAG, "parsed json = " + jsonString);


        byte[] mediaBinary = toPrimitives(bytesList);

        Bitmap bitmap = BitmapFactory.decodeByteArray(mediaBinary, 0, mediaBinary.length);
        mResponseImageView.setImageBitmap(bitmap);

    }

    private byte[] makeBinaryRequest(byte[] photoBinary) {
        List<Byte> listBytesRequest = new ArrayList<>();

        int lengthInBytes = testRequest.getBytes().length;

//        Log.d(LOG_TAG, "request json length = " + lengthInBytes);
        //first 2 bytes - json length  = n
        byte[] bytes = intToBytes(lengthInBytes);

        for (byte b : bytes) {
            listBytesRequest.add(b);
        }

//        Log.d(LOG_TAG, "request json = " + testRequest);
        //then n bytes - json content
        for (byte b : testRequest.getBytes()) {
            listBytesRequest.add(b);
        }

//        Log.d(LOG_TAG, Arrays.toString(toPrimitives(listBytesRequest)));
        //then n bytes - binary media content
        for(byte b : photoBinary) {
            listBytesRequest.add(b);
        }

        return toPrimitives(listBytesRequest.toArray(new Byte[listBytesRequest.size()]));
    }

    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return stream.toByteArray();
    }

    public void connectToServer(String serverUrl) {
        initWebSocket(serverUrl);
        doAsync(() -> {
            mWebSocket.connect();
            return true;
        });
    }

    public void doAsync(Callable<Boolean> callable) {
        Observable.fromCallable(callable)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {}, error -> {

                    Toast.makeText(MainActivity.this, "Error " + error.getMessage()+ " during connection", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();

                });
    }
}
