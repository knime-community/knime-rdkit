package org.rdkit.knime.nodes.descriptorcalculation;

import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.api.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.api.Persistor;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.persistors.settingsmodel.EnumSettingsModelStringPersistor;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.core.webui.node.dialog.defaultdialog.setting.selection.SelectionCheckboxesToSelectionModeMigration;
import java.util.Arrays;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;

/**
 * Settings for the Descriptor Calculation node using the webui framework.
 * 
 * @author Marc Lehner, KNIME GmbH, Zurich, Switzerland
 */
public final class DescriptorCalculationNodeSettings implements DefaultNodeSettings {

    
	static final class DescriptorChoices implements ChoicesProvider {
		@Override
		public String[] choices(final DefaultNodeSettingsContext context) {
			return Arrays.stream(Descriptor.values()).map(Enum::name).toArray(String[]::new);
		}
	}
	
	static final class DescriptorPersistor extends SelectionCheckboxesToSelectionModeMigration {
		protected Descriptor fromString(final String s) {
			return Descriptor.valueOf(s);
		}
		
	}
	
//	static final class DescriptorArrayPersistor implements FieldNodeSettingsPersistor<String[]> {
//        private static final String KEY = "selectedDescriptors";
//
//        @Override
//        public String[] load(final NodeSettingsRO settings) throws InvalidSettingsException {
//            return settings.getStringArray(KEY);
//        }
//
//        @Override
//        public void save(final String[] obj, final NodeSettingsWO settings) {
//            settings.addStringArray(KEY, obj);
//        }
//
//        @Override
//        public String[] getConfigKeys() {
//            return new String[]{KEY};
//        }
//    }
    

    @Persist(configKey = "input_column")
    @Widget(
        title = "Molecule Column",
        description = "Select the input column that contains the molecule data "
                    + "(e.g. SMILES, SMARTS, or SDF) for descriptor calculation."
    )
    @ChoicesWidget(choices = RDKitMoleculeColumnChoicesProvider.class)
    String m_inputColumn = "";

    
    @Persistor(value = EnumSettingsModelStringPersistor.class)
    @Widget(
        title = "Selected Descriptors",
        description = "The descriptors you wish to calculate."
    )
    @ChoicesWidget(choices = DescriptorChoices.class)
    String[] m_selectedDescriptors = new String[0];
}