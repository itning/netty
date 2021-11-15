package top.itning.netty;

import io.netty.util.concurrent.FastThreadLocal;

/**
 * @author itning
 * @since 2021/11/14 16:50
 */
public class FastThreadLocalTest {
    private static final FastThreadLocal<String> FAST_THREAD_LOCAL = new FastThreadLocal<String>() {
        @Override
        protected String initialValue() throws Exception {
            return "空串";
        }
    };

    public static void main(String[] args) {
        System.out.println(FAST_THREAD_LOCAL.get());
        FAST_THREAD_LOCAL.set("a");
        System.out.println(FAST_THREAD_LOCAL.get());
    }
}
