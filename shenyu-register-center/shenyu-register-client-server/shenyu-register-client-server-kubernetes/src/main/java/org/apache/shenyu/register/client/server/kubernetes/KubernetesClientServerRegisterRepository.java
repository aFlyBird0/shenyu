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

import com.google.common.base.Strings;
import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import org.apache.shenyu.common.utils.GsonUtils;
import org.apache.shenyu.register.client.server.api.ShenyuClientServerRegisterPublisher;
import org.apache.shenyu.register.client.server.api.ShenyuClientServerRegisterRepository;
import org.apache.shenyu.register.common.config.ShenyuRegisterCenterConfig;
import org.apache.shenyu.register.common.dto.MetaDataRegisterDTO;
import org.apache.shenyu.register.common.dto.URIRegisterDTO;
import org.apache.shenyu.register.common.enums.EventType;
import org.apache.shenyu.register.common.path.RegisterPathConstants.Kubernetes;
import org.apache.shenyu.spi.Join;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * etcd sever register repository.
 */
@Join
public class KubernetesClientServerRegisterRepository implements ShenyuClientServerRegisterRepository {

    private static final Logger LOG = LoggerFactory.getLogger(KubernetesClientServerRegisterRepository.class);

    private ShenyuClientServerRegisterPublisher publisher;

    @Override
    public void init(final ShenyuClientServerRegisterPublisher publisher, final ShenyuRegisterCenterConfig config) {
        LOG.info("test kubernetes register repository init");
        this.publisher = publisher;
        KubernetesClient client = new KubernetesClient(Kubernetes.NAMESPACE);
        client.subscribeConfigMap(new ConfigMapListener());
        LOG.info("init kubernetes register repository");
    }

    @Override
    public void close() {
        LOG.info("close kubernetes register repository");
    }

    private void publishFromConfigMap(final V1ConfigMap cm, final K8sAction action) {
        Map<String, String> labels = Objects.requireNonNull(cm.getMetadata()).getLabels();
        if (labels == null) {
            return;
        }
        String registerType = labels.get(Kubernetes.REGISTER_TYPE_LABEL);
        if (Strings.isNullOrEmpty(registerType)) {
            return;
        }

        switch (registerType) {
            case Kubernetes.REGISTER_TYPE_LABEL_METADATA_VALUE:
                publishMetadataFromConfigMap(cm, action);
                break;
            case Kubernetes.REGISTER_TYPE_LABEL_URI_VALUE:
                publishUriFromConfigMap(cm, action);
                break;
            case Kubernetes.REGISTER_TYPE_LABEL_API_DOC_VALUE:
                break;
            default:
                LOG.warn("unknown register type: {}", registerType);
        }
    }

    private void publishMetadataFromConfigMap(final V1ConfigMap cm, final K8sAction action) {
        // do nothing when delete
        if (action == K8sAction.DELETE) {
            return;
        }
        String metaData = Objects.requireNonNull(cm.getData()).get(Kubernetes.CONFIGMAP_DATA_KEY);
        LOG.info("metadata: {}", metaData);
        MetaDataRegisterDTO dto = GsonUtils.getInstance().fromJson(metaData, MetaDataRegisterDTO.class);
        if (dto == null) {
            return;
        }
        LOG.info("publish metadata from configmap: {}", dto);
        publisher.publish(dto);
    }

    private void publishUriFromConfigMap(final V1ConfigMap cm, final K8sAction action) {
        String data = Objects.requireNonNull(cm.getData()).get(Kubernetes.CONFIGMAP_DATA_KEY);
        LOG.info("uri eventType: {}, data: {}", action, data);
        URIRegisterDTO dto = GsonUtils.getInstance().fromJson(data, URIRegisterDTO.class);
        if (dto == null) {
            return;
        }
        if (Objects.requireNonNull(action) == K8sAction.DELETE) {
            dto.setEventType(EventType.OFFLINE);
        } else {
            dto.setEventType(EventType.REGISTER);
        }
        LOG.info("publish uri from configmap: {}", dto);
        publisher.publish(dto);
    }

    public class ConfigMapListener implements ResourceEventHandler<V1ConfigMap> {

        @Override
        public void onAdd(final V1ConfigMap configMap) {
            LOG.info("onAdd configMap:{}", configMap.getMetadata().getName());
            publishFromConfigMap(configMap, K8sAction.ADD);
        }

        @Override
        public void onUpdate(final V1ConfigMap configMap, final V1ConfigMap apiType1) {
            LOG.info("onUpdate configMap:{}", configMap.getMetadata().getName());
            publishFromConfigMap(configMap, K8sAction.UPDATE);
        }

        @Override
        public void onDelete(final V1ConfigMap configMap, final boolean b) {
            LOG.info("onDelete configMap:{}", configMap.getMetadata().getName());
//            publishFromConfigMap(configMap, K8sAction.DELETE);
        }
    }

    enum K8sAction {
        ADD,
        UPDATE,
        DELETE
    }
}

