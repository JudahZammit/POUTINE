package org.gersteinlab.coevolution.core.io;

import org.gersteinlab.coevolution.core.data.*;

import java.io.*;
import java.util.*;

/**
 *	<pre>
 *	This class defines a reader for data in Newick Tree file format.
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

public class NewickTreeReader
{
	/*----------------------------------------------------------------------
	 Class constants
	----------------------------------------------------------------------*/

	// States
	protected static final char NODE_START	= 'S';
	protected static final char ID		= 'I';
	protected static final char COLON	= 'C';
	protected static final char DIST	= 'D';
	protected static final char NODE_END	= 'E';
	protected static final char SEMI_COLON	= 'F';

	/*----------------------------------------------------------------------
	 Class variables
	----------------------------------------------------------------------*/

	/**
	 *	The underlying tokenizer.
	 */
	protected NewickTreeTokenizer t = null;


	/*----------------------------------------------------------------------
	 Public methods
	----------------------------------------------------------------------*/

	/**
	 *	Constructor: create a new reader that reads from an underlying
	 *	tokenizer.
	 *	@param		t			The underlying tokenizer
	 */
	public NewickTreeReader(NewickTreeTokenizer t)
	{
		this.t = t;
	}

	/**
	 *	Read the tree. (Stop at the first semi-colon, ignore everything
	 *	after it.)
	 *	@exception	DataFormatException	If a data format problem is encountered
	 *	@exception	IOException		If an IO problem is encountered
	 */
	public NewickTree readTree() throws DataFormatException, IOException
	{
		char state = NODE_START;
		NewickTreeNode currNode = null;		// Always point to the node that has been created,
							// and is still active (i.e., may still have something
							// to do with it)

		while (state != SEMI_COLON)
		{
			String token = t.nextToken();
			if (token == null)		// No more tokens
				throw new DataFormatException("Unexpected end of data.");

			switch (state)
			{
				case NODE_START:	// Expecting a new node to be defined next
					if (token.equals("("))		// The node is an internal node
					{
						NewickTreeNode newNode = new NewickTreeNode();
						newNode.setParent(currNode);
						if (currNode != null)
							currNode.addChild(newNode);
						currNode = newNode;
//						state = NODE_START;	// These commented lines are just for completeness
					}
					else if (token.equals(")"))	// The node is a leaf node without name or distance, and no more siblings
					{
						NewickTreeNode newNode = new NewickTreeNode();
						newNode.setParent(currNode);
						if (currNode != null)
							currNode.addChild(newNode);
						else			// Multiple roots: not allowed
							reportFormatError(state, token);
//						currNode = currNode;
						state = NODE_END;
					}
					else if (token.equals(","))	// The node is a leaf node without name or distance, and a sibling is coming
					{
						NewickTreeNode newNode = new NewickTreeNode();
						newNode.setParent(currNode);
						if (currNode != null)
							currNode.addChild(newNode);
						else			// Multiple roots: not allowed
							reportFormatError(state, token);
//						currNode = currNode;
//						state = NODE_START;
					}
					else if (token.equals(":"))	// The node is a leaf node without name but with a distance
					{
						NewickTreeNode newNode = new NewickTreeNode();
						newNode.setParent(currNode);
						if (currNode != null)
							currNode.addChild(newNode);
						currNode = newNode;
						state = COLON;
					}
					else if (!isDelimiter(token))	// The node is a leaf node with a name (not known whether with a distance yet)
					{
						NewickTreeNode newNode = new NewickTreeNode();
						newNode.setParent(currNode);
						if (currNode != null)
							currNode.addChild(newNode);
						newNode.setId(token);
						currNode = newNode;
						state = ID;
					}
					else
						reportFormatError(state, token);
					break;
				case ID:		// Just read an ID
					if (token.equals(")"))		// Ending the parent node, which should be an internal node
					{
						currNode = currNode.getParent();
						if (currNode == null)	// Closing too many brackets
							reportFormatError(state, token);
						else
							state = NODE_END;
					}
					else if (token.equals(","))	// Going to start a sibling. Pass the control back to the parent first
					{
						currNode = currNode.getParent();
						if (currNode == null)	// Multiple roots: not allowed
							reportFormatError(state, token);
						else
							state = NODE_START;
					}
					else if (token.equals(":"))	// Going to receive the distance
						state = COLON;
					else if (token.equals(";"))	// No more nodes. Let the final check to determine if the tree is complete
						state = SEMI_COLON;
					else
						reportFormatError(state, token);
					break;
				case COLON:		// Just read a colon
					if (!isDelimiter(token))	// The distance: check if it is a valid number
					{
						try
						{
							float dist = Float.parseFloat(token);
							currNode.setDistToParent(dist);
							state = DIST;
						}
						catch (NumberFormatException nfe)
						{
							reportFormatError(state, token);
						}
					}
					else
						reportFormatError(state, token);
					break;
				case DIST:		// Just read the distance
					if (token.equals(")"))		// Ending the parent node, which should be an internal node
					{
						currNode = currNode.getParent();
						if (currNode == null)	// Closing too many brackets
							reportFormatError(state, token);
						else
							state = NODE_END;
					}
					else if (token.equals(","))
					{
						currNode = currNode.getParent();
						if (currNode == null)	// Multiple roots: not allowed
							reportFormatError(state, token);
						else
							state = NODE_START;
					}
					else if (token.equals(";"))
						state = SEMI_COLON;
					else
						reportFormatError(state, token);
					break;
				case NODE_END:		// Just read the close bracket of an internal node, may have the name and/or the distance coming
					if (token.equals(")"))	// Nothing comes, and ending the parent node
					{
						currNode = currNode.getParent();
						if (currNode == null)	// Closing too many brackets
							reportFormatError(state, token);
						else
							state = NODE_END;
					}
					else if (token.equals(","))	// Nothing comes, and is going to have a sibling node: just pass the control back to the parent
					{
						currNode = currNode.getParent();
						if (currNode == null)	// Multiple roots: not allowed
							reportFormatError(state, token);
						else
							state = NODE_START;
					}
					else if (token.equals(":"))	// No name, but expect a distance to come next
						state = COLON;
					else if (token.equals(";"))	// Nothing comes, and no more things to read
						state = SEMI_COLON;
					else if (!isDelimiter(token))	// A name comes
					{
						currNode.setId(token);
						state = ID;
					}
					else
						reportFormatError(state, token);
					break;
				default:		// Should never happen
					throw new DataFormatException("Unknown state.");
			}
		}

		if (currNode == null)
			throw new DataFormatException("More close brackets then open brackets");
		else if (currNode.getParent() != null)
			throw new DataFormatException("More open brackets then close brackets");
		return new NewickTree(currNode);
	}

	/**
	 *	Close the reader.
	 *	@exception	IOException		If an IO problem is encountered
	 */
	public void close() throws IOException
	{
		t.close();
	}

	/**
	 *	Clean up.
	 */
	public void finalize()
	{
		try { t.close(); } catch (Exception e) {}
	}

	/**
	 *	For testing purpose
	 */
