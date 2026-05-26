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

package org.rdkit.knime.nodes.molfragmenter;

import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MaxValidation;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsPositiveIntegerValidation;
import org.rdkit.knime.util.RDKitMoleculeColumnAutoGuessProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;

/**
 * Node parameters for RDKit Molecule Fragmenter.
 *
 * @author Jannik Semperowitsch, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class RDKitMolFragmenterNodeParameters implements NodeParameters {

    @Widget(title = "RDKit mol column", description = "The column containing the molecules.")
    @ChoicesProvider(RDKitMoleculeColumnChoicesProvider.class)
    @ValueReference(InputColumnRef.class)
    @ValueProvider(InputColumnAutoGuesser.class)
    @Persist(configKey = RDKitMolFragmenterNodeModel.CFG_INPUT_COLUMN)
    String m_inputColumn;

    static final class InputColumnRef implements ParameterReference<String> {
    }

    static final class InputColumnAutoGuesser extends RDKitMoleculeColumnAutoGuessProvider {

        InputColumnAutoGuesser() {
            super(InputColumnRef.class, 0);
        }
    }

    @Widget(title = "Min path length", description = "The minimum size (in bonds) of the subgraphs to be included.")
    @Persist(configKey = RDKitMolFragmenterNodeModel.CFG_MIN_PATH)
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class, maxValidation = MaxPathLengthValidation.class)
    int m_minPath = 4;

    static final class MaxPathLengthValidation extends MaxValidation {
        @Override
        protected double getMax() {
            return 10;
        }
    }

    @Widget(title = "Max path length", description = "The maximum size (in bonds) of the subgraphs to be included.")
    @Persist(configKey = RDKitMolFragmenterNodeModel.CFG_MAX_PATH)
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class, maxValidation = MaxPathLengthValidation.class)
    int m_maxPath = 7;
}
