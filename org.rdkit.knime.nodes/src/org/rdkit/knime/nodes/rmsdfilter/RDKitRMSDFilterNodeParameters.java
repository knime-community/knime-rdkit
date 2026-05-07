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
    
package org.rdkit.knime.nodes.rmsdfilter;

import java.util.Optional;

import org.knime.core.data.DataColumnSpec;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.legacy.updates.ColumnNameAutoGuessValueProvider;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.util.AllColumnsProvider;
import org.knime.node.parameters.widget.choices.util.ColumnSelectionUtil;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsNonNegativeValidation;
import org.rdkit.knime.util.RDKitMoleculeColumnAutoGuessProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;

/**
 * Node parameters for RDKit RMSD Filter.
 *
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class RDKitRMSDFilterNodeParameters implements NodeParameters {

    @Widget(title = "RDKit mol column with conformers",
        description = "The name of the column with RDKit molecules, each with exactly one conformer. "
            + "If the molecule has more than one conformer embedded, only the first one will be used "
            + "for the calculation.")
    @Persist(configKey = "input_mol_column")
    @ChoicesProvider(RDKitMoleculeColumnChoicesProvider.class)
    @ValueProvider(MolColumnAutoGuessProvider.class)
    @ValueReference(MolColumnRef.class)
    String m_moleculeInputColumnName;

    static final class MolColumnRef implements ParameterReference<String> {
    }

    @Widget(title = "Reference column (e.g. an ID)",
        description = "The name of the column that defines which conformers belong to each other.")
    @Persist(configKey = "input_ref_column")
    @ChoicesProvider(AllColumnsProvider.class)
    @ValueProvider(RefColumnAutoGuessProvider.class)
    @ValueReference(RefColumnRef.class)
    String m_referenceInputColumnName;

    static final class RefColumnRef implements ParameterReference<String> {
	}
    
    @Widget(title = "RMSD threshold",
        description = "The RMSD threshold used by the node to split the input table into two output "
            + "tables. Conformers with an RMSD value above or equal to the threshold go to the first "
            + "output table, those below go to the second.")
    @NumberInputWidget(minValidation = IsNonNegativeValidation.class)
    @Persist(configKey = "rmsd_threshold")
    double m_rmsdThreshold = 0.5;

    @Widget(title = "Ignore Hs (increases performance)",
        description = "Set this option to remove any existing hydrogens before performing the "
            + "calculation. (Default is false)")
    @Persist(configKey = "ignore_hs")
    boolean m_ignoreHsOption;
    
    static final class MolColumnAutoGuessProvider extends RDKitMoleculeColumnAutoGuessProvider {
    	
        MolColumnAutoGuessProvider() {
            super(MolColumnRef.class, 0);
        }
        
    }
    
    static final class RefColumnAutoGuessProvider extends ColumnNameAutoGuessValueProvider {

		protected RefColumnAutoGuessProvider() {
			super(RefColumnRef.class);
		}

		@Override
		protected Optional<DataColumnSpec> autoGuessColumn(NodeParametersInput parametersInput) {
			final var compatibleColumns = ColumnSelectionUtil.getAllColumnsOfFirstPort(parametersInput);
			final var specificReferenceColumn = compatibleColumns.stream().filter(
					colSpec -> colSpec.getName() != null && colSpec.getName().toUpperCase().indexOf("REFERENCE") >= 0)
					.findFirst();
			return specificReferenceColumn.isPresent() ? specificReferenceColumn : 
				compatibleColumns.stream().findFirst();
		}
    	
    }
    
}
