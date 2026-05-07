package org.rdkit.knime.util;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
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
	
}
