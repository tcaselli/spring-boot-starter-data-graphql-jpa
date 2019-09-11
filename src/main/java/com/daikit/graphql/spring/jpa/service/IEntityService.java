package com.daikit.graphql.spring.jpa.service;

import java.util.Map;
import java.util.Optional;

import com.daikit.graphql.data.input.GQLListLoadConfig;
import com.daikit.graphql.data.output.GQLListLoadResult;
import com.daikit.graphql.datafetcher.GQLDynamicAttributeRegistry;

/**
 * Service allowing to execute persistence methods on no matter which entity
 * type
 *
 * @author Thibaut Caselli
 */
public interface IEntityService {

	/**
	 * Find an entity by its ID
	 *
	 * @param entityClass
	 *            the entity class
	 * @param id
	 *            the ID
	 * @return an {@link Optional} entity
	 */
	<T> Optional<T> findById(Class<T> entityClass, Long id);

	/**
	 * Find all entities with filter/paging/sorting according to given
	 * {@link GQLListLoadConfig}
	 *
	 * @param entityClass
	 *            the entity class
	 * @param listLoadConfig
	 *            the {@link GQLListLoadConfig}
	 * @return a {@link GQLListLoadResult}
	 */
	GQLListLoadResult findAll(Class<?> entityClass, GQLListLoadConfig listLoadConfig);

	/**
	 * Save an entity
	 *
	 * @param entity
	 *            the entity
	 */
	void save(Object entity);

	/**
	 * Delete an entity by its ID
	 *
	 * @param entityClass
	 *            the entity class
	 * @param id
	 *            the entity ID
	 */
	void delete(Class<?> entityClass, Long id);

	/**
	 * Find existing entity or create a new one and set properties
	 *
	 * @param entityClass
	 *            the entity class
	 * @param dynamicAttributeRegistry
	 *            the {@link GQLDynamicAttributeRegistry} for retrieving dynamic
	 *            attribute setters
	 * @param propertyValueMap
	 *            the map of properties to be set on object
	 * @return the found or created object with properties set
	 */
	<T> T findOrCreateAndSetProperties(Class<?> entityClass, GQLDynamicAttributeRegistry dynamicAttributeRegistry,
			Map<String, Object> propertyValueMap);

}
