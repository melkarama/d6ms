package test.com.d6ms;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.d6ms.DmsRepo;
import com.d6ms.DmsService;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ContextConfiguration(classes = BaseTestConfig.class)
@ActiveProfiles("test")
public class BaseTest {

	@PersistenceContext
	protected EntityManager em;

	@Inject
	protected DmsRepo dmsRepo;

	@Inject
	protected DmsService dmsService;

	@BeforeEach
	public void init() {
		System.out.println("Hello : " + em);

	}

}
