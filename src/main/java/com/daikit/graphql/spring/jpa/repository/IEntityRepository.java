package com.daikit.graphql.spring.jpa.repository;

import org.springframework.data.repository.NoRepositoryBean;

/**
 * Super interface for entities repositories
 *
 * @author Thibaut Caselli
 *
 * @param <ENTITY_TYPE>
 *            the model type
 */
@NoRepositoryBean
public interface IEntityRepository<ENTITY_TYPE> extends ICompositeEntityRepository<ENTITY_TYPE, Long> {

}
