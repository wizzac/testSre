package controllers;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class MyCallable implements Callable {
    EjecutarTarea tarea;
    MyCallable(EjecutarTarea tarea) {
        this.tarea=tarea;
    }
    @Override
    public Respuesta call() {
        Respuesta res=new Respuesta();
        try {
            Future<Respuesta> respuestaFuture = EjecutarTarea.getCall();
            res=respuestaFuture.get();
        }catch (Exception e){
            System.out.println(e.getMessage());

        }
        return res;
    }
}