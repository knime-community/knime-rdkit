package org.rdkit.knime.nodes.rgroupdecomposition;

import java.util.Arrays;

import org.RDKit.RGroupLabelling;
import org.RDKit.RGroupLabels;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings.DefaultNodeSettingsContext;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Section;
import org.knime.core.webui.node.dialog.defaultdialog.layout.After;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Layout;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.TextMessage.MessageType;
import org.knime.core.webui.node.dialog.defaultdialog.widget.TextMessage.SimpleTextMessageProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.choices.IdAndText;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ValueSwitchWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Label;
import org.knime.core.webui.node.dialog.defaultdialog.widget.RichTextInputWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.TextInputWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.TextMessage;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Effect;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Predicate;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.PredicateProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Effect.EffectType;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.PredicateProvider.PredicateInitializer;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.api.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.api.Persistor;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.persistors.settingsmodel.EnumSettingsModelStringPersistor;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.persistors.settingsmodel.SettingsModelColumnNamePersistor;

/**
 * Settings for the RDKit R-Group Decomposition node using the webui framework.
 * 
 * @author Marc Lehner, KNIME GmbH, Zurich, Switzerland
 * 
 */
public final class RDKitRGroupDecompositionNodeSettings implements DefaultNodeSettings {
	
	///////////////////////////////////////////////////////////////////////////
	// Persistors
	///////////////////////////////////////////////////////////////////////////
	public enum LabelSettingsEnum{
		@Label("Auto Detect") AutoDetect,
		@Label("Isotope Labels") IsotopeLabels,
		@Label("Atom Map Labels") AtomMapLabels,
		@Label("Atom Index Labels") AtomIndexLabels,
		@Label("Relabel Duplicate Labels") RelabelDuplicateLabels,
		@Label("Dummy Atom Labels") DummyAtomLabels,
		@Label("MDL RGroup Labels") MDLRGroupLabels;
	}
	
	public enum LabelingSettingsEnum{
		@Label("Atom Map") AtomMap,
		@Label("Isotope") Isotope,
		@Label("MDLR Group") MDLRGroup;
	}
	
	public static final class RDKitMoleculeColumnTable2ChoicesProvider extends RDKitMoleculeColumnChoicesProvider {
		public RDKitMoleculeColumnTable2ChoicesProvider() {
			super(1);
		}
	}
	
	static final class IsCoreInputConnectedPredicate implements PredicateProvider {
		@Override
		public Predicate init(PredicateInitializer initializer) {
			return initializer.getConstant(context -> context.getDataTableSpec(1).isPresent());
		}		
	}
	
	static final class LabelsPersistor extends EnumSettingsModelStringPersistor<LabelSettingsEnum> {
		private static final String KEY = "labels";
		protected LabelsPersistor(String configKey, Class<LabelSettingsEnum> enumClass) {
			super(configKey, enumClass);
		}
		
	}
	
	static final class LabelsChoices implements ChoicesProvider {
		@Override
		public String[] choices(final DefaultNodeSettingsContext context) {
			return Arrays.stream(LabelSettingsEnum.values()).map(Enum::name).toArray(String[]::new);
		}
	}
	
	
	static final class LabelingPersistor extends EnumSettingsModelStringPersistor<LabelingSettingsEnum> {
        private static final String KEY = "labeling";

		protected LabelingPersistor(String configKey, Class<LabelingSettingsEnum> enumClass) {
			super(configKey, enumClass);
		}
	}
	
	static final class LabelingChoices implements ChoicesProvider {
		@Override
		public String[] choices(final DefaultNodeSettingsContext context) {
			return Arrays.stream(LabelingSettingsEnum.values()).map(Enum::name).toArray(String[]::new);
		}
	}
	
	static final class CoreInputManagedByPortMessage implements SimpleTextMessageProvider {

        @Override
        public boolean showMessage(final DefaultNodeSettingsContext context) {
            return context.getDataTableSpec(1).isPresent();
        }

        @Override
        public String title() {
            return "Core input settings controlled by input port";
        }

        @Override
        public String description() {
            return "Remove the input port to change the settings";
        }

        @Override
        public MessageType type() {
            return MessageType.INFO;
        }

    }
	
