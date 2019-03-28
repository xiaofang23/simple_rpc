
package demo.connect;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.concurrent.TimeUnit;

@Component
public class RedisService {
    @Autowired//(自动注入redisTemplet)
    private RedisTemplate<String, String> redisTemplate;

    public void setRedisTemplate(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


/**
     * 判断key是否存在
     * @param key 键
     * @return true 存在 false不存在
     */

    public boolean hasKey(String key){
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

/**
     * 普通缓存获取
     * @param key 键
     * @return 值
     */

    public String get(String key){
        return key==null?null:redisTemplate.opsForValue().get(key);
    }

}

