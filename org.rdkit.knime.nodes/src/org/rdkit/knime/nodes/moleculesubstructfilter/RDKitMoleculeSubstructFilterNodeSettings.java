package org.rdkit.knime.nodes.moleculesubstructfilter;

import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.api.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Label;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ValueSwitchWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.choices.ColumnChoicesProviderUtil.AllColumnChoicesProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Effect;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Effect.EffectType;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.PredicateProvider.PredicateInitializer;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Predicate;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.PredicateProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Reference;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.ValueReference;
import org.rdkit.knime.types.RDKitMolValue;

/**
 * Settings for the RDKit Molecule Substructure Filter node using the webui
 * framework.
 * 
 * @author Marc Lehner
 */
public final class RDKitMoleculeSubstructFilterNodeSettings implements DefaultNodeSettings {

    interface UseChiralityRef extends Reference<Boolean> {}

    static final class UseChiralityTrue implements PredicateProvider {
		@Override
        public Predicate init(PredicateInitializer i) {
			return i.getBoolean(UseChiralityRef.class).isTrue();
		}
    }
    
    interface MartchingCriteriaRef extends Reference<RDKitMoleculeSubstructFilterNodeModel.MatchingCriteria> {}
    
    static final class MatchingCriteriaAtLeast implements PredicateProvider {
        @Override
        public Predicate init(PredicateInitializer i) {
            //return i.getEnum(MartchingCriteriaRef.class).isOneOf(RDKitMoleculeSubstructFilterNodeModel.MatchingCriteria.AtLeast);
        	//return i.getEnum(MartchingCriteriaRef.class).toString() == RDKitMoleculeSubstructFilterNodeModel.MatchingCriteria.AtLeast.toString();
        	return i.getEnum(MartchingCriteriaRef.class).isOneOf(RDKitMoleculeSubstructFilterNodeModel.MatchingCriteria.AtLeast);
        }
    }


    @Persist(configKey = "input_column")
    @Widget(title = "Input Molecule Column",
            description = "Select the input column containing RDKit molecules (e.g., RDKit Mol, SMILES, or SDF).")
    @ChoicesWidget(choices = AllColumnChoicesProvider.class)
    String m_inputColumn;

    @Persist(configKey = "query_column")
    @Widget(title = "Query Molecule Column",
            description = "Select the column containing the query molecules (e.g., SMARTS or RDKit Mol).")
    @ChoicesWidget(choices = AllColumnChoicesProvider.class)
    String m_queryColumn;

    @Persist(configKey = "use_chirality")
    @Widget(title = "Use Stereochemistry",
            description = "If enabled, stereochemistry will be considered during substructure matching.")
    @ValueReference(UseChiralityRef.class)
    boolean m_useChirality = false;

    @Persist(configKey = "useEnhancedStereo")
    @Widget(title = "Use Enhanced Stereochemistry",
            description = "If enabled, enhanced stereochemistry will be used during substructure matching.")
    @Effect(type = EffectType.SHOW, predicate = UseChiralityTrue.class)
    boolean m_useEnhancedStereo = false;

    @Persist(configKey = "matching")
    @Widget(title = "Matching Criteria",
            description = "Specify the criteria for matching substructures (e.g., All, Exact, At Least).")
    @ValueReference(MartchingCriteriaRef.class)
    @ValueSwitchWidget
    RDKitMoleculeSubstructFilterNodeModel.MatchingCriteria m_matchingCriteria;

    @Persist(configKey = "minimumMatches")
    @Widget(title = "Minimum Matches",
            description = "Specify the minimum number of matches required when using the 'At Least' matching criteria.")
    @Effect(type = EffectType.SHOW, predicate = MatchingCriteriaAtLeast.class)
    int m_minimumMatches = 1;

    @Persist(configKey = "new_column_name")
    @Widget(title = "New Column Name",
            description = "Specify the name of the new column that will contain the matching substructures.")
    String m_newColumnName = "Matched Substructs";

    @Persist(configKey = "fp_screening_threshold")
    @Widget(title = "Fingerprint Screening Threshold",
            description = "Specify the threshold for enabling fingerprint screening optimization. Use 0 to disable.")
    int m_fpScreeningThreshold = RDKitMoleculeSubstructFilterNodeModel.DEFAULT_FINGERPRINT_SCREENING_THRESHOLD;

    @Persist(configKey = "row_key_match_info")
    @Widget(title = "Use Row Keys for Match Information",
            description = "If enabled, row keys will be used as substructure match information. Otherwise, row indices will be used.")
    boolean m_rowKeyMatchInfo = true;
}