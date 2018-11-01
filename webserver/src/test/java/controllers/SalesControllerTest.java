package controllers;

import org.junit.Test;

import static org.junit.Assert.*;

public class SalesControllerTest {

    private final SalesController controller=new SalesController();
    @Test
    public void index() throws Exception {
        controller.index();
        Integer headers=controller.getResponseHeaders().size();
        assertTrue(headers>0);
        assertEquals(controller.getResponseStatus(),200);
        assertTrue(controller.getResponseBody()!=null);
    }

}