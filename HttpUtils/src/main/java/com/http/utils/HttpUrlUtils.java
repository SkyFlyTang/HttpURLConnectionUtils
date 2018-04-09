package com.http.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpUrlUtils {

    private ExecutorService mExecutorService;

    HttpUrlUtils() {
        mExecutorService = Executors.newCachedThreadPool();
    }

    private static class INSTANCE {
        private static HttpUrlUtils instance = new HttpUrlUtils();
    }

    public static HttpUrlUtils INSTANCE() {
        return INSTANCE.instance;
    }

    public <T> void execute(int what, Request<T> request, OnResponseListener<T> listener) {
        mExecutorService.execute(new RequestTask<>(request, Messenger.newInstance(what, listener)));
    }

    public <T> Response<T> synExecute(Request<T> request) throws Exception {
        return request.connect();
    }

    private static class RequestTask<T> implements Runnable {

        private Request<T> request;
        private Messenger mMessenger;

        private RequestTask(Request<T> request, Messenger messenger) {
            this.request = request;
            this.mMessenger = messenger;
        }

        @Override
        public void run() {
            // handle.
            Response<T> response = null;
            try {
                response = request.connect();
            } catch (Exception e) {
                e.printStackTrace();
                response = new ResponseImp<>(null, 400, false, e.getMessage());
            }
            //noinspection unchecked
            mMessenger.response(response);
        }
    }

    public static StringRequest createStringRequest(String url, RequestMethod requestMethod) {
        return new StringRequest(url, requestMethod);
    }
}
