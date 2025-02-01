package com.vider.quantum.engine.configuration;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.vider.quantum.engine.repository.camunda",
        entityManagerFactoryRef = "primaryEntityManagerFactory", 
        transactionManagerRef = "primaryTransactionManager"
)
public class PrimaryDataSourceConfig {

    @Value("${spring.datasource.primary.url}")
    private String primaryDbUrl;

    @Value("${spring.datasource.primary.username}")
    private String primaryDbUsername;

    @Value("${spring.datasource.primary.password}")
    private String primaryDbPassword;

    @Value("${spring.datasource.primary.driver-class-name}")
    private String primaryDbDriverClassName;

    @Primary
    @Bean(name = "primaryDataSource")
    public DataSource primaryDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(primaryDbDriverClassName);
        dataSource.setUrl(primaryDbUrl);
        dataSource.setUsername(primaryDbUsername);
        dataSource.setPassword(primaryDbPassword);
        return dataSource;
    }

    @Primary
    @Bean(name = "primaryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                               @Qualifier("primaryDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.vider.quantum.engine.entities.camunda")
                .persistenceUnit("primary")
                .build();
    }

    @Primary
    @Bean(name = "primaryTransactionManager")
    public PlatformTransactionManager primaryTransactionManager(@Qualifier("primaryEntityManagerFactory") EntityManagerFactory primaryEntityManagerFactory) {
        return new JpaTransactionManager(primaryEntityManagerFactory);
    }
}
