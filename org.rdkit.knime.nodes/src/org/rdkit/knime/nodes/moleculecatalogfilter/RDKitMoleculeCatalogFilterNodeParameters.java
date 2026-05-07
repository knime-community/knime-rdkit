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

package org.rdkit.knime.nodes.moleculecatalogfilter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.RDKit.FilterCatalogParams.FilterCatalogs;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.StringChoicesProvider;
import org.knime.node.parameters.widget.choices.filter.TwinlistWidget;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.rdkit.knime.util.RDKitMoleculeColumnAutoGuessProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;

/**
 * Node parameters for RDKit Molecule Catalog Filter.
 *
 * @author Jannik Semperowitsch, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class RDKitMoleculeCatalogFilterNodeParameters implements NodeParameters {

    @Widget(title = "RDKit mol column", description = "The input column with RDKit Molecules.")
    @ChoicesProvider(RDKitMoleculeColumnChoicesProvider.class)
    @ValueReference(InputColumnRef.class)
    @ValueProvider(InputColumnAutoGuesser.class)
    @Persist(configKey = RDKitMoleculeCatalogFilterNodeModel.CFG_INPUT_COLUMN)
    String m_inputColumn;

    static final class InputColumnRef implements ParameterReference<String> {
    }

    static final class InputColumnAutoGuesser extends RDKitMoleculeColumnAutoGuessProvider {

        InputColumnAutoGuesser() {
            super(InputColumnRef.class, 0);
        }
    }

    @Widget(title = "Filter catalogs to apply",
        description = "Define which filter catalogs shall be used for filtering.")
    @ChoicesProvider(FilterCatalogsChoicesProvider.class)
    @Persist(configKey = RDKitMoleculeCatalogFilterNodeModel.CFG_CATALOGS)
    @TwinlistWidget(includedLabel = "Apply", excludedLabel = "Do not apply")
    String [] m_filterCatalogs = new String [0];

    static final class FilterCatalogsChoicesProvider implements StringChoicesProvider {
        @Override
        public List<String> choices(final NodeParametersInput context) {
            return Arrays.stream(FilterCatalogs.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        }
    }

    @Widget(title = "Prefix for result columns",
        description = "Column names for the second (filtered out) table are automatically assigned, but a prefix can "
            + "be assigned, which makes it possible to run this node sub-sequentially with different filters.")
    @Persist(configKey = RDKitMoleculeCatalogFilterNodeModel.CFG_OUTPUT_COLUMN_PREFIX)
    String m_outputColumnPrefix = RDKitMoleculeCatalogFilterNodeDialog.DEFAULT_COLUMN_PREFIX;

    @Widget(title = "Atom list handling",
        description = "Controls whether or not lists of atoms matching the filters are generated. This also controls "
            + "whether all matching atoms are in a single list or if there is a column per filter catalog.")
    @Persist(configKey = RDKitMoleculeCatalogFilterNodeModel.CFG_GENERATE_ATOM_LIST)
    AtomListHandling m_atomListHandling = AtomListHandling.None;
}
