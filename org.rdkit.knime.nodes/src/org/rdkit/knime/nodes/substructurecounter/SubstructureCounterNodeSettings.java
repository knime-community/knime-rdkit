package org.rdkit.knime.nodes.substructurecounter;

import java.util.stream.Stream;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeDialog;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings.DefaultNodeSettingsContext;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Section;
import org.knime.core.webui.node.dialog.defaultdialog.layout.After;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Layout;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.choices.ColumnChoicesProviderUtil.AllColumnChoicesProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ValueSwitchWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ColumnChoicesProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Label;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.BooleanReference;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Effect;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Effect.EffectType;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.PredicateProvider;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.field.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Reference;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.ValueReference;
import org.knime.core.webui.node.dialog.defaultdialog.widget.TextInputWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Predicate;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.PredicateProvider.PredicateInitializer;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;

/**
 * Settings for the the RDKit Substructure Counter node using the webui framework.
 * .
 * @author Marc Lehner, KNIME GmbH, Zurich, Switzerland
 */
@SuppressWarnings("restriction")
public final class SubstructureCounterNodeSettings implements DefaultNodeSettings {

    ///////////////////////////////////////////////////////////////////////////
    // Creating sections for structuring the dialog (similar to the older grouping)
    ///////////////////////////////////////////////////////////////////////////

    @Section(title = "Input")
    interface InputSection {}

    @Section(title = "Query")
    @After(InputSection.class)
    interface QuerySection {}

    @Section(title = "Search")
    @After(QuerySection.class)
    interface SearchSection {}

    @Section(title = "Output")
    @After(SearchSection.class)
    interface OutputSection {}

    ///////////////////////////////////////////////////////////////////////////
    // Fields in "Input"
    ///////////////////////////////////////////////////////////////////////////

    @Persist(configKey = "input_column")
    @Widget(title = "RDKit Mol column", 
            description = "The column in the first input table containing the reference molecules (RDKitMol, SMILES, or SDF).")
    @ChoicesWidget(choices = RDKitMoleculeColumnChoicesProvider.class)
    @Layout(InputSection.class)
    String m_input_column;

    ///////////////////////////////////////////////////////////////////////////
    // Fields in "Query"
    ///////////////////////////////////////////////////////////////////////////

    @Persist(configKey = "inputQueryCol")
    @Widget(title = "Query molecule column", 
            description = "The column in the second input table containing the query molecules (SMARTS or RDKitMol).")
    @ChoicesWidget(choices = RDKitMoleculeColumnTable2ChoicesProvider.class)
    @Layout(QuerySection.class)
    String m_inputQueryCol;

    @Persist(configKey = "useQueryNameColumn")
    @Widget(title = "Use query name column", 
            description = "If selected, a second column is used to name the result columns by reading from a string column.")
    @ValueReference(SubstructureCounterNodeSettingsRef.UseQueryName.class)
    @Layout(QuerySection.class)
    boolean m_useQueryNameColumn;

    @Persist(configKey = "queryNameColumn")
    @Widget(title = "Query name column", 
            description = "A string column used to label the substructure-based result columns with more user-friendly names.")
    @Effect(type = EffectType.SHOW, predicate = UseQueryName.class)
    @Layout(QuerySection.class)
    @ChoicesWidget(choices = AllColumnTable2ChoicesProvider.class)
    String m_queryNameColumn;

    ///////////////////////////////////////////////////////////////////////////
    // Fields in "Search"
    ///////////////////////////////////////////////////////////////////////////

    @Persist(configKey = "countUniqueMatches")
    @Widget(title = "Count unique matches only", 
            description = "If selected, for each query molecule, repeated matches e.g. in symmetrical regions are counted only once.")
    @Layout(SearchSection.class)
    boolean m_countUniqueMatches;

    @Persist(configKey = "useChirality")
    @Widget(title = "Use chirality matching", 
            description = "If selected, stereochemistry/chiral centers are considered for the substructure matching.")
    @ValueReference(SubstructureCounterNodeSettingsRef.IsChiral.class)
    @Layout(SearchSection.class)
    boolean m_useChirality;

