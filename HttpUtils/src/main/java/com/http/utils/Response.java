package com.http.utils;

/**
 * <p>Http response, Including header information and response packets.</p>
 * Created in Oct 15, 2015 8:55:37 PM.
 *
 * @param <T> The handle data type, it should be with the {@link Request}, {@link OnResponseListener}.
 * @author Yan Zhenjie.
 */
public interface Response<T> {

    /**
     * Get the response code of handle.
     *
     * @return response code.
     */
    int responseCode();

    /**
     * Request is executed successfully.
     *
     * @return True: Succeed, false: failed.
     */
    boolean isSucceed();

    /**
     * Get handle results.
     *
     * @return {@link T}.
     */
    T get();

    /**
     * When the handle fail to getList the exception type.
     *
     * @return The exception.
     */
    String getError();

}