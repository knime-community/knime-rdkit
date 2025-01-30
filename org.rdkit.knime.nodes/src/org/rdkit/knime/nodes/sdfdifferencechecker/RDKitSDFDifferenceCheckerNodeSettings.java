package org.rdkit.knime.nodes.sdfdifferencechecker;

import java.util.stream.Stream;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings.DefaultNodeSettingsContext;
import org.knime.core.webui.node.dialog.defaultdialog.layout.After;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Layout;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Section;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ColumnChoicesProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.NumberInputWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.choices.ColumnChoicesProviderUtil.AllColumnChoicesProvider;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.field.Persist;

/**
 * Settings for the RDKit2MoleculeConverter node, using the new DefaultNodeSettings approach.
 * 
 * @author Marc Lehner, KNIME GmbH, Zurich, Switzerland
 */
public final class RDKitSDFDifferenceCheckerNodeSettings implements DefaultNodeSettings {
	
	public static final class AllColumnChoicesProvider2 implements ColumnChoicesProvider {
        @Override
        public DataColumnSpec[] columnChoices(final DefaultNodeSettingsContext context) {
            return context.getDataTableSpec(1) //
                .map(DataTableSpec::stream) //
                .orElseGet(Stream::empty) //
                .toArray(DataColumnSpec[]::new);
        }
    }
	
	/** The sections in which we group our settings. */
    @Section(title = "Input")
    interface InputSection {}
    
    @Section(title = "Comparison Settings")
    @After(InputSection.class)
    interface ComparisonSettingsSection {}
    
    // ------------------------------------------------------------------------
    // INPUT SECTION
    // ------------------------------------------------------------------------

    @Persist(configKey = "input_column_1")
    @Widget(title = "SDF Column (Table 1)",
            description = "Select the SDF (or string) column from the first input table.")
    @ChoicesWidget(choices = AllColumnChoicesProvider.class)
    @Layout(InputSection.class)
    String m_inputColumn1;

    @Persist(configKey = "input_column_2")
    @Widget(title = "SDF Column (Table 2)",
            description = "Select the SDF (or string) column from the second input table.")
    @ChoicesWidget(choices = AllColumnChoicesProvider2.class)
    @Layout(InputSection.class)
    String m_inputColumn2;

    // ------------------------------------------------------------------------
    // Comparison Settings
    // ------------------------------------------------------------------------

    @Persist(configKey = "tolerance")
    @Widget(title = "Tolerance for floating-point comparisons",
            description = "Differences smaller than this threshold will be ignored.")
    @NumberInputWidget
    @Layout(ComparisonSettingsSection.class)
    double m_tolerance = 0.1;

    @Persist(configKey = "failOnFirstDifference")
    @Widget(title = "Fail on first difference",
            description = "If checked, the node execution stops immediately at the first encountered difference.")
    @Layout(ComparisonSettingsSection.class)
    boolean m_failOnFirstDifference = true;

    @Persist(configKey = "limitConsoleOutput")
    @Widget(title = "Limit console output",
            description = "The maximum number of differences to be logged in the console.")
    @Layout(ComparisonSettingsSection.class)
    int m_limitConsoleOutput = 3;

}