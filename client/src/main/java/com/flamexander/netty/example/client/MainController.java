package com.flamexander.netty.example.client;

import com.flamexander.netty.example.common.AbstractMessage;
import com.flamexander.netty.example.common.FileMessage;
import com.flamexander.netty.example.common.FileRequest;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainController implements Initializable {

//    public ListView<String> serverListView;

    @FXML
    TextField tfFileName;

    @FXML
    ListView<String> filesList;
    @FXML
    ListView<String> serverFileList;

    ObservableList<String> serverList = FXCollections.observableList(new ArrayList<>());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();
                    if (am instanceof FileMessage) {
                        FileMessage fm = (FileMessage) am;
                        Files.write(Paths.get("client_storage/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                        refreshLocalFilesList();
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                Network.stop();
            }
        });

        filesList.setEffect(new DropShadow(10, Color.BLACK));
        serverFileList.setEffect(new DropShadow(10, Color.BLACK));
        t.setDaemon(true);
        t.start();
        filesList.setItems(FXCollections.observableArrayList());
        serverFileList.setItems(FXCollections.observableArrayList());
        refreshAll();
    }

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        if (tfFileName.getLength() > 0) {
            Network.sendMsg(new FileRequest(tfFileName.getText()));
            tfFileName.clear();
        }
    }

    private String getSelected(ListView<String> listView) {
        String selectedItem = listView.getSelectionModel().getSelectedItem();
        listView.getSelectionModel().clearSelection();
        return selectedItem;
    }

    public void sendFile(ActionEvent actionEvent) throws InterruptedException, IOException {
        String selectedItem = getSelected(filesList);
        if (selectedItem == null) return;
        Path file = Paths.get("client_storage/" + selectedItem);
        Network.sendMsg(new FileMessage(file));
        System.out.println(file);
        refreshAll();
    }

    public void sendFileToClient(ActionEvent actionEvent) throws InterruptedException, IOException {
        String selectedItem = getSelected(serverFileList);
        if (selectedItem == null) return;
        Path file = Paths.get("server_storage/" + selectedItem);
        Network.sendMsg(new FileRequest(selectedItem));
        System.out.println(file);
        refreshAll();
    }


    public void refreshAll() {
        refreshLocalFilesList();
//        refreshServerFilesList();
    }

    public void refreshLocalFilesList() {
        if (Platform.isFxApplicationThread()) {
            try {
                filesList.getItems().clear();
                Files.list(Paths.get("client_storage")).map(p -> p.getFileName().toString()).forEach(o -> filesList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Platform.runLater(() -> {
                try {
                    filesList.getItems().clear();
                    Files.list(Paths.get("client_storage")).map(p -> p.getFileName().toString()).forEach(o -> filesList.getItems().add(o));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

//    public void refreshServerFilesList() {
//        if (Platform.isFxApplicationThread()) {
//            try {
//                serverFileList.getItems().clear();
//                Files.list(Paths.get("server_storage")).map(p -> p.getFileName().toString()).forEach(o -> serverFileList.getItems().add(o));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } else {
//            Platform.runLater(() -> {
//                try {
//                    serverFileList.getItems().clear();
//                    Files.list(Paths.get("server_storage")).map(p -> p.getFileName().toString()).forEach(o -> serverFileList.getItems().add(o));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                serverList.clear();
//                serverFileList.setItems(serverList);
//            });
//        }
//    }

    private void refreshServerFilesList(FileMessage message) {
        Platform.runLater(() -> {
            serverList.clear();
            serverList.addAll(message.getListFileNames());
            serverFileList.setItems(serverList);
        });
    }

    public void deleteFileClient(ActionEvent actionEvent) {
        String selectedItem = getSelected(filesList);
        if (selectedItem == null) {
            return;
        }
        try {
            Files.delete(Paths.get("client_storage/" + selectedItem + "\\"));
            System.out.println("deleting " + "client_storage/" + selectedItem + "\\");
        } catch (IOException e) {
            e.printStackTrace();
        }
        refreshLocalFilesList();
    }

    public void deleteFileServer(ActionEvent actionEvent) {
        String selectedItem = getSelected(serverFileList);
        if (selectedItem == null) return;
        try {
            Files.delete(Paths.get("server_storage/" + selectedItem + "\\"));
            System.out.println("deleting " + "server_storage/" + selectedItem + "\\");
        } catch (IOException e) {
            e.printStackTrace();
        }
//        refreshServerFilesList();
    }
}
