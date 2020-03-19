package org.apache.dubbo.config.utils;

import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.rpc.service.EchoService;

/**
 * @Author: weichangyu
 * @Description:
 * @Date: Created in 14:15 2020/3/11
 * @Modified By:
 */
public class ServiceCheckUtil {

    public static final Logger logger = LoggerFactory.getLogger(ReferenceConfig.class);
    /**
     * wait service real available timeout 3 minutes
     */
    private static final int TIMEOUT = 180000;

    static WatchTimer timerLog = new WatchTimer();

    public static <T> T waitProviderExport(T providerService) {
        long startTime = System.currentTimeMillis();
        while (true) {
            try {
                // 强制转型为EchoService
                EchoService echoService = (EchoService) providerService;
                // 回声测试可用性
                String str = (String)echoService.$echo("OK");
                if (str.equals("OK")) {
                    return providerService;
                }
            } catch (Exception e) {
            }

            if (timerLog.checkActive()) {
                logger.warn("Wait " + providerService.getClass().getName() + " export!");
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error("dubbo check provider service whether the available sleep exception: ", e);
            }

            if (System.currentTimeMillis() - startTime > TIMEOUT) {
                throw new IllegalStateException("wait service timeout");
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        long time = System.currentTimeMillis();
        System.out.println(time);
        Thread.sleep(180000);
        long time2 = System.currentTimeMillis();
        System.out.println(time2 - time);
    }

}
