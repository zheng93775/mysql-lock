package com.github.zheng93775.mlock;

import org.junit.Test;

import java.util.Random;

/**
 * Created by zheng93775 on 2019/6/6.
 */
public class MLockTest {

    private MLock mLock;

    @Test
    public void test() throws InterruptedException {
        MLockManager.DEFAULT_EXPIRE_SECONDS = 5;
        mLock = new MLock("DailyJob");
        Thread thread1 = startThread(1);
        Thread thread2 = startThread(2);
        thread1.join();
        thread2.join();
    }

    private Thread startThread(int number) {
        Random random = new Random();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean lockResult = false;
                for (int i = 1; i < 10; i++) {
                    try {
                        System.out.println("thread " + number + " try lock");
                        lockResult = mLock.tryLock();
                        System.out.println("thread " + number + " try lock " + lockResult);
                        if (lockResult == false) {
                            int ms = random.nextInt(10000);
                            System.out.println("thread " + number + " waiting " + ms + " ms");
                            Thread.sleep(ms);
                            continue;
                        }
                        int ms = random.nextInt(10000);
                        System.out.println("thread " + number + " working " + ms + " ms");
                        Thread.sleep(ms);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        mLock.unlock();
                        int ms = random.nextInt(5000);
                        System.out.println("thread " + number + " unlock and sleep " + ms + " ms");
                        try {
                            Thread.sleep(ms);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        thread.start();
        return thread;
    }
}
