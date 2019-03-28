package demo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import demo.entity.User;
import demo.service.interfaces.UserService;
import demo.util.IdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;


@Controller
public class IndexController {

    Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    UserService userService;

    @RequestMapping("/index")
    public String index(){
        return "index.html";
    }

    @RequestMapping(value = "/getList",method = RequestMethod.POST)
    @ResponseBody
    public String getUserList(@RequestParam(name = "username") String username, @RequestParam(name = "age") int age,
                              @RequestParam(name = "address") String address){

        User user = new User(IdUtil.getId(),username,age,address);
        userService.insertUser(user);

        return JSON.toJSONString(user);
        /*long start = System.currentTimeMillis();
        int thread_count = 10;
        CountDownLatch countDownLatch = new CountDownLatch(thread_count);
        for(int i=0;i<thread_count;i++){
            new Thread(()->{
                User user = new User(IdUtil.getId(),"xiao",1,"hunan");
                userService.insertUser(user);
                countDownLatch.countDown();
            }).start();
        }
        countDownLatch.await();
        long end = System.currentTimeMillis();
        logger.info("线程数:{},执行时间:{}",thread_count,(end-start));*/
    }

    @RequestMapping("getById")
    @ResponseBody
    public User getById(String id){
        logger.info("根据ID查询:{}",id);
        return userService.getUserById(id);
    }

    @RequestMapping("/getAllUser")
    @ResponseBody
    public String getAllUser(){

        Map<String, User> allUser = userService.getAllUser();

        return JSON.toJSONString(allUser);
        /*final Map<String, User> users = new HashMap<>();
        AtomicReference<String> info = new AtomicReference<>("");
        long start = System.currentTimeMillis();
        int thread_count = 10;
        CountDownLatch countDownLatch = new CountDownLatch(thread_count);
        for (int i=0;i<thread_count;i++){
             new Thread(() -> {
                Map<String, User> allUser = userService.getAllUser();
                logger.info("查询所有用户信息：{}", JSONObject.toJSONString(allUser));
                info.set(JSONObject.toJSONString(allUser));
                countDownLatch.countDown();
            }).start();
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        logger.info("线程数：{},执行时间:{}",thread_count,(end-start));
        return info.get();*/
        //return null;
    }

}