	static final class CoreDefaultInputMessage implements SimpleTextMessageProvider {

		@Override
		public boolean showMessage(final DefaultNodeSettingsContext context) {
			return !context.getDataTableSpec(1).isPresent();
		}

		@Override
		public String title() {
			return "There is no core table connected, using SMARTS patterns";
		}

		@Override
		public String description() {
			return "If you want to use the core table, connect it to the second input port.";
		}

		@Override
		public MessageType type() {
			return MessageType.INFO;
		}

	}
	

    ///////////////////////////////////////////////////////////////////////////
    // Sections
    ///////////////////////////////////////////////////////////////////////////

    @Section(title = "Input Molecules")
    interface InputMoleculesSection {}

    @Section(title = "Input Core Patterns")
    @After(InputMoleculesSection.class)
    interface InputCorePatternSection {}

    @Section(title = "Output Handling")
    @After(InputCorePatternSection.class)
    interface OutputHandlingSection {}

    @Section(title = "Advanced R-Group Options", advanced = true)
    @After(OutputHandlingSection.class)
    interface AdvancedSection {}

    ///////////////////////////////////////////////////////////////////////////
    // Fields in Input Molecules Section
    ///////////////////////////////////////////////////////////////////////////

    @Persist(configKey = "input_column")
    @Widget(title = "RDKit Mol column",
        description = "The column in the first input table that contains the main molecules for R-Group Decomposition.")
    @ChoicesWidget(choices = RDKitMoleculeColumnChoicesProvider.class)
    @Layout(InputMoleculesSection.class)
    String m_inputColumn;

    ///////////////////////////////////////////////////////////////////////////
    // Fields in Input Scaffolds Section
    ///////////////////////////////////////////////////////////////////////////
    
    @TextMessage(CoreInputManagedByPortMessage.class)
    @Layout(InputCorePatternSection.class)
    Void m_showDefaultCorePatterns;

    @Persist(configKey = "cores_input_column")
    @Widget(title = "Core Input Column",
        description = "The optional second input table column that provides scaffold molecules. "
                   + "If connected, this takes precedence over any text-based scaffolds specified below.")
    @ChoicesWidget(choices = RDKitMoleculeColumnTable2ChoicesProvider.class)
    @Layout(InputCorePatternSection.class)
    @Effect(type = EffectType.SHOW, predicate = IsCoreInputConnectedPredicate.class)
    String m_coresInputColumn;    

    @Persist(configKey = "strict_parsing")
    @Widget(title = "Strict Parsing of Mol Blocks",
        description = "If enabled and the second input table's scaffold column is SDF, the parser is strict.")
    @Layout(InputCorePatternSection.class)
    @Effect(type = EffectType.SHOW, predicate = IsCoreInputConnectedPredicate.class)
    boolean m_strictParsing;
    
    @TextMessage(CoreDefaultInputMessage.class)
    @Layout(InputCorePatternSection.class)
    Void m_showDefaultCorePatterns1;

    @Persist(configKey = "smarts_value")
    @Widget(title = "Core SMARTS (multiline)",
        description = "One or more SMARTS patterns that define scaffolds if no second input table is connected. "
                    + "Separate multiple patterns by new lines.")
    @Layout(InputCorePatternSection.class)
    @Effect(type = EffectType.HIDE, predicate = IsCoreInputConnectedPredicate.class)
    //TODO: Change to MultiLineTextInput as soon as it is available
    @TextInputWidget
    String m_smartsValue;

    ///////////////////////////////////////////////////////////////////////////
    // Fields in Output Handling Section
    ///////////////////////////////////////////////////////////////////////////

    @Persist(configKey = "remove_empty_columns")
    @Widget(title = "Remove empty Rx columns",
        description = "If enabled, any R-Group column that remains empty for all rows is removed from the output.")
    @Layout(OutputHandlingSection.class)
    boolean m_removeEmptyColumns;

    @Persist(configKey = "fail_for_no_match")
    @Widget(title = "Fail if no cores match", 
        description = "If enabled, the node fails execution if there are no matches.")
    @Layout(OutputHandlingSection.class)
    boolean m_failForNoMatch;

