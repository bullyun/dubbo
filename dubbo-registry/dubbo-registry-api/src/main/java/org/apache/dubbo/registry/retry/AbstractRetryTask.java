/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dubbo.registry.retry;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.common.timer.Timeout;
import org.apache.dubbo.common.timer.Timer;
import org.apache.dubbo.common.timer.TimerTask;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.registry.support.FailbackRegistry;

import java.util.concurrent.TimeUnit;

import static org.apache.dubbo.registry.Constants.DEFAULT_REGISTRY_RETRY_PERIOD;
import static org.apache.dubbo.registry.Constants.DEFAULT_REGISTRY_RETRY_TIMES;
import static org.apache.dubbo.registry.Constants.REGISTRY_RETRY_PERIOD_KEY;
import static org.apache.dubbo.registry.Constants.REGISTRY_RETRY_TIMES_KEY;

/**
 * AbstractRetryTask
 */
public abstract class AbstractRetryTask implements TimerTask {

    protected final Logger logger = LoggerFactory.getLogger(getClass());


    protected final Timer timer;

    /**
     * url for retry task
     */
    protected final URL url;

    /**
     * registry for this task
     */
    protected final FailbackRegistry registry;

    /**
     * retry period
     */
    final long retryPeriod;

    /**
     * define the most retry times
     */
    private final int retryTimes;

    /**
     * task name for this task
     */
    private final String taskName;

    /**
     * times of retry.
     * retry task is execute in single thread so that the times is not need volatile.
     */
    private int failedTimes = 0;


    private Timeout timeout = null;
    private boolean state = false;

    AbstractRetryTask(Timer timer, URL url, FailbackRegistry registry, String taskName) {
        if (timer == null || url == null || StringUtils.isBlank(taskName)) {
            throw new IllegalArgumentException();
        }
        this.timer = timer;
        this.url = url;
        this.registry = registry;
        this.taskName = taskName;
        this.retryPeriod = url.getParameter(REGISTRY_RETRY_PERIOD_KEY, DEFAULT_REGISTRY_RETRY_PERIOD);
        this.retryTimes = url.getParameter(REGISTRY_RETRY_TIMES_KEY, DEFAULT_REGISTRY_RETRY_TIMES);
    }

    public synchronized void active(){
        if (timer.isStop()){
            return;
        }
        failedTimes = 0;
        state = true;
        startTimer(false);
    }

    public synchronized void stop(){
        state = false;
        stopTimer();
    }

    public synchronized boolean isStop(){
        return state == false;
    }

    private synchronized void startTimer(boolean delay){
        if (state == false){
            return;
        }
        if (timeout == null){
            timeout = timer.newTimeout(this, delay ? retryPeriod : 0, TimeUnit.MILLISECONDS);
        }
    }

    private synchronized void stopTimer(){
        timeout.cancel();
        timeout = null;
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        synchronized (this){
            if (timeout != this.timeout){
                return;
            }
            //代表这次触发已经结束，如果有新的过来，重新启动定时器
            this.timeout = null;
            if (timeout.isCancelled() || timeout.timer().isStop() || isStop()) {
                // other thread cancel this timeout or stop the timer.
                return;
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info(taskName + " : " + url);
        }
        try {
            doRetry(url, registry, timeout);
        } catch (Throwable t) { // Ignore all the exceptions and wait for the next retry
            logger.warn("Failed to execute task " + taskName + ", url: " + url + ", waiting for again, cause:" + t.getMessage(), t);
            // reput this task when catch exception.
            failedTimes++;
            if (failedTimes < retryTimes){
                startTimer(true);
            } else {
                onFinalFailed(url, registry, timeout);
            }
        }
    }

    protected abstract void doRetry(URL url, FailbackRegistry registry, Timeout timeout);
    protected void onFinalFailed(URL url, FailbackRegistry registry, Timeout timeout){ }
}
