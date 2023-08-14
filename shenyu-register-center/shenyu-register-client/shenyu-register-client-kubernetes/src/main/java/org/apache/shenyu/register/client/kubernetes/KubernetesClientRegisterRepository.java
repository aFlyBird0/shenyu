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

import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ConfigMapBuilder;
import org.apache.shenyu.common.utils.ContextPathUtils;
import org.apache.shenyu.common.utils.GsonUtils;
import org.apache.shenyu.register.client.api.ShenyuClientRegisterRepository;
import org.apache.shenyu.register.common.config.ShenyuRegisterCenterConfig;
import org.apache.shenyu.register.common.dto.MetaDataRegisterDTO;
import org.apache.shenyu.register.common.dto.URIRegisterDTO;
import org.apache.shenyu.register.common.path.RegisterPathConstants;
import org.apache.shenyu.register.common.path.RegisterPathConstants.Kubernetes;
import org.apache.shenyu.spi.Join;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Properties;

/**
 * Kubernetes register repository.
 */
@Join
public class KubernetesClientRegisterRepository implements ShenyuClientRegisterRepository {

    private static final Logger LOG = LoggerFactory.getLogger(KubernetesClientRegisterRepository.class);

    @Override
    public void init(final ShenyuRegisterCenterConfig config) {
        Properties properties = config.getProps();
        final String configmapName = properties.getProperty("configmapName");

        LOG.info("configmapName: {}", configmapName);

//        KubernetesClient.initOutClusterClient();
        KubernetesClient.initInClusterClient();

    }

    @Override
    public void persistInterface(final MetaDataRegisterDTO metadata) {
        LOG.info("persistInterface: {}", metadata);

        String rpcType = metadata.getRpcType();
        String contextPath = ContextPathUtils.buildRealNode(metadata.getContextPath(), metadata.getAppName());

        registerMetadata(rpcType, contextPath, metadata);

        LOG.info("persistInterface: {} successfully", metadata);

    }

    private void registerMetadata(final String rpcType,
                                  final String contextPath,
                                  final MetaDataRegisterDTO metadata) {
        String metadataNodeName = Kubernetes.buildMetadataNodeName(metadata);
        String metaDataPath = RegisterPathConstants.buildMetaDataParentPath(rpcType, contextPath);
        String realNode = RegisterPathConstants.buildRealNode(metaDataPath, metadataNodeName);

        String configMapName = Kubernetes.buildMetaDataConfigMapName(realNode);

        LOG.info("configMapName: {}", configMapName);

        // build configmap
        V1ConfigMap configMap = new V1ConfigMapBuilder()
                .withNewMetadata()
                .withName(configMapName)
                .addToLabels(Kubernetes.CONFIGMAP_LABEL, Kubernetes.CONFIGMAP_LABEL_VALUE)
                .addToLabels(Kubernetes.REGISTER_TYPE_LABEL, Kubernetes.REGISTER_TYPE_LABEL_METADATA_VALUE)
                .addToLabels(Kubernetes.RPC_TYPE_LABEL, metadata.getRpcType())
                .addToAnnotations(Kubernetes.RULE_NAME_ANNOTATION, metadata.getRuleName())
                .addToAnnotations(Kubernetes.CONTEXT_PATH_ANNOTATION, metadata.getContextPath())
                .addToAnnotations(Kubernetes.REGISTER_METADATA_FULL_PATH_ANNOTATION, realNode)
                .endMetadata()
                .withData(Collections.singletonMap(Kubernetes.CONFIGMAP_DATA_KEY, GsonUtils.getInstance().toJson(metadata)))
                .build();

        LOG.info("configmap: {}", configMap);

        KubernetesClient.createOrUpdateConfigMap(configMapName, configMap);
        LOG.info("{} kubernetes client register metadata success: {}", rpcType, metadata);
    }

    @Override
    public void persistURI(final URIRegisterDTO registerDTO) {
        String rpcType = registerDTO.getRpcType();
        String contextPath = ContextPathUtils.buildRealNode(registerDTO.getContextPath(), registerDTO.getAppName());
        registerURI(rpcType, contextPath, registerDTO);
        LOG.info("{} kubernetes client register uri success: {}", rpcType, registerDTO);
    }

    private synchronized void registerURI(final String rpcType, final String contextPath, final URIRegisterDTO registerDTO) {
        String uriNodeName = Kubernetes.buildURINodeName(registerDTO);
        String uriPath = RegisterPathConstants.buildURIParentPath(rpcType, contextPath);
        String realNode = RegisterPathConstants.buildRealNode(uriPath, uriNodeName);
        String nodeData = GsonUtils.getInstance().toJson(registerDTO);

        String configMapName = Kubernetes.buildURIConfigMapName(realNode);

        LOG.info("configMapName: {}", configMapName);

        // build configmap
        V1ConfigMap configMap = new V1ConfigMapBuilder()
                .withNewMetadata()
                .withName(configMapName)
                .addToLabels(Kubernetes.CONFIGMAP_LABEL, Kubernetes.CONFIGMAP_LABEL_VALUE)
                .addToLabels(Kubernetes.REGISTER_TYPE_LABEL, Kubernetes.REGISTER_TYPE_LABEL_URI_VALUE)
                .addToLabels(Kubernetes.RPC_TYPE_LABEL, registerDTO.getRpcType())
                .addToAnnotations(Kubernetes.REGISTER_IP_PORT_ANNOTATION, uriNodeName)
                .addToAnnotations(Kubernetes.CONTEXT_PATH_ANNOTATION, registerDTO.getContextPath())
                .addToAnnotations(Kubernetes.REGISTER_URI_FULL_PATH_ANNOTATION, realNode)
                .endMetadata()
                .withData(Collections.singletonMap("data", nodeData))
                .build();

        LOG.info("configmap: {}", configMap);

        KubernetesClient.createOrUpdateConfigMap(configMapName, configMap);

        LOG.info("{} kubernetes client register uri success: {}", rpcType, registerDTO);
    }

    @Override
    public void offline(final URIRegisterDTO offlineDTO) {
        LOG.info("offline: {}", offlineDTO);
    }
}
