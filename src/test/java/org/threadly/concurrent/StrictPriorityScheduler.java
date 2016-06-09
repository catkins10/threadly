package org.threadly.concurrent;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadFactory;

import org.threadly.concurrent.collections.ConcurrentArrayList;

/**
 * In order to avoid a performance hit by verifying state which would indicate a programmer 
 * error at runtime.  This class functions to verify those little things during unit tests.  
 * For that reason this class extends {@link PriorityScheduler} to do additional 
 * functions, but calls into the super functions to verify the actual behavior. 
 * 
 * @author jent - Mike Jensen
 */
public class StrictPriorityScheduler extends PriorityScheduler {
  /**
   * Constructs a new thread pool, though no threads will be started till it accepts it's first 
   * request.  This constructs a default priority of high (which makes sense for most use cases).  
   * It also defaults low priority worker wait as 500ms.  It also  defaults to all newly created 
   * threads being daemon threads.
   * 
   * @param poolSize Thread pool size that should be maintained
   */
  public StrictPriorityScheduler(int poolSize) {
    this(poolSize, null, DEFAULT_LOW_PRIORITY_MAX_WAIT_IN_MS, DEFAULT_NEW_THREADS_DAEMON);
  }
  
  /**
   * Constructs a new thread pool, though no threads will be started till it accepts it's first 
   * request.  This constructs a default priority of high (which makes sense for most use cases).  
   * It also defaults low priority worker wait as 500ms.
   * 
   * @param poolSize Thread pool size that should be maintained
   * @param useDaemonThreads {@code true} if newly created threads should be daemon
   */
  public StrictPriorityScheduler(int poolSize, boolean useDaemonThreads) {
    this(poolSize, null, DEFAULT_LOW_PRIORITY_MAX_WAIT_IN_MS, useDaemonThreads);
  }

  /**
   * Constructs a new thread pool, though no threads will be started till it accepts it's first 
   * request.  This provides the extra parameters to tune what tasks submitted without a priority 
   * will be scheduled as.  As well as the maximum wait for low priority tasks.  The longer low 
   * priority tasks wait for a worker, the less chance they will have to create a thread.  But it 
   * also makes low priority tasks execution time less predictable.
   * 
   * @param poolSize Thread pool size that should be maintained
   * @param defaultPriority priority to give tasks which do not specify it
   * @param maxWaitForLowPriorityInMs time low priority tasks wait for a worker
   */
  public StrictPriorityScheduler(int poolSize, TaskPriority defaultPriority, 
                                 long maxWaitForLowPriorityInMs) {
    this(poolSize, defaultPriority, maxWaitForLowPriorityInMs, DEFAULT_NEW_THREADS_DAEMON);
  }

  /**
   * Constructs a new thread pool, though no threads will be started till it accepts it's first 
   * request.  This provides the extra parameters to tune what tasks submitted without a priority 
   * will be scheduled as.  As well as the maximum wait for low priority tasks.  The longer low 
   * priority tasks wait for a worker, the less chance they will have to create a thread.  But it 
   * also makes low priority tasks execution time less predictable.
   * 
   * @param poolSize Thread pool size that should be maintained
   * @param defaultPriority priority to give tasks which do not specify it
   * @param maxWaitForLowPriorityInMs time low priority tasks wait for a worker
   * @param useDaemonThreads {@code true} if newly created threads should be daemon
   */
  public StrictPriorityScheduler(int poolSize, TaskPriority defaultPriority, 
                                 long maxWaitForLowPriorityInMs, 
                                 boolean useDaemonThreads) {
    this(poolSize, defaultPriority, maxWaitForLowPriorityInMs, 
         new ConfigurableThreadFactory(PriorityScheduler.class.getSimpleName() + "-", 
                                       true, useDaemonThreads, Thread.NORM_PRIORITY, null, null));
  }

  /**
   * Constructs a new thread pool, though no threads will be started till it accepts it's first 
   * request.  This provides the extra parameters to tune what tasks submitted without a priority 
   * will be scheduled as.  As well as the maximum wait for low priority tasks.  The longer low 
   * priority tasks wait for a worker, the less chance they will have to create a thread.  But it 
   * also makes low priority tasks execution time less predictable.
   * 
   * @param poolSize Thread pool size that should be maintained
   * @param defaultPriority priority to give tasks which do not specify it
   * @param maxWaitForLowPriorityInMs time low priority tasks wait for a worker
   * @param threadFactory thread factory for producing new threads within executor
   */
  public StrictPriorityScheduler(int poolSize, TaskPriority defaultPriority, 
                                 long maxWaitForLowPriorityInMs, ThreadFactory threadFactory) {
    super(new WorkerPool(threadFactory, poolSize), defaultPriority, maxWaitForLowPriorityInMs);
  }
  
  private static void verifyOneTimeTaskQueueSet(QueueSet queueSet, OneTimeTaskWrapper task) {
    if (task.taskQueue instanceof ConcurrentLinkedQueue) {
      if (queueSet.executeQueue != task.taskQueue) {
        throw new IllegalStateException("Queue missmatch");
      }
    } else if (task.taskQueue instanceof ConcurrentArrayList) {
      if (queueSet.scheduleQueue != task.taskQueue) {
        throw new IllegalStateException("Queue missmatch");
      }
    } else if (task.taskQueue != null) {
      throw new UnsupportedOperationException("Unhandled queue type");
    }
  }
  
  @Override
  protected void addToExecuteQueue(QueueSet queueSet, OneTimeTaskWrapper task) {
    verifyOneTimeTaskQueueSet(queueSet, task);
    
    super.addToExecuteQueue(queueSet, task);
  }
  
  @Override
  protected void addToScheduleQueue(QueueSet queueSet, TaskWrapper task) {
    if (task instanceof OneTimeTaskWrapper) {
      verifyOneTimeTaskQueueSet(queueSet, (OneTimeTaskWrapper)task);
    } else if (task instanceof RecurringTaskWrapper) {
      if (queueSet != ((RecurringTaskWrapper)task).queueSet) {
        throw new IllegalStateException("QueueSet mismatch");
      }
    } else {
      throw new UnsupportedOperationException("Unhandled task type");
    }
    
    super.addToScheduleQueue(queueSet, task);
  }
}
