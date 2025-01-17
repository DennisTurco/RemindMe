package remindme.Entities;

public class RemindListPath {
    private String directory;
    private String file;

    public RemindListPath(String directory, String file) {
        this.directory = directory;
        this.file = file;
    }

    public String getDirectory() {
        return directory;
    }
    public String getFile() {
        return file;
    }
    public void setDirectory(String directory) {
        this.directory = directory;
    }
    public void setFile(String file) {
        this.file = file;
    }
}
