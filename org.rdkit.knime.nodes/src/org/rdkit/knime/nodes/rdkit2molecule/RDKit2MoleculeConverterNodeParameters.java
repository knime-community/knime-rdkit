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
    
package org.rdkit.knime.nodes.rdkit2molecule;

import java.util.function.Supplier;

import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.util.BooleanReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.ValueSwitchWidget;
import org.rdkit.knime.nodes.rdkit2molecule.RDKit2MoleculeConverterNodeModel.DestinationFormat;
import org.rdkit.knime.util.RDKitLegacyPersistors.LegacyMoleculeColumnPersistor;
import org.rdkit.knime.util.RDKitMoleculeColumnAutoGuessProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;
import org.rdkit.knime.util.RDKitResultColumnNameAutoGuessProvider;

/**
 * Node parameters for RDKit To Molecule.
 * 
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class RDKit2MoleculeConverterNodeParameters implements NodeParameters {

    @Widget(title = "RDKit mol column", description = "The column containing RDKit molecules to be converted.")
    @Persistor(InputColumnNamePersistor.class)
    @ChoicesProvider(RDKitMoleculeColumnChoicesProvider.class)
    @ValueProvider(InputColumnAutoGuessProvider.class)
    @ValueReference(InputColumnNameRef.class)
    String m_inputColumnName;

    interface InputColumnNameRef extends ParameterReference<String> {
    }

    @Widget(title = "New column name", description = "Name of the new column appended to the output table.")
    @Persist(configKey = "new_column_name")
    @ValueProvider(NewColumnNameAutoGuessProvider.class)
    @ValueReference(NewColumnNameRef.class)
    String m_newColumnName;

    static final class NewColumnNameRef implements ParameterReference<String> {
    }

    @Widget(title = "Remove source column",
        description = "If checked, the input RDKit mol column is removed from the output table.")
    @Persist(configKey = "remove_source_columns")
    @ValueReference(RemoveSourceColumnRef.class)
    boolean m_removeSourceColumns;

    static final class RemoveSourceColumnRef implements BooleanReference {
    }

    @Widget(title = "Destination format", description = "The format to which the RDKit molecules will be converted.")
    @Persist(configKey = "destination_format")
    @ValueSwitchWidget
    DestinationFormat m_destinationFormat = DestinationFormat.SDF;

    static final class NewColumnNameAutoGuessProvider extends RDKitResultColumnNameAutoGuessProvider {

        NewColumnNameAutoGuessProvider() {
            super(InputColumnNameRef.class, NewColumnNameRef.class, "(Molecule)");
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
    
    static final class InputColumnAutoGuessProvider extends RDKitMoleculeColumnAutoGuessProvider {
    	
        InputColumnAutoGuessProvider() {
            super(InputColumnNameRef.class, 0);
        }
        
    }

    static final class InputColumnNamePersistor extends LegacyMoleculeColumnPersistor {
    	
        InputColumnNamePersistor() {
            super("input_column", "first_column");
        }
        
    }
    
}
