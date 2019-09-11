package com.daikit.graphql.spring.jpa.usertype;

import java.util.Set;

/**
 * User type for String {@link Set}
 *
 * @author Thibaut Caselli
 */
public class StringSetUserType extends AbstractJsonSetUserType<String> {

	/**
	 * The name of this class for usage in annotations
	 */
	public static final String NAME = "com.daikit.graphql.spring.jpa.usertype.StringSetUserType";

}
