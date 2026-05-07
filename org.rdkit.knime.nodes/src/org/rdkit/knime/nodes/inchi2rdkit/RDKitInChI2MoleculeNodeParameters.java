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

package org.rdkit.knime.nodes.inchi2rdkit;

import java.util.Arrays;
import java.util.Optional;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.StringValue;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.layout.After;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.legacy.updates.ColumnNameAutoGuessValueProvider;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.util.ColumnSelectionUtil;
import org.knime.node.parameters.widget.choices.util.CompatibleColumnsProvider;
import org.rdkit.knime.util.RDKitAdapterCellSupport;

/**
 * Node parameters for RDKit From InChI.
 *
 * @author Jannik Semperowitsch, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class RDKitInChI2MoleculeNodeParameters implements NodeParameters {

    interface DialogLayout {
        interface Input {
        }

        @Section(title = "RDKit Molecule Generation")
        @After(Input.class)
        interface MoleculeGeneration {
        }

        @Section(title = "Extra InChI Conversion Information")
        @After(MoleculeGeneration.class)
        interface ExtraInformation {
        }
    }

    @Widget(title = "InChI code column", description = "The input column with InChI codes.")
    @ChoicesProvider(InChIColumnChoicesProvider.class)
    @ValueProvider(InChIColumnNameProvider.class)
    @ValueReference(InChIColumnNameRef.class)
    @Layout(DialogLayout.Input.class)
    @Persist(configKey = RDKitInChI2MoleculeNodeModel.CFG_INPUT_COLUMN)
    String m_inputColumn;

    static final class InChIColumnNameRef implements ParameterReference<String> {
    }

    static final class InChIColumnChoicesProvider extends CompatibleColumnsProvider {
        InChIColumnChoicesProvider() {
        	super(Arrays.asList(RDKitAdapterCellSupport.expandByAdaptableTypes(StringValue.class)));
        }
    }

    static final class InChIColumnNameProvider extends ColumnNameAutoGuessValueProvider {
        protected InChIColumnNameProvider() {
            super(InChIColumnNameRef.class);
        }

        @Override
        protected Optional<DataColumnSpec> autoGuessColumn(final NodeParametersInput parametersInput) {
        	return ColumnSelectionUtil.getFirstCompatibleColumn(parametersInput, 0,
                    RDKitAdapterCellSupport.expandByAdaptableTypes(StringValue.class));
        }
    }

    @Widget(title = "Remove source column",
            description = "Enable to remove the specified source column from the result table.")
    @Layout(DialogLayout.Input.class)
    @Persist(configKey = RDKitInChI2MoleculeNodeModel.CFG_REMOVE_SOURCE_COLUMNS)
    boolean m_removeSourceColumns;

    @Widget(title = "New column name for RDKit molecules",
            description = "The name of the new column, which will contain the RDKit molecules.")
    @Layout(DialogLayout.MoleculeGeneration.class)
    @Persist(configKey = RDKitInChI2MoleculeNodeModel.CFG_NEW_COLUMN_NAME)
    String m_newMolColumnName;

    @Widget(title = "Sanitize molecule", description = "Enable to sanitize the generated molecule.")
    @Layout(DialogLayout.MoleculeGeneration.class)
    @Persist(configKey = RDKitInChI2MoleculeNodeModel.CFG_SANITIZE)
    boolean m_sanitize = true;

    @Widget(title = "Remove hydrogens", description = "Enable to remove hydrogens from the generated molecule.")
    @Layout(DialogLayout.MoleculeGeneration.class)
    @Persist(configKey = RDKitInChI2MoleculeNodeModel.CFG_REMOVE_HYDROGENS)
    boolean m_removeHydrogens = true;

    @Widget(title = "New column name prefix for extra information",
            description = "The prefix of column names, which will contain the extra information about the conversion.")
    @Layout(DialogLayout.ExtraInformation.class)
    @Persist(configKey = RDKitInChI2MoleculeNodeModel.CFG_NEW_EXTRA_INFO_COLUMN_NAME_PREFIX)
    String m_extraInformationColumnNamePrefix;

    @Widget(title = "Return code column",
            description = "Enable to also generate a column that contains the return code of the InChI code conversion.")
    @Layout(DialogLayout.ExtraInformation.class)
    @Persist(configKey = RDKitInChI2MoleculeNodeModel.CFG_GENERATE_RETURN_CODE)
    boolean m_generateReturnCode;

    @Widget(title = "Message column",
            description = "Enable to also generate a column that contains a message generated during the InChI code "
                    + "conversion.")
    @Layout(DialogLayout.ExtraInformation.class)
    @Persist(configKey = RDKitInChI2MoleculeNodeModel.CFG_GENERATE_MESSAGE)
    boolean m_generateMessage;

    @Widget(title = "Log column",
            description = "Enable to also generate a column that contains a log message of the InChI code conversion.")
    @Layout(DialogLayout.ExtraInformation.class)
    @Persist(configKey = RDKitInChI2MoleculeNodeModel.CFG_GENERATE_LOG)
    boolean m_generateLog;

}
