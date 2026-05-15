package org.rdkit.knime.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.knime.chem.types.SmartsValue;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.widget.choices.ColumnChoicesProvider;
import org.rdkit.knime.types.RDKitMolValue;

/**
 * Column choices provider for RDKit molecule columns. By default, it considers compatible columns of type 
 * {@link RDKitMolValue, SmartsValue, SmilesValue} but additional types can be specified via the constructor.
 * 
 * @author Jannik Semperowitsch, KNIME GmbH, Konstanz, Germany
 */
public class RDKitMoleculeColumnChoicesProvider implements ColumnChoicesProvider {
    private int m_inputTableIndex = 0;
    private final Class<? extends DataValue>[] m_valueClasses;
    
    /**
     * Default RDKit types used if none are provided.
     */
    @SuppressWarnings("unchecked")
	public static final Class<? extends DataValue>[] DEFAULT_CLASSES = new Class[] { 
        RDKitMolValue.class, 
        SmartsValue.class, 
        SmilesValue.class 
    };

	@Override
	public List<DataColumnSpec> columnChoices(final NodeParametersInput context) {
		return context.getInTableSpec(m_inputTableIndex)
				.map(spec -> spec.stream().filter(this::isColumnCompatible).toList()).orElse(List.of());
	}

	/**
	 * Empty constructor
	 */
    @SuppressWarnings("unchecked")
	public RDKitMoleculeColumnChoicesProvider() {
        this(0, new Class[0]);
    }

    /**
     * Constructor with custom input table index
     * 
     * @param inputTableIndex the index of the input table to consider for column choices
     */
    @SuppressWarnings("unchecked")
	public RDKitMoleculeColumnChoicesProvider(int inputTableIndex) {
        this(inputTableIndex, new Class[0]);
    }

    /**
     * Constructor with custom input table index and additional value classes
     * 
     * @param inputTableIndex the index of the input table to consider for column choices
     * @param valueClasses additional DataValue classes to consider for column compatibility, 
     * in addition to the default RDKit molecule types
     */
    @SuppressWarnings("unchecked")
	@SafeVarargs
	public RDKitMoleculeColumnChoicesProvider(int inputTableIndex, Class<? extends DataValue>... valueClasses) {
        this.m_inputTableIndex = inputTableIndex;
        Set<Class<? extends DataValue>> combined = 
        		new HashSet<Class<? extends DataValue>>(Arrays.asList(DEFAULT_CLASSES));
		if (valueClasses != null && valueClasses.length != 0) {
			combined.addAll(Arrays.asList(valueClasses));
		}
		m_valueClasses = (Class<? extends DataValue>[]) combined.toArray(new Class[0]);
    }

	private boolean isColumnCompatible(DataColumnSpec colSpec) {
        DataType colType = colSpec.getType();
        for (Class<? extends DataValue> clazz : RDKitAdapterCellSupport.expandByAdaptableTypes(m_valueClasses)) {
            if (colType.isCompatible(clazz) || colType.isAdaptable(clazz)) {
                return true;
            }
        }
        return false;
    }
	
}