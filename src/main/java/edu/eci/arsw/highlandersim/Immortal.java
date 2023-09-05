package edu.eci.arsw.highlandersim;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback=null;
    
    private int health;
    
    private int defaultDamageValue;

    private final List<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());
    private final AtomicBoolean lock;
    public static final AtomicInteger lockInmortal = new AtomicInteger(0);
    public static final AtomicInteger deadThreads = new AtomicInteger(0);
    private boolean dead;
    public static boolean allDead = false;

    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb, AtomicBoolean lock) {
        super(name);
        this.updateCallback=ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue=defaultDamageValue;
        this.lock = lock;
    }

    public void run() {

        while (!dead && deadThreads.get() < immortalsPopulation.size()) {

            checkPause();

            Immortal im;

            int myIndex = immortalsPopulation.indexOf(this);

            int nextFighterIndex = r.nextInt(immortalsPopulation.size());

            //avoid self-fight
            if (nextFighterIndex == myIndex) {
                nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
            }

            im = immortalsPopulation.get(nextFighterIndex);

            List<Immortal> fighters = assignHash(im);
            Immortal minor = fighters.get(0);
            Immortal major = fighters.get(1);

            synchronized (minor) {
                synchronized (major) {
                    if (!im.isDead() && !this.isDead()) {
                        this.fight(im);
                    }
                }
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }


    private void checkPause(){
        if(lock.get()){
            try {
                lockInmortal.addAndGet(1);
                if(lockInmortal.get() == (immortalsPopulation.size() - deadThreads.get())) {
                    synchronized (lock){
                        lock.notifyAll();
                    }
                    lockInmortal.set(0);
                }
                synchronized (lockInmortal){
                    lockInmortal.wait();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void fight(Immortal i2) {

        if (i2.getHealth() > 0) {
            i2.changeHealth(i2.getHealth() - defaultDamageValue);
            this.health += defaultDamageValue;

            if (i2.getHealth() == 0) {
                i2.killInmortal();
            }

            updateCallback.processReport("Fight: " + this + " vs " + i2+"\n");

            if (deadThreads.get() + 1 == immortalsPopulation.size()) {
                updateCallback.processReport("Winner: " + this +"\n");
                allDead = true;
            }

        }

    }

    public void killInmortal(){
        dead = true; // Mark this immortal as dead
        deadThreads.incrementAndGet(); // Increase the count of dead immortals
        updateCallback.processReport(this + " is already dead!\n");
    }

    public List<Immortal> assignHash(Immortal i2) {
        int hashi1 = System.identityHashCode(this);
        int hashi2 = System.identityHashCode(i2);
        Immortal minor;
        Immortal major;

        if (hashi1 > hashi2) {
            minor = i2;
            major = this;
        } else {
            minor = this;
            major = i2;
        }

        List<Immortal> immortalList = new ArrayList<>();
        immortalList.add(minor);
        immortalList.add(major);

        return immortalList;
    }

    public void changeHealth(int v) {
        health = v;
    }

    public int getHealth() {
        return health;
    }



    public boolean isDead() {
        return dead;
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

}
