/**
 * @author jesse hsj
 * @version 1.0
 * @date 2020/4/8 10:39
 */
public interface ThreadPool<Job extends Runnable> {

    // 执行一个Job，这个Job需要实现Runnable
    void execute(Job job);
    // 关闭线程池
    void shutdown();

}
