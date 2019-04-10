package demo.executor;
/*
creaed by xifoo om 2019-4-10
 */
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import demo.entity.CFile;
import demo.rpc_entity.RpcRequest;
import demo.rpc_entity.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.util.Map;


public class SimpleRpcTask implements Runnable{

    private int byteRead;
    private volatile int start = 0;
    private volatile int lastLength = 0;
    public RandomAccessFile randomAccessFile;
    private CFile cFile = null;

    private final Logger logger = LoggerFactory.getLogger(SimpleRpcTask.class);

    private ChannelHandlerContext ctx;
    private Object msg;
    private Map<String, Object> serviceMap = null;

    public SimpleRpcTask(ChannelHandlerContext ctx, Object msg,Map<String, Object> serviceMap) {
        this.ctx = ctx;
        this.msg = msg;
        this.serviceMap = serviceMap;
    }

    @Override
    public void run(){
        logger.info("msg类型:{}", JSON.toJSONString(msg));
        JSONObject object = (JSONObject)msg;
        if(object.containsKey("startPosition")){
            cFile = JSON.parseObject(msg.toString(), CFile.class);
            sendCFile();
        } else{
            RpcRequest request = JSON.parseObject(msg.toString(),RpcRequest.class);
            if ("heartBeat".equals(request.getMethodName())) {
                logger.info("客户端心跳信息..."+ctx.channel().remoteAddress());
            }
            else if("getFile".equals(request.getMethodName())){
                cFile = new CFile();
                cFile.setId(request.getId());
                File file = new File((String)request.getParameters()[0]);
                if(!file.exists()){
                    try {
                        throw new FileNotFoundException("file not found");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }else{
                    cFile.setFile(file);
                }
                cFile.setFilename(cFile.getFile().getName());
                sendCFile();
            } else{
                logger.info("RPC客户端请求接口:"+request.getServiceName()+"   方法名:"+request.getMethodName());
                RpcResponse response = new RpcResponse();
                response.setRequestId(request.getId());
                try {
                    Object result = this.handler(request);
                    response.setData(result);
                } catch (Throwable e) {
                    e.printStackTrace();
                    response.setCode(1);
                    response.setError_msg(e.toString());
                    logger.error("RPC Server handle request error",e);
                }
                ctx.writeAndFlush(response);
            }
        }
    }
    private void sendCFile(){
        File file = cFile.getFile();
        try {
            randomAccessFile = new RandomAccessFile(file,"r");
            if(cFile.getStartPosition()!=cFile.getEndPosition())
                throw new IllegalStateException("send file error");
            randomAccessFile.seek(cFile.getStartPosition());
            lastLength = 2014*10;
            byte[] bytes = new byte[lastLength];
            if((byteRead = randomAccessFile.read(bytes))!=-1) {
                cFile.setEndPosition(byteRead+cFile.getStartPosition());
            }else{
                cFile.setEndPosition(-1);
            }
            cFile.setBytes(bytes);
            logger.info("服务器发送对象:{}",JSON.toJSONString(cFile));
            ctx.writeAndFlush(cFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Object handler(RpcRequest request) throws Exception {
        String className = request.getServiceName();
        Object serviceBean = serviceMap.get(className);
        if (serviceBean!=null){
            Class<?> serviceClass = serviceBean.getClass();
            String methodName = request.getMethodName();
            Class<?>[] parameterTypes = request.getParametersType();
            Object[] parameters = request.getParameters();
            logger.info("参数是:{}",JSON.toJSONString(parameters));
            logger.info("参数类型是:{}",JSON.toJSONString(parameterTypes));
            Method method = serviceClass.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(serviceBean, getParameters(parameterTypes,parameters));
        }else{
            throw new Exception("未找到服务接口,请检查配置!:"+className+"#"+request.getMethodName());
        }
    }
    private Object[] getParameters(Class<?>[] parameterTypes,Object[] parameters){
        if (parameters==null || parameters.length==0){
            return parameters;
        }else{
            Object[] new_parameters = new Object[parameters.length];
            for(int i=0;i<parameters.length;i++){
                if(parameterTypes[i].isPrimitive()|| String.class.isAssignableFrom(parameterTypes[i]))
                    new_parameters[i] = new String(parameters[i].toString());
                else
                    new_parameters[i] = JSON.parseObject(parameters[i].toString(),parameterTypes[i]);
            }
            return new_parameters;
        }
    }
}
