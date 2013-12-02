package cucumber.runtime;

import org.junit.Test;

import java.util.concurrent.TimeoutException;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TimeoutTest {
    @Test
    public void doesnt_time_out_if_it_takes_too_long() throws Throwable {
        final Slow slow = new Slow();
        String what = Timeout.timeout(new Timeout.Callback<String>() {
            @Override
            public String call() throws Throwable {
                return slow.slow();
            }
        }, 50);
        assertEquals("slow", what);
    }

    @Test(expected = TimeoutException.class)
    public void times_out_if_it_takes_too_long() throws Throwable {
        final Slow slow = new Slow();
        Timeout.timeout(new Timeout.Callback<String>() {
            @Override
            public String call() throws Throwable {
                return slow.slower();
            }
        }, 50);
        fail();
    }

    @Test
    public void no_thread_leak() throws Throwable{

        final long startNumberOfThreads = Thread.getAllStackTraces().size();

        for(int i = 0; i < 1000; i++){
            Timeout.timeout(new Timeout.Callback<String>() {
                @Override
                public String call() throws Throwable {
                    return null;
                }
            }, 10);
        }
        Thread.sleep(10);

        final long finishNumberOfThreads = Thread.getAllStackTraces().size();

        assertTrue("The number of threads have grown significantly. start: " + startNumberOfThreads + ",  end:" + finishNumberOfThreads,
                Math.abs(finishNumberOfThreads - startNumberOfThreads) < 5);

    }

    @Test(expected = TimeoutException.class)
    public void times_out_infinite_loop_if_it_takes_too_long() throws Throwable {
        final Slow slow = new Slow();
        Timeout.timeout(new Timeout.Callback<Void>() {
            @Override
            public Void call() throws Throwable {
                slow.infinite();
                return null;
            }
        }, 10);
        fail();
    }




    public static class Slow {
        public String slow() throws InterruptedException {
            sleep(10);
            return "slow";
        }

        public String slower() throws InterruptedException {
            sleep(100);
            return "slower";
        }

        public void infinite() throws InterruptedException {
            while (true) {
                sleep(1);
            }
        }
    }
}
