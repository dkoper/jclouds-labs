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
package org.jclouds.azurecompute.features;

import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static org.jclouds.Fallbacks.EmptyListOnNotFoundOr404;
import static org.jclouds.Fallbacks.NullOnNotFoundOr404;

import java.util.List;

import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.jclouds.azurecompute.binders.ImageParamsToXML;
import org.jclouds.azurecompute.domain.Image;
import org.jclouds.azurecompute.domain.ImageParams;
import org.jclouds.azurecompute.functions.ImageParamsName;
import org.jclouds.azurecompute.functions.ParseRequestIdHeader;
import org.jclouds.azurecompute.xml.ListImagesHandler;
import org.jclouds.rest.annotations.BinderParam;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.Headers;
import org.jclouds.rest.annotations.ParamParser;
import org.jclouds.rest.annotations.ResponseParser;
import org.jclouds.rest.annotations.XMLResponseParser;

/**
 * The Service Management API includes operations for managing the OS images in your subscription.
 *
 * @see <a href="http://msdn.microsoft.com/en-us/library/jj157175">docs</a>
 */
@Path("/services/images")
@Headers(keys = "x-ms-version", values = "{jclouds.api-version}")
@Consumes(APPLICATION_XML)
public interface ImageApi {

   /**
    * The List Cloud Services operation lists the cloud services available under the current
    * subscription.
    */
   @Named("ListImages")
   @GET
   @XMLResponseParser(ListImagesHandler.class)
   @Fallback(EmptyListOnNotFoundOr404.class)
   List<Image> list();

   /**
    * The Add OS Image operation adds an OS image that is currently stored in a storage account in your subscription to
    * the image repository.
    */
   @Named("AddImage")
   @POST
   @Produces(APPLICATION_XML)
   @ResponseParser(ParseRequestIdHeader.class)
   String add(@BinderParam(ImageParamsToXML.class) ImageParams params);

   /**
    * The Update OS Image operation updates an OS image that in your image repository.
    */
   @Named("UpdateImage")
   @PUT
   @Path("/{imageName}")
   @Produces(APPLICATION_XML)
   @ResponseParser(ParseRequestIdHeader.class)
   String update(@PathParam("imageName") @ParamParser(ImageParamsName.class)
               @BinderParam(ImageParamsToXML.class) ImageParams params);

   /**
    * The Delete Cloud Service operation deletes the specified cloud service from Windows Azure.
    *
    * @param imageName
    *           the unique DNS Prefix value in the Windows Azure Management Portal
    */
   @Named("DeleteImage")
   @DELETE
   @Path("/{imageName}")
   @Fallback(NullOnNotFoundOr404.class)
   @ResponseParser(ParseRequestIdHeader.class)
   String delete(@PathParam("imageName") String imageName);
}
