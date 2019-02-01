package com.flamexander.netty.example.server;

import com.flamexander.netty.example.common.FileListRequest;
import com.flamexander.netty.example.common.FileMessage;
import com.flamexander.netty.example.common.FileRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class MainHandler extends ChannelInboundHandlerAdapter {

    static ObservableList<String> serverList = FXCollections.observableList(new ArrayList<>());

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected...");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null) {
                return;
            }
            if (msg instanceof FileRequest) {
                FileRequest fr = (FileRequest) msg;
                if (Files.exists(Paths.get("server_storage/" + fr.getFilename()))) {
                    FileMessage fm = new FileMessage(Paths.get("server_storage/" + fr.getFilename()));
                    ctx.writeAndFlush(fm);
                }
            }

            if (msg instanceof FileMessage) {
                System.out.println("Client send: " + ((FileMessage) msg).getFilename());
                FileMessage fm = (FileMessage) msg;
                Files.write(Paths.get("server_storage/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);

            }
            if (msg instanceof FileListRequest) {
                FileListRequest flr = (FileListRequest) msg;
                Platform.runLater(() -> {
                    serverList.clear();
                    serverList.addAll(flr.getListFileNames());
                    ctx.writeAndFlush(flr);
                });

            } else {
                System.out.printf("Server received wrong object!");
                return;
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}