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

package org.rdkit.knime.nodes.adjustqueryproperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import org.RDKit.AdjustQueryWhichFlags;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.NodeParametersPersistor;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.Effect.EffectType;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.updates.util.BooleanReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.StringChoice;
import org.knime.node.parameters.widget.choices.StringChoicesProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnAutoGuessProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;
import org.rdkit.knime.util.RDKitResultColumnNameAutoGuessProvider;

/**
 * Node parameters for RDKit Adjust Query Properties.
 *
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class RDKitAdjustQueryPropertiesNodeParameters implements NodeParameters {

    @Widget(title = "RDKit mol column",
        description = "The input column with molecules to be used as queries.")
    @Persist(configKey = "input_column")
    @ChoicesProvider(RDKitMoleculeColumnChoicesProvider.class)
    @ValueProvider(MolColumnAutoGuessProvider.class)
    @ValueReference(MolColumnRef.class)
    String m_inputColumn;

    static final class MolColumnRef implements ParameterReference<String> {
    }

    @Widget(title = "New column name", description = """
    		The name of the new column which will contain the tuned queries as RDKit Molecules.
    		""")
    @Persist(configKey = "new_column_name")
    @ValueProvider(NewColumnNameAutoGuessProvider.class)
    @ValueReference(NewColumnNameRef.class)
    String m_newColumnName;

    static final class NewColumnNameRef implements ParameterReference<String> {
    }

    @Widget(title = "Remove source column", description = """
    		Set to true to remove the specified source column from the result table.
    		""")
    @Persist(configKey = "remove_source_columns")
    @ValueReference(RemoveSourceColumnRef.class)
    boolean m_removeSourceColumns;

    static final class RemoveSourceColumnRef implements BooleanReference {
    }

    @Widget(title = "Adjust degree", description = """
    		Enable so that modified atoms have an explicit-degree query added based on their degree in the query.
    		""")
    @Persist(configKey = "adjust_degree")
    @ValueReference(AdjustDegreeRef.class)
    boolean m_adjustDegree = RDKitAdjustQueryPropertiesNodeDialog.DEFAULT_ADJUST_QUERY_PARAMETERS.getAdjustDegree();

    static final class AdjustDegreeRef implements BooleanReference {
    }

    @Widget(title = "Flags to adjust degree queries", description = "Control which atoms have a degree query added.")
    @Persistor(AdjustedDegreeFlagsPersistor.class)
    @ChoicesProvider(AdjustQueryWhichFlagsProvider.class)
    @Effect(predicate = AdjustDegreeRef.class, type = EffectType.ENABLE)
    String[] m_adjustDegreeFlags = flagNames(
    		RDKitAdjustQueryPropertiesNodeDialog.DEFAULT_ADJUST_QUERY_PARAMETERS.getAdjustDegreeFlags());
    
    static final class AdjustedDegreeFlagsPersistor extends ReducedFlagsPersistor {
		
		protected AdjustedDegreeFlagsPersistor() {
			super("adjust_degree_flags");
		}

    }
		
    @Widget(title = "Adjust ring count", description = """
    		Enable so that modified atoms have a ring-count query added based on their ring count in the query.
    		""")
    @Persist(configKey = "adjust_ring_count")
    @ValueReference(AdjustRingCountRef.class)
    boolean m_adjustRingCount =
        RDKitAdjustQueryPropertiesNodeDialog.DEFAULT_ADJUST_QUERY_PARAMETERS.getAdjustRingCount();

    static final class AdjustRingCountRef implements BooleanReference {
    }

    @Widget(title = "Flags to adjust ring-count queries", description = "Control which atoms have a ring-count query added.")
    @Persistor(AdjustedRingCountFlagsPersistor.class)
    @ChoicesProvider(AdjustQueryWhichFlagsProvider.class)
    @Effect(predicate = AdjustRingCountRef.class, type = EffectType.ENABLE)
    String[] m_adjustRingCountFlags = flagNames(
			RDKitAdjustQueryPropertiesNodeDialog.DEFAULT_ADJUST_QUERY_PARAMETERS.getAdjustRingCountFlags());

	static final class AdjustedRingCountFlagsPersistor extends ReducedFlagsPersistor {

		protected AdjustedRingCountFlagsPersistor() {
			super("adjust_ring_count_flags");
		}
		
	}
    
    @Widget(title = "Make atoms generic", description = "Convert atoms to any-atom queries.")
    @Persist(configKey = "make_atoms_generic")
    @ValueReference(MakeAtomsGenericRef.class)
    boolean m_makeAtomsGeneric =
        RDKitAdjustQueryPropertiesNodeDialog.DEFAULT_ADJUST_QUERY_PARAMETERS.getMakeAtomsGeneric();

    static final class MakeAtomsGenericRef implements BooleanReference {
    }

    @Widget(title = "Flags to adjust any-atom queries", 
    	description = "Control which atoms are converted to any-atom queries.")
    @Persistor(MakeAtomsGenericFlagsPersistor.class)
    @ChoicesProvider(AdjustQueryWhichFlagsProvider.class)
    @Effect(predicate = MakeAtomsGenericRef.class, type = EffectType.ENABLE)
    String[] m_makeAtomsGenericFlags = flagNames(
    		RDKitAdjustQueryPropertiesNodeDialog.DEFAULT_ADJUST_QUERY_PARAMETERS.getMakeAtomsGenericFlags());
    
	static final class MakeAtomsGenericFlagsPersistor extends ReducedFlagsPersistor {

		protected MakeAtomsGenericFlagsPersistor() {
			super("make_atoms_generic_flags");
		}
		
	}

    @Widget(title = "Make bonds generic", description = "Convert bonds to any-bond queries.")
    @Persist(configKey = "make_bonds_generic")
    @ValueReference(MakeBondsGenericRef.class)
    boolean m_makeBondsGeneric =
        RDKitAdjustQueryPropertiesNodeDialog.DEFAULT_ADJUST_QUERY_PARAMETERS.getMakeBondsGeneric();

    static final class MakeBondsGenericRef implements BooleanReference {
    }

    @Widget(title = "Flags to adjust any-bond queries", 
    	description = "Control which bonds are converted to any-bond queries.")
    @Persistor(MakeBondsGenericFlagsPersistor.class)
    @ChoicesProvider(AdjustQueryWhichFlagsProvider.class)
    @Effect(predicate = MakeBondsGenericRef.class, type = EffectType.ENABLE)
    String[] m_makeBondsGenericFlags = flagNames(
			RDKitAdjustQueryPropertiesNodeDialog.DEFAULT_ADJUST_QUERY_PARAMETERS.getMakeBondsGenericFlags());
    
	static final class MakeBondsGenericFlagsPersistor extends ReducedFlagsPersistor {

		protected MakeBondsGenericFlagsPersistor() {
			super("make_bonds_generic_flags");
		}
		
	}

    @Widget(title = "Make dummies queries", description = """
    		Enable so that dummy atoms that do not have a specified isotope are converted to any-atom queries.
    		""")
    @Persist(configKey = "make_dummies_queries")
    boolean m_makeDummiesQueries =
        RDKitAdjustQueryPropertiesNodeDialog.DEFAULT_ADJUST_QUERY_PARAMETERS.getMakeDummiesQueries();

    @Widget(title = "Aromatize if possible", description = "Perceive and set aromaticity.")
    @Persist(configKey = "aromatize_if_possible")
    boolean m_aromatizeIfPossible =
        RDKitAdjustQueryPropertiesNodeDialog.DEFAULT_ADJUST_QUERY_PARAMETERS.getAromatizeIfPossible();

    @Widget(title = "Adjust conjugated 5 rings", description = """
    		Sets bond queries in conjugated five-rings to SINGLE|DOUBLE|AROMATIC.
    		""")
    @Persist(configKey = "adjust_conjugated_five_rings")
    boolean m_adjustConjugatedFiveRings =
        RDKitAdjustQueryPropertiesNodeDialog.DEFAULT_ADJUST_QUERY_PARAMETERS.getAdjustConjugatedFiveRings();

    @Widget(title = "Set MDL 5 ring aromaticity", description = """
    		Uses the 5-ring aromaticity behavior of the (former) MDL software as documented in the Chemical 
    		Representation Guide.
            """)
    @Persist(configKey = "set_mdl_five_ring_aromaticity")
    boolean m_setMDLFiveRingAromaticity =
        RDKitAdjustQueryPropertiesNodeDialog.DEFAULT_ADJUST_QUERY_PARAMETERS.getSetMDLFiveRingAromaticity();

    @Widget(title = "Adjust single bonds to degree 1 neighbors", description = """
    		Sets single bonds between aromatic atoms and degree one neighbors to SINGLE|AROMATIC.
    		""")
    @Persist(configKey = "adjust_single_bonds_to_degree_1_neighbors")
    boolean m_adjustSingleBondsToDegree1Neighbors =
        RDKitAdjustQueryPropertiesNodeDialog.DEFAULT_ADJUST_QUERY_PARAMETERS
            .getAdjustSingleBondsToDegreeOneNeighbors();

    @Widget(title = "Adjust single bonds between aromatic atoms", description = """
    		Sets non-ring single bonds between two aromatic atoms to SINGLE|AROMATIC.
    		""")
    @Persist(configKey = "adjust_single_bonds_between_aromatic_atoms")
    boolean m_adjustSingleBondsBetweenAromaticAtoms =
        RDKitAdjustQueryPropertiesNodeDialog.DEFAULT_ADJUST_QUERY_PARAMETERS
            .getAdjustSingleBondsBetweenAromaticAtoms();

    @Widget(title = "Use stereo care for bonds", description = """
    		Remove stereochemistry info from double bonds that do not have the stereoCare property set.
    		""")
    @Persist(configKey = "use_stereo_care_for_bonds")
    boolean m_useStereoCareForBonds =
        RDKitAdjustQueryPropertiesNodeDialog.DEFAULT_ADJUST_QUERY_PARAMETERS.getUseStereoCareForBonds();

    static final class MolColumnAutoGuessProvider extends RDKitMoleculeColumnAutoGuessProvider {
    	
        protected MolColumnAutoGuessProvider() {
            super(MolColumnRef.class, 0);
        }
        
    }
    
    static final class NewColumnNameAutoGuessProvider extends RDKitResultColumnNameAutoGuessProvider {

        NewColumnNameAutoGuessProvider() {
            super(MolColumnRef.class, NewColumnNameRef.class, "(Adjusted Queries)");
        }

        private Supplier<Boolean> m_removeSourceColumn;

        @Override
        public void init(final StateProviderInitializer initializer) {
            super.init(initializer);
            m_removeSourceColumn = initializer.getValueSupplier(RemoveSourceColumnRef.class);
        }

        @Override
        protected String[] getExcludedColumnNames(final NodeParametersInput parametersInput,
                final String currentInputColumnName) {
            return (m_removeSourceColumn.get() ? new String[]{currentInputColumnName} : null);
        }

    }
    
    static final class AdjustQueryWhichFlagsProvider implements StringChoicesProvider {
    	
		@Override
		public List<StringChoice> computeState(final NodeParametersInput context) {
			return getReducedFlags(AdjustQueryWhichFlags.values()).stream()
					.map(flag -> StringChoice.fromId(flag.name())).toList();
		}
        
    }
    
    private static String[] flagNames(final long lCombinedFlags) {
        final var flags = getReducedFlags(RDKitAdjustQueryPropertiesNodeDialog.getFlags(lCombinedFlags));
    	return flags == null ? new String[0] : flags.stream().map(flag -> flag.name()).toArray(String[]::new);
    }
    
    private static ArrayList<AdjustQueryWhichFlags> getReducedFlags(final AdjustQueryWhichFlags[] flags) {
    	ArrayList<AdjustQueryWhichFlags> reducedFlags = new ArrayList<>(Arrays.asList(flags));
    	reducedFlags.remove(AdjustQueryWhichFlags.ADJUST_IGNORENONE);
    	reducedFlags.remove(AdjustQueryWhichFlags.ADJUST_IGNOREALL);
    	return reducedFlags;
	}
    
	abstract static class ReducedFlagsPersistor implements NodeParametersPersistor<String[]> {

		private String m_configKey;
    	
    	protected ReducedFlagsPersistor(final String configKey) {
			m_configKey = configKey;
		}
		
		@Override
		public String[] load(NodeSettingsRO settings) throws InvalidSettingsException {
			ArrayList<String> flags = 
    				new ArrayList<>(Arrays.asList(settings.getStringArray(m_configKey, new String[0])));
			flags.remove(AdjustQueryWhichFlags.ADJUST_IGNORENONE.name());
			flags.remove(AdjustQueryWhichFlags.ADJUST_IGNOREALL.name());
			return flags.toArray(new String[0]);
		}

		@Override
		public void save(String[] param, NodeSettingsWO settings) {
			settings.addStringArray(m_configKey, param);
		}

		@Override
		public String[][] getConfigPaths() {
			return new String[][] {{m_configKey}};
		}
		
	}
    
}
