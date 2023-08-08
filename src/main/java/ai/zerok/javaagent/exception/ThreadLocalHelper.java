package ai.zerok.javaagent.exception;


import ai.zerok.javaagent.utils.LRUCache;

public class ThreadLocalHelper {
    public static final ThreadLocalHelper threadLocalHelper = new ThreadLocalHelper();
    //TODO: Figure out a better limite for this LRU Map
    private static final ThreadLocal<LRUCache> threadLocalSet = ThreadLocal.withInitial(() -> new LRUCache(5000));

    public static ThreadLocalHelper getInstance(){
        return threadLocalHelper;
    }

    public boolean contains(int key){
        return threadLocalSet.get().get(key + "") != null;
    }

    public void add(int key){
        //Parse
        threadLocalSet.get().put(key + "", key + "");
    }

}
