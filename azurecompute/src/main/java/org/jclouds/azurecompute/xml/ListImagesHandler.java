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
package org.jclouds.azurecompute.xml;

import java.util.List;

import org.jclouds.azurecompute.domain.Image;
import org.jclouds.http.functions.ParseSax;
import org.xml.sax.Attributes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public final class ListImagesHandler extends ParseSax.HandlerForGeneratedRequestWithResult<List<Image>> {
   private boolean inOSImage;
   private final ImageHandler imageHandler = new ImageHandler();
   private final Builder<Image> images = ImmutableList.builder();

   @Override
   public List<Image> getResult() {
      return images.build();
   }

   @Override
   public void startElement(String url, String name, String qName, Attributes attributes) {
      if (qName.equals("OSImage")) {
         inOSImage = true;
      }
   }

   @Override
   public void endElement(String uri, String name, String qName) {
      if (qName.equals("OSImage")) {
         inOSImage = false;
         images.add(imageHandler.getResult());
      } else if (inOSImage) {
         imageHandler.endElement(uri, name, qName);
      }
   }

   @Override
   public void characters(char ch[], int start, int length) {
      if (inOSImage) {
         imageHandler.characters(ch, start, length);
      }
   }
}
