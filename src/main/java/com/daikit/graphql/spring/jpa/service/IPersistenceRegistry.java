package com.daikit.graphql.spring.jpa.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

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
	 * @param <ENTITY_TYPE>
	 *            the entity type
	 * @param <ID_TYPE>
	 *            the id type
	 * @param <RETURN_TYPE>
	 *            the return type
	 * @return the entity type
	 */
	<ENTITY_TYPE, ID_TYPE, RETURN_TYPE extends JpaRepository<ENTITY_TYPE, ID_TYPE> & QuerydslPredicateExecutor<ENTITY_TYPE>> RETURN_TYPE getRepository(
			Class<ENTITY_TYPE> entityClass);

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
