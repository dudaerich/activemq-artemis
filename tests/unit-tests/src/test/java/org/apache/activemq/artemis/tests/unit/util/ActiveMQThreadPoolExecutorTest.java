package org.apache.activemq.artemis.tests.unit.util;

import org.apache.activemq.artemis.utils.ActiveMQThreadFactory;
import org.apache.activemq.artemis.utils.ActiveMQThreadPoolExecutor;
import org.jboss.logging.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class ActiveMQThreadPoolExecutorTest {

   private static final Logger log = Logger.getLogger(ActiveMQThreadPoolExecutorTest.class);

   @Test
   public void threadUsageTest() throws InterruptedException {

      final int THREAD_COUNT = 60;
      final long WAIT = 30000;
      final CountDownLatch counter = new CountDownLatch(THREAD_COUNT);
      final CountDownLatch latchReady = new CountDownLatch(THREAD_COUNT);
      final CountDownLatch latchStart = new CountDownLatch(1);

      Executor threadPool = new ActiveMQThreadPoolExecutor(0, THREAD_COUNT, 60L, TimeUnit.SECONDS, ActiveMQThreadFactory.defaultThreadFactory());

      for (int i = 0; i < THREAD_COUNT + 1; i++) {
         final int number = i;
         new Thread(() -> {
            try {
               latchReady.countDown();
               latchStart.await();
               threadPool.execute(() -> {
                  log.info("Thread number " + number + " was executed.");
                  counter.countDown();
                  try {
                     Thread.sleep(WAIT);
                  } catch (InterruptedException e) {
                     log.error(e.getMessage(), e);
                  }
               });
            } catch (InterruptedException e) {
               log.error(e.getMessage(), e);
            }
         }).start();
      }

      latchReady.await();
      latchStart.countDown();

      boolean await = counter.await(WAIT, TimeUnit.MILLISECONDS);

      if (!await) {
         Assert.fail("Only " + (THREAD_COUNT - counter.getCount()) + " from " + THREAD_COUNT + " threads were executed.");
      }
   }

}
