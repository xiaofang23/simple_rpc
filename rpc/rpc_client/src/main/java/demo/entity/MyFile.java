package demo.entity;

import java.io.Serializable;

public class MyFile implements Serializable {
    private String filename;
    private String abpath;
    private boolean isDirectory;

    public MyFile() {
    }

    public MyFile(String filename, String abpath, boolean isDirectory) {
        this.filename = filename;
        this.abpath = abpath;
        this.isDirectory = isDirectory;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getAbpatht(){
        return abpath;
    }

    public void setAbpath(String abpath) {
        this.abpath = abpath;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }
}
