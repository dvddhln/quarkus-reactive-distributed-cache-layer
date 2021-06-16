package boundary;

import interceptor.Cached;
import io.smallrye.mutiny.Uni;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/v1/services")
public class CacheResource {
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("{id}")
    @Cached(cacheName = "my-cache")
    public Uni<String> getServiceValue(@PathParam("id") Long path) {
        return Uni.createFrom().item("CacheItemValue");
    }

}