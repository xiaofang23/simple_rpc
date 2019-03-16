package Test;

import Server.ServiceCenter;
import Server.ServiceCenterImp;
import Server.UserService;
import Server.UserServiceImp;

public class RpcServerTest {

    public static  void main(String args[]){

        ServiceCenter serviceCenter = new ServiceCenterImp();
        serviceCenter.register(UserService.class, UserServiceImp.class);
        System.out.println(UserServiceImp.class.getName());
        serviceCenter.start();
    }


}
