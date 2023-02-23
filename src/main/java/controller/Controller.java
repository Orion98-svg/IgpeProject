package controller;

import Model.ColonnaNome;
import Model.TaskRicerca;
import com.example.explorer.Main;
import Model.FileInfo;
import Model.GestoreTag;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;


public class Controller {
    @FXML
    public TextField percorso;
    @FXML
    public TextField campoCerca;
    @FXML
    public Button tastoCerca;
    @FXML
    public TableView<FileInfo> mainView;
    @FXML
    public TableColumn<FileInfo, String> colonnaNome;
    @FXML
    public TableColumn<FileInfo, String> colonnaUltimaModifica;
    @FXML
    public TableColumn<FileInfo, String> colonnaTag;
    @FXML
    public ChoiceBox<String> fileTypeSelector;
    public MenuItem incolla;
    private ObservableList<FileInfo> fileInDirectory = FXCollections.observableArrayList();
    @FXML
    private TreeView<FileInfo> radice;
    ToggleGroup group = new ToggleGroup();
    @FXML
    private RadioButton nameButton;
    @FXML
    public RadioButton tagButton;

    private List<FileInfo> copiaFilesMenu = new ArrayList<>();

    @FXML
    private void initialize() {
        Label labelMainView = new Label("La cartella è vuota");
        // percorso
        percorso.focusedProperty().addListener((observable, oldValue, newValue) -> percorso.setStyle("-fx-border-color: transparent"));

        // radio button
        nameButton.setToggleGroup(group);
        tagButton.setToggleGroup(group);

        // filtro
        ObservableList<String> items = FXCollections.observableArrayList("Tutti i File", "File di Testo", "Immagini", "Documenti", "Audio", "Video", "File Compressi");
        fileTypeSelector.setItems(items);
        fileTypeSelector.setValue("Tutti i File");

        // treeView
        radice.setRoot(new TreeItem<>(new FileInfo(new File(System.getProperty("user.home")))));
        for (File cartella : File.listRoots()) {
            TreeView<FileInfo> Albero = new TreeView<>();
            Albero.setRoot(new TreeItem<>(new FileInfo(cartella)));
            Albero.getRoot().getChildren().addAll(createFileChild(cartella));
            radice.getRoot().getChildren().addAll(Albero.getRoot());
        }

        File home;
        try {
            percorso.setText(radice.getRoot().getChildren().get(0).getValue().getPath());
            home = new File(radice.getRoot().getChildren().get(0).getValue().getPath());
        } catch (Exception e) {
            percorso.setText("Errore");
            labelMainView.setText("Errore Caricamento Files");
            return;
        }

        ArrayList<FileInfo> homeFiles = new ArrayList<>();
        for (File file : home.listFiles()) {
            if (!file.isHidden()) {
                homeFiles.add(new FileInfo(file));
            }
        }
        fileInDirectory.addAll(homeFiles);
        radice.getRoot().setExpanded(true);

        // mainView
        setupMainView(labelMainView);
    }

    private void setupMainView(Label labelMainView) {
        mainView.setPlaceholder(labelMainView);
        mainView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        colonnaNome.setCellValueFactory(new PropertyValueFactory<>("name"));
        colonnaNome.setEditable(true);
        colonnaNome.setCellFactory(TextFieldTableCell.forTableColumn());
        colonnaNome.setCellFactory(column -> new ColonnaNome());

        colonnaUltimaModifica.setCellValueFactory(new PropertyValueFactory<>("lastModified"));

        colonnaTag.setCellValueFactory(new PropertyValueFactory<>("tag"));
        colonnaTag.setEditable(true);
        colonnaTag.setCellFactory(TextFieldTableCell.forTableColumn());

        mainView.setItems(fileInDirectory);
    }

