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
import org.apache.dubbo.common.timer.Timeout;
import org.apache.dubbo.common.timer.Timer;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.support.FailbackRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * FailedNotifiedTask
 */
public final class FailedNotifiedTask extends AbstractRetryTask {

    private static final String NAME = "retry notify";

    private final NotifyListener listener;

    private final Queue<List<URL>> urlsList = new ConcurrentLinkedQueue<>();
    //private final List<URL> urls = new CopyOnWriteArrayList<>();

    public FailedNotifiedTask(Timer timer, URL url, NotifyListener listener) {
        super(timer, url, null, NAME);
        if (listener == null) {
            throw new IllegalArgumentException();
        }
        this.listener = listener;
    }

    public void addUrlToRetry(List<URL> urls) {
        if (CollectionUtils.isEmpty(urls)) {
            return;
        }
        urlsList.add(new ArrayList<>(urls));
        active();
    }

    @Override
    protected void doRetry(URL url, FailbackRegistry registry, Timeout timeout) {
        while (true) {
            List<URL> urls = urlsList.peek() ;
            if (urls == null){
                break;
            }
            listener.notify(urls);

            //一定要放在
            urlsList.remove();
        }
    }

    @Override
    protected void onFinalFailed(URL url, FailbackRegistry registry, Timeout timeout){
        urlsList.remove();
        if (urlsList.isEmpty() == false){
            //如果还有没回调的，继续
            active();
        }
    }
}
