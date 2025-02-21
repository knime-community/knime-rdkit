package org.rdkit.knime.nodes.molfragmenter;

import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.api.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.NumberInputWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.choices.ColumnChoicesProviderUtil.AllColumnChoicesProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;

/**
 * Settings for the RDKit Mol Fragmenter node using the webui framework.
 * 
 * @author Marc Lehner
 */
public final class RDKitMolFragmenterNodeSettings implements DefaultNodeSettings {

	@Persist(configKey = "input_column")
    @Widget(title = "Input Molecule Column",
            description = "Select the column containing the RDKit molecules (e.g., RDKit Mol, SMILES, or SDF).")
    @ChoicesWidget(choices = RDKitMoleculeColumnChoicesProvider.class)
    String m_inputColumn;

    @Persist(configKey = "min_path")
    @Widget(
        title = "Min Path Length",
        description = "Specify the minimum path length for the fragment generation."
    )
    @NumberInputWidget(min = 1, max = 10)
    int m_min_path = 4;

    @Persist(configKey = "max_path")
    @Widget(
        title = "Max Path Length",
        description = "Specify the maximum path length for the fragment generation."
    )
    @NumberInputWidget(min = 1, max = 10)
    int m_max_path = 7;
}