package org.rdkit.knime.nodes.addhs;

import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.api.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.choices.ColumnChoicesProviderUtil.AllColumnChoicesProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;

/**
 * Settings for the RDKit AddHs node using the webui framework.
 * 
 * @author Marc Lehner, KNIME GmbH, Zurich, Switzerland
 */
public final class RDKitAddHsNodeSettings implements DefaultNodeSettings {

    @Persist(configKey = "input_column")
    @Widget(title = "RDKit Mol column", description = "The column containing RDKit molecules.")
    @ChoicesWidget(choices = RDKitMoleculeColumnChoicesProvider.class)
    String m_inputColumnName;

    @Persist(configKey = "new_column_name")
    @Widget(title = "New column name", description = "The name of the new column to be added.")
    String m_newColumnName;

    @Persist(configKey = "remove_source_columns")
    @Widget(title = "Remove source column", description = "Option to remove the source column from the output table.")
    boolean m_removeSourceColumns = false;
}