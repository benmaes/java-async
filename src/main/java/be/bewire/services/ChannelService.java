package be.bewire.services;

import be.bewire.common.Channel;
import be.bewire.common.Config;
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

public class ChannelService {
    private static Logger LOG = LoggerFactory.getLogger(ChannelService.class);
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1,
            new ThreadFactoryBuilder().setNameFormat("channel-service-%d").build());
    private final ListeningScheduledExecutorService ls = MoreExecutors.listeningDecorator(executor);

    private final Map<String, Channel> channels;

    public static ChannelService channelService() {
        return new ChannelService(ImmutableMap.of(
                "SkyOne", new Channel("SkyOne"),
                "SkySportsOne", new Channel("SkySportsOne")
        ));
    }

    private ChannelService(Map<String, Channel> channels) {
        this.channels = channels;
    }

    public Channel lookupChannel(String name) {
        Uninterruptibles.sleepUninterruptibly(Config.CHANNEL_DELAY, TimeUnit.MILLISECONDS);
        LOG.info("Channel lookup complete");
        return channels.get(name);
    }

    public Future<Channel> lookupChannelAsync(String name) {
        return executor.schedule(() -> {
            LOG.info("Channel lookup complete");
            return channels.get(name);
        }, Config.CHANNEL_DELAY, TimeUnit.MILLISECONDS);
    }

    public ListenableFuture<Channel> lookupChannelListenable(String name) {
        return ls.schedule(() -> {
            LOG.info("Channel lookup complete");
            return channels.get(name);
        }, Config.CHANNEL_DELAY, TimeUnit.MILLISECONDS);
    }

    public CompletableFuture<Channel> lookupChannelCompletable(String name) {
        CompletableFuture<Channel> result = new CompletableFuture<>();
        executor.schedule(() -> {
            LOG.info("Channel lookup complete");
            result.complete(channels.get(name));
        }, Config.CHANNEL_DELAY, TimeUnit.MILLISECONDS);
        return result;
    }
}
