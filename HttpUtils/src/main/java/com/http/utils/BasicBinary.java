/*
 * Copyright 2015 Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.http.utils;

import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>
 * A basic implementation of Binary. All the methods are called in Son thread.
 * </p>
 * Created in Oct 17, 2015 12:40:54 PM.
 *
 * @author Yan Zhenjie.
 */
public abstract class BasicBinary implements Binary {
    public static final String HEAD_VALUE_CONTENT_TYPE_OCTET_STREAM = "application/octet-stream";

    private String fileName;

    private String mimeType;

    public BasicBinary(String fileName, String mimeType) {
        this.fileName = fileName;
        this.mimeType = mimeType;
    }


    @Override
    public final long getLength() {
        return getBinaryLength();
    }

    public abstract long getBinaryLength();

    protected abstract InputStream getInputStream() throws IOException;

    @Override
    public void onWriteBinary(OutputStream outputStream) throws IOException {
        InputStream inputStream;
        inputStream = getInputStream();
        if (inputStream == null) return;
        inputStream = IOUtils.toBufferedInputStream(inputStream);
        int len;
        byte[] buffer = new byte[4096];
        while ((len = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
    }

    @Override
    public String getFileName() {
        if (TextUtils.isEmpty(fileName))
            fileName = Long.toString(System.currentTimeMillis());
        return fileName;
    }

    @Override
    public String getMimeType() {
        String fileName = getFileName();
        if (TextUtils.isEmpty(mimeType) && !TextUtils.isEmpty(fileName)) {
            String extension = MimeTypeMap.getFileExtensionFromUrl(fileName);
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        if (TextUtils.isEmpty(mimeType))
            mimeType = HEAD_VALUE_CONTENT_TYPE_OCTET_STREAM;
        return mimeType;
    }


}
