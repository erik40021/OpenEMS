package io.openemes.edge.arduino.led;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import io.openemes.edge.arduino.led.ProviderImpl;

/*
 * Example JUNit test case
 *
 */

public class ProviderImplTest {

	/*
	 * Example test method
	 */

	@Test
	public void simple() {
		ProviderImpl impl = new ProviderImpl();
		assertNotNull(impl);
	}

}
