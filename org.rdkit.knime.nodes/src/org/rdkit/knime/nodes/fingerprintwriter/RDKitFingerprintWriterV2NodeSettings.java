package org.rdkit.knime.nodes.fingerprintwriter;

import org.knime.base.node.io.filehandling.webui.reader.CommonReaderNodeSettings.BaseSettings.FileSelectionRef;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Layout;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Section;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.api.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.api.Persistor;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.persistors.settingsmodel.SettingsModelColumnNamePersistor;
import org.knime.core.webui.node.dialog.defaultdialog.setting.fileselection.FileSelection;
import org.knime.core.webui.node.dialog.defaultdialog.setting.fileselection.LegacyReaderFileSelectionPersistor;
import org.knime.core.webui.node.dialog.defaultdialog.widget.FileWriterWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.choices.ColumnChoicesProviderUtil.AllColumnChoicesProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesWidget;

public final class RDKitFingerprintWriterV2NodeSettings implements DefaultNodeSettings {

    @Section(title = "File Output")
    interface FileOutputSection {
    }

    @Persistor(value = LegacyReaderFileSelectionPersistor.class)
    @Widget(title = "Output File Path",
            description = "Select the file path for the output fingerprint file.")
    @Modification.WidgetReference(FileSelectionRef.class)
    @FileWriterWidget
    @Layout(FileOutputSection.class)
    public FileSelection m_outputFilePath = new FileSelection();
    

    @Section(title = "Column Selection")
    interface ColumnSelectionSection {
    }

    @Persist(configKey = "fps_column")
    @Widget(title = "Fingerprint Column",
            description = "Select the column containing the fingerprint data.")
    @ChoicesWidget(choices = AllColumnChoicesProvider.class)
    @Layout(ColumnSelectionSection.class)
    String m_fpsColumn;

    @Persistor(value = SettingsModelColumnNamePersistor.class)
    @Widget(title = "ID Column",
            description = "Select the column containing the ID data. The Row ID can also be chosen.")
    @ChoicesWidget(choices = AllColumnChoicesProvider.class)
    @Layout(ColumnSelectionSection.class)
    String m_idColumn = "<row-id>";
    
    @Section(title = "Options")
    interface OptionsSection {
    }

    @Persist(configKey = "suppress_time")
    @Widget(title = "Suppress Time in Header",
            description = "If selected, the time in the FPS file header will be suppressed.")
    @Layout(OptionsSection.class)
    boolean m_suppressTime = false;

//    static final class FingerprintColumnChoices implements ColumnChoicesProvider {
//        @Override
//        public DataColumnSpec[] columnChoices(DefaultNodeSettingsContext context) {
//            return context.getDataTableSpec(0)
//                    .map(spec -> spec.stream()
//                        .filter(c -> c.getType().isCompatible(BitVectorValue.class))
//                        .map(col -> col.getName())
//                        .toArray(String[]::new))
//                    .orElse(new String[0]);
//        }
//    }

//    static final class IDColumnChoices implements ColumnChoicesProvider {
//        @Override
//        public DataColumnSpec[] columnChoices(DefaultNodeSettingsContext context) {
//            return context.getDataTableSpec(0)
//                    .map(spec -> spec.stream()
//                        .filter(c -> c.getType().isCompatible(StringValue.class))
//                        .map(col -> col.getName())
//                        .toArray(String[]::new))
//                    .orElse(new String[0]);
//        }
//    }
}
