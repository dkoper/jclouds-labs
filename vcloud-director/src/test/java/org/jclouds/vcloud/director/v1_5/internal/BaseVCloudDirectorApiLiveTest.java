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
package org.jclouds.vcloud.director.v1_5.internal;

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.contains;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Iterables.tryFind;
import static org.jclouds.util.Predicates2.retry;
import static org.jclouds.vcloud.director.v1_5.VCloudDirectorLiveTestConstants.ENTITY_NON_NULL;
import static org.jclouds.vcloud.director.v1_5.VCloudDirectorLiveTestConstants.TASK_COMPLETE_TIMELY;
import static org.jclouds.vcloud.director.v1_5.VCloudDirectorLiveTestConstants.URN_REQ_LIVE;
import static org.jclouds.vcloud.director.v1_5.VCloudDirectorMediaType.CATALOG;
import static org.jclouds.vcloud.director.v1_5.VCloudDirectorMediaType.MEDIA;
import static org.jclouds.vcloud.director.v1_5.VCloudDirectorMediaType.NETWORK;
import static org.jclouds.vcloud.director.v1_5.VCloudDirectorMediaType.ORG_NETWORK;
import static org.jclouds.vcloud.director.v1_5.VCloudDirectorMediaType.USER;
import static org.jclouds.vcloud.director.v1_5.VCloudDirectorMediaType.VAPP;
import static org.jclouds.vcloud.director.v1_5.VCloudDirectorMediaType.VAPP_TEMPLATE;
import static org.jclouds.vcloud.director.v1_5.VCloudDirectorMediaType.VDC;
import static org.jclouds.vcloud.director.v1_5.VCloudDirectorMediaType.VM;
import static org.jclouds.vcloud.director.v1_5.domain.params.UndeployVAppParams.PowerAction.SHUTDOWN;
import static org.jclouds.vcloud.director.v1_5.predicates.LinkPredicates.relEquals;
import static org.jclouds.vcloud.director.v1_5.predicates.LinkPredicates.typeEquals;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Set;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.jclouds.apis.BaseContextLiveTest;
import org.jclouds.date.DateService;
import org.jclouds.io.Payloads;
import org.jclouds.logging.Logger;
import org.jclouds.rest.ApiContext;
import org.jclouds.vcloud.director.testng.FormatApiResultsListener;
import org.jclouds.vcloud.director.v1_5.VCloudDirectorContext;
import org.jclouds.vcloud.director.v1_5.VCloudDirectorMediaType;
import org.jclouds.vcloud.director.v1_5.admin.VCloudDirectorAdminApi;
import org.jclouds.vcloud.director.v1_5.domain.Catalog;
import org.jclouds.vcloud.director.v1_5.domain.Checks;
import org.jclouds.vcloud.director.v1_5.domain.Link;
import org.jclouds.vcloud.director.v1_5.domain.Link.Rel;
import org.jclouds.vcloud.director.v1_5.domain.Media;
import org.jclouds.vcloud.director.v1_5.domain.Reference;
import org.jclouds.vcloud.director.v1_5.domain.ResourceEntity.Status;
import org.jclouds.vcloud.director.v1_5.domain.Role.DefaultRoles;
import org.jclouds.vcloud.director.v1_5.domain.RoleReferences;
import org.jclouds.vcloud.director.v1_5.domain.Session;
import org.jclouds.vcloud.director.v1_5.domain.Task;
import org.jclouds.vcloud.director.v1_5.domain.User;
import org.jclouds.vcloud.director.v1_5.domain.VApp;
import org.jclouds.vcloud.director.v1_5.domain.VAppTemplate;
import org.jclouds.vcloud.director.v1_5.domain.Vdc;
import org.jclouds.vcloud.director.v1_5.domain.Vm;
import org.jclouds.vcloud.director.v1_5.domain.network.Network;
import org.jclouds.vcloud.director.v1_5.domain.network.NetworkConfiguration;
import org.jclouds.vcloud.director.v1_5.domain.network.VAppNetworkConfiguration;
import org.jclouds.vcloud.director.v1_5.domain.org.Org;
import org.jclouds.vcloud.director.v1_5.domain.params.InstantiateVAppTemplateParams;
import org.jclouds.vcloud.director.v1_5.domain.params.InstantiationParams;
import org.jclouds.vcloud.director.v1_5.domain.params.UndeployVAppParams;
import org.jclouds.vcloud.director.v1_5.domain.section.NetworkConfigSection;
import org.jclouds.vcloud.director.v1_5.features.TaskApi;
import org.jclouds.vcloud.director.v1_5.features.VAppApi;
import org.jclouds.vcloud.director.v1_5.features.VAppTemplateApi;
import org.jclouds.vcloud.director.v1_5.features.VdcApi;
import org.jclouds.vcloud.director.v1_5.predicates.LinkPredicates;
import org.jclouds.vcloud.director.v1_5.predicates.ReferencePredicates;
import org.jclouds.vcloud.director.v1_5.predicates.TaskStatusEquals;
import org.jclouds.vcloud.director.v1_5.predicates.TaskSuccess;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.google.inject.Guice;

