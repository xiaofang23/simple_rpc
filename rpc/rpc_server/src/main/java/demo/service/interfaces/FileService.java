package demo.service.interfaces;

import demo.entity.MyFile;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;

public interface FileService {
    ArrayList<MyFile> getList(String path);
    void getFile(String path);
}
