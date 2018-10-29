package controllers;

import java.util.concurrent.Callable;

public class BuscarProducto implements Callable {

    private Long productoid;
    public BuscarProducto(Long productoid){
        this.productoid=productoid;
    }

    public Long call(){
        return 0l;
    }

}
