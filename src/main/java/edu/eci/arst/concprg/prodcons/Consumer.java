/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arst.concprg.prodcons;

import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class Consumer extends Thread{
    
    private Queue<Integer> queue;

    private Object lock;
    
    
    public Consumer(Queue<Integer> queue, Object lock){

        this.queue=queue;
        this.lock=lock;
    }
    
    @Override
    public void run() {
        while (true) {
            synchronized (lock) {
                try {
                    while (queue.isEmpty()) {
                        lock.wait(); // Wait if the queue is empty
                    }

                    int elem = queue.poll();
                    System.out.println("Consumer consumes " + elem);
                    //Thread.sleep(1000);

                } catch (InterruptedException ex) {
                    Logger.getLogger(Consumer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

}
