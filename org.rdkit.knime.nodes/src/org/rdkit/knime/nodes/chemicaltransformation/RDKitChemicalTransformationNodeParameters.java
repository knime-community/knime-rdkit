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

package org.rdkit.knime.nodes.chemicaltransformation;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.knime.chem.types.RxnValue;
import org.knime.chem.types.SmartsValue;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataValue;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.legacy.updates.ColumnNameAutoGuessValueProvider;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.updates.util.BooleanReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.util.ColumnSelectionUtil;
import org.knime.node.parameters.widget.choices.util.CompatibleColumnsProvider;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MaxValidation;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsPositiveIntegerValidation;
import org.rdkit.knime.util.RDKitAdapterCellSupport;
import org.rdkit.knime.util.RDKitMoleculeColumnAutoGuessProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;
import org.rdkit.knime.util.RDKitResultColumnNameAutoGuessProvider;

/**
 * Node parameters for RDKit Chemical Transformation.
 *
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class RDKitChemicalTransformationNodeParameters implements NodeParameters {
	
	static final Class<? extends DataValue>[] REACTION_COMPATIBLE_TYPES = 
			RDKitAdapterCellSupport.expandByAdaptableTypes(new Class[] {RxnValue.class, SmartsValue.class});
	
    @Widget(title = "Reactant RDKit mol column (table 1)",
        description = "The input column with RDKit Molecules.")
    @Persist(configKey = "input_mol_column")
    @ChoicesProvider(RDKitMoleculeColumnChoicesProvider.class)
    @ValueProvider(MolInputColumnAutoGuessProvider.class)
    @ValueReference(MolInputColumnRef.class)
    String m_molInputColumnName;

    static final class MolInputColumnRef implements ParameterReference<String> {
    }
    
    @Widget(title = "Reaction column (table 2)",
        description = "The input column with the reactions (either SMARTS or Rxn).")
    @Persist(configKey = "input_reaction_column")
    @ChoicesProvider(ReactionColumnChoicesProvider.class)
    @ValueProvider(ReactionInputColumnAutoGuessValueProvider.class)
    @ValueReference(ReactionInputColumnRef.class)
    String m_reactionInputColumnName;
    
    static final class ReactionInputColumnRef implements ParameterReference<String> {
	}

    @Widget(title = "New column name",
        description = "The name of the new column, which will contain the calculation results.")
    @Persist(configKey = "new_column_name")
    @ValueProvider(NewColumnNameProvider.class)
    @ValueReference(NewColumnNameRef.class)
    String m_newColumnName;

    static final class NewColumnNameRef implements ParameterReference<String> {
    }

    @Widget(title = "Remove source column",
        description = "Set to true to remove the molecule input column from the result table.")
    @Persist(configKey = "remove_source_columns")
    @ValueReference(RemoveSourceColumnRef.class)
    boolean m_removeSourceColumns;

    static final class RemoveSourceColumnRef implements BooleanReference {
    }

    @Widget(title = "Maximal number of applied reaction cycles",
        description = "To avoid eternal reaction cycles the number of cycles can be limited.")
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class, maxValidation = MaxCyclesValidation.class)
    @Persist(configKey = "max_reaction_cycles")
    int m_maxReactionCycles = RDKitChemicalTransformationNodeDialog.DEFAULT_MAX_REACTION_CYCLES;

    static final class MaxCyclesValidation extends MaxValidation {
    	
        @Override
        protected double getMax() {
            return 9999;
        }
        
    }
    
    static final class NewColumnNameProvider extends RDKitResultColumnNameAutoGuessProvider {

        protected NewColumnNameProvider() {
            super(MolInputColumnRef.class, NewColumnNameRef.class, "(Transformed)");
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
    
    static final class MolInputColumnAutoGuessProvider extends RDKitMoleculeColumnAutoGuessProvider {

		protected MolInputColumnAutoGuessProvider() {
			super(MolInputColumnRef.class, 1);
		}
    	
    }
    
	static final class ReactionInputColumnAutoGuessValueProvider extends ColumnNameAutoGuessValueProvider {

		protected ReactionInputColumnAutoGuessValueProvider() {
			super(ReactionInputColumnRef.class);

		}

		@Override
		protected Optional<DataColumnSpec> autoGuessColumn(NodeParametersInput parametersInput) {
			return ColumnSelectionUtil.getFirstCompatibleColumn(parametersInput, 1, REACTION_COMPATIBLE_TYPES);
		}
		
	}
    
    static final class ReactionColumnChoicesProvider extends CompatibleColumnsProvider {

		@Override
		public int getInputTableIndex(NodeParametersInput parametersInput) {
			return 1;
		}

		protected ReactionColumnChoicesProvider() {
			super(Arrays.asList(REACTION_COMPATIBLE_TYPES));
		}
    	
    }

}
