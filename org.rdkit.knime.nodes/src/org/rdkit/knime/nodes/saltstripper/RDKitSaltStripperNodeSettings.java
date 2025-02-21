package org.rdkit.knime.nodes.saltstripper;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.vector.bitvector.BitVectorValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings.DefaultNodeSettingsContext;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.api.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Label;
import org.knime.core.webui.node.dialog.defaultdialog.widget.TextMessage;
import org.knime.core.webui.node.dialog.defaultdialog.widget.TextMessage.MessageType;
import org.knime.core.webui.node.dialog.defaultdialog.widget.TextMessage.SimpleTextMessageProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ValueSwitchWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Effect;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Predicate;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.PredicateProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Effect.EffectType;
import org.rdkit.knime.types.RDKitMolValue;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ColumnChoicesProvider;
import org.knime.core.webui.node.dialog.defaultdialog.layout.After;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Layout;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Section;

/**
 * Settings for the RDKit Salt Stripper node using the webui framework.
 * 
 * @author Marc Lehner, KNIME GmbH, Zurich, Switzerland
 * 
 */
public final class RDKitSaltStripperNodeSettings implements DefaultNodeSettings {
	
	public static final class RDKitMoleculeColumnTable2ChoicesProvider extends RDKitMoleculeColumnChoicesProvider {
		public RDKitMoleculeColumnTable2ChoicesProvider() {
			super(1);
		}
	}
	
	static final class IsSaltInputConnectedPredicate implements PredicateProvider {
		@Override
		public Predicate init(PredicateInitializer initializer) {
			return initializer.getConstant(context -> context.getDataTableSpec(1).isPresent());
		}		
	}
	
//	static final class IsNOTSaltInputConnectedPredicate implements PredicateProvider {
//        @Override
//        public Predicate init(PredicateInitializer initializer) {
//            return initializer.getConstant(context -> !context.getDataTableSpec(1).isPresent());
//        }
//    }
		
	static final class SaltInputManagedByPortMessage implements SimpleTextMessageProvider {

        @Override
        public boolean showMessage(final DefaultNodeSettingsContext context) {
            return context.getDataTableSpec(1).isPresent();
        }

        @Override
        public String title() {
            return "Salt input settings controlled by input port";
        }

        @Override
        public String description() {
            return "Remove the input port to change the settings";
        }

        @Override
        public MessageType type() {
            return MessageType.INFO;
        }

    }
	
	static final class SaltDefaultInputMessage implements SimpleTextMessageProvider {

        @Override
        public boolean showMessage(final DefaultNodeSettingsContext context) {
            return !context.getDataTableSpec(1).isPresent();
        }

        @Override
        public String title() {
            return "There is no salt table connected, using default salt patterns";
        }
        
        @Override
        public String description() {
        	return """
        			Notes:
        			  1) don't include charges
					  2) The search for salts is a substructure search where the substructure
					  must match the entire fragment, so we don't need to be choosy about bond
					  types
					  3) The matching is done in order, so if you put the more complex stuff at the
					  bottom the "don't remove the last fragment" algorithm has a chance of
					  of returning something sensible
					  
					  start with simple inorganics:
					  [Cl,Br,I]
					  [Li,Na,K,Ca,Mg]
					  [O,N]
					  
					  "complex" inorganics
					  [N](=O)(O)O
					  [P](=O)(O)(O)O
					  [P](F)(F)(F)(F)(F)F
					  [S](=O)(=O)(O)O
					  [CH3][S](=O)(=O)(O)
					  c1cc([CH3])ccc1[S](=O)(=O)(O)
					  
					  "organics"
					  [CH3]C(=O)O
					  FC(F)(F)C(=O)O
					  OC(=O)C=CC(=O)O
					  OC(=O)C(=O)O
					  OC(=O)C(O)C(O)C(=O)O
					  C1CCCCC1[NH]C1CCCCC1
        			""";
        }

        @Override
        public MessageType type() {
            return MessageType.INFO;
        }

    }

	
	
	
    @Section(title = "Input")
    interface InputSection {
    }
    
    @Section(title = "Output")
    @After(InputSection.class)
	interface OutputSection {
	}
    
    @Persist(configKey = "input_column")
    @Widget(title = "Molecule column",
            description = "Select the molecule input column (SMILES / SDF / RDKit) to be salt stripped.")
    @ChoicesWidget(choices = RDKitMoleculeColumnChoicesProvider.class)
    @Layout(InputSection.class)
    String m_inputColumn = "";
    
    @TextMessage(value = SaltInputManagedByPortMessage.class)
    @Layout(InputSection.class)
    Void m_saltInputManagedByPortMessage;

    @Persist(configKey = "salt_input")
    @Widget(title = "Salt column",
            description = "If a second input table is connected, select a column with SMARTS or molecules that represent salts.")
    @ChoicesWidget(choices = RDKitMoleculeColumnTable2ChoicesProvider.class)
    @Effect(type = EffectType.SHOW, predicate = IsSaltInputConnectedPredicate.class)
    @Layout(InputSection.class)
    String m_saltInput = "";
    
    @TextMessage(value = SaltDefaultInputMessage.class)
	@Layout(InputSection.class)
	Void m_showDefaultSaltPatterns;
    
    
    
    @Persist(configKey = "new_column_name")
    @Widget(title = "Output column name",
            description = "Specify how the generated (salt stripped) molecule column should be named.")
    @Layout(OutputSection.class)
    String m_newColumnName = "Salt Stripped Molecule";


    @Persist(configKey = "remove_source_columns")
    @Widget(title = "Remove original column",
            description = "If checked, the original molecule column will not appear in the output table.")
    @Layout(OutputSection.class)
    boolean m_removeSourceColumns = false;

    @Persist(configKey = "keep_only_largest_fragment")
    @Widget(title = "Keep only largest fragment",
            description = "If selected, any multi-fragment result will be pruned to only the largest connected fragment.")
    @Layout(OutputSection.class)
    boolean m_keepOnlyLargestFragment = false;
}