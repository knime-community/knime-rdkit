package org.rdkit.knime.nodes.removehs;

import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Section;
import org.knime.core.webui.node.dialog.defaultdialog.layout.After;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Layout;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesWidget;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.field.Persist;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;

/**
 * Settings for the RDKit Remove Hydrogens node using the webui framework.
 * 
 * @author Marc Lehner, KNIME GmbH, Zurich, Switzerland
 */
public final class RDKitRemoveHsNodeSettings implements DefaultNodeSettings {

    /*----------------------------------------------------------
     * Sections. Since it's a fairly small node, we can keep it 
     * to two main sections (Input, Output). 
     *----------------------------------------------------------*/
    @Section(title = "Input", advanced = false)
    interface InputSection {
    }

    @Section(title = "Output", advanced = false)
    @After(InputSection.class)
    interface OutputSection {
    }

    /*----------------------------------------------------------
     * Fields for node settings.
     *----------------------------------------------------------*/

    @Persist(configKey = "input_column")
    @Widget(title = "Input Molecule Column",
        description = "Select the column containing the RDKit molecule to remove hydrogens from.")
    @ChoicesWidget(choices = RDKitMoleculeColumnChoicesProvider.class)
    @Layout(InputSection.class)
    String m_inputColumn;

    @Persist(configKey = "new_column_name")
    @Widget(title = "New Column Name",
        description = "Specify the name for the output column containing the molecule with H-atoms removed.")
    @Layout(OutputSection.class)
    String m_newColumnName;

    @Persist(configKey = "remove_source_columns")
    @Widget(title = "Remove Original Column",
        description = "If checked, the original input column is removed from the output table.")
    @Layout(OutputSection.class)
    boolean m_removeSourceColumns = false;
}