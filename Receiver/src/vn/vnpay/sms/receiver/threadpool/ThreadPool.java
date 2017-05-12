package vn.vnpay.sms.receiver.threadpool;

public class ThreadPool {

   private ObjectFIFO idleWorkers;
   private ThreadPoolWorker[] workerList;

   /**
    * ThreadPool
    *
    * @param numberOfThreads int
    */
   public ThreadPool(int numberOfThreads) {
      // make sure that it's at least one
      numberOfThreads = Math.max(1, numberOfThreads);

      idleWorkers = new ObjectFIFO(numberOfThreads);
      workerList = new ThreadPoolWorker[numberOfThreads];

      for (int i = 0; i < workerList.length; i++) {
         workerList[i] = new ThreadPoolWorker(idleWorkers);
      }
   }


   /**
    * execute
    *
    * @param target Runnable
    * @throws InterruptedException
    */
   public void execute(Runnable target)
      throws InterruptedException {
      // block (forever) until a worker is available
      ThreadPoolWorker worker = (ThreadPoolWorker) idleWorkers.remove();
      worker.process(target);
   }

   /**
    * stopRequestIdleWorkers
    */
   public void stopRequestIdleWorkers() {
      try {
         Object[] idle = idleWorkers.removeAll();
          for (Object anIdle : idle) {
              ((ThreadPoolWorker) anIdle).stopRequest();
          }
      }
      catch (InterruptedException x) {
         Thread.currentThread().interrupt(); // re-assert
      }
   }

   /**
    * stopRequestAllWorkers
    */
   public void stopRequestAllWorkers() {
      stopRequestIdleWorkers();

      try {
         Thread.sleep(250);
      }
      catch (InterruptedException ignored) {
      }

       for (ThreadPoolWorker aWorkerList : workerList) {
           if (aWorkerList.isAlive()) {
               aWorkerList.stopRequest();
           }
       }
   }
}
