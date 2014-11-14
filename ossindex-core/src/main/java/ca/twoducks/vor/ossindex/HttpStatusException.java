package ca.twoducks.vor.ossindex;

import java.io.IOException;
import java.net.URL;

/**
 * 
 * @author Ken Duck
 *
 */
public class HttpStatusException extends IOException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2117754883074571568L;

	/**
	 * 
	 * @param url
	 * @param code
	 */
	public HttpStatusException(URL url, int code)
	{
		super("Unexpected return code (" + code + ") for request [" + url + "]");
	}

}
