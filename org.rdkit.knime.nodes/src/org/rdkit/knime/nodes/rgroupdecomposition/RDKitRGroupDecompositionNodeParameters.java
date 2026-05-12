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

package org.rdkit.knime.nodes.rgroupdecomposition;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import org.knime.chem.types.SdfValue;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataType;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.experimental.persistence.booleanhelpers.DoNotPersistBoolean;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
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
import org.knime.node.parameters.updates.util.BooleanReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.StringChoice;
import org.knime.node.parameters.widget.choices.StringChoicesProvider;
import org.knime.node.parameters.widget.choices.ValueSwitchWidget;
import org.knime.node.parameters.widget.choices.filter.TwinlistWidget;
import org.knime.node.parameters.widget.text.TextAreaWidget;
import org.rdkit.knime.types.RDKitMolValue;
import org.rdkit.knime.types.preferences.RDKitTypesPreferencePage;
import org.rdkit.knime.util.RDKitMoleculeColumnAutoGuessProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;

/**
 * Node parameters for RDKit R-Group Decomposition.
 *
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class RDKitRGroupDecompositionNodeParameters implements NodeParameters {

    @Widget(title = "RDKit mol column", description = "The column containing molecules.")
    @Persist(configKey = "input_column")
    @ChoicesProvider(RDKitMoleculeColumnChoicesProvider.class)
    @ValueProvider(MolColumnAutoGuessProvider.class)
    @ValueReference(MolColumnRef.class)
    String m_inputColumn;

    static final class MolColumnRef implements ParameterReference<String> {
    }

    @Widget(title = "Core input column (2nd table)", description = """
    		If a second table is connected, the column containing cores as RDKit Molecules, SMARTS, SMILES or SDF. 
    		When no second table is connected, use the Core SMARTS text field below instead.
            """)
    @Persist(configKey = "cores_input_column")
    @ChoicesProvider(CoresColumnChoicesProvider.class)
    @ValueProvider(CoresColumnAutoGuessProvider.class)
    @ValueReference(CoresColumnRef.class)
    @Effect(predicate = HasSecondInputTable.class, type = EffectType.SHOW)
    String m_coresInputColumn;
    
    static final class CoresColumnRef implements ParameterReference<String> {
	}
    
    @SuppressWarnings("restriction")
	@Persistor(DoNotPersistBoolean.class)
    @ValueProvider(IsOnlySDFInputProvider.class)
    @ValueReference(IsStrictParsingEnabled.class)
    boolean m_strictParsingEnabled;
    
    static final class IsStrictParsingEnabled implements BooleanReference {
	}

    @Widget(title = "Strict parsing of mol blocks", description = """
    		When SDF molecules are used as cores, this option sets the tolerance level for parsing mol blocks. 
    		Only applicable when SDF-compatible columns are used as core input. The default value is defined in the 
    		RDKit preferences.
            """)
    @Persist(configKey = "strict_parsing")
    @Effect(predicate = IsStrictParsingEnabled.class, type = EffectType.ENABLE)
    @Effect(predicate = HasSecondInputTable.class, type = EffectType.SHOW)
    boolean m_strictParsing = RDKitTypesPreferencePage.isStrictParsingForNodeSettingsDefault();

    @Widget(title = "Core SMARTS", description = """
    		If no second table is connected, one or multiple new-line separated SMARTS can be defined as the cores to 
    		be used.
            """)
    @Persist(configKey = "smarts_value")
    @TextAreaWidget(rows = 5)
    @Effect(predicate = HasSecondInputTable.class, type = EffectType.HIDE)
    String m_smarts = "";

    @Widget(title = "Add matching SMARTS core", description = """
    		Flag to be set to add a column that contains the matching core from SMARTS input.
    		""")
    @Persist(configKey = "add_matching_smarts_core")
    @ValueReference(AddMatchingSmartsCoreRef.class)
    boolean m_addMatchingSmartsCore = true;

    static final class AddMatchingSmartsCoreRef implements BooleanReference {
    }

    @Widget(title = "Core column name", description = """
    		The column name for the SMARTS core column, if it is set to be added.
    		""")
    @Persist(configKey = "new_matching_smarts_core_column_name")
    @Effect(predicate = AddMatchingSmartsCoreRef.class, type = EffectType.SHOW)
    String m_newMatchingSmartsCoreColumnName = "Core";

    @Widget(title = "Add matching explicit core", description = """
    		Flag to be set to add a column that contains the matching explicit core based on the matching SMARTS and 
    		input molecule. It shows the real matching core derived from the input molecule.
            """)
    @Persist(configKey = "add_matching_explicit_core")
    @ValueReference(AddMatchingExplicitCoreRef.class)
    boolean m_addMatchingExplicitCore;

    static final class AddMatchingExplicitCoreRef implements BooleanReference {
    }

    @Widget(title = "Explicit core column name", description = """
    		The column name for the matching explicit core column, if it is set to be added.
    		""")
    @Persist(configKey = "new_matching_explicit_core_column_name")
    @Effect(predicate = AddMatchingExplicitCoreRef.class, type = EffectType.SHOW)
    String m_newMatchingExplicitCoreColumnName = "Explicit Core";

    @Widget(title = "Use atom maps", description = """
    		Flag to be set to control atom properties of the matching explicit core.
    		""")
    @Persist(configKey = "use_atom_maps_for_explicit_core")
    @Effect(predicate = AddMatchingExplicitCoreRef.class, type = EffectType.SHOW)
    boolean m_useAtomMapsForExplicitCore = true;

    @Widget(title = "Use R-labels", description = """
    		Flag to be set to control atom properties of the matching explicit core.
    		""")
    @Persist(configKey = "use_r_labels_for_explicit_core")
    @Effect(predicate = AddMatchingExplicitCoreRef.class, type = EffectType.SHOW)
    boolean m_useRLabelsForExplicitCore = true;

    @Widget(title = "Remove empty Rx columns", description = """
    		Flag to be set to detect Rx columns that are completely empty and remove them.
    		""")
    @Persist(configKey = "remove_empty_columns")
    boolean m_removeEmptyColumns = true;

    @Widget(title = "Fail if no cores are matching at all", description = """
    		Flag to be set to let the node fail if no matches for the provided cores have been found at all.
    		""")
    @Persist(configKey = "fail_for_no_match")
    boolean m_failForNoMatch = true;

    @Widget(title = "Labels to recognize R-Groups in scaffolds", description = """
    		Enable or disable one or multiple labels to be used to recognize R-Groups in scaffolds. At least one item 
    		needs to be picked.
    		""", advanced = true)
    @Persist(configKey = "labels")
    @ChoicesProvider(LabelsChoicesProvider.class)
    @TwinlistWidget(includedLabel = "Enabled", excludedLabel = "Disabled")
    String[] m_labels = new String[]{Labels.AutoDetect.name()};

    @Widget(title = "Matching strategy", description = "Select how matches shall be found.", advanced = true)
    @Persist(configKey = "matching_strategy")
    Matching m_matchingStrategy = Matching.GreedyChunks;

    @Widget(title = "Labeling for R-Groups output", description = """
    		Enable or disable options how to label R-Groups in the output.
    		""", advanced = true)
    @Persist(configKey = "labeling")
    @ChoicesProvider(LabelingChoicesProvider.class)
    @TwinlistWidget(includedLabel = "Enabled", excludedLabel = "Disabled")
    String[] m_labeling = new String[]{Labeling.AtomMap.name(), Labeling.MDLRGroup.name()};

    @Widget(title = "Core alignment", description = "Select how cores shall be aligned.", advanced = true)
    @Persist(configKey = "core_alignment")
    @ValueSwitchWidget
    CoreAlignment m_coreAlignment = CoreAlignment.MCS;

    @Widget(title = "Match only at R-Groups", description = "Flag to be set to find matches only at R-Groups.", 
    	advanced = true)
    @Persist(configKey = "match_only_at_rgroups")
    boolean m_matchOnlyAtRGroups = RDKitRGroupDecompositionNodeDialog.DEFAULT_RGROUP_DECOMPOSITION_PARAMETERS
        .getOnlyMatchAtRGroups();

    @Widget(title = "Remove hydrogen only R-Groups", description = """
    		Flag to be set to remove R-Groups that consist only of hydrogens from matching.
    		""", advanced = true)
    @Persist(configKey = "remove_hydrogen_only_rgroups")
    boolean m_removeHydrogenOnlyRGroups = RDKitRGroupDecompositionNodeDialog.DEFAULT_RGROUP_DECOMPOSITION_PARAMETERS
        .getRemoveAllHydrogenRGroups();

    @Widget(title = "Remove hydrogens post match", description = """
    		Flag to be set to remove all hydrogens in the resulting R-Groups output.
    		""", advanced = true)
    @Persist(configKey = "remove_hydrogens_post_match")
    boolean m_removeHydrogensPostMatch = RDKitRGroupDecompositionNodeDialog.DEFAULT_RGROUP_DECOMPOSITION_PARAMETERS
        .getRemoveHydrogensPostMatch();
    
    static final class HasSecondInputTable implements EffectPredicateProvider {
    	
        @Override
        public EffectPredicate init(final PredicateInitializer i) {
            return i.getConstant(input -> input.getInTableSpec(1).isPresent());
        }
        
    }
    
    static final class HasSecondInputTableAndIsSDFOnlyCoreInputColumn implements EffectPredicateProvider {
    	
        @Override
        public EffectPredicate init(final PredicateInitializer i) {
            return i.getConstant(input -> input.getInTableSpec(1).isPresent())
            		.and(i.getPredicate(IsStrictParsingEnabled.class));
        }
        
    }
    
    static final class MolColumnAutoGuessProvider extends RDKitMoleculeColumnAutoGuessProvider {
    	
        protected MolColumnAutoGuessProvider() {
            super(MolColumnRef.class, 0);
        }
        
    }
    
    static final class CoresColumnAutoGuessProvider extends RDKitMoleculeColumnAutoGuessProvider {
		
		protected CoresColumnAutoGuessProvider() {
			super(CoresColumnRef.class, 1, 0, SdfValue.class);
		}
		
	}
    
    static final class CoresColumnChoicesProvider extends RDKitMoleculeColumnChoicesProvider {
    	
        public CoresColumnChoicesProvider() {
            super(1, SdfValue.class);
        }
        
    }
    
    static final class IsOnlySDFInputProvider implements StateProvider<Boolean> {

    	Supplier<String> m_coresInputColumnSupplier;
    	
		@Override
		public void init(StateProviderInitializer initializer) {
			initializer.computeBeforeOpenDialog();
			m_coresInputColumnSupplier = initializer.computeFromValueSupplier(CoresColumnRef.class);
		}

		@Override
		public Boolean computeState(NodeParametersInput parametersInput) throws StateComputationAbortException {
			final var coresInputColumn = m_coresInputColumnSupplier.get();
			if (coresInputColumn == null || coresInputColumn.isEmpty()) {
				return false;
			}
			final var inputSpecOpt = parametersInput.getInTableSpec(1);
			if (inputSpecOpt.isEmpty()) {
				return false;
			}
			final var inputSpec = inputSpecOpt.get();
			
			final DataColumnSpec coreInputColumnSpec = inputSpec.getColumnSpec(coresInputColumn);
			final DataType dataType = coreInputColumnSpec != null ? coreInputColumnSpec.getType() : null;
			
			return (coreInputColumnSpec != null && !dataType.isCompatible(RDKitMolValue.class) 
					&& (dataType.isCompatible(SdfValue.class) || dataType.isAdaptable(SdfValue.class)));
		}
    	
    }
    
    static final class LabelsChoicesProvider implements StringChoicesProvider {
    	
        @Override
        public List<StringChoice> computeState(final NodeParametersInput context) {
            return Arrays.stream(Labels.values())
                .map(l -> new StringChoice(l.name(), l.toString()))
                .toList();
        }
        
    }
    
    static final class LabelingChoicesProvider implements StringChoicesProvider {
    	
        @Override
        public List<StringChoice> computeState(final NodeParametersInput context) {
            return Arrays.stream(Labeling.values())
                .map(l -> new StringChoice(l.name(), l.toString()))
                .toList();
        }
        
    }
    
}
