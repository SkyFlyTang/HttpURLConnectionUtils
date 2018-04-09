package com.http.utils;

import android.os.Build;
import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public abstract class Request<T> {

    private static final String BOUNDARY = java.util.UUID.randomUUID().toString();
    private static final String TWO_HYPHENS = "--";
    private static final String LINE_END = "\r\n";

    private String url;
    private Proxy proxy;
    private int connectTimeout = 10 * 1000;
    private int readTimeout = 10 * 1000;
    private RequestMethod requestMethod;
    private Map<String, String> headers;
    private boolean isCaches = false;
    private Map<String, String> formsBody;
    private Map<String, Binary> binaryFormsBody;


    public static final String HEAD_KEY_CONTENT_ENCODING = "Content-Encoding";
    private String mParamEncoding;

    public Request(String url, RequestMethod requestMethod) {
        this.url = url;
        this.requestMethod = requestMethod;
    }

    private String getUrl() {
        return url;
    }

    private Proxy getProxy() {
        return proxy;
    }

    private int getConnectTimeout() {
        return connectTimeout;
    }

    private int getReadTimeout() {
        return readTimeout;
    }

    private RequestMethod getRequestMethod() {
        return requestMethod;
    }

    private Map<String, String> getHeaders() {
        return headers;
    }

    private boolean isCaches() {
        return isCaches;
    }

    private Map<String, String> getFormsBody() {
        return formsBody;
    }

    private Map<String, Binary> getBinaryFormsBody() {
        return binaryFormsBody;
    }


    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    public Request<T> addBinaryFormsBody(String key, Binary binary) {
        if (binaryFormsBody == null) {
            binaryFormsBody = new HashMap<>();
        }
        binaryFormsBody.put(key, binary);
        return this;
    }

    public Request<T> addHeader(String key, String value) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(key, value);
        return this;
    }


    public void setCaches(boolean caches) {
        isCaches = caches;
    }

    public Request<T> addFromBoy(String key, String value) {
        if (formsBody == null) {
            formsBody = new HashMap<>();
        }
        formsBody.put(key, value);
        return this;
    }

    public void setParamsEncoding(String paramsEncoding) {
        mParamEncoding = paramsEncoding;
    }


    public Response<T> connect() throws Exception {
        URL url = new URL(getUrl());
        HttpURLConnection connection;
        Proxy proxy = getProxy();
        Response<T> response;
        if (proxy == null)
            connection = (HttpURLConnection) url.openConnection();
        else
            connection = (HttpURLConnection) url.openConnection(proxy);
        connection.setConnectTimeout(getConnectTimeout());
        connection.setReadTimeout(getReadTimeout());
        connection.setInstanceFollowRedirects(false);
        // Base attribute
        String method = getRequestMethod().getValue();
        connection.setRequestMethod(method);

        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Charset", "UTF-8");
        connection.setRequestProperty("Content-Type", "multipart/form-data; BOUNDARY=" + BOUNDARY);

        boolean isAllowBody = isAllowBody(getRequestMethod());
        connection.setDoInput(true);
        connection.setDoOutput(isAllowBody);
        if (isAllowBody) connection.setUseCaches(false);

        // Adds all handle header to connection.
        Map<String, String> requestHeaders = getHeaders();
        if (requestHeaders != null) {
            for (Map.Entry<String, String> headerEntry : requestHeaders.entrySet()) {
                String headKey = headerEntry.getKey();
                String headValue = headerEntry.getValue();
                connection.setRequestProperty(headKey, headValue);
            }
        }
        // 5. Connect
        connection.connect(); //必须先getOutputStream，才能getResponseCode，否则getOutputStream会报错
        if (isAllowBody) {
            OutputStream os = connection.getOutputStream();
            onWriteRequestBody(os);
        }
        int responseCode = connection.getResponseCode();
        Map<String, List<String>> headers = connection.getHeaderFields();
        List<String> headerList = headers.get(HEAD_KEY_CONTENT_ENCODING);
        String contentEncoding = null;
        if (headerList != null && headerList.size() > 0) {
            contentEncoding = headerList.get(0);
        }
        InputStream is;
        if (responseCode >= 400) {
            is = gzipInputStream(contentEncoding, connection.getErrorStream());
            String error = IOUtils.toString(is);
            response = new ResponseImp<>(null, responseCode, false, error);
        } else {
            is = gzipInputStream(contentEncoding, connection.getInputStream());
            T t = parseResponse(is);
            response = new ResponseImp<>(t, responseCode, true, null);
        }
        IOUtils.closeQuietly(is);
        return response;
    }

    private boolean isAllowBody(RequestMethod requestMethod) {
        boolean allowRequestBody = requestMethod.allowRequestBody();
        // Fix Android bug.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return allowRequestBody && requestMethod != RequestMethod.DELETE;
        return allowRequestBody;
    }


    /**
     * Pressure http input stream.
     *
     * @param contentEncoding {@value Request#HEAD_KEY_CONTENT_ENCODING} value of the HTTP response headers.
     * @param inputStream     {@link InputStream}.
     * @return It can directly read normal data flow
     * @throws IOException if an {@code IOException} occurs.
     */
    private InputStream gzipInputStream(String contentEncoding, InputStream inputStream) throws
            IOException {
        if (isGzipContent(contentEncoding)) {
            inputStream = new GZIPInputStream(inputStream);
        }
        return inputStream;
    }

    private boolean isGzipContent(String contentEncoding) {
        return contentEncoding != null && contentEncoding.contains("gzip");
    }

    private void onWriteRequestBody(OutputStream writer) throws IOException {
        if (getFormsBody() != null) {
            writeParamStreamData(writer);
        } else if (getBinaryFormsBody() != null) {
            writeBinaryStreamData(writer);
        }
        IOUtils.closeQuietly(writer);

    }

    /**
     * Write params.
     */
    private void writeParamStreamData(OutputStream writer) throws IOException {
        StringBuilder paramBuilder = buildCommonParams(getFormsBody(), getParamsEncoding());
        if (paramBuilder.length() > 0) {
            String params = paramBuilder.toString();
            IOUtils.write(params.getBytes(), writer);
        }
    }

    private void writeBinaryStreamData(OutputStream writer) throws IOException {
        for (Map.Entry<String, Binary> stringBinaryEntry : getBinaryFormsBody().entrySet()) {
            writer.write(getFileParamsString(stringBinaryEntry.getValue(), stringBinaryEntry.getKey()).getBytes());
            stringBinaryEntry.getValue().onWriteBinary(writer);
            byte[] endData = (LINE_END + TWO_HYPHENS + BOUNDARY + TWO_HYPHENS + LINE_END).getBytes();//写结束标记位
            writer.write(endData);
            writer.flush();
        }
    }

    /**
     * 上传文件时得到一定格式的拼接字符串
     */
    private String getFileParamsString(Binary binary, String key) {
        StringBuffer strBuf = new StringBuffer();
        strBuf.append(LINE_END);
        strBuf.append(TWO_HYPHENS);
        strBuf.append(BOUNDARY);
        strBuf.append(LINE_END);
        strBuf.append("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + binary.getFileName() + "\"");
        strBuf.append(LINE_END);
        strBuf.append("Content-Type: " + binary.getMimeType());
        strBuf.append(LINE_END);
        strBuf.append("Content-Lenght: " + binary.getLength());
        strBuf.append(LINE_END);
        strBuf.append(LINE_END);
        return strBuf.toString();
    }

    private StringBuilder buildCommonParams(Map<String, String> paramMap, String encodeCharset) {
        StringBuilder paramBuilder = new StringBuilder();
        Set<String> keySet = paramMap.keySet();
        for (String key : keySet) {
            if (TextUtils.isEmpty(key)) continue;
            paramBuilder.append("&").append(key).append("=");
            String value = paramMap.get(key);
            if (value != null) {
                try {
                    paramBuilder.append(URLEncoder.encode(value, encodeCharset));
                } catch (UnsupportedEncodingException e) {
                    paramBuilder.append(value);
                }

            }
        }
        if (paramBuilder.length() > 0)
            paramBuilder.deleteCharAt(0);
        return paramBuilder;
    }


    /**
     * Get the params encoding.
     *
     * @return such as {@code utf-8}, default is {@code utf-8}.
     * @see #setParamsEncoding(String)
     */
    public String getParamsEncoding() {
        if (TextUtils.isEmpty(mParamEncoding))
            mParamEncoding = "utf-8";
        return mParamEncoding;
    }

    public abstract T parseResponse(InputStream responseBody) throws Exception;

}
