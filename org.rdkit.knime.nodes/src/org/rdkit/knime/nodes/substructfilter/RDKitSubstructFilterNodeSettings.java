package org.rdkit.knime.nodes.substructfilter;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Section;
import org.knime.core.webui.node.dialog.defaultdialog.layout.After;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Layout;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Label;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ValueSwitchWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.TextInputWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.PredicateProvider;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.field.FieldNodeSettingsPersistor;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.field.Persist;
import org.knime.core.node.InvalidSettingsException;

/**
 * New settings for the RDKit Substructure Filter node using the Web UI approach.
 * 
 * @author Marc Lehner, KNIME GmbH, Zurich, Switzerland
 */
public final class RDKitSubstructFilterNodeSettings implements DefaultNodeSettings {

    // We re-declare the match handling enum to store it with minimal overhead.
    public enum MatchHandling {
    	@Label("Do not add column with matching atoms")
        DoNotAddMatchColumn,
        @Label("Add column with atom list of first match")
        AddFirstMatchColumn,
        @Label("Add column with atom list of all matches (overlap possible)")
        AddAllFlattenedMatchColumn;

        @Override
        public String toString() {
            switch (this) {
            case DoNotAddMatchColumn:
                return "Do not add column with matching atoms";
            case AddFirstMatchColumn:
                return "Add column with atom list of first match";
            case AddAllFlattenedMatchColumn:
                return "Add column with atom list of all matches (overlap possible)";
            default:
                return super.toString();
            }
        }
    }

    /**
     * A simple persistor for the MatchHandling enumeration.
     */
    static final class MatchHandlingPersistor implements FieldNodeSettingsPersistor<MatchHandling> {

        private static final String KEY = "match_handling";

        @Override
        public MatchHandling load(final NodeSettingsRO settings) throws InvalidSettingsException {
            String storedVal = settings.getString(KEY);
            for (MatchHandling mh : MatchHandling.values()) {
                if (mh.name().equals(storedVal)) {
                    return mh;
                }
            }
            throw new InvalidSettingsException("Unknown match handling option: " + storedVal);
        }

        @Override
        public void save(final MatchHandling obj, final NodeSettingsWO settings) {
            settings.addString(KEY, obj.name());
        }

        @Override
        public String[] getConfigKeys() {
            return new String[]{KEY};
        }
    }

    ////////////////////////////////////////////////////////////////////////
    // Sections for the node
    ////////////////////////////////////////////////////////////////////////

    @Section(title = "Input Configuration")
    interface InputSection {}

    @Section(title = "Global Options")
    @After(InputSection.class)
    interface GlobalSection {}

    @Section(title = "Match Handling")
    @After(GlobalSection.class)
    interface MatchHandlingSection {}

    ////////////////////////////////////////////////////////////////////////

    @Persist(configKey = "input_column")
    @Widget(title = "RDKit Mol column",
        description = "Select the input column containing RDKit molecules to filter")
    @TextInputWidget
    @Layout(InputSection.class)
    String m_inputColumn = "";

    @Persist(configKey = "smarts_value")
    @Widget(title = "SMARTS query",
        description = "Specify the SMARTS pattern or substructure query to filter the input molecules")
    @TextInputWidget
    @Layout(InputSection.class)
    String m_smartsValue = "";

    @Persist(configKey = "exact_match")
    @Widget(title = "Do exact match",
        description = "If checked, the entire molecule must match the given SMARTS rather than just a substructure")
    @Layout(GlobalSection.class)
    boolean m_exactMatch;

    @Persist(configKey = "use_chirality")
    @Widget(title = "Use stereochemistry",
        description = "If checked, stereochemistry will be considered during substructure matching")
    @Layout(GlobalSection.class)
    boolean m_useChirality;

    // Enum for match handling
    @Persist(customPersistor = MatchHandlingPersistor.class)
    @Widget(title = "Match handling",
        description = "Defines how match details (atom lists) should be handled for matched substructures.")
    @Layout(MatchHandlingSection.class)
    MatchHandling m_matchHandling = MatchHandling.DoNotAddMatchColumn;

    @Persist(configKey = "new_match_column")
    @Widget(title = "Column name for matching atom list",
        description = "The name of the newly added column that will list matched atom indices if match handling is enabled.")
    @Layout(MatchHandlingSection.class)
    String m_newMatchColumn = "Matching Atom List";
}