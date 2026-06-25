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

package org.rdkit.knime.nodes.structurenormalizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.knime.chem.types.SdfValue;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.node.parameters.Advanced;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.layout.After;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.legacy.updates.ColumnNameAutoGuessValueProvider;
import org.knime.node.parameters.legacy.widget.file.LegacyFileWriterWithOverwritePolicyOptions;
import org.knime.node.parameters.legacy.widget.file.LegacyFileWriterWithOverwritePolicyOptions.OverwritePolicy;
import org.knime.node.parameters.legacy.widget.file.LegacyReaderFileSelectionPersistor;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.migration.Migration;
import org.knime.node.parameters.modification.Modification;
import org.knime.node.parameters.modification.Modification.WidgetGroupModifier;
import org.knime.node.parameters.persistence.NodeParametersPersistor;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.Effect.EffectType;
import org.knime.node.parameters.updates.EffectPredicate;
import org.knime.node.parameters.updates.EffectPredicateProvider;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.StateComputationAbortException;
import org.knime.node.parameters.updates.StateProvider;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.Label;
import org.knime.node.parameters.widget.choices.StringChoice;
import org.knime.node.parameters.widget.choices.StringChoicesProvider;
import org.knime.node.parameters.widget.choices.ValueSwitchWidget;
import org.knime.node.parameters.widget.choices.filter.TwinlistWidget;
import org.knime.node.parameters.widget.choices.util.ColumnSelectionUtil;
import org.knime.node.parameters.widget.choices.util.CompatibleColumnsProvider;
import org.knime.node.parameters.widget.file.FileReaderWidget;
import org.knime.node.parameters.widget.file.FileSelection;
import org.knime.node.parameters.widget.file.FileWriterWidget;
import org.knime.node.parameters.widget.text.TextAreaWidget;
import org.rdkit.knime.util.RDKitAdapterCellSupport;
import org.rdkit.knime.util.RDKitLegacyPersistors.DefaultFileSwitchMigration;
import org.rdkit.knime.util.SettingsUtils;

