package top.grayson.util.concurrent.threadpool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/20 10:43
 * @Description
 */
@Slf4j
@NoArgsConstructor
public final class ThreadPoolFactoryUtils {
    /**
     * 通过 threadNamePrefix 来区分不同线程池（我们可以把相同 threadNamePrefix 的线程池看作是为同一业务场景服务）
     * key: threadNamePrefix
     * value: threadPool
     */
    private static final Map<String, ExecutorService> THREAD_POOLS = new ConcurrentHashMap<>();

    /**
     * 创建一个自定义的线程池
     *
     * @param threadNamePrefix 线程名称前缀
     * @return 自定义的线程池
     */
    public static ExecutorService createCustomThreadPoolIfAbsent(String threadNamePrefix) {
        CustomThreadPoolConfig customThreadPoolConfig = new CustomThreadPoolConfig();
        return createCustomThreadPoolIfAbsent(customThreadPoolConfig, threadNamePrefix, false);
    }

    /**
     * 创建一个自定义的线程池（默认不是守护线程）
     *
     * @param customThreadPoolConfig 自定义线程池配置
     * @param threadNamePrefix       线程名称前缀
     * @return 自定义的线程池
     */
    private static ExecutorService createCustomThreadPoolIfAbsent(CustomThreadPoolConfig customThreadPoolConfig, String threadNamePrefix) {
        return createCustomThreadPoolIfAbsent(customThreadPoolConfig, threadNamePrefix, false);
    }

    /**
     * 创建一个自定义的线程池
     *
     * @param customThreadPoolConfig 自定义线程池配置
     * @param threadNamePrefix       线程名称前缀
     * @param daemon                 是否为守护进程
     * @return 自定义的线程池
     */
    private static ExecutorService createCustomThreadPoolIfAbsent(CustomThreadPoolConfig customThreadPoolConfig, String threadNamePrefix, boolean daemon) {
        ExecutorService threadPool = THREAD_POOLS.computeIfAbsent(threadNamePrefix, k -> createThreadPool(customThreadPoolConfig, threadNamePrefix, daemon));
        //  如果 threadPool 被 shutdown 的话就重新创建一个
        if (threadPool.isShutdown() || threadPool.isTerminated()) {
            THREAD_POOLS.remove(threadNamePrefix);
            threadPool = createThreadPool(customThreadPoolConfig, threadNamePrefix, daemon);
            THREAD_POOLS.put(threadNamePrefix, threadPool);
        }
        return threadPool;
    }

    /**
     * 创建一个线程池
     *
     * @param customThreadPoolConfig 自定义线程池配置
     * @param threadNamePrefix       线程名称前缀
     * @param daemon                 是否为守护进程
     * @return 线程池
     */
    private static ExecutorService createThreadPool(CustomThreadPoolConfig customThreadPoolConfig, String threadNamePrefix, boolean daemon) {
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefix, daemon);
        return new ThreadPoolExecutor(
                customThreadPoolConfig.getCorePoolSize(),
                customThreadPoolConfig.getMaximumPoolSize(),
                customThreadPoolConfig.getKeepAliveTime(),
                customThreadPoolConfig.getTimeUnit(),
                customThreadPoolConfig.getWorkQueue(),
                threadFactory
        );
    }

    /**
     * 创建线程池工厂，如果 threadNamePrefix 不为空则使用自建的线程池工厂，否则使用默认的线程池工厂
     *
     * @param threadNamePrefix 线程名称前缀
     * @param daemon           是否为守护进程
     * @return 线程池工厂
     */
    public static ThreadFactory createThreadFactory(String threadNamePrefix, Boolean daemon) {
        if (threadNamePrefix != null) {
            if (daemon != null) {
                return new ThreadFactoryBuilder()
                        .setNameFormat(threadNamePrefix + "-%d")
                        .setDaemon(daemon).build();
            } else {
                return new ThreadFactoryBuilder()
                        .setNameFormat(threadNamePrefix + "-%d").build();
            }
        }
        return Executors.defaultThreadFactory();
    }

    /**
     * 停止所有线程池
     */
    public static void shutDownAllThreadPool() {
        log.info("Start to shutdown all thread pool...");
        THREAD_POOLS.entrySet()
                .parallelStream()
                .forEach((entry -> {
                    ExecutorService executorService = entry.getValue();
                    executorService.shutdown();
                    log.info("Shut down thread pool [{}] [{}]", entry.getKey(), executorService.isTerminated());
                    try {
                        executorService.awaitTermination(10, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        log.error("Thread pool never terminated, we will shut down it right now.");
                        executorService.shutdownNow();
                    }
                }));
    }

    /**
     * 打印线程池的状态
     *
     * @param threadPool 线程池对象
     */
    public static void printThreadPoolStatus(ThreadPoolExecutor threadPool) {
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1, createThreadFactory("print-thread-pool-status", false));
        scheduledThreadPoolExecutor.scheduleAtFixedRate(() -> {
            log.info("============ThreadPool Status=============");
            log.info("ThreadPool Size: [{}]", threadPool.getPoolSize());
            log.info("Active Threads: [{}]", threadPool.getActiveCount());
            log.info("Number of Tasks : [{}]", threadPool.getCompletedTaskCount());
            log.info("Number of Tasks in Queue: {}", threadPool.getQueue().size());
            log.info("===========================================");
        }, 0, 1, TimeUnit.SECONDS);
    }
}
