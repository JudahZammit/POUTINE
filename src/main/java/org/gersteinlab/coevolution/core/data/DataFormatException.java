package org.gersteinlab.coevolution.core.data;

/**
 *	<pre>
 *	This class represents an exception due to invalid data format.
 *	</pre>
 *
 *	@author		Kevin Yip
 *	@version	1.0 (May 16, 2006)
 *
 *	<pre>
 *	Change history:
 *	1.0	- Initial version
 *	</pre>
 */

public class DataFormatException extends Exception
{
	/*----------------------------------------------------------------------
	 Constructors
	----------------------------------------------------------------------*/

	/**
	 *	Default constructor: create a new object without an error
	 *	message.
	 */
	public DataFormatException()
	{
		super();
	}

	/**
	 *	Create a new object with an error message.
	 *	@param		message			The error message
	 */
	public DataFormatException(String message)
	{
		super(message);
	}

	/**
	 *	Create a new object with the cause of this exception.
	 *	@param		cause			The cause of the exception
	 */
	public DataFormatException(Throwable cause)
	{
		super(cause);
	}

	/**
	 *	Create a new object with an error message and the cause of this
	 *	exception.
	 *	@param		message			The error message
	 *	@param		cause			The cause of the exception
	 */
	public DataFormatException(String message, Throwable cause)
	{
		super(message, cause);
	}
}