/*
 * ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright (C)2012-2023
 * Novartis Pharma AG, Switzerland
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
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
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
 * ---------------------------------------------------------------------
 */
package org.rdkit.knime.nodes.rdkit2inchi;

import java.util.function.Supplier;

import org.knime.node.parameters.Advanced;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.layout.After;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.Effect.EffectType;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.updates.util.BooleanReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.text.TextAreaWidget;
import org.rdkit.knime.util.RDKitMoleculeColumnAutoGuessProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;
import org.rdkit.knime.util.RDKitResultColumnNameAutoGuessProvider;

/**
 * Node parameters for the "RDKit To InChI" node.
 *
 * @author Jannik Semperowitsch, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class RDKitMolecule2InChINodeParameters implements NodeParameters {

    @Section(title = "Extra InChI Generation Information")
    interface ExtraInfoSection {
    }
    
    @Section(title = "Advanced")
    @Advanced
    @After(ExtraInfoSection.class)
    interface AdvancedSection {
    }

    @Widget(title = "RDKit mol column", description = "The input column with RDKit Molecules.")
    @ChoicesProvider(RDKitMoleculeColumnChoicesProvider.class)
    @ValueProvider(InputColumnAutoGuesser.class)
    @ValueReference(InputColumnRef.class)
    @Persist(configKey = RDKitMolecule2InChINodeModel.CFG_INPUT_COLUMN)
    String m_inputColumnName;

    static final class InputColumnRef implements ParameterReference<String> {
    }

    static final class InputColumnAutoGuesser extends RDKitMoleculeColumnAutoGuessProvider {
        InputColumnAutoGuesser() {
            super(InputColumnRef.class, 0);
        }
    }

    @Widget(title = "Remove source column",
        description = "Enable to remove the specified source column from the result table.")
    @Persist(configKey = RDKitMolecule2InChINodeModel.CFG_REMOVE_SOURCE_COLUMNS)
    @ValueReference(RemoveSourceColumnRef.class)
    boolean m_removeSourceColumns;
    
    static final class RemoveSourceColumnRef implements BooleanReference {
	}

    @Widget(title = "New column name for InChI codes",
        description = "The name of the new column, which will contain the InChi code.")
    @Persist(configKey = RDKitMolecule2InChINodeModel.CFG_NEW_INCHI_CODE_COLUMN_NAME)
    @ValueProvider(NewInChICodeColumnNameProvider.class)
    @ValueReference(NewInChICodeColumnNameRef.class)
    String m_newInChICodeColumnName;
    
    static final class NewInChICodeColumnNameRef implements ParameterReference<String> {
    }
    
    static final class NewInChICodeColumnNameProvider extends RDKitResultColumnNameAutoGuessProvider {

		protected NewInChICodeColumnNameProvider() {
			super(InputColumnRef.class, NewInChICodeColumnNameRef.class, "(InChI Code)");
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

    @Widget(title = "Generate also InChI keys",
        description = "Enable to also generated InChI Keys.")
    @ValueReference(GenerateInChIKeysRef.class)
    @Persist(configKey = RDKitMolecule2InChINodeModel.CFG_GENERATE_INCHI_KEYS)
    boolean m_generateInChIKeysOption;

    static final class GenerateInChIKeysRef implements BooleanReference {
    }

    @Widget(title = "New column name for InChI keys",
        description = "The name of the new column, which will contain the InChi keys.")
    @Effect(predicate = GenerateInChIKeysRef.class, type = EffectType.ENABLE)
    @Persist(configKey = RDKitMolecule2InChINodeModel.CFG_NEW_INCHI_KEY_COLUMN_NAME)
    @ValueProvider(NewInChIKeyColumnNameProvider.class)
    @ValueReference(NewInChIKeyColumnNameRef.class)
    String m_newInChIKeyColumnName;
    
    static final class NewInChIKeyColumnNameRef implements ParameterReference<String> {
    }
    
    static final class NewInChIKeyColumnNameProvider extends RDKitResultColumnNameAutoGuessProvider {

		protected NewInChIKeyColumnNameProvider() {
			super(InputColumnRef.class, NewInChIKeyColumnNameRef.class, "(InChI Key)");
		}
		
		private Supplier<Boolean> m_removeSourceColumn;
		
		private Supplier<String> m_newInChICodeColumnName;
		
		@Override
		public void init(StateProviderInitializer initializer) {
			super.init(initializer);
			m_removeSourceColumn = initializer.getValueSupplier(RemoveSourceColumnRef.class);
			m_newInChICodeColumnName = initializer.getValueSupplier(NewInChICodeColumnNameRef.class);
		}

		@Override
		protected String[] getExcludedColumnNames(NodeParametersInput parametersInput, 
			final String currentInputColumnName) {
			return (m_removeSourceColumn.get() ? new String[] { currentInputColumnName } : null);
		}

		@Override
		protected String[] getAdditionalColumnNames(NodeParametersInput parametersInput,
				String currentInputColumnName) {
			return new String[] { m_newInChICodeColumnName.get() };
		}
    	
    }

    @Widget(title = "New column name prefix for extra information",
        description = "The prefix of column names, which will contain the extra information about InChI code generation.")
    @Layout(ExtraInfoSection.class)
    @Persist(configKey = RDKitMolecule2InChINodeModel.CFG_NEW_EXTRA_INFO_COLUMN_NAME_PREFIX)
    String m_extraInformationColumnNamePrefix;

    @Widget(title = "Return code column",
        description = "Enable to also generate a column that contains the return code of the InChI generation routine.")
    @Layout(ExtraInfoSection.class)
    @Persist(configKey = RDKitMolecule2InChINodeModel.CFG_GENERATE_RETURN_CODE)
    boolean m_extraReturnCodeOption;

    @Widget(title = "Aux info column",
        description = "Enable to also generate a column that contains aux information about the InChI code.")
    @Layout(ExtraInfoSection.class)
    @Persist(configKey = RDKitMolecule2InChINodeModel.CFG_GENERATE_AUX_INFO)
    boolean m_extraAuxInfoOption;

    @Widget(title = "Message column",
        description = "Enable to also generate a column that contains a message generated during the InChI generation routine.")
    @Layout(ExtraInfoSection.class)
    @Persist(configKey = RDKitMolecule2InChINodeModel.CFG_GENERATE_MESSAGE)
    boolean m_extraMessageOption;

    @Widget(title = "Log column",
        description = "Enable to also generate a column that contains a log message of the InChI generation routine.")
    @Layout(ExtraInfoSection.class)
    @Persist(configKey = RDKitMolecule2InChINodeModel.CFG_GENERATE_LOG)
    boolean m_extraLogOption;

    @Widget(title = "InChI code generation switches",
        description = "Specify here space-separated switches, which need to start with / or - to influence the InChI "
            + "code generation. Some of them make the resulting InChI code Non-standard. A good overview about "
            + "possible switches can be found here: "
            + "<a href=\"https://www.inchi-trust.org/technical-faq-2/#15.14\">"
            + "https://www.inchi-trust.org/technical-faq-2/#15.14</a>.")
    @Advanced
    @Layout(AdvancedSection.class)
    @Persist(configKey = RDKitMolecule2InChINodeModel.CFG_ADVANCED_OPTIONS)
    @TextAreaWidget(rows = 5)
    String m_advancedOptions = "";
}
