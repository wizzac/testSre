package controllers;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;


public class ThreadPool implements Executor {

    private final int cantidad;
    private final Slave[] threads;

    //crea un queue con block para esperar
    private LinkedBlockingQueue queue;


    // instanciar la cantidad necesaria de hilos
    public ThreadPool(int cantidad) {
        this.cantidad = cantidad;
        queue = new LinkedBlockingQueue();
        threads = new Slave[cantidad];
        for (int i = 0; i < cantidad; i++) {
            threads[i] = new Slave();
            threads[i].start();
        }
    }

    public class Slave extends Thread {
        public void run() {
            Runnable task;
            while (true) {
                synchronized (queue) {
                    //si esta vacia la pool esperar
                    while (queue.isEmpty()) {
                        try {
                            queue.wait();
                        } catch (Exception e) {
                            System.out.println("An error occurred while queue is waiting: " + e.getMessage());
                        }
                    }
                    //preguntar
                    //poll me devuelve un object, castear para respetar interfaz?
                    task = (Runnable) queue.poll();
                }
                try {
                    task.run();
                } catch (Exception e) {
                    System.out.println("Thread pool is interrupted due to an issue: " + e.getMessage());
                }
            }
        }
    }

    public void execute(Runnable task) {
        synchronized (queue) {
            queue.add(task);
            queue.notify();
        }
    }


}
