package com.daikit.graphql.spring.jpa.service;

import com.daikit.graphql.spring.jpa.repository.IEntityRepository;
import com.querydsl.core.types.dsl.EntityPathBase;

/**
 * Registry for quick access to beans and APT object related to persistence
 *
 * @author Thibaut Caselli
 */
public interface IPersistenceRegistry {

	/**
	 * Get a repository by entity class
	 *
	 * @param entityClass
	 *            the entity class
	 * @param <T>
	 *            the entity type
	 * @return the entity type
	 */
	<T> IEntityRepository<T> getRepository(Class<T> entityClass);

	/**
	 * Get an APT {@link EntityPathBase} from given entity class
	 *
	 * @param entityClass
	 *            the entity class
	 * @param <T>
	 *            the entity type
	 * @return the {@link EntityPathBase}
	 */
	<T> EntityPathBase<T> getEntityPath(Class<T> entityClass);

}
