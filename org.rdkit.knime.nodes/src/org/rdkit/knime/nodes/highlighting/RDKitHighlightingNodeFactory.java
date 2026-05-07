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
package org.rdkit.knime.nodes.highlighting;

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
 * <code>NodeFactory</code> for the RDKit based "RDKitHighlighting" Node.
 * Creates a SVG column showing a molecule with highlighted atoms/bonds based on information in the input table. 
 * A molecule column as well as a column with a list of the atoms/bonds to be highlighted needs to be provided.
 *
 * @author Manuel Schwarze
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
public class RDKitHighlightingNodeFactory extends NodeFactory<RDKitHighlightingNodeModel> 
	implements NodeDialogFactory {

    private static final String NODE_NAME = "RDKit Molecule Highlighting";
    
    private static final String NODE_ICON = "default.png";
    
    private static final String SHORT_DESCRIPTION = """
            Creates an SVG column showing a molecule with highlighted atoms and bonds based on information in
            the input table.
            """;
    
    private static final String FULL_DESCRIPTION = """
            Creates an SVG column showing a molecule with highlighted atoms and bonds based on information in
            the input table. A molecule column as well as column(s) with a list of the atom and/or bond indexes
            to be highlighted needs to be provided. The node lets the user define colors to be applied for the
            highlighting. If highlighting definitions overlap (e.g. atom indexes in definition 1 is 1,2,3 and in
            definition 2 is 3,4,5) the highlighting of the first definition will be applied (e.g to atom 3) with
            the color it defined.
            """;
    
    private static final List<PortDescription> INPUT_PORTS = List.of(
            fixedPort("Table with RDKit molecules and atom/bond list(s)", """
                Table with an RDKit molecules and list(s) of atoms and/or bonds to be highlighted.
                """)
    );
    
    private static final List<PortDescription> OUTPUT_PORTS = List.of(
            fixedPort("Highlighted molecules", """
                The input table with an additional column that shows the highlighted atoms and bonds in an SVG molecule
                graphic.
                """)
    );
	
	/**
	 * Creates a model for the RDKitHighlightingAtoms functionality
	 * of the RDKit library. The model is derived from the
	 * abstract class AbstractRDKitNodeModel, which provides
	 * common base functionality for RDKit nodes.
	 * {@inheritDoc}
	 *
	 * @see org.rdkit.knime.nodes.AbstractRDKitNodeModel
	 */
	@Override
	public RDKitHighlightingNodeModel createNodeModel() {
		return new RDKitHighlightingNodeModel();
	}

	/**
	 * This node does not have any views.
	 * 
	 * @return Always null.
	 */
	@Override
	public NodeView<RDKitHighlightingNodeModel> createNodeView(
			final int viewIndex,
			final RDKitHighlightingNodeModel nodeModel) {
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

    @Override
    public NodeDialogPane createNodeDialogPane() {
        return NodeDialogManager.createLegacyFlowVariableNodeDialog(createNodeDialog());
    }

    @Override
    public NodeDialog createNodeDialog() {
        return new DefaultNodeDialog(SettingsType.MODEL, RDKitHighlightingNodeParameters.class);
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
            RDKitHighlightingNodeParameters.class, //
            null, //
            NodeType.Manipulator, //
            List.of(), //
            null //
        );
    }
    
}