@Listeners(FormatApiResultsListener.class)
@Test(groups = "live")
public abstract class BaseVCloudDirectorApiLiveTest extends BaseContextLiveTest<VCloudDirectorContext> {

   @Resource
   protected Logger logger = Logger.NULL;

   protected static final long TASK_TIMEOUT_SECONDS = 100L;
   protected static final long LONG_TASK_TIMEOUT_SECONDS = 300L;

   public static final int REQUIRED_ADMIN_VM_QUOTA = 0;
   public static final int REQUIRED_USER_VM_QUOTA = 0;

   public Predicate<Task> retryTaskSuccess;
   public Predicate<Task> retryTaskSuccessLong;

   protected ApiContext<VCloudDirectorAdminApi> adminContext;

   protected Session adminSession;
   protected Session session;

   protected String orgId;
   protected Org org;
   protected String catalogId;
   private Catalog catalog;
   protected String vAppTemplateId;
   private VAppTemplate vAppTemplate;
   protected String mediaId;
   private Media media;
   protected String networkId;
   private Network network;
   protected String vdcId;
   private Vdc vdc;
   protected String userId;
   private User user;

   protected final Set<String> vAppNames = Sets.newLinkedHashSet();
   protected static final Random random = new Random();

   protected BaseVCloudDirectorApiLiveTest() {
      provider = "vcloud-director";
   }

   protected DateService dateService;

   protected static String testStamp;

   @BeforeClass(alwaysRun = true)
   protected void setupDateService() {
      dateService = Guice.createInjector().getInstance(DateService.class);
      assertNotNull(dateService);
   }

   /** Implement as required to populate xxxApi fields, or NOP */
   protected abstract void setupRequiredApis();

   @Inject
   protected void initTaskSuccess(TaskSuccess taskSuccess) {
      retryTaskSuccess = retry(taskSuccess, TASK_TIMEOUT_SECONDS * 1000L);
   }

   @Inject
   protected void initTaskSuccessLong(TaskSuccess taskSuccess) {
      retryTaskSuccessLong = retry(taskSuccess, LONG_TASK_TIMEOUT_SECONDS * 1000L);
   }

   @BeforeClass(groups = { "integration", "live" })
   public void setupContext() {
      super.setupContext();
      adminContext = context.getAdminContext();

      adminSession = adminContext.getApi().getCurrentSession();
      adminContext.utils().injector().injectMembers(this);

      session = context.getApi().getCurrentSession();
      context.utils().injector().injectMembers(this);

      initTestParametersFromPropertiesOrLazyDiscover();
      setupRequiredApis();
   }

   public static String getTestDateTimeStamp() {
      if (testStamp == null) {
         testStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
      }

      return testStamp;
   }

   public Reference getRoleReferenceFor(String name) {
      return getRoleReferenceFor(name, adminContext);
   }

   public static Reference getRoleReferenceFor(String name, ApiContext<VCloudDirectorAdminApi> adminContext) {
      RoleReferences roles = adminContext.getApi().getQueryApi().roleReferencesQueryAll();
      // backend in a builder to strip out unwanted xml cruft that the api chokes on
      return Reference.builder().fromReference(find(roles.getReferences(), ReferencePredicates.nameEquals(name)))
               .build();
   }

   public User randomTestUser(String prefix) {
      return randomTestUser(prefix, getRoleReferenceFor(DefaultRoles.USER.value()));
   }

