package com.flamexander.netty.example.common;

import java.util.List;

public class FileListRequest extends AbstractMessage {
    private String filename;
    private List<String> listFileNames;

    public String getFilename() {
        return filename;
    }

    public FileListRequest(String filename) {
        this.filename = filename;
    }

    public List<String> getListFileNames() {
        return listFileNames;
    }
}
