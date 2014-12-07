package javaGCDTester;

import java.util.Date;

import javaGCD.Dispatch;

public class Tester {

	static Date beginning;
	public static void main(String[] args) {
		beginning = new Date();
		
		Thread t1 = new Thread(new Runnable(){
			@Override
			public void run(){
				runTest();
			}
		});
		
		Thread t2 = new Thread(new Runnable(){
			@Override
			public void run(){
				runTest();
			}
		});
		
		t1.start();
		t2.start();
	}
	
	static void runTest(){
		for(int i = 0; i< 500; i++){
			Dispatch.async(Dispatch.getGlobalQueue(Dispatch.Priority.DEFAULT), new Runnable(){
				@Override
				public void run() {
					Date start = new Date();
					int sleep = 500;
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {}
					System.out.println("Thread id: "+Thread.currentThread().getId());
					Date finish = new Date();
					long timeSpent = (finish.getTime() - start.getTime()) - sleep;
					System.out.println("Thread "+Thread.currentThread().getId() + " took " +
							timeSpent + " to finish");
					long total = (finish.getTime() - beginning.getTime() - sleep);
					System.out.println("Program has been running for: "+total+"ms");
				}
			});
		}
	}
}
