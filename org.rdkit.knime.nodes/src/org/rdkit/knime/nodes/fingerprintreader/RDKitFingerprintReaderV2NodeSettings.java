package org.rdkit.knime.nodes.fingerprintreader;

import org.knime.base.node.io.filehandling.webui.reader.CommonReaderNodeSettings.BaseSettings.FileSelectionRef;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Layout;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Section;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.api.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.api.Persistor;
import org.knime.core.webui.node.dialog.defaultdialog.setting.fileselection.FileSelection;
import org.knime.core.webui.node.dialog.defaultdialog.setting.fileselection.LegacyReaderFileSelectionPersistor;
import org.knime.core.webui.node.dialog.defaultdialog.widget.FileReaderWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;

/**
 * Settings for the RDKit Fingerprint Reader Node using the new DefaultNodeSettings approach.
 */
@SuppressWarnings("restriction")
public final class RDKitFingerprintReaderV2NodeSettings implements DefaultNodeSettings {

    @Section(title = "File Input")
    interface FileInputSection {
    }

    @Persistor(value = LegacyReaderFileSelectionPersistor.class)
    @Widget(title = "Fingerprint File Path",
            description = "Select the file path for the fingerprint file to be read.")
    @Modification.WidgetReference(FileSelectionRef.class)
    @FileReaderWidget
    @Layout(FileInputSection.class)
    public FileSelection m_filePath = new FileSelection();

    @Section(title = "Options")
    interface OptionsSection {
    }

    @Persist(configKey = "use_file_ids_as_row_ids")
    @Widget(title = "Use IDs from File as Row IDs",
            description = "If selected, the IDs read from the fingerprint file will be used as row IDs.")
    @Layout(OptionsSection.class)
    boolean m_useIdsFromFileAsRowIds = false;
}