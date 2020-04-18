package org.apache.dubbo.state;


import java.util.concurrent.atomic.AtomicInteger;

public class DubboBootstrapStatus {

    private static AtomicInteger state = new AtomicInteger(0);

    public static void setState(DUBBO_STATE state) {
        DubboBootstrapStatus.state.getAndSet(state.value());
    }

    public static DUBBO_STATE getState() {
        return DUBBO_STATE.valueOf(DubboBootstrapStatus.state.get());
    }

}
