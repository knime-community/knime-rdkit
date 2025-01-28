package org.rdkit.knime.nodes.rdkit2molecule;

import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Section;
import org.knime.core.webui.node.dialog.defaultdialog.layout.After;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.field.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ValueSwitchWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.TextInputWidget;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Layout;

/**
 * Settings for the RDKit2MoleculeConverter node, using the new DefaultNodeSettings approach.
 * 
 * @author Marc Lehner
 */
public final class RDKit2MoleculeConverterNodeSettings implements DefaultNodeSettings {

    @Section(title = "Input configuration")
    interface InputSection {}

    @Section(title = "Output configuration")
    @After(InputSection.class)
    interface OutputSection {}

    @Section(title = "Format options", advanced = true)
    @After(OutputSection.class)
    interface FormatSection {}

    /**
     * Destination molecule formats (SMILES, SMARTS, SDF) as in the old code.
     */
    enum DestinationFormat {
        Smiles,
        Smarts,
        SDF
    }

    @Persist(configKey = "input_column")
    @Widget(title = "RDKit Mol column",
            description = "Select the column containing the RDKit molecule.")
    @TextInputWidget
    @Layout(InputSection.class)
    String m_inputColumn = "";

    @Persist(configKey = "new_column_name")
    @Widget(title = "New column name",
            description = "The name of the newly created column containing the converted molecule.")
    @TextInputWidget
    @Layout(OutputSection.class)
    String m_newColumnName = "";

    @Persist(configKey = "remove_source_columns")
    @Widget(title = "Remove source column",
            description = "If checked, the original input column will be removed from the output table.")
    @Layout(OutputSection.class)
    boolean m_removeSourceColumns = false;

    @Persist(configKey = "destination_format")
    @Widget(title = "Destination format",
            description = "Choose the output format for the molecule.")
    @ChoicesWidget(optional = false, choices = DestinationFormatChoices.class)
    @ValueSwitchWidget
    @Layout(FormatSection.class)
    DestinationFormat m_destinationFormat = DestinationFormat.SDF;

    /**
     * Provides the list of possible output formats as user-facing strings.
     */
    static final class DestinationFormatChoices implements ChoicesProvider {
        @Override
        public String[] choices(final DefaultNodeSettingsContext context) {
            return new String[] {"Smiles", "Smarts", "SDF"};
        }
    }
}