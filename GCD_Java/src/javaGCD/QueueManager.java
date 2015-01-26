package javaGCD;

import java.util.concurrent.PriorityBlockingQueue;

class QueueManager implements Runnable{

	private final static QueueManager instance = new QueueManager();
	
	private final int MAX_QUEUE_SIZE = 15;
	private final int MAX_QUEUE_TOLERANCE = 15;
	
	private PriorityBlockingQueue<DispatchQueue>defaultQueues;
	private PriorityBlockingQueue<DispatchQueue>highQueues;
	private PriorityBlockingQueue<DispatchQueue>lowQueues;
	private PriorityBlockingQueue<DispatchQueue>backgroundQueues;
	
	private QueueManager(){
		defaultQueues = new PriorityBlockingQueue<DispatchQueue>(10);
		highQueues = new PriorityBlockingQueue<DispatchQueue>(10);
		lowQueues = new PriorityBlockingQueue<DispatchQueue>(10);
		backgroundQueues = new PriorityBlockingQueue<DispatchQueue>(10);
		
		Thread monitor = new Thread(this);
		monitor.setPriority(priorityForQueue(backgroundQueues));
		monitor.start();
	}

	DispatchQueue getDefaultQueue(){
		return getDispatchQueue(defaultQueues);
	}
	
	DispatchQueue getBackgroundQueue(){
		return getDispatchQueue(backgroundQueues);
	}
	
	DispatchQueue getLowPriorityQueue(){
		return getDispatchQueue(lowQueues);
	}
	
	DispatchQueue getHighPriorityQueue(){
		return getDispatchQueue(highQueues);
	}
	
	private DispatchQueue getDispatchQueue(PriorityBlockingQueue<DispatchQueue> blockerQueue){
		DispatchQueue queue = blockerQueue.peek();
		if (queue == null ||blockerQueue.size() <= MAX_QUEUE_SIZE || queue.size() >= MAX_QUEUE_TOLERANCE){
			queue = new DispatchQueue("Dispatch Queue "+blockerQueue.size(), priorityForQueue(blockerQueue));
		}else{
			do{
				try {
					queue = blockerQueue.take();
				} catch (InterruptedException e) {}
			}while(queue == null);
		}
		System.out.println("queue has "+queue.size()+" jobs");
		System.out.println(blockerQueue.size()+" Queues");
		blockerQueue.offer(queue);
		synchronized(monitorLock){
			monitorLock.notifyAll();
		}
		return queue;
	}
	
	private int priorityForQueue(PriorityBlockingQueue<DispatchQueue>queue){
		if(queue == defaultQueues){
			return Thread.NORM_PRIORITY;
		}
		
		if(queue == highQueues){
			return Thread.MAX_PRIORITY;
		}
		
		if(queue == lowQueues){
			return Thread.MIN_PRIORITY;
		}
		
		return 3;//background
	}
	
	static QueueManager getQueueManager(){
		return instance;
	}

	private Object monitorLock = new Object();
	@Override
	public void run() {
		boolean checked;
		while(true){
			checked = false;
			System.out.println("Checking Queue integraty");
			if(defaultQueues.size() > MAX_QUEUE_SIZE){
				checked = true;
				checkPriortyQueue(defaultQueues);
			}
			if(highQueues.size() > MAX_QUEUE_SIZE){
				checked = true;
				checkPriortyQueue(highQueues);
			}
			if(lowQueues.size() > MAX_QUEUE_SIZE){
				checked = true;
				checkPriortyQueue(lowQueues);
			}
			if(backgroundQueues.size() > MAX_QUEUE_SIZE){
				checked = true;
				checkPriortyQueue(backgroundQueues);
			}
			try {
				if(!checked){
					synchronized(monitorLock){
						monitorLock.wait();
					}
				}else{
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {}
		}
	}
	
	private void checkPriortyQueue(PriorityBlockingQueue<DispatchQueue> blockingQueue){
		while(blockingQueue.size() > MAX_QUEUE_SIZE){
			DispatchQueue queue = null;
			try {
				queue = blockingQueue.take();
				if(queue == null){
					return;
				}
			} catch (InterruptedException e) {}
			if(queue.state == DispatchQueue.State.IDLE || queue.state == DispatchQueue.State.DISPOSED){
				queue.dispose();
				System.out.println("Removing Queue");
			}else{
				break;
			}
		}
		
	}
	
}
