package controllers;

public class Respuesta {
    private String msj;
    private int status;

    public Respuesta(String msj,int status){
        this.msj=msj;
        this.status=status;
    }
    public Respuesta(){

    }

    public int getStatus() {
        return status;
    }

    public String getMsj() {
        return msj;
    }
}
