package demo.netty;

import com.alibaba.fastjson.JSON;
import demo.entity.CFile;
import demo.entity.MyFile;
import demo.rpc_entity.RpcRequest;
import demo.rpc_entity.RpcResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.util.Map;

@ChannelHandler.Sharable
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private int byteRead;
    private volatile int start = 0;
    private volatile int lastLength = 0;
    public RandomAccessFile randomAccessFile;
    private volatile String filepath;
    private CFile cFile = null;

    private final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    private final Map<String, Object> serviceMap;

    public NettyServerHandler(Map<String, Object> serviceMap) {
        this.serviceMap = serviceMap;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        logger.info("客户端连接成功! "+ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.info("客户端断开连接!+ "+ctx.channel().remoteAddress());
        ctx.channel().close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        //logger.info("客户端请求:{}",JSON.toJSONString(msg));
        logger.info("msg类型:{}",JSON.toJSONString(msg));
        if(msg instanceof Integer){
            start = (Integer) msg;
            if(start!=-1) {
                randomAccessFile = new RandomAccessFile(cFile.getFile(), "r");
                randomAccessFile.seek(start);
                logger.info("长度:{}", (randomAccessFile.length() - start));
                int sentlenth = (int) (randomAccessFile.length() - start);
                if (sentlenth < lastLength)
                    lastLength = sentlenth;
                byte[] bytes = new byte[lastLength];
                logger.info("发送长度:{}", lastLength);
                if ((byteRead = randomAccessFile.read(bytes)) != -1 && (randomAccessFile.length() - start > 0)) {
                    logger.info("读取到 = " + byteRead);
                    cFile.setEndPosition(byteRead);
                    cFile.setBytes(bytes);
                    try {
                        ctx.writeAndFlush(cFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                logger.info("发送长度12345654345:{}", lastLength);
                if(lastLength==0){
                    cFile.setEndPosition(byteRead);
                    cFile.setBytes(bytes);
                    ctx.writeAndFlush(cFile);
                }
            }
        }else{
            if(null!= randomAccessFile){
                randomAccessFile.close();
                randomAccessFile = null;
                logger.info("文件已经读取完:{}",byteRead);
            }
            RpcRequest request = JSON.parseObject(msg.toString(),RpcRequest.class);
            if ("heartBeat".equals(request.getMethodName())) {
                logger.info("客户端心跳信息..."+ctx.channel().remoteAddress());
            }
            else if("getFile".equals(request.getMethodName())){
                filepath = (String)request.getParameters()[0];
                File file = new File(filepath);
                if(!file.exists()){
                    logger.info("文件不存在");
                }else{
                    cFile = new CFile();
                    cFile.setFile(file);
                    cFile.setFilename(file.getName());
                }
                randomAccessFile = new RandomAccessFile(file,"r");
                randomAccessFile.seek(cFile.getStartPosition());
                lastLength = 2014*10;
                byte[] bytes = new byte[lastLength];
                if((byteRead = randomAccessFile.read(bytes))!=-1) {
                    cFile.setEndPosition(byteRead);
                    cFile.setBytes(bytes);
                    cFile.setId(request.getId());
                    ctx.writeAndFlush(cFile);
                }
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
