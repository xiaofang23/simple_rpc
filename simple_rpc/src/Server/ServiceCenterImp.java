package Server;

import Client.RpcRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServiceCenterImp implements  ServiceCenter{


    private static final int PORT = 9999;
    private static Map<String,Class> serviceMap = new HashMap<>();

    //连接池

    //java.lang.Runtime.availableProcessors()  返回可用处理器的Java虚拟机的数量。
    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private ServerSocket serverSocket;
    private boolean isRunning = false;

    public ServiceCenterImp(){

    }

    /*
    开启服务
     */
    @Override
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        isRunning = true;
            while(true){
                System.out.println("Start server......");
                Socket socket = null;

                try {
                    socket= serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                executor.execute(new ServiceTask(socket));

            }
    }

    /*
    关闭服务
     */
    @Override
    public void close() {
        isRunning = false;
        executor.shutdown();
    }

    /*
    注册服务
     */
    @Override
    public void register(Class service,Class serviceImp) {
        serviceMap.put(service.getName(),serviceImp);
    }


    private static class ServiceTask implements Runnable{

        private Socket socket;

        public ServiceTask(){};

        public ServiceTask(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            ObjectOutputStream ooStream = null;
            ObjectInputStream oiStream = null;


            try {
                ooStream = new ObjectOutputStream(socket.getOutputStream());
                oiStream = new ObjectInputStream(socket.getInputStream());

                //接受客户端发送
                RpcRequest request = new RpcRequest();
                request = (RpcRequest) oiStream.readObject();
                /*//接受客户端发送
                String serviceName = oiStream.readUTF();
                String methodname = oiStream.readUTF();

                Class[] getParameterTypes = (Class[]) oiStream.readObject();
                Object[] getParameters = (Object[]) oiStream.readObject();*/

                //执行函数 反射
                Class service = serviceMap.get(request.getServiceName());
                if(service==null)
                    System.out.println(123456);
                Method method  = service.getMethod(request.getMethodName(),request.getParametersType());
                Object resule = method.invoke(service.newInstance(),request.getParameters());

                //发送结果
                ooStream.writeObject(resule);


            } catch (Exception e) {
               e.printStackTrace();
            }finally {
                try {
                    if (ooStream != null)
                        ooStream.close();
                    if (oiStream != null)
                        oiStream.close();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }


        }
    }
}
