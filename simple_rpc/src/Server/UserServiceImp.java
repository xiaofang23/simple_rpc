package Server;

public class UserServiceImp implements UserService{

    @Override
    public String say(String content) {

        return "say"+content;
    }
}
