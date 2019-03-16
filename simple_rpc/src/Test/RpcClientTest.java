package Test;

import Client.RpcClient;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;

public class RpcClientTest {


    public static  void main(String args[]) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,InvocationTargetException {

        InetSocketAddress i = new InetSocketAddress("127.0.0.1",9999);
        Object o = RpcClient.geteRemoteProxyObl(Class.forName("Server.UserService"),i);

        Method method = o.getClass().getDeclaredMethod("say", String.class);

        Object obj = method.invoke(o, "123");
        System.out.println(obj);

    }


}
