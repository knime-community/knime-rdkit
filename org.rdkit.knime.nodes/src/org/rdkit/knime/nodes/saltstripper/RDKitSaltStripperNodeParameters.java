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
 *  propagated with or for interoperate with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 */

package org.rdkit.knime.nodes.saltstripper;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.Effect.EffectType;
import org.knime.node.parameters.updates.EffectPredicate;
import org.knime.node.parameters.updates.EffectPredicateProvider;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.ColumnChoicesProvider;
import org.knime.node.parameters.widget.message.TextMessage;
import org.knime.node.parameters.widget.message.TextMessage.MessageType;
import org.knime.node.parameters.widget.message.TextMessage.SimpleTextMessageProvider;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.updates.util.BooleanReference;
import org.rdkit.knime.types.RDKitMolValue;
import org.rdkit.knime.util.RDKitAdapterCellSupport;
import org.rdkit.knime.util.RDKitMoleculeColumnAutoGuessProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;
import org.rdkit.knime.util.RDKitResultColumnNameAutoGuessProvider;

/**
 * Node parameters for RDKit Salt Stripper.
 *
 * @author Jannik Semperowitsch, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class RDKitSaltStripperNodeParameters implements NodeParameters {

    @Widget(title = "RDKit mol column",
        description = "The name of the column in first table containing RDKit molecules for stripping.")
    @ChoicesProvider(RDKitMoleculeColumnChoicesProvider.class)
    @ValueReference(InputColumnRef.class)
    @ValueProvider(InputColumnAutoGuesser.class)
    @Persist(configKey = RDKitSaltStripperNodeModel.CFG_INPUT_COLUMN)
    String m_inputColumnName;

    static final class InputColumnRef implements ParameterReference<String> {
    }

    static final class InputColumnAutoGuesser extends RDKitMoleculeColumnAutoGuessProvider {

        InputColumnAutoGuesser() {
            super(InputColumnRef.class, 0);
        }
    }

    @Widget(title = "New column name",
        description = "The new name of the column that will contained the salt stripped molecule.")
    @Persist(configKey = RDKitSaltStripperNodeModel.CFG_NEW_COLUMN_NAME)
    @ValueProvider(NewColumnNameProvider.class)
    @ValueReference(NewColumnNameRef.class)
    String m_newColumnName;
    
    static final class NewColumnNameRef implements ParameterReference<String> {
    }
    
    static final class NewColumnNameProvider extends RDKitResultColumnNameAutoGuessProvider {

		protected NewColumnNameProvider() {
			super("Salt Stripped Molecule", InputColumnRef.class, NewColumnNameRef.class);
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
        description = "Toggles removal of the input RDKit Mol column in the output table.")
    @Persist(configKey = RDKitSaltStripperNodeModel.CFG_REMOVE_SOURCE_COLUMNS)
    @ValueReference(RemoveSourceColumnRef.class)
    boolean m_removeSourceColumns;
    
    static final class RemoveSourceColumnRef implements BooleanReference {
	}

    @TextMessage(NoSaltTableMessage.class)
    Void m_noSaltTableMessage;

    static final class NoSaltTableMessage implements SimpleTextMessageProvider {
        @Override
        public boolean showMessage(final NodeParametersInput context) {
            return !hasSaltInputTable(context);
        }

        @Override
        public String title() {
            return "No salt table connected";
        }

        @Override
        public String description() {
            return "There is no salt table connected.\nUsing predefined salts.";
        }

        @Override
        public MessageType type() {
            return MessageType.WARNING;
        }
    }

    @Widget(title = "Salt definition column",
        description = "The name of the column in the optional second table containing RDKit molecules as salt "
            + "definitions (generated usually from SMARTS). This only applies, if a second table is connected.")
    @ChoicesProvider(RDKitMolColumnsPort1Provider.class)
    @ValueReference(SaltColumnRef.class)
    @ValueProvider(SaltColumnAutoGuesser.class)
    @Effect(predicate = HasSaltInputTable.class, type = EffectType.SHOW)
    @Persist(configKey = RDKitSaltStripperNodeModel.CFG_SALT_INPUT)
    String m_saltColumnName;

    static final class SaltColumnRef implements ParameterReference<String> {
    }

    static final class SaltColumnAutoGuesser extends RDKitMoleculeColumnAutoGuessProvider {

        SaltColumnAutoGuesser() {
            super(SaltColumnRef.class, 1, 0);
        }

        @Override
        protected Optional<DataColumnSpec> autoGuessColumn(final NodeParametersInput parametersInput) {
            return parametersInput.getInTableSpec(1)
                .flatMap(spec -> spec.stream()
                    .filter(RDKitMolColumnsPort1Provider::isRdkitMolCompatible)
                    .findFirst());
        }
    }

    static final class RDKitMolColumnsPort1Provider implements ColumnChoicesProvider {

        @Override
        public List<DataColumnSpec> columnChoices(final NodeParametersInput context) {
            return context.getInTableSpec(1)
                .map(spec -> spec.stream()
                    .filter(RDKitMolColumnsPort1Provider::isRdkitMolCompatible)
                    .toList())
                .orElse(List.of());
        }

        @SuppressWarnings("unchecked")
        static boolean isRdkitMolCompatible(final DataColumnSpec colSpec) {
            DataType colType = colSpec.getType();
            for (Class<? extends DataValue> clazz : RDKitAdapterCellSupport
                    .expandByAdaptableTypes(new Class[]{RDKitMolValue.class})) {
                if (colType.isCompatible(clazz) || colType.isAdaptable(clazz)) {
                    return true;
                }
            }
            return false;
        }
    }

    static final class HasSaltInputTable implements EffectPredicateProvider {
        @Override
        public EffectPredicate init(final PredicateInitializer i) {
            return i.getConstant(RDKitSaltStripperNodeParameters::hasSaltInputTable);
        }
    }

    @Widget(title = "Keep only largest fragment after salt stripping",
        description = "Option to reduce the salt stripping outcome even more keeping only the largest fragment of all "
            + "remaining fragments. If there are multiple fragments with the same number of atoms identified as "
            + "largest fragments, it will keep only the first one it encounters.")
    @Persist(configKey = RDKitSaltStripperNodeModel.CFG_KEEP_ONLY_LARGEST_FRAGMENT)
    boolean m_keepOnlyLargestFragment;

    private static boolean hasSaltInputTable(final NodeParametersInput input) {
        return RDKitSaltStripperNodeModel.hasSaltInputTable(new PortObjectSpec[] {
            input.getInTableSpec(0).orElse(null),
            input.getInTableSpec(1).orElse(null)
        });
    }
}
