package org.rdkit.knime.nodes.functionalgroupfilter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.filehandling.core.defaultnodesettings.filechooser.reader.SettingsModelReaderFileChooser;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.array.ArrayWidget;
import org.knime.node.parameters.array.ArrayWidget.ElementLayout;
import org.knime.node.parameters.layout.After;
import org.knime.node.parameters.layout.HorizontalLayout;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.experimental.persistence.array.ArrayPersistor;
import org.knime.node.parameters.experimental.persistence.array.ElementFieldPersistor;
import org.knime.node.parameters.experimental.persistence.array.PersistArray;
import org.knime.node.parameters.experimental.persistence.array.PersistArrayElement;
import org.knime.node.parameters.legacy.persistence.PersistWithin;
import org.knime.node.parameters.legacy.widget.file.LegacyReaderFileSelectionPersistor;
import org.knime.node.parameters.migration.ConfigMigration;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.migration.Migration;
import org.knime.node.parameters.migration.NodeParametersMigration;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.EffectPredicate;
import org.knime.node.parameters.updates.EffectPredicateProvider;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.StateComputationAbortException;
import org.knime.node.parameters.updates.StateProvider;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.updates.Effect.EffectType;
import org.knime.node.parameters.updates.util.BooleanReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.Label;
import org.knime.node.parameters.widget.choices.ValueSwitchWidget;
import org.knime.node.parameters.widget.file.FileReaderWidget;
import org.knime.node.parameters.widget.file.FileSelection;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsNonNegativeValidation;
import org.rdkit.knime.nodes.functionalgroupfilter.FunctionalGroupFilterV2NodeParameters.GroupConfigFileChooserProvider;
import org.rdkit.knime.nodes.functionalgroupfilter.FunctionalGroupFilterV2NodeParameters.WithError;
import org.rdkit.knime.nodes.functionalgroupfilter.RDKitFunctionalGroupFilterNodeParameters.FunctionalGroupConditionEntry.GroupConditionActivity;
import org.rdkit.knime.nodes.functionalgroupfilter.SettingsModelFunctionalGroupConditions.Qualifier;
import org.rdkit.knime.util.FileSystemsUtils;
import org.rdkit.knime.util.RDKitLegacyPersistors.DefaultFileSwitchMigration;
import org.rdkit.knime.util.RDKitLegacyPersistors.LegacyMoleculeColumnPersistor;
import org.rdkit.knime.util.RDKitMoleculeColumnAutoGuessProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;
import org.rdkit.knime.util.SettingsUtils;

/**
 * {@link NodeParameters} for the {@link RDKitFunctionalGroupFilterNodeModel}.
 * 
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 */
@SuppressWarnings("restriction")
@LoadDefaultsForAbsentFields
final class RDKitFunctionalGroupFilterNodeParameters implements NodeParameters {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(RDKitFunctionalGroupFilterNodeParameters.class);

    @Section(title = "Functional Group Conditions")
    interface ConditionsSection {
    }

    @Widget(title = "RDKit mol column", description = "It specifies which column contains the set of RDKit molecules.")
    @Persistor(InputColumnPersistor.class)
    @ChoicesProvider(RDKitMoleculeColumnChoicesProvider.class)
    @ValueProvider(MolColumnAutoGuessProvider.class)
    @ValueReference(MolColumnRef.class)
    String m_inputColumn;

    static final class MolColumnRef implements ParameterReference<String> {
    }

    @Widget(title = "Functional group definition file (optional)", description = """
            Lets the user define a customized functional group definition file. The default built-in \
            <a href="https://github.com/rdkit/knime-rdkit/blob/master/org.rdkit.knime.nodes/src/org/rdkit/knime/nodes/
            functionalgroupfilter/Functional_Group_Hierarchy.txt">definitions</a> will be used when no file is 
            specified.
            """)
    @ValueSwitchWidget
    @ValueReference(DefinitionFileSwitchRef.class)
    @Migration(DefinitionFileSwitchMigration.class)
    FileSwitch m_definitionFileSwitch = FileSwitch.DEFAULT_CONFIGURATION;

    static final class DefinitionFileSwitchRef implements ParameterReference<FileSwitch> {
    }
    
    static final class DefinitionFileSwitchMigration extends DefaultFileSwitchMigration<FileSwitch> {

		protected DefinitionFileSwitchMigration() {
			super("definitionFileSwitch", "group_definition_file", 
					() -> FileSwitch.DEFAULT_CONFIGURATION, () -> FileSwitch.FILE_SELECTION);
		}
    	
    }

