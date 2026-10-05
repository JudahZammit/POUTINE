package org.gersteinlab.coevolution.core.data;

import java.util.*;

/**
 *	<pre>
 *	This class defines a tree node in the Newick/New Hampshire/PHYLIP
 *	format.
 *	</pre>
 *
 *	@author		Kevin Yuk-Lap Yip
 *	@version	1.0 (May 16, 2006)
 *
 *	<pre>
 *	Change History:
 *	1.0	- Initial version
 *	</pre>
 */
public class NewickTreeNode
{
	/*----------------------------------------------------------------------
	 Class variables
	----------------------------------------------------------------------*/

	/**
	 *	The parent node (null for root node).
	 */
	protected NewickTreeNode parent = null;

	/**
	 *	The list of children nodes (empty for leaf nodes).
	 */
	protected List children = null;

	/**
	 *	Node ID (can be null).
	 */
	protected String id = null;

	/**
	 *	Distance to parent (Float.NaN if not known).
	 */
	protected float distToParent = Float.NaN;


	/*----------------------------------------------------------------------
	 Public methods
	----------------------------------------------------------------------*/

	/**
	 *	Constructor: create a blank tree node.
	 */
	public NewickTreeNode()
	{
		children = new ArrayList();
	}

	/**
	 *	Set the parent node.
	 *	@param		parent			The parent node
	 */
	public void setParent(NewickTreeNode parent)
	{
		this.parent = parent;
	}

	/**
	 *	Get the parent node.
	 *	@return					The parent node, null for root node
	 */
	public NewickTreeNode getParent()
	{
		return parent;
	}

	/**
	 *	Add a child node.
	 *	@param		child			The child node
	 */
	public void addChild(NewickTreeNode child)
	{
		children.add(child);
	}

	/**
	 *	Remove a child node.
	 *	@param		child			The child node
	 */
	public void removeChild(NewickTreeNode child)
	{
		children.remove(child);
	}

	/**
	 *	Get the number of children.
	 *	@return					The number
	 */
	public int getChildrenCount()
	{
		return children.size();
	}

	/**
	 *	Get a copy of the list of children.
	 *	@return					The list
	 */
	public List getChildren()
	{
		return new ArrayList(children);
	}

	/**
	 *	Get an iterator of the children.
	 *	@return					The iterator
	 */
	public Iterator getChildrenIterator()
	{
		return children.iterator();
	}

	/**
	 *	Get the i-th child of the node.
	 *	@return					The child
	 *	@exception	ArrayIndexOutOfBoundException	If the node does not have i children
	 */
	public NewickTreeNode getChild(int i)
	{
		return (NewickTreeNode)children.get(i);
	}

	/**
	 *	Set the node ID.
	 *	@param		id			The ID
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	/**
	 *	Get the node ID.
	 *	@return					The node ID
	 */
	public String getId()
	{
		return id;
	}

	/**
	 *	Set the distance to parent.
	 *	@param		distToParent		The distance
	 */
	public void setDistToParent(float distToParent)
	{
		this.distToParent = distToParent;
	}

	/**
	 *	Get the distance to parent.
	 *	@return					The distance
	 */
	public float getDistToParent()
	{
		return distToParent;
	}

	/**
	 *	Return the string representation of the node in the Newick
	 *	format, not including the parent and children.
	 *	@return					The string representation
	 */
	public String toString()
	{
		return (id == null ?"" :id) +
		       (Float.isNaN(distToParent) ?"" :":" + distToParent);
	}
}