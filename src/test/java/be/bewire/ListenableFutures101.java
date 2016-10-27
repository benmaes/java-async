package be.bewire;

import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.common.util.concurrent.Uninterruptibles;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Examples of how to use Guava's ListenableFuture.
 * <p>
 * One of the big differences when using Guava is that if you don't specify an
 * Executor a direct executor is used.
 * <p>
 * Where as with a lot of other frameworks a different default executor is used.
 */
public class ListenableFutures101 {

    private static Logger LOGGER = LoggerFactory.getLogger(ListenableFutures101.class);

    private ListeningExecutorService lse = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

    @Test
    public void creationAndBasicUsage() throws Exception {
        ListenableFuture<String> hello = lse.submit(() -> "Hello");

        String result = hello.get();

        assertFalse("Do not expect it to be cancelled", hello.isCancelled());
        assertTrue("Expect future to be complete", hello.isDone());
        assertEquals("Hello", result);
    }

    @Test
    public void listeners101() throws Exception {
        ListenableFuture<String> hello = lse.submit(() -> "Hello");

        hello.addListener(() -> System.out.println("Well this is pretty useless, I don't even have the value"), lse);

        hello.get();
    }

    @Test
    public void listeners102() throws Exception {
        ListenableFuture<String> hello = lse.submit(() -> "Hello");

        // No lambdas, sad panda.
        Futures.addCallback(hello, new FutureCallback<String>() {
            @Override
            public void onSuccess(String result) {
                System.out.println("This time I get thr result! " + result);

            }

            @Override
            public void onFailure(Throwable t) {

            }
        });

        // Block until complete
        hello.get();
        assertTrue(hello.isDone());
    }

    /**
     * Ignoring exceptions with catching
     */
    @Test
    public void dealingWithDanger() throws Exception {
        ListenableFuture<String> dangerChris = lse.submit(() -> {
            throw new RuntimeException("Oh dear");
        });

        ListenableFuture<String> safeChris = Futures.catching(dangerChris, RuntimeException.class, r -> "Chris");

        String result = safeChris.get();
        assertEquals("Chris", result);
    }

    /**
     * Futures can be transformed
     */
    @Test
    public void transformingAFuture() throws Exception {
        ListenableFuture<String> chris = lse.submit(() -> {
            LOGGER.info("Hrmm which thread??");
            Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
            return "Chris";
        });

        // hey type inferencer, why is this cast required?
        ListenableFuture<String> helloChris = Futures.transform(chris, (Function<? super String, ? extends String>) result -> {
            LOGGER.info("Which thread now??");
            return "Hello " + result;
        });

        String result = helloChris.get();
        assertEquals("Hello Chris", result);
    }

    /**
     * The above method works if your transformation function doesn't return
     * a ListenableFuture.
     */
    @Test
    public void transformingAsync() throws Exception {
        ListenableFuture<String> chris = lse.submit(() -> {
            LOGGER.info("Hrmm which thread??");
            Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
            return "Chris";
        });

        // Without transformAsync this would be a LF<LF<String>> :(
        ListenableFuture<String> helloChris = Futures.transformAsync(chris, this::asyncHello);

        String result = helloChris.get();
        assertEquals("Hello Chris", result);
    }

    @Test
    public void avoidingTheDirectExecutor() throws Exception {
        Executor es = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
                .setNameFormat("Chris Thread %d").build());
        ListenableFuture<String> chris = lse.submit(() -> {
            LOGGER.info("Hrmm which thread??");
            Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
            return "Chris";
        });

        // Without transformAsync this would be a LF<LF<String>> :(
        ListenableFuture<String> helloChris = Futures.transformAsync(
                chris,
                this::asyncHello,
                es);

        String result = helloChris.get();
        assertEquals("Hello Chris", result);
    }

    @Test
    public void sandbox() throws Exception {
        ListenableFuture<String> chris = Futures.immediateFuture("Chris");

    }

    // dereference

    // immediateFuture

    // inCompletionOrder

    // successfulAsList

    // ListenableFutureTask

    private ListenableFuture<String> asyncHello(String name) {
        LOGGER.info("Which thread now?");
        return Futures.immediateFuture("Hello " + name);
    }
}