    private ArrayList<TreeItem<FileInfo>> createFileChild(File file) {
        ArrayList<TreeItem<FileInfo>> allChildren = new ArrayList<>();
        if (file.isDirectory()) {
            File[] children = file.listFiles();

            if (children != null) {
                for (File child : children) {
                    if (!child.isHidden() && child.isDirectory()) {
                        TreeItem<FileInfo> item = new TreeItem<>(new FileInfo(child));
                        allChildren.add(item);
                    }
                }
            }
        }

        if (!allChildren.isEmpty()) {
            allChildren.get(0).parentProperty().addListener((parent, oldValue, newValue) -> parent.getValue().expandedProperty().addListener((is_expanded, oldValue1, newValue1) -> {
                if (is_expanded.getValue()) {
                    for (TreeItem<FileInfo> ch : parent.getValue().getChildren()) {
                        if (ch.getChildren().isEmpty()) {
                            ch.getChildren().addAll(createFileChild(new File(ch.getValue().getPath())));
                        }
                    }
                }
            }));
        }
        return allChildren;
    }

    public void cerca(MouseEvent mouseEvent) {
        ricerca();
    }

    public void cerca2(ActionEvent actionEvent) {
        ricerca();
    }

    private void ricerca() {
        ObservableList<FileInfo> data2 = FXCollections.observableArrayList();
        ObservableList<FileInfo> dataSearch = FXCollections.observableArrayList();
        data2.addAll(fileInDirectory);
        String cerca = campoCerca.getText();

        if (cerca.equals("")) {
            mainView.setItems(fileInDirectory);
            mainView.refresh();
        } else if (nameButton.isSelected()) {
            dataSearch.clear();
            Button cancelButton = new Button("Annulla");

            Stage loadingStage = popupCaricamento(cancelButton);

            Task<ObservableList<FileInfo>> task = new TaskRicerca(data2, mainView, cerca, loadingStage, dataSearch);
            Thread t = new Thread(task);
            t.setDaemon(true);
            t.start();

            cancelButton.setOnAction(event -> {
                t.interrupt();
                loadingStage.close();
            });

        } else if (tagButton.isSelected()) {
            ArrayList<FileInfo> fileTag = new ArrayList<>();
            HashMap<String, String> tags = GestoreTag.getMap();
            for (String tag : tags.keySet()) {
                if (tags.get(tag).equalsIgnoreCase(cerca)) {
                    fileTag.add(new FileInfo(new File(tag)));
                }
            }
            dataSearch.addAll(fileTag);
            mainView.setItems(dataSearch);
        }
    }

