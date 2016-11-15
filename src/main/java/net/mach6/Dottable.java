/*
 * Copyright (C) 2016 Doug Simmons
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance 
 * with the License.
 * 
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0  
 */

package net.mach6;

public interface Dottable {
    /**
     * Return a Graphviz .dot representation of the object
     * 
     * @param asSubgraph
     *            whether to return the dot content as a "subgraph g { ... }" entry
     * @return string content which can be written to a .dot file or an empty string if there is nothing significant to
     *         represent for this object
     */
    String toDot(boolean asSubgraph);
}
