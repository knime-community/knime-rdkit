package org.rdkit.knime.nodes.diversitypicker;

import org.knime.core.data.vector.bitvector.BitVectorValue;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Layout;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Section;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.field.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.rdkit.knime.types.RDKitMolValue;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;

/**
 * Settings for the RDKit Diversity Picker node using the webui framework.
 * 
 * @author Marc Lehner, KNIME GmbH, Zurich, Switzerland
 */
@SuppressWarnings("restriction")
public final class RDKitDiversityPickerNodeSettings implements DefaultNodeSettings {
    /**
     * Provides choices for the additional input column (molecule or fingerprint).
     */
    static final class AdditionalInputColumns implements ChoicesProvider {
        @Override
        public String[] choices(final DefaultNodeSettingsContext context) {
            return context.getDataTableSpec(1)
                    .map(spec -> spec.stream()
                        .filter(c -> c.getType().isCompatible(BitVectorValue.class) || c.getType().isCompatible(RDKitMolValue.class))
                        .map(col -> col.getName())
                        .toArray(String[]::new))
                    .orElse(new String[0]);
        }
    }

    @Section(title = "Input")
    interface InputSection {
    }

    @Persist(configKey = "input_column")
    @Widget(title = "Molecule or fingerprint column (table 1)",
            description = "Select the column from the first input table that contains the molecule or fingerprint data.")
    @ChoicesWidget(choices = RDKitMoleculeColumnChoicesProvider.class)
    @Layout(InputSection.class)
    String m_inputColumn;

    @Persist(configKey = "additional_input_column")
    @Widget(title = "Molecule or fingerprint column to bias away from (table 2)",
            description = "Select the column from the second input table that contains the molecule or fingerprint data to bias away from.")
    @ChoicesWidget(choices = AdditionalInputColumns.class)
    @Layout(InputSection.class)
    String m_additionalInputColumn;

    @Section(title = "Settings")
    interface SettingsSection {
    }

    @Persist(configKey = "num_picks")
    @Widget(title = "Number to pick",
            description = "Specify the number of diverse points to pick from the input data.")
    @Layout(SettingsSection.class)
    int m_numPicks;

    @Persist(configKey = "random_seed")
    @Widget(title = "Random seed",
            description = "Specify the random seed for reproducibility. Use -1 for a non-deterministic seed.")
    @Layout(SettingsSection.class)
    int m_randomSeed;
}
