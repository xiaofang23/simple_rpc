package demo.servicecenter;


import demo.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 通过redis注册服务
 */
@Component
public class ServiceCenter {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${rpc.host}")
    private String rpchost;

    @Value("${rpc.port}")
    private int rpcport;

    private boolean isrunning = false;

    @Autowired
    private RedisService redisService;

    public void registe( Map<String, Object> servceMap) {
        if(!isrunning) {
            logger.info("服务中心启动失败");
            return;
        }
        for(String servicename:servceMap.keySet()){
            if(redisService.hasKey(servicename)) {
                continue;
            }
            redisService.set(servicename,rpchost+":"+String.valueOf(rpcport));
        }

    }

    public void close(){
        isrunning = false;
    }

    public void start(){
        isrunning = true;
    }

}
