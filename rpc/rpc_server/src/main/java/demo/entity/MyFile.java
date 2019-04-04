package demo.entity;

public class MyFile {
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

    @Override
    public String toString() {
        return "MyFile{" +
                "filename='" + filename + '\'' +
                ", abpath='" + abpath + '\'' +
                ", isDirectory=" + isDirectory +
                '}';
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
