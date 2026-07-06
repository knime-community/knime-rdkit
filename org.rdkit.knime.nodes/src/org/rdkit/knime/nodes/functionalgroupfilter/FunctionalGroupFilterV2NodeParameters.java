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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.knime.base.node.io.filehandling.webui.FileChooserPathAccessor;
import org.knime.base.node.io.filehandling.webui.FileSystemPortConnectionUtil;
import org.knime.chem.types.SmartsValue;
import org.knime.chem.types.SmilesValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.filehandling.core.connections.FSCategory;
import org.knime.filehandling.core.connections.FSLocation;
import org.knime.filehandling.core.util.WorkflowContextUtil;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.array.ArrayWidget;
import org.knime.node.parameters.array.ArrayWidget.ElementLayout;
import org.knime.node.parameters.experimental.persistence.array.ArrayPersistor;
import org.knime.node.parameters.experimental.persistence.array.ElementFieldPersistor;
import org.knime.node.parameters.experimental.persistence.array.PersistArray;
import org.knime.node.parameters.experimental.persistence.array.PersistArrayElement;
import org.knime.node.parameters.layout.After;
import org.knime.node.parameters.layout.HorizontalLayout;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.legacy.widget.file.LegacyReaderFileSelectionPersistor;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.migration.Migration;
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
import org.knime.node.parameters.updates.internal.StateProviderInitializerInternal;
import org.knime.node.parameters.updates.Effect.EffectType;
import org.knime.node.parameters.updates.util.BooleanReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.Label;
import org.knime.node.parameters.widget.choices.ValueSwitchWidget;
import org.knime.node.parameters.widget.choices.util.CompatibleColumnsProvider;
import org.knime.node.parameters.widget.file.FileReaderWidget;
import org.knime.node.parameters.widget.file.FileSelection;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsNonNegativeValidation;
import org.rdkit.knime.nodes.functionalgroupfilter.FunctionalGroupFilterV2NodeParameters.FunctionalGroupConditionEntry.GroupConditionActivity;
import org.rdkit.knime.nodes.functionalgroupfilter.SettingsModelFunctionalGroupConditions.Qualifier;
import org.rdkit.knime.types.RDKitMolValue;
import org.rdkit.knime.util.RDKitAdapterCellSupport;
import org.rdkit.knime.util.RDKitMoleculeColumnAutoGuessProvider;
import org.rdkit.knime.util.SettingsUtils;
import org.rdkit.knime.util.RDKitLegacyPersistors.DefaultFileSwitchMigration;
import org.rdkit.knime.util.RDKitLegacyPersistors.LegacyMoleculeColumnPersistor;

