package org.nem.nac.common;

import org.nem.nac.application.AppHost;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolExecutorFactory {
	public static ThreadPoolExecutor createDefaultExecutor() {
		final int processors = AppHost.getAvailableProcessors();
		return createExecutor(processors, Math.max(processors, 2), 1000);
	}

	public static ThreadPoolExecutor createExecutor(final int corePoolSize, final int maxPoolSize, final int keepAliveMs) {
		BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
		return new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveMs, TimeUnit.MILLISECONDS, queue);
	}
}
