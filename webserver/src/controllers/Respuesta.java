package controllers;

import java.util.concurrent.Callable;

public class Respuesta implements Callable{
    private String msj;
    private int status;

    public Respuesta(String msj,int status){
        this.msj=msj;
        this.status=status;
    }
    public Respuesta(){

    }

    @Override
    public Respuesta call(){
        return this;
    };

    public int getStatus() {
        return status;
    }

    public String getMsj() {
        return msj;
    }
}
