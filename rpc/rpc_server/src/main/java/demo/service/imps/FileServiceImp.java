package demo.service.imps;

import demo.annotation.RpcService;
import demo.entity.MyFile;
import demo.service.interfaces.FileService;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

@RpcService
public class FileServiceImp implements FileService {
    @Override
    public ArrayList<MyFile> getList(String path) {

        ArrayList<MyFile> myFiles = new ArrayList<>();
        File file = new File(path);
        if(!file.exists())
            return null;
        else{
            for(File file1:file.listFiles()){
                    myFiles.add(new MyFile(file1.getAbsolutePath().replaceAll("\\\\","/"),file1.getName(),file1.isDirectory()));
            }
        }
        Collections.sort(myFiles, (o1, o2) -> {
            if(o1.isDirectory()&&!o2.isDirectory())
                return -1;
            if(!o1.isDirectory()&&!o2.isDirectory())
                return 1;
            return 0;
        });
        for(MyFile myFile:myFiles)
            System.out.println(myFile.toString());
        return myFiles;
    }

    @Override
    public void getFile(String path) {

    }
}
