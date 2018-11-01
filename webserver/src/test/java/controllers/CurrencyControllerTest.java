package controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

public class CurrencyControllerTest {

    private final CurrencyController controller=new CurrencyController();
    @Test
    public void initCurrency() throws Exception {
        controller.initCurrency();
        assertNotSame(CurrencyController.currency,0d);
    }
}