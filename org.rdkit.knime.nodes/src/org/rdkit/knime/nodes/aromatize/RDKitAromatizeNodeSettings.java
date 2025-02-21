package org.rdkit.knime.nodes.aromatize;

import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.api.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.choices.ColumnChoicesProviderUtil.AllColumnChoicesProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;

/**
 * Settings for the RDKit Aromatize node using the webui framework.
 * 
 * @author Marc Lehner, KNIME GmbH, Zurich, Switzerland
 */
public final class RDKitAromatizeNodeSettings implements DefaultNodeSettings {

    @Persist(configKey = "input_column")
    @Widget(title = "RDKit Mol column", description = "Select the column containing RDKit molecules for aromatization.")
    @ChoicesWidget(choices = RDKitMoleculeColumnChoicesProvider.class)
    String m_inputColumnName;

    @Persist(configKey = "new_column_name")
    @Widget(title = "New column name", description = "Specify the name for the new column that will contain the aromatized molecules.")
    String m_newColumnName;

    @Persist(configKey = "remove_source_columns")
    @Widget(title = "Remove source column", description = "Check this option to remove the original source column from the output table.")
    boolean m_removeSourceColumns = false;
}