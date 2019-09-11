package com.daikit.graphql.spring.jpa;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.daikit.graphql.spring.jpa.service.DefaultEntityService;
import com.daikit.graphql.spring.jpa.service.DefaultPersistenceRegistry;
import com.daikit.graphql.spring.jpa.service.IEntityService;
import com.daikit.graphql.spring.jpa.service.IPersistenceRegistry;

/**
 * Spring data GraphQL JPA auto configuration
 *
 * @author Thibaut Caselli
 */
@Configuration
@EnableConfigurationProperties(SpringDataGraphqlJpaProperties.class)
public class SpringDataGraphqlJpaAutoconfiguration {

	/**
	 * Create the persistence registry
	 *
	 * @return a {@link DefaultPersistenceRegistry}
	 */
	@Bean
	@ConditionalOnMissingBean
	public IPersistenceRegistry createPersistenceRegistry() {
		return new DefaultPersistenceRegistry();
	}

	/**
	 * Create the entity service
	 *
	 * @return a {@link DefaultEntityService}
	 */
	@Bean
	@ConditionalOnMissingBean
	public IEntityService createEntityService() {
		return new DefaultEntityService();
	}

}
