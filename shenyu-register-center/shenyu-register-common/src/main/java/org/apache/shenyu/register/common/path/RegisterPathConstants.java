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

package org.apache.shenyu.register.common.path;

import org.apache.commons.codec.binary.Hex;
import org.apache.shenyu.common.constant.Constants;
import org.apache.shenyu.common.constant.DefaultPathConstants;
import org.apache.shenyu.common.enums.RpcTypeEnum;
import org.apache.shenyu.register.common.dto.MetaDataRegisterDTO;
import org.apache.shenyu.register.common.dto.URIRegisterDTO;

import static org.apache.shenyu.common.constant.Constants.PATH_SEPARATOR;

/**
 *  register center path constants.
 */
public class RegisterPathConstants {

    /**
     * uri register path pattern.
     * e.g. /shenyu/register/uri/{rpcType}/{context}/{urlInstance}
     */
    public static final String REGISTER_URI_INSTANCE_PATH = "/shenyu/register/uri/*/*/*";

    /**
     * metadata register path pattern.
     * e.g. /shenyu/register/metadata/{rpcType}/{context}/{metadata}
     */
    public static final String REGISTER_METADATA_INSTANCE_PATH = "/shenyu/register/metadata/*/*/*";

    /**
     * root path of  register center.
     */
    public static final String ROOT_PATH = "/shenyu/register";

    /**
     * root path of uri register.
     */
    public static final String REGISTER_URI_INSTANCE_ROOT_PATH = ROOT_PATH + "/uri";

    /**
     * root path of metadata register.
     */
    public static final String REGISTER_METADATA_INSTANCE_ROOT_PATH = ROOT_PATH + "/metadata";

    /**
     * constants of separator.
     */
    private static final String SEPARATOR = "/";

    /**
     * Dot separator.
     */
    private static final String DOT_SEPARATOR = ".";

    /**
     * build child path of "/shenyu/register/metadata/{rpcType}/".
     *
     * @param rpcType rpc type
     * @return path string
     */
    public static String buildMetaDataContextPathParent(final String rpcType) {
        return String.join(SEPARATOR, REGISTER_METADATA_INSTANCE_ROOT_PATH, rpcType);
    }
    
    /**
     * build child path of "/shenyu/register/metadata/{rpcType}/{contextPath}/".
     *
     * @param rpcType rpc type
     * @param contextPath context path
     * @return path string
     */
    public static String buildMetaDataParentPath(final String rpcType, final String contextPath) {
        return String.join(SEPARATOR, REGISTER_METADATA_INSTANCE_ROOT_PATH, rpcType, contextPath);
    }
    
    /**
     * Build uri path string.
     * build child path of "/shenyu/register/uri/{rpcType}/".
     *
     * @param rpcType the rpc type
     * @return the string
     */
    public static String buildURIContextPathParent(final String rpcType) {
        return String.join(SEPARATOR, REGISTER_URI_INSTANCE_ROOT_PATH, rpcType);
    }
    
    /**
     * Build uri path string.
     * build child path of "/shenyu/register/uri/{rpcType}/{contextPath}/".
     *
     * @param rpcType the rpc type
     * @param contextPath the context path
     * @return the string
     */
    public static String buildURIParentPath(final String rpcType, final String contextPath) {
        return String.join(SEPARATOR, REGISTER_URI_INSTANCE_ROOT_PATH, rpcType, contextPath);
    }
    
    /**
     * Build instance parent path string.
     * build child path of "/shenyu/register/instance/
     *
     * @return the string
     */
    public static String buildInstanceParentPath() {
        return buildInstanceParentPath("instance");
    }

    /**
     * Build instance parent path string.
     * build child path of "/shenyu/register/instance/
     *
     * @param registerServiceName registerServiceName
     * @return the string
     */
    public static String buildInstanceParentPath(final String registerServiceName) {
        return String.join(SEPARATOR, ROOT_PATH, registerServiceName);
    }
    
    /**
     * Build real node string.
     *
     * @param nodePath the node path
     * @param nodeName the node name
     * @return the string
     */
    public static String buildRealNode(final String nodePath, final String nodeName) {
        return String.join(SEPARATOR, nodePath, nodeName);
    }
    
    /**
     * Build nacos instance service path string.
     * build child path of "shenyu.register.service.{rpcType}".
     *
     * @param rpcType the rpc type
     * @return the string
     */
    public static String buildServiceInstancePath(final String rpcType) {
        return String.join(SEPARATOR, ROOT_PATH, "service", rpcType)
                .replace("/", DOT_SEPARATOR).substring(1);
    }
    
    /**
     * Build nacos config service path string.
     * build child path of "shenyu.register.service.{rpcType}.{contextPath}".
     *
     * @param rpcType the rpc type
     * @param contextPath the context path
     * @return the string
     */
    public static String buildServiceConfigPath(final String rpcType, final String contextPath) {
        final String serviceConfigPathOrigin = String.join(SEPARATOR, ROOT_PATH, "service", rpcType, contextPath)
                .replace("/", DOT_SEPARATOR).replace("*", "");
        final String serviceConfigPathAfterSubstring = serviceConfigPathOrigin.substring(1);
        if (serviceConfigPathAfterSubstring.endsWith(".")) {
            return serviceConfigPathAfterSubstring.substring(0, serviceConfigPathAfterSubstring.length() - 1);
        }
        return serviceConfigPathAfterSubstring;
    }
    
