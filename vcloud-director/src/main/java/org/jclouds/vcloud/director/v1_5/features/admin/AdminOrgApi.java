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
package org.jclouds.vcloud.director.v1_5.features.admin;

import static org.jclouds.Fallbacks.NullOnNotFoundOr404;
import static org.jclouds.vcloud.director.v1_5.VCloudDirectorMediaType.ORG_EMAIL_SETTINGS;
import static org.jclouds.vcloud.director.v1_5.VCloudDirectorMediaType.ORG_GENERAL_SETTINGS;
import static org.jclouds.vcloud.director.v1_5.VCloudDirectorMediaType.ORG_LEASE_SETTINGS;
import static org.jclouds.vcloud.director.v1_5.VCloudDirectorMediaType.ORG_PASSWORD_POLICY_SETTINGS;
import static org.jclouds.vcloud.director.v1_5.VCloudDirectorMediaType.ORG_SETTINGS;
import static org.jclouds.vcloud.director.v1_5.VCloudDirectorMediaType.ORG_VAPP_TEMPLATE_LEASE_SETTINGS;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.jclouds.rest.annotations.BinderParam;
import org.jclouds.rest.annotations.EndpointParam;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.JAXBResponseParser;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.binders.BindToXMLPayload;
import org.jclouds.vcloud.director.v1_5.domain.org.AdminOrg;
import org.jclouds.vcloud.director.v1_5.domain.org.OrgEmailSettings;
import org.jclouds.vcloud.director.v1_5.domain.org.OrgGeneralSettings;
import org.jclouds.vcloud.director.v1_5.domain.org.OrgLdapSettings;
import org.jclouds.vcloud.director.v1_5.domain.org.OrgLeaseSettings;
import org.jclouds.vcloud.director.v1_5.domain.org.OrgPasswordPolicySettings;
import org.jclouds.vcloud.director.v1_5.domain.org.OrgSettings;
import org.jclouds.vcloud.director.v1_5.domain.org.OrgVAppTemplateLeaseSettings;
import org.jclouds.vcloud.director.v1_5.features.OrgApi;
import org.jclouds.vcloud.director.v1_5.filters.AddVCloudAuthorizationAndCookieToRequest;

@RequestFilters(AddVCloudAuthorizationAndCookieToRequest.class)
public interface AdminOrgApi extends OrgApi {

   /** Retrieves an admin view of an organization or null if not found. */
   @Override
   @GET
   @Consumes
   @JAXBResponseParser
   @Fallback(NullOnNotFoundOr404.class)
   AdminOrg get(@EndpointParam URI adminOrgHref);

   @GET
   @Path("/settings")
   @Consumes
   @JAXBResponseParser
   @Fallback(NullOnNotFoundOr404.class)
   OrgSettings getSettings(@EndpointParam URI adminOrgHref);

   @PUT
   @Path("/settings")
   @Consumes(ORG_SETTINGS)
   @Produces(ORG_SETTINGS)
   @JAXBResponseParser
   OrgSettings editSettings(@EndpointParam URI adminOrgHref,
         @BinderParam(BindToXMLPayload.class) OrgSettings settings);

   @GET
   @Path("/settings/email")
   @Consumes
   @JAXBResponseParser
   @Fallback(NullOnNotFoundOr404.class)
   OrgEmailSettings getEmailSettings(@EndpointParam URI adminOrgHref);

   @PUT
   @Path("/settings/email")
   @Consumes(ORG_EMAIL_SETTINGS)
   @Produces(ORG_EMAIL_SETTINGS)
   @JAXBResponseParser
   OrgEmailSettings editEmailSettings(@EndpointParam URI adminOrgHref,
         @BinderParam(BindToXMLPayload.class) OrgEmailSettings settings);

   @GET
   @Path("/settings/general")
   @Consumes
   @JAXBResponseParser
   @Fallback(NullOnNotFoundOr404.class)
   OrgGeneralSettings getGeneralSettings(@EndpointParam URI adminOrgHref);

   @PUT
   @Path("/settings/general")
   @Consumes(ORG_GENERAL_SETTINGS)
   @Produces(ORG_GENERAL_SETTINGS)
   @JAXBResponseParser
   OrgGeneralSettings editGeneralSettings(@EndpointParam URI adminOrgHref,
         @BinderParam(BindToXMLPayload.class) OrgGeneralSettings settings);

   @GET
   @Path("/settings/ldap")
   @Consumes
   @JAXBResponseParser
   @Fallback(NullOnNotFoundOr404.class)
   OrgLdapSettings getLdapSettings(@EndpointParam URI adminOrgHref);

   @GET
   @Path("/settings/passwordPolicy")
   @Consumes
   @JAXBResponseParser
   @Fallback(NullOnNotFoundOr404.class)
   OrgPasswordPolicySettings getPasswordPolicy(@EndpointParam URI adminOrgHref);

   @PUT
   @Path("/settings/passwordPolicy")
   @Consumes(ORG_PASSWORD_POLICY_SETTINGS)
   @Produces(ORG_PASSWORD_POLICY_SETTINGS)
   @JAXBResponseParser
   OrgPasswordPolicySettings editPasswordPolicy(@EndpointParam URI adminOrgHref,
         @BinderParam(BindToXMLPayload.class) OrgPasswordPolicySettings settings);

   @GET
   @Path("/settings/vAppLeaseSettings")
   @Consumes
   @JAXBResponseParser
   @Fallback(NullOnNotFoundOr404.class)
   OrgLeaseSettings getVAppLeaseSettings(@EndpointParam URI adminOrgHref);

   @PUT
   @Path("/settings/vAppLeaseSettings")
   @Consumes(ORG_LEASE_SETTINGS)
   @Produces(ORG_LEASE_SETTINGS)
   @JAXBResponseParser
   OrgLeaseSettings editVAppLeaseSettings(@EndpointParam URI adminOrgHref,
         @BinderParam(BindToXMLPayload.class) OrgLeaseSettings settings);

   @GET
   @Path("/settings/vAppTemplateLeaseSettings")
   @Consumes
   @JAXBResponseParser
   @Fallback(NullOnNotFoundOr404.class)
   OrgVAppTemplateLeaseSettings getVAppTemplateLeaseSettings(@EndpointParam URI adminOrgHref);

   @PUT
   @Path("/settings/vAppTemplateLeaseSettings")
   @Consumes(ORG_VAPP_TEMPLATE_LEASE_SETTINGS)
   @Produces(ORG_VAPP_TEMPLATE_LEASE_SETTINGS)
   @JAXBResponseParser
   OrgVAppTemplateLeaseSettings editVAppTemplateLeaseSettings(@EndpointParam URI adminOrgHref,
         @BinderParam(BindToXMLPayload.class) OrgVAppTemplateLeaseSettings settings);
}
