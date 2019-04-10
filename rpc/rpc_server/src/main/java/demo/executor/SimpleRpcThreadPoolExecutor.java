package demo.executor;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

/*
    cread by xifoo on 2019-4-10
 */

@Component
public class SimpleRpcThreadPoolExecutor implements Executor {

    private volatile boolean ISRUNNING = true;
    private BlockingQueue<Runnable> taskQueue = null;
    private HashSet<Worker> rpcworks = new HashSet<>();
    private List<Thread> threads = new ArrayList<>();

    int poolSize = 20;
    int coreSize = 0;

    public SimpleRpcThreadPoolExecutor(){
        taskQueue = new LinkedBlockingQueue<Runnable>(poolSize);
    }
    public SimpleRpcThreadPoolExecutor(int poolSize){
        this.poolSize = poolSize;
        taskQueue = new LinkedBlockingQueue<Runnable>(poolSize);
    }
    @Override
    public synchronized void execute(Runnable runnable) {
        if(runnable==null)
            throw new NullPointerException("thread is null");
        if(!this.ISRUNNING)
            throw new RuntimeException("ThreadPool is stopped");
        if(coreSize<poolSize){
            addThread(runnable);
        }else {
            try {
                taskQueue.put(runnable);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }
    private void addThread(Runnable runnable) {
        coreSize++;
        Worker work = new Worker(runnable);
        rpcworks.add(work);
        Thread t =new Thread(work);
        threads.add(t);
        try {
            t.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void close(){
        ISRUNNING = false;
        if(!rpcworks.isEmpty()){
            for(Worker work:rpcworks){
                work.interrupt();
            }
        }
        Thread.currentThread().interrupt();
    }
    /*
    内部类Worker，这个内部类Worker是用来执行每个任务的，在创建线程池后，往线程里添加任务，每个任务都是由Worker一个一个来启动的
     */
    class Worker implements Runnable{
        public Worker(Runnable runnable) {
            taskQueue.offer(runnable);
        }

        @Override
        public void run() {
            while(true&&ISRUNNING){
                SimpleRpcTask task = null;
                try {
                    task = (SimpleRpcTask) getTask();
                    task.run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }

        private Runnable getTask() throws InterruptedException {
            return taskQueue.take();
        }

        public void interrupt() {
            for(Thread thread:threads){
                thread.interrupt();
            }
        }
    }

}
