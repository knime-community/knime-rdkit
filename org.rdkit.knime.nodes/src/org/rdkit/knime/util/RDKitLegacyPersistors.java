package org.rdkit.knime.util;

import java.util.Optional;
import java.util.function.Supplier;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import java.util.List;

import org.knime.node.parameters.migration.ConfigMigration;
import org.knime.node.parameters.migration.NodeParametersMigration;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.persistence.NodeParametersPersistor;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.StateComputationAbortException;
import org.knime.node.parameters.updates.StateProvider;
import org.knime.node.parameters.updates.internal.StateProviderInitializerInternal;
import org.knime.node.parameters.widget.choices.RowIDChoice;
import org.knime.node.parameters.widget.choices.StringOrEnum;

/**
 * Utility class for legacy persistors related to RDKit nodes. This includes persistors for molecule column settings that
 * handle both the current and deprecated parameter keys to ensure backward compatibility with older node configurations.
 * 
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 */
public class RDKitLegacyPersistors {

	RDKitLegacyPersistors() {
		// utility class with only static members, prevent instantiation
	}
	
	/**
	 * Abstract base class for legacy molecule column persistors. This class handles loading and saving of molecule 
	 * column settings while supporting both the current parameter key and a deprecated key for backward compatibility.
	 */
    public abstract static class LegacyMoleculeColumnPersistor implements NodeParametersPersistor<String> {

    	protected final String m_primaryKey;
    	
    	protected final String m_deprecatedKey;
    	
    	public LegacyMoleculeColumnPersistor(final String primaryKeyIndex, final String deprecatedKeyIndex) {
    		m_primaryKey = primaryKeyIndex;
			m_deprecatedKey = deprecatedKeyIndex;
    	}
    	
        @Override
        public String load(final NodeSettingsRO settings) throws InvalidSettingsException {
            if (settings.containsKey(m_primaryKey)) {
                return settings.getString(m_primaryKey);
            } else if (settings.containsKey(m_deprecatedKey)) {
                return settings.getString(m_deprecatedKey);
            }
            return null;
        }

