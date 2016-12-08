package be.bewire.services;

import be.bewire.common.Config;
import be.bewire.common.Permissions;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.common.util.concurrent.Uninterruptibles;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PermissionsService {

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1,
            new ThreadFactoryBuilder().setNameFormat("permissions-service-%d").build());
    private final ListeningScheduledExecutorService ls = MoreExecutors.listeningDecorator(executor);

    public static PermissionsService permissionsService() {
        Map<Integer, Permissions> of = ImmutableMap.of(
                1, Permissions.permissions("SPORTS", "ENTS"),
                2, Permissions.permissions()
        );
        return new PermissionsService(of);
    }

    private final Map<Integer, Permissions> permissions;

    private PermissionsService(Map<Integer, Permissions> permissions) {
        this.permissions = permissions;
    }

    public Permissions permissions(int userId) {
        Uninterruptibles.sleepUninterruptibly(Config.PERMISSION_DELAY, TimeUnit.MILLISECONDS);
        return permissions.get(userId);
    }

    public Future<Permissions> permissionsAsync(int userId) {
        return executor.schedule(() -> {
            return permissions.get(userId);
        }, Config.PERMISSION_DELAY, TimeUnit.MILLISECONDS);
    }

    public ListenableFuture<Permissions> permissionsListenable(int userId) {
        return ls.schedule(() -> {
            return permissions.get(userId);
        }, Config.PERMISSION_DELAY, TimeUnit.MILLISECONDS);
    }

    public CompletableFuture<Permissions> permissionsCompletable(int userId) {
        CompletableFuture<Permissions> result = new CompletableFuture<>();
        executor.schedule(() -> {
            result.complete(permissions.get(userId));
        }, Config.PERMISSION_DELAY, TimeUnit.MILLISECONDS);
        return result;
    }
}
