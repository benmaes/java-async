package be.bewire.resources;

import be.bewire.common.Channel;
import be.bewire.common.Permissions;
import be.bewire.common.Result;
import be.bewire.common.User;
import be.bewire.services.ChannelService;
import be.bewire.services.PermissionsService;
import be.bewire.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Path("/sync")
public class SyncTvResource {

    private static final Logger LOG = LoggerFactory.getLogger(SyncTvResource.class);

    private final UserService users = UserService.userService();
    private final PermissionsService permissions = PermissionsService.permissionsService();
    private final ChannelService channels = ChannelService.channelService();

    @GET
    @Path("/user/{user}")
    public String user(@PathParam("user") String userName) {
        User user = users.lookupUser(userName);
        return user.getName();
    }

    @GET
    @Path("/user/{user}/{permission}")
    public boolean userPermission(@PathParam("user") String userName,
                                  @PathParam("permission") String permission) {
        User user = users.lookupUser(userName);
        Permissions p = permissions.permissions(user.getUserId());
        return p.hasPermission(permission);
    }

    @GET
    @Path("/watch-channel/{user}/{permission}/{channel}")
    public boolean watchChannel(@PathParam("user") String userName,
                                @PathParam("permission") String permission,
                                @PathParam("channel") String channel) {
        User user = users.lookupUser(userName);
        Permissions p = permissions.permissions(user.getUserId());
        Channel c = channels.lookupChannel(channel);
        return c != null && p.hasPermission(permission);

    }

    @GET
    @Path("/watch-channel-fast/{user}/{permission}/{channel}")
    public boolean watchChannelFast(@PathParam("user") String userName,
                                    @PathParam("permission") String permission,
                                    @PathParam("channel") String channel) throws Exception {
        Future<Channel> fChannel = se.submit(() -> channels.lookupChannel(channel));
        User user = users.lookupUser(userName);
        Permissions p = permissions.permissions(user.getUserId());
        Channel c = fChannel.get();
        return c != null && p.hasPermission(permission);
    }

    @GET
    @Path("/watch-channel-timeout/{user}/{permission}/{channel}")
    public boolean watchChannelTimeout(@PathParam("user") String userName,
                                       @PathParam("permission") String permission,
                                       @PathParam("channel") String channel) throws Exception {

        Future<Result> fResult = se.submit(() -> {
            Future<Channel> fChannel = se.submit(() -> channels.lookupChannel(channel));
            User user = users.lookupUser(userName);
            Permissions p = permissions.permissions(user.getUserId());
            Channel c = fChannel.get();
            return new Result(c, p);
        });

        Result result = fResult.get(500, TimeUnit.MILLISECONDS);
        return result.getChannel() != null && result.getPermissions().hasPermission(permission);
    }
    private final ScheduledExecutorService se = Executors.newScheduledThreadPool(5);
}
