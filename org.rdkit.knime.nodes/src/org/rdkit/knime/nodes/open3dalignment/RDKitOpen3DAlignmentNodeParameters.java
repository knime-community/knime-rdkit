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
package org.rdkit.knime.nodes.open3dalignment;

import java.util.function.Supplier;

import org.knime.node.parameters.Advanced;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.updates.util.BooleanReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsNonNegativeValidation;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsPositiveIntegerValidation;
import org.rdkit.knime.util.RDKitMoleculeColumnAutoGuessProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;
import org.rdkit.knime.util.RDKitResultColumnNameAutoGuessProvider;

/**
 * Node parameters for RDKit Open 3D Alignment.
 *
 * @author Jannik Semperowitsch, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class RDKitOpen3DAlignmentNodeParameters implements NodeParameters {

    @Widget(title = "Query RDKit mol column (table 1)",
        description = "The input column of table 1 with RDKit Query Molecules to be aligned.")
    @ChoicesProvider(RDKitMoleculeColumnChoicesProvider.class)
    @ValueProvider(QueryColumnAutoGuesser.class)
    @ValueReference(QueryColumnRef.class)
    @Persist(configKey = RDKitOpen3DAlignmentNodeModel.CFG_QUERY_INPUT_COLUMN)
    String m_queryInputColumn;

    /** Provides RDKit Mol compatible columns from input port 0 (query table). */
    static final class QueryMolColumnsProvider extends RDKitMoleculeColumnChoicesProvider {
        QueryMolColumnsProvider() {
            super(0);
        }
    }

    static final class QueryColumnRef implements ParameterReference<String> {
    }

    static final class QueryColumnAutoGuesser extends RDKitMoleculeColumnAutoGuessProvider {
        QueryColumnAutoGuesser() {
            super(QueryColumnRef.class, 0);
        }
    }

    @Widget(title = "Reference RDKit mol column (table 2)",
        description = "The input column of table 2 with RDKit Reference Molecules to base the alignment on. "
            + "If the table contains only a single row all query molecules of table 1 will be aligned based on this. "
            + "If the table contains more than one row, the alignment will be performed row by row until one of the "
            + "tables has no more rows to process.")
    @ChoicesProvider(ReferenceMolColumnsProvider.class)
    @ValueProvider(ReferenceColumnAutoGuesser.class)
    @ValueReference(ReferenceColumnRef.class)
    @Persist(configKey = RDKitOpen3DAlignmentNodeModel.CFG_REFERENCE_INPUT_COLUMN)
    String m_referenceInputColumn;

    /** Provides RDKit Mol compatible columns from input port 1 (reference table). */
    static final class ReferenceMolColumnsProvider extends RDKitMoleculeColumnChoicesProvider {
        ReferenceMolColumnsProvider() {
            super(1);
        }
    }

    static final class ReferenceColumnRef implements ParameterReference<String> {
    }

    static final class ReferenceColumnAutoGuesser extends RDKitMoleculeColumnAutoGuessProvider {
        ReferenceColumnAutoGuesser() {
            super(ReferenceColumnRef.class, 1, 0);
        }
    }

    @Widget(title = "New column name for aligned molecule",
        description = "The name of the new column, which will contain the aligned molecule.")
    @Persist(configKey = RDKitOpen3DAlignmentNodeModel.CFG_NEW_ALIGNED_COLUMN_NAME)
    @ValueProvider(NewAlignedColumnNameProvider.class)
    @ValueReference(NewAlignedColumnNameRef.class)
    String m_newAlignedColumnName;
    
    static final class NewAlignedColumnNameRef implements ParameterReference<String> {
	}
    
    static final class NewAlignedColumnNameProvider extends RDKitResultColumnNameAutoGuessProvider {

		protected NewAlignedColumnNameProvider() {
			super(QueryColumnRef.class, NewAlignedColumnNameRef.class, "(Aligned)");
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
        description = "Set to true to remove the specified source column of table 1 from the result table.")
    @Persist(configKey = RDKitOpen3DAlignmentNodeModel.CFG_REMOVE_SOURCE_COLUMNS)
    @ValueReference(RemoveSourceColumnRef.class)
    boolean m_removeSourceColumns;
    
    static final class RemoveSourceColumnRef implements BooleanReference {
    }

    @Widget(title = "New column name for Row IDs of reference used molecule",
        description = "The name of the new column, which will contain the row id of the used reference molecule of "
            + "table 2.")
    @Persist(configKey = RDKitOpen3DAlignmentNodeModel.CFG_NEW_REFID_COLUMN_NAME)
    @ValueProvider(NewRefIdColumnNameProvider.class)
    @ValueReference(NewRefIdColumnNameRef.class)
    String m_newRefIdColumnName;
    
    static final class NewRefIdColumnNameRef implements ParameterReference<String> {
	}
    
    static final class NewRefIdColumnNameProvider extends RDKitResultColumnNameAutoGuessProvider {

		protected NewRefIdColumnNameProvider() {
			super("Reference Row ID", QueryColumnRef.class, NewRefIdColumnNameRef.class);
		}
		
		private Supplier<Boolean> m_removeSourceColumn;
		
		private Supplier<String> m_newAlignedColumnName;
		
		@Override
		public void init(StateProviderInitializer initializer) {
			super.init(initializer);
			m_removeSourceColumn = initializer.getValueSupplier(RemoveSourceColumnRef.class);
			m_newAlignedColumnName = initializer.getValueSupplier(NewAlignedColumnNameRef.class);
		}

		@Override
		protected String[] getExcludedColumnNames(NodeParametersInput parametersInput, 
			final String currentInputColumnName) {
			return (m_removeSourceColumn.get() ? new String[] { currentInputColumnName } : null);
		}

		@Override
		protected String[] getAdditionalColumnNames(NodeParametersInput parametersInput,
				String currentInputColumnName) {
			return new String[] { m_newAlignedColumnName.get() };
		}
    	
    }

    @Widget(title = "New column name for RMSD information",
        description = "The name of the new column, which will contain the root-mean-square deviation (RMSD) for the "
            + "aligned molecule.")
    @Persist(configKey = RDKitOpen3DAlignmentNodeModel.CFG_NEW_RMSD_COLUMN_NAME)
    @ValueProvider(NewRmsdColumnNameProvider.class)
    @ValueReference(NewRmsdColumnNameRef.class)
    String m_newRmsdColumnName;
    
    static final class NewRmsdColumnNameRef implements ParameterReference<String> {
    }
    
    static final class NewRmsdColumnNameProvider extends RDKitResultColumnNameAutoGuessProvider {

		protected NewRmsdColumnNameProvider() {
			super("RMSD", QueryColumnRef.class, NewRmsdColumnNameRef.class);
		}
		
		private Supplier<Boolean> m_removeSourceColumn;
		
		private Supplier<String> m_newAlignedColumnName;
		
		private Supplier<String> m_newRefIdColumnName;
		
		@Override
		public void init(StateProviderInitializer initializer) {
			super.init(initializer);
			m_removeSourceColumn = initializer.getValueSupplier(RemoveSourceColumnRef.class);
			m_newAlignedColumnName = initializer.getValueSupplier(NewAlignedColumnNameRef.class);
			m_newRefIdColumnName = initializer.getValueSupplier(NewRefIdColumnNameRef.class);
		}

		@Override
		protected String[] getExcludedColumnNames(NodeParametersInput parametersInput, 
			final String currentInputColumnName) {
			return (m_removeSourceColumn.get() ? new String[] { currentInputColumnName } : null);
		}

		@Override
		protected String[] getAdditionalColumnNames(NodeParametersInput parametersInput,
				String currentInputColumnName) {
			return new String[] { m_newAlignedColumnName.get(), m_newRefIdColumnName.get() };
		}
    	
    }

    @Widget(title = "New column name for score information",
        description = "The name of the new column, which will contain the score for the aligned molecule.")
    @Persist(configKey = RDKitOpen3DAlignmentNodeModel.CFG_NEW_SCORE_COLUMN_NAME)
    @ValueProvider(NewScoreColumnNameProvider.class)
    @ValueReference(NewScoreColumnNameRef.class)
    String m_newScoreColumnName;
    
    static final class NewScoreColumnNameRef implements ParameterReference<String> {
	}
    
    static final class NewScoreColumnNameProvider extends RDKitResultColumnNameAutoGuessProvider {

		protected NewScoreColumnNameProvider() {
			super("Score", QueryColumnRef.class, NewScoreColumnNameRef.class);
		}
		
		private Supplier<Boolean> m_removeSourceColumn;
		
		private Supplier<String> m_newAlignedColumnName;
		
		private Supplier<String> m_newRefIdColumnName;
		
		private Supplier<String> m_newRmsdColumnName;
		
		@Override
		public void init(StateProviderInitializer initializer) {
			super.init(initializer);
			m_removeSourceColumn = initializer.getValueSupplier(RemoveSourceColumnRef.class);
			m_newAlignedColumnName = initializer.getValueSupplier(NewAlignedColumnNameRef.class);
			m_newRefIdColumnName = initializer.getValueSupplier(NewRefIdColumnNameRef.class);
			m_newRmsdColumnName = initializer.getValueSupplier(NewRmsdColumnNameRef.class);
		}

		@Override
		protected String[] getExcludedColumnNames(NodeParametersInput parametersInput, 
			final String currentInputColumnName) {
			return (m_removeSourceColumn.get() ? new String[] { currentInputColumnName } : null);
		}

		@Override
		protected String[] getAdditionalColumnNames(NodeParametersInput parametersInput,
				String currentInputColumnName) {
			return new String[] { m_newAlignedColumnName.get(), m_newRefIdColumnName.get(), m_newRmsdColumnName.get() };
		}
    	
    }

    @Advanced
    @Widget(title = "Allow reflection",
        description = "Set to true to allow reflection of structure parts during the alignment process.")
    @Persist(configKey = RDKitOpen3DAlignmentNodeModel.CFG_ALLOW_REFLECTION)
    boolean m_allowReflection = RDKitOpen3DAlignmentNodeDialog.DEFAULT_ALLOW_REFLECTION;

    @Advanced
    @Widget(title = "Maximal number of iterations",
        description = "The maximal number of iterations used in the alignment process.")
    @Persist(configKey = RDKitOpen3DAlignmentNodeModel.CFG_MAX_ITERATIONS)
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class)
    int m_maxIterations = RDKitOpen3DAlignmentNodeDialog.DEFAULT_MAX_ITERATIONS;
    
    @Advanced
    @Widget(title = "Accuracy (0 - most accurate, 3 - least accurate)",
        description = "Determines how accurate the alignment process shall work (0 - most accurate, 3 - least "
            + "accurate).")
    @NumberInputWidget(minValidation = IsNonNegativeValidation.class, maxValidation = Max3Validation.class)
    @Persist(configKey = RDKitOpen3DAlignmentNodeModel.CFG_ACCURACY)
    int m_accuracy = RDKitOpen3DAlignmentNodeDialog.DEFAULT_ACCURACY;

    static final class Max3Validation extends NumberInputWidgetValidation.MaxValidation {
        @Override
        protected double getMax() {
            return 3;
        }
    }
}
