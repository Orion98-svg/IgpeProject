package Model;

import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;

public class ColonnaNome extends TableCell<FileInfo, String> {
    private final ImageView iconView = new ImageView();
    {
        iconView.setFitHeight(16);
        iconView.setFitWidth(16);
    }

    @Override
    protected void updateItem(String name, boolean empty) {
        super.updateItem(name, empty);

        if (empty || name == null) {
            setText(null);
            setGraphic(null);
        } else {
            FileInfo fileInfo = getTableView().getItems().get(getIndex());
            // set the icon based on the file type
            if (new File(fileInfo.getPath()).isDirectory()) {
                iconView.setImage(new Image("folder.png"));
            } else {
                iconView.setImage(new Image("file.png"));
            }
            setText(name);
            setGraphic(iconView);
        }
    }
}
