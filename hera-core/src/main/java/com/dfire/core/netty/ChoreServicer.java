package com.dfire.core.netty;

public interface ChoreServicer {

    void cancelChore(ScheduledChore chore);

    void cancelChore(ScheduledChore chore, boolean mayInterruptIfRunning);

    boolean isChoreScheduled(ScheduledChore chore);

    boolean triggerNow(ScheduledChore chore);

    void shutDown();
}
