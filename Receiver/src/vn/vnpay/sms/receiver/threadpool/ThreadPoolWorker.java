package vn.vnpay.sms.receiver.threadpool;

public class ThreadPoolWorker {

    private static int nextWorkerID = 0;
    private ObjectFIFO idleWorkers;
    private ObjectFIFO handoffBox;
    private Thread internalThread;
    private volatile boolean noStopRequested;

    /**
     * ThreadPoolWorker
     *
     * @param idleWorkers ObjectFIFO
     */
    public ThreadPoolWorker(ObjectFIFO idleWorkers) {
        this.idleWorkers = idleWorkers;

        int workerID = getNextWorkerID();
        handoffBox = new ObjectFIFO(1); // only one slot

        // just before returning, the vn.vnpay.sms.forwarder.threadpool should be created and started.
        noStopRequested = true;

        Runnable r = new Runnable() {

            public void run() {
                try {
                    runWork();
                } catch (Exception x) {
                    // in case ANY exception slips through
                    x.printStackTrace();
                }
            }
        };

        internalThread = new Thread(r);
        internalThread.start();

    }

    /**
     * getNextWorkerID
     *
     * @return int
     */
    public static synchronized int getNextWorkerID() {
        // notice: synchronized at the class level to ensure uniqueness
        int id = nextWorkerID;
        nextWorkerID++;
        return id;
    }

    /**
     * process
     *
     * @param target Runnable
     * @throws InterruptedException
     */
    public void process(Runnable target)
            throws InterruptedException {
        handoffBox.add(target);
    }

    /**
     * runWork
     */
    private void runWork() {
        while (noStopRequested) {
            try {
                //System.out.println("workerID=" + workerID + ", ready for work");
                idleWorkers.add(this);

                Runnable r = (Runnable) handoffBox.remove();
                //System.out.println("workerID=" + workerID + ", starting execution of new Runnable: " + r);
                runIt(r);
            } catch (InterruptedException x) {
                Thread.currentThread().interrupt(); // re-assert
            }
        }
    }

    /**
     * runIt
     *
     * @param r Runnable
     */
    private void runIt(Runnable r) {
        try {
            r.run();
        } catch (Exception runex) {
            System.err.println("Uncaught exception fell through from run()");
            runex.printStackTrace();
        } finally {
            Thread.interrupted();
        }
    }

    /**
     * stopRequest
     */
    public void stopRequest() {
        //System.out.println("workerID=" + workerID + ", stopRequest() received.");
        noStopRequested = false;
        internalThread.interrupt();
    }

    /**
     * isAlive
     *
     * @return boolean
     */
    public boolean isAlive() {
        return internalThread.isAlive();
    }
}
