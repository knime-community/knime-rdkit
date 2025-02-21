package org.rdkit.knime.nodes.addconformers;

import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings.DefaultNodeSettingsContext;
import org.knime.core.webui.node.dialog.defaultdialog.layout.After;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Layout;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Section;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.api.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.api.Persistor;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.persistors.settingsmodel.SettingsModelColumnNamePersistor;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.rdkit.knime.types.RDKitMolValue;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;
/*
 * import org.knime.core.webui.node.dialog.defaultdialog.persistence.field.FieldNodeSettingsPersistor;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.field.settingsmodel.SettingsModelColumnNamePersistor;
 */

/**
 * Settings for the RDKit AddConformers node using the webui framework.
 * 
 * @author Marc Lehner, KNIME GmbH, Zurich, Switzerland
 */
public final class RDKitAddConformersNodeSettings implements DefaultNodeSettings {
    /**
     * Provides choices for the reference column (String or Double), and also allows using the row ID.
     */
    static final class ReferenceColumns implements ChoicesProvider {
        @Override
        public String[] choices(final DefaultNodeSettingsContext context) {
            return context.getDataTableSpec(0)
                    .map(spec -> spec.stream()
                        .filter(c -> c.getType().isCompatible(StringValue.class)
                                  || c.getType().isCompatible(DoubleValue.class))
                        .map(col -> col.getName())
                        .toArray(String[]::new))
                    .orElse(new String[0]);
        }
    }
    
    @Section(title = "Input")
    interface InputSection {    	
    }

   
    @Persist(configKey = "input_mol_column")
    @Widget(title = "RDKit Mol column", description = "Select the input column that contains the RDKit Molecule.")
    @ChoicesWidget(choices = RDKitMoleculeColumnChoicesProvider.class)
    @Layout(InputSection.class)
    String m_inputMolColumn;

    @Persistor(value = SettingsModelColumnNamePersistor.class)
    @Widget(title = "Reference column",
            description = "Select a reference column (String or Double). The row ID can also be chosen.")
    @ChoicesWidget(choices = ReferenceColumns.class, showRowKeysColumn = true)
    @Layout(InputSection.class)
    String m_inputRefColumn;
    
    @Section(title = "Conformer Calculation")
    @After(InputSection.class)
    interface ConformerCalculationSection {
    }

    @Persist(configKey = "numberConformers")
    @Widget(title = "Number of conformers", description = "Number of conformers to generate per input molecule.")
    @Layout(ConformerCalculationSection.class)
    int m_numberConformers;

    @Persist(configKey = "maxIterations")
    @Widget(title = "Maximum iterations",
            description = "Maximum number of attempts to generate conformers. 0 uses a default rule.")
    @Layout(ConformerCalculationSection.class)
    int m_maxIterations;

    @Persist(configKey = "seed")
    @Widget(title = "Random seed",
            description = "Seed for random number generation. Use -1 for a non-deterministic seed.")
    @Layout(ConformerCalculationSection.class)
    int m_seed;

    @Persist(configKey = "pruneRmsThreshold")
    @Widget(title = "RMS threshold for pruning",
            description = "Conformers within this RMS threshold of an existing conformer will be discarded.")
    @Layout(ConformerCalculationSection.class)
    double m_pruneRmsThreshold;
    
    @Section(title = "Output")
    @After(ConformerCalculationSection.class)
    interface OutputSection{
    }
    
    @Persist(configKey = "output_mol_name")
    @Widget(title = "Molecule output column name",
            description = "Name of the new column containing the generated conformers.")
    @Layout(OutputSection.class)
    String m_outputMolName;

    @Persist(configKey = "output_ref_name")
    @Widget(title = "Reference output column name",
            description = "Name of the new column containing the copied reference data.")
    @Layout(OutputSection.class)
    String m_outputRefName;
    
    
    
    
    @Section(title = "Advanced Conformer Calculation Options", advanced = true)
    @After(OutputSection.class)
    interface AdvancedSection{
    }

