package org.nem.nac.common.models;

import junit.framework.Assert;

import org.junit.Test;
import org.nem.nac.common.enums.NetworkVersion;
import org.nem.nac.models.NacPublicKey;
import org.nem.nac.models.primitives.AddressValue;

public class AddressValueTest {

	@Test
	public void testFromPublicKey() throws Exception {
		final NacPublicKey pubKey = new NacPublicKey("f5496c59ff336ae2d497140f0ad48306092beeda0d36c31c7373103956c90261");
		final AddressValue expected = new AddressValue("TBUD2C7EECXPZLCFUP3WHOG57OZSY4Q3WBMPVTW3");

		final AddressValue actual = AddressValue.fromPublicKey(NetworkVersion.TEST_NETWORK.get(), pubKey);

		Assert.assertEquals(expected, actual);
	}
}