    private static Stage popupCaricamento(Button cancelButton) {
        VBox loadingLayout = new VBox(10);
        loadingLayout.setAlignment(Pos.CENTER);
        Text text = new Text("Caricamento");
        text.setFont(Font.font("System", FontWeight.BOLD, 16));
        loadingLayout.getChildren().addAll(text, new ProgressIndicator(), cancelButton);

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (text.getText().endsWith("...")) {
                text.setText("Caricamento");
            } else {
                text.setText(text.getText() + ".");
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        Stage loadingStage = new Stage();
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.initStyle(StageStyle.UNDECORATED);
        loadingStage.setScene(new Scene(loadingLayout, 200, 100, Color.GOLDENROD));

        loadingStage.show();
        return loadingStage;
    }

    public static ObservableList<FileInfo> listContenuto(ObservableList<FileInfo> data2) {
        if (Thread.currentThread().isInterrupted()) {
            return null;
        }

        ObservableList<FileInfo> files = FXCollections.observableArrayList();
        ObservableList<FileInfo> fileScan = FXCollections.observableArrayList();
        ObservableList<FileInfo> fileScan2 = FXCollections.observableArrayList();

        ArrayList<FileInfo> figli = new ArrayList<>();
        ArrayList<FileInfo> figli2 = new ArrayList<>();

        files.addAll(data2);
        for (FileInfo file : data2) {
            if (new File(file.getPath()).isDirectory()) {
                figli.add(file);
            }
        }

        for (FileInfo file : figli) {
            try {
                for (File file2 : new File(file.getPath()).listFiles()) {
                    if (!file2.isHidden()) {
                        figli2.add(new FileInfo(file2));
                    }
                }
            } catch (NullPointerException ignored) {
                //Errore durante la scasione delle cartelle i cui sono vuoti o non hanno permessi di lettura;
            }
        }

        fileScan.addAll(figli2);

        if (!figli2.isEmpty()) {
            fileScan2.addAll(listContenuto(fileScan));
        }

        files.addAll(fileScan2);

        return files;
    }

    public void tornaParentDirectory(MouseEvent mouseEvent) {
        File fileSelezionato;
        try {
            fileSelezionato = new File(percorso.getText()).getParentFile();
        } catch (NullPointerException e) {
            //Ci troviamo nella radice
            return;
        }
        if (fileSelezionato != null) {
            percorso.setText(fileSelezionato.getPath());
        } else {
            percorso.setText("");
        }
        ArrayList<FileInfo> files = new ArrayList<>();
        File current = new File(percorso.getText());
        estraiFile(files, current);
    }

    public void clickAlbero(MouseEvent mouseEvent) {
        File selectedFiles;

        if (radice.getSelectionModel().getSelectedItems().size() == 0) {
            return;
        }
        if (mouseEvent.getTarget().toString().contains("'null'")) {
            radice.getSelectionModel().clearSelection();
            return;
        }

        try {
            selectedFiles = new File(radice.getSelectionModel().getSelectedItem().getValue().getPath());
        } catch (NullPointerException e) {
            //non sono stati selezionati elementi
            return;
        }

        percorso.setText(selectedFiles.getAbsolutePath());
        TreeItem<FileInfo> item = radice.getSelectionModel().getSelectedItem();
        ArrayList<FileInfo> files = new ArrayList<>();

        File current = new File(item.getValue().getPath());

        estraiFile(files, current);
    }

    private void estraiFile(ArrayList<FileInfo> files, File current) {
        try {
            for (File file : current.listFiles()) {
                if (!file.isHidden()) {
                    files.add(new FileInfo(file));
                }
            }
        } catch (NullPointerException ignored) {
            //Errore durante la scasione delle cartelle i cui sono vuoti o non hanno permessi di lettura;
        }

        fileInDirectory.clear();
        fileInDirectory.addAll(files);
        mainView.setItems(fileInDirectory);
    }

    private void apriItem() {
        File selectedFiles = new File(mainView.getSelectionModel().getSelectedItem().getPath());
        percorso.setText(selectedFiles.getAbsolutePath());
        ArrayList<FileInfo> files = new ArrayList<>();
        File current = new File(percorso.getText());
        if (current.isDirectory()) {
            estraiFile(files, current);
            percorso.setText(selectedFiles.getAbsolutePath());
        } else if (current.isFile()) {
            HostServices hostServices = Main.hs;
            hostServices.showDocument(current.toURI().toString());
        }
    }

    private void apriItemsNewWindow() {
        for (FileInfo file : mainView.getSelectionModel().getSelectedItems()) {
            if (new File(file.getPath()).isDirectory()) {
                openNewWindow(new ActionEvent(), file.getPath());
            }
        }
    }

    private void eliminaItems() {
        List<FileInfo> fileSelezione = mainView.getSelectionModel().getSelectedItems();
        if (!fileSelezione.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Conferma eliminazione");
            alert.setHeaderText("Vuoi davvero eliminare i file selezionati?");
            alert.setContentText("Questa azione è irreversibile.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                for (FileInfo file : fileSelezione) {
                    try {
                        if (new File(file.getPath()).isDirectory()) {
                            FileUtils.deleteDirectory(new File(file.getPath()));
                        } else {
                            FileUtils.forceDelete(new File(file.getPath()));
                        }
                    } catch (IOException e) {
                        Dialog<String> dialog = new Dialog<>();
                        dialog.setTitle("Errore");
                        dialog.setContentText("Non è stato possibile eliminare il file " + file.getName() + "");
                        dialog.show();
                    }
                }
            }
        }

        ArrayList<FileInfo> files = new ArrayList<>();
        File current = new File(percorso.getText());
        if (current.isDirectory()) {
            estraiFile(files, current);
        }
    }

