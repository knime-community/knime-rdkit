package org.rdkit.knime.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataValue;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.legacy.updates.ColumnNameAutoGuessValueProvider;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.widget.choices.util.ColumnSelectionUtil;

public abstract class RDKitMoleculeColumnAutoGuessProvider extends ColumnNameAutoGuessValueProvider {

	protected Class<? extends ParameterReference<String>> m_moleculeColumnRef;
	
	protected Integer m_inputTableIndex;
	
	protected Integer m_valueIndex;

    private final Class<? extends DataValue>[] m_valueClasses;
	
	protected RDKitMoleculeColumnAutoGuessProvider(Class<? extends ParameterReference<String>> moleculeColumnRef,
			final Integer valueIndex) {
		this(moleculeColumnRef, 0, valueIndex, null);
	}

	protected RDKitMoleculeColumnAutoGuessProvider(Class<? extends ParameterReference<String>> moleculeColumnRef,
		final Integer inputTableIndex, final Integer valueIndex) {
		this(moleculeColumnRef, inputTableIndex, valueIndex, null);
	}

	protected RDKitMoleculeColumnAutoGuessProvider(Class<? extends ParameterReference<String>> moleculeColumnRef,
		final Integer inputTableIndex, final Integer valueIndex, Class<? extends DataValue>[] valueClasses) {
		super(moleculeColumnRef);
		m_inputTableIndex = inputTableIndex;
		m_valueIndex = valueIndex;
		m_valueClasses = valueClasses;
	}

	@Override
	protected Optional<DataColumnSpec> autoGuessColumn(NodeParametersInput parametersInput) {
		ArrayList<Class<? extends DataValue>> combined = 
				new ArrayList<>(Arrays.asList(RDKitMoleculeColumnChoicesProvider.DEFAULT_CLASSES));
		if (m_valueClasses != null && m_valueClasses.length != 0) {
			combined.addAll(Arrays.asList(m_valueClasses));
		}
		@SuppressWarnings("unchecked")
		final var compatibleColumns = ColumnSelectionUtil.getCompatibleColumns(parametersInput, m_inputTableIndex, 
				(Class<? extends DataValue>[])combined.toArray(new Class[0]));
		if (compatibleColumns.isEmpty()) {
			return Optional.empty();
		} else if (m_valueIndex < compatibleColumns.size()) {
			return Optional.of(compatibleColumns.get(m_valueIndex));
		} else {
			return Optional.of(compatibleColumns.get(0));
		}
	}

}
