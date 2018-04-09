package com.http.utils;

/**
 * <p>Http response, Including header information and response packets.</p>
 * Created in Oct 15, 2015 8:55:37 PM.
 *
 * @param <T> The handle data type, it should be with the {@link Request}, {@link OnResponseListener}.
 * @author Yan Zhenjie.
 */
public class ResponseImp<T> implements Response<T> {
    private T t;
    private int responseCode;
    private boolean isSucceed;
    private String error;


    public ResponseImp(T t, int responseCode, boolean isSucceed, String error) {
        this.t = t;
        this.responseCode = responseCode;
        this.isSucceed = isSucceed;
        this.error = error;
    }

    @Override
    public int responseCode() {
        return responseCode;
    }

    @Override
    public boolean isSucceed() {
        return isSucceed;
    }

    @Override
    public T get() {
        return t;
    }

    @Override
    public String getError() {
        return error;
    }


}