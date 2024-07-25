package name.menghw;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * 本地方法锁、共享锁、排他锁
 * @author: menghw
 * @create: 2024/7/4
 * @Description:
 */
public class MSLocalLock {

    //用于实现各方法的锁
    private final static Map<String,AtomicReference> methodAtcMap = new HashMap<>();
    //各方法上等待锁的线程
    private final static Map<String, ConcurrentLinkedQueue<Thread>> waiterMap = new ConcurrentHashMap<>();

    public static void lock(String method) {
        //从字符串常量池中获取
        method = method.intern();
        //要锁定的值
        String willLockedValue = (method + "," + Thread.currentThread().getId()).intern();

        //获取具体方法的引用
        AtomicReference methodAtc = methodAtcMap.get(method);
        //初始化methodAtcMap中具体方法的引用
        //双重检查
        if(methodAtc == null){
            synchronized (method){
                methodAtc = methodAtcMap.get(method);
                if (methodAtc == null) {
                    waiterMap.put(method,new ConcurrentLinkedQueue<>());
                    //一定要在下面
                    methodAtcMap.put(method,new AtomicReference(willLockedValue));
                    return;
                }
            }
        }
        Thread waitThread = null;
        while (true){
            //支持锁重入
            String inLockedValue = (String)methodAtc.get();
            if (willLockedValue.equals(inLockedValue)) {
                return;
            }
            //设置成功--表示加锁成功
            boolean lockSucess = methodAtc.compareAndSet(null, willLockedValue);
            if(lockSucess){
                waiterMap.get(method).remove(Thread.currentThread());
                return;
            }
            //添加等待队列和阻塞线程分两步走
            // 不然可能存在别的线程释放了锁当前线程刚执行到这里添加到等待队列后阻塞住了
            if(waitThread == null){
                waitThread = Thread.currentThread();
                waiterMap.get(method).offer(waitThread);
            }else{
                LockSupport.park();
            }
        }
    }

    /**
     * 释放方法上的锁
     * @param method
     */
    public static void unLock(String method){
        //要解锁的值
        String lockedValue = (method + "," + Thread.currentThread().getId()).intern();
        //释放method上的锁
        if (methodAtcMap.containsKey(method) &&
                methodAtcMap.get(method).compareAndSet(lockedValue, null)) {
            //唤醒阻塞的线程
            if (waiterMap.containsKey(method)) {
                for (Thread thread : waiterMap.get(method)) {
                    LockSupport.unpark(thread);
                }
            }
        }
    }
//----------------------------------共享锁和排他锁---------------------------------------
    static final AtomicInteger atomicInteger = new AtomicInteger(0);
    static final ConcurrentLinkedQueue<Thread> SQueue = new ConcurrentLinkedQueue();
    static final ConcurrentLinkedQueue<Thread> XQueue = new ConcurrentLinkedQueue();

    //窗口长度
    final static int WINDOW_SIZE = 5;
    //每个窗口单位1s
    final static int WINDOW_UNIT_MILL = 1*1000;
    static  int[] window_s = new int[WINDOW_SIZE];
    static  int[] window_x = new int[WINDOW_SIZE];

    //最后一个窗口的开始时间
    static  long s_last_window_start_time = -1;
    //最后一个窗口的开始时间
    static  long x_last_window_start_time = -1;

