package com.daikit.graphql.spring.jpa.usertype;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Json user type for {@link List}
 *
 * @author Thibaut Caselli
 * @param <JAVA_VALUE_DATA_TYPE>
 *            the list generic type
 */
public abstract class AbstractJsonListUserType<JAVA_VALUE_DATA_TYPE>
		extends
			AbstractJsonCollectionUserType<JAVA_VALUE_DATA_TYPE> {

	@Override
	public Class<?> returnedClass() {
		return ArrayList.class;
	}

	@Override
	protected TypeReference<?> getTypeReference() {
		return new TypeReference<List<JAVA_VALUE_DATA_TYPE>>() {
			// Nothing done
		};
	}

}
