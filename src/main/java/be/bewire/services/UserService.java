package be.bewire.services;

import be.bewire.common.Config;
import be.bewire.common.User;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.common.util.concurrent.Uninterruptibles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1,
            new ThreadFactoryBuilder().setNameFormat("user-service-%d").build());
    private final ListeningScheduledExecutorService ls = MoreExecutors.listeningDecorator(executor);

    public static UserService userService() {
        return new UserService(ImmutableMap.of(
                "chbatey", new User("Christopher Batey", "chbatey", 1),
                "trevor", new User("Trevor Sinclair", "trevor", 2)
        ));
    }

    private final Map<String, User> users;

    private UserService(Map<String, User> users) {
        this.users = users;
    }

    public User lookupUser(String userName) {
        Uninterruptibles.sleepUninterruptibly(Config.USER_DELAY, TimeUnit.MILLISECONDS);
        LOG.info("User look up complete");
        return users.get(userName);
    }

    public Future<User> lookupUserAsync(String userName) {
        return executor.schedule(() -> {
            LOG.info("User look up complete");
            return users.get(userName);
        }, Config.USER_DELAY, TimeUnit.MILLISECONDS);
    }

    public ListenableFuture<User> lookupUserListenable(String userName) {
        return ls.schedule(() -> {
            LOG.info("User lookup complete");
            return users.get(userName);
        }, Config.USER_DELAY, TimeUnit.MILLISECONDS);
    }

    public CompletableFuture<User> lookupUserCompletable(String userName) {
        CompletableFuture<User> cUser = new CompletableFuture<>();
        // How you can very easily wrap existing APIs with an API that returns
        // completable futures.
        executor.schedule(() -> {
                    LOG.info("User lookup complete");
                    cUser.complete(users.get(userName));
                },
                Config.USER_DELAY, TimeUnit.MILLISECONDS);
        return cUser;
    }
}
