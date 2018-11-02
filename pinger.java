import java.io.*;
import java.net.*;

public class pinger {
  public static void main(String[] args) {
    Socket s = null;
    try{
        s = new Socket("127.0.0.1", 8889);
        System.out.println("anda");
    }
    catch (Exception e){
      System.out.println("rompe");
    }
  }
}
