package org.rdkit.knime.util;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import java.util.List;
import java.util.function.Supplier;

import org.knime.node.parameters.migration.ConfigMigration;
import org.knime.node.parameters.migration.NodeParametersMigration;
import org.knime.node.parameters.persistence.NodeParametersPersistor;

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
		
		private Supplier<E> m_defaultValueSupplier;
		
		private Supplier<E> m_customValueSupplier;
		
		/**
		 * Constructor for the DefaultFileSwitchMigration.
		 * 
		 * @param fileSwitchConfigKey the configuration key for the file switch parameter
		 * @param fileIOConfigKey the configuration key for the file path that should be checked for emptiness
		 * @param defaultValueSupplier a supplier that provides the default value to set if the file path is empty
		 */
		protected DefaultFileSwitchMigration(String fileSwitchConfigKey, String fileIOConfigKey, 
			Supplier<E> defaultValueSupplier, Supplier<E> customValueSupplier) {
			m_fileSwitchConfigKey = fileSwitchConfigKey;
			m_fileIOConfigKey = fileIOConfigKey;
			m_defaultValueSupplier = defaultValueSupplier;
			m_customValueSupplier = customValueSupplier;
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
				return m_defaultValueSupplier.get();
			}
			
			return path == null || path.isEmpty() ? m_defaultValueSupplier.get() : m_customValueSupplier.get();
		}
		
	}
	
}
