package com.daikit.graphql.spring.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Super interface for composite entities repositories
 *
 * @author Thibaut Caselli
 *
 * @param <ENTITY_TYPE>
 *            the model type
 * @param <ID_TYPE>
 *            the type of the ID (may be simple type like Long, or composite key
 *            type)
 */
@NoRepositoryBean
public interface ICompositeEntityRepository<ENTITY_TYPE, ID_TYPE>
		extends
			JpaRepository<ENTITY_TYPE, ID_TYPE>,
			JpaSpecificationExecutor<ENTITY_TYPE>,
			QuerydslPredicateExecutor<ENTITY_TYPE> {

}
