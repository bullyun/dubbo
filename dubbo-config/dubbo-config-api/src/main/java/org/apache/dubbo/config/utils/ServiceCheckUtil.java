package org.apache.dubbo.config.utils;

import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.rpc.Invoker;

/**
 * @Author: weichangyu
 * @Description:
 * @Date: Created in 14:15 2020/3/11
 * @Modified By:
 */
public class ServiceCheckUtil {

    public static final Logger logger = LoggerFactory.getLogger(ReferenceConfig.class);

    static WatchTimer timerLog = new WatchTimer();

    public static void waitProviderExport(Invoker invoker, int timeout) {
        long startTime = System.currentTimeMillis();
        while (true) {
            try {
                if (invoker.isAvailable()) {
                    return;
                }
            } catch (Exception e) {
            }

            if (timerLog.checkActive()) {
                logger.warn("Wait " + invoker.getInterface().getTypeName() + " export!");
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error("dubbo check provider service whether the available sleep exception: ", e);
            }

            if (System.currentTimeMillis() - startTime > timeout) {
                throw new IllegalStateException("wait service timeout");
            }
        }
    }

}