    @Persistor(DefinitionFilePersistor.class)
    @Widget(title = "Selected definition file", 
    	description = "Specify a custom functional group definition file to use.")
    @FileReaderWidget
    @Effect(predicate = IsDefinitionFileSelectionEnabled.class, type = EffectType.SHOW)
    @ValueReference(DefinitionFileRef.class)
    FileSelection m_definitionFile = new FileSelection();

    static final class DefinitionFileRef implements ParameterReference<FileSelection> {
    }
    
    @Widget(title = "Enable recording in the following new column", description = """
            If checked, an additional column (the name can be specified) containing the first \
            non-matching pattern is added to the table of failing molecules.
            """)
    @Persist(configKey = "record_pattern")
    @ValueReference(EnableRecording.class)
    boolean m_recordPattern;

    static final class EnableRecording implements BooleanReference {
    }

    @Widget(title = "Column name for first non-matching pattern", description = """
    		Name of the new column in the failed molecules table that will contain the first non-matching pattern.
    		""")
    @Persist(configKey = "failed_pattern_column_name")
    @Effect(predicate = EnableRecording.class, type = EffectType.SHOW)
    String m_failedPatternColumnName;
    
    @Layout(ConditionsSection.class)
    @Widget(title = "Functional group filter conditions", description = """
            Defines the conditions for filtering molecules. Select the ones that need to be applied on the \
            molecules. If no filter pattern is selected, molecules will not be filtered at all. \
            Each condition has a qualifier (e.g. "AtLeast", "Exactly") and a count for comparison.
            """)
    @PersistWithin("conditions")
    @PersistArray(ConditionsArrayPersistor.class)
    @ArrayWidget(hasFixedSize = true, elementLayout = ElementLayout.VERTICAL_CARD)
    @ValueProvider(ConditionsFromFileProvider.class)
    @ValueReference(ConditionsRef.class)
    FunctionalGroupConditionEntry[] m_conditions = new FunctionalGroupConditionEntry[0];

    static final class ConditionsRef implements ParameterReference<FunctionalGroupConditionEntry[]> {
    }
    
    static final class ConditionsMigration implements NodeParametersMigration<FunctionalGroupConditionEntry[]> {

        private static final String DEPRECATED_CONFIG_KEY = "properties";

		private static FunctionalGroupConditionEntry[] load(final NodeSettingsRO settings)
				throws InvalidSettingsException {
			final var legacyConditions = settings.getNodeSettings(DEPRECATED_CONFIG_KEY);
			final var count = legacyConditions.getInt("count", 0);
			FunctionalGroupConditionEntry[] conditions = new FunctionalGroupConditionEntry[count];
			for (int i = 0; i < count; i++) {
				FunctionalGroupConditionEntry e = new FunctionalGroupConditionEntry();
				e.m_name = legacyConditions.getString("name_" + i, "");
				e.m_active = legacyConditions.getBoolean("select_" + i, false) ? GroupConditionActivity.ACTIVE 
						: GroupConditionActivity.INACTIVE;
				final var qualifierStr = legacyConditions.getString("qualifier_" + i, Qualifier.Exactly.name());
				e.m_qualifier = SettingsUtils.getEnumValueFromString(Qualifier.class, qualifierStr, Qualifier.Exactly);
				e.m_count = legacyConditions.getInt("count_" + i, 0);
				conditions[i] = e;
			}
			return conditions;
		}
    	
		@Override
		public List<ConfigMigration<FunctionalGroupConditionEntry[]>> getConfigMigrations() {
			return List.of(//
	                ConfigMigration.builder(ConditionsMigration::load) //
	                    .withDeprecatedConfigPath(DEPRECATED_CONFIG_KEY)//
	                    .build());
		}
    	
    }

    static final class FunctionalGroupConditionEntry implements NodeParameters {

    	@HorizontalLayout
    	interface GroupSection {
    	}
    	
    	@HorizontalLayout
    	@After(GroupSection.class)
    	interface ConditionSection {
    	}
    	
        @Widget(title = "Group condition", description = """
        		Whether this functional group condition is active and shall be applied during filtering.
        		""")
        @PersistArrayElement(ActiveElementPersistor.class)
        @ValueSwitchWidget
        @Layout(GroupSection.class)
        GroupConditionActivity m_active = GroupConditionActivity.INACTIVE;

		@Widget(title = "Group name", description = """
				The unique name of the functional group (populated from the definition file).
				""")
		@Layout(GroupSection.class)
		@PersistArrayElement(NameElementPersistor.class)
		String m_name = "";
            
        @Widget(title = "Qualifier", description = """
        		The comparison qualifier to apply when counting occurrences of this functional group.
        		""")
        @Layout(ConditionSection.class)
        @PersistArrayElement(QualifierElementPersistor.class)
        Qualifier m_qualifier = Qualifier.Exactly;

