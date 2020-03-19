package org.apache.dubbo.config.utils;

/**
 * @Author: weichangyu
 * @Description:
 * @Date: Created in 14:19 2020/3/11
 * @Modified By:
 */
public class WatchTimer {

    private int interval;
    private long lastActiveTime;

    public WatchTimer(int interval) {
        this.interval = interval;
        this.lastActiveTime = System.currentTimeMillis();
    }

    public WatchTimer() {
        this(1000);
    }

    public boolean checkActive() {
        long cur = System.currentTimeMillis();
        if ((cur - lastActiveTime) > 1000) {
            lastActiveTime = cur;
            return true;
        }
        return false;
    }

}
