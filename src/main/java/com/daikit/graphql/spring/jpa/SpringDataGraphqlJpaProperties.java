package com.daikit.graphql.spring.jpa;

import org.springframework.boot.context.properties.ConfigurationProperties;
/**
 * Spring data GraphQL JPA properties
 *
 * @author Thibaut Caselli
 */
@ConfigurationProperties(prefix = "spring.data.graphql.jpa", ignoreUnknownFields = true)
public class SpringDataGraphqlJpaProperties {

}
