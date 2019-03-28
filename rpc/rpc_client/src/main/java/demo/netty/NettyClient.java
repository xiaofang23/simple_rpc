package demo.netty;

import com.alibaba.fastjson.JSONArray;
import demo.en_decode.Decoder;
import demo.en_decode.Encoder;
import demo.connect.ConnectManage;

import demo.rpc_entity.RpcRequest;
import demo.rpc_entity.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.net.SocketAddress;
import java.util.concurrent.SynchronousQueue;

@Component
public class NettyClient {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private EventLoopGroup group = new NioEventLoopGroup(1);
    private Bootstrap bootstrap = new Bootstrap();

    @Autowired
    NettyClientHandler clientHandler;

    @Autowired
    ConnectManage connectManage;


    public NettyClient(){
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .option(ChannelOption.SO_KEEPALIVE,true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new IdleStateHandler(0, 0, 30));
                            pipeline.addLast(new Encoder());
                            pipeline.addLast(new Decoder());
                            pipeline.addLast("handler",clientHandler);
                        }
                    });
    }
    @PreDestroy
    public void destroy(){
        logger.info("RPC客户端退出,释放资源!");
        group.shutdownGracefully();
    }
    public Object send(RpcRequest request) throws InterruptedException{

        Channel channel = connectManage.chooseChannel();
        if (channel!=null && channel.isActive()) {
            SynchronousQueue<Object> queue = clientHandler.sendRequest(request,channel);
            Object result = queue.take();
            return JSONArray.toJSONString(result);
        }else{
            RpcResponse res = new RpcResponse();
            res.setCode(1);
            res.setError_msg("未正确连接到服务器.请检查相关配置信息!");
            return JSONArray.toJSONString(res);
        }
    }
    public Channel doConnect(SocketAddress address) throws InterruptedException {
        ChannelFuture future = bootstrap.connect(address);
        Channel channel = future.sync().channel();
        return channel;
    }
}
