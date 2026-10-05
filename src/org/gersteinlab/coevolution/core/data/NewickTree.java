package org.gersteinlab.coevolution.core.data;

import java.util.*;

/**
 *	<pre>
 *	This class defines a tree in the Newick/New Hampshire/PHYLIP format.
 *	</pre>
 *
 *	@author		Kevin Yuk-Lap Yip
 *	@version	1.1 (March 22, 2007)
 *
 *	<pre>
 *	Change History:
 *	1.0	- Initial version
 *	1.1	- Added removeLeaf method
 *	</pre>
 */
public class NewickTree
{
	/*----------------------------------------------------------------------
	 Class variables
	----------------------------------------------------------------------*/

	protected NewickTreeNode root = null;		// The root node of the tree
	protected Map nodeMap = null;			// A (node ID -> node) map
	protected List leafList = null;			// A list of all leaf nodes
//	protected Map distMap = null;			// A (node ID + "," + node ID -> dist) map, the smaller ID (based on String.compareTo()) goes first


	/*----------------------------------------------------------------------
	 Public methods
	----------------------------------------------------------------------*/

	/**
	 *	Constructor: create a tree with a given root node.
	 *	The tree is assumed not to be changed after construction.
	 */
	public NewickTree(NewickTreeNode root)
	{
		this.root = root;

		// Build the cached structures: based on a simple tree traversal
		nodeMap = new HashMap();
		leafList = new ArrayList();
//		distMap = new HashMap();
		buildCaches(root);
	}

	/**
	 *	Get the root node.
	 *	@return					The root node, null for root node
	 */
	public NewickTreeNode getRoot()
	{
		return root;
	}

	/**
	 *	Get the node with a particular ID.
	 *	@param		id			The node ID
	 *	@return					The node, null if not found
	 */
	public NewickTreeNode getNode(String id)
	{
		return (NewickTreeNode)nodeMap.get(id);
	}

	/**
	 *	Get a copy of the list of all leaf nodes.
	 *	@return					The list
	 */
	public List getLeafNodes()
	{
		return new ArrayList(leafList);
	}

	/**
	 *	Get an iterator of all leaf nodes.
	 *	@return					The iterator
	 */
	public Iterator getLeafIterator()
	{
		return leafList.iterator();
	}

	/**
	 *	Get the distance between the two nodes with particular IDs.
	 *	@param		id1			The ID of one of the nodes (anyone, order not important)
	 *	@param		id2			The ID of the other node
	 *	@return					The distance, Float.NaN if not defined or any of the nodes are not found
	 */
/*
	public float getDist(String id1, String id2)
	{
		Float dist = (Float)distMap.get(getDistMapKey(id1, id2));
		if (dist == null)
			return Float.NaN;
		else
			return dist.floatValue();
	}
*/

	/**
	 *	Remove a node from the tree.
	 *	@param		id			The ID of the node
	 */
	public void removeNode(String id)
	{
		NewickTreeNode node = (NewickTreeNode)nodeMap.get(id);
		if (node == null)
			throw new IllegalArgumentException("Node [" + id + "] cannot be found in the tree.");
		removeNode(node);
	}

	/**
	 *	Remove a node from the tree.
	 *	@param		node			The node
	 */
	protected void removeNode(NewickTreeNode node)
	{
		String id = node.getId();
		if (id != null)
			nodeMap.remove(id);
		leafList.remove(node);

		NewickTreeNode parent = node.getParent();
		if (parent == null)
		{
			int childrenCount = node.getChildrenCount();
			if (childrenCount == 0)
				root = null;
			else if (childrenCount == 1)
			{
				NewickTreeNode newRoot = node.getChild(0);
				root = newRoot;
				newRoot.setParent(null);
			}
			else
				throw new RuntimeException("Removing the node would make the tree a forest.");
		}
		else
		{
			parent.removeChild(node);
			int childrenCount = node.getChildrenCount();
			if (childrenCount != 0)
			{
				for (int i=0; i<childrenCount; i++)
				{
					NewickTreeNode child = node.getChild(i);
					parent.addChild(child);
					child.setParent(parent);
					child.setDistToParent(child.getDistToParent() + node.getDistToParent());
				}
			}

			if (parent.getChildrenCount() <= 1)
				removeNode(parent);
		}
	}

