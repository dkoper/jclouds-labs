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
package org.jclouds.azurecompute;

import java.net.URI;
import java.util.Properties;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.providers.internal.BaseProviderMetadata;

import static org.jclouds.azurecompute.config.AzureComputeProperties.SUBSCRIPTION_ID;

/**
 * Implementation of {@link ProviderMetadata} for Microsoft Azure Service Management Service.
 */
public class AzureComputeProviderMetadata extends BaseProviderMetadata {

   public static Builder builder() {
      return new Builder();
   }

   @Override
   public Builder toBuilder() {
      return builder().fromProviderMetadata(this);
   }

   public AzureComputeProviderMetadata() {
      super(builder());
   }

   public AzureComputeProviderMetadata(Builder builder) {
      super(builder);
   }

   public static Properties defaultProperties() {
      Properties properties = new Properties();
      return properties;
   }

   public static class Builder extends BaseProviderMetadata.Builder {

      protected Builder() {
         id("azurecompute")
         .name("Microsoft Azure Service Management Service")
         .apiMetadata(new AzureManagementApiMetadata())
         .endpoint("https://management.core.windows.net/${" + SUBSCRIPTION_ID + "}")
         .homepage(URI.create("https://www.windowsazure.com/"))
         .console(URI.create("https://windows.azure.com/default.aspx"))
         .linkedServices("azureblob", "azurequeue", "azuretable")
         .iso3166Codes("US-TX", "US-IL", "IE-D", "SG", "NL-NH", "HK")
         .defaultProperties(AzureComputeProviderMetadata.defaultProperties());
      }

      @Override
      public AzureComputeProviderMetadata build() {
         return new AzureComputeProviderMetadata(this);
      }

      @Override
      public Builder fromProviderMetadata(ProviderMetadata in) {
         super.fromProviderMetadata(in);
         return this;
      }
   }
}
