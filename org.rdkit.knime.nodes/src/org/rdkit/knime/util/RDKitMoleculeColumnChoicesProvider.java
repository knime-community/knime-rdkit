package org.rdkit.knime.util;

import org.knime.chem.types.SmartsValue;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings.DefaultNodeSettingsContext;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ColumnChoicesProvider;
import org.rdkit.knime.types.RDKitMolValue;

public class RDKitMoleculeColumnChoicesProvider implements ColumnChoicesProvider {
	private int INPUT_TABLE_INDEX = 0;
	
	// provide a choices list of all columns that are compatible with RDKit
    @Override
    public DataColumnSpec[] columnChoices(final DefaultNodeSettingsContext context) {
		return context
				.getDataTableSpec(INPUT_TABLE_INDEX).map(spec -> spec.stream()
						.filter(this::isColumnCompatible).toArray(DataColumnSpec[]::new))
				.orElse(new DataColumnSpec[0]);
    }
    
    // Constructor with default INPUT_TABLE_INDEX = 0
	public RDKitMoleculeColumnChoicesProvider() {
	}
	
	// Constructor with custom INPUT_TABLE_INDEX
	public RDKitMoleculeColumnChoicesProvider(int inputTableIndex) {
		INPUT_TABLE_INDEX = inputTableIndex;
	}
	
	// check if the column is compatible with RDKit
	private boolean isColumnCompatible(DataColumnSpec colSpec) {
        DataType colType = colSpec.getType();
        Class<? extends DataValue>[] m_valueClasses = new Class[] { RDKitMolValue.class, SmartsValue.class, SmilesValue.class };
		for (Class<? extends DataValue> clazz : m_valueClasses ) {
            if (colType.isCompatible(clazz) || colType.isAdaptable(clazz)) {
                return true;
            }
        }
        return false;
    }
}