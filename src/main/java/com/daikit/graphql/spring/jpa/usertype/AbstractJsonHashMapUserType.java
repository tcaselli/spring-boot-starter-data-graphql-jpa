package com.daikit.graphql.spring.jpa.usertype;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Abstract super type for {@link Map} stored as Json
 * 
 * @param <KEY_TYPE>
 *            the map key generic type
 * @param <VALUE_TYPE>
 *            the map value generic type
 * @author Thibaut Caselli
 */
public abstract class AbstractJsonHashMapUserType<KEY_TYPE, VALUE_TYPE>
		extends
			AbstractJsonCollectionUserType<Entry<KEY_TYPE, VALUE_TYPE>> {

	@SuppressWarnings("rawtypes")
	@Override
	public Class returnedClass() {
		return HashMap.class;
	}

}
