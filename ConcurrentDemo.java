package com.optimal.solution;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is some simple demo of using java concurrency
 * 
 */
public class ConcurrentDemo {

	// Original way: Managing Threads yourself
	private void threadExample() {
		Thread t1 = new Thread(() -> {
			System.out.println("Thread Task running in " + Thread.currentThread().getName());
		});
		System.out.println("About to start Thread");
		t1.start();
		System.out.println("Thread started");
	}

	// ExecutorService with fixed thread pool: you submit tasks and let Java manage
	// the thread pool
	private void executorThreadDemo() {
		ExecutorService executor = Executors.newFixedThreadPool(3);
		for (int i = 1; i <= 5; i++) {
			int taskId = i;
			System.out.printf("About to submit task %d in ExecutorService fixed pool\n", taskId);
			executor.submit(() -> {
				System.out.println("Executor Task " + taskId + " running in " + Thread.currentThread().getName());
			});
		}
		executor.shutdown();
	}

	// ExecutorService with Futures (blocking call)
	private void executorFuture() throws InterruptedException, ExecutionException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		System.out.println("About to submit task for Future");
		Future<String> future = executor.submit(() -> {
			Thread.sleep(2000);
			return "some stuff";
		});
		System.out.println("Doing other stuff after submitting task for future");
		String result = future.get(); // blocks until ready
		System.out.println("Result from future: " + result);
		executor.shutdown();
	}

	// CompletableFuture: no blocking
	public void completableFutureDemo() {
		System.out.println("About to supply a long task to CompletableFuture");
		CompletableFuture.supplyAsync(() -> {
			try {
				Thread.sleep(2000);
			} catch (Exception e) {
			}
			return "Data supplied to completable future";
		}).thenApply(data -> data.toUpperCase()).thenAccept(
				result -> System.out.println("Result from completableFuture with apply afterwards: " + result));
		System.out.println("Main thread continues...");
		// the following sleep is so that the program doesn't end before the above ends
		try {
			Thread.sleep(3000);
		} catch (Exception e) {
		}
	}

	// Wait for multiple CompletableFutures
	public void waitForMultipleCompletableFutures() {
		CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> "Result from Future 1");
		CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> "Result from Future 2");
		CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> "Result from Future 3");

		// Use allOf() to wait for all futures to complete
		CompletableFuture<Void> allFutures = CompletableFuture.allOf(future1, future2, future3);

		// Chain a callback to process results after all futures are done
		CompletableFuture<List<String>> allResults = allFutures.thenApply(v -> {
			return Stream.of(future1, future2, future3).map(CompletableFuture::join) // `join()` is a non-throwing
																						// version of `get()`
					.collect(Collectors.toList());
		});

		// Block and get the final list of results
		List<String> results = allResults.join();
		System.out.println("All futures completed. Results: " + results);
	}

	public static void main(String arg[]) {
		ConcurrentDemo me = new ConcurrentDemo();
		try {
			// you can comment all except the one you want to test
			me.threadExample();
			me.executorThreadDemo();
			me.executorFuture();
			me.completableFutureDemo();
			me.waitForMultipleCompletableFutures();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
