/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.controller.registry;

import java.util.EnumSet;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.logging.ControllerLogger;
import org.jboss.as.controller.OperationDefinition;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ProxyController;
import org.jboss.as.controller.ResourceDefinition;
import org.jboss.as.controller.access.management.AccessConstraintUtilizationRegistry;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.OverrideDescriptionProvider;

/**
 * A registration for a management resource which consists of a resource description plus registered operation handlers.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public interface ManagementResourceRegistration extends ImmutableManagementResourceRegistration {

    /**
     * Get a specifically named resource that overrides this {@link PathElement#WILDCARD_VALUE wildcard registration}
     * by adding additional attributes, operations or child types.
     *
     * @param name the specific name of the resource. Cannot be {@code null} or {@link PathElement#WILDCARD_VALUE}
     *
     * @return the resource registration, <code>null</code> if there is none
     *
     * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
     */
    ManagementResourceRegistration getOverrideModel(String name);

    /**
     * Get a sub model registration.
     * <p>This method overrides the superinterface method of the same name in order to require
     * that the returned registration be mutable.
     * </p>
     *
     * @param address the address, relative to this node
     * @return the resource registration, <code>null</code> if there is none
     *
     * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
     */
    @Override
    ManagementResourceRegistration getSubModel(PathAddress address);

    /**
     * Register the existence of an addressable sub-resource of this resource.
     *
     * @param address the address of the submodel (may include a wildcard)
     * @param descriptionProvider source for descriptive information describing this
     *                            portion of the model (must not be {@code null})
     * @return a resource registration which may be used to add attributes, operations and sub-models
     *
     * @throws IllegalArgumentException if a submodel is already registered at {@code address}
     * @throws IllegalStateException if {@link #isRuntimeOnly()} returns {@code true}
     * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
     *
     * @deprecated use {@link ManagementResourceRegistration#registerSubModel(org.jboss.as.controller.ResourceDefinition)}
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    ManagementResourceRegistration registerSubModel(PathElement address, DescriptionProvider descriptionProvider);

    /**
     * Register the existence of an addressable sub-resource of this resource. Before this method returns the provided
     * {@code resourceDefinition} will be given the opportunity to
     * {@link ResourceDefinition#registerAttributes(ManagementResourceRegistration) register attributes}
     * and {@link ResourceDefinition#registerOperations(ManagementResourceRegistration) register operations}.
     *
     * @param resourceDefinition source for descriptive information describing this
     *                            portion of the model (must not be {@code null})
     * @return a resource registration which may be used to add attributes, operations and sub-models
     *
     * @throws IllegalArgumentException if a submodel is already registered at {@code address}
     * @throws IllegalStateException if {@link #isRuntimeOnly()} returns {@code true}
     * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
     */
    ManagementResourceRegistration registerSubModel(ResourceDefinition resourceDefinition);

    /**
     * Unregister the existence of an addressable sub-resource of this resource.
     *
     * @param address the child of this registry that should no longer be available
     *
     * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
     */
    void unregisterSubModel(PathElement address);

    /**
     * Gets whether this registration will always throw an exception if
     * {@link #registerOverrideModel(String, OverrideDescriptionProvider)} is invoked. An exception will always
     * be thrown for root resource registrations, {@link PathElement#WILDCARD_VALUE non-wildcard registrations}, or
     * {@link #isRemote() remote registrations}.
     *
     * @return {@code true} if an exception will not always be thrown; {@code false} if it will
     *
     * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
     */
    boolean isAllowsOverride();


    /**
     * Sets whether this model node only exists in the runtime and has no representation in the
     * persistent configuration model.
     *
     * @param runtimeOnly {@code true} if the model node will have no representation in the
     * persistent configuration model; {@code false} otherwise
     *
     * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
     */
    void setRuntimeOnly(final boolean runtimeOnly);

    /**
     * Register a specifically named resource that overrides this {@link PathElement#WILDCARD_VALUE wildcard registration}
     * by adding additional attributes, operations or child types.
     *
     * @param name the specific name of the resource. Cannot be {@code null} or {@link PathElement#WILDCARD_VALUE}
     * @param descriptionProvider provider for descriptions of the additional attributes or child types
     *
     * @return a resource registration which may be used to add attributes, operations and sub-models
     *
     * @throws IllegalArgumentException if either parameter is null or if there is already a registration under {@code name}
     * @throws IllegalStateException if {@link #isRuntimeOnly()} returns {@code true} or if {@link #isAllowsOverride()} returns false
     * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
     */
    ManagementResourceRegistration registerOverrideModel(final String name, final OverrideDescriptionProvider descriptionProvider);

    /**
     * Unregister a specifically named resource that overrides a {@link PathElement#WILDCARD_VALUE wildcard registration}
     * by adding additional attributes, operations or child types.
     *
     * @param name the specific name of the resource. Cannot be {@code null} or {@link PathElement#WILDCARD_VALUE}
     *
     * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
     */
    void unregisterOverrideModel(final String name);

    /**
     * Register an operation handler for this resource.
     *
     * @param operationName the operation name
     * @param handler the operation handler
     * @param descriptionProvider the description provider for this operation
     * @throws IllegalArgumentException if either parameter is {@code null}
     * @deprecated use {@link #registerOperationHandler(org.jboss.as.controller.OperationDefinition, org.jboss.as.controller.OperationStepHandler)}
     */
     @Deprecated
    void registerOperationHandler(String operationName, OperationStepHandler handler, DescriptionProvider descriptionProvider);

    /**
     * Register an operation handler for this resource.
     *
     * @param operationName the operation name
     * @param handler the operation handler
     * @param descriptionProvider the description provider for this operation
     * @param flags operational modifier flags for this operation (e.g. read-only)
     * @throws IllegalArgumentException if either parameter is {@code null}
     * @deprecated use {@link #registerOperationHandler(org.jboss.as.controller.OperationDefinition, org.jboss.as.controller.OperationStepHandler)}
     */
    @Deprecated
    void registerOperationHandler(String operationName, OperationStepHandler handler, DescriptionProvider descriptionProvider, EnumSet<OperationEntry.Flag> flags);

    /**
     * Register an operation handler for this resource.
     *
     * @param operationName the operation name
     * @param handler the operation handler
     * @param descriptionProvider the description provider for this operation
     * @param inherited {@code true} if the operation is inherited to child nodes, {@code false} otherwise
     * @throws IllegalArgumentException if either parameter is {@code null}
     * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
     * @deprecated use {@link #registerOperationHandler(org.jboss.as.controller.OperationDefinition, org.jboss.as.controller.OperationStepHandler)}
     */
    @Deprecated
    void registerOperationHandler(String operationName, OperationStepHandler handler, DescriptionProvider descriptionProvider, boolean inherited);

    /**
     * Register an operation handler for this resource.
     *
     * @param operationName the operation name
     * @param handler the operation handler
     * @param descriptionProvider the description provider for this operation
     * @param inherited {@code true} if the operation is inherited to child nodes, {@code false} otherwise
     * @param entryType the operation entry type
     * @throws IllegalArgumentException if either parameter is {@code null}
     * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
     * @deprecated use {@link #registerOperationHandler(org.jboss.as.controller.OperationDefinition, org.jboss.as.controller.OperationStepHandler)}
     */
    @Deprecated
    void registerOperationHandler(String operationName, OperationStepHandler handler, DescriptionProvider descriptionProvider, boolean inherited, OperationEntry.EntryType entryType);


    /**
     * Register an operation handler for this resource.
     *
     * @param operationName the operation name
     * @param handler the operation handler
     * @param descriptionProvider the description provider for this operation
     * @param inherited {@code true} if the operation is inherited to child nodes, {@code false} otherwise
     * @param flags operational modifier flags for this operation (e.g. read-only)
     * @throws IllegalArgumentException if either parameter is {@code null}
     * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
     * @deprecated use {@link #registerOperationHandler(org.jboss.as.controller.OperationDefinition, org.jboss.as.controller.OperationStepHandler)}
     */
    @Deprecated
    void registerOperationHandler(String operationName, OperationStepHandler handler, DescriptionProvider descriptionProvider, boolean inherited, EnumSet<OperationEntry.Flag> flags);

    /**
     * Register an operation handler for this resource.
     *
     * @param operationName the operation name
     * @param handler the operation handler
     * @param descriptionProvider the description provider for this operation
     * @param inherited {@code true} if the operation is inherited to child nodes, {@code false} otherwise
     * @param entryType the operation entry type
     * @param flags operational modifier flags for this operation (e.g. read-only)
     * @throws IllegalArgumentException if either parameter is {@code null}
     * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
     * @deprecated use {@link #registerOperationHandler(org.jboss.as.controller.OperationDefinition, org.jboss.as.controller.OperationStepHandler)}
     */
    @Deprecated
    void registerOperationHandler(String operationName, OperationStepHandler handler, DescriptionProvider descriptionProvider, boolean inherited, OperationEntry.EntryType entryType, EnumSet<OperationEntry.Flag> flags);

    /**
     * Register an operation handler for this resource.
     *
     * @param definition the definition of operation
     * @param handler    the operation handler
     * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
     */
    void registerOperationHandler(OperationDefinition definition, OperationStepHandler handler);

    /**
     * Register an operation handler for this resource.
     *
     * @param definition the definition of operation
     * @param handler    the operation handler
     * @param inherited  {@code true} if the operation is inherited to child nodes, {@code false} otherwise
     * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
     */
    void registerOperationHandler(OperationDefinition definition, OperationStepHandler handler, boolean inherited);

    /**
     * Unregister an operation handler for this resource.
     *
     * @param operationName       the operation name
     * @throws IllegalArgumentException if operationName is not registered
     * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
     */
    void unregisterOperationHandler(final String operationName);


    /**
     * Records that the given attribute can be both read from and written to, and
     * provides operation handlers for the read and the write.
     *
     * @param attributeName the name of the attribute. Cannot be {@code null}
     * @param readHandler the handler for attribute reads. May be {@code null}
     *                    in which case the default handling is used
     * @param writeHandler the handler for attribute writes. Cannot be {@code null}
     * @param storage the storage type for this attribute
     * @throws IllegalArgumentException if {@code attributeName} or {@code writeHandler} are {@code null}
     * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
     * @deprecated use {@link ManagementResourceRegistration#registerReadWriteAttribute(org.jboss.as.controller.AttributeDefinition, org.jboss.as.controller.OperationStepHandler, org.jboss.as.controller.OperationStepHandler)}
     */
    @Deprecated
    void registerReadWriteAttribute(String attributeName, OperationStepHandler readHandler, OperationStepHandler writeHandler, AttributeAccess.Storage storage);

    /**
     * Records that the given attribute can be both read from and written to, and
     * provides operation handlers for the read and the write. The attribute is assumed to be
     * {@link org.jboss.as.controller.registry.AttributeAccess.Storage#CONFIGURATION} unless parameter
     * {@code flags} includes {@link org.jboss.as.controller.registry.AttributeAccess.Flag#STORAGE_RUNTIME}.
     *
     * @param attributeName the name of the attribute. Cannot be {@code null}
     * @param readHandler the handler for attribute reads. May be {@code null}
     *                    in which case the default handling is used
     * @param writeHandler the handler for attribute writes. Cannot be {@code null}
     * @param flags additional flags describing this attribute
     * @throws IllegalArgumentException if {@code attributeName} or {@code writeHandler} are {@code null}
     * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
     * @deprecated use {@link ManagementResourceRegistration#registerReadWriteAttribute(org.jboss.as.controller.AttributeDefinition, org.jboss.as.controller.OperationStepHandler, org.jboss.as.controller.OperationStepHandler)}
     */
     @Deprecated
    void registerReadWriteAttribute(String attributeName, OperationStepHandler readHandler, OperationStepHandler writeHandler,
                                    EnumSet<AttributeAccess.Flag> flags);

    /**
     * Records that the given attribute can be both read from and written to, and
     * provides operation handlers for the read and the write. The attribute is assumed to be
     * {@link org.jboss.as.controller.registry.AttributeAccess.Storage#CONFIGURATION} unless parameter
     * {@code flags} includes {@link org.jboss.as.controller.registry.AttributeAccess.Flag#STORAGE_RUNTIME}.
     *
     * @param definition the attribute definition. Cannot be {@code null}
     * @param readHandler the handler for attribute reads. May be {@code null}
     *                    in which case the default handling is used
     * @param writeHandler the handler for attribute writes. Cannot be {@code null}
     *
     * @throws IllegalArgumentException if {@code definition} or {@code writeHandler} are {@code null}
     * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
     */
    void registerReadWriteAttribute(AttributeDefinition definition, OperationStepHandler readHandler, OperationStepHandler writeHandler);


    /**
     * Records that the given attribute can be read from but not written to, and
     * optionally provides an operation handler for the read.
     *
     * @param attributeName the name of the attribute. Cannot be {@code null}
     * @param readHandler the handler for attribute reads. May be {@code null}
     *                    in which case the default handling is used
     * @param storage the storage type for this attribute
     * @throws IllegalArgumentException if {@code attributeName} is {@code null}
     * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
     * @deprecated use {@link ManagementResourceRegistration#registerReadOnlyAttribute(org.jboss.as.controller.AttributeDefinition, org.jboss.as.controller.OperationStepHandler)}
     */
    @Deprecated
    void registerReadOnlyAttribute(String attributeName, OperationStepHandler readHandler, AttributeAccess.Storage storage);

    /**
     * Records that the given attribute can be read from but not written to, and
     * optionally provides an operation handler for the read. The attribute is assumed to be
     * {@link org.jboss.as.controller.registry.AttributeAccess.Storage#CONFIGURATION} unless parameter
     * {@code flags} includes {@link org.jboss.as.controller.registry.AttributeAccess.Flag#STORAGE_RUNTIME}.
     *
     * @param attributeName the name of the attribute. Cannot be {@code null}
     * @param readHandler the handler for attribute reads. May be {@code null}
     *                    in which case the default handling is used
     * @param flags additional flags describing this attribute
     * @throws IllegalArgumentException if {@code attributeName} is {@code null}
     * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
     * @deprecated use {@link ManagementResourceRegistration#registerReadOnlyAttribute(org.jboss.as.controller.AttributeDefinition, org.jboss.as.controller.OperationStepHandler)}
      */
    @Deprecated
    void registerReadOnlyAttribute(String attributeName, OperationStepHandler readHandler, EnumSet<AttributeAccess.Flag> flags);

    /**
     * Records that the given attribute can be read from but not written to, and
     * optionally provides an operation handler for the read. The attribute is assumed to be
     * {@link org.jboss.as.controller.registry.AttributeAccess.Storage#CONFIGURATION} unless parameter
     * {@code flags} includes {@link org.jboss.as.controller.registry.AttributeAccess.Flag#STORAGE_RUNTIME}.
     *
     * @param definition the attribute definition. Cannot be {@code null}
     * @param readHandler the handler for attribute reads. May be {@code null}
     *                    in which case the default handling is used
     *
     * @throws IllegalArgumentException if {@code definition} is {@code null}
     * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
     */
    void registerReadOnlyAttribute(AttributeDefinition definition, OperationStepHandler readHandler);

    /**
     * Records that the given attribute is a metric.
     *
     * @param attributeName the name of the attribute. Cannot be {@code null}
     * @param metricHandler the handler for attribute reads. Cannot be {@code null}
     *
     * @throws IllegalArgumentException if {@code attributeName} or {@code metricHandler} are {@code null}
     * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
     * @deprecated use {@link ManagementResourceRegistration#registerMetric(org.jboss.as.controller.AttributeDefinition, org.jboss.as.controller.OperationStepHandler)}
     */
    @Deprecated
    void registerMetric(String attributeName, OperationStepHandler metricHandler);

    /**
     * Records that the given attribute is a metric.
     *
     * @param definition the attribute definition. Cannot be {@code null}
     * @param metricHandler the handler for attribute reads. Cannot be {@code null}
     *
     * @throws IllegalArgumentException if {@code definition} or {@code metricHandler} are {@code null}
     * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
     */
    void registerMetric(AttributeDefinition definition, OperationStepHandler metricHandler);

    /**
     * Records that the given attribute is a metric.
     *
     * @param attributeName the name of the attribute. Cannot be {@code null}
     * @param metricHandler the handler for attribute reads. Cannot be {@code null}
     * @param flags additional flags describing this attribute
     *
     * @throws IllegalArgumentException if {@code attributeName} or {@code metricHandler} are {@code null}
     * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
     * @deprecated use {@link ManagementResourceRegistration#registerMetric(org.jboss.as.controller.AttributeDefinition, org.jboss.as.controller.OperationStepHandler)}
     */
    void registerMetric(String attributeName, OperationStepHandler metricHandler, EnumSet<AttributeAccess.Flag> flags);


    /**
     * Remove that the given attribute if present.
     *
     * @param attributeName the name of the attribute. Cannot be {@code null}
     *
     * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
     */
    void unregisterAttribute(String attributeName);

    /**
     * Register a proxy controller.
     *
     * @param address the child of this registry that should be proxied
     * @param proxyController the proxy controller
     * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
     */
    void registerProxyController(PathElement address, ProxyController proxyController);

    /**
     * Unregister a proxy controller
     *
     * @param address the child of this registry that should no longer be proxied
     * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
     */
    void unregisterProxyController(PathElement address);

    /**
     * Register an alias registration to another part of the model
     *
     * @param address the child of this registry that is an alias
     * @param aliasEntry the target model
     * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
     */
    void registerAlias(PathElement address, AliasEntry aliasEntry);

    /**
     * Unregister an alias
     *
     * @param address the child of this registry that is an alias
     * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
     */
    void unregisterAlias(PathElement address);

    /**
     * A factory for creating a new, root model node registration.
     */
    class Factory {

        private Factory() {
        }

        /**
         * Create a new root model node registration.
         *
         * @param rootModelDescriptionProvider the model description provider for the root model node
         * @return the new root model node registration
         *
         * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
         */
        public static ManagementResourceRegistration create(final DescriptionProvider rootModelDescriptionProvider) {
            return create(rootModelDescriptionProvider, null);
        }

        /**
         * Create a new root model node registration.
         *
         * @param rootModelDescriptionProvider the model description provider for the root model node
         * @param constraintUtilizationRegistry registry for recording access constraints. Can be {@code null} if
         *                                      tracking access constraint usage is not supported
         * @return the new root model node registration
         *
         * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
         */
        public static ManagementResourceRegistration create(final DescriptionProvider rootModelDescriptionProvider,
                                                            AccessConstraintUtilizationRegistry constraintUtilizationRegistry) {
            if (rootModelDescriptionProvider == null) {
                throw ControllerLogger.ROOT_LOGGER.nullVar("rootModelDescriptionProvider");
            }
            ResourceDefinition rootResourceDefinition = new ResourceDefinition() {

                @Override
                public PathElement getPathElement() {
                    return null;
                }

                @Override
                public DescriptionProvider getDescriptionProvider(ImmutableManagementResourceRegistration resourceRegistration) {
                    return rootModelDescriptionProvider;
                }

                @Override
                public void registerOperations(ManagementResourceRegistration resourceRegistration) {
                    //  no-op
                }

                @Override
                public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
                    //  no-op
                }

                @Override
                public void registerChildren(ManagementResourceRegistration resourceRegistration) {
                    //  no-op
                }
            };
            return new ConcreteResourceRegistration(null, null, rootResourceDefinition, constraintUtilizationRegistry, false);
        }

        /**
         * Create a new root model node registration.
         *
         * @param resourceDefinition the facotry for the model description provider for the root model node
         * @return the new root model node registration
         *
         * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
         */
        public static ManagementResourceRegistration create(final ResourceDefinition resourceDefinition) {
            return create(resourceDefinition, null);
        }

        /**
         * Create a new root model node registration.
         *
         * @param resourceDefinition the facotry for the model description provider for the root model node
         * @param constraintUtilizationRegistry registry for recording access constraints. Can be {@code null} if
         *                                      tracking access constraint usage is not supported
         * @return the new root model node registration
         *
         * @throws SecurityException if the caller does not have {@link ImmutableManagementResourceRegistration#ACCESS_PERMISSION}
         */
        public static ManagementResourceRegistration create(final ResourceDefinition resourceDefinition,
                                                            AccessConstraintUtilizationRegistry constraintUtilizationRegistry) {
            if (resourceDefinition == null) {
                throw ControllerLogger.ROOT_LOGGER.nullVar("rootModelDescriptionProviderFactory");
            }
            ConcreteResourceRegistration resourceRegistration = new ConcreteResourceRegistration(null, null, resourceDefinition, constraintUtilizationRegistry, false);
            resourceDefinition.registerAttributes(resourceRegistration);
            resourceDefinition.registerOperations(resourceRegistration);
            resourceDefinition.registerChildren(resourceRegistration);
            return resourceRegistration;
        }
    }
}
