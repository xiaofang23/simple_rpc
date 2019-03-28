package demo.netty;

import com.alibaba.fastjson.JSON;
import demo.rpc_entity.RpcRequest;
import demo.rpc_entity.RpcResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

@ChannelHandler.Sharable
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
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
        RpcRequest request = JSON.parseObject(msg.toString(),RpcRequest.class);
        if ("heartBeat".equals(request.getMethodName())) {
            logger.info("客户端心跳信息..."+ctx.channel().remoteAddress());
        }else{
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
                new_parameters[i] = JSON.parseObject(parameters[i].toString(),parameterTypes[i]);
            }
            return new_parameters;
        }
    }
}
