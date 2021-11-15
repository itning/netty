package top.itning.netty;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.concurrent.TimeUnit;

/**
 * @author itning
 * @since 2021/11/15 15:46
 */
public class DefaultPromiseTest {
    public static void main(String[] args) {
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
        final DefaultPromise<String> defaultPromise = new DefaultPromise<String>(eventExecutors.next());
        eventExecutors.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(2);
                    if(true){
                        throw new InterruptedException("123");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    defaultPromise.setFailure(e);
                    return;
                }
                defaultPromise.setSuccess("success");
            }
        });
        defaultPromise.addListener(new GenericFutureListener<Future<? super String>>() {
            @Override
            public void operationComplete(Future<? super String> future) throws Exception {
                if (future.isSuccess()) {
                    System.out.println(future.get());
                } else {
                    System.out.println(future.cause());
                }
            }
        });
        defaultPromise.cancel(true);
    }
}
