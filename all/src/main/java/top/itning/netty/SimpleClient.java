package top.itning.netty;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 简单客户端
 *
 * @author itning
 */
public class SimpleClient {
    public static void main(String[] args) throws Exception {
        // 创建线程池 默认线程数量CPU核数*2
        EventLoopGroup loopGroup = new NioEventLoopGroup();
        try {
            new Bootstrap()
                    .group(loopGroup)
                    // NioSocketChannel构造 打开了SocketChannel
                    // 配置非阻塞的
                    // 实例化一个 unsafe 对象
                    // 每一个channel都有自己的pipeline
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new SimpleClientHandler());
                        }
                    })
                    // 从尾节点找到第一个outbound为true的
                    // 调unSafe的connect方法
                    .connect("127.0.0.1", 8080)
                    .sync()
                    // 等待连接关闭
                    .channel()
                    .closeFuture()
                    .sync();
        } finally {
            loopGroup.shutdownGracefully();
        }
    }
}
