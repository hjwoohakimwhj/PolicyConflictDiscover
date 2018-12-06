package thread;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ThreadPoolExecutor;

public class ServiceMultiThreads {
	private final NotifyThreadFactory notifyThreadFactory;
	protected final TrackingExecutorService serviceThreads;
	private final LinkedBlockingQueue<Runnable> workQueue;
	public static final AtomicInteger taskNumber = new AtomicInteger();
	
	/**
	 * the number of thread is limited ,and the size of the queue is also fixed
	 * when the queue is fulled , we'll drop the task
	 */
	public ServiceMultiThreads(int threadNum, int capacity) {
		System.out.println("ServiceManager initiate begin!");
		notifyThreadFactory =  new NotifyThreadFactory();
	    workQueue = new LinkedBlockingQueue<Runnable>(capacity);
		serviceThreads = new TrackingExecutorService(new ThreadPoolExecutor(threadNum, threadNum, 0L, 
				TimeUnit.SECONDS, workQueue, notifyThreadFactory, new ThreadPoolExecutor.AbortPolicy()));
		System.out.println("ServiceManager initiate finish!");
		System.out.println("current thread number is " + NotifyThread.getRunningNumber());
	}
	
	//example
	public void handler() {
		try {
			serviceThreads.execute(new Runnable() {
				public void run() {
					try {
					   int taskNumLocal = taskNumber.incrementAndGet();
					   System.out.println("in run() , and i am " + taskNumLocal + "begin sleep");
					   Thread.sleep(3000);				
					   System.out.println("in run() , and i am " + taskNumLocal + "finish sleep");
					}catch(InterruptedException e) {
						//set the interrupt flag to true,because we have captured it
						Thread.currentThread().interrupt();
					}
				}
			});
		}catch(RejectedExecutionException e) {
			int taskNumLocal = taskNumber.incrementAndGet();
			//the work queue is full , and the task will be drop
			System.out.println("the queue is blocked, task " + taskNumLocal + " has been dropped");
		}
	}
	/**
	 * waiting for the task which is in queue or running to finish 
	 */
	public void slowStop() {
		try {
			serviceThreads.shutdown();
			serviceThreads.awaitTermination(Long.MAX_VALUE,TimeUnit.SECONDS);
		}
		finally {
			System.out.println("Service Finish");
		}
	}
	
	
    /**
     * cancel all running tasks and clear the queue	
     */
	public void quickStop() {
		try {
			System.out.println("quickStop begin");
			int tasksInQueue = serviceThreads.shutdownNow().size();
			System.out.println("the number of the tasks in queue has been cancelled is " + tasksInQueue);
			int tasksAtRun = serviceThreads.getTaskCancel().size();
			System.out.println("the number of the tasks at run has been cancelled is " + tasksAtRun);
		}catch(IllegalStateException e) {
			System.out.println("quickStop error");
		}
		finally {
			System.out.println("quickStop finish");
		}
	}
	
/*	public static void main(String[] args) {
		int threadNumber = 5;
		int queueSize = 5;
		ServiceMgr serviceManager = new ServiceMgr(threadNumber, queueSize);
		for(int i = 1 ; i < 7 ; i++) {
			serviceManager.handler();
		}
		try {
			Thread.sleep(2000);
		}catch(InterruptedException e) {
		}finally {
			serviceManager.quickStop();
		}
	}*/
}
