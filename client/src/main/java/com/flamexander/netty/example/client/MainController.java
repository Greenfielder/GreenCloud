package com.flamexander.netty.example.client;

import com.flamexander.netty.example.common.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
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
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    TextField tfFileName;
    @FXML
    ListView<String> filesList;
    @FXML
    ListView<String> serverFileList;

    @FXML
    Label status;

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
                    if (am instanceof FileListRequest) {
                        FileListRequest flo = (FileListRequest) am;
                        serverFileList.getItems().clear();
                        serverFileList.getItems().addAll((flo.getListFileNames()));
                    }
                    if (am instanceof FileListUpdater){
                        FileListUpdater fileListUpdater = (FileListUpdater) am;
                        Platform.runLater(() -> {
                            serverFileList.getItems().clear();
                            fileListUpdater.getList().forEach(o -> serverFileList.getItems().add(o));
                        });
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
        status.setText("GreenCloud version: 0.8");
        t.setDaemon(true);
        t.start();
        refreshLocalFilesList();
        refreshServerFilesList();
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
        status.setText(String.valueOf(selectedItem) + " cкопирован на сервер");
    }

    public void sendFileToClient(ActionEvent actionEvent) throws InterruptedException, IOException {
        String selectedItem = getSelected(serverFileList);
        if (selectedItem == null) return;
        Network.sendMsg(new FileRequest(selectedItem));
        status.setText(String.valueOf(selectedItem) + " cкопирован на клиент");
        refreshLocalFilesList();
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

    public void refreshServerFilesList() {
        updateGUI(() -> {
            serverFileList.getItems().clear();
        });
        try {
            Network.sendMsg(new FileListRequest());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteFileClient(ActionEvent actionEvent) {
        String selectedItem = getSelected(filesList);
        if (selectedItem == null) {
            return;
        }
        try {
            Files.delete(Paths.get("client_storage/" + selectedItem + "\\"));
//            System.out.println("deleting " + "client_storage/" + selectedItem + "\\");
            status.setText(String.valueOf(selectedItem) + " удален с клиента");
        } catch (IOException e) {
            e.printStackTrace();
        }
        refreshLocalFilesList();
    }

    public void deleteFileServer(ActionEvent actionEvent) {
        String selectedItem = getSelected(serverFileList);
        if (selectedItem == null) return;
        Network.sendMsg(new FileDelete(selectedItem));
        status.setText(String.valueOf(selectedItem) + " удален с сервера");
    }

    public static void updateGUI(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }
}
