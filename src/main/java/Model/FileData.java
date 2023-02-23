package Model;

import java.io.File;
import java.util.Date;
import java.util.List;

public class FileData {
    private File file;
    private Date lastModified;
    private List<String> tags;

    public FileData(File file, Date lastModified, List<String> tags) {
        this.file = file;
        this.lastModified = lastModified;
        this.tags = tags;
    }

    public FileData(String s) {
        this.file = new File(s);
    }

    public File getFile() {
        return file;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public List<String> getTags() {
        return tags;
    }
}
