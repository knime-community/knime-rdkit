/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright by KNIME AG, Zurich, Switzerland
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.com
 * email: contact@knime.com
 * ---------------------------------------------------------------------
 *
 * History
 *   Created on Dec 14, 2025 by paulbaernreuther
 */
package org.rdkit.knime.nodes.functionalgroupfilter;

import java.util.UUID;
import java.util.function.Supplier;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.persistence.NodeParametersPersistor;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.StateProvider;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.rdkit.knime.nodes.functionalgroupfilter.FunctionalGroupFilterV2NodeParameters.GroupConfigFileChooserProvider;
import org.rdkit.knime.nodes.functionalgroupfilter.RDKitFunctionalGroupFilterNodeParameters.DefinitionFileRef;

/**
 * Temporary state for the dialog to keep track of the currently running configuration file fetch and whether a fetch 
 * is ongoing.
 *
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 */
final class DialogTempState implements NodeParameters {

    /**
     * When any of the parameters affecting which group configuration is called changes, we change this uuid and cancel 
     * any running file execution with the old uuid.
     */
    @ValueProvider(SetUUIDForRunning.class)
    @ValueReference(ProcessingUuidRef.class)
    String m_lastProcessingUuid = "initialProcessingUuid";

    interface ProcessingUuidRef extends ParameterReference<String> {
    }

    /**
     * This is the trigger for the group configuration file fetch. It has to be a copy to update the below 
     * boolean beforehand.
     */
    @ValueProvider(CopyFromProcessingUuid.class)
    @ValueReference(CopiedProcessingUuidRef.class)
    String m_copiedLastProcessingUuid;

    interface CopiedProcessingUuidRef extends ParameterReference<String> {
    }

    /**
     * When the group configuration file execution finishes, we copy the processing uuid here to indicate that no file 
     * is being loaded anymore.
     */
    @ValueReference(FinishedProcessingUuidRef.class)
    @ValueProvider(CopyFromCopiedProcessingUuid.class)
    String m_finishedProcessingUuid = "initialFinishedProcessingUuid";

    interface FinishedProcessingUuidRef extends ParameterReference<String> {
    }

    static final class DoNotPersist implements NodeParametersPersistor<DialogTempState> {

        @Override
        public DialogTempState load(final NodeSettingsRO settings) throws InvalidSettingsException {
            return new DialogTempState();
        }

        @Override
        public void save(final DialogTempState param, final NodeSettingsWO settings) {
            // Do not persist
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[0][];
        }
    }

    static final class SetUUIDForRunning implements StateProvider<String> {

        private Supplier<String> m_currentValueProvider;

        @Override
        public void init(final StateProviderInitializer initializer) {
            initializer.computeAfterOpenDialog();
            initializer.computeOnValueChange(DefinitionFileRef.class);
            m_currentValueProvider = initializer.getValueSupplier(ProcessingUuidRef.class);
        }

        @Override
        public String computeState(final NodeParametersInput parametersInput) {
            final var currentValue = m_currentValueProvider.get();
            GroupConfigFileChooserProvider.cancelIfRunningAndRemove(currentValue);
            return UUID.randomUUID().toString();
        }

    }

    static final class CopyFromProcessingUuid implements StateProvider<String> {

        private Supplier<String> m_processingUuidSupplier;

        @Override
        public void init(final StateProviderInitializer initializer) {
            m_processingUuidSupplier = initializer.computeFromValueSupplier(ProcessingUuidRef.class);
        }

        @Override
        public String computeState(final NodeParametersInput parametersInput) {
            return m_processingUuidSupplier.get();
        }

    }

    static final class CopyFromCopiedProcessingUuid implements StateProvider<String> {

        private Supplier<String> m_processingUuidSupplier;

        @Override
        public void init(final StateProviderInitializer initializer) {
            m_processingUuidSupplier = initializer.computeFromValueSupplier(CopiedProcessingUuidRef.class);
        }

        @Override
        public String computeState(final NodeParametersInput parametersInput) {
            return m_processingUuidSupplier.get();
        }

    }

    static final class ProvideTrueOnUnfinishedProcessingUuid implements StateProvider<Boolean> {

        private Supplier<String> m_processingUuidSupplier;

        private Supplier<String> m_finishedProcessingUuidSupplier;

        /**
         * We deliberately do not set the same trigger as the one used in {@link GroupConfigFileChooserProvider} as
         * trigger here but rather use a copy of the processing uuid there. Otherwise, this would be called in the same
         * thread as the group configuration file fetching and is blocked until the file fetching finishes.
         */
        @Override
        public void init(final StateProviderInitializer initializer) {
            initializer.computeBeforeOpenDialog();
            m_processingUuidSupplier = initializer.computeFromValueSupplier(ProcessingUuidRef.class);
            m_finishedProcessingUuidSupplier = initializer.computeFromValueSupplier(FinishedProcessingUuidRef.class);
        }

        @Override
        public Boolean computeState(final NodeParametersInput parametersInput) {
            final var processingUuid = m_processingUuidSupplier.get();
            final var finishedUuid = m_finishedProcessingUuidSupplier.get();
            GroupConfigFileChooserProvider.remove(finishedUuid);
            return processingUuid != null && !processingUuid.equals(finishedUuid);
        }

    }
}
