package org.rdkit.knime.nodes.descriptorcalculation;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.field.FieldBasedNodeSettingsPersistor;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.field.FieldNodeSettingsPersistor;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.field.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.SortListWidget; 
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.rdkit.knime.types.RDKitMolValue;
import org.knime.core.data.DataTableSpec;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings.DefaultNodeSettingsContext;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.util.filter.NameFilterConfiguration.EnforceOption;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Layout;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Section;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.field.EnumFieldPersistor;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.field.NodeSettingsPersistorWithConfigKey;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.field.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.setting.columnfilter.ColumnFilter;
import org.knime.core.webui.node.dialog.defaultdialog.setting.columnfilter.LegacyColumnFilterPersistor;
import org.knime.core.webui.node.dialog.defaultdialog.setting.selection.SelectionCheckboxesToSelectionModePersistor;
import org.knime.core.webui.node.dialog.defaultdialog.setting.columnselection.StringToColumnSelectionPersistor;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ColumnChoicesProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Label;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ValueSwitchWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Effect;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Effect.EffectType;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Predicate;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.PredicateProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Reference;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.ValueReference;

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
	
	static final class DescriptorPersistor extends SelectionCheckboxesToSelectionModePersistor {
		protected Descriptor fromString(final String s) {
			return Descriptor.valueOf(s);
		}
		
	}
	
	static final class DescriptorArrayPersistor implements FieldNodeSettingsPersistor<String[]> {
        private static final String KEY = "selectedDescriptors";

        @Override
        public String[] load(final NodeSettingsRO settings) throws InvalidSettingsException {
            return settings.getStringArray(KEY);
        }

        @Override
        public void save(final String[] obj, final NodeSettingsWO settings) {
            settings.addStringArray(KEY, obj);
        }

        @Override
        public String[] getConfigKeys() {
            return new String[]{KEY};
        }
    }
    

    @Persist(configKey = "input_column")
    @Widget(
        title = "Molecule Column",
        description = "Select the input column that contains the molecule data "
                    + "(e.g. SMILES, SMARTS, or SDF) for descriptor calculation."
    )
    @ChoicesWidget(choices = RDKitMoleculeColumnChoicesProvider.class)
    String m_inputColumn = "";

    
    @Persist(configKey = "selectedDescriptors", customPersistor = DescriptorArrayPersistor.class)
    @Widget(
        title = "Selected Descriptors",
        description = "The descriptors you wish to calculate."
    )
    @ChoicesWidget(choices = DescriptorChoices.class)
    String[] m_selectedDescriptors = new String[0];
}