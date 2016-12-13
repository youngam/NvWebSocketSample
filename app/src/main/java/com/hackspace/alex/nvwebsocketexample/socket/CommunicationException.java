package com.hackspace.alex.nvwebsocketexample.socket;

import java.io.IOException;

import android.util.Log;


public class CommunicationException extends IOException {
    private static final long serialVersionUID = 1L;
    public static enum CommunicationExceptionType {
        UNKNOWN("Unknown error"),//
        NO_INTERNET("No internet"),//
        SLOW_INTERNET("Slow internet"),//
        INCOMPATIBLE_FORMAT("Incompatible json format"),//
        SERVER_PROBLEM("Problem on server side."),//
        CONNECTION_ESTABLISHMENT("connection establishment problem"),//
        API_ERRORS("api errors");


        String discription;
        private CommunicationExceptionType(String discription) {
            this.discription = discription;
        }

        public String getDiscription() {
            return discription;
        }
    }
    private CommunicationExceptionType mCommunicationExceptionType;

    public CommunicationException() {
        this(CommunicationExceptionType.UNKNOWN, "");
    }

    public CommunicationException(String detailMessage) {
        this(CommunicationExceptionType.UNKNOWN, detailMessage);
    }

    public CommunicationException(CommunicationExceptionType communicationExceptionType, String msgDetails) {
        super(msgDetails);
        mCommunicationExceptionType = communicationExceptionType;
    }
    public CommunicationException(CommunicationExceptionType communicationExceptionType) {
        super();
        mCommunicationExceptionType = communicationExceptionType;
    }
    public CommunicationException(CommunicationExceptionType communicationExceptionType, String msgDetails, Exception e) {
        super(msgDetails, e);
        mCommunicationExceptionType = communicationExceptionType;
    }

    public CommunicationException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public CommunicationException(Throwable throwable) {
        super(throwable);
    }

    public void log() {
        if(mCommunicationExceptionType != null) {
            switch (mCommunicationExceptionType) {
            case NO_INTERNET:
            case SLOW_INTERNET:
            case CONNECTION_ESTABLISHMENT:
                Log.d("ExceptionLog", mCommunicationExceptionType + " " + toString());
                break;
            case API_ERRORS:
                Log.d("ExceptionLog", toString());
                break;
            default:
                Log.d("ExceptionLog", toString());
                break;
            }

        } else Log.d("ExceptionLog", toString());
    }

    public CommunicationExceptionType getType() {
        return mCommunicationExceptionType;
    }
}
