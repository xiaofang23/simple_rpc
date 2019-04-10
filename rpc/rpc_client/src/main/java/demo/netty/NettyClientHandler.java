package demo.netty;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import demo.connect.ConnectManage;
import demo.entity.CFile;
import demo.rpc_entity.RpcRequest;
import demo.rpc_entity.RpcResponse;
import demo.service.imps.UserPathService;
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

/*
created by xifoo ,i didnot mark the date when it first was created,maybe i can refer the date on git about the file
 */
@Component
@ChannelHandler.Sharable
public class NettyClientHandler extends ChannelInboundHandlerAdapter  {

    @Autowired
    ConnectManage connectManage;

    @Autowired
    UserPathService userPathService;

    Logger logger = LoggerFactory.getLogger(this.getClass());

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
        handle(ctx,msg);
        logger.info("RPC服务端发送:"+JSON.toJSONString(msg));
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

    private void handle(ChannelHandlerContext ctx, Object msg) throws Exception{
        int byteRead;
        JSONObject object = (JSONObject)msg;
        if(object.containsKey("startPosition")){
            int start=0;
            CFile cFile = JSON.parseObject(msg.toString(), CFile.class);
            String filename = cFile.getFilename();
            String path = userPathService.getStorypath()+ File.separator+filename;
            byte[] bytes = cFile.getBytes();
            byteRead = bytes.length;
            if(cFile.getEndPosition()==-1){
                logger.info("处理完毕,文件路径:"+path+","+byteRead);
                String requestId = cFile.getId();
                SynchronousQueue<Object> queue = queueMap.get(requestId);
                queue.put(cFile);
                queueMap.remove(requestId);
                return ;
            }
            File file = new File(path);
            RandomAccessFile randomAccessFile = new RandomAccessFile(file,"rw");
            randomAccessFile.seek(cFile.getStartPosition());
            randomAccessFile.write(bytes);
            start = cFile.getEndPosition();
            cFile.setStartPosition(start);
            cFile.setBytes(null);
            ctx.writeAndFlush(cFile);
            randomAccessFile.close();
        }else{
            logger.info("RPC服务端发送:"+JSON.toJSONString(msg));
            RpcResponse response = JSON.parseObject(msg.toString(), RpcResponse.class);
            String requestId = response.getRequestId();
            SynchronousQueue<Object> queue = queueMap.get(requestId);
            queue.put(response);
            queueMap.remove(requestId);
        }
    }
}
