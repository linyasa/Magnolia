/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.gui.controlx.list;

import info.magnolia.cms.gui.controlx.Control;
import info.magnolia.cms.gui.controlx.Renderer;


/**
 * Renders a list view.
 * 
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 */
public class ListControlRenderer implements Renderer {
    
    public String render(Control list){
        return "the list";
    }

    protected String onClick(ListModelIterator iter){
        return "";
    }
    
    protected String onDbClick(ListModelIterator iter){
        return "";
    }
    
}
