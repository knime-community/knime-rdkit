package org.rdkit.knime.nodes.inchi2rdkit;

import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.field.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.choices.ColumnChoicesProviderUtil.AllColumnChoicesProvider;

/**
 * Settings for the RDKit InChI to Molecule node using the webui framework.
 * 
 * @author Marc Lehner, KNIME GmbH, Zurich, Switzerland
 */
public final class RDKitInChI2MoleculeNodeSettings implements DefaultNodeSettings {

    @Persist(configKey = "input_column")
    @Widget(title = "InChI Code Column",
            description = "The column containing InChI codes to be converted into RDKit Molecules.")
    @ChoicesWidget(choices = AllColumnChoicesProvider.class)
    String m_inputColumn;

    @Persist(configKey = "new_column_name")
    @Widget(title = "New RDKit Molecule Column Name",
            description = "Name for the new column containing the RDKit Molecules generated from InChI codes.")
    String m_newMolColumnName;

    @Persist(configKey = "remove_source_columns")
    @Widget(title = "Remove Source Column",
            description = "If checked, the original InChI code column will be removed from the output table.")
    boolean m_removeSourceColumns = false;

    @Persist(configKey = "sanitize")
    @Widget(title = "Sanitize Molecule",
            description = "If checked, the generated RDKit Molecule will be sanitized.")
    boolean m_sanitize = true;

    @Persist(configKey = "remove_hydrogens")
    @Widget(title = "Remove Hydrogens",
            description = "If checked, explicit hydrogens will be removed from the generated RDKit Molecule.")
    boolean m_removeHydrogens = true;

    @Persist(configKey = "new_extra_info_column_name_prefix")
    @Widget(title = "Extra Information Column Name Prefix",
            description = "Prefix for the column names containing extra information about the InChI conversion process.")
    String m_extraInfoColumnNamePrefix;

    @Persist(configKey = "generate_return_code")
    @Widget(title = "Generate Return Code Column",
            description = "If checked, a column with the InChI conversion return code will be added.")
    boolean m_generateReturnCode = false;

    @Persist(configKey = "generate_message")
    @Widget(title = "Generate Message Column",
            description = "If checked, a column with messages from the InChI conversion process will be added.")
    boolean m_generateMessage = false;

    @Persist(configKey = "generate_log")
    @Widget(title = "Generate Log Column",
            description = "If checked, a column with logs from the InChI conversion process will be added.")
    boolean m_generateLog = false;
}