    @Persist(configKey = "add_matching_smarts_core")
    @Widget(title = "Add matching SMARTS core",
        description = "If enabled, a new column is appended containing the core that matched, in SMARTS form.")
    @Layout(OutputHandlingSection.class)
    boolean m_addMatchingSmartsCore;

    @Persist(configKey = "new_matching_smarts_core_column_name")
    @Widget(title = "Core column name (SMARTS)",
        description = "The new column name for the matched SMARTS-based core.")
    @Layout(OutputHandlingSection.class)
    String m_newMatchingSmartsCoreColumnName;

    @Persist(configKey = "add_matching_explicit_core")
    @Widget(title = "Add matching explicit core",
        description = "If enabled, a new column is appended containing the matched substructure of each molecule.")
    @Layout(OutputHandlingSection.class)
    boolean m_addMatchingExplicitCore;

    @Persist(configKey = "new_matching_explicit_core_column_name")
    @Widget(title = "Explicit core column name",
        description = "The new column name for the matched explicit substructure.")
    @Layout(OutputHandlingSection.class)
    String m_newMatchingExplicitCoreColumnName;

    @Persist(configKey = "use_atom_maps_for_explicit_core")
    @Widget(title = "Use atom maps",
        description = "Determines whether to embed map numbers on each mapped atom in the explicit core.")
    @Layout(OutputHandlingSection.class)
    boolean m_useAtomMapsForExplicitCore;

    @Persist(configKey = "use_r_labels_for_explicit_core")
    @Widget(title = "Use R-labels",
        description = "If enabled, each mapped atom in the explicit core is assigned an R-label property.")
    @Layout(OutputHandlingSection.class)
    boolean m_useRLabelsForExplicitCore;

    ///////////////////////////////////////////////////////////////////////////
    // Fields in Advanced R-Group Section
    ///////////////////////////////////////////////////////////////////////////

    @Persistor(value = LabelsPersistor.class)
    @Widget(title = "R-Group labels",
        description = "Configures the labeling style for the R-group decomposition. "
                    + "Multiple label types can be enabled or disabled.",
        advanced = true)
    @ChoicesWidget(choices = LabelsChoices.class)
    @Layout(AdvancedSection.class)
    String[] m_labels;

    @Persist(configKey = "matching_strategy")
    @Widget(title = "Matching strategy",
        description = "Controls the algorithm used for the R-group decomposition.",
        advanced = true)
    @Layout(AdvancedSection.class)
    Matching m_matchingStrategy;

    @Persistor(value = LabelingPersistor.class)
    @Widget(title = "Additional labeling options",
        description = "Specifies special labeling for the R-groups in the output, e.g., whether to use atom maps, "
                    + "MDL R-group notation, etc.",
        advanced = true)
    @ChoicesWidget(choices = LabelingChoices.class)
    @Layout(AdvancedSection.class)
    String[] m_labeling;

    @Persist(configKey = "core_alignment")
    @Widget(title = "Core alignment",
        description = "Determines how molecules are aligned to the core in the R-Group decomposition process.",
        advanced = true)
    @Layout(AdvancedSection.class)
    CoreAlignment m_coreAlignment;

    @Persist(configKey = "match_only_at_rgroups")
    @Widget(title = "Match only at R-Groups",
        description = "If enabled, the decomposition only accepts matches at recognized R-group sites in the scaffold.",
        advanced = true)
    @Layout(AdvancedSection.class)
    boolean m_matchOnlyAtRGroups;

    @Persist(configKey = "remove_hydrogen_only_rgroups")
    @Widget(title = "Remove hydrogen-only R-groups",
        description = "If enabled, all R-groups that contain only hydrogen atoms are filtered out.",
        advanced = true)
    @Layout(AdvancedSection.class)
    boolean m_removeHydrogenOnlyRgroups;

    @Persist(configKey = "remove_hydrogens_post_match")
    @Widget(title = "Remove hydrogens post-match",
        description = "If enabled, any leftover hydrogens are removed after the R-group matching is complete.",
        advanced = true)
    @Layout(AdvancedSection.class)
    boolean m_removeHydrogensPostMatch;
}
