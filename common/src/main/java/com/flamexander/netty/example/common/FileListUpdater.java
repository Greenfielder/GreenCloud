package com.flamexander.netty.example.common;

import java.util.ArrayList;
import java.util.List;

public class FileListUpdater extends AbstractMessage {
    List<String> list = new ArrayList<>();

    public FileListUpdater(List<String> list) {
        this.list = list;
    }

    public List<String> getList() {
        return list;
    }
}
