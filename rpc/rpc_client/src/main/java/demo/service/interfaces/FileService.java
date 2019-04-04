package demo.service.interfaces;

import demo.entity.MyFile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
@Component
public interface FileService {
    ArrayList<MyFile> getList(String path);
    void getFile(String path);
}
