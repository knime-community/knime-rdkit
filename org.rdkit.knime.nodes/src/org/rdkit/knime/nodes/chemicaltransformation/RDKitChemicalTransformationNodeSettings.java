package org.rdkit.knime.nodes.chemicaltransformation;

import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.field.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Label;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ValueSwitchWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.choices.ColumnChoicesProviderUtil.AllColumnChoicesProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;

/**
 * Settings for the RDKit Chemical Transformation node using the webui
 * framework.
 * 
 * @author Marc Lehner, KNIME GmbH, Zurich, Switzerland
 */
public class RDKitChemicalTransformationNodeSettings implements DefaultNodeSettings {
	
	@Persist(configKey = "input_mol_column")
	@Widget(title = "Input Molecule Column", description = "The column containing RDKit molecules (e.g. from RDKit Mol, SMILES, or SDF).")
	@ChoicesWidget(choices = RDKitMoleculeColumnChoicesProvider.class)
	String m_inputColumn;

	@Persist(configKey = "input_reaction_column")
	@Widget(title = "Input Reaction Column", description = "The column containing RDKit reactions (e.g. from RDKit Reaction, SMARTS, or SDF).")
	@ChoicesWidget(choices = AllColumnChoicesProvider.class)
	String m_reactionColumn;
	
	@Persist(configKey = "new_column_name")
	@Widget(title = "New Column Name", description = "Name for the newly created column with the transformed molecules.")
	String m_newColumnName;
	
	@Persist(configKey = "max_reaction_cycles")
	@Widget(title = "Maximal Number of Applied Reaction Cycles", description = "The maximal number of applied reaction cycles.")
	int m_maxReactionCycles = 100;
	
	@Persist(configKey = "remove_source_columns")
	@Widget(title = "Remove Source Columns", description = "If checked, the original source columns are removed from the output table.")
	boolean m_removeSourceColumns = false;
}
