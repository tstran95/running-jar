package com.vn.runjar.resources;

import com.vn.runjar.model.ClassInfo;
import com.vn.runjar.response.Response;
import com.vn.runjar.services.impl.AppServiceImpl;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.UUID;

@Slf4j
@Path("/fly")
public class AppResource {
    AppServiceImpl appService = AppServiceImpl.getInstance();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response fly(ClassInfo classInfo) {
        log.info("AppResource fly() START");
        String uuid = UUID.randomUUID().toString();
        try {
            log.info("AppResource fly() END");
            classInfo.setTokenID(uuid);
            return appService.run(classInfo);
        } catch (Exception e) {
            log.error("AppResource fly() FAIL with message ", e);
            return Response.responseError(uuid);
        }
    }
}
