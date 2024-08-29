package test.com.d6ms;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;

import com.d6ms.DmsRepo;
import com.d6ms.DmsService;
import com.d6ms.entity.Store;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ContextConfiguration(classes = BaseTestConfig.class)
public class BaseTest {

	@PersistenceContext
	protected EntityManager em;

	@Inject
	protected DmsRepo dmsRepo;

	@Inject
	protected DmsService dmsService;

	@Inject
	protected TrxService trxService;

	protected static String storeId;

	@BeforeEach
	public void init() throws Exception {

		Store store = new Store();
		store.setName(UUID.randomUUID().toString());

		trxService.execute(() -> {
			em.persist(store);
			return null;
		});

		storeId = store.getId();
	}

}
