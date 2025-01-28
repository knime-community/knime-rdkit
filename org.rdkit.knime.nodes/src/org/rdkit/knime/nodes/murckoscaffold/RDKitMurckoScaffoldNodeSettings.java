package org.rdkit.knime.nodes.murckoscaffold;

import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.field.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.choices.ColumnChoicesProviderUtil.AllColumnChoicesProvider;

/**
 * Settings for the RDKit Murcko Scaffold node using the webui framework.
 * 
 * @author Marc Lehner, KNIME GmbH, Zurich, Switzerland
 */
public final class RDKitMurckoScaffoldNodeSettings implements DefaultNodeSettings {

    @Persist(configKey = "input_column")
    @Widget(title = "Input Molecule Column",
            description = "Select the input column containing RDKit molecules (e.g., RDKit Mol, SMILES, or SDF).")
    @ChoicesWidget(choices = AllColumnChoicesProvider.class)
    String m_inputColumn;

    @Persist(configKey = "new_column_name")
    @Widget(title = "Output Column Name",
            description = "Specify the name of the new column that will contain the Murcko scaffolds.")
    String m_outputColumn;

    @Persist(configKey = "remove_source_columns")
    @Widget(title = "Remove Source Column",
            description = "If enabled, the source column will be removed from the output table.")
    boolean m_includeSideChains = false;

    @Persist(configKey = "do_frameworks")
    @Widget(title = "Create frameworks",
            description = "If enabled, generate frameworks")
    boolean m_sideChainColumn;
}