   public User randomTestUser(String prefix, Reference role) {
      return User.builder().name(name(prefix) + getTestDateTimeStamp()).fullName("testFullName")
               .emailAddress("test@test.com").telephone("555-1234").isEnabled(false).im("testIM").isAlertEnabled(false)
               .alertEmailPrefix("testPrefix").alertEmail("testAlert@test.com").isExternal(false).isGroupRole(false)
               .role(role).password("password").build();
   }

   protected void initTestParametersFromPropertiesOrLazyDiscover() {
      catalogId = emptyToNull(System.getProperty("test." + provider + ".catalog-id"));

      vAppTemplateId = emptyToNull(System.getProperty("test." + provider + ".vapptemplate-id"));

      vdcId = emptyToNull(System.getProperty("test." + provider + ".vdc-id"));

      mediaId = emptyToNull(System.getProperty("test." + provider + ".media-id"));

      networkId = emptyToNull(System.getProperty("test." + provider + ".network-id"));

      userId = emptyToNull(System.getProperty("test." + provider + ".user-id"));
     
      org = context
               .getApi()
               .getOrgApi()
               .get(find(context.getApi().getOrgApi().list(), ReferencePredicates.<Reference> nameEquals(session.get()))
                        .getHref());
      orgId = org.getId();

      if (any(Lists.newArrayList(vAppTemplateId, networkId, vdcId), Predicates.isNull())) {

         if (vdcId == null) {
            vdc = context.getApi().getVdcApi()
                     .get(find(org.getLinks(), ReferencePredicates.<Link> typeEquals(VDC)).getHref());
            vdcId = vdc.getId();

            if (vAppTemplateId == null) {
               Optional<VAppTemplate> optionalvAppTemplate = tryFindVAppTemplateInOrg();
               if (optionalvAppTemplate.isPresent()) {
               vAppTemplate = optionalvAppTemplate.get();
               vAppTemplateId = vAppTemplate.getId();
               }
            }
         }

         if (networkId == null) {
            Optional<Network> optionalNetwork = tryFindBridgedNetworkInOrg();
            if (optionalNetwork.isPresent()) {
               network = optionalNetwork.get();
               networkId = network.getId();
            }
         }

         if (catalogId == null) {
            Optional<Catalog> optionalCatalog = tryFindWritableCatalogInOrg();
            if (optionalCatalog.isPresent()) {
               catalog = optionalCatalog.get();
               catalogId = catalog.getId();
            }
         }
      }
   }

   Function<VAppTemplate, String> prettyVAppTemplate = new Function<VAppTemplate, String>() {

      @Override
      public String apply(VAppTemplate input) {
         return MoreObjects.toStringHelper("").omitNullValues().add("name", input.getName()).add("id", input.getId())
                  .add("owner", input.getOwner()).toString();
      }

   };
   
   public Optional<VAppTemplate> tryFindVAppTemplateInOrg() {
      FluentIterable<VAppTemplate> vAppTemplates =  FluentIterable.from(vdc.getResourceEntities())
               .filter(ReferencePredicates.<Reference> typeEquals(VAPP_TEMPLATE))
               .transform(new Function<Reference, VAppTemplate>() {

                  @Override
                  public VAppTemplate apply(Reference in) {
                     return context.getApi().getVAppTemplateApi().get(in.getHref());
                  }})
               .filter(Predicates.notNull());      
      
      Optional<VAppTemplate> optionalVAppTemplate = tryFind(vAppTemplates, new Predicate<VAppTemplate>() {

         @Override
         public boolean apply(VAppTemplate input) {
            return input.getOwner().getUser().getName().equals(session.getUser());
         }

      });
      
      if (optionalVAppTemplate.isPresent()) {
         Logger.CONSOLE.info("found vAppTemplate: %s", prettyVAppTemplate.apply(optionalVAppTemplate.get()));
      } else {
         Logger.CONSOLE.warn("%s doesn't own any vApp Template in org %s; vApp templates: %s", context.getApi()
                  .getCurrentSession().getUser(), org.getName(), Iterables.transform(vAppTemplates, prettyVAppTemplate));
      }

      return optionalVAppTemplate;
   }
   