    @Persist(configKey = "useRandomCoordinates")
    @Widget(title = "Use random coordinates",
            description = "If checked, random coordinates are used as a starting point instead of distance geometry.")
    @Layout(AdvancedSection.class)
    boolean m_useRandomCoordinates;

    @Persist(configKey = "useSmallRingTorsions")
    @Widget(title = "Use small ring torsions",
            description = "Use experimental torsion angle terms for small rings.", advanced = true)
    @Layout(AdvancedSection.class)
    boolean m_useSmallRingTorsions;

    @Persist(configKey = "useMacrocycleTorsions")
    @Widget(title = "Use macrocycle torsions",
            description = "Use experimental torsion angle terms for macrocycles.", advanced = true)
    @Layout(AdvancedSection.class)
    boolean m_useMacrocycleTorsions;

    @Persist(configKey = "useMacrocycle14")
    @Widget(title = "Use 1-4 heuristics for macrocycles",
            description = "Use 1-4 distance bound heuristics for macrocycles.", advanced = true)
    @Layout(AdvancedSection.class)
    boolean m_useMacrocycle14;

    @Persist(configKey = "forceTransAmides")
    @Widget(title = "Force trans amides",
            description = "Force amide bonds to be trans.", advanced = true)
    @Layout(AdvancedSection.class)
    boolean m_forceTransAmides;

    @Persist(configKey = "onlyHeavyAtomsForRMS")
    @Widget(title = "Only heavy atoms for RMS",
            description = "Only heavy atoms are used when calculating RMS values.", advanced = true)
    @Layout(AdvancedSection.class)
    boolean m_onlyHeavyAtomsForRMS;

    @Persist(configKey = "useSymmetryForPruning")
    @Widget(title = "Use symmetry for pruning",
            description = "Use molecular symmetry when pruning conformers.", advanced = true)
    @Layout(AdvancedSection.class)
    boolean m_useSymmetryForPruning;

    @Persist(configKey = "embedFragmentsSeparately")
    @Widget(title = "Embed fragments separately",
            description = "If checked, fragments are embedded separately.", advanced = true)
    @Layout(AdvancedSection.class)
    boolean m_embedFragmentsSeparately;

    @Persist(configKey = "ETversion")
    @Widget(title = "Experimental torsion version",
            description = "Version of the experimental torsion angle preferences (1 or 2).", advanced = true)
    @Layout(AdvancedSection.class)
    int m_ETversion;

    @Persist(configKey = "enforceChirality")
    @Widget(title = "Enforce chirality",
            description = "If checked, the preservation of input chirality is enforced.", advanced = true)
    @Layout(AdvancedSection.class)
    boolean m_enforceChirality;

    @Persist(configKey = "useExpTorsionAngles")
    @Widget(title = "Use experimental torsion angles",
            description = "If checked, experimental torsion angle terms are used.", advanced = true)
    @Layout(AdvancedSection.class)
    boolean m_useExpTorsionAngles;

    @Persist(configKey = "useBasicKnowledge")
    @Widget(title = "Use basic knowledge",
            description = "If checked, basic chemical knowledge terms are used (e.g., planar aromatic atoms).", advanced = true)
    @Layout(AdvancedSection.class)
    boolean m_useBasicKnowledge;

    @Persist(configKey = "boxSizeMultiplier")
    @Widget(title = "Box size multiplier",
            description = "Multiplier for the size of the box when starting from random coordinates.", advanced = true)
    @Layout(AdvancedSection.class)
    double m_boxSizeMultiplier;
    
    @Persist(configKey = "cleanup_with_uff")
    @Widget(title = "Perform UFF cleanup",
            description = "If checked, a UFF cleanup is performed after generating the conformers.", advanced = true)
    @Layout(AdvancedSection.class)
    boolean m_cleanupWithUff;

}