    private void nuovaCartella() {
        File selectedFiles = new File(percorso.getText());
        File file = new File(selectedFiles.getAbsolutePath() + File.separator + "Nuova cartella");
        try {
            if (!file.exists()) {
                file.mkdir();
            } else {
                int i = 1;
                while (file.exists()) {
                    file = new File(selectedFiles.getAbsolutePath() + File.separator + "Nuova cartella" + i);
                    i++;
                }
                file.mkdir();
            }
        } catch (Exception e) {
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Errore");
            dialog.setContentText("Impossibile creare la cartella in questo percorso");
            dialog.show();
        }
        ArrayList<FileInfo> files = new ArrayList<>();
        File current = new File(percorso.getText());
        if (current.isDirectory()) {
            estraiFile(files, current);
        }
    }

    public void MenuContesto(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem item1 = new MenuItem("Apri");
            MenuItem item2 = new MenuItem("Elimina");
            MenuItem item3 = new MenuItem("Rinomina");
            MenuItem item4 = new MenuItem("Nuovo");
            MenuItem item5 = new MenuItem("Imposta TAG");
            MenuItem item6 = new MenuItem("Rimuovi TAG");
            MenuItem item7 = new MenuItem("Apri In Nuova Finestra");

            item1.onActionProperty().set(event -> {
                apriItem();
            });

            item2.onActionProperty().set(event -> {
                eliminaItems();
            });

            item3.onActionProperty().set(event -> {
                File current_path = new File(percorso.getText());
                ObservableList<FileInfo> fileSelezione = mainView.getSelectionModel().getSelectedItems();
                File modifica;
                String estensione = "";
                File name;

                TextInputDialog dialog = new TextInputDialog("");
                dialog.setTitle("Rinomina");
                dialog.setHeaderText("Insersci il Nome:");
                Optional<String> result = dialog.showAndWait();

                if (result.isPresent()) {
                    for (FileInfo file : fileSelezione) {
                        modifica = new File(file.getPath());
                        if (modifica.isFile()) {
                            estensione = modifica.getName();
                            int dotIndex = estensione.lastIndexOf(".");
                            estensione = (dotIndex == -1) ? estensione : estensione.substring(dotIndex);
                        }
                        String inputName = result.get().trim();
                        name = new File(current_path.getAbsolutePath() + File.separator + inputName);
                        File persistent = name;
                        System.out.println(name);
                        try {
                            if (!name.exists()) {
                                if (modifica.isFile()) {
                                    name = new File(name.getAbsolutePath() + estensione);
                                }
                                file.rename(modifica, name);

                            } else {
                                int i = 1;
                                while (name.exists() && !inputName.isEmpty()) {
                                    name = new File(persistent.getAbsolutePath() + i);
                                    i++;
                                }
                                if (modifica.isFile()) {
                                    name = new File(name.getAbsolutePath() + estensione);
                                }
                                file.rename(modifica, name);
                            }
                        } catch (Exception e) {
                            Dialog<String> errorDialog = new Dialog<>();
                            errorDialog.setTitle("Errore");
                            errorDialog.setContentText("Impossibile rinominare il file");
                            errorDialog.show();
                        }
                    }
                }
                ArrayList<FileInfo> files = new ArrayList<>();
                File current = new File(percorso.getText());
                if (current.isDirectory()) {
                    aggiornaDati(files, current);
                }
            });

            item4.onActionProperty().set(event -> {
                nuovaCartella();
            });

            item5.onActionProperty().set(event -> {
                ObservableList<FileInfo> selectedItems = mainView.getSelectionModel().getSelectedItems();
                TextInputDialog dialog = new TextInputDialog("");
                dialog.setTitle("Imposta TAG");
                dialog.setHeaderText("Inserisci il TAG:");
                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    String inputTag = result.get().trim();
                    if (!inputTag.isEmpty()) {
                        for (FileInfo file : selectedItems) {
                            file.setTag(inputTag);
                        }
                    }
                }
                mainView.refresh();
            });

            item6.onActionProperty().set(event -> {
                ObservableList<FileInfo> selectedItems = mainView.getSelectionModel().getSelectedItems();
                for (FileInfo file : selectedItems) {
                    file.setTag("");
                    for (FileInfo file2 : fileInDirectory) {
                        if (file2.getPath().equals(file.getPath())) {
                            file2.setTag("");
                        }
                    }
                }
                mainView.refresh();
            });