        @Widget(title = "Count", description = "The number of occurrences to compare against using the qualifier.")
        @NumberInputWidget(minValidation = IsNonNegativeValidation.class)
        @PersistArrayElement(CountElementPersistor.class)
        @Layout(ConditionSection.class)
        int m_count;

        static final class ActiveElementPersistor
            implements ElementFieldPersistor<GroupConditionActivity, Integer, FunctionalGroupConditionEntry> {

            @Override
            public GroupConditionActivity load(final NodeSettingsRO nodeSettings, final Integer idx)
                throws InvalidSettingsException {
                return nodeSettings.getBoolean("select_" + idx, false) ? GroupConditionActivity.ACTIVE 
                		: GroupConditionActivity.INACTIVE;
            }

            @Override
            public void save(final GroupConditionActivity value, final FunctionalGroupConditionEntry dto) {
                dto.m_active = value;
            }

            @Override
            public String[][] getConfigPaths() {
                return new String[][]{{"select_" + ARRAY_INDEX_PLACEHOLDER}};
            }
        }

        static final class NameElementPersistor
            implements ElementFieldPersistor<String, Integer, FunctionalGroupConditionEntry> {

            @Override
            public String load(final NodeSettingsRO nodeSettings, final Integer idx)
                throws InvalidSettingsException {
                return nodeSettings.getString("name_" + idx, "");
            }

            @Override
            public void save(final String value, final FunctionalGroupConditionEntry dto) {
                dto.m_name = value;
            }

            @Override
            public String[][] getConfigPaths() {
                return new String[][]{{"name_" + ARRAY_INDEX_PLACEHOLDER}};
            }
        }
        
        static final class QualifierElementPersistor
            implements ElementFieldPersistor<Qualifier, Integer, FunctionalGroupConditionEntry> {

            @Override
            public Qualifier load(final NodeSettingsRO nodeSettings, final Integer idx)
                throws InvalidSettingsException {
                final String qualifierStr = nodeSettings.getString("qualifier_" + idx, "=");
                return SettingsUtils.getEnumValueFromString(Qualifier.class, qualifierStr, Qualifier.Exactly);
            }

            @Override
            public void save(final Qualifier value, final FunctionalGroupConditionEntry dto) {
                dto.m_qualifier = value;
            }

            @Override
            public String[][] getConfigPaths() {
                return new String[][]{{"qualifier_" + ARRAY_INDEX_PLACEHOLDER}};
            }
        }

        static final class CountElementPersistor
            implements ElementFieldPersistor<Integer, Integer, FunctionalGroupConditionEntry> {

            @Override
            public Integer load(final NodeSettingsRO nodeSettings, final Integer idx)
                throws InvalidSettingsException {
                return nodeSettings.getInt("count_" + idx, 0);
            }

            @Override
            public void save(final Integer value, final FunctionalGroupConditionEntry dto) {
                dto.m_count = value;
            }

            @Override
            public String[][] getConfigPaths() {
                return new String[][]{{"count_" + ARRAY_INDEX_PLACEHOLDER}};
            }
        }
        
        enum GroupConditionActivity {
        	
        	@Label(value = "Inactive")
        	INACTIVE, //
        	@Label(value = "Active")
        	ACTIVE;
        	
        }

    }
    
    static final class IsDefinitionFileSelectionEnabled implements EffectPredicateProvider {
    	
        @Override
        public EffectPredicate init(final PredicateInitializer i) {
            return i.getEnum(DefinitionFileSwitchRef.class).isOneOf(FileSwitch.FILE_SELECTION);
        }
        
    }

    static final class MolColumnAutoGuessProvider extends RDKitMoleculeColumnAutoGuessProvider {
    	
        protected MolColumnAutoGuessProvider() {
            super(MolColumnRef.class, 0);
        }
        
    }

	static final class ConditionsFromFileProvider implements StateProvider<FunctionalGroupConditionEntry[]> {

		private Supplier<FunctionalGroupConditionEntry[]> m_existingConditionsSupplier;

		private Supplier<WithError<SettingsModelReaderFileChooser, Exception>> 
			m_groupDefinitionConfigFileChooserSupplier;

		@Override
		public void init(final StateProviderInitializer initializer) {
			initializer.computeBeforeOpenDialog();
			m_existingConditionsSupplier = initializer.getValueSupplier(ConditionsRef.class);
			m_groupDefinitionConfigFileChooserSupplier = initializer
					.computeFromProvidedState(GroupConfigFileChooserProvider.class);
		}

