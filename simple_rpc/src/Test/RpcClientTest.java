package Test;

import Client.RpcClient;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;

public class RpcClientTest {


    public static  void main(String args[]) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,InvocationTargetException {

        InetSocketAddress i = new InetSocketAddress("127.0.0.1",9999);
        Object o = RpcClient.geteRemoteProxyObl(Class.forName("Server.UserService"),i);

        Method method = o.getClass().getMethod("say", String.class);
        /*
        在这里 int与integer是两个不同 的对象
        server端方法add（int,int）  因此这边也要对应写（int.class,int.class）
         */
        Method methods = o.getClass().getMethod("add", int.class,int.class);

        Object say_result = method.invoke(o, "123");
        Object add_result = methods.invoke(o,new Object[]{2,3});
        System.out.println(say_result);
        System.out.println(add_result);

    }


}
