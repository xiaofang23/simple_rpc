package demo.netty;

import demo.executor.SimpleRpcTask;
import demo.executor.SimpleRpcThreadPoolExecutor;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@ChannelHandler.Sharable
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private SimpleRpcThreadPoolExecutor simpleRpcThreadPoolExecutor;

    private final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    private final Map<String, Object> serviceMap;

    public NettyServerHandler(SimpleRpcThreadPoolExecutor simpleRpcThreadPoolExecutor,Map<String, Object> serviceMap) {
        this.simpleRpcThreadPoolExecutor = simpleRpcThreadPoolExecutor;
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
        simpleRpcThreadPoolExecutor.execute(new SimpleRpcTask(ctx,msg,serviceMap));
    }
}
