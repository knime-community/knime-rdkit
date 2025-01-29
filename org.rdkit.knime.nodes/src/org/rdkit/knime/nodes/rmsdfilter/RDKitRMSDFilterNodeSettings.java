package org.rdkit.knime.nodes.rmsdfilter;

import org.knime.core.data.DataValue;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Section;
import org.knime.core.webui.node.dialog.defaultdialog.layout.After;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Layout;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.field.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.NumberInputWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ValueSwitchWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.rdkit.knime.types.RDKitMolValue;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.choices.ColumnChoicesProviderUtil.AllColumnChoicesProvider;

/**
 * Settings for the RDKit RMSD Filter node using the webui framework.
 * 
 * @author Marc Lehner, KNIME GmbH, Zurich, Switzerland
 * 
 */
public final class RDKitRMSDFilterNodeSettings implements DefaultNodeSettings {

    @Section(title = "Input")
    interface InputSection {
    }

    @After(InputSection.class)
    @Section(title = "RMSD Filter")
    interface FilterSection {
    }

    @Persist(configKey = "input_mol_column")
    @Widget(title = "Conformer column",
            description = "Select the input column containing RDKit molecules with conformers.")
    @ChoicesWidget(choices = RDKitMoleculeColumnChoicesProvider.class)
    @Layout(InputSection.class)
    String m_inputMolColumn;

    @Persist(configKey = "input_ref_column")
    @Widget(title = "Reference column",
            description = "Select the reference column, e.g. a unique ID for grouping conformers.")
    @ChoicesWidget(choices = AllColumnChoicesProvider.class)
    @Layout(InputSection.class)
    String m_inputRefColumn;

    @Persist(configKey = "rmsd_threshold")
    @Widget(title = "RMSD threshold",
            description = "If the best RMSD value between conformers is >= this threshold, the row is included in the first output table. Otherwise, it goes to the second output table.")
    @NumberInputWidget
    @Layout(FilterSection.class)
    double m_rmsdThreshold = 0.5;

    @Persist(configKey = "ignore_hs")
    @Widget(title = "Ignore Hs",
            description = "If selected, hydrogen atoms are removed before RMSD calculation for faster execution.")
    @Layout(FilterSection.class)
    boolean m_ignoreHs = false;
}