/**
 * Node parameters for RDKit Functional Group Filter.
 *
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
@SuppressWarnings("restriction")
final class FunctionalGroupFilterV2NodeParameters implements NodeParameters {
    
	private static final String CONFIG_KEY_CONDITIONS = "conditions";
	
    private static final String DEPRECATED_CONFIG_KEY_CONDITIONS = "properties";
	
    private static final NodeLogger LOGGER = NodeLogger.getLogger(FunctionalGroupFilterV2NodeParameters.class);

    @Section(title = "Functional Group Conditions")
    interface ConditionsSection {
    }

    @Widget(title = "RDKit mol column", description = "It specifies which column contains the set of RDKit molecules.")
    @Persistor(InputColumnPersistor.class)
    @ChoicesProvider(MolColumnChoicesProvider.class)
    @ValueProvider(MolColumnAutoGuessProvider.class)
    @ValueReference(MolColumnRef.class)
    String m_inputColumn;

    static final class MolColumnRef implements ParameterReference<String> {
    }

    @Widget(title = "Functional group definition configuration", description = """
            Lets the user define a customized functional group definition file. The default built-in \
            <a href="https://github.com/rdkit/knime-rdkit/blob/master/org.rdkit.knime.nodes/src/org/rdkit/knime/nodes/functionalgroupfilter/Functional_Group_Hierarchy.txt">definitions</a>
             will be used when no file is specified.
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
					FileSwitch.DEFAULT_CONFIGURATION, FileSwitch.FILE_SELECTION);
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
            Each condition has a qualifier (e.g. "Less than or equal", "Equals") and a count for comparison.
            """)
    @PersistArray(ConditionsArrayPersistor.class)
    @ArrayWidget(hasFixedSize = true, elementLayout = ElementLayout.VERTICAL_CARD)
    @ValueProvider(ConditionsFromFileProvider.class)
    @ValueReference(ConditionsRef.class)
    FunctionalGroupConditionEntry[] m_conditions = new FunctionalGroupConditionEntry[0];

    static final class ConditionsRef implements ParameterReference<FunctionalGroupConditionEntry[]> {
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

        @Layout(GroupSection.class)
        @PersistArrayElement(NameElementPersistor.class)
        GroupNameParameters m_groupNameParameters = new GroupNameParameters();
        
		static final class GroupNameParameters implements NodeParameters {

			@Widget(title = "Group name", description = """
					The unique name of the functional group (populated from the definition file).
					""")
			@Effect(predicate = IsAlwaysTrue.class, type = EffectType.DISABLE)
			String m_displayName = "";

			String m_name = "";
			
			GroupNameParameters() {
			}
			
			GroupNameParameters(final String name, final String displayName) {
				m_name = name;
				m_displayName = displayName;
			}

			static final class IsAlwaysTrue implements EffectPredicateProvider {
				
				@Override
				public EffectPredicate init(final PredicateInitializer i) {
					return i.getConstant(pi -> true);
				}
				
			}
			
		}
		
            
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
            	final var conditionsSettings = nodeSettings.containsKey(DEPRECATED_CONFIG_KEY_CONDITIONS) ? 
            			nodeSettings.getNodeSettings(DEPRECATED_CONFIG_KEY_CONDITIONS) : 
            			nodeSettings.getNodeSettings(CONFIG_KEY_CONDITIONS);
                return conditionsSettings.getBoolean("select_" + idx, false) ? GroupConditionActivity.ACTIVE 
                		: GroupConditionActivity.INACTIVE;
            }

            @Override
            public void save(final GroupConditionActivity value, final FunctionalGroupConditionEntry dto) {
                dto.m_active = value;
            }

            @Override
            public String[][] getConfigPaths() {
                return new String[][]{{CONFIG_KEY_CONDITIONS, "select_" + ARRAY_INDEX_PLACEHOLDER}};
            }
        }

        static final class NameElementPersistor
            implements ElementFieldPersistor<GroupNameParameters, Integer, FunctionalGroupConditionEntry> {

            @Override
            public GroupNameParameters load(final NodeSettingsRO nodeSettings, final Integer idx)
                throws InvalidSettingsException {
            	final var conditionsSettings = loadFunctionalGroupConditionSettings(nodeSettings);
                return new GroupNameParameters(conditionsSettings.getString("name_" + idx, ""), null);
            }

            @Override
            public void save(final GroupNameParameters value, final FunctionalGroupConditionEntry dto) {
                dto.m_groupNameParameters = value;
            }

            @Override
            public String[][] getConfigPaths() {
                return new String[][]{{CONFIG_KEY_CONDITIONS, "name_" + ARRAY_INDEX_PLACEHOLDER}};
            }
        }
        
        static final class QualifierElementPersistor
            implements ElementFieldPersistor<Qualifier, Integer, FunctionalGroupConditionEntry> {

            @Override
            public Qualifier load(final NodeSettingsRO nodeSettings, final Integer idx)
                throws InvalidSettingsException {
            	final var conditionsSettings = loadFunctionalGroupConditionSettings(nodeSettings);
                final String qualifierStr = conditionsSettings.getString("qualifier_" + idx, "=");
                return SettingsUtils.getEnumValueFromString(Qualifier.class, qualifierStr, Qualifier.Exactly);
            }

            @Override
            public void save(final Qualifier value, final FunctionalGroupConditionEntry dto) {
                dto.m_qualifier = value;
            }

            @Override
            public String[][] getConfigPaths() {
                return new String[][]{{CONFIG_KEY_CONDITIONS, "qualifier_" + ARRAY_INDEX_PLACEHOLDER}};
            }
        }

        static final class CountElementPersistor
            implements ElementFieldPersistor<Integer, Integer, FunctionalGroupConditionEntry> {

            @Override
            public Integer load(final NodeSettingsRO nodeSettings, final Integer idx)
                throws InvalidSettingsException {
            	final var conditionsSettings = loadFunctionalGroupConditionSettings(nodeSettings);
                return conditionsSettings.getInt("count_" + idx, 0);
            }

            @Override
            public void save(final Integer value, final FunctionalGroupConditionEntry dto) {
                dto.m_count = value;
            }

            @Override
            public String[][] getConfigPaths() {
                return new String[][]{{CONFIG_KEY_CONDITIONS, "count_" + ARRAY_INDEX_PLACEHOLDER}};
            }
        }
        
        enum GroupConditionActivity {
        	
        	@Label(value = "Inactive")
        	INACTIVE, //
        	@Label(value = "Active")
        	ACTIVE;
        	
        }

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof FunctionalGroupConditionEntry)) {
				return false;
			}
			final FunctionalGroupConditionEntry other = (FunctionalGroupConditionEntry) obj;
			return m_active == other.m_active 
					&& m_groupNameParameters.m_name.equals(other.m_groupNameParameters.m_name)
					&& m_groupNameParameters.m_displayName.equals(other.m_groupNameParameters.m_displayName)
					&& m_qualifier == other.m_qualifier
					&& m_count == other.m_count;
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
    
    static final class MolColumnChoicesProvider extends CompatibleColumnsProvider {

		protected MolColumnChoicesProvider() {
			super(RDKitAdapterCellSupport.expandByAdaptableTypes(
					List.of(RDKitMolValue.class, SmartsValue.class, SmilesValue.class)));
		}

		@Override
		public int getInputTableIndex(NodeParametersInput parametersInput) {
			return FileSystemPortConnectionUtil.hasFileSystemPort(parametersInput) ? 1 : 0;
		}
    	
    }
    
	static final class ConditionsFromFileProvider implements StateProvider<FunctionalGroupConditionEntry[]> {

		private Supplier<FunctionalGroupConditionEntry[]> m_existingConditionsSupplier;
		private Supplier<FileSwitch> m_definitionFileSwitchSupplier;
		private Supplier<FileSelection> m_definitionFileSupplier;

		@Override
		public void init(final StateProviderInitializer initializer) {
			((StateProviderInitializerInternal)initializer).computeOnParametersLoaded();
			m_existingConditionsSupplier = initializer.getValueSupplier(ConditionsRef.class);
			m_definitionFileSwitchSupplier = initializer.computeFromValueSupplier(DefinitionFileSwitchRef.class);
			m_definitionFileSupplier = initializer.computeFromValueSupplier(DefinitionFileRef.class);
		}

		@Override
		public FunctionalGroupConditionEntry[] computeState(final NodeParametersInput context)
				throws StateComputationAbortException {
			final var definitions = m_definitionFileSwitchSupplier.get() == FileSwitch.DEFAULT_CONFIGURATION ? 
					getFunctionalGroupDefinitionsFromDefaultFile() : 
					getFunctionalGroupDefinitionsFromCustomFile(context, m_definitionFileSupplier.get());
			if (definitions == null) {
				throw new StateComputationAbortException();
			}

			final FunctionalGroupConditionEntry[] existingEntries = m_existingConditionsSupplier.get();
			final Map<String, FunctionalGroupConditionEntry> existingMap = new HashMap<>();
			if (existingEntries != null) {
				for (final FunctionalGroupConditionEntry entry : existingEntries) {
					if (entry.m_groupNameParameters.m_name != null && !entry.m_groupNameParameters.m_name.isBlank()) {
						existingMap.put(entry.m_groupNameParameters.m_name, entry);
					}
				}
			}

			final String[] groupNames = definitions.getFunctionalGroupNames();
			final List<FunctionalGroupConditionEntry> newEntryList = new ArrayList<>(groupNames.length);
			for (final String name : groupNames) {
				final var group = definitions.get(name);
				final FunctionalGroupConditionEntry newEntry;
				if (existingMap.containsKey(name)) {
					newEntry = existingMap.get(name);
					newEntry.m_groupNameParameters.m_displayName = group.getLabel();
				} else {
					newEntry = new FunctionalGroupConditionEntry();
					newEntry.m_groupNameParameters.m_name = group.getName();
					newEntry.m_groupNameParameters.m_displayName = group.getLabel();
				}
				newEntryList.add(newEntry);
			}
			
			return loadFunctionalGroupDefinitionEntries(newEntryList, existingEntries);
		}
		
		private static FunctionalGroupDefinitions getFunctionalGroupDefinitionsFromDefaultFile() {
			try (InputStream is = FunctionalGroupDefinitions.class.getClassLoader()
					.getResourceAsStream(FunctionalGroupFilterV2NodeModel.DEFAULT_DEFINITION_FILE)) {
				if (is != null) {
					return new FunctionalGroupDefinitions(is);
				}
			} catch (IOException e) {
				LOGGER.warn("Could not load default functional group definitions: " + e.getMessage());
			}
			return null;
		}
		
		private static FunctionalGroupDefinitions getFunctionalGroupDefinitionsFromCustomFile(
			final NodeParametersInput context, final FileSelection fileSelection) 
			throws StateComputationAbortException {
			final var fsLocation = fileSelection.getFSLocation();

			if (!WorkflowContextUtil.hasWorkflowContext()
					|| fsLocation.equals(new FSLocation(FSCategory.LOCAL, ""))) {
				throw new StateComputationAbortException();
			}

			final var fsConnection = FileSystemPortConnectionUtil.getFileSystemConnection(context);
			if (fsLocation.getFSCategory() == FSCategory.CONNECTED && fsConnection.isEmpty()) {
				throw new StateComputationAbortException();
			}

			try (final var accessor = new FileChooserPathAccessor(fileSelection, fsConnection)) {
				final var paths = accessor.getFSPaths(s -> {
					switch (s.getType()) {
						case INFO -> LOGGER.info(s.getMessage());
						case WARNING -> LOGGER.warn(s.getMessage());
						case ERROR -> LOGGER.error(s.getMessage());
					}
				});
				if (!paths.isEmpty()) {
					try (InputStream is = Files.newInputStream(paths.get(0))) {
						return new FunctionalGroupDefinitions(is);
					}
				}
			} catch (IOException | InvalidSettingsException e) {
				LOGGER.warn("Could not load functional group definitions from file: " + e.getMessage());
			}
			return null;
		}
		
		private static FunctionalGroupConditionEntry[] loadFunctionalGroupDefinitionEntries(
			final List<FunctionalGroupConditionEntry> newEntryList, 
			final FunctionalGroupConditionEntry[] existingEntries) throws StateComputationAbortException {
			final var newEntryArray = newEntryList.toArray(FunctionalGroupConditionEntry[]::new);
			if (newEntryArray == null) {
				throw new StateComputationAbortException();
			}
			if (existingEntries == null) {
				return newEntryArray;
			}
			if (newEntryArray.length == existingEntries.length) {
				boolean allEqual = true;
				for (int i = 0; i < newEntryArray.length; i++) {
					if (!newEntryArray[i].equals(existingEntries[i])) {
						allEqual = false;
						break;
					}
				}
				if (allEqual) {
					throw new StateComputationAbortException();
				}
			}
			return newEntryArray;
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
        	final var conditionsSettings = loadFunctionalGroupConditionSettings(nodeSettings);
            return conditionsSettings.getInt("count", 0);
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
            final var conditionsSettings = nodeSettings.addNodeSettings(CONFIG_KEY_CONDITIONS);
            conditionsSettings.addInt("count", count);
            if (entries != null) {
                for (int i = 0; i < entries.size(); i++) {
                    final FunctionalGroupConditionEntry e = entries.get(i);
                    conditionsSettings.addString("name_" + i, e.m_groupNameParameters.m_name);
                    conditionsSettings.addBoolean("select_" + i, e.m_active == GroupConditionActivity.ACTIVE);
                    conditionsSettings.addString("qualifier_" + i,
                        e.m_qualifier != null ? e.m_qualifier.name() : Qualifier.Exactly.name());
                    conditionsSettings.addInt("count_" + i, e.m_count);
                }
            }
        }
        
    }
    
	enum FileSwitch {

		@Label(value = "Default", description = "Loads the default configuration file.")
		DEFAULT_CONFIGURATION, //
		@Label(value = "Custom", description = "Specify a configuration file.")
		FILE_SELECTION;

	}
	
	private static NodeSettingsRO loadFunctionalGroupConditionSettings(final NodeSettingsRO nodeSettings)
		throws InvalidSettingsException {
		return nodeSettings.containsKey(DEPRECATED_CONFIG_KEY_CONDITIONS)
				? nodeSettings.getNodeSettings(DEPRECATED_CONFIG_KEY_CONDITIONS)
				: nodeSettings.getNodeSettings(CONFIG_KEY_CONDITIONS);
	}
    
}
