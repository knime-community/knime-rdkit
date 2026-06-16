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
package org.rdkit.knime.nodes.molextractor;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.node.parameters.Advanced;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.NodeParametersPersistor;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.Effect.EffectType;
import org.knime.node.parameters.updates.EffectPredicate;
import org.knime.node.parameters.updates.EffectPredicateProvider;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.Label;
import org.knime.node.parameters.widget.choices.StringOrEnum;
import org.knime.node.parameters.widget.choices.util.AllColumnsProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnAutoGuessProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;

/**
 * Node parameters for RDKit Molecule Extractor.
 *
 * @author Jannik Semperowitsch, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class RDKitMoleculeExtractorNodeParameters implements NodeParameters {

    static final class TableIsConnected implements EffectPredicateProvider {
        @Override
        public EffectPredicate init(final PredicateInitializer i) {
            return i.getConstant(input -> input.getInTableSpec(0).isPresent());
        }
    }

    static final class ReferenceIsNone implements EffectPredicateProvider {
        @Override
        public EffectPredicate init(final PredicateInitializer i) {
            return i.getStringOrEnum(ReferenceColumnRef.class).isEnumChoice(ReferenceColumnOption.NONE);
        }
    }

    @Widget(title = "Molecules",
        description = "A textual representation of the molecules to be split. Usually, you will attach a flow "
            + "variable to control this setting, e.g. coming from a Molecule Sketcher. This setting will only "
            + "be used, if no table is connected.")
    @Effect(predicate = TableIsConnected.class, type = EffectType.HIDE)
    @Persist(configKey = RDKitMoleculeExtractorNodeModel.CFG_INPUT_MOLECULES)
    String m_inputMolecules;

    @Widget(title = "Format",
        description = "The format of the molecules: MOL, SDF or SMILES are supported. This setting will only be "
            + "used, if no table is connected.")
    @Effect(predicate = TableIsConnected.class, type = EffectType.HIDE)
    @Persist(configKey = RDKitMoleculeExtractorNodeModel.CFG_INPUT_MOLECULES_FORMAT)
    String m_inputMoleculesFormat;

    @Widget(title = "RDKit mol column",
        description = "The input column with RDKit Molecules, which may contain multiple disconnected fragments "
            + "to be extracted.")
    @ChoicesProvider(RDKitMoleculeColumnChoicesProvider.class)
    @ValueProvider(InputColumnAutoGuesser.class)
    @ValueReference(InputColumnRef.class)
    @Effect(predicate = TableIsConnected.class, type = EffectType.SHOW)
    @Persist(configKey = RDKitMoleculeExtractorNodeModel.CFG_INPUT_COLUMN)
    String m_inputColumn;

    static final class InputColumnRef implements ParameterReference<String> {
    }

    static final class InputColumnAutoGuesser extends RDKitMoleculeColumnAutoGuessProvider {
        InputColumnAutoGuesser() {
            super(InputColumnRef.class, 0);
        }
    }

    @Widget(title = "Reference column (e.g. an ID)",
        description = "The column to be used as reference column. Its values are assigned to the cells with the "
            + "extracted molecules. You may use the Row ID here or set it to None, in which case the reference "
            + "column will not be added.")
    @ChoicesProvider(AllColumnsProvider.class)
    @ValueReference(ReferenceColumnRef.class)
    @Effect(predicate = TableIsConnected.class, type = EffectType.SHOW)
    @Persistor(ReferenceColumnPersistor.class)
    StringOrEnum<ReferenceColumnOption> m_referenceInputColumn = new StringOrEnum<>(ReferenceColumnOption.ROW_ID);

    enum ReferenceColumnOption {
            ROW_ID,
            NONE
    }

    static final class ReferenceColumnRef implements ParameterReference<StringOrEnum<ReferenceColumnOption>> {
    }

    static final class ReferenceColumnPersistor
            implements NodeParametersPersistor<StringOrEnum<ReferenceColumnOption>> {

        private static final String CFG_COLUMN_NAME = "columnName";

        private static final String CFG_ROWID = "useRowID";

        @Override
        public StringOrEnum<ReferenceColumnOption> load(final NodeSettingsRO settings)
                throws InvalidSettingsException {
            final var sub = settings.getNodeSettings(RDKitMoleculeExtractorNodeModel.CFG_INPUT_REF_COLUMN);
            final boolean useRowId = sub.getBoolean(CFG_ROWID);
            if (useRowId) {
                return new StringOrEnum<>(ReferenceColumnOption.ROW_ID);
            }
            final var colName = sub.getString(CFG_COLUMN_NAME);
            if (colName == null) {
                return new StringOrEnum<>(ReferenceColumnOption.NONE);
            }
            return new StringOrEnum<>(colName);
        }

        @Override
        public void save(final StringOrEnum<ReferenceColumnOption> value, final NodeSettingsWO settings) {
            final var sub = settings.addNodeSettings(RDKitMoleculeExtractorNodeModel.CFG_INPUT_REF_COLUMN);
            final var enumChoice = value.getEnumChoice();
            if (enumChoice.filter(e -> e == ReferenceColumnOption.ROW_ID).isPresent()) {
                sub.addBoolean(CFG_ROWID, true);
                sub.addString(CFG_COLUMN_NAME, null);
            } else if (enumChoice.filter(e -> e == ReferenceColumnOption.NONE).isPresent()) {
                sub.addBoolean(CFG_ROWID, false);
                sub.addString(CFG_COLUMN_NAME, null);
            } else {
                sub.addBoolean(CFG_ROWID, false);
                sub.addString(CFG_COLUMN_NAME, value.getStringChoice());
            }
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{
                {RDKitMoleculeExtractorNodeModel.CFG_INPUT_REF_COLUMN, CFG_ROWID},
                {RDKitMoleculeExtractorNodeModel.CFG_INPUT_REF_COLUMN, CFG_COLUMN_NAME}
            };
        }
    }

    @Widget(title = "Column name for extracted molecules",
        description = "The name to be used for the new column used to store extracted molecules.")
    @Persist(configKey = RDKitMoleculeExtractorNodeModel.CFG_OUTPUT_MOL_NAME)
    String m_moleculeOutputColumnName;

    @Widget(title = "Column name for copied reference data",
        description = "The name to be used for the new reference column used to store the reference values.")
    @Effect(predicate = TableIsConnected.class, type = EffectType.SHOW)
    @Effect(predicate = ReferenceIsNone.class, type = EffectType.DISABLE)
    @Persist(configKey = RDKitMoleculeExtractorNodeModel.CFG_OUTPUT_REF_NAME)
    String m_referenceOutputColumnName;

    @Advanced
    @Widget(title = "Sanitize fragments",
        description = "Flag to determine, if fragments shall be sanitized when being extracted. Selecting this "
            + "option may lead to additional fragmentation errors. Default is false.")
    @Persist(configKey = RDKitMoleculeExtractorNodeModel.CFG_SANITIZE_FRAGMENTS)
    boolean m_sanitizeFragments;

    @Advanced
    @Widget(title = "How to react on conversion errors",
        description = "Define here, how the node shall behave, if an input molecule to be processed could not be "
            + "converted into an RDKit molecule. Usually, this is the case if the molecule is invalid. By "
            + "default, the node will generate a missing cell and no warning. If conversion requires special "
            + "treatment, you may use the RDKit From Molecule node to perform the conversion before executing "
            + "this node. It offers different options for conversion.")
    @Persist(configKey = RDKitMoleculeExtractorNodeModel.CFG_ERROR_HANDLING)
    ErrorHandling m_errorHandling = ErrorHandling.MissingCellWithWarning;

    @Advanced
    @Widget(title = "How to react on empty (missing) cells",
        description = "Define here, how the node shall behave, if an empty (missing) cell is used as input. By "
            + "default, the node will generate a missing cell and no warning.")
    @Persist(configKey = RDKitMoleculeExtractorNodeModel.CFG_EMPTY_CELL_HANDLING)
    EmptyCellHandling m_emptyCellHandling = EmptyCellHandling.MissingCellWithoutWarning;

    @Advanced
    @Widget(title = "How to react on empty (zero atom) molecules",
        description = "Define here, how the node shall behave, if an empty molecule is used as input. This means "
            + "that the molecule has zero atoms. By default, the node will generate a missing cell and no warning.")
    @Persist(configKey = RDKitMoleculeExtractorNodeModel.CFG_EMPTY_MOLECULE_HANDLING)
    EmptyMoleculeHandling m_emptyMoleculeHandling = EmptyMoleculeHandling.SkipWithoutWarning;
}
