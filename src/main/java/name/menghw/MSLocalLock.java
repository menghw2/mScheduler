package name.menghw;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * 本地锁
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
}
