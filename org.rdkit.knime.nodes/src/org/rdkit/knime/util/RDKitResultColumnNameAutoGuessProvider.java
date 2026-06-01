package org.rdkit.knime.util;

import java.util.List;
import java.util.function.Supplier;

import org.knime.core.data.DataTableSpec;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.StateComputationAbortException;
import org.knime.node.parameters.updates.StateProvider;
import org.knime.node.parameters.updates.internal.StateProviderInitializerInternal;

/**
 * Auto guess provider for new column names depending on input RDKit column names.
 * 
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 */
public abstract class RDKitResultColumnNameAutoGuessProvider implements StateProvider<String> {

	private Class<? extends ParameterReference<String>> m_inputColumnNameRef;
	
	private Class<? extends ParameterReference<String>> m_resultColumnNameRef;
	
	private Integer m_tableInputIndex;
	
	private String m_resultColumnNameSuffix;
	
	/**
	 * Create a new auto guess provider for result column names.
	 * 
	 * @param inputColumnNameRef the parameter reference for the input column name parameter, which is used to obtain 
	 * the input column name for the auto-guess
	 * @param resultColumnNameRef the parameter reference for the result column name parameter, which is used to obtain 
	 * the user input for the new column name
	 * @param tableInputIndex the index of the table input to consider for the auto-guess (in case there are multiple 
	 * table inputs)
	 * @param resultColumnNameSuffix the suffix to append to the input column name for the suggested new column name 
	 * (e.g. "(Aromatized)"), which is used in case the user did not specify a new column name
	 */
	protected RDKitResultColumnNameAutoGuessProvider(
		final Class<? extends ParameterReference<String>> inputColumnNameRef,
		final Class<? extends ParameterReference<String>> resultColumnNameRef, final Integer tableInputIndex, 
		final String resultColumnNameSuffix) {
		m_inputColumnNameRef = inputColumnNameRef;
		m_resultColumnNameRef = resultColumnNameRef;
		m_tableInputIndex = tableInputIndex;
		m_resultColumnNameSuffix = resultColumnNameSuffix;
	}
	
	
	/**
	 * Create a new auto guess provider for result column names, which considers the first table input (index 0).
	 * 
	 * @param inputColumnNameRef the parameter reference for the input column name parameter, which is used to obtain 
	 * the input column name for the auto-guess
	 * @param resultColumnRef the parameter reference for the result column name parameter, which is used to obtain the 
	 * user input for the new column name
	 * @param resultColumnNameSuffix the suffix to append to the input column name for the suggested new column name 
	 * (e.g. "(Aromatized)"), which is used in case the user did not specify a new column name
	 */
	protected RDKitResultColumnNameAutoGuessProvider(
		final Class<? extends ParameterReference<String>> inputColumnNameRef, 
		final Class<? extends ParameterReference<String>> resultColumnRef, final String resultColumnNameSuffix) {
		this(inputColumnNameRef, resultColumnRef, 0, resultColumnNameSuffix);
	}
	
	private Supplier<String> m_inputColumnNameSupplier;
	
	private Supplier<String> m_resultColumnNameSupplier;
	
	@Override
	public void init(StateProviderInitializer initializer) {
		((StateProviderInitializerInternal)initializer).computeOnParametersLoaded();
		m_inputColumnNameSupplier = initializer.computeFromValueSupplier(m_inputColumnNameRef);
		m_resultColumnNameSupplier = initializer.getValueSupplier(m_resultColumnNameRef);
	}

	@Override
	public String computeState(NodeParametersInput parametersInput) throws StateComputationAbortException {
		final String strInputColumnName = m_inputColumnNameSupplier.get();
		return autoGuessColumnName(parametersInput.getInTableSpec(m_tableInputIndex).orElse(null), 
				m_resultColumnNameSupplier.get(),
				getAdditionalColumnNames(parametersInput, strInputColumnName),
				getExcludedColumnNames(parametersInput, strInputColumnName), 
				"%s %s".formatted(strInputColumnName, m_resultColumnNameSuffix));
	}
	
	/**
	 * Override this method to provide additional column names to consider for the auto-guess, which are not part of 
	 * the input table spec (e.g. because they are created by other parameters of the same node). 
	 * The provided column names will be considered as existing column names in the input table spec for the auto-guess,
	 * i.e. the auto-guess will try to avoid suggesting these names.
	 * 
	 * @param parametersInput the parameters input
	 * @param currentInputColumnName the current input column name, which can be 
	 * used e.g. to exclude from the auto-guess
	 * @return an array of additional column names to consider for the auto-guess, or null
	 */
	protected String[] getAdditionalColumnNames(final NodeParametersInput parametersInput, 
		final String currentInputColumnName) {
		return null;
	}
	
	/**
	 * Override this method to provide column names to exclude from the auto-guess e.g. columns which are part of the 
	 * input table spec.
	 * 
	 * @param parametersInput the parameters input
	 * @param currentInputColumnName the current input column name, which can be 
	 * used e.g. to exclude from the auto-guess
	 * @return an array of column names to exclude from the auto-guess, or null
	 */
	protected String[] getExcludedColumnNames(final NodeParametersInput parametersInput, 
		final String currentInputColumnName) {
		return null;
	}
	
	private static String autoGuessColumnName(final DataTableSpec inSpec, String strNewColumnName, 
		final String[] arrMoreColumnNames, final String[] arrExclColumnNames, final String strSuggestedName) 
		throws StateComputationAbortException {
		String result = strSuggestedName;

		// Pre-checks
		if (inSpec == null) {
			throw new StateComputationAbortException();
		}
		if (strSuggestedName == null) {
			throw new StateComputationAbortException();
		}

		// Make the name unique and set it, if new column name is still empty
		if (strNewColumnName == null || strNewColumnName.isEmpty()) {
			// Create list of all existing names
			final List<String> listNames = SettingsUtils.createMergedColumnNameList(inSpec, arrMoreColumnNames,
					arrExclColumnNames);

			// Unify the name
			int uniquifier = 1;

			while (listNames.contains(result)) {
				result = strSuggestedName + " (#" + uniquifier + ")";
				uniquifier++;
			}
		} else {
			result = strNewColumnName;
		}

		return result;
	}
	
}