	/**
	 *	Return the string representation of the tree in the Newick
	 *	format.
	 *	@return					The string representation
	 */
	public String toString()
	{
		return toString(root) + ";";
	}

	/**
	 *	Return the string representation of a subtree in the Newick
	 *	format, without the final semi-colon.
	 *	@param		root			The root of the subtree
	 *	@return					The string representation
	 */
	public String toString(NewickTreeNode root)
	{
		if (root.getChildrenCount() == 0)	// A leaf node
			return root.toString();
		else					// An internal node
		{
			StringBuffer result = new StringBuffer();
			result.append("(");
			Iterator it = root.getChildrenIterator();
			boolean firstChild = true;
			while (it.hasNext())
			{
				NewickTreeNode node = (NewickTreeNode)it.next();
				if (firstChild)
					firstChild = false;
				else
					result.append(",");
				result.append(toString(node));
			}
			result.append(")");
			result.append(root.toString());
			return result.toString();
		}
	}


	/*----------------------------------------------------------------------
	 Non-public methods
	----------------------------------------------------------------------*/

	/**
	 *	Get the key used in distMap for a pair of node IDs.
	 *	@param		id1			The first ID
	 *	@param		id2			The second ID
	 *	@return					The key
	 */
/*
	protected String getDistMapKey(String id1, String id2)
	{
		if (id1.compareTo(id2) < 0)
			return id1 + "," + id2;
		else
			return id2 + "," + id1;
	}
*/

	/**
	 *	Build the node and distance maps.
	 *	Nodes are added to the global node map immediately when
	 *	encountered during the traversal.
	 *	Distance are added to the global dist map immediately once the
	 *	values are known.
	 *	@param		currNode		The current node being visited
	 *	@return					A map that stores the distances between the current
	 *						node and all its named offspring nodes
	 */
	protected Map buildCaches(NewickTreeNode currNode)
	{ 
//		Map currDistMap = new HashMap();	// The distance map to be returned

		String nodeId = currNode.getId();
		if (nodeId != null)
		{
			nodeMap.put(nodeId, currNode);
//			currDistMap.put(nodeId, new Float(0));
		}

		// Process the children recursively
		List children = currNode.getChildren();
		if (children.size() != 0)
		{
//			Map[] childDistMaps = new Map[children.size()];
			for (int i=0; i<children.size(); i++)
			{
				NewickTreeNode child = (NewickTreeNode)children.get(i);
//				float childDist = child.getDistToParent();
				Map tempMap = buildCaches(child);
//				childDistMaps[i] = new HashMap(tempMap.size());

/*
				Iterator it = tempMap.keySet().iterator();
				while (it.hasNext())
				{
					String offspringId = (String)it.next();
					float offspringDist = ((Float)tempMap.get(offspringId)).floatValue();
					childDistMaps[i].put(offspringId, new Float(offspringDist + childDist));
					currDistMap.put(offspringId, new Float(offspringDist + childDist));
				}
*/
			}
/*
			for (int i=0; i<childDistMaps.length; i++)
			{
				Map map1 = childDistMaps[i];
				for (int j=i+1; j<childDistMaps.length; j++)
				{
					Map map2 = childDistMaps[j];
					Iterator it1 = map1.keySet().iterator();

					while (it1.hasNext())
					{
						String id1 = (String)it1.next();
						float dist1 = ((Float)map1.get(id1)).floatValue();
						Iterator it2 = map2.keySet().iterator();

						while (it2.hasNext())
						{
							String id2 = (String)it2.next();
							float dist2 = ((Float)map2.get(id2)).floatValue();
							distMap.put(getDistMapKey(id1, id2), new Float(dist1 + dist2));
						}
					}
				}
			}
*/
		}
		else
			leafList.add(currNode);

//		return currDistMap;
		return null;
	}
}