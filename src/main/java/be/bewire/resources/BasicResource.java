package be.bewire.resources;

import com.google.common.util.concurrent.Uninterruptibles;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import java.util.concurrent.TimeUnit;

@Path("/")
public class BasicResource {

    // Synchronous
    @GET
    @Path("/funky-sync-stuff")
    public String sync() {
        return "Hello World";
    }

    // Asynchronous
    @GET
    @Path("funky-async-stuff")
    public void asyncGet1(@Suspended AsyncResponse asyncResponse) {
        // More business value
        // We need to do this on a different thread
        // Without blocking another thread :-/
        asyncResponse.resume("Hello World");
    }

    @GET
    @Path("funky-async-stuff")
    @Produces("text/plain")
    public void asyncGet2(@Suspended AsyncResponse asyncResponse) {
        new Thread() {
            @Override
            public void run() {
                // This counts as blocking!
                Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
                asyncResponse.resume("Hello World");
            }
        }.start();
    }

    @GET
    @Path("/funky-business-stuff")
    public String funky() {
        // Do something of great business value
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        return "Hello World";
    }

}