   Function<Vm, String> prettyVm = new Function<Vm, String>() {

      @Override
      public String apply(Vm input) {
         return MoreObjects.toStringHelper("").omitNullValues().add("name", input.getName()).add("id", input.getId()).toString();
      }

   };
   
   public Optional<Vm> tryFindVmInOrg() {
      FluentIterable<Vm> vms =  FluentIterable.from(vdc.getResourceEntities())
               .filter(ReferencePredicates.<Reference> typeEquals(VM))
               .transform(new Function<Reference, Vm>() {

                  @Override
                  public Vm apply(Reference in) {
                     return context.getApi().getVmApi().get(in.getHref());
                  }})
               .filter(Predicates.notNull());      
      
      Optional<Vm> optionalVm = tryFind(vms, new Predicate<Vm>() {

         @Override
         public boolean apply(Vm input) {
            return input.getId() != null;
         }
      });
      
      if (optionalVm.isPresent()) {
         Logger.CONSOLE.info("found vm: %s", prettyVm.apply(optionalVm.get()));
      } else {
         Logger.CONSOLE.warn("%s doesn't have any vm in org %s; vms: %s", context.getApi()
                  .getCurrentSession().getUser(), org.getName(), Iterables.transform(vms, prettyVm));
      }
      
      return optionalVm;
   }   
   
   Function<Catalog, String> prettyCatalog = new Function<Catalog, String>() {

      @Override
      public String apply(Catalog input) {
         return MoreObjects.toStringHelper("").omitNullValues().add("name", input.getName()).add("id", input.getId())
                  .add("owner", input.getOwner()).add("isPublished", input.isPublished()).toString();
      }

   };

   /**
    * If I can add to a catalog, I can write to it
    */
   public Optional<Catalog> tryFindWritableCatalogInOrg() {
      FluentIterable<Catalog> catalogs = FluentIterable.from(org.getLinks())
               .filter(ReferencePredicates.<Link> typeEquals(CATALOG)).transform(new Function<Link, Catalog>() {
                  @Override
                  public Catalog apply(Link in) {
                     return context.getApi().getCatalogApi().get(in.getHref());
                  }
               });

      Optional<Catalog> optionalCatalog = tryFind(catalogs, new Predicate<Catalog>() {

         @Override
         public boolean apply(Catalog input) {
            return Iterables.any(input.getLinks(), LinkPredicates.relEquals(Rel.ADD));
         }

      });
      if (optionalCatalog.isPresent()) {
         Logger.CONSOLE.info("found catalog: %s", prettyCatalog.apply(optionalCatalog.get()));
      } else {
         Logger.CONSOLE.warn("%s doesn't own any catalogs in org %s; catalogs: %s", context.getApi()
                  .getCurrentSession().getUser(), org.getName(), Iterables.transform(catalogs, prettyCatalog));
      }
      return optionalCatalog;
   }

   Function<Network, String> prettyNetwork = new Function<Network, String>() {

      @Override
      public String apply(Network input) {
         return MoreObjects.toStringHelper("").omitNullValues().add("name", input.getName()).add("id", input.getId())
                  .add("fenceMode", input.getConfiguration().getFenceMode())
                  .add("taskCount", input.getTasks().size() > 0 ? input.getTasks().size() : null).toString();
      }

   };
   
   public Optional<Network> tryFindBridgedNetworkInOrg() {
      FluentIterable<Network> networks = FluentIterable.from(org.getLinks())
               .filter(ReferencePredicates.<Link> typeEquals(ORG_NETWORK)).transform(new Function<Link, Network>() {
                  @Override
                  public Network apply(Link in) {
                     return context.getApi().getNetworkApi().get(in.getHref());
                  }
               });

      Optional<Network> optionalNetwork = tryFind(networks, new Predicate<Network>() {

         @Override
         public boolean apply(Network input) {
            if (input.getConfiguration().getFenceMode().equals(Network.FenceMode.BRIDGED)) {
               if (input.getTasks().size() == 0) {
                  return true;
               }
            }
            return false;
         }

      });
      if (optionalNetwork.isPresent()) {
         Logger.CONSOLE.info("found network: %s", prettyNetwork.apply(optionalNetwork.get()));
      } else {
         Logger.CONSOLE.warn("no ready bridged networks present in org %s; networks: %s", org.getName(),
                  Iterables.transform(networks, prettyNetwork));
      }
      return optionalNetwork;
   }
   