		@Override
		public FunctionalGroupConditionEntry[] computeState(final NodeParametersInput context)
				throws StateComputationAbortException {
			FunctionalGroupDefinitions definitions = null;
			final var groupDefinitionConfigFileChooserResult = m_groupDefinitionConfigFileChooserSupplier.get();
			if (groupDefinitionConfigFileChooserResult.hasError()) {
				throw new StateComputationAbortException();
			}
			final var groupDefinitionConfigFileChooser = groupDefinitionConfigFileChooserResult.value();
			if (groupDefinitionConfigFileChooser == null || groupDefinitionConfigFileChooser.getPath().isBlank()) {
				try (InputStream is = FunctionalGroupDefinitions.class.getClassLoader()
						.getResourceAsStream(FunctionalGroupFilterV2NodeModel.DEFAULT_DEFINITION_FILE)) {
					if (is != null) {
						definitions = new FunctionalGroupDefinitions(is);
					}
				} catch (IOException e) {
					LOGGER.warn("Could not load default functional group definitions: " + e.getMessage());
				}
			}
			if (definitions == null) {
				try {
					definitions = FileSystemsUtils.readFile(groupDefinitionConfigFileChooser,
							FunctionalGroupDefinitions::new, LOGGER);
				} catch (IOException | InvalidSettingsException e) {
					LOGGER.warn("Could not load functional group definitions from %s: %s"
							.formatted(groupDefinitionConfigFileChooser.getPath(), e.getMessage()));
				}
			}

			if (definitions == null) {
				throw new StateComputationAbortException();
			}

			final FunctionalGroupConditionEntry[] existing = m_existingConditionsSupplier.get();
			final Map<String, FunctionalGroupConditionEntry> existingMap = new HashMap<>();
			if (existing != null) {
				for (final FunctionalGroupConditionEntry entry : existing) {
					if (entry.m_name != null && !entry.m_name.isBlank()) {
						existingMap.put(entry.m_name, entry);
					}
				}
			}

			final String[] groupNames = definitions.getFunctionalGroupNames();
			final List<FunctionalGroupConditionEntry> result = new ArrayList<>(groupNames.length);
			for (final String name : groupNames) {
				final var group = definitions.get(name);
				FunctionalGroupConditionEntry newEntry = new FunctionalGroupConditionEntry();
				if (existingMap.containsKey(name)) {
					newEntry = existingMap.get(name);
				} else {
					newEntry.m_active = GroupConditionActivity.INACTIVE;
					newEntry.m_name = group.getName();
					newEntry.m_qualifier = Qualifier.Exactly;
					newEntry.m_count = 0;
				}
				result.add(newEntry);
			}
			return result.toArray(FunctionalGroupConditionEntry[]::new);
		}

	}

    static final class InputColumnPersistor extends LegacyMoleculeColumnPersistor {

		public InputColumnPersistor() {
			super("input_column", "colName");
		}
    	
    }
	
    static final class DefinitionFilePersistor extends LegacyReaderFileSelectionPersistor {

        DefinitionFilePersistor() {
            super("group_definition_file");
        }

    }

    static final class ConditionsArrayPersistor implements ArrayPersistor<Integer, FunctionalGroupConditionEntry> {

        @Override
        public int getArrayLength(final NodeSettingsRO nodeSettings) throws InvalidSettingsException {
            return nodeSettings.getInt("count", 0);
        }

        @Override
        public Integer createElementLoadContext(final int index) {
            return index;
        }

        @Override
        public FunctionalGroupConditionEntry createElementSaveDTO(final int index) {
            return new FunctionalGroupConditionEntry();
        }

        @Override
        public void save(final List<FunctionalGroupConditionEntry> entries, final NodeSettingsWO nodeSettings) {
            final int count = entries == null ? 0 : entries.size();
            nodeSettings.addInt("count", count);
            if (entries != null) {
                for (int i = 0; i < entries.size(); i++) {
                    final FunctionalGroupConditionEntry e = entries.get(i);
                    nodeSettings.addString("name_" + i, e.m_name != null ? e.m_name : null);
                    nodeSettings.addBoolean("select_" + i, e.m_active == GroupConditionActivity.ACTIVE);
                    nodeSettings.addString("qualifier_" + i,
                        e.m_qualifier != null ? e.m_qualifier.name() : Qualifier.Exactly.name());
                    nodeSettings.addInt("count_" + i, e.m_count);
                }
            }
        }
        
    }
    
	enum FileSwitch {

		@Label(value = "Default configuration", description = "Loads the default configuration file.")
		DEFAULT_CONFIGURATION, //
		@Label(value = "File selection", description = "Specify a configuration file.")
		FILE_SELECTION;

	}

}
