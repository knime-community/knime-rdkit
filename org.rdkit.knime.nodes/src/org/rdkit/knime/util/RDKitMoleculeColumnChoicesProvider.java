package org.rdkit.knime.util;

import java.util.List;

import org.knime.chem.types.SmartsValue;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.widget.choices.ColumnChoicesProvider;
import org.rdkit.knime.types.RDKitMolValue;

public class RDKitMoleculeColumnChoicesProvider implements ColumnChoicesProvider {
    private int m_inputTableIndex = 0;
    private final Class<? extends DataValue>[] m_valueClasses;
    
    // Default RDKit types used if none are provided
    @SuppressWarnings("unchecked")
	private static final Class<? extends DataValue>[] DEFAULT_CLASSES = new Class[] { 
        RDKitMolValue.class, 
        SmartsValue.class, 
        SmilesValue.class 
    };

    @Override
    public List<DataColumnSpec> columnChoices(final NodeParametersInput context) {
        return context
                .getInTableSpec(m_inputTableIndex).map(spec -> spec.stream()
                        .filter(this::isColumnCompatible).toList())
                .orElse(List.of());
    }

    // 1- Empty constructor
    public RDKitMoleculeColumnChoicesProvider() {
        this(0, DEFAULT_CLASSES);
    }

    // 2- Constructor with custom inputTableIndex
    public RDKitMoleculeColumnChoicesProvider(int inputTableIndex) {
        this(inputTableIndex, DEFAULT_CLASSES);
    }

    // 3- Constructor with custom index and specific value classes
    @SuppressWarnings("unchecked")
    public RDKitMoleculeColumnChoicesProvider(int inputTableIndex, Class<? extends DataValue>[] valueClasses) {
        this.m_inputTableIndex = inputTableIndex;
        var combined = new Class[valueClasses.length + DEFAULT_CLASSES.length];
        System.arraycopy(valueClasses, 0, combined, 0, valueClasses.length);
        System.arraycopy(DEFAULT_CLASSES, 0, combined, valueClasses.length, DEFAULT_CLASSES.length);
        this.m_valueClasses = combined;
    }

    @SuppressWarnings("unused")
	private boolean isColumnCompatible(DataColumnSpec colSpec) {
        DataType colType = colSpec.getType();
        for (Class<? extends DataValue> clazz : m_valueClasses) {
            if (colType.isCompatible(clazz) || colType.isAdaptable(clazz)) {
                return true;
            }
        }
        return false;
    }
}