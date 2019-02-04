package com.flamexander.netty.example.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileListRequest extends AbstractMessage {
    private List<FileRequest> listFiles;

    public List<FileRequest> getListFileNames() {
        return listFiles;
    }

    public FileListRequest(List<FileRequest> listFiles) {
        this.listFiles = listFiles;
    }

    public FileListRequest() throws IOException {
        this.listFiles = new ArrayList<>();
    }
}
