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
package org.apache.dubbo.common.config;

import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.rpc.model.ApplicationModel;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.apache.dubbo.common.constants.CommonConstants.DEFAULT_SERVER_SHUTDOWN_TIMEOUT;
import static org.apache.dubbo.common.constants.CommonConstants.SHUTDOWN_WAIT_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.SHUTDOWN_WAIT_SECONDS_KEY;

/**
 * Utilities for manipulating configurations from different sources
 */
public class ConfigurationUtils {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationUtils.class);

    // FIXME
    @SuppressWarnings("deprecation")
    public static int getServerShutdownTimeout() {
        int timeout = DEFAULT_SERVER_SHUTDOWN_TIMEOUT;
        Configuration configuration = ApplicationModel.getEnvironment().getConfiguration();
        String value = StringUtils.trim(configuration.getString(SHUTDOWN_WAIT_KEY));

        if (value != null && value.length() > 0) {
            try {
                timeout = Integer.parseInt(value);
            } catch (Exception e) {
                // ignore
            }
        } else {
            value = StringUtils.trim(configuration.getString(SHUTDOWN_WAIT_SECONDS_KEY));
            if (value != null && value.length() > 0) {
                try {
                    timeout = Integer.parseInt(value) * 1000;
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        return timeout;
    }

    public static String getProperty(String property) {
        return getProperty(property, null);
    }

    public static String getProperty(String property, String defaultValue) {
        return StringUtils.trim(ApplicationModel.getEnvironment().getConfiguration().getString(property, defaultValue));
    }

    public static Map<String, String> parseProperties(String content) throws IOException {
        Map<String, String> map = new HashMap<>();
        if (StringUtils.isEmpty(content)) {
            logger.warn("You specified the config center, but there's not even one single config item in it.");
        } else {
            Properties properties = new Properties();
            properties.load(new StringReader(content));
            properties.stringPropertyNames().forEach(
                    k -> map.put(k, properties.getProperty(k))
            );
        }
        return map;
    }

    public static String resolvePlaceholder(String value, Configuration configuration) {
        int start = value.indexOf("${");
        int end = value.indexOf('}');
        if ((start >= 0) && (end > start)) {
            String key = value.substring(start + 2, end);
            if (StringUtils.isNotEmpty(key)){
                String value2 = (String)configuration.getProperty(key);
                value2 = resolvePlaceholder(value2, configuration);
                value = value.substring(0, start) + value2 + value.substring(end + 1);
            }
        }
        return value;
    }

    public static Map<String, String> resolvePlaceholderProperties(Map<String, String> properties, Configuration configuration) {
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            entry.setValue(resolvePlaceholder(entry.getValue(), configuration));
        }
        return properties;
    }
}
