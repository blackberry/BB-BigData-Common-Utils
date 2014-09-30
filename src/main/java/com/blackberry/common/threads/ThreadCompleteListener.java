package com.blackberry.common.threads;

import com.blackberry.common.threads.NotifyingThread;

public interface ThreadCompleteListener {
	void notifyOfThreadComplete(final NotifyingThread notifyingThread, Exception e);
}