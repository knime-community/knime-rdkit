/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 */

package org.rdkit.knime.nodes.functionalgroupfilter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.workflow.NativeNodeContainer;
import org.knime.core.node.workflow.NodeContext;
import org.knime.core.node.workflow.NodeID;
import org.knime.core.webui.node.dialog.defaultdialog.NodeParametersUtil;
import org.knime.filehandling.core.defaultnodesettings.filechooser.reader.SettingsModelReaderFileChooser;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.legacy.persistence.PersistWithin.PersistEmbedded;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.StateComputationAbortException;
import org.knime.node.parameters.updates.StateProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.rdkit.knime.nodes.functionalgroupfilter.DialogTempState.CopiedProcessingUuidRef;
import org.rdkit.knime.nodes.functionalgroupfilter.RDKitFunctionalGroupFilterNodeParameters.FileSwitch;

/**
 * Node parameters for RDKit Functional Group Filter.
 *
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class FunctionalGroupFilterV2NodeParameters implements NodeParameters {
    
    @PersistEmbedded
    @ValueReference(RDKitFunctionalGroupFilterNodeParametersRef.class)
    RDKitFunctionalGroupFilterNodeParameters m_deploymentCreationParameters = 
    	new RDKitFunctionalGroupFilterNodeParameters();

    interface RDKitFunctionalGroupFilterNodeParametersRef 
    	extends ParameterReference<RDKitFunctionalGroupFilterNodeParameters> {
    }

    @Persistor(DialogTempState.DoNotPersist.class)
    DialogTempState m_dialogState = new DialogTempState();

    /**
     * Extend this base class to add triggers and continue to compute a state that depends on the fetch configuration.
     */
    abstract static class DependOnFetchConfig<T> implements StateProvider<T> {

        private Supplier<RDKitFunctionalGroupFilterNodeParameters> m_parametersSupplier;

        @Override
        public final void init(final StateProviderInitializer initializer) {
            m_parametersSupplier = initializer.getValueSupplier(RDKitFunctionalGroupFilterNodeParametersRef.class);
            additionalInit(initializer);
        }

        abstract void additionalInit(final StateProviderInitializer initializer);

        protected FunctionalGroupFilterV2NodeDialog getFetchConfig(final NodeParametersInput input)
            throws InvalidSettingsException {
            final var tempParameters = new FunctionalGroupFilterV2NodeParameters();
            final var workflowSelectionParameters = m_parametersSupplier.get();
            tempParameters.m_deploymentCreationParameters = workflowSelectionParameters;

            final var tempSettings = new NodeSettings("temp");

            NodeParametersUtil.saveSettings(
            		FunctionalGroupFilterV2NodeParameters.class, tempParameters, tempSettings);

            final var nodeContainer = NodeContext.getContext().getNodeContainer();
            final var nativeNodeContainer = (NativeNodeContainer)nodeContainer;
            final var nodeCreationConfig = nativeNodeContainer.getNode()
                .getCopyOfCreationConfig().orElseThrow(IllegalStateException::new);
            final var tempConfig = new FunctionalGroupFilterV2NodeDialog(nodeCreationConfig);

            final var nodeModel = nativeNodeContainer.getNodeModel();
            final var functionalGroupFilterNodeModel = (FunctionalGroupFilterV2NodeModel)nodeModel;
            
            try {
				tempConfig.loadAdditionalSettingsFrom(tempSettings, null);
			} catch (NotConfigurableException e) {
				throw new IllegalStateException(e);
			}
            tempConfig.configureGroupConfigFileChooser(input.getInPortSpecs(), 
            		functionalGroupFilterNodeModel.getModelStatusConsumer());
            return tempConfig;
        }

        protected String getGroupConfigFilePath() {
            final var functionalGroupFilterNodeParameters = m_parametersSupplier.get();
            return functionalGroupFilterNodeParameters.m_definitionFile.getFSLocation().getPath();
        }
        
        protected boolean useDefaultGroupConfigFilePath() {
            final var functionalGroupFilterNodeParameters = m_parametersSupplier.get();
            return functionalGroupFilterNodeParameters.m_definitionFileSwitch == FileSwitch.DEFAULT_CONFIGURATION;
        }

    }

    /**
     * Provides the group configuration file chooser based on the current dialog parameters.
     */
    public static final class GroupConfigFileChooserProvider
        extends DependOnFetchConfig<WithError<SettingsModelReaderFileChooser, Exception>> {

        private Supplier<String> m_currentUuidSupplier;

        @Override
        public void additionalInit(final StateProviderInitializer initializer) {
            m_currentUuidSupplier = initializer.computeFromValueSupplier(CopiedProcessingUuidRef.class);
        }

        private static final Map<NodeID, Map<String, Thread>> RUNNING_PER_NODE = new LinkedHashMap<>();

        static NodeID getCurrentNodeID() {
            return NodeContext.getContext().getNodeContainer().getID();
        }

        static Map<String, Thread> getRunningForNode() {
            return RUNNING_PER_NODE.computeIfAbsent(getCurrentNodeID(), k -> new LinkedHashMap<>());
        }

        static void cancelIfRunningAndRemove(final String uuid) {
            synchronized (RUNNING_PER_NODE) {
                final var runningForNode = getRunningForNode();
                final var thread = runningForNode.get(uuid);
                if (thread != null) {
                    thread.interrupt();
                    runningForNode.remove(uuid);
                }
            }
        }

        static void remove(final String uuid) {
            synchronized (RUNNING_PER_NODE) {
                final var runningForNode = getRunningForNode();
                runningForNode.remove(uuid);
            }
        }

        /**
         * Called on deactivation
         *
         * @param nodeID the node ID
         */
        public static void terminateAndClearAllRunningThreads(final NodeID nodeID) {
            synchronized (RUNNING_PER_NODE) {
                final var runningForNode = RUNNING_PER_NODE.get(nodeID);
                if (runningForNode != null) {
                    for (final var thread : runningForNode.values()) {
                        thread.interrupt();
                    }
                    RUNNING_PER_NODE.remove(nodeID);
                }
            }
        }

        @Override
        public WithError<SettingsModelReaderFileChooser, Exception> computeState(
            final NodeParametersInput parametersInput) throws StateComputationAbortException {
            synchronized (RUNNING_PER_NODE) {
                final var currentUuid = m_currentUuidSupplier.get();
                final var runningForNode = getRunningForNode();
                runningForNode.put(currentUuid, Thread.currentThread());
            }
            try {
                if (useDefaultGroupConfigFilePath()) {
                	return new WithError<>(null);
				}
                final var workflowPath = getGroupConfigFilePath();
                if (workflowPath == null || workflowPath.isEmpty()) {
                	throw new IllegalStateException(
                			"Select a group configuration file by entering a path or browsing a file system.");
                }
                final var fetchConfig = getFetchConfig(parametersInput);
                try (var connection = fetchConfig.getGroupConfigFileChooserModel().getConnection()) {
                    return new WithError<>(fetchConfig.getGroupConfigFileChooserModel());
                }
            } catch (Exception e) {
                return new WithError<>(e);
            }

        }

    }
    
    /**
     * A generic type that holds either a value or an exception.
     *
     * @param value a value that is only present if no exception occurred
     * @param exception that is only present if an exception occurred
     *
     * @param <V> The value type
     * @param <E> The exception type
     */
    record WithError<V, E extends Exception>(V value, E exception) {

        /**
         * Positive case constructor.
         *
         * @param value the value
         */
        public WithError(final V value) {
            this(value, null);
        }

        /**
         * Negative case constructor.
         *
         * @param exception the exception
         */
        public WithError(final E exception) {
            this(null, exception);
        }

        /**
         * Check before accessing the exception.
         *
         * @return true if an exception occurred
         */
        public boolean hasError() {
            return exception != null;
        }

    }
    
}
