package com.flamexander.netty.example.server;

import com.flamexander.netty.example.common.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class MainHandler extends ChannelInboundHandlerAdapter {

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
                updater(ctx);

            }
            if (msg instanceof FileListRequest) {
                List<String> serverFiles = new ArrayList<>();
                FileListRequest answer = new FileListRequest(serverFiles);
                Files.list(Paths.get("server_storage/" + "/"))
                        .forEach(p -> answer.getListFileNames().add(p.getFileName().toString()));
                ctx.writeAndFlush(answer);
            }
            if (msg instanceof FileDelete) {
                FileDelete fileDelete = (FileDelete) msg;
                Files.delete(Paths.get("server_storage/" + fileDelete.getFilename()));
                System.out.println("Фаил " + fileDelete.getFilename() + " удален");
                updater(ctx);
            }
            if (msg instanceof FileListUpdater) {
                updater(ctx);
            }
            else {
                System.out.printf("Server received wrong object!");
                return;
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void updater(ChannelHandlerContext ctx) {
        FileListUpdater fileListUpdater = new FileListUpdater(getServerList());
        ctx.writeAndFlush(fileListUpdater);
    }

    public List<String> getServerList(){
        List<String> fileList = new ArrayList<>();
        try {
            Files.list(Paths.get("server_storage/")).map(p -> p.getFileName().toString()).forEach(o -> fileList.add(o));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileList;
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