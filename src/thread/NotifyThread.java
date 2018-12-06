package thread;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NotifyThread extends Thread{
	//The name of the ThreadFacotry which creates the current Thread
	public static final String defaultName = "hjwoohakim";
	
	//Debug symbol
	private static volatile boolean debugSymbol = false;
	
	//Count the number of the Threads which have been created
	private static final AtomicInteger createdNumber = new AtomicInteger();
	
	//Count the number of the Threads which have been running
	private static final AtomicInteger runningNumber = new AtomicInteger();
	
	//logger
	private static final Logger log = Logger.getAnonymousLogger();
	
	public NotifyThread(Runnable runnable) {
		this(runnable, defaultName);
	}
	
	public NotifyThread(Runnable runnable, String newName) {
		super(runnable, newName + createdNumber.incrementAndGet());
		setUncaughtExceptionHandler(
			new Thread.UncaughtExceptionHandler() {
				public void uncaughtException(Thread thread, Throwable error) {
					log.log(Level.SEVERE, "Uncaught at Thread" + thread.getName(), error);
				}
			});
	}
	
	public void run() {
		//copy the debugSymbol because other thread may modify it 
		boolean debugCopy = debugSymbol;
		if (debugCopy) {
			//log.log(Level.FINE, "running thread : " + getName());
		}
		//we don't need to catch the exception because the UncaughtExceptionHandler can copy with it
		try {
			runningNumber.incrementAndGet();
			super.run();
		}finally {
			runningNumber.decrementAndGet();
			if(debugCopy) {
				//log.log(Level.FINE,"finish thread : " + getName());
			}
		}
	}
	
	public static int getCreatedNumber() {
		return createdNumber.get();
	}
	
	public static int getRunningNumber() {
		return runningNumber.get();
	}
	
	public static boolean getDebugSymbol() {
		return debugSymbol;
	}

	public static void setDebugSymbol(boolean debugSymbol) {
		NotifyThread.debugSymbol = debugSymbol;
	}
}
