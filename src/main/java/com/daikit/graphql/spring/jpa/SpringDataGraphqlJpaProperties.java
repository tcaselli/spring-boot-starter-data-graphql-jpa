package com.daikit.graphql.spring.jpa;

import org.springframework.boot.context.properties.ConfigurationProperties;
/**
 * Spring data GraphQL JPA properties
 *
 * @author Thibaut Caselli
 */
@ConfigurationProperties(prefix = "spring.data.graphql.jpa", ignoreUnknownFields = true)
public class SpringDataGraphqlJpaProperties {

	private String querydslParentPackages;

	// *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	// GETTERS / SETTERS
	// *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	/**
	 * @return the querydslParentPackages
	 */
	public String getQuerydslParentPackages() {
		return querydslParentPackages;
	}

	/**
	 * @param querydslParentPackages
	 *            the querydslParentPackages to set
	 */
	public void setQuerydslParentPackages(String querydslParentPackages) {
		this.querydslParentPackages = querydslParentPackages;
	}

}
