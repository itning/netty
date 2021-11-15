package top.itning.netty;

import io.netty.util.Recycler;

import java.util.UUID;

/**
 * @author itning
 * @since 2021/11/14 19:05
 */
public class RecyclerTest {
    private static final Recycler<Test> RECYCLER = new Recycler<Test>() {
        @Override
        protected Test newObject(Handle<Test> handle) {
            String value = UUID.randomUUID().toString();
            System.out.println("newObject value " + value);
            return new Test(value, handle);
        }
    };

    public static void main(String[] args) {
        Test test = RECYCLER.get();
        System.out.println("get " + test.value);
        test.recycle();
        Test test2 = RECYCLER.get();
        System.out.println("get2 " + test2.value);
    }

    public static class Test {
        public Test(String value, Recycler.Handle<Test> handle) {
            this.value = value;
            this.handle = handle;
        }

        private final String value;
        private final Recycler.Handle<Test> handle;

        public String getValue() {
            return value;
        }

        public void recycle() {
            //回收
            handle.recycle(this);
        }


        @Override
        public String toString() {
            return "Test{" +
                    "value='" + value + '\'' +
                    '}';
        }
    }
}
