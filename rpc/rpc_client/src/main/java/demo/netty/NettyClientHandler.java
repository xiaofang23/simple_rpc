package demo.netty;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import demo.connect.ConnectManage;
import demo.entity.CFile;
import demo.rpc_entity.RpcRequest;
import demo.rpc_entity.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;


@Component
@ChannelHandler.Sharable
public class NettyClientHandler extends ChannelInboundHandlerAdapter  {

    @Autowired
    ConnectManage connectManage;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private int byteRead;
    private volatile int start = 0;
    private String file_dir = "C:\\Users\\lenovo\\Desktop\\filedemo\\dir1";

    private ConcurrentHashMap<String,SynchronousQueue<Object>> queueMap = new ConcurrentHashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx)   {
        logger.info("已连接到RPC服务器.{}",ctx.channel().remoteAddress());
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx)   {
        InetSocketAddress address =(InetSocketAddress) ctx.channel().remoteAddress();
        logger.info("与RPC服务器断开连接."+address);
        ctx.channel().close();
        connectManage.removeChannel(ctx.channel());
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)throws Exception {
        logger.info("RPC服务端发送:"+JSON.toJSONString(msg));
        JSONObject object = (JSONObject)msg;
        if(object.containsKey("startPosition")){
            CFile cFile = JSON.parseObject(msg.toString(), CFile.class);;
            byte[] bytes = cFile.getBytes();
            byteRead = cFile.getEndPosition();
            String filename = cFile.getFilename();
            String path = file_dir+ File.separator+filename;
            File file = new File(path);
            RandomAccessFile randomAccessFile = new RandomAccessFile(file,"rw");
            randomAccessFile.seek(start);
            randomAccessFile.write(bytes);
            start = start+byteRead;
            if(byteRead>0){
                ctx.writeAndFlush(start);
                randomAccessFile.close();
                if(byteRead!=1024*10){
                    Thread.sleep(1000);
                }
            }else{
                randomAccessFile.close();
                logger.info("处理完毕,文件路径:"+path+","+byteRead);
                String requestId = cFile.getId();
                SynchronousQueue<Object> queue = queueMap.get(requestId);
                queue.put(cFile);
                queueMap.remove(requestId);
            }
        }else{
            logger.info("RPC服务端发送:"+JSON.toJSONString(msg));
            RpcResponse response = JSON.parseObject(msg.toString(), RpcResponse.class);
            String requestId = response.getRequestId();
            SynchronousQueue<Object> queue = queueMap.get(requestId);
            queue.put(response);
            queueMap.remove(requestId);
        }
    }

    public SynchronousQueue<Object> sendRequest(RpcRequest request, Channel channel) {
        SynchronousQueue<Object> queue = new SynchronousQueue<>();
        queueMap.put(request.getId(), queue);
        logger.info("发送:{}",JSON.toJSONString(request));
        channel.writeAndFlush(request);
        return queue;
    }

    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)throws Exception {
        logger.info("已超过30秒未与RPC服务器进行读写操作!将发送心跳消息...");
        if (evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent)evt;
            if (event.state()== IdleState.ALL_IDLE){
                RpcRequest request = new RpcRequest();
                request.setMethodName("heartBeat");
                ctx.channel().writeAndFlush(request);
            }
        }else{
            super.userEventTriggered(ctx,evt);
        }
    }


    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        logger.info("RPC通信服务器发生异常.{}",cause);
        ctx.channel().close();
    }
}
