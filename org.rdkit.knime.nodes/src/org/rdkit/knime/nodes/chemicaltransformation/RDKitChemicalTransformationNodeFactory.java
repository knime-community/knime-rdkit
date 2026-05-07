/*
 * ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright (C)2014-2023
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
package org.rdkit.knime.nodes.chemicaltransformation;

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
 * <code>NodeFactory</code> for the RDKit based "RDKitChemicalTransformation" Node.
 * Transforms a structure into another structure by applying several reactions provided as SMARTS values.
 *
 * @author Manuel Schwarze
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
public class RDKitChemicalTransformationNodeFactory extends NodeFactory<RDKitChemicalTransformationNodeModel> 
	implements NodeDialogFactory {

    private static final String NODE_NAME = "RDKit Chemical Transformation";
    
    private static final String NODE_ICON = "default.png";
    
    private static final String SHORT_DESCRIPTION = """
            Transforms a structure into another structure by applying several reactions provided as SMARTS
            values or Rxn Blocks.
            """;
    
    private static final String FULL_DESCRIPTION = """
            Transforms a structure into another structure by applying several reactions provided as SMARTS
            values or Rxn Blocks. Every valid reaction found in table 2 will be applied multiple times to every
            input molecule in table 1. If the result does not change anymore for one reaction, the next one will
            be applied, and so on. Sometimes, a reaction could be applied forever. To avoid this scenario a
            maximal number of reaction cycles can be set. If a reaction fails in some cycle the failure will be
            ignored and the next reaction is being executed on the last successful product. At the very end
            after all reactions were applied the end product is being sanitized. If this sanitization fails for
            some reason the result cell will be empty.
            """;
    
    private static final List<PortDescription> INPUT_PORTS = List.of(
            fixedPort("Molecules", """
                Table with RDKit Molecule column.
                """),
            fixedPort("Reactions", """
                Table with reactions to be applied.
                """)
    );
    
    private static final List<PortDescription> OUTPUT_PORTS = List.of(
            fixedPort("Transformed molecules", """
                Transformed molecules.
                """)
    );
	
    /**
     * Creates a model for the RDKitChemicalTransformation functionality
     * of the RDKit library. The model is derived from the
     * abstract class AbstractRDKitNodeModel, which provides
     * common base functionality for RDKit nodes.
     * {@inheritDoc}
     *
     * @see org.rdkit.knime.nodes.AbstractRDKitNodeModel
     */
    @Override
    public RDKitChemicalTransformationNodeModel createNodeModel() {
        return new RDKitChemicalTransformationNodeModel();
    }
    
    /**
     * This node does not have any views.
     * 
	 * @return Always null.
     */
    @Override
    public NodeView<RDKitChemicalTransformationNodeModel> createNodeView(
            final int viewIndex,
            final RDKitChemicalTransformationNodeModel nodeModel) {
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
        return new DefaultNodeDialog(SettingsType.MODEL, RDKitChemicalTransformationNodeParameters.class);
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
            RDKitChemicalTransformationNodeParameters.class, //
            null, //
            NodeType.Manipulator, //
            List.of(), //
            null //
        );
    }
    
}

