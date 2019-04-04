package demo.connect;

import demo.netty.NettyClient;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ConnectManage {

    @Autowired
    NettyClient nettyClient;

    @Autowired
    RedisService redisService;

    Logger logger = LoggerFactory.getLogger(this.getClass());
    private AtomicInteger roundRobin = new AtomicInteger(0);
    private CopyOnWriteArrayList<Channel> channels = new CopyOnWriteArrayList<>();
    private Map<SocketAddress, Channel> channelNodes = new ConcurrentHashMap<>();


    /*
    循环队列
     */
    public  Channel chooseChannel() {
        if (channels.size()>0) {
            int size = channels.size();
            int index = (roundRobin.getAndAdd(1) + size) % size;
            return channels.get(index);
        }else{
            return null;
        }
    }

    @PostConstruct
    private void connectServerNode(){
        String servicename = "demo.service.interfaces.FileService";
        String host =null;
        int port = 0;
        if(redisService.hasKey(servicename)){
            String[] temp = redisService.get(servicename).split(":");
            host = temp[0];
            port = Integer.valueOf(temp[1]);
        }else{
            logger.info("未找到该服务:{}",servicename);
        }
        SocketAddress address = new InetSocketAddress(host,port);
        try {
            Channel channel = nettyClient.doConnect(address);
            addChannel(channel,address);
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.info("未能成功连接到服务器:{}",address);
        }
    }

    private void addChannel(Channel channel, SocketAddress address) {
        logger.info("加入Channel到连接管理器.{}",address);
        channels.add(channel);
        channelNodes.put(address, channel);
    }
    public void removeChannel(Channel channel){
        logger.info("从连接管理器中移除失效Channel.{}",channel.remoteAddress());
        SocketAddress remotePeer = channel.remoteAddress();
        channelNodes.remove(remotePeer);
        channels.remove(channel);
    }

}
