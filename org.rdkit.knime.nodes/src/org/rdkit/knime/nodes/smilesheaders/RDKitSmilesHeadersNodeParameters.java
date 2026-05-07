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
    
package org.rdkit.knime.nodes.smilesheaders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.knime.chem.types.SmilesValue;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.StringValue;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.legacy.updates.ColumnNameAutoGuessValueProvider;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.Effect.EffectType;
import org.knime.node.parameters.updates.EffectPredicate;
import org.knime.node.parameters.updates.EffectPredicateProvider;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.ColumnChoicesProvider;
import org.knime.node.parameters.widget.choices.util.ColumnSelectionUtil;
import org.knime.node.parameters.widget.choices.util.CompatibleColumnsProvider;
import org.knime.node.parameters.widget.message.TextMessage;
import org.knime.node.parameters.widget.message.TextMessage.MessageType;
import org.knime.node.parameters.widget.message.TextMessage.SimpleTextMessageProvider;
import org.rdkit.knime.util.RDKitAdapterCellSupport;

/**
 * Node parameters for RDKit SMILES Headers.
 *
 * @author Jannik Semperowitsch, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class RDKitSmilesHeadersNodeParameters implements NodeParameters {

    @Widget(title = "Use column titles of data table as SMILES property definitions",
        description = "Flag this option to use column titles as SMILES values. This option is only visible and takes "
            + "only effect, if there is no SMILES Definition Table connected and if it is checked. Invalid SMILES "
            + "values in header titles are ignored without warnings or errors.")
    @Effect(predicate = HasSmilesDefinitionTable.class, type = EffectType.HIDE)
    @Persist(configKey = RDKitSmilesHeadersNodeModel.CFG_USE_COLUMN_TITLES_AS_SMILES)
    boolean m_useColumnTitles = true;

    @Widget(title = "Column with target column names",
        description = "The input column of the SMILES Definition Table that defines which Data Table column headers "
            + "shall be changed.")
    @ChoicesProvider(InputColumnChoicesProvider.class)
    @ValueReference(TargetColumnRef.class)
    @ValueProvider(TargetColumnAutoGuesser.class)
    @Effect(predicate = HasSmilesDefinitionTable.class, type = EffectType.SHOW)
    @Persist(configKey = RDKitSmilesHeadersNodeModel.CFG_NAMES_COLUMN)
    String m_targetColumnName;

    static final class TargetColumnRef implements ParameterReference<String> {
    }

    static final class TargetColumnAutoGuesser extends ColumnNameAutoGuessValueProvider {

        TargetColumnAutoGuesser() {
            super(TargetColumnRef.class);
        }

        @Override
        protected Optional<DataColumnSpec> autoGuessColumn(final NodeParametersInput parametersInput) {
            return ColumnSelectionUtil.getFirstCompatibleColumn(parametersInput, 1,
                    RDKitAdapterCellSupport.expandByAdaptableTypes(StringValue.class));
        }
    }

    static final class HasSmilesDefinitionTable implements EffectPredicateProvider {
        @Override
        public EffectPredicate init(final PredicateInitializer i) {
            return i.getConstant(input -> RDKitSmilesHeadersNodeModel.hasSmilesDefinitionTable(
                new PortObjectSpec[]{
                    input.getInTableSpec(0).orElse(null),
                    input.getInTableSpec(1).orElse(null)
                }));
        }
    }

    static final class InputColumnChoicesProvider extends CompatibleColumnsProvider {

		protected InputColumnChoicesProvider() {
			super(Arrays.asList(RDKitAdapterCellSupport.expandByAdaptableTypes(StringValue.class)));
		}

		@Override
		public int getInputTableIndex(NodeParametersInput parametersInput) {
			return 1;
		}
    	
    }
    
    @TextMessage(HintMessage.class)
    @Effect(predicate = HasSmilesDefinitionTable.class, type = EffectType.HIDE)
    Void m_hint;
    
    static final class HintMessage implements SimpleTextMessageProvider {

        @Override
        public boolean showMessage(final NodeParametersInput context) {
            return true;
        }

        @Override
        public String title() {
            return "Hint";
        }

        @Override
        public String description() {
            return "Connect a SMILES definition table to define target columns and their SMILES values.";
        }

        @Override
        public MessageType type() {
            return MessageType.INFO;
        }
    }
    

    @Widget(title = "Column with new SMILES values",
        description = "The new SMILES values to be set for the specified Data Table columns. Note, that an empty or "
            + "missing cell will actually remove the SMILES header information.")
    @ChoicesProvider(SmilesColumnsFromPort1.class)
    @ValueReference(SmilesColumnRef.class)
    @ValueProvider(SmilesColumnAutoGuesser.class)
    @Effect(predicate = HasSmilesDefinitionTable.class, type = EffectType.SHOW)
    @Persist(configKey = RDKitSmilesHeadersNodeModel.CFG_SMILES_COLUMN)
    String m_smilesColumnName;

    static final class SmilesColumnRef implements ParameterReference<String> {
    }

    static final class SmilesColumnAutoGuesser extends ColumnNameAutoGuessValueProvider {

        SmilesColumnAutoGuesser() {
            super(SmilesColumnRef.class);
        }

        @Override
        protected Optional<DataColumnSpec> autoGuessColumn(final NodeParametersInput parametersInput) {
            return ColumnSelectionUtil.getFirstCompatibleColumn(parametersInput, 1,
                    RDKitAdapterCellSupport.expandByAdaptableTypes(SmilesValue.class));
        }
    }

    static final class SmilesColumnsFromPort1 implements ColumnChoicesProvider {
        @Override
        public List<DataColumnSpec> columnChoices(final NodeParametersInput context) {
            return ColumnSelectionUtil.getCompatibleColumns(context, 1,
                RDKitAdapterCellSupport.expandByAdaptableTypes(SmilesValue.class));
        }
    }

    @Widget(title = "Remove existing SMILES values in all headers first",
        description = "Flag this option to remove all existing SMILES values from the properties of all(!) header "
            + "columns of the data table before setting new SMILES values based on the SMILES Definition Table.")
    @Persist(configKey = RDKitSmilesHeadersNodeModel.CFG_COMPLETE_RESET)
    boolean m_completeReset;
}