/*
	public static void main(String argv[])
	{
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(argv[0]));
			String line = br.readLine();
			System.out.println("Input:");
			System.out.println(line);
			System.out.println();
			line = line.replaceAll("\\s", "");

			NewickTreeReader ntdr = new NewickTreeReader(new NewickTreeTokenizer(new FileReader(argv[0])));
			NewickTree tree = ntdr.readTree();
			ntdr.close();
			String outStr = tree.toString();
			System.out.println("Output:");
			System.out.println(outStr);
			System.out.println();

			if (line.equals(outStr))
				System.out.println("Equal");
			else
				System.out.println("Not equal");

			List leafNodes = tree.getLeafNodes();
			System.out.println("Leaf nodes:");
			for (int i=0; i<leafNodes.size(); i++)
				System.out.println(leafNodes.get(i));

			System.out.println("Leaf distances:");
			for (int i=0; i<leafNodes.size(); i++)
			{
				NewickTreeNode node1 = (NewickTreeNode)leafNodes.get(i);
				String id1 = node1.getId();
				if (id1 != null)
				{
					for (int j=i+1; j<leafNodes.size(); j++)
					{
						NewickTreeNode node2 = (NewickTreeNode)leafNodes.get(j);
						String id2 = node2.getId();
						if (id2 != null)
							System.out.println("Distance between " + id1 + " and " + id2 + ": " + tree.getDist(id1, id2));
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
*/

	/*----------------------------------------------------------------------
	 Protected methods
	----------------------------------------------------------------------*/

	/**
	 *	Check if the input token is a delimiter.
	 *	@param		token			The token
	 *	@return					True if the token is a delimiter,
	 *						false otherwise.
	 */
	public boolean isDelimiter(String token)
	{
		return token.equals("(") ||
		       token.equals(")") ||
		       token.equals(",") ||
		       token.equals(":") ||
		       token.equals(";");
	}

	/**
	 *	Report a format error.
	 *	@param		state			The current state
	 *	@param		token			The token being processed
	 *	@exception	DataFormatException	Always thrown to report tht error
	 */
	public void reportFormatError(char state, String token) throws DataFormatException
	{
		String stateStr = null;
		if (state == NODE_START)
			stateStr = "NODE_START";
		else if (state == ID)
			stateStr = "ID";
		else if (state == COLON)
			stateStr = "COLON";
		else if (state == DIST)
			stateStr = "DIST";
		else if (state == NODE_END)
			stateStr = "NODE_END";
		else if (state == SEMI_COLON)
			stateStr = "SEMI_COLON";

		throw new DataFormatException("Unexpected token [" + token + "] encountered in the " + stateStr + " state.");
	}
}