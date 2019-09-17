package com.daikit.graphql.spring.jpa.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QPageRequest;

import com.daikit.graphql.config.GQLSchemaConfig;
import com.daikit.graphql.data.input.GQLFilterEntry;
import com.daikit.graphql.data.input.GQLListLoadConfig;
import com.daikit.graphql.data.output.GQLListLoadResult;
import com.daikit.graphql.data.output.GQLOrderByEntry;
import com.daikit.graphql.datafetcher.GQLDynamicAttributeRegistry;
import com.daikit.graphql.dynamicattribute.IGQLDynamicAttributeSetter;
import com.daikit.graphql.enums.GQLFilterOperatorEnum;
import com.daikit.graphql.enums.GQLOrderByDirectionEnum;
import com.daikit.graphql.spring.jpa.repository.IEntityRepository;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.ArrayPath;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.CollectionPathBase;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.DslPath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.MapPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.core.types.dsl.TimePath;

/**
 * Implementation of {@link IEntityService}
 *
 * @author Thibaut Caselli
 */
public class DefaultEntityService implements IEntityService {

	@Autowired
	private IPersistenceRegistry persistenceRegistry;
	@Autowired
	private GQLSchemaConfig schemaConfig;

	// *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	// PUBLIC METHODS
	// *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	@Override
	public <T> Optional<T> findById(final Class<T> entityClass, final Long id) {
		return persistenceRegistry.getRepository(entityClass).findById(id);
	}

