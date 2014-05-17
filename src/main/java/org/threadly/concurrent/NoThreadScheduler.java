package org.threadly.concurrent;

/**
 * <p>Executor which has no threads itself.  This allows you to have the same 
 * scheduler abilities (schedule tasks, recurring tasks, etc, etc), without having 
 * to deal with multiple threads, memory barriers, or other similar concerns.  
 * This class can be very useful in GUI development (if you want it to run on the GUI 
 * thread).  It also can be useful in android development in a very similar way.</p>
 * 
 * <p>The tasks in this scheduler are only progressed forward with calls to .tick().  
 * Since it is running on the calling thread, calls to .wait() and .sleep() from sub 
 * tasks will block (possibly forever).  The call to .tick() will not unblock till there 
 * is no more work for the scheduler to currently handle.</p>
 * 
 * @author jent - Mike Jensen
 * @since 2.0.0
 */
public class NoThreadScheduler extends AbstractTickableScheduler {
  private final ClockWrapper clockWrapper;
  
  /**
   * Constructs a new {@link NoThreadScheduler} scheduler.
   * 
   * @param tickBlocksTillAvailable true if calls to .tick() should block till there is something to run
   */
  public NoThreadScheduler(boolean tickBlocksTillAvailable) {
    super(tickBlocksTillAvailable);
    
    clockWrapper = new ClockWrapper();
  }

  @Override
  protected long nowInMillis() {
    return clockWrapper.getSemiAccurateTime();
  }
  
  @Override
  protected void startInsertion() {
    clockWrapper.stopForcingUpdate();
  }
  
  @Override
  protected void endInsertion() {
    clockWrapper.resumeForcingUpdate();
  }
  
  /**
   * Progresses tasks for the current time.  This will block as it runs
   * as many scheduled or waiting tasks as possible.  It is CRITICAL that 
   * only one thread at a time calls the .tick() function.  While this class 
   * is in general thread safe, if multiple threads call .tick() at the same 
   * time, it is possible a given task may run more than once.  In order to 
   * maintain high performance, threadly does not guard against this condition.
   * 
   * Depending on how this class was constructed, this may or may not block 
   * if there are no tasks to run yet.
   * 
   * If any tasks throw a RuntimeException, they will be bubbled up to this 
   * tick call.  Any tasks past that task will not run till the next call to 
   * tick.  So it is important that the implementor handle those exceptions.  
   * 
   * @return qty of steps taken forward.  Returns zero if no events to run.
   * @throws InterruptedException thrown if thread is interrupted waiting for task to run
   *           (this can only throw if constructed with a true to allow blocking)
   */
  public int tick() throws InterruptedException {
    return super.tick();
  }
}
