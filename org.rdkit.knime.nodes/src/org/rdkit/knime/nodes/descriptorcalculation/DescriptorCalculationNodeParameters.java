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

package org.rdkit.knime.nodes.descriptorcalculation;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

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
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.StringChoice;
import org.knime.node.parameters.widget.choices.StringChoicesProvider;
import org.knime.node.parameters.widget.choices.filter.TwinlistWidget;
import org.rdkit.knime.util.RDKitMoleculeColumnAutoGuessProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;

/**
 * Node parameters for RDKit Descriptor Calculation.
 *
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class DescriptorCalculationNodeParameters implements NodeParameters {

    @Widget(title = "RDKit mol column", description = """
            The name of the column in input table containing RDKit molecules.
            """)
    @Persist(configKey = "input_column")
    @ChoicesProvider(RDKitMoleculeColumnChoicesProvider.class)
    @ValueProvider(MolColumnAutoGuessProvider.class)
    @ValueReference(MolColumnRef.class)
    String m_inputColumn;

    static final class MolColumnRef implements ParameterReference<String> {
    }

    @Widget(title = "Available descriptors", description = """
            The list of descriptors that are available for calculation.
            """)
    @Persistor(SelectedDescriptorsPersistor.class)
    @ChoicesProvider(DescriptorChoicesProvider.class)
    @TwinlistWidget(includedLabel = "Calculate", excludedLabel = "Skip")
    String[] m_selectedDescriptors = Arrays.stream(Descriptor.class.getEnumConstants())
            .filter(d -> (d != null && !d.name().startsWith("FlowVariablePlaceHolder"))).map(Descriptor::name)
            .toArray(String[]::new);

    static final class MolColumnAutoGuessProvider extends RDKitMoleculeColumnAutoGuessProvider {

        protected MolColumnAutoGuessProvider() {
            super(MolColumnRef.class, 0);
        }

    }

    static final class DescriptorChoicesProvider implements StringChoicesProvider {

        @Override
        public List<StringChoice> computeState(final NodeParametersInput context) {
            return Arrays.stream(Descriptor.class.getEnumConstants())
                    .filter(d -> (d != null && !d.name().startsWith("FlowVariablePlaceHolder")))
                    .map(d -> new StringChoice(d.name(), d.toString())).toList();
        }

    }

    static final class SelectedDescriptorsPersistor implements NodeParametersPersistor<String[]> {

        @Override
        public String[] load(final NodeSettingsRO settings) throws InvalidSettingsException {
            return Arrays
                    .stream(settings.getStringArray("selectedDescriptors",
                            Arrays.stream(Descriptor.class.getEnumConstants()).map(Descriptor::name)
                                    .toArray(String[]::new)))
                    .filter(d -> (d != null && !d.startsWith("FlowVariablePlaceHolder"))).toArray(String[]::new);
        }

        @Override
        public void save(final String[] param, final NodeSettingsWO settings) {
            final long maxPlaceholders = Arrays.stream(Descriptor.values())
                    .filter(d -> d.name().startsWith("FlowVariablePlaceHolder")).count();
            final long numPlaceholders = Math.max(0, maxPlaceholders - param.length);
            final String[] allSelectedDescriptors = Stream.concat( //
                    Arrays.stream(param), //
                    Arrays.stream(Descriptor.values()).filter(d -> d.name().startsWith("FlowVariablePlaceHolder"))
                            .limit(numPlaceholders).map(Descriptor::name)) //
                    .toArray(String[]::new);
            settings.addStringArray("selectedDescriptors", allSelectedDescriptors);
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][] { new String[] { "selectedDescriptors" } };
        }

    }

}
