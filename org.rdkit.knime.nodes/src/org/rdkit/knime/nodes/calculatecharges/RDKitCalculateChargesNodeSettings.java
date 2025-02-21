package org.rdkit.knime.nodes.calculatecharges;

import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.api.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.choices.ColumnChoicesProviderUtil.AllColumnChoicesProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;

/**
 * Settings for the RDKit Calculate Charges node using the webui framework.
 * 
 * @author Marc Lehner, KNIME GmbH, Zurich, Switzerland
 */
public class RDKitCalculateChargesNodeSettings implements DefaultNodeSettings{
	
	@Persist(configKey = "input_column")
	@Widget(title = "Input Column",
            description = "The column containing RDKit molecules (e.g. from RDKit Mol, SMILES, or SDF).")
	@ChoicesWidget(choices = RDKitMoleculeColumnChoicesProvider.class)
	String m_inputColumn;
	
	@Persist(configKey = "new_column_name")
	@Widget(title = "New Column Name",
            description = "Name for the newly created column with generated coordinates.")
	String m_newColumnName;
}
