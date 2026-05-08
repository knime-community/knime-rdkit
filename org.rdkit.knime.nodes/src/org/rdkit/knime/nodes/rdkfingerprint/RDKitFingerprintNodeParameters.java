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
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE plugin APIs.
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

package org.rdkit.knime.nodes.rdkfingerprint;

import java.util.List;
import java.util.function.Supplier;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.IntValue;
import org.knime.core.data.collection.CollectionDataValue;
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
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.Effect.EffectType;
import org.knime.node.parameters.updates.EffectPredicate;
import org.knime.node.parameters.updates.EffectPredicateProvider;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.updates.util.BooleanReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.ColumnChoicesProvider;
import org.knime.node.parameters.widget.choices.NoneChoice;
import org.knime.node.parameters.widget.choices.StringOrEnum;
import org.knime.node.parameters.widget.choices.util.ColumnSelectionUtil;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsNonNegativeValidation;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsPositiveIntegerValidation;
import org.rdkit.knime.util.RDKitAdapterCellSupport;
import org.rdkit.knime.util.RDKitMoleculeColumnAutoGuessProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;
import org.rdkit.knime.util.RDKitResultColumnNameAutoGuessProvider;

/**
 * Node parameters for RDKit Fingerprint.
 *
 * @author Jannik Semperowitsch, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class RDKitFingerprintNodeParameters implements NodeParameters {

    @Widget(title = "RDKit mol column", description = "The column containing reactant molecules.")
    @ChoicesProvider(RDKitMoleculeColumnChoicesProvider.class)
    @ValueProvider(MolColumnAutoGuessProvider.class)
    @ValueReference(MolColumnRef.class)
    @Persist(configKey = AbstractRDKitFingerprintNodeModel.CFG_SMILES_COLUMN)
    String m_molColumn;

    interface MolColumnRef extends ParameterReference<String> {
    }

    static final class MolColumnAutoGuessProvider extends RDKitMoleculeColumnAutoGuessProvider {
        MolColumnAutoGuessProvider() {
            super(MolColumnRef.class, 0);
        }
    }

    @Widget(title = "Fingerprint type",
        description = "More details about different RDKit fingerprint implementations can be found here: "
            + "https://www.rdkit.org/docs/RDKit_Book.html#additional-information-about-the-fingerprints.")
    @ValueReference(FingerprintTypeRef.class)
    @Persist(configKey = AbstractRDKitFingerprintNodeModel.CFG_FP_TYPE)
    FingerprintType m_fingerprintType = FingerprintType.morgan;

    static final class FingerprintTypeRef implements ParameterReference<FingerprintType> {
    }

    @Widget(title = "New fingerprint column name",
        description = "Name of the fingerprint column in the output table.")
    @Persist(configKey = AbstractRDKitFingerprintNodeModel.CFG_NEW_COLUMN_NAME)
    @ValueProvider(NewColumnNameProvider.class)
    @ValueReference(NewColumnNameRef.class)
    String m_newColumnName;
    
    static final class NewColumnNameRef implements ParameterReference<String> {
    }
    
    static final class NewColumnNameProvider extends RDKitResultColumnNameAutoGuessProvider {

		protected NewColumnNameProvider() {
			super(MolColumnRef.class, NewColumnNameRef.class, "(Fingerprint)");
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
    @Persist(configKey = AbstractRDKitFingerprintNodeModel.CFG_REMOVE_SOURCE_COLUMNS)
    @ValueReference(RemoveSourceColumnRef.class)
    boolean m_removeSourceColumns;
    
    static final class RemoveSourceColumnRef implements BooleanReference {
	}

    @Widget(title = "Num bits", description = "Number of bits in the fingerprint.")
    @Effect(predicate = NumBitsVisible.class, type = EffectType.SHOW)
    @Persist(configKey = AbstractRDKitFingerprintNodeModel.CFG_NUM_BITS)
    @NumberInputWidget(minValidation = Min32Validation.class)
    int m_numBits = AbstractRDKitFingerprintNodeDialog.DEFAULT_NUM_BITS;

    private static final class NumBitsVisible implements EffectPredicateProvider {
        @Override
        public EffectPredicate init(final PredicateInitializer i) {
            return i.getEnum(FingerprintTypeRef.class).isOneOf(
                FingerprintType.morgan, FingerprintType.featmorgan, FingerprintType.atompair,
                FingerprintType.torsion, FingerprintType.rdkit, FingerprintType.avalon,
                FingerprintType.layered, FingerprintType.pattern);
        }
    }
    
    static final class Min32Validation extends NumberInputWidgetValidation.MinValidation {
        @Override
        protected double getMin() {
            return 32;
        }
    }

    @Widget(title = "Radius", description = "The radius of the atomic environments considered.")
    @Effect(predicate = RadiusVisible.class, type = EffectType.SHOW)
    @Persist(configKey = AbstractRDKitFingerprintNodeModel.CFG_RADIUS)
    @NumberInputWidget(minValidation = IsNonNegativeValidation.class, maxValidation = Max6Validation.class)
    int m_radius = AbstractRDKitFingerprintNodeDialog.DEFAULT_RADIUS;

    private static final class RadiusVisible implements EffectPredicateProvider {
        @Override
        public EffectPredicate init(final PredicateInitializer i) {
            return i.getEnum(FingerprintTypeRef.class).isOneOf(FingerprintType.morgan, FingerprintType.featmorgan);
        }
    }
    
    static final class Max6Validation extends NumberInputWidgetValidation.MaxValidation {
        @Override
        protected double getMax() {
            return 6;
        }
    }

    @Widget(title = "Layer flags",
        description = "Which atomic layers should be included. This value is the total of the following layer "
            + "flag values: 0x01 pure topology, 0x02 bond order, 0x04 atom types, 0x08 presence of rings, "
            + "0x10 ring sizes, 0x20 aromaticity. The default value of 7 (0x01 pure topology + 0x02 bond order "
            + "+ 0x04 atom types) allows classic substructure searches.")
    @Effect(predicate = LayerFlagsVisible.class, type = EffectType.SHOW)
    @Persist(configKey = AbstractRDKitFingerprintNodeModel.CFG_LAYER_FLAGS)
    @NumberInputWidget(minValidation = IsNonNegativeValidation.class, maxValidation = Max65535Validation.class)
    int m_layerFlags = AbstractRDKitFingerprintNodeDialog.DEFAULT_LAYER_FLAGS;

    private static final class LayerFlagsVisible implements EffectPredicateProvider {
        @Override
        public EffectPredicate init(final PredicateInitializer i) {
            return i.getEnum(FingerprintTypeRef.class).isOneOf(FingerprintType.layered);
        }
    }
    
    static final class Max65535Validation extends NumberInputWidgetValidation.MaxValidation {
        @Override
        protected double getMax() {
            return 65535;
        }
    }

    @Widget(title = "Path length", description = "Path length to be used (Torsion only).")
    @Effect(predicate = TorsionPathLengthVisible.class, type = EffectType.SHOW)
    @Persist(configKey = AbstractRDKitFingerprintNodeModel.CFG_TORSION_PATH_LENGTH)
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class, maxValidation = Max10Validation.class)
    int m_torsionPathLength = AbstractRDKitFingerprintNodeDialog.DEFAULT_TORSION_PATH_LENGTH;

    private static final class TorsionPathLengthVisible implements EffectPredicateProvider {
        @Override
        public EffectPredicate init(final PredicateInitializer i) {
            return i.getEnum(FingerprintTypeRef.class).isOneOf(FingerprintType.torsion);
        }
    }
    
    static final class Max10Validation extends NumberInputWidgetValidation.MaxValidation {
        @Override
        protected double getMax() {
            return 10;
        }
    }

    @Widget(title = "Min path length",
        description = "Minimum length (in bonds) of the paths to be considered.")
    @Effect(predicate = PathLengthVisible.class, type = EffectType.SHOW)
    @Persist(configKey = AbstractRDKitFingerprintNodeModel.CFG_MIN_PATH)
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class, maxValidation = Max10Validation.class)
    int m_minPath = AbstractRDKitFingerprintNodeDialog.DEFAULT_MIN_PATH;

    @Widget(title = "Max path length",
        description = "Maximum length (in bonds) of the paths to be considered.")
    @Effect(predicate = PathLengthVisible.class, type = EffectType.SHOW)
    @Persist(configKey = AbstractRDKitFingerprintNodeModel.CFG_MAX_PATH)
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class, maxValidation = Max10Validation.class)
    int m_maxPath = AbstractRDKitFingerprintNodeDialog.DEFAULT_MAX_PATH;

    private static final class PathLengthVisible implements EffectPredicateProvider {
        @Override
        public EffectPredicate init(final PredicateInitializer i) {
            return i.getEnum(FingerprintTypeRef.class).isOneOf(FingerprintType.rdkit, FingerprintType.layered);
        }
    }

    @Widget(title = "AtomPair min path length",
        description = "Minimum length (in bonds) of the atom pair paths to be considered.")
    @Effect(predicate = AtomPairPathVisible.class, type = EffectType.SHOW)
    @Persist(configKey = AbstractRDKitFingerprintNodeModel.CFG_ATOMPAIRS_MIN_PATH)
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class, maxValidation = Max30Validation.class)
    int m_atomPairMinPath = AbstractRDKitFingerprintNodeDialog.DEFAULT_ATOMPAIR_MIN_PATH;

    @Widget(title = "AtomPair max path length",
        description = "Maximum length (in bonds) of the atom pair paths to be considered.")
    @Effect(predicate = AtomPairPathVisible.class, type = EffectType.SHOW)
    @Persist(configKey = AbstractRDKitFingerprintNodeModel.CFG_ATOMPAIRS_MAX_PATH)
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class, maxValidation = Max30Validation.class)
    int m_atomPairMaxPath = AbstractRDKitFingerprintNodeDialog.DEFAULT_ATOMPAIR_MAX_PATH;

    private static final class AtomPairPathVisible implements EffectPredicateProvider {
        @Override
        public EffectPredicate init(final PredicateInitializer i) {
            return i.getEnum(FingerprintTypeRef.class).isOneOf(FingerprintType.atompair);
        }
    }
    
    static final class Max30Validation extends NumberInputWidgetValidation.MaxValidation {
        @Override
        protected double getMax() {
            return 30;
        }
    }

    @Widget(title = "Use chirality",
        description = "If checked, chirality information is taken into account when generating the fingerprint.")
    @Persist(configKey = AbstractRDKitFingerprintNodeModel.CFG_USE_CHIRALITY)
    boolean m_useChirality;

    @Widget(title = "Create rooted fingerprint",
        description = "Check this option to create a rooted fingerprint based on an atom list. "
            + "This option is not applicable to all fingerprint types.")
    @ValueReference(RootedOptionRef.class)
    @Persist(configKey = AbstractRDKitFingerprintNodeModel.CFG_IS_ROOTED)
    @Effect(predicate = AvalonOrMACCSOrPatternVisible.class, type = EffectType.HIDE)
    boolean m_rootedOption;

    static final class RootedOptionRef implements BooleanReference {
    }
    
    private static final class AvalonOrMACCSOrPatternVisible implements EffectPredicateProvider {
        @Override
        public EffectPredicate init(final PredicateInitializer i) {
            return i.getEnum(FingerprintTypeRef.class).isOneOf(
            		FingerprintType.avalon, FingerprintType.maccs, FingerprintType.pattern);
        }
    }

    @Widget(title = "Atom list column for rooted fingerprints",
        description = "The column containing the atom lists that are necessary when calculating rooted "
            + "fingerprints. Instead of a list it is also possible to provide an integer column with a "
            + "single atom index.")
    @ChoicesProvider(AtomListColumnsProvider.class)
    @Effect(predicate = RootedAndCanRooted.class, type = EffectType.SHOW)
    @Persistor(AtomListColumnPersistor.class)
    StringOrEnum<NoneChoice> m_atomListColumn = new StringOrEnum<>(NoneChoice.NONE);

    @SuppressWarnings("unchecked")
    private static final Class<? extends org.knime.core.data.DataValue>[] ATOM_LIST_COMPATIBLE_TYPES =
        RDKitAdapterCellSupport.expandByAdaptableTypes(
            new Class[]{CollectionDataValue.class, IntValue.class});

    static final class AtomListColumnsProvider implements ColumnChoicesProvider {
        @Override
        public List<DataColumnSpec> columnChoices(final NodeParametersInput context) {
            return ColumnSelectionUtil.getCompatibleColumnsOfFirstPort(context, ATOM_LIST_COMPATIBLE_TYPES);
        }
    }

    static final class AtomListColumnPersistor implements NodeParametersPersistor<StringOrEnum<NoneChoice>> {
        @Override
        public StringOrEnum<NoneChoice> load(final NodeSettingsRO settings) throws InvalidSettingsException {
            final String value = settings.getString(AbstractRDKitFingerprintNodeModel.CFG_ATOM_LIST_COLUMN, null);
            return value == null ? new StringOrEnum<>(NoneChoice.NONE) : new StringOrEnum<>(value);
        }

        @Override
        public void save(final StringOrEnum<NoneChoice> value, final NodeSettingsWO settings) {
            final boolean isNone = value.getEnumChoice().isPresent();
            settings.addString(AbstractRDKitFingerprintNodeModel.CFG_ATOM_LIST_COLUMN, isNone ? 
            		null : value.getStringChoice());
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{AbstractRDKitFingerprintNodeModel.CFG_ATOM_LIST_COLUMN}};
        }
    }

    private static final class RootedAndCanRooted implements EffectPredicateProvider {
        @Override
        public EffectPredicate init(final PredicateInitializer i) {
            return i.getEnum(FingerprintTypeRef.class).isOneOf(
                    FingerprintType.morgan, FingerprintType.featmorgan, FingerprintType.atompair,
                    FingerprintType.torsion, FingerprintType.rdkit, FingerprintType.layered)
                .and(i.getPredicate(RootedOptionRef.class));
        }
    }

    @Widget(title = "Include atoms",
        description = "Check this option to include the atoms when calculating rooted fingerprints "
            + "or uncheck to exclude them.")
    @Effect(predicate = RootedAndCanRooted.class, type = EffectType.SHOW)
    @Persist(configKey = AbstractRDKitFingerprintNodeModel.CFG_INCLUDE_ATOMS)
    boolean m_includeAtoms = AbstractRDKitFingerprintNodeDialog.DEFAULT_ATOM_LIST_INCLUDE_HANDLING;

}
