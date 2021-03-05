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

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    public List<Inventory> post(List<Inventory> inventoryList) 
    {
        var log = Logger.getLogger(InventoryResource.class);

        for( var inv : inventoryList) {
            log.infof("Processing inventory with itemId: %s", inv.itemId);
            
            var existing = Inventory.findByItemId(inv.itemId);
            if( existing.isEmpty() )
            {
                inv.persist();
                log.infof("Created new inventory with id %d (itemId: %s)",
                    inv.id, inv.itemId );
            }
            else
            {
                log.infof("Updating existing record id: %d (itemId: %s)",
                    existing.get().id, existing.get().itemId);
                
                // change the id so that the record can be updated
                existing.get().setEqual(inv);
                inv.id = existing.get().id;
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