	public FluentIterable<Media> findAllEmptyMediaInOrg() {
		vdc = context.getApi().getVdcApi().get(vdc.getHref());
		return FluentIterable
				.from(vdc.getResourceEntities())
				.filter(ReferencePredicates.<Reference> typeEquals(MEDIA))
				.transform(new Function<Reference, Media>() {

					@Override
					public Media apply(Reference in) {
						return context.getApi().getMediaApi()
								.get(in.getHref());
					}
				}).filter(new Predicate<Media>() {

					@Override
					public boolean apply(Media input) {
						return input.getSize() == 0;
					}
				});
	}
	
	public void cleanUpVAppTemplateInOrg() {
		FluentIterable<VAppTemplate> vAppTemplates = FluentIterable
				.from(vdc.getResourceEntities())
				.filter(ReferencePredicates
						.<Reference> typeEquals(VAPP_TEMPLATE))
				.transform(new Function<Reference, VAppTemplate>() {

					@Override
					public VAppTemplate apply(Reference in) {
						return context.getApi().getVAppTemplateApi()
								.get(in.getHref());
					}
				}).filter(Predicates.notNull());

		Iterables.removeIf(vAppTemplates, new Predicate<VAppTemplate>() {

			@Override
			public boolean apply(VAppTemplate input) {
				if (input.getName().startsWith("captured-") || input.getName().startsWith("uploaded-") || input.getName().startsWith("vappTemplateClone-"))
					context.getApi().getVAppTemplateApi().remove(input.getHref());
				return false;
			}});
	}	
	
   protected Vdc lazyGetVdc() {
      if (vdc == null) {
         assertNotNull(vdcId, String.format(URN_REQ_LIVE, VDC));
         vdc = context.getApi().getVdcApi().get(context.resolveIdToHref(vdcId));
         assertNotNull(vdc, String.format(ENTITY_NON_NULL, VDC));
      }
      return vdc;
   }

   protected Network lazyGetNetwork() {
      if (network == null) {
         assertNotNull(networkId, String.format(URN_REQ_LIVE, NETWORK));
         network = context.getApi().getNetworkApi().get(context.resolveIdToHref(networkId));
         assertNotNull(network, String.format(ENTITY_NON_NULL, NETWORK));
      }
      return network;
   }

   protected Catalog lazyGetCatalog() {
      if (catalog == null) {
         assertNotNull(catalogId, String.format(URN_REQ_LIVE, CATALOG));
         catalog = context.getApi().getCatalogApi().get(context.resolveIdToHref(catalogId));
         assertNotNull(catalog, String.format(ENTITY_NON_NULL, CATALOG));
      }
      return catalog;
   }

   protected User lazyGetUser() {
      if (user == null) {
         assertNotNull(userId, String.format(URN_REQ_LIVE, USER));
         user = adminContext.getApi().getUserApi().get(context.resolveIdToHref(userId));
         assertNotNull(user, String.format(ENTITY_NON_NULL, USER));
      }
      return user;
   }

