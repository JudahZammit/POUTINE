package org.gersteinlab.coevolution.core.io;

import java.io.*;

/**
 *	<pre>
 *	This class defines a tokenizer for a tree in the Newick/New Hampshire/
 *	PHYLIP format.
 *	</pre>
 *
 *	@author		Kevin Yuk-Lap Yip
 *	@version	1.0 (May 17, 2006)
 *
 *	<pre>
 *	Change History:
 *	1.0	- Initial version
 *	</pre>
 */
public class NewickTreeTokenizer
{
	/*----------------------------------------------------------------------
	 Class variables
	----------------------------------------------------------------------*/

	/**
	 *	The underlying reader.
	 */
	protected PushbackReader pr = null;


	/*----------------------------------------------------------------------
	 Public methods
	----------------------------------------------------------------------*/

	/**
	 *	Constructor: create a new tokenizer that reads from an
	 *	underlying input stream.
	 *	@param		is			The underlying input stream
	 */
	public NewickTreeTokenizer(InputStream is)
	{
		this.pr = new PushbackReader(new InputStreamReader(is));
	}

	/**
	 *	Constructor: create a new tokenizer that reads from an
	 *	underlying reader.
	 *	@param		r			The underlying reader
	 */
	public NewickTreeTokenizer(Reader r)
	{
		this.pr = new PushbackReader(r);
	}

	/**
	 *	Returns the next token.
	 *	@return					The next token,
	 *						null if no more
	 *	@exception	IOException		If an IO problem is encountered
	 */
	public String nextToken() throws IOException
	{
		StringBuffer token = new StringBuffer();

		boolean done = false;
		while (!done)
		{
			int i = pr.read();
			char ch = (char)i;
			switch(i)
			{
				case -1:				// No more
					done = true;
					break;
				case '(':				// Delimiters
				case ')':
				case ',':
				case ';':
				case ':':
					if (token.length() != 0)
						pr.unread(i);
					else
						token.append(ch);
					done = true;
					break;
				default:				// Others
					if (Character.isWhitespace(ch))
					{
						if (token.length() != 0)
							done = true;	// May push back, but doesn't matter
					}
					else
						token.append(ch);
					break;
			}
		}

		return (token.length() == 0) ?null :token.toString();
	}

	/**
	 *	Close the tokenizer.
	 *	@exception	IOException		If an IO problem is encountered
	 */
	public void close() throws IOException
	{
		pr.close();
	}

	/**
	 *	Clean up.
	 */
	public void finalize()
	{
		try { pr.close(); } catch (Exception e) {}
	}
}