# Spring boot starter data graphql JPA

[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

## Documentation

Spring boot starter for GraphQL endpoint over a JPA persistence layer based on [java-data-graphql](https://github.com/tcaselli/java-data-graphql) library.  
See [spring-data-graphql-jpa-demo](https://github.com/tcaselli/spring-data-graphql-jpa-demo) for an example of usage.

The goal of this starter is to make the glue between a GraphQL engine and a SQL database. We have to automatically convert GraphQL queries and mutations to database SQL queries.  
To achieve this goal this starter is using these third party libraries :
* [JPA + hibernate](https://hibernate.org/) for managing persistence.
* [spring-data-jpa](https://spring.io/projects/spring-data-jpa) for integration of JPA within a Spring project.
* [querydsl](http://www.querydsl.com/) for JPA queries generation.
* [java-data-graphql](https://github.com/tcaselli/java-data-graphql) for the GraphQL layer.

### Configuration properties

When creating a GQLExecutor, you will have to provide a ```com.daikit.graphql.config.GQLSchemaConfig```. The default implementation takes these properties default values. You can override these properties in your ```application.properties``` file.

```properties
# the base package where your querydsl entity paths will be generated
spring.data.graphql.jpa.querydsl-parent-packages=com.daikit
```

### QueryDSL entity paths

In order to be able to automatically convert GraphQL queries to JPA queries, all entities referenced in GraphQL schema must have a corresponding queryDSL entityPath.  
These entityPaths can be generated thanks to [apt-maven-plugin](https://github.com/querydsl/apt-maven-plugin) (see [demo project](https://github.com/tcaselli/spring-data-graphql-jpa-demo) for example).

### Spring data JPA repositories

In order to be able to automatically convert GraphQL queries to JPA queries, all entities referenced in GraphQL schema must have a corresponding spring data repository interface extending ```com.daikit.graphql.spring.jpa.repository.IEntityRepository``` (see [demo project](https://github.com/tcaselli/spring-data-graphql-jpa-demo) for example).

### The persistence registry

The persistence registry (extending ```com.daikit.graphql.spring.jpa.service.IPersistenceRegistry```) with its default implementation ```com.daikit.graphql.spring.jpa.service.DefaultPersistenceRegistry``` (which can be overridden) is registering all entity paths and repositories and provide simple access by entity class for the entity service.

### The entity service

The entity service (extending ```com.daikit.graphql.spring.jpa.service.IEntityService```) is providing methods accessible by GraphQL data fetchers for CRUD operations on any entity types. It uses the persistence registry underthehood.

### Custom hibernate user types

When you want to store data as JSON in a SQL column you need to use hibernate custom ```org.hibernate.usertype.UserType```. These data can then be available in the GraphQL layer thanks to "embedded entities" (set the entity meta data as embedded when building the GraphQL meta model).  
In order to simplify the creation/update of these embedded entities you can extend some custom user types that will simplify your life. (See ```com.daikit.graphql.spring.jpa.usertype.AbstractJsonUserType``` and ```com.daikit.graphql.spring.jpa.usertype.AbstractJsonCollectionUserType``` and extending classes for for info) (see [demo project](https://github.com/tcaselli/spring-data-graphql-jpa-demo) for example)

## Where can I get the latest release?

You can check latest version and pull it from the [central Maven repositories](https://mvnrepository.com/artifact/com.daikit/spring-boot-starter-data-graphql):

With maven

```xml
<dependency>
    <groupId>com.daikit</groupId>
    <artifactId>spring-boot-starter-data-graphql-jpa</artifactId>
    <version>x.x</version>
</dependency>
```

Or with gradle 

```gradle
compile group: 'com.daikit', name: 'spring-boot-starter-data-graphql-jpa', version: 'x.x'
```

## Contributing

We accept Pull Requests via GitHub. There are some guidelines which will make applying PRs easier for us:
+ No spaces :) Please use tabs for indentation.
+ Respect the code style.
+ Create minimal diffs - disable on save actions like reformat source code or organize imports. If you feel the source code should be reformatted create a separate PR for this change.
+ Provide JUnit tests for your changes and make sure your changes don't break any existing tests by running ```mvn clean test```.

## License

This code is under the [Apache Licence v2](https://www.apache.org/licenses/LICENSE-2.0).