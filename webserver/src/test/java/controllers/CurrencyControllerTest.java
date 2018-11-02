package controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Future;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static org.junit.Assert.*;

public class CurrencyControllerTest {
    @InjectMocks
    CurrencyController controllerMock=Mockito.mock(CurrencyController.class);;

    @Test
    public void initCurrency() throws Exception {
        controllerMock.initCurrency();
        assertNotSame(CurrencyController.currency,0d);
    }

//    @Test
//    public void mockInitCurrency() {
//        verify(controllerMock).initCurrency();
//        Mockito.when(controllerMock).initCurrency()
//        assertNotSame(0d,CurrencyController.currency);
    }