	@Override
	public GQLListLoadResult findAll(final Class<?> entityClass, final GQLListLoadConfig listLoadConfig) {
		final GQLListLoadResult result = new GQLListLoadResult();

		final IEntityRepository<?> repository = persistenceRegistry.getRepository(entityClass);
		final EntityPathBase<?> basePath = persistenceRegistry.getEntityPath(entityClass);

		// Apply filtering
		Predicate predicate = null;
		if (listLoadConfig.isFiltered()) {
			BooleanExpression expression = null;
			for (final GQLFilterEntry filter : listLoadConfig.getFilters()) {
				BooleanExpression current;
				try {
					current = applyFilter(basePath, filter);
				} catch (final IllegalAccessException e) {
					throw new RuntimeException(e);
				}
				expression = expression == null ? current : expression.and(current);
			}
			predicate = expression;
		}

		// Apply ordering
		final List<OrderSpecifier<?>> orders = new ArrayList<>();
		if (listLoadConfig.isOrdered()) {
			for (final GQLOrderByEntry orderBy : listLoadConfig.getOrderBy()) {
				try {
					orders.add(applyOrderBy(basePath, orderBy));
				} catch (final IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}

		final OrderSpecifier<?>[] orderSpecifiers = orders.toArray(new OrderSpecifier[orders.size()]);

		// Apply paging
		if (listLoadConfig.isPaged()) {
			final int page = listLoadConfig.getOffset() % listLoadConfig.getLimit();
			final Pageable pageable = new QPageRequest(page, listLoadConfig.getLimit(), orderSpecifiers);
			final Page<?> resultPage = repository.findAll(predicate, pageable);
			result.setData(resultPage.getContent());
			result.setPaging(listLoadConfig.getLimit(), listLoadConfig.getOffset(),
					Long.valueOf(resultPage.getTotalElements()).intValue());
			result.setOrderBy(listLoadConfig.getOrderBy());
		}
		// Else if no paging then return all data
		else {
			final List<Object> data = new ArrayList<>();
			repository.findAll(predicate, orderSpecifiers).forEach(data::add);
			result.setData(data);
			result.setOrderBy(listLoadConfig.getOrderBy());
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void save(final Object entity) {
		persistenceRegistry.getRepository((Class<Object>) entity.getClass()).save(entity);
	}

	@Override
	public void delete(final Class<?> entityClass, final Long id) {
		persistenceRegistry.getRepository(entityClass).deleteById(id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T findOrCreateAndSetProperties(final Class<T> entityClass,
			final GQLDynamicAttributeRegistry dynamicAttributeRegistry, final Map<String, Object> propertyValueMap) {
		// Find or create entity
		final String id = (String) propertyValueMap.get(schemaConfig.getAttributeIdName());

		Object entity;

		if (StringUtils.isEmpty(id)) {
			try {
				entity = entityClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		} else {
			final Optional<?> existing = findById((Class<?>) entityClass, Long.valueOf(id));
			if (existing.isPresent()) {
				entity = existing.get();
			} else {
				throw new RuntimeException("No [" + entityClass.getName() + "] exists with ID : " + id);
			}
		}
		// Set properties
		for (final Entry<String, Object> entry : propertyValueMap.entrySet()) {
			final Optional<IGQLDynamicAttributeSetter<Object, Object>> dynamicAttributeSetter = dynamicAttributeRegistry
					.getSetter(entityClass, entry.getKey());
			if (!schemaConfig.getAttributeIdName().equals(entry.getKey())) {
				Object value = entry.getValue();
				if (entry.getValue() instanceof Map) {
					final Class<?> propertyType = FieldUtils.getField(entity.getClass(), entry.getKey(), true)
							.getType();
					value = findOrCreateAndSetProperties(propertyType, dynamicAttributeRegistry,
							(Map<String, Object>) entry.getValue());
				}
				if (dynamicAttributeSetter.isPresent()) {
					dynamicAttributeSetter.get().setValue(entity, value);
				} else {
					try {
						FieldUtils.writeField(entity, entry.getKey(), value, true);
					} catch (final IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		return (T) entity;
	}

	// *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	// PRIVATE METHODS
	// *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	@SuppressWarnings("unchecked")
	protected OrderSpecifier<?> applyOrderBy(final EntityPathBase<?> basePath, final GQLOrderByEntry orderBy)
			throws IllegalAccessException {
		final Path<? extends Comparable<?>> path = (Path<? extends Comparable<?>>) FieldUtils.readField(basePath,
				orderBy.getField(), true);
		return new OrderSpecifier<>(
				GQLOrderByDirectionEnum.DESC.equals(orderBy.getDirection()) ? Order.DESC : Order.ASC, path);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	protected BooleanExpression applyFilter(final EntityPathBase<?> basePath, final GQLFilterEntry filter)
			throws IllegalAccessException {
		BooleanExpression expression;
		if (filter.isDynamic()) {
			// TODO
			expression = null;
		} else {
			final Path<?> path = (Path<?>) FieldUtils.readField(basePath, filter.getFieldName(), true);
			final GQLFilterOperatorEnum operator = filter.getOperator();
			final Object value = filter.getValue();
			if (path instanceof ArrayPath) {
				expression = applyFilter((ArrayPath<?, ?>) path, operator, value);
			} else if (path instanceof EntityPathBase) {
				expression = applyFilter((EntityPathBase<?>) path, operator, value);
			} else if (path instanceof SimplePath) {
				expression = applyFilter((SimplePath<?>) path, operator, value);
			} else if (path instanceof BooleanPath) {
				expression = applyFilter((BooleanPath) path, operator, value);
			} else if (path instanceof CollectionPathBase) {
				expression = applyFilter((CollectionPathBase<?, ?, ?>) path, operator, value);
			} else if (path instanceof ComparablePath) {
				expression = applyFilter((ComparablePath<?>) path, operator, value);
			} else if (path instanceof DatePath) {
				expression = applyFilter((DatePath<?>) path, operator, value);
			} else if (path instanceof DateTimePath) {
				expression = applyFilter((DateTimePath<?>) path, operator, value);
			} else if (path instanceof DslPath) {
				expression = applyFilter((DslPath<?>) path, operator, value);
			} else if (path instanceof EnumPath) {
				expression = applyFilter((EnumPath<?>) path, operator, value);
			} else if (path instanceof MapPath) {
				expression = applyFilter((MapPath<?, ?, ?>) path, operator, value);
			} else if (path instanceof NumberPath) {
				expression = applyFilter((NumberPath) path, operator, value);
			} else if (path instanceof StringPath) {
				expression = applyFilter((StringPath) path, operator, value);
			} else if (path instanceof TimePath) {
				expression = applyFilter((TimePath<?>) path, operator, value);
			} else {
				throw new IllegalArgumentException(
						"Unhandled attribute type with path : " + path == null ? null : path.getClass().getName());
			}
		}
		return expression;
	}

	protected BooleanExpression applyFilter(final DslPath<?> path, final GQLFilterOperatorEnum operator,
			final Object value) {
		throw new IllegalArgumentException("Unhandled path : " + path);
	}

	protected BooleanExpression applyFilter(final MapPath<?, ?, ?> path, final GQLFilterOperatorEnum operator,
			final Object value) {
		throw new IllegalArgumentException("Unhandled path : " + path);
	}

	protected BooleanExpression applyFilter(final CollectionPathBase<?, ?, ?> path,
			final GQLFilterOperatorEnum operator, final Object value) {
		throw new IllegalArgumentException("Unhandled path : " + path);
	}

	protected <T> BooleanExpression applyFilter(final ArrayPath<T, ?> path, final GQLFilterOperatorEnum operator,
			final Object value) {
		return applyFilter((SimpleExpression<T>) path, operator, value);
	}

	protected <T> BooleanExpression applyFilter(final EntityPathBase<T> path, final GQLFilterOperatorEnum operator,
			final Object value) {
		return applyFilter((SimpleExpression<T>) path, operator, value);
	}

	protected <T> BooleanExpression applyFilter(final SimplePath<T> path, final GQLFilterOperatorEnum operator,
			final Object value) {
		return applyFilter((SimpleExpression<T>) path, operator, value);
	}

	@SuppressWarnings("unchecked")
	protected <T> BooleanExpression applyFilter(final SimpleExpression<T> path, final GQLFilterOperatorEnum operator,
			final Object value) {
		BooleanExpression expression;
		switch (operator) {
			case NULL :
				expression = path.isNull();
				break;
			case NOT_NULL :
				expression = path.isNotNull();
				break;
			case EQUAL :
				expression = path.eq((T) value);
				break;
			case NOT_EQUAL :
				expression = path.ne((T) value);
				break;
			case IN :
				expression = path.in((Collection<T>) value);
				break;
			case NOT_IN :
				expression = path.notIn((Collection<T>) value);
				break;
			default :
				throw new IllegalArgumentException("Unhandled operator : " + operator);
		}
		return expression;
	}

	protected <T extends Comparable<T>> BooleanExpression applyFilter(final ComparablePath<T> path,
			final GQLFilterOperatorEnum operator, final Object value) {
		return applyFilter((ComparableExpression<T>) path, operator, value);
	}

	protected <T extends Comparable<T>> BooleanExpression applyFilter(final DatePath<T> path,
			final GQLFilterOperatorEnum operator, final Object value) {
		return applyFilter((ComparableExpression<T>) path, operator, value);
	}

	protected <T extends Comparable<T>> BooleanExpression applyFilter(final DateTimePath<T> path,
			final GQLFilterOperatorEnum operator, final Object value) {
		return applyFilter((ComparableExpression<T>) path, operator, value);
	}

	protected <T extends Comparable<T>> BooleanExpression applyFilter(final TimePath<T> path,
			final GQLFilterOperatorEnum operator, final Object value) {
		return applyFilter((ComparableExpression<T>) path, operator, value);
	}

	@SuppressWarnings("unchecked")
	protected <T extends Comparable<T>> BooleanExpression applyFilter(final ComparableExpression<T> path,
			final GQLFilterOperatorEnum operator, final Object value) {
		BooleanExpression expression;
		switch (operator) {
			case NULL :
				expression = path.isNull();
				break;
			case NOT_NULL :
				expression = path.isNotNull();
				break;
			case EQUAL :
				expression = path.eq((T) value);
				break;
			case NOT_EQUAL :
				expression = path.ne((T) value);
				break;
			case GREATER_THAN :
				expression = path.gt((T) value);
				break;
			case GREATER_EQUAL :
				expression = path.gt((T) value).or(path.eq((T) value));
				break;
			case LOWER_THAN :
				expression = path.lt((T) value);
				break;
			case LOWER_EQUAL :
				expression = path.lt((T) value).or(path.eq((T) value));
				break;
			case IN :
				expression = path.in((Collection<T>) value);
				break;
			case NOT_IN :
				expression = path.notIn((Collection<T>) value);
				break;
			default :
				throw new IllegalArgumentException("Unhandled operator : " + operator);
		}
		return expression;
	}

	@SuppressWarnings("unchecked")
	protected BooleanExpression applyFilter(final BooleanPath path, final GQLFilterOperatorEnum operator,
			final Object value) {
		BooleanExpression expression;
		switch (operator) {
			case NULL :
				expression = path.isNull();
				break;
			case NOT_NULL :
				expression = path.isNotNull();
				break;
			case EQUAL :
				expression = path.eq((Boolean) value);
				break;
			case NOT_EQUAL :
				expression = path.ne((Boolean) value);
				break;
			case IN :
				expression = path.in((Collection<Boolean>) value);
				break;
			case NOT_IN :
				expression = path.notIn((Collection<Boolean>) value);
				break;
			default :
				throw new IllegalArgumentException("Unhandled operator : " + operator);
		}
		return expression;
	}

	@SuppressWarnings("unchecked")
	protected <T extends Enum<T>> BooleanExpression applyFilter(final EnumPath<T> path,
			final GQLFilterOperatorEnum operator, final Object value) {
		BooleanExpression expression;
		switch (operator) {
			case NULL :
				expression = path.isNull();
				break;
			case NOT_NULL :
				expression = path.isNotNull();
				break;
			case EQUAL :
				expression = path.eq((T) value);
				break;
			case NOT_EQUAL :
				expression = path.ne((T) value);
				break;
			case IN :
				expression = path.in((Collection<T>) value);
				break;
			case NOT_IN :
				expression = path.notIn((Collection<T>) value);
				break;
			default :
				throw new IllegalArgumentException("Unhandled operator : " + operator);
		}
		return expression;
	}

	@SuppressWarnings("unchecked")
	protected <T extends Number & Comparable<T>> BooleanExpression applyFilter(final NumberPath<T> path,
			final GQLFilterOperatorEnum operator, final Object value) {
		BooleanExpression expression;
		switch (operator) {
			case NULL :
				expression = path.isNull();
				break;
			case NOT_NULL :
				expression = path.isNotNull();
				break;
			case EQUAL :
				expression = path.eq((T) value);
				break;
			case NOT_EQUAL :
				expression = path.ne((T) value);
				break;
			case GREATER_THAN :
				expression = path.gt((T) value);
				break;
			case GREATER_EQUAL :
				expression = path.gt((T) value).or(path.eq((T) value));
				break;
			case LOWER_THAN :
				expression = path.lt((T) value);
				break;
			case LOWER_EQUAL :
				expression = path.lt((T) value).or(path.eq((T) value));
				break;
			case IN :
				expression = path.in((Collection<T>) value);
				break;
			case NOT_IN :
				expression = path.notIn((Collection<T>) value);
				break;
			default :
				throw new IllegalArgumentException("Unhandled operator : " + operator);
		}
		return expression;
	}

	@SuppressWarnings("unchecked")
	protected BooleanExpression applyFilter(final StringPath path, final GQLFilterOperatorEnum operator,
			final Object value) {
		BooleanExpression expression;
		switch (operator) {
			case EMPTY :
				expression = path.isEmpty();
				break;
			case NOT_EMPTY :
				expression = path.isNotEmpty();
				break;
			case NULL :
				expression = path.isNull();
				break;
			case NOT_NULL :
				expression = path.isNotNull();
				break;
			case ENDS_WITH :
				expression = path.endsWith((String) value);
				break;
			case STARTS_WITH :
				expression = path.startsWith((String) value);
				break;
			case CONTAINS :
				expression = path.contains((String) value);
				break;
			case LIKE :
				expression = path.like((String) value);
				break;
			case EQUAL :
				expression = path.eq((String) value);
				break;
			case NOT_EQUAL :
				expression = path.ne((String) value);
				break;
			case IN :
				expression = path.in((Collection<String>) value);
				break;
			case NOT_IN :
				expression = path.notIn((Collection<String>) value);
				break;
			default :
				throw new IllegalArgumentException("Unhandled operator : " + operator);
		}
		return expression;
	}

}
