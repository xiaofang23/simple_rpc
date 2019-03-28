package demo.en_decode;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * 解码
 */
public class Decoder extends LengthFieldBasedFrameDecoder  {

    public Decoder() {
        super(65535, 0, 4,0,4);
    }
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception{
        ByteBuf decode = (ByteBuf) super.decode(ctx, in);
        if (decode==null){
            return null;
        }
        int datalen = decode.readableBytes();
        byte[] bytes = new byte[datalen];
        decode.readBytes(bytes);
        Object object = JSON.parse(bytes);
        return object;
    }
}
