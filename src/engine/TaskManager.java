package engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A task manager spawns a set of threads and manages them. Tasks can then
 * be submitted to these threads to run on. You are given no guarantees as
 * to which thread/threads the task will run on, and if you submit multiple tasks
 * as part of a single task list then it is very possible that multiple of them
 * will run concurrently.
 *
 * Each submission of a task list will give back a Counter object. You can use this
 * to either check on the status of the tasks (Counter.isComplete()), or you can use the
 * Counter.waitForCompletion() method to wait for the task list to complete. Note that this
 * is completely safe to call if you are running on a task manager thread, but if you are
 * on a different thread (ex: JavaFX thread), this can be dangerous as the task manager will
 * temporarily take over that thread until the task list completes.
 *
 * @author Justin Hall
 */
public class TaskManager {
    private final int _NUM_THREADS;
    volatile private boolean _isRunning;
    private HashMap<Thread, Worker> _workers;
    private ConcurrentLinkedQueue<TaskWrapper> _tasks;

    /**
     * A counter is a wrapper around a set of jobs and provides a way
     * to check when they have completed (counter hits 0)
     */
    public class Counter {
        private AtomicInteger _counter;
        private TaskManager _manager;

        Counter(int value, TaskManager manager) {
            _counter = new AtomicInteger(value);
            _manager = manager;
        }

        /**
         * Returns true if the counter has reached 0
         */
        public boolean isComplete() {
            return _counter.get() == 0;
        }

        /**
         * Stalls and waits for the counter to complete. This should ONLY
         * ever be called from a task manager thread, unless you are ok
         * with the thread you are calling from being taken over (temporarily)
         * while the counter completes.
         */
        public void waitForCompletion() {
            _manager._waitForTasks(this);
        }

        private void _decrement() {
            int value = _counter.getAndDecrement();
            if (value <= 0) _counter.getAndIncrement();
        }
    }

    /**
     * Stores the task-counter pairs for easy and thread-safe access.
     */
    private class TaskWrapper {
        ArrayList<Task> _tasks;
        Counter _counter;
        AtomicInteger _taskIndex;

        public TaskWrapper(ArrayList<Task> tasks, TaskManager manager) {
            _tasks = tasks;
            _counter = new Counter(tasks.size(), manager);
            _taskIndex = new AtomicInteger(0);
        }

        public Counter getCounter() {
            return _counter;
        }

        public Task getTask() {
            int index = _taskIndex.getAndIncrement();
            if (index >= _tasks.size()) return null;
            return _tasks.get(index);
        }
    }

    /**
     * A worker which can run on a thread and execute tasks from
     * the task manager's internal task queue.
     */
    private class Worker implements Runnable {
        private AtomicBoolean _isRunning;
        private TaskManager _manager;

        Worker(TaskManager manager) {
            _isRunning = new AtomicBoolean(true);
            _manager = manager;
        }

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + " has started");
            while (_isRunning.get()) {
                _manager._getAndExecuteTask();
            }
            System.out.println(Thread.currentThread().getName() + " has stopped");
        }

        public void stop() {
            _isRunning.set(false);
        }
    }

    /**
     * Main constructor for the task manager. This will NOT start the task manager,
     * but instead do initial setup. In order to start all threads associated with
     * the task manager, call the start() method.
     *
     * @param numThreads number of worker threads to spawn
     */
    public TaskManager(int numThreads) {
        _NUM_THREADS = Math.abs(numThreads);
        _isRunning = false;
        _workers = new HashMap<>();
        _tasks = new ConcurrentLinkedQueue<>();
    }

    /**
     * Starts the task manager. This should only be called once, but can be called
     * again if stop() has been called.
     */
    public void start() {
        synchronized(this) {
            if (_isRunning) return; // Already running
            _workers.clear();
            _tasks.clear();
            for (int i = 0; i < _NUM_THREADS; ++i) {
                Worker worker = new Worker(this);
                Thread thread = new Thread(worker, this + "_internal_worker#" + i);
                _workers.put(thread, worker);
                thread.start();
            }
            _isRunning = true; // Make sure to set this at the end
        }
    }

    /**
     * WARNING: This might return null if the task manager was stopped/was never started.
     *
     * Submits a task to the task pool so that one of the internal worker
     * threads can pick them up and execute them. You can use the returned
     * counter to wait for the tasks to complete, if needed.
     *
     * @param tasks tasks to be executed by the task manager's thread pool
     * @return counter that can be used to wait on the tasks to complete/check if they completed
     */
    public Counter submitTasks(ArrayList<Task> tasks) {
        if (!_isRunning) return null;
        TaskWrapper wrapper = new TaskWrapper(tasks, this);
        _tasks.add(wrapper);
        return wrapper.getCounter();
    }

    public void stop() {
        synchronized(this) {
            if (!_isRunning) return; // Not running
            for (Map.Entry<Thread, Worker> entry : _workers.entrySet()) {
                // Instruct worker to stop
                entry.getValue().stop();
            }
            _isRunning = false; // Make sure to do this at the end
        }
    }

    private TaskWrapper _getTask() {
        return _tasks.peek();
    }

    private void _waitForTasks(Counter counter) {
        while (!counter.isComplete()) {
            if (!_isRunning) return; // Task manager was shut down during the loop
            _getAndExecuteTask();
        }
    }

    private void _getAndExecuteTask() {
        Counter counter = null;
        boolean successfullyCompletedTask = false;
        try {
            TaskWrapper wrapper = _getTask();
            // If the wrapper was null, sleep for a little
            if (wrapper == null) {
                Thread.sleep(1);
                return;
            }
            Task task = wrapper.getTask();
            counter = wrapper.getCounter();
            if (task == null) {
                // The task was null, so try to remove the task wrapper that
                // resulted in the null task - make sure to check and see if
                // the one you removed is actually the one you think it is
                TaskWrapper removed = _tasks.poll();
                if (wrapper != removed && removed != null) _tasks.add(removed); // Add it back - it was falsely removed
                Thread.sleep(1);
            }
            else {
                successfullyCompletedTask = true;
                task.execute();
                counter._decrement();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            // If this is true then we technically executed the task, but it threw
            // an exception while it was running so we need to mark it completed
            if (counter != null && successfullyCompletedTask) {
                counter._decrement();
            }
        }
    }
}