    /**
     * Build node name by DOT_SEPARATOR.
     *
     * @param serviceName the service name
     * @param methodName the method name
     * @return the string
     */
    public static String buildNodeName(final String serviceName, final String methodName) {
        return String.join(DOT_SEPARATOR, serviceName, methodName);
    }

    public static class Kubernetes {
        public static final String NAMESPACE = "shenyu";

        public static final String LABEL_PREFIX = "shenyu.apache.org/";

        public static final String ANNOTATION_PREFIX = "shenyu.apache.org/";

        public static final String CONFIGMAP_LABEL = LABEL_PREFIX + "register";

        public static final String CONFIGMAP_LABEL_VALUE = "true";

        public static final String CONFIGMAP_DATA_KEY = "data";

        public static final String REGISTER_TYPE_LABEL = LABEL_PREFIX + "register-type";

        public static final String REGISTER_TYPE_LABEL_METADATA_VALUE = "metadata";

        public static final String REGISTER_TYPE_LABEL_URI_VALUE = "uri";

        public static final String REGISTER_TYPE_LABEL_API_DOC_VALUE = "api-doc";

        public static final String RPC_TYPE_LABEL = LABEL_PREFIX + "register-rpc-type";

        public static final String RULE_NAME_ANNOTATION = ANNOTATION_PREFIX + "rule-name";

        public static final String CONTEXT_PATH_ANNOTATION = ANNOTATION_PREFIX + "context-path";

        public static final String REGISTER_IP_PORT_ANNOTATION = ANNOTATION_PREFIX + "ip-port";

        public static final String REGISTER_METADATA_FULL_PATH_ANNOTATION = ANNOTATION_PREFIX + "register-metadata-full-path";

        public static final String REGISTER_URI_FULL_PATH_ANNOTATION = ANNOTATION_PREFIX + "register-uri-full-path";

        public static final String CONFIGMAP_NAME_METADATA_PREFIX = "register-metadata-";

        public static final String CONFIGMAP_NAME_URI_PREFIX = "register-uri-";

        /**
         * Build uri node name string.
         * @param registerDTO the register dto
         * @return the string
         */
        public static String buildURINodeName(final URIRegisterDTO registerDTO) {
            String host = registerDTO.getHost();
            int port = registerDTO.getPort();
            return String.join(Constants.COLONS, host, Integer.toString(port));
        }

        /**
         * Convert metadata node name to configmap name string.
         * @param nodeName the metadata node name
         * @return the string
         */
        public static String buildMetaDataConfigMapName(final String nodeName) {
            return buildConfigMapName(nodeName, REGISTER_METADATA_INSTANCE_ROOT_PATH, Kubernetes.CONFIGMAP_NAME_METADATA_PREFIX);
        }

        /**
         * Convert uri node name to configmap name string.
         * @param nodeName the uri node name
         * @return the string
         */
        public static String buildURIConfigMapName(final String nodeName) {
            return buildConfigMapName(nodeName, REGISTER_URI_INSTANCE_ROOT_PATH, Kubernetes.CONFIGMAP_NAME_URI_PREFIX);
        }

        private static String buildConfigMapName(final String fullNodeName, final String parentPath, final String configMapPrefix) {
            // if realNode has prefix parentPath, such as "/shenyu/register/metadata", remove it
            String nodeNameWithoutPrefix = fullNodeName.startsWith(parentPath)
                    ? fullNodeName.substring(parentPath.length())
                    : fullNodeName;

            // use base16 to encode node name, because kubernetes configmap name follows lowercase RFC 1123
            String nodeNameBase16 = Hex.encodeHexString(nodeNameWithoutPrefix.getBytes()).toLowerCase();

            // use human-readable prefix to make configmap name more readable
            return configMapPrefix + nodeNameBase16;
        }

        /**
         * Build metadata node name string.
         * @param metadata the metadata
         * @return the string
         */
        public static String buildMetadataNodeName(final MetaDataRegisterDTO metadata) {
            String nodeName;
            String rpcType = metadata.getRpcType();
            if (RpcTypeEnum.HTTP.getName().equals(rpcType)
                    || RpcTypeEnum.SPRING_CLOUD.getName().equals(rpcType)) {
                nodeName = String.join(DefaultPathConstants.SELECTOR_JOIN_RULE,
                        metadata.getContextPath(),
                        metadata.getRuleName().replace(PATH_SEPARATOR, DefaultPathConstants.SELECTOR_JOIN_RULE));
            } else {
                nodeName = RegisterPathConstants.buildNodeName(metadata.getServiceName(), metadata.getMethodName());
            }
            return nodeName.startsWith(PATH_SEPARATOR) ? nodeName.substring(1) : nodeName;
        }
    }

}
