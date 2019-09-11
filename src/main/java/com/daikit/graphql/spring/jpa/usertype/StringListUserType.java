package com.daikit.graphql.spring.jpa.usertype;

import java.util.List;

/**
 * User type for String {@link List}
 *
 * @author Thibaut Caselli
 */
public class StringListUserType extends AbstractJsonListUserType<String> {

	/**
	 * The name of this class for usage in annotations
	 */
	public static final String NAME = "com.daikit.graphql.spring.jpa.usertype.StringListUserType";

}