        @Override
        public void save(final String value, final NodeSettingsWO settings) {
            settings.addString(m_primaryKey, value);
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{m_primaryKey}};
        }
        
	}

    /**
     * Abstract base class for auto-guessing column name providers that support both string and enum choices.
     * 
     * @param <E> the type of the enum choices
     */
	public abstract static class StringOrEnumColumnNameAutoGuessProvider<E extends Enum<E>>
			implements StateProvider<StringOrEnum<E>> {

		protected Class<? extends ParameterReference<StringOrEnum<E>>> m_selfReference;
		
		protected StringOrEnumColumnNameAutoGuessProvider(
				final Class<? extends ParameterReference<StringOrEnum<E>>> selfReference) {
			m_selfReference = selfReference;
		}
		
		Supplier<StringOrEnum<E>> m_currentValueSupplier;
		
	    @Override
	    public void init(final StateProviderInitializer initializer) {
	        ((StateProviderInitializerInternal)initializer).computeOnParametersLoaded();
	        m_currentValueSupplier = initializer.getValueSupplier(m_selfReference);
	    }

		protected abstract Optional<DataColumnSpec> autoGuessColumn(final NodeParametersInput parametersInput);

	    @Override
	    public StringOrEnum<E> computeState(final NodeParametersInput parametersInput) 
	    	throws StateComputationAbortException {
	    	if (isEmpty(parametersInput, m_currentValueSupplier.get())) {
	            return autoGuessValue(parametersInput);
	        }
	        throw new StateComputationAbortException();
	    }
	    
	    private boolean isEmpty(final NodeParametersInput parametersInput, final StringOrEnum<E> currentValue) {
			final var autoGuessColumn = autoGuessColumn(parametersInput);
	    	if (currentValue.getEnumChoice().isPresent() && autoGuessColumn.isEmpty()) {
				return false;
			}
	    	if (currentValue.getEnumChoice().isPresent() && !autoGuessColumn.isEmpty()) {
				return true;
			}
			final var valueString = currentValue.getStringChoice();
			return valueString == null || valueString.isEmpty();
		}
		
		private final StringOrEnum<E> autoGuessValue(final NodeParametersInput parametersInput)
				throws StateComputationAbortException {
			return autoGuessColumn(parametersInput).map(spec -> new StringOrEnum<E>(spec.getName()))
					.orElseThrow(StateComputationAbortException::new);
		}

	}

	/**
	 * Abstract base class for legacy column name persistors that support both string and RowID choices.
	 */
	public abstract static class LegacyColumnNamePersistor 
		implements NodeParametersPersistor<StringOrEnum<RowIDChoice>> {

		private static final String CFG_KEY_USE_ROWID = "useRowID";
		private static final String CFG_KEY_COLUMN_NAME = "columnName";

		private String m_columnNameDefault;

		protected LegacyColumnNamePersistor() {
			m_columnNameDefault = null;
		}

		protected LegacyColumnNamePersistor(final String columnNameDefault) {
			m_columnNameDefault = columnNameDefault;
		}

		@Override
		public StringOrEnum<RowIDChoice> load(final NodeSettingsRO settings) throws InvalidSettingsException {
			if (settings.getBoolean(CFG_KEY_USE_ROWID, false)) {
				return new StringOrEnum<>(RowIDChoice.ROW_ID);
			}
			return new StringOrEnum<>(settings.getString(CFG_KEY_COLUMN_NAME, m_columnNameDefault));
		}

		@Override
		public void save(final StringOrEnum<RowIDChoice> param, final NodeSettingsWO settings) {
			if (param.getEnumChoice().isPresent()) {
				settings.addBoolean(CFG_KEY_USE_ROWID, true);
				settings.addString(CFG_KEY_COLUMN_NAME, null);
			} else {
				settings.addBoolean(CFG_KEY_USE_ROWID, false);
				final var columnName = param.getStringChoice();
				settings.addString(CFG_KEY_COLUMN_NAME, columnName == null ? m_columnNameDefault : columnName);
			}
		}

		@Override
		public String[][] getConfigPaths() {
			return new String[][] {{CFG_KEY_COLUMN_NAME}, {CFG_KEY_USE_ROWID}};
		}

	}
	
	/**
	 * Abstract base class for file switch migrations that set a default value for a parameter if the path to the file 
	 * is empty. This is used to migrate old configurations where the file path was not set, but the parameter should 
	 * now have a default value.
	 * 
	 * @param <E> the type of the enum choices for the parameter that should be set to a default value if the file 
	 * path is empty
	 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
	 */
	public abstract static class DefaultFileSwitchMigration<E extends Enum<E>> implements NodeParametersMigration<E> {

		private String m_fileSwitchConfigKey;
		
		private String m_fileIOConfigKey;
		
		private E m_defaultValue;
		
		private E m_customValue;
		
		/**
		 * Constructor for the DefaultFileSwitchMigration.
		 * 
		 * @param fileSwitchConfigKey the configuration key for the file switch parameter
		 * @param fileIOConfigKey the configuration key for the file path that should be checked for emptiness
		 * @param defaultValue the default value to set if the file path is empty
		 * @param customValue the value to set if the file path is not empty
		 */
		protected DefaultFileSwitchMigration(final String fileSwitchConfigKey, final String fileIOConfigKey, 
			final E defaultValue, final E customValue) {
			m_fileSwitchConfigKey = fileSwitchConfigKey;
			m_fileIOConfigKey = fileIOConfigKey;
			m_defaultValue = defaultValue;
			m_customValue = customValue;
		}
		
		@Override
		public List<ConfigMigration<E>> getConfigMigrations() {
			return List.of(ConfigMigration.builder(settings -> loadFileSwitchValue(settings))
					.withMatcher(s -> !s.containsKey(m_fileSwitchConfigKey)).build());
		}
		
		private E loadFileSwitchValue(final NodeSettingsRO settings) {
			String path;
			try {
				final var logFileSettings = settings.getNodeSettings(m_fileIOConfigKey);
				final var pathSettings = logFileSettings.getNodeSettings("path");
				path = pathSettings.getString("path");
			} catch (InvalidSettingsException e) {
				return m_defaultValue;
			}
			
			return path == null || path.isEmpty() ? m_defaultValue : m_customValue;
		}
		
	}
	
}
