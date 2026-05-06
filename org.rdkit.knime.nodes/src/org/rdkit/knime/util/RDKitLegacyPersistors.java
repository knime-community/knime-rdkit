package org.rdkit.knime.util;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.node.parameters.persistence.NodeParametersPersistor;

public class RDKitLegacyPersistors {

	RDKitLegacyPersistors() {
		// utility class with only static members, prevent instantiation
	}
	
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
