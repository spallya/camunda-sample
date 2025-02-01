package com.vider.quantum.engine.configuration;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.vider.quantum.engine.repository.vider",
        entityManagerFactoryRef = "secondaryEntityManagerFactory", 
        transactionManagerRef = "secondaryTransactionManager"
)
public class SecondaryDataSourceConfig {

    @Value("${spring.datasource.secondary.url}")
    private String secondaryDbUrl;

    @Value("${spring.datasource.secondary.username}")
    private String secondaryDbUsername;

    @Value("${spring.datasource.secondary.password}")
    private String secondaryDbPassword;

    @Value("${spring.datasource.secondary.driver-class-name}")
    private String secondaryDbDriverClassName;

    @Bean(name = "secondaryDataSource")
    public DataSource secondaryDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(secondaryDbDriverClassName);
        dataSource.setUrl(secondaryDbUrl);
        dataSource.setUsername(secondaryDbUsername);
        dataSource.setPassword(secondaryDbPassword);
        return dataSource;
    }

    @Bean(name = "secondaryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean secondaryEntityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                                @Qualifier("secondaryDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.vider.quantum.engine.entities.vider")
                .persistenceUnit("secondary")
                .build();
    }

    @Bean(name = "secondaryTransactionManager")
    public PlatformTransactionManager secondaryTransactionManager(@Qualifier("secondaryEntityManagerFactory") EntityManagerFactory secondaryEntityManagerFactory) {
        return new JpaTransactionManager(secondaryEntityManagerFactory);
    }
}
