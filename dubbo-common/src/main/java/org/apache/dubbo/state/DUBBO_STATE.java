package org.apache.dubbo.state;

public enum DUBBO_STATE {
    ZERO(0),
    INITED(1),
    STARTED(2),
    DESTROY(3);

    private int state = 0;

    DUBBO_STATE(int state) {
        this.state = state;
    }

    public int value() {
        return state;
    }

    public static DUBBO_STATE valueOf(int state) {
        switch (state) {
            case 0:return ZERO;
            case 1:return INITED;
            case 2:return STARTED;
            case 3:return DESTROY;
            default:return ZERO;
        }
    }
}
