package io.graphys.wfdb;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = TestConfiguration.class)
@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:test.properties")
public class BaseTests {
    @Value("${path.local.home_dir}")
    protected String LOCAL_HOME;

    @Value("${path.remote.home_dir}")
    protected String REMOTE_HOME;

    @Value("${cache.path.limit}")
    protected int PATH_CACHING_LIMIT;

    @Value("${cache.record.limit}")
    protected int RECORD_CACHING_LIMIT;
}
