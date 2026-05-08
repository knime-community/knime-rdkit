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

package org.rdkit.knime.nodes.optimizegeometry;

import java.util.function.Supplier;

import org.knime.node.parameters.Advanced;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.updates.util.BooleanReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsPositiveIntegerValidation;
import org.rdkit.knime.util.RDKitMoleculeColumnAutoGuessProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;
import org.rdkit.knime.util.RDKitResultColumnNameAutoGuessProvider;

/**
 * Node parameters for RDKit Optimize Geometry.
 *
 * @author Jannik Semperowitsch, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class RDKitOptimizeGeometryNodeParameters implements NodeParameters {

    @Section(title = "Output")
    interface OutputSection {
    }

    @Widget(title = "RDKit mol column",
        description = "The input column with RDKit Molecules.")
    @ChoicesProvider(RDKitMoleculeColumnChoicesProvider.class)
    @ValueProvider(InputColumnAutoGuessProvider.class)
    @ValueReference(InputColumnRef.class)
    @Persist(configKey = RDKitOptimizeGeometryNodeModel.CFG_INPUT_COLUMN)
    String m_inputColumn;

    interface InputColumnRef extends ParameterReference<String> {
    }

    static final class InputColumnAutoGuessProvider extends RDKitMoleculeColumnAutoGuessProvider {
        InputColumnAutoGuessProvider() {
            super(InputColumnRef.class, 0);
        }
    }

    @Widget(title = "Force field",
        description = "The force field type that shall be used for optimization.")
    @Persist(configKey = RDKitOptimizeGeometryNodeModel.CFG_FORCE_FIELD)
    ForceFieldType m_forceField = ForceFieldType.MMFF94;

    @Widget(title = "New column name for optimized molecule",
        description = "The name of the new column, which will contain the optimized molecule.")
    @Layout(OutputSection.class)
    @Persist(configKey = RDKitOptimizeGeometryNodeModel.CFG_NEW_MOLECULE_COLUMN_NAME)
    @ValueProvider(NewMoleculeColumnNameProvider.class)
    @ValueReference(NewMoleculeColumnNameRef.class)
    String m_newMoleculeColumnName;
    
    static final class NewMoleculeColumnNameRef implements ParameterReference<String> {
    }
    
    static final class NewMoleculeColumnNameProvider extends RDKitResultColumnNameAutoGuessProvider {

		protected NewMoleculeColumnNameProvider() {
			super(InputColumnRef.class, NewMoleculeColumnNameRef.class, "(Optimized Geometry)");
		}
		
		private Supplier<Boolean> m_removeSourceColumn;
		
		@Override
		public void init(StateProviderInitializer initializer) {
			super.init(initializer);
			m_removeSourceColumn = initializer.getValueSupplier(RemoveSourceColumnRef.class);
		}

		@Override
		protected String[] getExcludedColumnNames(NodeParametersInput parametersInput, 
			final String currentInputColumnName) {
			return (m_removeSourceColumn.get() ? new String[] { currentInputColumnName } : null);
		}
    	
    }

    @Widget(title = "Remove source column",
        description = "Set to true to remove the specified source column from the result table.")
    @Layout(OutputSection.class)
    @Persist(configKey = RDKitOptimizeGeometryNodeModel.CFG_REMOVE_SOURCE_COLUMNS)
    @ValueReference(RemoveSourceColumnRef.class)
    boolean m_removeSourceColumns;
    
    static final class RemoveSourceColumnRef implements BooleanReference {
    }

    @Widget(title = "New column name for converge information",
        description = "The name of the new column, which will contain a flag to tell whether the optimized molecule is "
            + "converged. If iterations were set to 0 (no optimization) this value will always be false.")
    @Layout(OutputSection.class)
    @Persist(configKey = RDKitOptimizeGeometryNodeModel.CFG_NEW_CONVERGE_COLUMN_NAME)
    @ValueReference(NewConvergenceColumnNameRef.class)
    String m_newConvergeColumnName;
    
    static final class NewConvergenceColumnNameRef implements ParameterReference<String> {
    }
    
    static final class NewConvergenceColumnNameProvider extends RDKitResultColumnNameAutoGuessProvider {

		protected NewConvergenceColumnNameProvider() {
			super("Converged", InputColumnRef.class, NewConvergenceColumnNameRef.class);
		}
		
		private Supplier<Boolean> m_removeSourceColumn;
		
		private Supplier<String> m_newMoleculeColumnName;
		
		@Override
		public void init(StateProviderInitializer initializer) {
			super.init(initializer);
			m_removeSourceColumn = initializer.getValueSupplier(RemoveSourceColumnRef.class);
			m_newMoleculeColumnName = initializer.getValueSupplier(NewMoleculeColumnNameRef.class);
		}

		@Override
		protected String[] getExcludedColumnNames(NodeParametersInput parametersInput, 
			final String currentInputColumnName) {
			return (m_removeSourceColumn.get() ? new String[] { currentInputColumnName } : null);
		}

		@Override
		protected String[] getAdditionalColumnNames(NodeParametersInput parametersInput, 
			String currentInputColumnName) {
			return new String[] { m_newMoleculeColumnName.get() };
		}

    }

    @Widget(title = "New column name for energy information",
        description = "The name of the new column, which will contain the energy value of the optimized molecule in "
            + "kcal/mol.")
    @Layout(OutputSection.class)
    @Persist(configKey = RDKitOptimizeGeometryNodeModel.CFG_NEW_ENERGY_COLUMN_NAME)
    @ValueProvider(NewEnergyColumnNameProvider.class)
    @ValueReference(NewEnergyColumnNameRef.class)
    String m_newEnergyColumnName;
    
    static final class NewEnergyColumnNameRef implements ParameterReference<String> {
    }
    
    static final class NewEnergyColumnNameProvider extends RDKitResultColumnNameAutoGuessProvider {

		protected NewEnergyColumnNameProvider() {
			super("Energy", InputColumnRef.class, NewEnergyColumnNameRef.class);
		}
		
		private Supplier<Boolean> m_removeSourceColumn;
		
		private Supplier<String> m_newMoleculeColumnName;
		
		private Supplier<String> m_newConvergenceColumnName;
		
		@Override
		public void init(StateProviderInitializer initializer) {
			super.init(initializer);
			m_removeSourceColumn = initializer.getValueSupplier(RemoveSourceColumnRef.class);
			m_newMoleculeColumnName = initializer.getValueSupplier(NewMoleculeColumnNameRef.class);
			m_newConvergenceColumnName = initializer.getValueSupplier(NewConvergenceColumnNameRef.class);
		}

		@Override
		protected String[] getExcludedColumnNames(NodeParametersInput parametersInput, 
			final String currentInputColumnName) {
			return (m_removeSourceColumn.get() ? new String[] { currentInputColumnName } : null);
		}

		@Override
		protected String[] getAdditionalColumnNames(NodeParametersInput parametersInput, 
			String currentInputColumnName) {
			return new String[] { m_newMoleculeColumnName.get(), m_newConvergenceColumnName.get() };
		}

    }

    @Widget(title = "Iterations",
        description = "Number of iterations to use for optimization. If the number is set too small, molecules will "
            + "not be fully converged. Set it to 0 iterations to skip optimizing and only calculate the energy value.")
    @Advanced
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class)
    @Persist(configKey = RDKitOptimizeGeometryNodeModel.CFG_ITERATIONS)
    int m_iterations = RDKitOptimizeGeometryNodeDialog.DEFAULT_ITERATIONS;

    @Widget(title = "Remove starting coordinates before optimizing the molecule",
        description = "Set to true to remove coordinates before starting the optimization process. This may affect "
            + "the results in a positive way.")
    @Advanced
    @Persist(configKey = RDKitOptimizeGeometryNodeModel.CFG_REMOVE_STARTING_COORDINATES)
    boolean m_removeStartingCoordinates;
}
