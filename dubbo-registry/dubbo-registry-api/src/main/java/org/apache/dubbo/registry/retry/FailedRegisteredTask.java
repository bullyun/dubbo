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
import org.apache.dubbo.registry.support.FailbackRegistry;

/**
 * FailedRegisteredTask
 */
public final class FailedRegisteredTask extends AbstractRetryTask {

    private static final String NAME = "retry register";

    public FailedRegisteredTask(Timer timer, URL url, FailbackRegistry registry) {
        super(timer, url, registry, NAME);
    }

    @Override
    protected void doRetry(URL url, FailbackRegistry registry, Timeout timeout) {
        registry.doRegister(url);

        //对象长期保留，避免对象管理的多线程问题
    }

    @Override
    protected void onFinalFailed(URL url, FailbackRegistry registry, Timeout timeout) {

    }
}
