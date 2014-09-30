package com.blackberry.common.threads;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class NotifyingThread implements Runnable {
	private final Set<ThreadCompleteListener> listeners = new CopyOnWriteArraySet<ThreadCompleteListener>();

	public final void addListener(final ThreadCompleteListener listener) {
		listeners.add(listener);
	}

	public final void removeListener(final ThreadCompleteListener listener) {
		listeners.remove(listener);
	}

	private final void notifyListeners() {
		for (ThreadCompleteListener listener : listeners) {
			listener.notifyOfThreadComplete(this, null);
		}
	}

	private final void notifyListeners(Exception e) {
		for (ThreadCompleteListener listener : listeners) {
			listener.notifyOfThreadComplete(this, e);
		}
	}

	@Override
	public final void run() {
		try {
			doRun();
		} 
		catch (Exception e) {
			notifyListeners(e);
		}
		finally {
			notifyListeners();
		}
	}
	

	public abstract void doRun() throws Exception;
}