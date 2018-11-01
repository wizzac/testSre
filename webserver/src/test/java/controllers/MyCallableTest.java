package controllers;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.*;

public class MyCallableTest extends EjecutarTareaTest{


    @Test
    public void call() throws Exception {
        init();
        EjecutarTarea tarea=new EjecutarTarea(user,rate,map);
        MyCallable myCallable=new MyCallable(tarea);
        Respuesta resp= myCallable.call();
        assert(resp!=null);
    }



}