package test.com.d6ms;

import java.util.concurrent.Callable;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class TrxService {

	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public <R> R execute(Callable<R> c) throws Exception {
		return c.call();
	}

}
