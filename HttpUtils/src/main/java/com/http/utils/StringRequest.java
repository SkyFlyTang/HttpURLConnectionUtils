package com.http.utils;

import java.io.InputStream;

public class StringRequest extends Request<String> {
    private static final String LINE_SEP = System.getProperty("line.separator");

    public StringRequest(String url, RequestMethod requestMethod) {
        super(url, requestMethod);
    }

    @Override
    public String parseResponse(InputStream responseBody) throws Exception {
        return IOUtils.toString(responseBody);
    }
}
