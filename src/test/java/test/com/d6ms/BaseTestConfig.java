package test.com.d6ms;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.d6ms.DmsRepo;
import com.d6ms.DmsService;
import com.d6ms.TrxService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableJpaRepositories
@EnableTransactionManagement
public class BaseTestConfig {

	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource ds = new DriverManagerDataSource();
		ds.setDriverClassName("org.hsqldb.jdbc.JDBCDriver");
		ds.setUrl("jdbc:hsqldb:mem:testdb;DB_CLOSE_DELAY=-1");
		ds.setUsername("sa");
		ds.setPassword("");
		return ds;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource ds) {
		LocalContainerEntityManagerFactoryBean emfBean = new LocalContainerEntityManagerFactoryBean();
		emfBean.setDataSource(ds);
		emfBean.setPackagesToScan("com.d6ms.entity");
		emfBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

		Properties props = new Properties();
		props.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
		props.setProperty("hibernate.hbm2ddl.auto", "create-drop");
		props.setProperty("hibernate.globally_quoted_identifiers", "true");
		emfBean.setJpaProperties(props);

		return emfBean;
	}

	@Bean
	public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
		return new JpaTransactionManager(emf);
	}

	@Bean
	public DmsService dmsRepo(DmsRepo dmsRepo) {
		return new DmsService(dmsRepo);
	}

	@Bean
	public DmsRepo dmsService(EntityManager em) {
		return new DmsRepo(em);
	}

	@Bean
	public TrxService trxService() {
		return new TrxService();
	}

}
