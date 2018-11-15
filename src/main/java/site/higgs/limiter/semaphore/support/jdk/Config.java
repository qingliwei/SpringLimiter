package site.higgs.limiter.semaphore.support.jdk;

import java.util.concurrent.TimeUnit;

public class Config {

    //缓存锁的容量，当内存中存在的锁实例超过该阈值时将会根据LUR清除最近最少用到的锁实例
    private int size = 2 << 20;

    //在多久没获取该锁时自动解锁
    private long duration = 300;

    // 单位
    private TimeUnit timeUnit = TimeUnit.SECONDS;

    //看门狗 多久进行一次大扫除  单位毫秒
    private long timerduration = 1800000;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public long getTimerduration() {
        return timerduration;
    }

    public void setTimerduration(long timerduration) {
        this.timerduration = timerduration;
    }
}