/**
 * Node parameters for RDKit Structure Normalizer.
 *
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class RDKitStructureNormalizerV2NodeParameters implements NodeParameters {

    @Section(title = "Passed Output")
    interface PassedOutputSection {
    }

    @Section(title = "Failed Output")
    @After(PassedOutputSection.class)
    interface FailedOutputSection {
    }

    @After(FailedOutputSection.class)
    @Section(title = "Advanced")
    @Advanced
    interface AdvancedSection {
    }

    @Widget(title = "Mol column", description = """
            The input column with SDF, SMILES or RDKit Molecules. The latter ones are treated as SDF values. \
            SMILES input will be converted internally into mol blocks before the normalization is done.
            """)
    @Persist(configKey = "input_column")
    @ChoicesProvider(InputColumnChoicesProvider.class)
    @ValueProvider(InputColumnAutoGuessProvider.class)
    @ValueReference(InputColumnRef.class)
    String m_inputColumn;

    static final class InputColumnRef implements ParameterReference<String> {
    }

    @Persistor(DoNotPersistString.class)
    @ValueProvider(PrevInputColumnStateProvider.class)
    @ValueReference(PrevInputColumnRef.class)
    String m_prevInputColumnString;

    static final class PrevInputColumnRef implements ParameterReference<String> {
    }

    @Layout(PassedOutputSection.class)
    @Widget(title = "Corrected structure column name", description = """
            The name of the column that will contain the original or corrected structure, in case that any \
            normalization has been applied.
            """)
    @Persist(configKey = "passed_corrected_structure_column")
    @ValueProvider(PassedCorrectedStructureColumnAutoGuessProvider.class)
    @ValueReference(PassedCorrectedStructureColumnRef.class)
    String m_passedCorrectedStructureColumn;

    static final class PassedCorrectedStructureColumnRef implements ParameterReference<String> {
    }

    static final class PassedCorrectedStructureColumnAutoGuessProvider
            extends StructureNormalizerOutputColumnAutoGuessProvider {

        protected PassedCorrectedStructureColumnAutoGuessProvider() {
            super(PassedCorrectedStructureColumnRef.class,
                    "- " + RDKitStructureNormalizerV2NodeModel.DEFAULT_POSTFIX_PASSED_CORRECTED);
        }

    }

    @Layout(PassedOutputSection.class)
    @Widget(title = "Flags column name", description = """
            The name of the column that will contain the warning flags. This is a bit mask where each bit has a \
            certain meaning as described in the node description.
            """)
    @Persist(configKey = "passed_flags_column")
    @ValueProvider(PassedFlagsColumnAutoGuessProvider.class)
    @ValueReference(PassedFlagsColumnRef.class)
    String m_passedFlagsColumn;

    static final class PassedFlagsColumnRef implements ParameterReference<String> {
    }

    static final class PassedFlagsColumnAutoGuessProvider extends StructureNormalizerOutputColumnAutoGuessProvider {

        private Supplier<String> m_passedCorrectedStructureColumnNameSupplier;

        protected PassedFlagsColumnAutoGuessProvider() {
            super(PassedFlagsColumnRef.class, "- " + RDKitStructureNormalizerV2NodeModel.DEFAULT_POSTFIX_PASSED_FLAGS);
        }

        @Override
        public void init(final StateProviderInitializer initializer) {
            super.init(initializer);
            m_passedCorrectedStructureColumnNameSupplier = initializer
                    .getValueSupplier(PassedCorrectedStructureColumnRef.class);
        }

        @Override
        protected String[] getAdditionalColumnNames(final NodeParametersInput parametersInput,
                final String currentInputColumnName) {
            return new String[] { m_passedCorrectedStructureColumnNameSupplier.get() };
        }

    }

    @Layout(PassedOutputSection.class)
    @Widget(title = "Warning messages column name", description = """
            The name of the column that will contain the warning messages associated with the flags. \
            The "Passed Molecules" table contains only warnings, which are not classified as errors.
            """)
    @Persist(configKey = "passed_warning_messages_column")
    @ValueProvider(PassedWarningMessagesColumnAutoGuessProvider.class)
    @ValueReference(PassedWarningMessagesColumnRef.class)
    String m_passedWarningMessagesColumn;

    static final class PassedWarningMessagesColumnRef implements ParameterReference<String> {
    }

    static final class PassedWarningMessagesColumnAutoGuessProvider
            extends StructureNormalizerOutputColumnAutoGuessProvider {

        private Supplier<String> m_passedCorrectedStructureColumnNameSupplier;

        private Supplier<String> m_passedFlagsColumnNameSupplier;

        protected PassedWarningMessagesColumnAutoGuessProvider() {
            super(PassedWarningMessagesColumnRef.class,
                    "- " + RDKitStructureNormalizerV2NodeModel.DEFAULT_POSTFIX_PASSED_WARNINGS);
        }

        @Override
        public void init(final StateProviderInitializer initializer) {
            super.init(initializer);
            m_passedCorrectedStructureColumnNameSupplier = initializer
                    .getValueSupplier(PassedCorrectedStructureColumnRef.class);
            m_passedFlagsColumnNameSupplier = initializer.getValueSupplier(PassedFlagsColumnRef.class);
        }

        @Override
        protected String[] getAdditionalColumnNames(final NodeParametersInput parametersInput,
                final String currentInputColumnName) {
            return new String[] { m_passedCorrectedStructureColumnNameSupplier.get(),
                    m_passedFlagsColumnNameSupplier.get() };
        }

    }

    @Layout(FailedOutputSection.class)
    @Widget(title = "Flags column name", description = """
            The name of the column that will contain the error flags. This is a bit mask where each bit has a \
            certain meaning as described in the node description.
            """)
    @Persist(configKey = "failed_error_flags_column")
    @ValueProvider(FailedFlagsColumnAutoGuessProvider.class)
    @ValueReference(FailedFlagsColumnRef.class)
    String m_failedFlagsColumn;

    static final class FailedFlagsColumnRef implements ParameterReference<String> {
    }

    static final class FailedFlagsColumnAutoGuessProvider extends StructureNormalizerOutputColumnAutoGuessProvider {

        protected FailedFlagsColumnAutoGuessProvider() {
            super(FailedFlagsColumnRef.class, "- " + RDKitStructureNormalizerV2NodeModel.DEFAULT_POSTFIX_FAILED_FLAGS);
        }

    }

    @Layout(FailedOutputSection.class)
    @Widget(title = "Error messages column name", description = """
            The name of the column that will contain the error messages associated with the flags. \
            The "Failed Molecules" table contains only errors that prevented successful normalization.
            """)
    @Persist(configKey = "failed_error_messages_column")
    @ValueProvider(FailedErrorMessagesColumnAutoGuessProvider.class)
    @ValueReference(FailedErrorMessagesColumnRef.class)
    String m_failedErrorMessagesColumn;

    static final class FailedErrorMessagesColumnRef implements ParameterReference<String> {
    }

    static final class FailedErrorMessagesColumnAutoGuessProvider
            extends StructureNormalizerOutputColumnAutoGuessProvider {

        private Supplier<String> m_failedFlagsColumnNameSupplier;

        protected FailedErrorMessagesColumnAutoGuessProvider() {
            super(FailedErrorMessagesColumnRef.class,
                    "- " + RDKitStructureNormalizerV2NodeModel.DEFAULT_POSTFIX_FAILED_ERRORS);
        }

        @Override
        public void init(final StateProviderInitializer initializer) {
            super.init(initializer);
            m_failedFlagsColumnNameSupplier = initializer.getValueSupplier(FailedFlagsColumnRef.class);
        }

        @Override
        protected String[] getAdditionalColumnNames(final NodeParametersInput parametersInput,
                final String currentInputColumnName) {
            return new String[] { m_failedFlagsColumnNameSupplier.get() };
        }

    }

    @Layout(FailedOutputSection.class)
    @Widget(title = "Logfile output (optional)", description = """
            A logfile can be specified here which logs additional output in case of normalizations of \
               structures. It can be used for informal purposes only. There is no need to define a logfile \
               for the node to work correctly. Leave this field empty to disable logging.
            """)
    @ValueSwitchWidget
    @ValueReference(LogFileSwitchRef.class)
    @Migration(LogFileSwitchMigration.class)
    LogFileSwitch m_logFileSwitch = LogFileSwitch.DISABLE_LOGGING;

    static final class LogFileSwitchRef implements ParameterReference<LogFileSwitch> {
    }

    static final class LogFileSwitchMigration extends DefaultFileSwitchMigration<LogFileSwitch> {

        protected LogFileSwitchMigration() {
            super("logFileSwitch", "log_file", LogFileSwitch.DISABLE_LOGGING, LogFileSwitch.ENABLE_LOGGING);
        }

    }

    @Layout(FailedOutputSection.class)
    @Persist(configKey = "log_file")
    @Effect(predicate = IsLoggingEnabled.class, type = EffectType.SHOW)
    @Modification(LogFileModifier.class)
    LegacyFileWriterWithOverwritePolicyOptions m_logFile = new LegacyFileWriterWithOverwritePolicyOptions();

    static final class LogFileRef implements ParameterReference<LegacyFileWriterWithOverwritePolicyOptions> {
    }

    static final class LogFileModifier implements LegacyFileWriterWithOverwritePolicyOptions.Modifier {

        @Override
        public void modify(final WidgetGroupModifier group) {
            findFileSelection(group).modifyAnnotation(Widget.class).withProperty("title", "Selected log file")
                    .withProperty("description", """
                            Select the file to which the log output of the node will be written. If the file already
                            exists, the behavior depends on the selected overwrite policy. If no file is selected, no
                            log output will be produced.
                            """).modify();
            findFileSelection(group).modifyAnnotation(FileWriterWidget.class).withProperty("fileExtension", "log")
                    .modify();
            findCreateMissingFolders(group).modifyAnnotation(Widget.class).withProperty("description", """
                    Select if the folders of the selected output location should be created if they do not already \
                    exist. If this option is unchecked, the node will fail if a folder does not exist.
                    """).modify();
            restrictOverwritePolicyOptions(group, LogFileOverwritePolicyProvider.class);
        }

    }

    static final class LogFileOverwritePolicyProvider
            extends LegacyFileWriterWithOverwritePolicyOptions.OverwritePolicyChoicesProvider {

        @Override
        protected List<OverwritePolicy> getChoices() {
            return List.of(OverwritePolicy.fail, OverwritePolicy.overwrite);
        }

    }

    @Layout(FailedOutputSection.class)
    @Widget(title = "Warning codes to treat as failures", description = """
            Define here which warning flags should be treated as errors. If defined as an error, the rows will appear \
            in the second output table ("Failed Molecules" table). Hover over a code to see its description and value.
            """)
    @Persist(configKey = "additional_failure_codes")
    @ChoicesProvider(StruCheckCodeChoicesProvider.class)
    @TwinlistWidget(includedLabel = "Treat as Failure", excludedLabel = "Treat as Warning")
    String[] m_additionalFailureCodes = new String[0];

    @Layout(AdvancedSection.class)
    @Widget(title = "Transformation configuration file (.trn) (optional)", description = """
            Lets the user define a customized transformation configuration file. The default built-in
            <a href="https://github.com/rdkit/rdkit/blob/master/Data/struchk/checkfgs.trn">configuration file</a> will
            be used when no file is specified. The file must have the .trn extension.
            """, advanced = true)
    @ValueSwitchWidget
    @ValueReference(TransformationConfigFileSwitchRef.class)
    @Migration(TransformationConfigFileSwitchMigration.class)
    FileSwitch m_transformationConfigFileSwitch = FileSwitch.DEFAULT_CONFIGURATION;

    static final class TransformationConfigFileSwitchRef implements ParameterReference<FileSwitch> {
    }

    static final class TransformationConfigFileSwitchMigration extends DefaultFileSwitchMigration<FileSwitch> {

        protected TransformationConfigFileSwitchMigration() {
            super("transformationConfigFileSwitch", "transformation_configuration_file",
                    FileSwitch.DEFAULT_CONFIGURATION, FileSwitch.FILE_SELECTION);
        }
    }

    @Layout(AdvancedSection.class)
    @Advanced
    @Persistor(TransformationConfigFilePersistor.class)
    @Widget(title = "Selected transformation config file", description = """
            Specify a custom transformation configuration file to use. The file must have the
            .trn extension.
            """)
    @FileReaderWidget(fileExtensions = { "trn" })
    @Effect(predicate = IsTransformationConfigFileSelectionEnabled.class, type = EffectType.SHOW)
    @ValueReference(TransformationConfigFileRef.class)
    FileSelection m_transformationConfigFile = new FileSelection();

    static final class TransformationConfigFileRef implements ParameterReference<FileSelection> {
    }

    @Layout(AdvancedSection.class)
    @Widget(title = "Augmented atoms configuration file (.chk) (optional)", description = """
            Lets the user define a customized augmented atoms configuration file. The default built-in
            <a href="https://github.com/rdkit/rdkit/blob/master/Data/struchk/checkfgs.chk">configuration file</a> will
            be used when no file is specified. The file must have the .chk extension.
            """, advanced = true)
    @ValueSwitchWidget
    @ValueReference(AugmentedAtomsConfigFileSwitchRef.class)
    @Migration(AugmentedAtomsConfigFileSwitchMigration.class)
    FileSwitch m_augmentedAtomsConfigFileSwitch = FileSwitch.DEFAULT_CONFIGURATION;

    static final class AugmentedAtomsConfigFileSwitchRef implements ParameterReference<FileSwitch> {
    }

    static final class AugmentedAtomsConfigFileSwitchMigration extends DefaultFileSwitchMigration<FileSwitch> {

        protected AugmentedAtomsConfigFileSwitchMigration() {
            super("augmentedAtomsConfigFileSwitch", "augmented_atoms_configuration_file",
                    FileSwitch.DEFAULT_CONFIGURATION, FileSwitch.FILE_SELECTION);
        }

    }

    @Layout(AdvancedSection.class)
    @Advanced
    @Persistor(AugmentedAtomsConfigFilePersistor.class)
    @Widget(title = "Selected augmented atoms config file", description = """
             		Specify a custom augmented atoms configuration file to use. The file must have the .chk extension.
            """)
    @FileReaderWidget(fileExtensions = { "chk" })
    @Effect(predicate = IsAugmentedAtomicConfigFileSelectionEnabled.class, type = EffectType.SHOW)
    @ValueReference(AugmentedAtomsConfigFileRef.class)
    FileSelection m_augmentedAtomsConfigFile = new FileSelection();

    static final class AugmentedAtomsConfigFileRef implements ParameterReference<FileSelection> {
    }

    @Layout(AdvancedSection.class)
    @Widget(title = "Advanced switches (optional)", description = """
            Configure here certain switches that influence how the Structure Normalizer performs its work:
            """, advanced = true)
    @Persist(configKey = "switches")
    @ChoicesProvider(StruCheckSwitchChoicesProvider.class)
    @TwinlistWidget(includedLabel = "To Be Used", excludedLabel = "Not Used")
    String[] m_switches = new String[0];

    @Layout(AdvancedSection.class)
    @Widget(title = "Additional options", description = """
            Normally, there is no need to change these settings. However, if you are familiar with the underlying \
            StruChk tool, you may define here manually options to be passed to it. These options come in addition \
            to the switches defined above.
            """, advanced = true)
    @Persist(configKey = "advanced_options")
    @TextAreaWidget(rows = 5)
    String m_advancedOptions = RDKitStructureNormalizerV2NodeModel.DEFAULT_ADVANCED_OPTIONS;

    static final class IsLoggingEnabled implements EffectPredicateProvider {

        @Override
        public EffectPredicate init(PredicateInitializer i) {
            return i.getEnum(LogFileSwitchRef.class).isOneOf(LogFileSwitch.ENABLE_LOGGING);
        }

    }

    static final class IsTransformationConfigFileSelectionEnabled implements EffectPredicateProvider {

        @Override
        public EffectPredicate init(PredicateInitializer i) {
            return i.getEnum(TransformationConfigFileSwitchRef.class).isOneOf(FileSwitch.FILE_SELECTION);
        }

    }

    static final class IsAugmentedAtomicConfigFileSelectionEnabled implements EffectPredicateProvider {

        @Override
        public EffectPredicate init(PredicateInitializer i) {
            return i.getEnum(AugmentedAtomsConfigFileSwitchRef.class).isOneOf(FileSwitch.FILE_SELECTION);
        }

    }

    static final class InputColumnAutoGuessProvider extends ColumnNameAutoGuessValueProvider {

        protected InputColumnAutoGuessProvider() {
            super(InputColumnRef.class);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Optional<DataColumnSpec> autoGuessColumn(final NodeParametersInput parametersInput) {
            return ColumnSelectionUtil.getFirstCompatibleColumnOfFirstPort(parametersInput,
                    RDKitAdapterCellSupport.expandByAdaptableTypes(new Class[] { SdfValue.class, SmilesValue.class }));
        }

    }

    static final class InputColumnChoicesProvider extends CompatibleColumnsProvider {

        @SuppressWarnings("unchecked")
        protected InputColumnChoicesProvider() {
            super(Arrays.asList(
                    RDKitAdapterCellSupport.expandByAdaptableTypes(new Class[] { SdfValue.class, SmilesValue.class })));
        }

    }

    static final class StruCheckCodeChoicesProvider implements StringChoicesProvider {

        @Override
        public List<StringChoice> computeState(NodeParametersInput context) {
            List<StringChoice> listDefaultNonErrorCodes = new ArrayList<>();
            for (final StruCheckCode code : StruCheckCode.values()) {
                if (!code.isError()) {
                    listDefaultNonErrorCodes
                            .add(new StringChoice(code.name(), "%s (%s)".formatted(code.name(), code.getValue())));
                }
            }
            return listDefaultNonErrorCodes;
        }

    }

    static final class StruCheckSwitchChoicesProvider implements StringChoicesProvider {

        @Override
        public List<StringChoice> computeState(NodeParametersInput context) {
            return Arrays.stream(StruCheckSwitch.values()).map(swtch -> new StringChoice(swtch.name(),
                    "%s - %s".formatted(swtch.name(), swtch.getShortDescription()))).toList();
        }

    }

    static final class TransformationConfigFilePersistor extends LegacyReaderFileSelectionPersistor {

        protected TransformationConfigFilePersistor() {
            super("transformation_configuration_file");
        }

    }

    static final class AugmentedAtomsConfigFilePersistor extends LegacyReaderFileSelectionPersistor {

        protected AugmentedAtomsConfigFilePersistor() {
            super("augmented_atoms_configuration_file");
        }

    }

    enum LogFileSwitch {

        @Label(value = "Disable logging", description = "No log file will be created and no log output will be produced.")
        DISABLE_LOGGING, //
        @Label(value = "Enable logging", description = """
                A log file will be created and log output will be produced according to the selected options.
                """)
        ENABLE_LOGGING;

    }

    enum FileSwitch {

        @Label(value = "Default configuration", description = "Loads the default configuration file.")
        DEFAULT_CONFIGURATION, //
        @Label(value = "File selection", description = "Specify a configuration file.")
        FILE_SELECTION;

    }

    record PrevAndCurrentInputColumn(String prevInputColumn, String currInputColumn) {
    }

    static final class PrevAndCurrentInputColumnStateProvider implements StateProvider<PrevAndCurrentInputColumn> {

        private Supplier<String> m_currInputColumn;
        private Supplier<String> m_prevInputColumn;

        @Override
        public void init(StateProviderInitializer initializer) {
            m_currInputColumn = initializer.computeFromValueSupplier(InputColumnRef.class);
            m_prevInputColumn = initializer.getValueSupplier(PrevInputColumnRef.class);
        }

        @Override
        public PrevAndCurrentInputColumn computeState(NodeParametersInput parametersInput)
                throws StateComputationAbortException {
            return new PrevAndCurrentInputColumn(m_prevInputColumn.get(), m_currInputColumn.get());
        }

    }

    static final class PrevInputColumnStateProvider implements StateProvider<String> {

        private Supplier<PrevAndCurrentInputColumn> m_prevAndCurrInputColumn;

        @Override
        public void init(StateProviderInitializer initializer) {
            initializer.computeBeforeOpenDialog();
            m_prevAndCurrInputColumn = initializer
                    .computeFromProvidedState(PrevAndCurrentInputColumnStateProvider.class);
        }

        @Override
        public String computeState(NodeParametersInput parametersInput) throws StateComputationAbortException {
            return m_prevAndCurrInputColumn.get().currInputColumn();
        }

    }

    /**
     * Abstract base for output column name auto-guess providers in this node.
     * Subscribes to {@link PrevAndCurrentInputColumnStateProvider} to obtain both
     * the previous and current input column name, then decides whether to suggest a
     * fresh name or preserve the existing one.
     *
     * <p>
     * A fresh name is suggested when the existing output column name is empty, or
     * when it exactly matches the name that would have been auto-generated for the
     * previous input column (meaning the user has not manually customized it).
     */
    static abstract class StructureNormalizerOutputColumnAutoGuessProvider implements StateProvider<String> {

        private final String m_suffix;

        private final Class<? extends ParameterReference<String>> m_resultColumnRef;

        private Supplier<PrevAndCurrentInputColumn> m_prevAndCurrInputColumn;

        private Supplier<String> m_resultColumnNameSupplier;

        protected StructureNormalizerOutputColumnAutoGuessProvider(
                final Class<? extends ParameterReference<String>> resultColumnRef, final String suffix) {
            m_resultColumnRef = resultColumnRef;
            m_suffix = suffix;
        }

        @Override
        public void init(final StateProviderInitializer initializer) {
            m_prevAndCurrInputColumn = initializer
                    .computeFromProvidedState(PrevAndCurrentInputColumnStateProvider.class);
            m_resultColumnNameSupplier = initializer.getValueSupplier(m_resultColumnRef);
        }

        @Override
        public String computeState(final NodeParametersInput parametersInput) throws StateComputationAbortException {
            final PrevAndCurrentInputColumn prevAndCurr = m_prevAndCurrInputColumn.get();
            final String currInputCol = prevAndCurr.currInputColumn();
            final String prevInputCol = prevAndCurr.prevInputColumn();

            if (currInputCol == null || currInputCol.isEmpty()) {
                throw new StateComputationAbortException();
            }

            if (prevInputCol == null || prevInputCol.isEmpty()) {
                throw new IllegalStateException(
                        "Previous input column name should not be null or empty if the current is not null or empty.");
            }

            final String currentOutputName = m_resultColumnNameSupplier.get();
            final String expectedOutputName = prevInputCol + " " + m_suffix;

            final var shouldAutoGuess = currentOutputName == null || currentOutputName.isEmpty()
                    || currentOutputName.equals(expectedOutputName)
                    || currentOutputName.matches(Pattern.quote(expectedOutputName) + " \\(#\\d+\\)");

            if (!shouldAutoGuess) {
                throw new StateComputationAbortException();
            }

            final var inSpec = parametersInput.getInTableSpec(0).orElse(null);
            if (inSpec == null) {
                throw new StateComputationAbortException();
            }

            final List<String> existingNames = SettingsUtils.createMergedColumnNameList(inSpec,
                    getAdditionalColumnNames(parametersInput, currInputCol), null);

            final String suggestedOutputName = currInputCol + " " + m_suffix;
            String result = suggestedOutputName;
            int uniquifier = 1;
            while (existingNames.contains(result)) {
                result = suggestedOutputName + " (#" + uniquifier + ")";
                uniquifier++;
            }
            return result;
        }

        protected String[] getAdditionalColumnNames(final NodeParametersInput parametersInput,
                final String currentInputColumnName) {
            return null;
        }

    }

    static final class DoNotPersistString implements NodeParametersPersistor<String> {

        @Override
        public String load(NodeSettingsRO settings) throws InvalidSettingsException {
            return "";
        }

        @Override
        public void save(String param, NodeSettingsWO settings) {
        }

        @Override
        public String[][] getConfigPaths() {
            return null;
        }

    }

}
