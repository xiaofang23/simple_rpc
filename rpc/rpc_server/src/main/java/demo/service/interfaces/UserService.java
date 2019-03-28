package demo.service.interfaces;

import demo.entity.User;

import java.util.Map;

public interface UserService {
    boolean insertUser(User user);
    User getUserById(String id);
    boolean deleteUserById(String id);
    Map<String,User> getAllUser();

}