    public static void lock_S() {
        Thread currentThread = null;
        for(;;){
            if(XQueue.size() > 0 && chooseX()){
                if(currentThread == null){
                    currentThread = Thread.currentThread();
                    SQueue.offer(Thread.currentThread());
                }else {
                    Thread peeked = XQueue.peek();
                    if(peeked != null){
                        LockSupport.unpark(peeked);
                    }
                    LockSupport.park();
                }
                continue;
            }
            int i = atomicInteger.get();
            //不存在排他锁
            if(i >= 0){
                boolean b = atomicInteger.compareAndSet(i, i+1);
                if(b){
                    logS();
                    SQueue.remove(Thread.currentThread());
                    return;
                }
            }else {
                //存在排他锁
                if (currentThread == null){
                    currentThread = Thread.currentThread();
                    SQueue.offer(currentThread);
                }else{
                    LockSupport.park();
                }
            }
        }
    }
    public static void unLock_S()  {
        for(;;){
            int i = atomicInteger.get();

            if(i == 0){
                return;
            }
            //释放一个共享锁
            boolean b = atomicInteger.compareAndSet(i, i-1);
            if(b){
                //表示锁都释放完了
                if (i-1 == 0 && XQueue.size() > 0){
                    Thread peeked = XQueue.peek();
                    if(peeked != null){
                        LockSupport.unpark(peeked);
                    }
                }
                return;
            }
        }
    }
    static int spinCountX = 0;
    public static void lock_X() throws InterruptedException {
        Thread currentThread = null;
        for(;;){
            //尝试加排他锁
            boolean b = atomicInteger.compareAndSet(0, -1);
            if(b){
                logX();
                XQueue.remove(Thread.currentThread());
                return;
            }

            if(spinCountX >= 50){
                if (currentThread == null){
                    currentThread = Thread.currentThread();
                    XQueue.offer(currentThread);
                }else{
                    LockSupport.park();
                }
            }
            spinCountX++;
            Thread.sleep(50);
        }
    }
    public static void unLock_X()  {
        atomicInteger.set(0);
        //优先唤醒一个写阻塞
        if (XQueue.size() > 0 && chooseX()) {
            Thread peeked = XQueue.peek();
            if(peeked != null){
                LockSupport.unpark(peeked);
            }
        }else{  //唤醒要加共享锁时阻塞线程
            for (Thread thread : SQueue) {
                LockSupport.unpark(thread);
            }
        }
    }

    private static void logS(){
        //第一次访问
        if (s_last_window_start_time == -1) {
            s_last_window_start_time = System.currentTimeMillis();
        }else{
            //距离第一次访问的时间差
            int passedUnit = (int) ((System.currentTimeMillis()
                    - s_last_window_start_time)
                    / WINDOW_UNIT_MILL);
            //需要滑动
            if(passedUnit >= WINDOW_SIZE){
                s_last_window_start_time = System.currentTimeMillis();
                window_s = new int[5];
            }else if(passedUnit > 0){
                int j = passedUnit;
                for (int i = 0; i < WINDOW_SIZE; i++,j++) {
                    if(j < WINDOW_SIZE){
                        window_s[i] = window_s[j];
                    }else{
                        window_s[i] = 0;
                    }
                }
                //滑动
                s_last_window_start_time = s_last_window_start_time + passedUnit * WINDOW_UNIT_MILL;
            }
        }
        window_s[WINDOW_SIZE -1]++;
    }
    private static void logX(){
        //第一次访问
        if (x_last_window_start_time == -1) {
            x_last_window_start_time = System.currentTimeMillis();
        }else{
            //距离第一次访问的时间差
            int passedUnit = (int) ((System.currentTimeMillis()
                    - x_last_window_start_time)
                    / WINDOW_UNIT_MILL);
            //需要滑动
            if(passedUnit >= WINDOW_SIZE){
                x_last_window_start_time = System.currentTimeMillis();
                window_x = new int[5];
            }else if(passedUnit > 0){
                int j = passedUnit;
                for (int i = 0; i < WINDOW_SIZE; i++,j++) {
                    if(j < WINDOW_SIZE){
                        window_x[i] = window_x[j];
                    }else{
                        window_x[i] = 0;
                    }
                }
                //滑动
                x_last_window_start_time = x_last_window_start_time + passedUnit*WINDOW_UNIT_MILL;
            }
        }
        window_x[WINDOW_SIZE -1]++;
    }
    private static boolean chooseX(){
        int totCountS = 0;
        for (int i = 0; i < WINDOW_SIZE; i++) {
            totCountS += window_s[i];
        }
        int totCountX = 0;
        for (int i = 0; i < WINDOW_SIZE; i++) {
            totCountX += window_x[i];
        }
        double rateX = ((double)totCountX)/totCountS;
        if (rateX < 0.25) {
            return true;
        }
        return false;
    }
}