    @Persist(configKey = "useEnhancedStereo")
    @Widget(title = "Use enhanced stereochemistry", 
            description = "If selected, the substructure match includes enhanced stereo (cis/trans) matching.")
    @Effect(type = EffectType.SHOW, predicate = IsChiral.class)
    @Layout(SearchSection.class)
    boolean m_useEnhancedStereo;

    ///////////////////////////////////////////////////////////////////////////
    // Fields in "Output"
    ///////////////////////////////////////////////////////////////////////////

    @Persist(configKey = "countTotalHits")
    @Widget(title = "Add total hits count column", 
            description = "If selected, one additional column is appended containing the total sum of matches across all queries.")
    @ValueReference(SubstructureCounterNodeSettingsRef.AddTotalHits.class)
    @Layout(OutputSection.class)
    boolean m_countTotalHits;

    @Persist(configKey = "countTotalHitsColumn")
    @Widget(title = "New column name for total hits count", 
            description = "Name for the appended column with the total hits across all queries for each row.")
    @Effect(type = EffectType.SHOW, predicate = AddTotalHits.class)
    @Layout(OutputSection.class)
    String m_countTotalHitsColumn;

    @Persist(configKey = "trackQueryTags")
    @Widget(title = "Add column with tags for matching queries", 
            description = "If selected, a new column is appended listing the queries that matched each row.")
    @ValueReference(SubstructureCounterNodeSettingsRef.AddTrackTags.class)
    @Layout(OutputSection.class)
    boolean m_trackQueryTags;

    @Persist(configKey = "trackQueryTagsColumn")
    @Widget(title = "New column name for tags", 
            description = "Name for the appended column with query 'tags' for each row, i.e., which queries matched.")
    @Effect(type = EffectType.SHOW, predicate = AddTrackTags.class)
    @Layout(OutputSection.class)
    String m_trackQueryTagsColumn;

    ///////////////////////////////////////////////////////////////////////////
    // Additional references used for toggling fields
    ///////////////////////////////////////////////////////////////////////////
    
    static final class IsChiral implements PredicateProvider {
        @Override
        public Predicate init(final PredicateInitializer i) {
            return i.getBoolean(SubstructureCounterNodeSettingsRef.IsChiral.class).isTrue();
        }
    }
    
	static final class UseQueryName implements PredicateProvider {
		@Override
		public Predicate init(final PredicateInitializer i) {
			return i.getBoolean(SubstructureCounterNodeSettingsRef.UseQueryName.class).isTrue();
		}
	}
	
	static final class AddTotalHits implements PredicateProvider {
		@Override
		public Predicate init(final PredicateInitializer i) {
			return i.getBoolean(SubstructureCounterNodeSettingsRef.AddTotalHits.class).isTrue();
		}
	}
	
	static final class AddTrackTags implements PredicateProvider {
		@Override
		public Predicate init(final PredicateInitializer i) {
			return i.getBoolean(SubstructureCounterNodeSettingsRef.AddTrackTags.class).isTrue();
		}
	}
    

    ///////////////////////////////////////////////////////////////////////////
    // Implementation detail: references
    ///////////////////////////////////////////////////////////////////////////

    interface SubstructureCounterNodeSettingsRef {
        interface IsChiral extends Reference<Boolean>  {}
        interface UseQueryName extends Reference<Boolean>  {}
        interface AddTotalHits extends Reference<Boolean>  {}
        interface AddTrackTags extends Reference<Boolean>  {}
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Implementation detail: Choice providers
    ///////////////////////////////////////////////////////////////////////////
    
    public static final class RDKitMoleculeColumnTable2ChoicesProvider extends RDKitMoleculeColumnChoicesProvider {
		public RDKitMoleculeColumnTable2ChoicesProvider() {
			super(1);
		}
	}
    
    public static final class AllColumnTable2ChoicesProvider implements ColumnChoicesProvider {
        @Override
        public DataColumnSpec[] columnChoices(final DefaultNodeSettingsContext context) {
            return context.getDataTableSpec(1) //
                .map(DataTableSpec::stream) //
                .orElseGet(Stream::empty) //
                .toArray(DataColumnSpec[]::new);
        }
    }
}