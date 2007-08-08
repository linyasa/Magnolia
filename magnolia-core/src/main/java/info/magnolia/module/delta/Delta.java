/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.delta;

import java.util.List;

/**
 * A Delta represents the differences from one version of a module to another.
 * It holds a set of Tasks and should provide a description of what it does.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public interface Delta {
    /**
     * Returns the version number for which this is needed.
     */
    // Version getVersion();

    /**
     * Should not return a read-only List, as the version handler might add tasks as appropriate.
     */
    List getTasks();

    String getTitle();

    String getDescription();

    DeltaType getType();
}
