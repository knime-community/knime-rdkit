/*
 * ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright (C)2013-2023
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
package org.rdkit.knime.nodes.optimizegeometry;

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
 * <code>NodeFactory</code> for the RDKit based "RDKitOptimizeGeometry" Node.
 * 
 *
 * @author Manuel Schwarze
 * @author Jannik Semperowitsch, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
public class RDKitOptimizeGeometryNodeFactory 
        extends NodeFactory<RDKitOptimizeGeometryNodeModel> implements NodeDialogFactory {

    /**
     * Creates a model for the RDKitOptimizeGeometry functionality
     * of the RDKit library. The model is derived from the
     * abstract class AbstractRDKitNodeModel, which provides
     * common base functionality for RDKit nodes.
     * {@inheritDoc}
     *
     * @see org.rdkit.knime.nodes.AbstractRDKitNodeModel
     */
    @Override
    public RDKitOptimizeGeometryNodeModel createNodeModel() {
        return new RDKitOptimizeGeometryNodeModel();
    }
    
    /**
     * This node does not have any views.
     * 
	 * @return Always null.
     */
    @Override
    public NodeView<RDKitOptimizeGeometryNodeModel> createNodeView(
            final int viewIndex,
            final RDKitOptimizeGeometryNodeModel nodeModel) {
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

    private static final String NODE_NAME = "RDKit Optimize Geometry";
    private static final String NODE_ICON = "default.png";
    private static final String SHORT_DESCRIPTION = """
            Optimizes the geometry for an input RDKit Mol column and calculates the molecule's energy in
                kcal/mol.
            """;
    private static final String FULL_DESCRIPTION = """
            Optimizes the geometry for an input RDKit Mol column and calculates the molecule's energy in
                kcal/mol. If the passed in molecules have no conformation, it will be calculated. The optimization
                is based on a particular force field and a number of iterations. It is also possible to turn off
                optimization completely by iterating 0 times. Optionally already available coordinates can be
                removed in order to calculate new ones from scratch. The following force fields are supported: <ul>
                <li>UFF: Universal force field is an all atom potential containing parameters for every atom. The
                force field parameters are estimated using general rules based only on the element, its
                hybridization, and its connectivity. Published in: UFF, a Full Periodic Table Force Field for
                Molecular Mechanics and Molecular Dynamics Simulations by A.K. Rappe, C.J. Casewit, K.S. Colwell,
                W.A. Goddard III, W.M. Skiff, J.Am. Chem. Soc. 114 (1992) 10024–10035 </li> <li>MMFF94: Merck
                molecular force field. Published in: Basis, form, scope, parameterization, and performance of
                MMFF94, Thomas A. Halgren, J. Comp. Chem.; 1996; 490-519 </li> <li>MMFF94S: Static variant of
                MMFF94. MMFF94S incorporates altered out of plane bending parameters that yield planar (or nearly
                planar) energy-minimized geometries at unstrained delocalized trigonal nitrogen centers. Published
                in: MMFF VI. MMFF94s option for energy minimization studies, Thomas A. Halgren; 1999; J. Comput.
                Chem., 20: 720–729 </li> </ul>
            """;
    private static final List<PortDescription> INPUT_PORTS = List.of(
            fixedPort("Input table with RDKit molecules", """
                The molecules to optimize the geometry for.
                """)
    );
    private static final List<PortDescription> OUTPUT_PORTS = List.of(
            fixedPort("Result table", """
                Optimized molecules with converge and energy information (kcal/mol).
                """)
    );

    @Override
    public NodeDialogPane createNodeDialogPane() {
        return NodeDialogManager.createLegacyFlowVariableNodeDialog(createNodeDialog());
    }

    @Override
    public NodeDialog createNodeDialog() {
        return new DefaultNodeDialog(SettingsType.MODEL, RDKitOptimizeGeometryNodeParameters.class);
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
            RDKitOptimizeGeometryNodeParameters.class, //
            null, //
            NodeType.Manipulator, //
            List.of(), //
            null //
        );
    }
    
}

