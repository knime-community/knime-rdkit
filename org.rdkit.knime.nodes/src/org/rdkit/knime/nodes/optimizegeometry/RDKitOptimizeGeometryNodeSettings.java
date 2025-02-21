package org.rdkit.knime.nodes.optimizegeometry;

import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.api.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.choices.ColumnChoicesProviderUtil.AllColumnChoicesProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Effect;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Effect.EffectType;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.PredicateProvider.PredicateInitializer;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Predicate;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.PredicateProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Reference;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.ValueReference;
import org.rdkit.knime.types.RDKitMolValue;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;

/**
 * Settings for the RDKitOptimizeGeometry node, using the webui framework.
 * 
 * @author Marc Lehner
 */
public final class RDKitOptimizeGeometryNodeSettings implements DefaultNodeSettings {

    @Persist(configKey = "input_column")
    @Widget(title = "Input Molecule Column",
            description = "Select the input column containing RDKit molecules (e.g., RDKit Mol, SMILES, or SDF).")
    @ChoicesWidget(choices = RDKitMoleculeColumnChoicesProvider.class)
    String m_inputColumn;

    @Persist(configKey = "new_molecule_column_name")
    @Widget(title = "Output Molecule Column",
            description = "Specify the name of the new column that will contain the optimized molecules.")
    String m_outputColumn;

    @Persist(configKey = "forceField")
	@Widget(title = "Force Field", 
			description = "Select the force field to be used for the optimization.")
        ForceFieldType m_forceField = ForceFieldType.MMFF94;
    
    @Persist(configKey = "remove_source_columns")
    @Widget(title = "Remove Source Column",
            description = "If enabled, the source column will be removed from the output table.")
    boolean m_removeSourceColumn = false;
    
    @Persist(configKey = "new_converge_column_name")
    @Widget(title = "Convergence Column Name",
            description = "Specify the name of the new column that will contain the convergence information.")
    String m_convergeColumn = "Converged";
    
    @Persist(configKey = "new_energy_column_name")
    @Widget(title = "Energy Column Name",
            description = "Specify the name of the new column that will contain the energy information.")
    String m_energyColumn = "Energy";

    @Persist(configKey = "iterations")
    @Widget(title = "Maximum Iterations",
            description = "Specify the maximum number of iterations for the optimization process.", 
            advanced = true)
    int m_maxIterations = 1000;   

    @Persist(configKey = "remove_starting_coordinates")
    @Widget(title = "Remove Starting Coordinates",
            description = "If enabled, the starting coordinates will be removed from the output table.",
            advanced = true)
    boolean m_removeStartingCoordinates = false;
}