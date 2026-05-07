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

package org.rdkit.knime.nodes.sdfdifferencechecker;

import java.util.List;
import java.util.Optional;

import org.knime.chem.types.SdfValue;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataValue;
import org.knime.core.data.StringValue;
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
import org.knime.node.parameters.widget.choices.util.ColumnSelectionUtil;
import org.knime.node.parameters.widget.choices.util.CompatibleColumnsProvider;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsPositiveIntegerValidation;
import org.rdkit.knime.util.RDKitAdapterCellSupport;

/**
 * Node parameters for RDKit SDF Difference Checker.
 *
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class RDKitSDFDifferenceCheckerNodeParameters implements NodeParameters {

    final static List<Class<? extends DataValue>> SUPPORTED_TYPES =
            RDKitAdapterCellSupport.expandByAdaptableTypes(List.of(SdfValue.class, StringValue.class));

    @Widget(title = "SDF column (table 1)",
        description = "The first input column with SDF strings.")
    @Persist(configKey = "input_column_1")
    @ChoicesProvider(SdfColumnFromPort0Provider.class)
    @ValueProvider(InputColumn1AutoGuessProvider.class)
    @ValueReference(InputColumn1Ref.class)
    String m_inputColumn1Name;

    static final class InputColumn1Ref implements ParameterReference<String> {
    }

    @Widget(title = "SDF column (table 2)",
        description = "The second input column with SDF strings.")
    @Persist(configKey = "input_column_2")
    @ChoicesProvider(SdfColumnFromPort1Provider.class)
    @ValueProvider(InputColumn2AutoGuessProvider.class)
    @ValueReference(InputColumn2Ref.class)
    String m_inputColumn2Name;

    static final class InputColumn2Ref implements ParameterReference<String> {
    }

    @Widget(title = "Tolerance for all floating point numbers", description = """
            Every floating point number in the SDF string to be compared is allowed to have this tolerance range.
            """)
    @Persist(configKey = "tolerance")
    double m_tolerance = 0.1;

    @Widget(title = "Fail already on first encountered difference", description = """
            Set to true to fail immediately when the first difference has been encountered. Set to false to walk
            through the tables until the end to find more differences (console output) and then fail and report the
            first encountered difference.
            """)
    @Persist(configKey = "failOnFirstDifference")
    boolean m_failOnFirstDifference = true;

    @Widget(title = "Limit console output about different rows to", description = """
            To avoid that the console gets cluttered with all differences that may be found you may set a limit here.
            Default is 3.
            """)
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class)
    @Persist(configKey = "limitConsoleOutput")
    int m_limitConsoleOutput = 3;

    static final class InputColumn1AutoGuessProvider extends ColumnNameAutoGuessValueProvider {

        protected InputColumn1AutoGuessProvider() {
            super(InputColumn1Ref.class);
        }

        @Override
        protected Optional<DataColumnSpec> autoGuessColumn(NodeParametersInput parametersInput) {
            final var firstGuess = ColumnSelectionUtil.getFirstCompatibleColumnOfFirstPort(
                    parametersInput, RDKitAdapterCellSupport.expandByAdaptableTypes(SdfValue.class));
            return firstGuess.isEmpty() ? ColumnSelectionUtil.getFirstCompatibleColumnOfFirstPort(
                    parametersInput, RDKitAdapterCellSupport.expandByAdaptableTypes(StringValue.class)) : firstGuess;
        }

    }

    static final class InputColumn2AutoGuessProvider extends ColumnNameAutoGuessValueProvider {

        protected InputColumn2AutoGuessProvider() {
            super(InputColumn2Ref.class);
        }

        @Override
        protected Optional<DataColumnSpec> autoGuessColumn(NodeParametersInput parametersInput) {
            final var firstGuessOpt = getInputColumn2AutoGuessColumnForType(parametersInput,
                    RDKitAdapterCellSupport.expandByAdaptableTypes(SdfValue.class));
            return firstGuessOpt.isEmpty() ? getInputColumn2AutoGuessColumnForType(
                    parametersInput, RDKitAdapterCellSupport.expandByAdaptableTypes(StringValue.class)) : firstGuessOpt;
        }

        private static Optional<DataColumnSpec> getInputColumn2AutoGuessColumnForType(
                NodeParametersInput parametersInput, final Class<? extends DataValue>[] type) {
            final var inSpec1Opt = parametersInput.getInTableSpec(0);
            final var inSpec2Opt = parametersInput.getInTableSpec(1);
            final var compatibleColumns = ColumnSelectionUtil.getCompatibleColumns(parametersInput, 1, type);
            if (inSpec1Opt.isEmpty() && inSpec2Opt.isEmpty()) {
                return Optional.empty();
            } else if (inSpec1Opt.isEmpty() ^ inSpec2Opt.isEmpty()) {
                return compatibleColumns.isEmpty() ? Optional.empty() : Optional.of(compatibleColumns.get(0));
            } else if (inSpec1Opt.get() == inSpec2Opt.get()) {
                if (compatibleColumns.isEmpty()) {
                    return Optional.empty();
                }
                return compatibleColumns.size() > 1
                        ? Optional.of(compatibleColumns.get(1))
                        : Optional.of(compatibleColumns.get(0));
            } else {
                return compatibleColumns.isEmpty() ? Optional.empty() : Optional.of(compatibleColumns.get(0));
            }
        }

    }

    static final class SdfColumnFromPort0Provider extends CompatibleColumnsProvider {

        SdfColumnFromPort0Provider() {
            super(SUPPORTED_TYPES);
        }

    }

    static final class SdfColumnFromPort1Provider extends CompatibleColumnsProvider {

        SdfColumnFromPort1Provider() {
            super(SUPPORTED_TYPES);
        }

        @Override
        public int getInputTableIndex(final NodeParametersInput parametersInput) {
            return 1;
        }

    }

}
