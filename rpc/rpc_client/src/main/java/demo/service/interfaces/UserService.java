package demo.service.interfaces;

import demo.entity.User;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public interface UserService {
    boolean insertUser(User user);
    User getUserById(String id);
    boolean deleteUserById(String id);
    Map<String,User> getAllUser();

}
