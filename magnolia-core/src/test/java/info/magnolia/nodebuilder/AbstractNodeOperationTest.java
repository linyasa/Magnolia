/**
 * This file Copyright (c) 2011-2012 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.nodebuilder;

import static org.easymock.EasyMock.*;
import info.magnolia.cms.core.Content;
import info.magnolia.test.mock.MockContent;

import javax.jcr.RepositoryException;

import org.junit.Test;


public class AbstractNodeOperationTest {

    /**
     * MAGNOLIA-3773
     */
    @Test
    public void testThatSubsequentCallsToTheThenMethodAdd() {
        MockContent node = new MockContent("test");
        StrictErrorHandler errorHandler = new StrictErrorHandler();

        NodeOperation op1 = createStrictMock(NodeOperation.class);
        op1.exec(node, errorHandler);
        NodeOperation op2 = createStrictMock(NodeOperation.class);
        op2.exec(node, errorHandler);
        replay(op1, op2);

        AbstractNodeOperation op = new AbstractNodeOperation() {

            @Override
            protected Content doExec(Content context, ErrorHandler errorHandler) throws RepositoryException {
                return context;
            }
        };

        // WHEN we add both operations separately
        op.then(op1);
        op.then(op2);

        // THEN the both operations are executed
        op.exec(node, errorHandler);

        verify(op1, op2);

    }
}
