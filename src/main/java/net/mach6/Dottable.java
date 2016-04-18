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
