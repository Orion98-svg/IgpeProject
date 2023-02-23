package Model;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileInfo {
    private String name;
    private String lastModified;
    public String tag;

    private String path;

    public FileInfo(File file, String tag) {
        this.name = file.getName();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date d = new Date(file.lastModified());
        this.lastModified = sdf.format(d);
        this.tag = tag;
        this.path = file.getAbsolutePath();
    }

    public FileInfo(File file) {
        this.name = file.getName();
        if (this.name.equals("")) {
            this.name = file.toString();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date d = new Date(file.lastModified());
        this.lastModified = sdf.format(d);
        this.path = file.getAbsolutePath();
        this.tag = GestoreTag.getValue(this.path);
    }

    public String getName() {
        return name;
    }

    public String getLastModified() {
        return lastModified;
    }

    public String getTag() {
        return tag;
    }

    public String getPath() {
        return path;
    }

    public void setTag(String tag) {
        this.tag = tag;
        GestoreTag.setValue(this.path, tag);
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public void rename(File current, File dest) {
        String oldPath = current.getAbsolutePath();
        if(!current.renameTo(dest)){
            System.out.println("ERRORE RINOMINAZIONE");
            return;
        }
        this.name = dest.getName();
        this.path = dest.getAbsolutePath();

        if (GestoreTag.map.containsKey(oldPath)) {
            GestoreTag.map.remove(oldPath);
            GestoreTag.map.put(dest.getAbsolutePath(), this.tag);
        }
        GestoreTag.setValue(oldPath, this.tag);
    }
}

