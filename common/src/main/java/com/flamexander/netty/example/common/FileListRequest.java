package com.flamexander.netty.example.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileListRequest extends AbstractMessage {
    private List<String> listFiles;

    public List<String> getListFileNames() {
        return listFiles;
    }

    public FileListRequest(List<String> listFiles) {
        this.listFiles = listFiles;
    }

    public FileListRequest() throws IOException {
        this.listFiles = new ArrayList<>();
    }
}
