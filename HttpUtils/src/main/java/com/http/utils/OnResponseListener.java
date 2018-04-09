package com.http.utils;

public interface OnResponseListener<T> {
    /**
     * Server correct response to callback when an HTTP handle.
     *
     * @param what     the credit of the incoming handle is used to distinguish between multiple requests.
     * @param response successful callback.
     */
    void onSucceed(int what, Response<T> response);

    /**
     * When there was an error correction.
     *
     * @param what     the credit of the incoming handle is used to distinguish between multiple requests.
     * @param response failure callback.
     */
    void onFailed(int what, Response<T> response);

}
