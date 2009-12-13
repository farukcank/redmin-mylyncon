package org.svenk.redmine.core.client;

import java.io.InputStream;
import java.util.Collection;

import org.svenk.redmine.core.exception.RedmineException;

import junit.framework.TestCase;


public class RedmineResponseReaderTest extends TestCase {

	private RedmineResponseReader testee;
	
	protected void setUp() throws Exception {
		super.setUp();
		testee = new RedmineResponseReader();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testReadAuthenticityToken() throws RedmineException {
		InputStream in = getClass().getResourceAsStream("/response/authenticity.html");
		String token = testee.readAuthenticityToken(in);
		assertNotNull(token);
		assertEquals("46ecd7461ff4b05f247b49d39a3dddc86a44bc56", token);
	}
	
	public void testReadnExistsNoAuthenticityToken() throws RedmineException {
		InputStream in = getClass().getResourceAsStream("/response/noauthenticity.html");
		String token = testee.readAuthenticityToken(in);
		assertNull(token);
	}
	
	public void testReadError_exists() throws RedmineException {
		InputStream in = getClass().getResourceAsStream("/response/failure.html");
		
		Collection<String> errors = testee.readErrors(in);
		assertNotNull(errors);
		assertEquals(2, errors.size());
		assertTrue(errors.contains("« Subject » can't be blank"));
		assertTrue(errors.contains("« Due date » must be greater than start date"));
	}

	public void testReadError_nonexists() throws RedmineException {
		InputStream in = getClass().getResourceAsStream("/response/success.html");
		
		Collection<String> errors = testee.readErrors(in);
		assertNull(errors);
	}
	
}
