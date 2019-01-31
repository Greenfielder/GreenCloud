package com.flamexander.netty.example.server;

import com.flamexander.netty.example.common.FileMessage;
import com.flamexander.netty.example.common.FileRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class MainHandler extends ChannelInboundHandlerAdapter {

    public static ListView<String> serverFilesList;

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

            if (msg instanceof FileMessage){
                System.out.println("Client send: " + ((FileMessage) msg).getFilename());
                FileMessage fm = (FileMessage) msg;
                Files.write(Paths.get("server_storage/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);

            } else {
                System.out.printf("Server received wrong object!");
                return;
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

//    private String getSelected(ListView<String> listView) {
//        String selectedItem = listView.getSelectionModel().getSelectedItem();
//        listView.getSelectionModel().clearSelection();
//        return selectedItem;
//    }
//
//    public void sendFileToClient(ActionEvent actionEvent) throws InterruptedException, IOException {
//        String selectedItem = getSelected(serverFilesList);
//        if (selectedItem == null) return;
//        Path file = Paths.get("server_storage/" + selectedItem);
//        Network.sendMsg(new FileRequest(selectedItem));
//        System.out.println(file);
//    }
//
    public void refreshServerFilesList() {
        if (Platform.isFxApplicationThread()) {
            try {
                serverFilesList.getItems().clear();
                Files.list(Paths.get("server_storage")).map(p -> p.getFileName().toString()).forEach(o -> serverFilesList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Platform.runLater(() -> {
                try {
                    serverFilesList.getItems().clear();
                    Files.list(Paths.get("server_storage")).map(p -> p.getFileName().toString()).forEach(o -> serverFilesList.getItems().add(o));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                serverList.clear();
                serverFilesList.setItems(serverList);
            });
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
