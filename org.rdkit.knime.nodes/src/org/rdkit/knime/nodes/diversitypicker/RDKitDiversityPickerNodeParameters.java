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

package org.rdkit.knime.nodes.diversitypicker;

import org.knime.core.data.vector.bitvector.BitVectorValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.persistence.NodeParametersPersistor;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.NoneChoice;
import org.knime.node.parameters.widget.choices.StringOrEnum;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsPositiveIntegerValidation;
import org.rdkit.knime.util.RDKitLegacyPersistors.LegacyMoleculeColumnPersistor;
import org.rdkit.knime.util.RDKitMoleculeColumnAutoGuessProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;

/**
 * Node parameters for RDKit Diversity Picker.
 *
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class RDKitDiversityPickerNodeParameters implements NodeParameters {

    @Widget(title = "Molecule or fingerprint column (table 1)", description = """
    		The column containing the molecules or fingerprints to pick from. If molecules are selected their 
    		fingerprints will be calculated automatically with Morgan, Radius 2, 2048 bit length.
    		""")
    @Persistor(InputColumnNamePersistor.class)
    @ChoicesProvider(MolOrFpColumnPort0Provider.class)
    @ValueProvider(InputColumnNameAutoGuessProvider.class)
    @ValueReference(InputColumnRef.class)
    String m_inputColumnName;

    static final class InputColumnRef implements ParameterReference<String> {
    }
    
    @Widget(title = "Molecule or fingerprint column to bias away from (table 2)", description = """
    		The column containing molecules or fingerprints to bias away from. This option has the effect of seeding 
    		the diversity pick: Molecules selected will be diverse with respect to these biasing molecules as well as 
    		each other. If molecules are provided as input their fingerprints will be calculated automatically based on 
    		input of table 1. If table 1 has fingerprints with unknown settings this calculation will fail. In this 
    		case please regenerate fingerprints in table 1 with the RDKit Fingerprint Node or select a compatible 
    		fingerprint column in table 2 instead of a molecule column.
    		""")
    @Persistor(AdditionalInputColumnPersistor.class)
    @ChoicesProvider(MolOrFpColumnPort1Provider.class)
    StringOrEnum<NoneChoice> m_additionalInputColumnName = new StringOrEnum<>(NoneChoice.NONE);
    
    @Widget(title = "Number to pick",
        description = "Number of diverse rows to pick.")
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class)
    @Persist(configKey = "num_picks")
    int m_numPicks = 10;

    @Widget(title = "Random seed",
        description = "Random number seed to use.")
    @Persist(configKey = "random_seed")
    int m_randomSeed = -1;

    static final class InputColumnNameAutoGuessProvider extends RDKitMoleculeColumnAutoGuessProvider {

		protected InputColumnNameAutoGuessProvider() {
			super(InputColumnRef.class, 0, 1, BitVectorValue.class);
		}
    	
    }
    
    static final class MolOrFpColumnPort0Provider extends RDKitMoleculeColumnChoicesProvider {

		public MolOrFpColumnPort0Provider() {
			super(0, BitVectorValue.class);
		}
    	
    }

    static final class MolOrFpColumnPort1Provider extends RDKitMoleculeColumnChoicesProvider {

		public MolOrFpColumnPort1Provider() {
			super(1, BitVectorValue.class);
		}
    	
    }

    static final class InputColumnNamePersistor extends LegacyMoleculeColumnPersistor {

		public InputColumnNamePersistor() {
			super("input_column", "first_column");
		}
    	
    }

    static final class AdditionalInputColumnPersistor implements NodeParametersPersistor<StringOrEnum<NoneChoice>> {

        private static final String CONFIG_KEY = "additional_input_column";

        @Override
        public StringOrEnum<NoneChoice> load(final NodeSettingsRO settings) throws InvalidSettingsException {
            String columnName = settings.getString(CONFIG_KEY);
            if (columnName == null || columnName.isEmpty()) {
                return new StringOrEnum<>(NoneChoice.NONE);
            }
            return new StringOrEnum<>(columnName);
        }

		@Override
		public void save(final StringOrEnum<NoneChoice> value, final NodeSettingsWO settings) {
			settings.addString(CONFIG_KEY, value.getEnumChoice().isPresent() ? null : value.getStringChoice());
		}

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{CONFIG_KEY}};
        }
        
    }
    
}
