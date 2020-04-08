import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author jesse hsj
 * @version 1.0
 * @date 2020/4/8 10:56
 * @noinspection ALL
 */
@SuppressWarnings("all")
public class MyThreadPool<Job extends Runnable> implements ThreadPool<Job> {


    // 线程池最大数
    private static final int MAX_WORKER_NUMBERS = 10;
    // 线程池默认的数量
    private static final int DEFAULT_WORKER_NUMBERS = 5;
    // 这是一个工作列表，将会向里面插入工作
    private final LinkedList<Job> jobs = new LinkedList<Job>();
    // 工作者列表
    private final List<Worker> workers = Collections.synchronizedList(new
            ArrayList<Worker>());
    // 工作者线程的数量
    private int workerNum = DEFAULT_WORKER_NUMBERS;
    // 线程编号生成
    private AtomicLong threadNum = new AtomicLong();
    public MyThreadPool() {
        initializeWokers(DEFAULT_WORKER_NUMBERS);
    }
    public MyThreadPool(int num) {
        workerNum = num > MAX_WORKER_NUMBERS ? MAX_WORKER_NUMBERS : num ;
        initializeWokers(workerNum);
    }
    @Override
    public void execute(Job job) {
        if (job != null) {
            // 添加一个工作，然后进行通知
            synchronized (jobs) {
                jobs.addLast(job);
                jobs.notify();
            }
        }
    }
    @Override
    public void shutdown() {
        for (Worker worker : workers) {
            worker.shutdown();
        }
    }

    // 初始化
    private void initializeWokers(int num) {
        for (int i = 0; i < num; i++) {
            Worker worker = new Worker();
            workers.add(worker);
            Thread thread = new Thread(worker, "ThreadPool-Worker-" + threadNum.
                    incrementAndGet());
            thread.start();
        }
    }
    // Job
    class Worker implements Runnable {
        // 是否工作 这里通过volatile变量来控制程序的中断，而不是使用interupt()
        private volatile boolean running = true;
        @Override
        public void run() {
            while (running) {
                Job job = null;
                synchronized (jobs) {
                    // 如果工作者列表是空的，那么就wait
                    while (jobs.isEmpty()) {
                        try {
                            //调用wait() 会释放jobs的锁
                            jobs.wait();
                        } catch (InterruptedException ex) {
                            //  线程处于被阻塞状态（例如处于sleep, wait, join 等状态），此时被调用interupt(),JVM会使该线程离开阻塞状态，并抛出一个异常并抛出一个InterruptedException异常
                            // 感知到外部对WorkerThread的中断操作，返回
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    // 取出一个Job
                    job = jobs.removeFirst();
                }
                if (job != null) {
                    try {
                        job.run();
                    } catch (Exception ex) {
                        // 忽略Job执行中的Exception
                    }
                }
            }
        }
        public void shutdown() {
            running = false;
        }
    }

}
