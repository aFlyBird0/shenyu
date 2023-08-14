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

package org.apache.shenyu.register.client.kubernetes;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;

import java.io.FileReader;
import java.io.IOException;

import org.apache.shenyu.register.common.path.RegisterPathConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KubernetesClient {

    private static final Logger LOG = LoggerFactory.getLogger(KubernetesClient.class);

    /**
     * Init in-cluster client(when running in kubernetes).
     */
    public static void initInClusterClient() {
        try {
            // loading in-cluster config
            ApiClient client = ClientBuilder.cluster().build();
            Configuration.setDefaultApiClient(client);
        } catch (IOException e) {
            LOG.error("init in cluster client error: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Init out-cluster client(when running not in kubernetes, such as local test).
     */
    public static void initOutClusterClient() {
        try {
            // file path to kubeconfig
            String kubeConfigPath = System.getenv("HOME") + "/.kube/config";
            ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
            Configuration.setDefaultApiClient(client);
        } catch (IOException e) {
            LOG.error("init out cluster client error: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Create or update configmap.
     *
     * @param configMapName configmap name
     * @param configMap     configmap
     */
    public static void createOrUpdateConfigMap(final String configMapName, final V1ConfigMap configMap) {
        CoreV1Api api = new CoreV1Api();

        // check if configmap exist
        boolean isExist = false;
        try {
            V1ConfigMap configMapExist = api.readNamespacedConfigMap(configMapName, RegisterPathConstants.Kubernetes.NAMESPACE, null);
            if (configMapExist != null) {
                isExist = true;
            }
        } catch (ApiException e) {
            if (e.getCode() != 404) {
                LOG.error("ApiException in read configmap: {}, {}", e.getCode(), e.getMessage());
                throw new RuntimeException(e);
            }
        }

        // if configmap not exist, create it; else update it
        try {
            if (isExist) {
                LOG.debug("configmap: {} already exist, replace it", configMapName);
                api.replaceNamespacedConfigMap(configMapName, RegisterPathConstants.Kubernetes.NAMESPACE, configMap, null, null, null, null);
            } else {
                LOG.debug("configmap: {} not exist, create it", configMapName);
                api.createNamespacedConfigMap(RegisterPathConstants.Kubernetes.NAMESPACE, configMap, null, null, null, null);
            }
        } catch (ApiException e) {
            LOG.error("ApiException in create/replace configmap: {}, {}", e.getCode(), e.getMessage());
        }

    }
}
