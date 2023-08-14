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

package org.apache.shenyu.register.client.server.kubernetes;

import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ConfigMapList;
import io.kubernetes.client.util.CallGeneratorParams;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import org.apache.shenyu.register.common.path.RegisterPathConstants.Kubernetes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;

public class KubernetesClient {
    private static final Logger LOG = LoggerFactory.getLogger(KubernetesClient.class);

    private static final String MATCH_LABELS = Kubernetes.CONFIGMAP_LABEL + "=" + Kubernetes.CONFIGMAP_LABEL_VALUE;

    private final String namespace;

    KubernetesClient(final String namespace) {
        this.namespace = namespace;
        initInClusterClient();
//        initOutClusterClient();
    }

    private static void initInClusterClient() {
        try {
            // loading in-cluster config
            ApiClient client = ClientBuilder.cluster().build();
            Configuration.setDefaultApiClient(client);
        } catch (IOException e) {
            LOG.error("init in cluster client error: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void initOutClusterClient() {
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
     * Subscribe the updates of ConfigMap.
     *
     * @param handler the handler
     */
    public void subscribeConfigMap(final ResourceEventHandler<V1ConfigMap> handler) {

        CoreV1Api coreV1Api = new CoreV1Api();

        ApiClient apiClient = Configuration.getDefaultApiClient();

        SharedInformerFactory factory = new SharedInformerFactory(apiClient);

        SharedIndexInformer<V1ConfigMap> configMapInformer = factory.sharedIndexInformerFor(
            (CallGeneratorParams params) ->
                coreV1Api.listNamespacedConfigMapCall(
                    namespace, null,
                    null,
                    null,
                    null,
                    MATCH_LABELS,
                    Integer.valueOf(params.resourceVersion),
                    null,
                    null,
                    params.timeoutSeconds,
                    params.watch,
                    null),
            V1ConfigMap.class,
            V1ConfigMapList.class);

        configMapInformer.addEventHandler(handler);

        factory.startAllRegisteredInformers();
    }

}
