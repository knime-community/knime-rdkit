package org.rdkit.knime.nodes.iupac2rdkit;

import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.api.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.choices.ColumnChoicesProviderUtil.AllColumnChoicesProvider;

/**
 * Settings for the RDKit IUPAC to RDKit node using the webui framework.
 * 
 * @author Marc Lehner, KNIME GmbH, Zurich, Switzerland
 */
public final class RDKitIUPACToRDKitNodeSettings implements DefaultNodeSettings {

    @Persist(configKey = "input_column")
    @Widget(title = "IUPAC Name Column",
            description = "Select the input column that contains the IUPAC names to be converted to RDKit molecules.")
    @ChoicesWidget(choices = AllColumnChoicesProvider.class)
    String m_inputColumn;

    @Persist(configKey = "new_column_name")
    @Widget(title = "New Column Name",
            description = "Specify the name for the new column that will contain the RDKit molecules generated from the IUPAC names.")
    String m_newColumnName;
}