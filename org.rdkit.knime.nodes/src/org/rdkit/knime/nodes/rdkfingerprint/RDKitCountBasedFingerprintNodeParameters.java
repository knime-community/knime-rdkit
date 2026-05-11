/** LICENSE PLACEHOLDER */

package org.rdkit.knime.nodes.rdkfingerprint;

import java.util.List;
import java.util.function.Supplier;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.collection.CollectionDataValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.node.parameters.Advanced;
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
import org.knime.node.parameters.widget.choices.EnumChoicesProvider;
import org.knime.node.parameters.widget.choices.NoneChoice;
import org.knime.node.parameters.widget.choices.StringOrEnum;
import org.knime.node.parameters.widget.choices.util.ColumnSelectionUtil;
import org.rdkit.knime.util.RDKitAdapterCellSupport;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsNonNegativeValidation;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsPositiveIntegerValidation;
import org.rdkit.knime.util.RDKitMoleculeColumnAutoGuessProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;
import org.rdkit.knime.util.RDKitResultColumnNameAutoGuessProvider;

/**
 * Node parameters for RDKit Count-Based Fingerprint.
 *
 * @author Jannik Semperowitsch, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class RDKitCountBasedFingerprintNodeParameters implements NodeParameters {

    @Widget(title = "RDKit mol column", description = "The column containing reactant molecules.")
    @ChoicesProvider(RDKitMoleculeColumnChoicesProvider.class)
    @ValueProvider(SmilesColumnAutoGuesser.class)
    @ValueReference(SmilesColumnRef.class)
    @Persist(configKey = AbstractRDKitFingerprintNodeModel.CFG_SMILES_COLUMN)
    String m_smilesColumn;

    static final class SmilesColumnRef implements ParameterReference<String> {
    }

    static final class RDKitMolColumnsProvider extends RDKitMoleculeColumnChoicesProvider {
        RDKitMolColumnsProvider() {
            super(0);
        }
    }

    static final class SmilesColumnAutoGuesser extends RDKitMoleculeColumnAutoGuessProvider {
        SmilesColumnAutoGuesser() {
            super(SmilesColumnRef.class, 0);
        }
    }

    @Widget(title = "Fingerprint type", description = "")
    @ChoicesProvider(CountBasedFingerprintTypesProvider.class)
    @ValueReference(FingerprintTypeRef.class)
    @Persist(configKey = AbstractRDKitFingerprintNodeModel.CFG_FP_TYPE)
    FingerprintType m_fpType = FingerprintType.morgan;

    static final class FingerprintTypeRef implements ParameterReference<FingerprintType> {
    }

    static final class CountBasedFingerprintTypesProvider implements EnumChoicesProvider<FingerprintType> {
        @Override
        public List<FingerprintType> choices(final NodeParametersInput context) {
            return List.of(RDKitCountBasedFingerprintNodeModel.getCountBasedFingerprintTypes());
        }
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
			super(SmilesColumnRef.class, NewColumnNameRef.class, "(Fingerprint)");
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
    @Advanced
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class, maxValidation = Max65535Validation.class)
    @Persist(configKey = AbstractRDKitFingerprintNodeModel.CFG_NUM_BITS)
    int m_numBits = 1024;
    
    static final class Max65535Validation extends NumberInputWidgetValidation.MaxValidation {
        @Override
        protected double getMax() {
            return 65535;
        }
    }

    @Widget(title = "Radius", description = "The radius of the atomic environments considered.")
    @Advanced
    @NumberInputWidget(minValidation = IsNonNegativeValidation.class, maxValidation = Max6Validation.class)
    @Effect(predicate = IsRadiusRelevant.class, type = EffectType.SHOW)
    @Persist(configKey = AbstractRDKitFingerprintNodeModel.CFG_RADIUS)
    int m_radius = 2;

    static final class IsRadiusRelevant implements EffectPredicateProvider {
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

    @Widget(title = "Path length", description = "Path length to be used (Torsion only).")
    @Advanced
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class, maxValidation = Max10Validation.class)
    @Effect(predicate = IsTorsion.class, type = EffectType.SHOW)
    @Persist(configKey = AbstractRDKitFingerprintNodeModel.CFG_TORSION_PATH_LENGTH)
    int m_torsionPathLength = 4;

    static final class IsTorsion implements EffectPredicateProvider {
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
        description = "Minimum length (in bonds) of the paths to be considered (AtomPair only).")
    @Advanced
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class, maxValidation = Max30Validation.class)
    @Effect(predicate = IsAtomPair.class, type = EffectType.SHOW)
    @Persist(configKey = AbstractRDKitFingerprintNodeModel.CFG_ATOMPAIRS_MIN_PATH)
    int m_atomPairMinPath = 1;
    
    static final class Max30Validation extends NumberInputWidgetValidation.MaxValidation {
        @Override
        protected double getMax() {
            return 30;
        }
    }

    @Widget(title = "Max path length",
        description = "Maximum length (in bonds) of the paths to be considered (AtomPair only).")
    @Advanced
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class, maxValidation = Max30Validation.class)
    @Effect(predicate = IsAtomPair.class, type = EffectType.SHOW)
    @Persist(configKey = AbstractRDKitFingerprintNodeModel.CFG_ATOMPAIRS_MAX_PATH)
    int m_atomPairMaxPath = 30;

    static final class IsAtomPair implements EffectPredicateProvider {
        @Override
        public EffectPredicate init(final PredicateInitializer i) {
            return i.getEnum(FingerprintTypeRef.class).isOneOf(FingerprintType.atompair);
        }
    }

    @Widget(title = "Use chirality",
        description = "Whether chirality should be taken into account when calculating the fingerprint.")
    @Advanced
    @Persist(configKey = AbstractRDKitFingerprintNodeModel.CFG_USE_CHIRALITY)
    boolean m_useChirality;

    @Widget(title = "Create rooted fingerprint",
        description = "Check this option to create a rooted fingerprint based on an atom list.")
    @Advanced
    @ValueReference(IsRootedRef.class)
    @Persist(configKey = AbstractRDKitFingerprintNodeModel.CFG_IS_ROOTED)
    boolean m_isRooted;

    static final class IsRootedRef implements BooleanReference {
    }

    @Widget(title = "Atom list column for rooted fingerprints",
        description = "The column containing the atom lists that are necessary when calculating rooted fingerprints. "
            + "Instead of a list it is also possible to provide an integer column with a single atom index.")
    @ChoicesProvider(AtomListColumnsProvider.class)
    @Advanced
    @Effect(predicate = IsRootedRef.class, type = EffectType.SHOW)
    @Persistor(AtomListColumnPersistor.class)
    StringOrEnum<NoneChoice> m_atomListColumn = new StringOrEnum<>(NoneChoice.NONE);

    @SuppressWarnings("unchecked")
    static final class AtomListColumnsProvider implements ColumnChoicesProvider {

        private static final Class<? extends DataValue>[] COMPATIBLE_TYPES =
            RDKitAdapterCellSupport.expandByAdaptableTypes(
                new Class[]{CollectionDataValue.class, IntValue.class});

        @Override
        public List<DataColumnSpec> columnChoices(final NodeParametersInput context) {
            return ColumnSelectionUtil.getCompatibleColumns(context, 0, COMPATIBLE_TYPES);
        }
    }
    
    static final class AtomListColumnPersistor implements NodeParametersPersistor<StringOrEnum<NoneChoice>> {
    	
		@Override
		public StringOrEnum<NoneChoice> load(NodeSettingsRO settings) throws InvalidSettingsException {
			final String value = settings.getString(AbstractRDKitFingerprintNodeModel.CFG_ATOM_LIST_COLUMN, null);
			if (value == null) {
				return new StringOrEnum<>(NoneChoice.NONE);
			} else {
				return new StringOrEnum<>(value);
			}
		}

		@Override
		public void save(StringOrEnum<NoneChoice> param, NodeSettingsWO settings) {
			final var enumChoice = param.getEnumChoice();
			settings.addString(AbstractRDKitFingerprintNodeModel.CFG_ATOM_LIST_COLUMN, 
					enumChoice.isEmpty() ? param.getStringChoice() : null);
		}

		@Override
		public String[][] getConfigPaths() {
			return new String[][] {{AbstractRDKitFingerprintNodeModel.CFG_ATOM_LIST_COLUMN}};
		}
	}

    @Widget(title = "Include atoms",
        description = "Check this option to include the atoms when calculating rooted fingerprints "
            + "or uncheck to exclude them.")
    @Advanced
    @Effect(predicate = IsRootedRef.class, type = EffectType.SHOW)
    @Persist(configKey = AbstractRDKitFingerprintNodeModel.CFG_INCLUDE_ATOMS)
    boolean m_includeAtoms = true;
}
