/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arst.concprg.prodcons;

import java.util.Queue;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class Producer extends Thread {

    private Queue<Integer> queue = null;

    private int dataSeed = 0;
    private Random rand=null;
    private final long stockLimit;
    private Object lock;

    public Producer(Queue<Integer> queue,long stockLimit, Object lock) {
        this.queue = queue;
        rand = new Random(System.currentTimeMillis());
        this.stockLimit=stockLimit;
        this.lock=lock;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (lock) {
                try {
                    while (queue.size() >= stockLimit) {
                        lock.wait(); // Wait if queue is full
                    }

                    dataSeed = dataSeed + rand.nextInt(100);
                    System.out.println("Producer added " + dataSeed);
                    queue.add(dataSeed);
                    lock.notify(); //Notify consumers that items are available
                    Thread.sleep(1000);

                } catch (InterruptedException ex) {
                    Logger.getLogger(Producer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
