package com.daikit.graphql.spring.jpa.usertype;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Json user type for {@link Set}
 *
 * @author Thibaut Caselli
 * @param <JAVA_VALUE_DATA_TYPE>
 *            the set generic type
 */
public abstract class AbstractJsonSetUserType<JAVA_VALUE_DATA_TYPE>
		extends
			AbstractJsonCollectionUserType<JAVA_VALUE_DATA_TYPE> {

	@Override
	public Class<?> returnedClass() {
		return HashSet.class;
	}

	@Override
	protected TypeReference<?> getTypeReference() {
		return new TypeReference<Set<JAVA_VALUE_DATA_TYPE>>() {
			// Nothing done
		};
	}
}
