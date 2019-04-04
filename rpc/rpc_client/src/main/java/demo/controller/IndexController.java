package demo.controller;

import com.alibaba.fastjson.JSONObject;
import demo.entity.MyFile;
import demo.service.interfaces.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;


@Controller
public class IndexController {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    FileService fileService;

    @RequestMapping("/index")
    public String index(){
        return "index.html";
    }

    @RequestMapping("/getFiles")
    public String getFIles(Model model){
        String path = "C:/Users/lenovo/Desktop/filedemo";
        ArrayList<MyFile> files = fileService.getList(path);
        model.addAttribute("currPath",getBefore(path));
        model.addAttribute("filelist",files);
        return "filelist";
    }

    @RequestMapping(value={"/getfiles"},method= RequestMethod.GET)
    public String getfIles(@RequestParam("filepath") String filepath,Model model){
        ArrayList<MyFile> files = fileService.getList(filepath);
        System.out.println(filepath);
        model.addAttribute("currPath",getBefore(filepath));
        model.addAttribute("filelist",files);
        return "filelist";
    }


    @RequestMapping(value={"/getFile"},method= RequestMethod.GET)
    @ResponseBody
    public String getFIle(@RequestParam("filepath") String filepath){
        filepath = filepath.replaceAll("/","\\\\");
        fileService.getFile(filepath);
        String msg = "文件位置:"+filepath;
        return msg;
    }

    public String getBefore(String path){
        int pos = path.lastIndexOf("/");
        path= path.substring(0,pos);
        return path;
    }

}
