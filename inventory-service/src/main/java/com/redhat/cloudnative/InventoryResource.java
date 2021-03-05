package com.redhat.cloudnative;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

@Path("/api/inventory")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class InventoryResource {

    @GET
    public List<Inventory> getAll() {
        return Inventory.listAll();
    }

    @GET
    @Path("{itemId}")
    public List<Inventory> getAvailability(@PathParam String itemId) {
        return Inventory.<Inventory>streamAll()
        .filter(p -> p.itemId.equals(itemId))
        .collect(Collectors.toList());
    }

    private Boolean isAvailable( String itemId )
    {
        return !getAvailability(itemId).isEmpty();
    }

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    public List<Inventory> post(List<Inventory> inventoryList) 
    {
        var log = Logger.getLogger("InventoryResource");

        for( var inv : inventoryList) {
            log.infof("Got inventory with itemId: %s", inv.itemId);
            
            List<Inventory> existing = Inventory.list("itemId", inv.itemId);
            if( existing.isEmpty() )
            {
                inv.persist();
            }
            else
            {
                log.infof("FIXME: Need to update existing record id: %d",existing.get(0).id);
            }
        }

        return inventoryList;
    }

    @Provider
    public static class ErrorMapper implements ExceptionMapper<Exception> {

        @Override
        public Response toResponse(Exception exception) {
            int code = 500;
            if (exception instanceof WebApplicationException) {
                code = ((WebApplicationException) exception).getResponse().getStatus();
            }
            return Response.status(code)
                    .entity(Json.createObjectBuilder().add("error", exception.getMessage()).add("code", code).build())
                    .build();
        }

    }
}
