package org.rdkit.knime.nodes.molextractor;

import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.api.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.choices.ColumnChoicesProviderUtil.AllColumnChoicesProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Effect;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Effect.EffectType;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.PredicateProvider.PredicateInitializer;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Predicate;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.PredicateProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Reference;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.ValueReference;

public final class RDKitMoleculeExtractorNodeSettings implements DefaultNodeSettings {

    interface ReferenceColumnRef extends Reference {}

    @Persist(configKey = "input_molecules")
    @Widget(title = "Input Molecule Column",
            description = "Select the column containing the RDKit molecules (e.g., RDKit Mol, SMILES, or SDF).")
    @ChoicesWidget(choices = RDKitMoleculeColumnChoicesProvider.class)
    String m_inputColumn;
    
    @Persist(configKey = "input_molecules_format")
	@Widget(title = "Input Molecule Format", description = "Specify the format of the input molecules (e.g., RDKit Mol, SMILES, or SDF).")
    String m_input_molecules_format;

    @Persist(configKey = "input_column")
    @Widget(title = "Reference Column",
            description = "Select a reference column (e.g., an ID column). The row ID can also be chosen.")
    @ChoicesWidget(choices = AllColumnChoicesProvider.class, showRowKeysColumn = true)
    String m_inputRefColumn;
    
//    @Persist(configKey = "input_ref_column")
//	@Widget(title = "Input Reference Column", description = "Select the column containing the reference data.")
//    String m_input_ref_column;

    @Persist(configKey = "output_mol_name")
    @Widget(title = "Output Molecule Column Name",
            description = "Specify the name of the new column that will contain the extracted molecules.")
    String m_outputMolName = "Molecules";

    @Persist(configKey = "output_ref_name")
    @Widget(title = "Output Reference Column Name",
            description = "Specify the name of the new column that will contain the copied reference data.")
    String m_outputRefName = "Reference";

    @Persist(configKey = "sanitize_fragments")
    @Widget(title = "Sanitize Fragments",
            description = "If enabled, the extracted fragments will be sanitized.", advanced = true)
    boolean m_sanitizeFragments = false;

    @Persist(configKey = "error_handling")
    @Widget(title = "Error Handling",
            description = "Specify how to handle errors during molecule extraction (e.g., fail, skip, or create missing cells).", advanced = true)
    EmptyMoleculeHandling m_errorHandling;

    @Persist(configKey = "empty_cell_handling")
    @Widget(title = "Empty Cell Handling",
            description = "Specify how to handle empty (missing) cells in the input (e.g., fail, skip, or create missing cells).", advanced = true)
    EmptyMoleculeHandling m_emptyCellHandling;

    @Persist(configKey = "empty_molecule_handling")
    @Widget(title = "Empty Molecule Handling",
            description = "Specify how to handle empty molecules (e.g., fail, skip, or create missing cells).", advanced = true)
    EmptyMoleculeHandling m_emptyMoleculeHandling;
}