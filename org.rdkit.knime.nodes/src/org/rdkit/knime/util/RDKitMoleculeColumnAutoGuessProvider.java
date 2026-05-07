package org.rdkit.knime.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataValue;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.legacy.updates.ColumnNameAutoGuessValueProvider;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.widget.choices.util.ColumnSelectionUtil;

/**
 * Auto guess provider for RDKit molecule columns. By default, it considers compatible columns of type 
 * {@link RDKitMolValue, SmartsValue, SmilesValue} but additional types can be specified via the constructor.
 * 
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 */
public abstract class RDKitMoleculeColumnAutoGuessProvider extends ColumnNameAutoGuessValueProvider {

	protected Class<? extends ParameterReference<String>> m_moleculeColumnRef;
	
	protected Integer m_inputTableIndex;
	
	protected Integer m_valueIndex;

    private final Class<? extends DataValue>[] m_valueClasses;
	
    /**
	 * Constructor with default input table index and value index, and default RDKit types.
	 * 
	 * @param moleculeColumnRef the parameter reference for the molecule column
	 */
	@SuppressWarnings("unchecked")
	protected RDKitMoleculeColumnAutoGuessProvider(Class<? extends ParameterReference<String>> moleculeColumnRef,
			final Integer valueIndex) {
		this(moleculeColumnRef, 0, valueIndex, new Class[0]);
	}

	/**
	 * Constructor with default value index and default RDKit types.
	 * 
	 * @param moleculeColumnRef the parameter reference for the molecule column
	 * @param inputTableIndex the index of the input table to consider for column choices
	 */
	@SuppressWarnings("unchecked")
	protected RDKitMoleculeColumnAutoGuessProvider(Class<? extends ParameterReference<String>> moleculeColumnRef,
		final Integer inputTableIndex, final Integer valueIndex) {
		this(moleculeColumnRef, inputTableIndex, valueIndex, new Class[0]);
	}

	/**
	 * Constructor with all parameters specified.
	 * 
	 * @param moleculeColumnRef the parameter reference for the molecule column
	 * @param inputTableIndex the index of the input table to consider for column choices
	 * @param valueIndex the index of the compatible column to select if multiple are available (0-based)
	 * @param valueClasses additional value classes to consider as compatible, in addition to the default RDKit types
	 */
	@SuppressWarnings("unchecked")
	@SafeVarargs
	protected RDKitMoleculeColumnAutoGuessProvider(Class<? extends ParameterReference<String>> moleculeColumnRef,
		final Integer inputTableIndex, final Integer valueIndex, Class<? extends DataValue>... valueClasses) {
		super(moleculeColumnRef);
		m_inputTableIndex = inputTableIndex;
		m_valueIndex = valueIndex;
        Set<Class<? extends DataValue>> combined = new HashSet<Class<? extends DataValue>>(
        		Arrays.asList(RDKitMoleculeColumnChoicesProvider.DEFAULT_CLASSES));
		if (valueClasses != null && valueClasses.length != 0) {
			combined.addAll(Arrays.asList(valueClasses));
		}
		m_valueClasses = (Class<? extends DataValue>[]) combined.toArray(new Class[0]);
	}

	@Override
	protected Optional<DataColumnSpec> autoGuessColumn(NodeParametersInput parametersInput) {
		final var compatibleColumns = ColumnSelectionUtil.getCompatibleColumns(
				parametersInput, m_inputTableIndex, m_valueClasses);
		if (compatibleColumns.isEmpty()) {
			return Optional.empty();
		} else if (m_valueIndex < compatibleColumns.size()) {
			return Optional.of(compatibleColumns.get(m_valueIndex));
		} else {
			return Optional.of(compatibleColumns.get(0));
		}
	}

}
