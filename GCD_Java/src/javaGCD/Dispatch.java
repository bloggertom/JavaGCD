package javaGCD;
import java.lang.Thread;
public class Dispatch {
	
	public enum Priority{
		BACKGROUND,
		DEFAULT,
		HIGH,
		LOW
	}
	public static void async(DispatchQueue queue, Runnable block){
		queue.dispatchAsync(block);
	}
	
	public static void sync(DispatchQueue queue, Runnable block){
		queue.dispatchSync(block);
	}
	
	public static DispatchQueue dispatchQueueCreate(String label){
		return new DispatchQueue(label, Thread.NORM_PRIORITY);
	}
	
	public static DispatchQueue getGlobalQueue(Priority identifier){
		switch(identifier){
		case BACKGROUND:
			return QueueManager.getQueueManager().getBackgroundQueue();
		case LOW:
			return QueueManager.getQueueManager().getLowPriorityQueue();
		case HIGH:
			return QueueManager.getQueueManager().getHighPriorityQueue();
		default:
			return  QueueManager.getQueueManager().getDefaultQueue();
		}
	}
	
	public static void after(final long delay, DispatchQueue queue, Runnable block){
		new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {}
				Dispatch.async(queue, block);
			}
		}).start();
	}
}
