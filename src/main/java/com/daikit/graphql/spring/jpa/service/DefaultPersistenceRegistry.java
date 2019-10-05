package com.daikit.graphql.spring.jpa.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.text.WordUtils;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import com.daikit.generics.utils.GenericsUtils;
import com.daikit.graphql.utils.Assert;
import com.querydsl.core.types.dsl.EntityPathBase;

/**
 * Default persistence registry {@link IPersistenceRegistry}
 *
 * @author Thibaut Caselli
 */
public class DefaultPersistenceRegistry implements IPersistenceRegistry, ApplicationListener<ContextRefreshedEvent> {

	@Autowired
	private ApplicationContext applicationContext;

	private final Map<Class<?>, JpaRepository<?, ?>> repositories = new HashMap<>();
	private final Set<Class<?>> allEntityTypes = new HashSet<>();
	private final Map<Class<?>, EntityPathBase<?>> entityPaths = new HashMap<>();

	private final Collection<String> queryDslParentPackages;

	// *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	// CONSTRUCTORS
	// *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	/**
	 * Constructor.
	 *
	 * @param queryDslParentPackages
	 *            a collection of query DSL entity path base packages (where apt
	 *            plugin generates querydsl entity paths)
	 */
	public DefaultPersistenceRegistry(final String... queryDslParentPackages) {
		this(Arrays.asList(queryDslParentPackages));
	}

	/**
	 * Constructor.
	 *
	 * @param queryDslParentPackages
	 *            a collection of query DSL entity path base packages (where apt
	 *            plugin generates querydsl entity paths)
	 */
	public DefaultPersistenceRegistry(final Collection<String> queryDslParentPackages) {
		Assert.assertTrue(
				queryDslParentPackages.size() > 0
						&& !(queryDslParentPackages.size() == 1 && queryDslParentPackages.iterator().next() == null),
				"There should be at least one query DSL entity path base package");
		this.queryDslParentPackages = queryDslParentPackages;
	}

	// *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	// PUBLIC METHODS
	// *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	/**
	 * Bean initialization method
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void onApplicationEvent(final ContextRefreshedEvent event) {
		// Register entities
		for (final EntityManagerFactory entityManagerFactory : applicationContext
				.getBeansOfType(EntityManagerFactory.class, false, false).values()) {
			for (final EntityType<?> entityType : entityManagerFactory.getMetamodel().getEntities()) {
				allEntityTypes.add(entityType.getJavaType());
			}
		}
		// Register repositories for entities
		for (final JpaRepository<?, ?> repository : applicationContext.getBeansOfType(JpaRepository.class, false, false)
				.values()) {
			final Class<?> repositoryEntityClass = GenericsUtils
					.getTypeArgumentsAsClasses(repository.getClass(), JpaRepository.class).get(0);
			repositories.put(repositoryEntityClass, repository);
		}
		// Register queryDSL for each entity
		final Reflections reflections = new Reflections(queryDslParentPackages);
		final Set<Class<? extends EntityPathBase>> entityPathClasses = reflections.getSubTypesOf(EntityPathBase.class);
		for (final Class<? extends EntityPathBase> entityPathClass : entityPathClasses) {
			final Class<?> entityClass = GenericsUtils.getTypeArgumentsAsClasses(entityPathClass, EntityPathBase.class)
					.get(0);
			EntityPathBase<?> entityPath;
			try {
				entityPath = (EntityPathBase<?>) FieldUtils.readDeclaredStaticField(entityPathClass,
						WordUtils.uncapitalize(entityClass.getSimpleName()));
			} catch (final IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			entityPaths.put(entityClass, entityPath);
		}
		// Validate all repositories and queryDSL are present
		for (final Class<?> entityClass : allEntityTypes) {
			final Object repository = repositories.get(entityClass);
			if (repository == null) {
				throw new RuntimeException("No repository registered for entity : " + entityClass.getName());
			}
			if (!(repository instanceof QuerydslPredicateExecutor)) {
				throw new RuntimeException(
						"Repository should be extending " + QuerydslPredicateExecutor.class.getSimpleName() + " and "
								+ JpaRepository.class.getSimpleName() + " : " + entityClass.getName());
			}
			if (!entityPaths.containsKey(entityClass)) {
				throw new RuntimeException("No queryDSL class registered for entity : " + entityClass.getName());
			}
		}
	}

	// *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	// PUBLIC METHODS
	// *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	@SuppressWarnings("unchecked")
	@Override
	public <ENTITY_TYPE, ID_TYPE, RETURN_TYPE extends JpaRepository<ENTITY_TYPE, ID_TYPE> & QuerydslPredicateExecutor<ENTITY_TYPE>> RETURN_TYPE getRepository(
			final Class<ENTITY_TYPE> entityClass) {
		final Object repository = repositories.get(entityClass);
		if (repository == null) {
			throw new IllegalArgumentException("No repository registered for entity : " + entityClass.getName());
		}
		return (RETURN_TYPE) repository;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> EntityPathBase<T> getEntityPath(final Class<T> entityClass) {
		final EntityPathBase<T> entityPath = (EntityPathBase<T>) entityPaths.get(entityClass);
		if (entityPath == null) {
			throw new IllegalArgumentException("No APT entity path registered for entity : " + entityClass.getName());
		}
		return entityPath;
	}
}
