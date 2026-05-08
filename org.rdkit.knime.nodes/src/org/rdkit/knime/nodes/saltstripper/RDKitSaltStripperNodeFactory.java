/*
 * ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright (C)2012-2023
 * Novartis Pharma AG, Switzerland
 *
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
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
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
 * ---------------------------------------------------------------------
 */
package org.rdkit.knime.nodes.saltstripper;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.webui.node.dialog.NodeDialog;
import org.knime.core.webui.node.dialog.NodeDialogFactory;
import org.knime.core.webui.node.dialog.NodeDialogManager;
import org.knime.core.webui.node.dialog.SettingsType;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeDialog;
import org.knime.core.node.NodeDescription;
import org.knime.node.impl.description.DefaultNodeDescriptionUtil;
import org.knime.node.impl.description.PortDescription;
import java.util.List;
import static org.knime.node.impl.description.PortDescription.fixedPort;

/**
 * <code>NodeFactory</code> for the RDKit based "RDKitSaltStripper" Node.
 * 
 * @author Dillip K Mohanty
 * @author Manuel Schwarze
 * @author Jannik Semperowitsch, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
public class RDKitSaltStripperNodeFactory extends NodeFactory<RDKitSaltStripperNodeModel> implements NodeDialogFactory {

	/**
	 * Creates a model for the RDKitSaltStripper functionality
	 * of the RDKit library. The model is derived from the
	 * abstract class AbstractRDKitNodeModel, which provides
	 * common base functionality for RDKit nodes.
	 * {@inheritDoc}
	 *
	 * @see org.rdkit.knime.nodes.AbstractRDKitNodeModel
	 */
	@Override
	public RDKitSaltStripperNodeModel createNodeModel() {
		return new RDKitSaltStripperNodeModel();
	}

	/**
	 * This node does not have any views.
	 * 
	 * @return Always null.
	 */
	@Override
	public NodeView<RDKitSaltStripperNodeModel> createNodeView(
			final int viewIndex,
			final RDKitSaltStripperNodeModel nodeModel) {
		return null;
	}

	/**
	 * This node does not have any views.
	 * 
	 * @return Always 0.
	 */
	@Override
	public int getNrNodeViews() {
		return 0;
	}

	/**
	 * This node possesses a configuration dialog.
	 * 
	 * @return Always true.
	 */
	@Override
	public boolean hasDialog() {
		return true;
	}

    private static final String NODE_NAME = "RDKit Salt Stripper";
    private static final String NODE_ICON = "default.png";
    private static final String SHORT_DESCRIPTION = """
            Node for stripping salts from molecules.
            """;
    private static final String FULL_DESCRIPTION = """
            <p>This node is used for removing salts from RDKit molecules and display the salt stripped molecules in
            an additional column in the output table. The user can optionally input salt definitions into the
            node. If no salt definition table is provided by the user then the default salt definitions will be
            applied.</p>

            <p>The predefined salts included by default are:</p>

            <b>Simple Inorganics:</b>
            <ul>
                <li>[Cl], [Br], [I], [Li], [Na], [K], [Ca], [Mg], [O], [N]</li>
            </ul>

            <b>Complex Inorganics:</b>
            <ul>
                <li><b>Nitric acid:</b> [N](=O)(O)O</li>
                <li><b>Phosphoric acid:</b> [P](=O)(O)(O)O</li>
                <li><b>Hexafluorophosphate:</b> [P](F)(F)(F)(F)(F)F</li>
                <li><b>Sulfuric acid:</b> [S](=O)(=O)(O)O</li>
                <li><b>Methanesulfonic acid:</b> [CH3][S](=O)(=O)(O)</li>
                <li><b>p-Toluene sulfonate:</b> c1cc([CH3])ccc1[S](=O)(=O)(O)</li>
            </ul>

            <b>Organics:</b>
            <ul>
                <li><b>Acetic acid:</b> [CH3]C(=O)O</li>
                <li><b>TFA (Trifluoroacetic acid):</b> FC(F)(F)C(=O)O</li>
                <li><b>Fumarate/Maleate:</b> OC(=O)C=CC(=O)O</li>
                <li><b>Oxalate:</b> OC(=O)C(=O)O</li>
                <li><b>Tartrate:</b> OC(=O)C(O)C(O)C(=O)O</li>
                <li><b>Dicyclohexylammonium:</b> C1CCCCC1[NH]C1CCCCC1</li>
            </ul>

            <p><i>Note: The stripping process utilizes substructure matching against entire fragments. Matching is performed
            sequentially; complex definitions are prioritized last to ensure the "don't remove the last fragment"
            logic remains robust.</i></p>
            """;
    private static final List<PortDescription> INPUT_PORTS = List.of(
            fixedPort("RDKit molecules", """
                Table having at least one RDKit molecule type column containing RDKit molecules for stripping.
                """),
            fixedPort("Salt definitions", """
                Table containing RDKit molecules as salt definitions (generated usually from SMARTS). This table is
                optional.
                """)
    );
    private static final List<PortDescription> OUTPUT_PORTS = List.of(
            fixedPort("Molecules without salts", """
                Table containing the column with salt stripped RDKit molecules.
                """)
    );

    @Override
    public NodeDialogPane createNodeDialogPane() {
        return NodeDialogManager.createLegacyFlowVariableNodeDialog(createNodeDialog());
    }

    @Override
    public NodeDialog createNodeDialog() {
        return new DefaultNodeDialog(SettingsType.MODEL, RDKitSaltStripperNodeParameters.class);
    }

    @Override
    public NodeDescription createNodeDescription() {
        return DefaultNodeDescriptionUtil.createNodeDescription( //
            NODE_NAME, //
            NODE_ICON, //
            INPUT_PORTS, //
            OUTPUT_PORTS, //
            SHORT_DESCRIPTION, //
            FULL_DESCRIPTION, //
            List.of(), //
            RDKitSaltStripperNodeParameters.class, //
            null, //
            NodeType.Manipulator, //
            List.of(), //
            null //
        );
    }
    
}

