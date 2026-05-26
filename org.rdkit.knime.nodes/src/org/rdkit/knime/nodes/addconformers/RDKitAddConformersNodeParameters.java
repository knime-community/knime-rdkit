/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 */

package org.rdkit.knime.nodes.addconformers;

import java.util.List;

import org.RDKit.EmbedParameters;
import org.RDKit.RDKFuncs;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.node.parameters.Advanced;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.legacy.persistence.PersistWithin;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.RowIDChoice;
import org.knime.node.parameters.widget.choices.StringOrEnum;
import org.knime.node.parameters.widget.choices.util.CompatibleColumnsProvider;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MaxValidation;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsNonNegativeValidation;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsPositiveIntegerValidation;
import org.rdkit.knime.util.RDKitLegacyPersistors.LegacyColumnNamePersistor;
import org.rdkit.knime.util.RDKitAdapterCellSupport;
import org.rdkit.knime.util.RDKitMoleculeColumnAutoGuessProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;
import org.rdkit.knime.util.RDKitResultColumnNameAutoGuessProvider;

/**
 * Node parameters for RDKit Add Conformers.
 *
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class RDKitAddConformersNodeParameters implements NodeParameters {

    /** Default RDKit ETKDGv3 parameters. */
    private static final EmbedParameters RDKIT_DEFAULTS = RDKFuncs.getETKDGv3();

    @Section(title = "Conformer Calculation")
    interface ConformerCalcSection {
    }

    @Widget(title = "RDKit mol column", description = "The input column with RDKit molecules.")
    @Persist(configKey = "input_mol_column")
    @ChoicesProvider(RDKitMoleculeColumnChoicesProvider.class)
    @ValueProvider(MoleculeColumnAutoGuessProvider.class)
    @ValueReference(MoleculeColumnRef.class)
    String m_moleculeInputColumnName;

    static final class MoleculeColumnRef implements ParameterReference<String> {
    }

    @Widget(title = "Reference column (e.g. an ID)", 
    	description = "The input column with reference data to be assigned to conformer rows.")
    @PersistWithin("input_ref_column")
    @Persistor(ReferenceColumnPersistor.class)
    @ChoicesProvider(ReferenceInputColumnChoicesProvider.class)
    StringOrEnum<RowIDChoice> m_referenceInputColumnName = new StringOrEnum<>(RowIDChoice.ROW_ID);
    
    @Layout(ConformerCalcSection.class)
    @Widget(title = "Number of conformers", description = "Number of conformers to be generated.")
    @Persist(configKey = "numberConformers")
    int m_numberOfConformers = RDKitAddConformersNodeDialog.DEFAULT_NUMBER_OF_CONFORMERS;

    @Layout(ConformerCalcSection.class)
    @Widget(title = "Maximum number of tries to generate conformers", description = """
            Maximum number of tries that the RDKit will use to generate conformers. \
            0 means that the RDKit code sets the value to 10 times the number of atoms.
            """)
    @NumberInputWidget(minValidation = IsNonNegativeValidation.class)
    @Persist(configKey = "maxIterations")
    int m_maxIterations = (int) Math.min(RDKIT_DEFAULTS.getMaxIterations(), Integer.MAX_VALUE);

    @Layout(ConformerCalcSection.class)
    @Widget(title = "Random seed", description = "A random seed to initialize random number generator.")
    @Persist(configKey = "seed")
    int m_randomSeed = RDKIT_DEFAULTS.getRandomSeed();

    @Layout(ConformerCalcSection.class)
    @Widget(title = "RMS threshold for keeping a conformer", description = "The RMS threshold for keeping a conformer.")
    @Persist(configKey = "pruneRmsThreshold")
    double m_pruneRmsThreshold = RDKIT_DEFAULTS.getPruneRmsThresh();

    @Layout(ConformerCalcSection.class)
    @Widget(title = "Column name for molecules with conformers", 
    	description = "The output column with the generated conformers.")
    @Persist(configKey = "output_mol_name")
    @ValueProvider(MoleculeOutputColumnNameProvider.class)
    @ValueReference(MoleculeOutputColumnNameRef.class)
    String m_moleculeOutputColumnName;
    
    static final class MoleculeOutputColumnNameRef implements ParameterReference<String> {
    }
    
    @Layout(ConformerCalcSection.class)
    @Widget(title = "Column name for copied reference data", 
    	description = "The output column with reference data taken from the input table.")
    @Persist(configKey = "output_ref_name")
    @ValueProvider(ReferenceOutputColumnNameProvider.class)
    @ValueReference(ReferenceOutputColumnNameRef.class)
    String m_referenceOutputColumnName;
    
    static final class ReferenceOutputColumnNameRef implements ParameterReference<String> {
    }
    
    @Layout(ConformerCalcSection.class)
    @Advanced
    @Widget(title = "Enforce the preservation of input chirality", description = """
            Ensures that the chirality/bond stereochemistry specified in the input structures is reflected in the \
            conformers.
            """)
    @Persist(configKey = "enforceChirality")
    boolean m_enforceChirality = RDKIT_DEFAULTS.getEnforceChirality();

    @Layout(ConformerCalcSection.class)
    @Advanced
    @Widget(title = "Use experimental torsion angle terms", description = """
            Use data from experimental crystal structures to refine some torsion angles. This uses the "ET" method \
            described in https://doi.org/10.1021/acs.jcim.5b00654 .
            """)
    @Persist(configKey = "useExpTorsionAngles")
    boolean m_useExpTorsionAngles = RDKIT_DEFAULTS.getUseExpTorsionAnglePrefs();

    @Layout(ConformerCalcSection.class)
    @Advanced
    @Widget(title = "Experimental torsion angles version",
        description = "Choose the version of the experimental torsion definitions to use. Possible values are 1 and 2.")
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class, maxValidation = IsAtMost2Validation.class)
    @Persist(configKey = "ETversion")
    int m_etVersion = (int) RDKIT_DEFAULTS.getETversion();

    static final class IsAtMost2Validation extends MaxValidation {

        @Override
        public double getMax() {
            return 2.0;
        }

    }
    
    @Layout(ConformerCalcSection.class)
    @Advanced
    @Widget(title = "Use experimental torsion angle terms for small rings", description = """
            Use data from experimental crystal structures to refine some torsion angles in small rings. This uses the \
            "srET" method described in https://doi.org/10.1021/acs.jcim.2c00043 .
            """)
    @Persist(configKey = "useSmallRingTorsions")
    boolean m_useSmallRingTorsions = RDKIT_DEFAULTS.getUseSmallRingTorsions();

    @Layout(ConformerCalcSection.class)
    @Advanced
    @Widget(title = "Use experimental torsion angle terms for macrocycles", description = """
            Use data from experimental crystal structures to refine some torsion angles in macrocycles. This uses the \
            method described in https://doi.org/10.1021/acs.jcim.0c00025 .
            """)
    @Persist(configKey = "useMacrocycleTorsions")
    boolean m_useMacrocycleTorsions = RDKIT_DEFAULTS.getUseMacrocycleTorsions();

    @Layout(ConformerCalcSection.class)
    @Advanced
    @Widget(title = "Use 1-4 distance bound heuristics for macrocycles", description = """
            Use empirical corrections to modify 1-4 distance bounds in macrocycles. This uses the method described in \
            https://doi.org/10.1021/acs.jcim.0c00025 .
            """)
    @Persist(configKey = "useMacrocycle14")
    boolean m_useMacrocycle14 = RDKIT_DEFAULTS.getUseMacrocycle14config();

    @Layout(ConformerCalcSection.class)
    @Advanced
    @Widget(title = "Use basic knowledge terms", description = """
            Apply "basic knowledge" constraints to planarize atoms which should be planar (e.g. aromatic atoms, \
            conjugated atoms). This uses the "K" method described in https://doi.org/10.1021/ci200159y .
            """)
    @Persist(configKey = "useBasicKnowledge")
    boolean m_useBasicKnowledge = RDKIT_DEFAULTS.getUseBasicKnowledge();

    @Layout(ConformerCalcSection.class)
    @Advanced
    @Widget(title = "Force amide bonds to be trans",
        description = "Applies constraints to force amide, ester, and related bonds to have trans configurations.")
    @Persist(configKey = "forceTransAmides")
    boolean m_forceTransAmides = RDKIT_DEFAULTS.getForceTransAmides();

    @Layout(ConformerCalcSection.class)
    @Advanced
    @Widget(title = "Only use heavy atoms when calculating RMS values",
        description = "When doing RMS pruning only heavy atoms will be used to calculate RMS.")
    @Persist(configKey = "onlyHeavyAtomsForRMS")
    boolean m_onlyHeavyAtomsForRMS = RDKIT_DEFAULTS.getOnlyHeavyAtomsForRMS();

    @Layout(ConformerCalcSection.class)
    @Advanced
    @Widget(title = "Use molecular symmetry when pruning conformers", description = """
            When doing RMS pruning molecular symmetry will be taken into account. Note that for reasons of computation \
            efficiency, when this option is enabled only the atoms' indices will be used to compute their symmetry class.
            """)
    @Persist(configKey = "useSymmetryForPruning")
    boolean m_useSymmetryForPruning = RDKIT_DEFAULTS.getUseSymmetryForPruning();

    @Layout(ConformerCalcSection.class)
    @Advanced
    @Widget(title = "Embed fragments separately", description = """
            Embed each of the molecule's fragments individually. Each fragment will be centered at the origin (i.e. the \
            fragments will overlap). When this option is not set the fragments will be placed randomly.
            """)
    @Persist(configKey = "embedFragmentsSeparately")
    boolean m_embedFragmentsSeparately = RDKIT_DEFAULTS.getEmbedFragmentsSeparately();

    @Layout(ConformerCalcSection.class)
    @Advanced
    @Widget(title = "Use random coordinates as a starting point instead of distance geometry",
        description = "Setting this flag will use random coordinates as a starting point instead of distance geometry.")
    @Persist(configKey = "useRandomCoordinates")
    boolean m_useRandomCoordinates = RDKIT_DEFAULTS.getUseRandomCoords();

    @Layout(ConformerCalcSection.class)
    @Advanced
    @Widget(title = "Multiplier for the size of the box for random coordinates",
        description = "Specifies a multiplier for the size of the box for random coordinates.")
    @Persist(configKey = "boxSizeMultiplier")
    double m_boxSizeMultiplier = RDKIT_DEFAULTS.getBoxSizeMult();

    @Layout(ConformerCalcSection.class)
    @Advanced
    @Widget(title = "Perform a cleanup using UFF (Universal force field) after calculation", description = """
            Set this flag to perform cleanup with UFF after the conformer's calculation. Just clear this flag to 
            output unprocessed conformers in case you want to use another force field for cleanup.
            """)
    @Persist(configKey = "cleanup_with_uff")
    boolean m_cleanupWithUff = RDKitAddConformersNodeDialog.DEFAULT_CLEANUP_WITH_UFF;
    
    static final class MoleculeColumnAutoGuessProvider extends RDKitMoleculeColumnAutoGuessProvider {
    	
        protected MoleculeColumnAutoGuessProvider() {
            super(MoleculeColumnRef.class, 0);
        }
        
    }
    
    static final class ReferenceInputColumnChoicesProvider extends CompatibleColumnsProvider {
    	
        ReferenceInputColumnChoicesProvider() {
            super(RDKitAdapterCellSupport.expandByAdaptableTypes(List.of(StringValue.class, DoubleValue.class)));
        }
        
    }
    
    static final class MoleculeOutputColumnNameProvider extends RDKitResultColumnNameAutoGuessProvider {

		protected MoleculeOutputColumnNameProvider() {
			super(MoleculeColumnRef.class, MoleculeOutputColumnNameRef.class, "(Conformers)");
		}
    	
    }
    
    static final class ReferenceOutputColumnNameProvider extends RDKitResultColumnNameAutoGuessProvider {

		protected ReferenceOutputColumnNameProvider() {
			super("Reference", MoleculeColumnRef.class, ReferenceOutputColumnNameRef.class);
		}
    	
    }
    
    static final class ReferenceColumnPersistor extends LegacyColumnNamePersistor {
    	
    	ReferenceColumnPersistor() {
		}
    	
    }
    
}
