/** LICENSE PLACEHOLDER */

package org.rdkit.knime.nodes.molecule2rdkit;

import java.util.function.Supplier;

import org.knime.chem.types.SdfValue;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataType;
import org.knime.node.parameters.Advanced;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.experimental.persistence.booleanhelpers.DoNotPersistBoolean;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.Effect.EffectType;
import org.knime.node.parameters.updates.EffectPredicate;
import org.knime.node.parameters.updates.EffectPredicateProvider;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.StateComputationAbortException;
import org.knime.node.parameters.updates.StateProvider;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.updates.util.BooleanReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.RadioButtonsWidget;
import org.rdkit.knime.nodes.molecule2rdkit.Molecule2RDKitConverterNodeModel.ParseErrorPolicy;
import org.rdkit.knime.util.RDKitMoleculeColumnAutoGuessProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;
import org.rdkit.knime.util.RDKitResultColumnNameAutoGuessProvider;

/**
 * Node parameters for RDKit From Molecule.
 *
 * @author Jannik Semperowitsch, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
@SuppressWarnings("restriction")
final class Molecule2RDKitConverterNodeParameters implements NodeParameters {

    @Widget(title = "Molecule column",
        description = "The column that contains the molecules (SMILES, SDF or SMARTS type column). "
            + "If the input column type supports multiple molecule types, the node will take SMILES before SDF before SMARTS.")
    @ChoicesProvider(MoleculeColumnsProvider.class)
    @ValueProvider(MoleculeColumnAutoGuessProvider.class)
    @ValueReference(InputColumnRef.class)
    @Persist(configKey = Molecule2RDKitConverterNodeModel.CFG_INPUT_COLUMN)
    String m_inputColumn;

    static final class MoleculeColumnsProvider extends RDKitMoleculeColumnChoicesProvider {
        MoleculeColumnsProvider() {
            super(0, SdfValue.class);
        }
    }

    static final class InputColumnRef implements ParameterReference<String> {
    }

    static final class MoleculeColumnAutoGuessProvider extends RDKitMoleculeColumnAutoGuessProvider {
        MoleculeColumnAutoGuessProvider() {
            super(InputColumnRef.class, 0, 0, SdfValue.class);
        }
    }

    @Widget(title = "Treat as query",
        description = "Flag that can be used for SMILES and SDF input columns. Select it to prepare the resulting "
            + "RDKit molecules as queries. This forces a full sanitization, keeps hydrogens in the molecule and "
            + "merges query hydrogens.")
    @ValueReference(TreatAsQueryRef.class)
    @Persist(configKey = Molecule2RDKitConverterNodeModel.CFG_TREAT_AS_QUERY)
    @Effect(predicate = EnableTreatAsQueryRef.class, type = EffectType.ENABLE)
    boolean m_treatAsQuery;

    static final class TreatAsQueryRef implements BooleanReference {
    }

    @Widget(title = "New column name",
        description = "Name of the new column in the output table.")
    @Persist(configKey = Molecule2RDKitConverterNodeModel.CFG_NEW_COLUMN_NAME)
    @ValueProvider(NewColumnNameProvider.class)
    @ValueReference(NewColumnNameRef.class)
    String m_newColumnName;
    
    static final class NewColumnNameRef implements ParameterReference<String> {
    }
    
    static final class NewColumnNameProvider extends RDKitResultColumnNameAutoGuessProvider {

		protected NewColumnNameProvider() {
			super(InputColumnRef.class, NewColumnNameRef.class, "(RDKit Mol)");
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
        description = "Toggles removal of the input molecule column in the output table.")
    @Persist(configKey = Molecule2RDKitConverterNodeModel.CFG_REMOVE_SOURCE_COLUMNS)
    @ValueReference(RemoveSourceColumnRef.class)
    boolean m_removeSourceColumns;
    
    static final class RemoveSourceColumnRef implements BooleanReference {
	}

    @Widget(title = "Error handling",
        description = "If the molecule in an input row cannot be converted, you can select if this row should be "
            + "separated from the output table and be delivered at the second port, or if a missing value should "
            + "be inserted instead of the RDKit molecule in the result table.")
    @ValueReference(SeparateRowsRef.class)
    @Persist(configKey = Molecule2RDKitConverterNodeModel.CFG_BAD_ROWS_TO_PORT1)
    @RadioButtonsWidget
    ParseErrorPolicy m_separateRows = ParseErrorPolicy.SPLIT_ROWS;

    static final class SeparateRowsRef
            implements ParameterReference<ParseErrorPolicy> {
    }

    @Widget(title = "Generate error information column",
        description = "If checked, an additional column will be added that contains error information for all "
            + "structures that failed conversion.")
    @ValueReference(GenerateErrorInfoRef.class)
    @Persist(configKey = Molecule2RDKitConverterNodeModel.CFG_GENERATE_ERROR_INFO)
    boolean m_generateErrorInfo;

    static final class GenerateErrorInfoRef implements BooleanReference {
    }
    
    @Widget(title = "Error information column name",
        description = "Name of the column that will contain error information for structures that failed conversion.")
    @Effect(predicate = GenerateErrorInfoRef.class, type = EffectType.SHOW)
    @Persist(configKey = Molecule2RDKitConverterNodeModel.CFG_ERROR_INFO_COLUMN_NAME)
    @ValueProvider(ErrorInfoColumnNameProvider.class)
    @ValueReference(ErrorInfoColumnNameRef.class)
    String m_errorInfoColumnName = "";
    
    static final class ErrorInfoColumnNameRef implements ParameterReference<String> {
    }
    
    static final class ErrorInfoColumnNameProvider extends RDKitResultColumnNameAutoGuessProvider {

		protected ErrorInfoColumnNameProvider() {
			super(InputColumnRef.class, ErrorInfoColumnNameRef.class, "(RDKit Error Info)");
		}
		
		private Supplier<Boolean> m_removeSourceColumn;
		
		private Supplier<String> m_newColumnName;
		
		private Supplier<ParseErrorPolicy> m_separateRows;
		
		@Override
		public void init(StateProviderInitializer initializer) {
			super.init(initializer);
			m_removeSourceColumn = initializer.getValueSupplier(RemoveSourceColumnRef.class);
			m_newColumnName = initializer.getValueSupplier(NewColumnNameRef.class);
			m_separateRows = initializer.getValueSupplier(SeparateRowsRef.class);
		}

		@Override
		protected String[] getExcludedColumnNames(NodeParametersInput parametersInput, 
			final String currentInputColumnName) {
			return m_separateRows.get() != ParseErrorPolicy.SPLIT_ROWS && m_removeSourceColumn.get() ?
					null : new String[] { currentInputColumnName };
		}

		@Override
		protected String[] getAdditionalColumnNames(NodeParametersInput parametersInput,
				String currentInputColumnName) {
			return m_separateRows.get() == ParseErrorPolicy.SPLIT_ROWS ? null : new String[] { m_newColumnName.get() };
		}
		
    }

	@Widget(title = "Generate 2D coordinates", description = "Select this option to generate 2D coordinates for the "
			+ "molecules. The coordinates are used "
			+ "for, among other things, 2D renderings of the structures. If not selected, the renderer will "
			+ "(re-)compute coordinates on demand.")
	@ValueReference(GenerateCoordinatesRef.class)
	@Persist(configKey = Molecule2RDKitConverterNodeModel.CFG_GENERATE_COORDINATES)
	boolean m_generateCoordinates;

	static final class GenerateCoordinatesRef implements BooleanReference {
	}

	@Widget(title = "Force generation", description = "Select this box to enforce 2D coordinate generation. "
			+ "Coordinates may already be available "
			+ "in the (SDF) input, so this option allows you to discard the original coordinates and recompute "
			+ "from scratch.")
	@Effect(predicate = GenerateCoordinatesRef.class, type = EffectType.ENABLE)
	@Persist(configKey = Molecule2RDKitConverterNodeModel.CFG_FORCE_GENERATE_COORDINATES)
	boolean m_forceGenerateCoordinates;

    @Widget(title = "Keep hydrogens",
        description = "Switch this option on to prevent the RDKit from removing hydrogens from molecules "
            + "constructed from SDF. This option has no effect for SMILES or SMARTS input.")
    @Advanced
    @Persist(configKey = Molecule2RDKitConverterNodeModel.CFG_KEEP_HS)
    @Effect(predicate = EnableKeepHsOptionRef.class, type = EffectType.ENABLE)
    boolean m_keepHs;

    @Widget(title = "Partial sanitization",
        description = "The RDKit does a fair amount of work when compounds are loaded to make sure that they are "
            + "chemically reasonable and correct. This is called sanitization. When switching on partial "
            + "sanitization the following options allow specific aspects of the sanitization to be turned off.")
    @ValueReference(PartialSanitizationRef.class)
    @Advanced
    @Persist(configKey = Molecule2RDKitConverterNodeModel.CFG_SKIP_SANITIZATION)
    @Effect(predicate = EnableSanitizationOptionRef.class, type = EffectType.ENABLE)
    boolean m_partialSanitization;

    static final class PartialSanitizationRef implements BooleanReference {
    }

    @Widget(title = "Strict parsing of mol blocks",
        description = "When SDF molecules are converted into RDKit molecules, this option sets the tolerance level "
            + "for parsing mol blocks. The default value for new nodes is defined in RDKit Nodes / RDKit Types "
            + "preferences. (Introduced in December 2022)")
    @Advanced
    @Persist(configKey = Molecule2RDKitConverterNodeModel.CFG_STRICT_PARSING)
    @Effect(predicate = EnableStrictParsingOptionRef.class, type = EffectType.ENABLE)
    boolean m_strictParsing = true;

    @Widget(title = "Reperceive aromaticity",
        description = "If enabled (the default) all molecules are converted to a Kekule form and then Hueckel's "
            + "rules are applied to determine aromaticity. If disabled, whatever aromaticity information is "
            + "present in the input will be used. NOTE: SDF/MOL data contains no information about aromaticity.")
    @Effect(predicate = EnableAromaticityAndStereochem.class, type = EffectType.DISABLE)
    @Advanced
    @Persist(configKey = Molecule2RDKitConverterNodeModel.CFG_DO_AROMATICITY)
    boolean m_doAromaticity = true;
    
    static final class EnableAromaticityAndStereochem implements EffectPredicateProvider {

		@Override
		public EffectPredicate init(PredicateInitializer i) {
			return or(i.getPredicate(PartialSanitizationRef.class), i.getPredicate(EnableSanitizationOptionRef.class))
					.and(not(and(i.getPredicate(PartialSanitizationRef.class),
							i.getPredicate(EnableSanitizationOptionRef.class))));
		}
    	
    }

    @Widget(title = "Correct stereochemistry",
        description = "If enabled (the default) all stereochemistry specifications will be checked to ensure that "
            + "they are not redundant or that stereochemistry markers are not set on either atoms or double bonds "
            + "that should not have them.")
    @Effect(predicate = EnableAromaticityAndStereochem.class, type = EffectType.DISABLE)
    @Advanced
    @Persist(configKey = Molecule2RDKitConverterNodeModel.CFG_DO_STEREOCHEM)
    boolean m_doStereochem = true;

    @Persistor(DoNotPersistBoolean.class)
    @ValueProvider(EnableTreatAsQuery.class)
    @ValueReference(EnableTreatAsQueryRef.class)
    boolean m_enableTreatAsQuery;
    
    static final class EnableTreatAsQueryRef implements BooleanReference {
    }
    
    static final class EnableTreatAsQuery implements StateProvider<Boolean> {

    	private Supplier<DataColumnSpec> m_inputColumnSpec;
    	
		@Override
		public void init(StateProviderInitializer initializer) {
			m_inputColumnSpec = initializer.computeFromProvidedState(InputColumnSpecProvider.class);
		}

		@Override
		public Boolean computeState(NodeParametersInput parametersInput) throws StateComputationAbortException {
			final var inputColumnSpec = m_inputColumnSpec.get();
			final DataType dataType = inputColumnSpec != null ? inputColumnSpec.getType() : null;
			return inputColumnSpec != null
					&& (dataType.isCompatible(SmilesValue.class) || dataType.isAdaptable(SmilesValue.class)
							|| dataType.isCompatible(SdfValue.class) || dataType.isAdaptable(SdfValue.class));
		}
    	
    }
    
	@Persistor(DoNotPersistBoolean.class)
    @ValueProvider(EnableSanitizationOption.class)
    @ValueReference(EnableSanitizationOptionRef.class)
    boolean m_enableSanitizationOption;
    
    static final class EnableSanitizationOptionRef implements BooleanReference {
	}
    
    static final class EnableSanitizationOption implements StateProvider<Boolean> {

    	private Supplier<DataColumnSpec> m_inputColumnSpec;
    	
    	private Supplier<Boolean> m_treatAsQuery;
    	
    	private Supplier<Boolean> m_enableTreatAsQuery;
    	
		@Override
		public void init(StateProviderInitializer initializer) {
			m_inputColumnSpec = initializer.computeFromProvidedState(InputColumnSpecProvider.class);
			m_treatAsQuery = initializer.computeFromValueSupplier(TreatAsQueryRef.class);
			m_enableTreatAsQuery = initializer.computeFromProvidedState(EnableTreatAsQuery.class);
		}

		@Override
		public Boolean computeState(NodeParametersInput parametersInput) throws StateComputationAbortException {
			final var inputColumnSpec = m_inputColumnSpec.get();
			final DataType dataType = inputColumnSpec != null ? inputColumnSpec.getType() : null;
			return (inputColumnSpec != null
					&& (dataType.isCompatible(SmilesValue.class) || dataType.isAdaptable(SmilesValue.class)
							|| dataType.isCompatible(SdfValue.class) || dataType.isAdaptable(SdfValue.class)))
					&& (!m_enableTreatAsQuery.get() || !m_treatAsQuery.get());
		}
    	
    }
    
    @Persistor(DoNotPersistBoolean.class)
    @ValueProvider(EnableKeepHsOption.class)
    @ValueReference(EnableKeepHsOptionRef.class)
    boolean m_enableKeepHsOption;
    
    static final class EnableKeepHsOptionRef implements BooleanReference {
    }
    
    static final class EnableKeepHsOption implements StateProvider<Boolean> {

    	private Supplier<DataColumnSpec> m_inputColumnSpec;
    	
    	private Supplier<Boolean> m_treatAsQuery;
    	
    	private Supplier<Boolean> m_enableTreatAsQuery;
    	
		@Override
		public void init(StateProviderInitializer initializer) {
			m_inputColumnSpec = initializer.computeFromProvidedState(InputColumnSpecProvider.class);
			m_treatAsQuery = initializer.computeFromValueSupplier(TreatAsQueryRef.class);
			m_enableTreatAsQuery = initializer.computeFromProvidedState(EnableTreatAsQuery.class);
		}

		@Override
		public Boolean computeState(NodeParametersInput parametersInput) throws StateComputationAbortException {
			final var inputColumnSpec = m_inputColumnSpec.get();
			final DataType dataType = inputColumnSpec != null ? inputColumnSpec.getType() : null;
			return (inputColumnSpec != null
					&& (dataType.isCompatible(SdfValue.class) || dataType.isAdaptable(SdfValue.class)))
					&& (!m_enableTreatAsQuery.get() || !m_treatAsQuery.get());
		}
    	
    }
    
    @Persistor(DoNotPersistBoolean.class)
    @ValueProvider(EnableStrictParsingOption.class)
    @ValueReference(EnableStrictParsingOptionRef.class)
    boolean m_enableStrictParsingOption;
    
    static final class EnableStrictParsingOptionRef implements BooleanReference {
	}
    
    static final class EnableStrictParsingOption implements StateProvider<Boolean> {

    	private Supplier<DataColumnSpec> m_inputColumnSpec;
    	
		@Override
		public void init(StateProviderInitializer initializer) {
			m_inputColumnSpec = initializer.computeFromProvidedState(InputColumnSpecProvider.class);
		}

		@Override
		public Boolean computeState(NodeParametersInput parametersInput) throws StateComputationAbortException {
			final var inputColumnSpec = m_inputColumnSpec.get();
			final DataType dataType = inputColumnSpec != null ? inputColumnSpec.getType() : null;
			return (inputColumnSpec != null
					&& (dataType.isCompatible(SdfValue.class) || dataType.isAdaptable(SdfValue.class)));
		}
    	
    }
    
    static final class InputColumnSpecProvider implements StateProvider<DataColumnSpec> {

    	private Supplier<String> m_inputColumn;
    	
		@Override
		public void init(StateProviderInitializer initializer) {
			initializer.computeBeforeOpenDialog();
			m_inputColumn = initializer.computeFromValueSupplier(InputColumnRef.class);
		}

		@Override
		public DataColumnSpec computeState(NodeParametersInput parametersInput) throws StateComputationAbortException {
			final String columnName = m_inputColumn.get();
			if (columnName == null || columnName.isEmpty()) {
				return null;
			}
			final var inSpecOpt = parametersInput.getInTableSpec(0);
			if (inSpecOpt.isEmpty()) {
				return null;
			}
			return inSpecOpt.get().getColumnSpec(columnName);
		}
    	
    }
    
}
