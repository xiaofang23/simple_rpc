package demo.service.imps;

import com.alibaba.fastjson.JSONObject;
import demo.annotation.RpcService;
import demo.entity.User;
import demo.service.interfaces.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RpcService
public class UserServiceImp implements UserService {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private final static Map<String,User> users = new ConcurrentHashMap<>() ;



    @Override
    public boolean insertUser(User user) {
        if(users.containsKey(user.getId())) {
            logger.info("新增用户ID已存在,操作取消,重复id:{}", user.getId());
            return false;
        }
        logger.info("新增用户信息:{}", JSONObject.toJSONString(user));
        users.put(user.getId(),user);
        return true;
    }

    @Override
    public User getUserById(String id) {
        logger.info("通过ID查询,ID:{}", id);
        if(users.containsKey(id))
            return null;
        return users.get(id);
    }

    @Override
    public boolean deleteUserById(String id) {
        if(users.containsKey(id))
            return false;
        for(String uid:users.keySet()){
            if(uid==id)
            {
                logger.info("删除用户信息:{}", JSONObject.toJSONString(users.get(uid)));
                users.remove(users.get(uid));
                break;
            }
        }
        return true;
    }

    @Override
    public Map<String, User> getAllUser() {
        logger.info("查询所有用户信息:{}", JSONObject.toJSONString(users));
        return users;
    }
}
