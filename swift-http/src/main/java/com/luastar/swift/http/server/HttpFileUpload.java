package com.luastar.swift.http.server;

import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.multipart.FileUpload;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件上传使用内存和文件混合模式
 */
public class HttpFileUpload {

    private FileUpload fileUpload;

    public HttpFileUpload(FileUpload fileUpload) {
        this.fileUpload = fileUpload;
    }

    public FileUpload getFileUpload() {
        return fileUpload;
    }

    public long length() {
        return fileUpload.length();
    }

    public String getName() {
        return fileUpload.getName();
    }

    public String getFilename() {
        return fileUpload.getFilename();
    }

    public String getContentType() {
        return fileUpload.getContentType();
    }

    public String getContentTransferEncoding() {
        return fileUpload.getContentTransferEncoding();
    }

    public HttpFileUpload copy() {
        return new HttpFileUpload(fileUpload.copy());
    }

    public byte[] get() throws IOException {
        return fileUpload.get();
    }

    public InputStream getInputStream() throws IOException {
        if (fileUpload.isInMemory()) {
            return new ByteBufInputStream(fileUpload.content());
        } else {
            return new FileInputStream(fileUpload.getFile());
        }
    }

}