   protected Media lazyGetMedia(){
      if (media == null || mediaId == null) {
         Predicate<Link> addMediaLink = and(relEquals(Link.Rel.ADD), typeEquals(VCloudDirectorMediaType.MEDIA));
         if (contains(lazyGetVdc().getLinks(), addMediaLink)) {
            Link addMedia = find(lazyGetVdc().getLinks(), addMediaLink);
            byte[] iso = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };

            Media sourceMedia = Media.builder().type(VCloudDirectorMediaType.MEDIA).name(name("media"))
                     .size(iso.length).imageType(Media.ImageType.ISO)
                     .description("Test media generated by VmApiLiveTest").build();
            media = context.getApi().getMediaApi().add(addMedia.getHref(), sourceMedia);

            Link uploadLink = getFirst(getFirst(media.getFiles(), null).getLinks(), null);
            context.getApi().getUploadApi().upload(uploadLink.getHref(), Payloads.newPayload(iso));

            media = context.getApi().getMediaApi().get(media.getHref());

            if (media.getTasks().size() == 1) {
               Task uploadTask = Iterables.getOnlyElement(media.getTasks());
               Checks.checkTask(uploadTask);
               assertEquals(uploadTask.getStatus(), Task.Status.RUNNING);
               assertTrue(retryTaskSuccess.apply(uploadTask), String.format(TASK_COMPLETE_TIMELY, "uploadTask"));
               media = context.getApi().getMediaApi().get(media.getHref());
            }
            mediaId = media.getId();
         } else {
            media = context.getApi().getMediaApi().get(context.resolveIdToHref(mediaId));
         }
      }
      return media;
   }
   
   protected VAppTemplate lazyGetVAppTemplate() {
      if (vAppTemplate == null) {
         assertNotNull(vAppTemplateId, String.format(URN_REQ_LIVE, VAPP_TEMPLATE));
         vAppTemplate = context.getApi().getVAppTemplateApi().get(context.resolveIdToHref(vAppTemplateId));
         assertNotNull(vAppTemplate, String.format(ENTITY_NON_NULL, VAPP_TEMPLATE));
      }
      return vAppTemplate;
   }

   protected void assertTaskSucceeds(Task task) {
      assertTrue(retryTaskSuccess.apply(task), String.format(TASK_COMPLETE_TIMELY, task));
   }

   protected void assertTaskSucceedsLong(Task task) {
      assertTrue(retryTaskSuccessLong.apply(task), String.format(TASK_COMPLETE_TIMELY, task));
   }

   protected boolean taskStatusEventually(Task task, Task.Status running, ImmutableSet<Task.Status> immutableSet) {
      TaskApi taskApi = context.getApi().getTaskApi();
      TaskStatusEquals predicate = new TaskStatusEquals(taskApi, running, immutableSet);
      return retry(predicate, TASK_TIMEOUT_SECONDS * 1000L).apply(task);
   }

   protected void assertTaskStatusEventually(Task task, Task.Status running, ImmutableSet<Task.Status> immutableSet) {
      assertTrue(taskStatusEventually(task, running, immutableSet),
               String.format("Task '%s' must reach status %s", task.getOperationName(), running));
   }

   protected boolean taskDoneEventually(Task task) {
      TaskApi taskApi = context.getApi().getTaskApi();
      TaskStatusEquals predicate = new TaskStatusEquals(taskApi, ImmutableSet.of(Task.Status.ABORTED,
               Task.Status.CANCELED, Task.Status.ERROR, Task.Status.SUCCESS), ImmutableSet.<Task.Status> of());
      return retry(predicate, TASK_TIMEOUT_SECONDS * 1000L).apply(task);
   }

   protected void assertTaskDoneEventually(Task task) {
      assertTrue(taskDoneEventually(task), String.format("Task '%s' must complete", task.getOperationName()));
   }

   /**
    * Instantiate a {@link VApp} in a {@link Vdc} using the {@link VAppTemplate} we have configured
    * for the tests.
    * 
    * @return the VApp that is being instantiated
    */
   protected VApp instantiateVApp() {
      return instantiateVApp(name("test-vapp-"));
   }

   protected VApp instantiateVApp(String name) {
      InstantiateVAppTemplateParams instantiate = InstantiateVAppTemplateParams.builder().name(name).notDeploy()
               .notPowerOn().description("Test VApp").instantiationParams(instantiationParams())
               .source(Reference.builder().href(lazyGetVAppTemplate().getHref()).build()).build();

      VdcApi vdcApi = context.getApi().getVdcApi();
      VApp vAppInstantiated = vdcApi.instantiateVApp(context.resolveIdToHref(vdcId), instantiate);
      assertNotNull(vAppInstantiated, String.format(ENTITY_NON_NULL, VAPP));

      Task instantiationTask = getFirst(vAppInstantiated.getTasks(), null);
      if (instantiationTask != null)
         assertTaskSucceedsLong(instantiationTask);

      // Save VApp name for cleanUp
      vAppNames.add(name);

      return vAppInstantiated;
   }

   /** Build an {@link InstantiationParams} object. */
   protected InstantiationParams instantiationParams() {
      InstantiationParams instantiationParams = InstantiationParams.builder()
               .sections(ImmutableSet.of(networkConfigSection())).build();

      return instantiationParams;
   }

   /** Build a {@link NetworkConfigSection} object. */
   private NetworkConfigSection networkConfigSection() {
      NetworkConfigSection networkConfigSection = NetworkConfigSection
               .builder()
               .info("Configuration parameters for logical networks")
               .networkConfigs(
                        ImmutableSet.of(VAppNetworkConfiguration.builder()
                                 .networkName("vAppNetwork")
                                 .configuration(networkConfiguration()).build())).build();

      return networkConfigSection;
   }

   /** Build a {@link NetworkConfiguration} object. */
   private NetworkConfiguration networkConfiguration() {
      Vdc vdc = context.getApi().getVdcApi().get(context.resolveIdToHref(vdcId));
      assertNotNull(vdc, String.format(ENTITY_NON_NULL, VDC));

      Set<Reference> networks = vdc.getAvailableNetworks();
      network = lazyGetNetwork();
      // Look up the network in the Vdc with the id configured for the tests
      Optional<Reference> parentNetwork = tryFind(networks, new Predicate<Reference>() {
         @Override
         public boolean apply(Reference reference) {
            return reference.getHref().equals(network.getHref());
         }
      });

      // Check we actually found a network reference
      if (!parentNetwork.isPresent()) {
         fail(String.format("Could not find network %s in vdc", network.getHref().toASCIIString()));
      }

      // Build the configuration object
      NetworkConfiguration networkConfiguration = NetworkConfiguration.builder().parentNetwork(parentNetwork.get())
               .fenceMode(Network.FenceMode.BRIDGED).build();

      return networkConfiguration;
   }

   protected void cleanUpVAppTemplate(VAppTemplate vAppTemplate) {
      VAppTemplateApi vappTemplateApi = context.getApi().getVAppTemplateApi();
      try {
         Task task = vappTemplateApi.remove(vAppTemplate.getHref());
         taskDoneEventually(task);
      } catch (Exception e) {
         logger.warn(e, "Error deleting template '%s'", vAppTemplate.getName());
      }
   }

   protected void cleanUpVApp(VApp vApp) {
      VAppApi vAppApi = context.getApi().getVAppApi();

      URI href = vApp.getHref();
      vApp = vAppApi.get(href); // Refresh
      if (vApp == null) {
         logger.info("Cannot find VApp at %s", href);
         return; // Presumably vApp has already been removed. Ignore.
      }
      logger.debug("Deleting VApp %s (%s)", vApp.getName(), href);

      // Wait for busy tasks to complete (don't care if it's failed or successful)
      // Otherwise, get error on remove "entity is busy completing an operation.
      if (vApp.getTasks() != null) {
         for (Task task : vApp.getTasks()) {
            if (!taskDoneEventually(task)) {
               logger.warn("Task '%s' did not complete", task.getOperationName());
            }
         }
      }

      // power off the VApp if necessary
      if (vApp.getStatus() == Status.POWERED_ON) {
         try {
            Task shutdownTask = vAppApi.powerOff(href);
            taskDoneEventually(shutdownTask);
         } catch (Exception e) {
            // keep going; cleanup as much as possible
            logger.warn(e, "Continuing cleanup after error shutting down VApp %s", vApp.getName());
         }
      }

      // Undeploy the VApp if necessary
      if (vApp.isDeployed()) {
         try {
            UndeployVAppParams params = UndeployVAppParams.builder().undeployPowerAction(SHUTDOWN).build();
            Task undeployTask = vAppApi.undeploy(href, params);
            taskDoneEventually(undeployTask);
         } catch (Exception e) {
            // keep going; cleanup as much as possible
            logger.warn(e, "Continuing cleanup after error undeploying VApp %s", vApp.getName());
         }
      }

      try {
         Task task = vAppApi.remove(href);
         taskDoneEventually(task);
         vAppNames.remove(vApp.getName());
         logger.info("Deleted VApp %s", vApp.getName());
      } catch (Exception e) {
         vApp = vAppApi.get(href); // Refresh
         logger.warn(e, "Deleting VApp %s failed (%s)", vApp.getName(), href);
      }
   }

   public static String name(String prefix) {
      return prefix + Integer.toString(random.nextInt(Integer.MAX_VALUE));
   }

   @Override
   protected TypeToken<VCloudDirectorContext> contextType() {
      return TypeToken.of(VCloudDirectorContext.class);
   }

}