            item7.onActionProperty().set(event -> {
                apriItemsNewWindow();
            });

            contextMenu.getItems().addAll(item1, item7, item2, item3, item4, item5, item6);

            if (item7.disableProperty().get()) {
                item7.setDisable(false);
            }

            if (mouseEvent.getTarget().toString().contains("'null'")) {
                for (MenuItem item : contextMenu.getItems()) {
                    if (item != item4) {
                        item.setDisable(true);
                    }
                }
            } else {
                for (MenuItem item : contextMenu.getItems()) {
                    item.setDisable(false);
                }
            }

            for (FileInfo file : mainView.getSelectionModel().getSelectedItems()) {
                if (new File(file.getPath()).isFile()) {
                    item7.setDisable(true);
                }

            }
            mainView.setContextMenu(contextMenu);
        }
    }

    private void aggiornaDati(ArrayList<FileInfo> files, File current) {
        try {
            for (File file2 : current.listFiles()) {
                if (!file2.isHidden()) {
                    files.add(new FileInfo(file2));
                }
            }
        } catch (NullPointerException ignored) {
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Errore");
            dialog.setContentText("Errore nell'apertura della cartella/file selezionata");
            dialog.show();
        }
        fileInDirectory.clear();
        fileInDirectory.addAll(files);
        mainView.setItems(fileInDirectory);
    }

    public void clickMainView(MouseEvent mouseEvent) {
        FileInfo item = mainView.getSelectionModel().getSelectedItem();

        if (mouseEvent.getTarget().toString().contains("'null'")) {
            mainView.getSelectionModel().clearSelection();
        }

        MenuContesto(mouseEvent);

        File itemSelezionati;
        try {
            itemSelezionati = new File(mainView.getSelectionModel().getSelectedItem().getPath());
        } catch (NullPointerException e) {
            //non sono stati selezionati file
            return;
        }

        ArrayList<FileInfo> files = new ArrayList<>();
        if (mouseEvent.getClickCount() == 2) {
            File current = new File(item.getPath());

            if (current.isDirectory()) {
                estraiFile(files, current);
                percorso.setText(itemSelezionati.getAbsolutePath());

            } else if (current.isFile()) {
                HostServices hostServices = Main.hs;
                hostServices.showDocument(current.toURI().toString());
            }
        }
    }

    public void filtro(MouseEvent mouseEvent) {
        final ObservableList<FileInfo> filtrato = FXCollections.observableArrayList(mainView.getItems());

        fileTypeSelector.setOnAction(event -> {
            String selected = fileTypeSelector.getValue();
            switch (selected) {
                case "Tutti i File":
                    if (campoCerca.getText().isEmpty())
                        mainView.setItems(fileInDirectory);
                    else {
                        ricerca();
                    }
                    break;
                case "File di Testo": {
                    ArrayList<FileInfo> homeFiles = new ArrayList<>();
                    String[] extensions = new String[]{".txt", ".log", ".ini", ".conf", ".cfg"};
                    filtroFile(filtrato, homeFiles, extensions);
                }
                case "Immagini": {
                    ArrayList<FileInfo> homeFiles = new ArrayList<>();
                    String[] extensions = new String[]{".jpg", ".png", ".gif", ".bmp", ".jpeg", ".tiff", ".tif"};
                    filtroFile(filtrato, homeFiles, extensions);
                }
                case "Documenti": {
                    ArrayList<FileInfo> homeFiles = new ArrayList<>();
                    String[] extensions = new String[]{".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx"};
                    filtroFile(filtrato, homeFiles, extensions);
                    break;
                }
                case "Audio": {
                    ArrayList<FileInfo> homeFiles = new ArrayList<>();
                    String[] extensions = new String[]{".mp3", ".wav", ".wma", ".ogg", ".flac", ".aac", ".m4a"};
                    filtroFile(filtrato, homeFiles, extensions);
                }
                case "Video": {
                    ArrayList<FileInfo> homeFiles = new ArrayList<>();
                    String[] extensions = new String[]{".mp4", ".avi", ".mkv", ".flv", ".wmv", ".mov", ".mpg"};
                    filtroFile(filtrato, homeFiles, extensions);
                }
                case "File Compressi": {
                    ArrayList<FileInfo> homeFiles = new ArrayList<>();
                    String[] extensions = new String[]{".zip", ".rar", ".7z", ".tar", ".gz", ".bz2", ".xz"};
                    filtroFile(filtrato, homeFiles, extensions);
                }
            }
        });
    }

    private void filtroFile(ObservableList<FileInfo> filtrato, ArrayList<FileInfo> homeFiles, String[] extensions) {
        for (FileInfo file : filtrato) {
            for (String extension : extensions) {
                if (new File(file.getPath()).isFile() && !new File(file.getPath()).isHidden() && file.getName().endsWith(extension)) {
                    homeFiles.add(file);
                }
            }
        }

        filtrato.clear();
        filtrato.addAll(homeFiles);
        mainView.setItems(filtrato);
    }

    public void cambioDirectoryManuale(ActionEvent actionEvent) {
        cambiaPercorso(percorso.getText());
    }

    public void cambiaPercorso(String path) {
        percorso.setText(path);
        File current = new File(path);
        if (current.exists() && current.isDirectory()) {
            ArrayList<FileInfo> files = new ArrayList<>();
            aggiornaDati(files, current);
            mainView.requestFocus();
        } else if (current.isFile()) {
            HostServices hostServices = Main.hs;
            hostServices.showDocument(current.toURI().toString());
        } else {
            percorso.setStyle("-fx-border-color: #FF0000");
        }
    }

    public void nuovoMenu(ActionEvent actionEvent) {
        nuovaCartella();
    }

    public void apriMenu(ActionEvent actionEvent) {
        if (mainView.getSelectionModel().getSelectedItem() != null) {
            List<FileInfo> listApri = mainView.getSelectionModel().getSelectedItems();
            if (listApri.size() > 1) {
                for (FileInfo file : listApri) {
                    if (new File(file.getPath()).isFile()) {
                        HostServices hostServices = Main.hs;
                        hostServices.showDocument(new File(file.getPath()).toURI().toString());
                    }
                }
            } else {
                apriItem();
            }
        }
    }

    public void eliminaMenu(ActionEvent actionEvent) {
        eliminaItems();
    }

    public void esci(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void copiaMenu(ActionEvent actionEvent) {
        if (mainView.getSelectionModel().getSelectedItems() != null) {
            copiaFilesMenu.addAll(mainView.getSelectionModel().getSelectedItems());
            if (!copiaFilesMenu.isEmpty())
                incolla.setDisable(false);
        }
    }

    public void incollaMenu(ActionEvent actionEvent) {
        if (!copiaFilesMenu.isEmpty()) {
            for (FileInfo file : copiaFilesMenu) {
                File source = new File(file.getPath());
                File dest = new File(percorso.getText() + File.separator + file.getName());
                try {
                    Files.copy(source.toPath(), dest.toPath());
                } catch (IOException e) {
                    Dialog<String> dialog = new Dialog<>();
                    dialog.setTitle("Errore");
                    dialog.setContentText("Impossibile Incollare File in questa Percorso");
                    dialog.show();
                    return;
                }
            }
            copiaFilesMenu.clear();
            aggiornaDati(new ArrayList<>(), new File(percorso.getText()));
        }
    }

    public void selezionaTutti(ActionEvent actionEvent) {
        mainView.getSelectionModel().selectAll();
        mainView.requestFocus();
    }

    public void deselezionaTutti(ActionEvent actionEvent) {
        mainView.getSelectionModel().clearSelection();
    }

    public void openNewWindow(ActionEvent actionEvent, String path) {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("View/Explorer.fxml"));
        Scene scene;
        try {
            scene = new Scene(fxmlLoader.load());
        } catch (Exception e) {
            //File xml non trovato
            return;
        }
        Stage stage = new Stage();
        stage.setTitle("Explorer");
        stage.getIcons().add(new Image("icona.png"));
        stage.setScene(scene);

        Controller controller = fxmlLoader.getController();
        controller.cambiaPercorso(path);

        stage.show();
    }
}

