package Model;

import controller.Controller;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableView;
import javafx.stage.Stage;


public class TaskRicerca extends Task<ObservableList<FileInfo>> {
    ObservableList<FileInfo> data2;

    public TaskRicerca(ObservableList<FileInfo> l, TableView mainView, String cerca, Stage loadingStage, ObservableList<FileInfo> dataSearch) {
        this.data2 = l;
        this.setOnSucceeded(event -> {
            if (this.getValue() == null) {
                System.out.println("Errore");
                return;
            }
            dataSearch.setAll(this.getValue().filtered(pair -> {
                String fileName = pair.getName();
                int dotIndex = fileName.lastIndexOf(".");
                String nameWithoutExtension = (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
                return nameWithoutExtension.toLowerCase().contains(cerca.toLowerCase());
            }));
            mainView.setItems(dataSearch);
            loadingStage.close();
        });
        this.setOnCancelled(event -> {
            loadingStage.close();
        });
    }

    @Override
    protected ObservableList<FileInfo> call() {
        try {
            return Controller.listContenuto(data2);
        } catch (Exception e) {
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Errore");
            dialog.setContentText("Errore Critico Durante L'Esecuzione Il Programma Verrà Chiuso");
            dialog.showAndWait();
            Platform.exit();
            return null;
        }
    }
}
