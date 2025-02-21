package org.rdkit.knime.nodes.addcoordinates;

import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.api.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Label;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ValueSwitchWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.choices.ColumnChoicesProviderUtil.AllColumnChoicesProvider;

public final class RDKitAddCoordinatesNodeSettings implements DefaultNodeSettings {

    /**
     * Represents 2D or 3D coordinate generation.
     */
    public static enum CoordinateDimension {
        @Label("2D Coordinates")   COORD_2D,
        @Label("3D Coordinates")   COORD_3D
    }

    @Persist(configKey = "input_column")
    @Widget(title = "Input Column",
            description = "The column containing RDKit molecules (e.g. from RDKit Mol, SMILES, or SDF).")
    @ChoicesWidget(choices = AllColumnChoicesProvider.class)
    String m_inputColumn;

    @Persist(configKey = "new_column_name")
    @Widget(title = "New Column Name",
            description = "Name for the newly created column with generated coordinates.")
    String m_newColumnName;

    @Persist(configKey = "remove_source_columns")
    @Widget(title = "Remove Source Column",
            description = "If checked, the original source column is removed from the output table.")
    boolean m_removeSourceColumns = false;

    @Persist(configKey = "dimension")
    @Widget(title = "Coordinate Dimension",
            description = "Select '2D Coordinates' or '3D Coordinates' for the new molecule.")
    @ValueSwitchWidget
    CoordinateDimension m_dimension = CoordinateDimension.COORD_3D;

    @Persist(configKey = "template_smarts_value")
    @Widget(title = "Template SMARTS",
            description = "Optional SMARTS pattern used as a template for 2D coordinate generation. " +
                          "Only applied if '2D Coordinates' is selected.")
    String m_templateSmartsValue = "";
}
