package Client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;

public class RpcClient {

    /**
     *
     * @param serviceinterface 请求的接口名
     * @param address           请求服务的ip和端口
     * @param <T>               代理对象（基本类）
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T geteRemoteProxyObl(Class serviceinterface, InetSocketAddress address){




     return
             /**
              * 类加载器 需要代理对象 接口
              */
             (T) Proxy.newProxyInstance(serviceinterface.getClassLoader(),new Class[]{serviceinterface},new InvocationHandler(){


         @Override
         public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
             Socket socket  = new Socket();
             ObjectOutputStream objectOutputStream = null;
             socket.connect(address);
             //发送请求数据  包括 请求接口 请求方法  请求参数类型 请求参数
             objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

             objectOutputStream.writeUTF(serviceinterface.getName());
             objectOutputStream.writeUTF(method.getName());

             objectOutputStream.writeObject(method.getParameterTypes());
             objectOutputStream.writeObject(objects);


             //接受服务返回的对象
             ObjectInputStream oi = null;
             oi = new ObjectInputStream(socket.getInputStream());
             Object result = oi.readObject();
             return result;
         }
     });

    }


}
