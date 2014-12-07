package javaGCD;

import java.util.concurrent.ConcurrentLinkedQueue;

class DispatchQueue implements Runnable, Comparable<DispatchQueue>{
	
	enum State{
		IDLE,
		BUSY,
		DISPOSED
	}
	
	private String label;
	private ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<Runnable>();
	private Thread queueThread = new Thread();
	private ConcurrentLinkedQueue<Runnable> syncTrash;
	DispatchQueue.State state;

	
	public DispatchQueue(String label, int priority){
		this.label = label;
		queueThread.setName(label);
		queueThread = new Thread(this);
		queueThread.setPriority(priority);
		queueThread.start();
	}
	
	void dispatchAsync(Runnable runner){
		addRunner(runner);
	}
	private Object syncLock = new Object();;
	void dispatchSync(Runnable runner){
		Runnable wrapper = new Runnable(){
			@Override
			public void run() {
				
				runner.run();
				syncTrash.add(runner);
				syncLock.notify();
			}
		};
		addRunner(wrapper);
		try {
			while(!syncTrash.contains(runner) && state != DispatchQueue.State.DISPOSED){
				syncLock.wait();
			}
			syncTrash.remove(runner);
		} catch (InterruptedException e) {}
	}

	private synchronized void addRunner(Runnable runner){
		if(state == DispatchQueue.State.DISPOSED){
			throw new IllegalStateException();
		}
		queue.add(runner);
		synchronized(queueBlocker){
			queueBlocker.notify();
		}
		
	}
	
	private Object queueBlocker  = new Object();;
	@Override
	public void run() {
		while(state != DispatchQueue.State.DISPOSED){
			synchronized(queueBlocker){
				while(queue.size() == 0 && state != DispatchQueue.State.DISPOSED){
					try {
						this.state = DispatchQueue.State.IDLE;
						queueBlocker.wait();
					} catch (InterruptedException e) {}
				}
			}
			state = DispatchQueue.State.BUSY;
			Runnable job = queue.poll();
			if(job != null) job.run();
		}
	}
	
	int size(){
		return queue.size();
	}
	
	void dispose(){
		state = DispatchQueue.State.DISPOSED;
		synchronized(syncLock){
			syncLock.notify();
		}
		synchronized(queueBlocker){
			queueBlocker.notify();
		}
		
	}

	@Override
	public int compareTo(DispatchQueue o) {
		return this.size() - o.size();
	}
	
}
