package com.ciscospark.helloworld;

import com.cisco.wx2.dto.User;
import com.cisco.wx2.server.auth.AuthInfo;
import com.cisco.wx2.server.config.ConfigProperties;
import com.ciscospark.helloworld.api.Greeting;
import com.ciscospark.server.CiscoSparkServerProperties;
import com.codahale.metrics.MetricRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;

/**
 * The purpose of this test is to show how to consume a stubbed version of the Feature Service using
 * the Spring Cloud Contract framework. Once the stubs are generated by the Producer, they are packaged
 * in a jar and finally published, so they can be consumed from a local or remote repository.
 * <p>
 *  All stubs are created from contracts placed inside the Feature Service (Producer) by the
 * Hello World Service (Consumer). These contracts specify what Feature Toggle is returned for Authorised
 * and Non Authorised users. On this example, the Hello World Service looks for the feature key
 * “hello-world-adduserresponse”, which will be enabled for authorised users but disabled for not authorised.
 * <p>
 * Finally, note that in this tests real HTTP requests are being fired against a locally deployed version
 * of Feature Service - see localhost:8090. As could be seen down below, the tests use a real version of
 * the FeatureClientFactory to talk with the Feature Service
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = TestConfig.class,
        properties = {
                "cisco-spark.server.importLegacyServerConfig=false",
                "hello-world.defaultGreetingPrefix=Doh!",
                "hello-world.message=" + GreetingServiceTest.message,
                "hello-world.trailer=" + GreetingServiceTest.trailer,
                "featureServicePublicUrl=" + "http://localhost:8090/"})
/*
 * If you want to run this test against local changes to the stubs you will need to
 *   - ensure the locally modified stubs are published to your local repo using mvn install
 *   - update the stub runner config: remove repositoryRoot and set workOffline=true
 *   - if you are using a non default location for local repo (i.e. have set localRepository in maven settings.xml) then
 *      pass the property  -Dmaven.repo.local=/path/to/local/repo  to the jvm when running the tests
 *   - optionally: you can change the '+' to refer to a specific version, e.g. com.cisco.wx2:feature-server:1.0-SNAPSHOT:stubs:8090
 * */
//@AutoConfigureStubRunner(workOffline = true, ids = "com.cisco.wx2:feature-server:+:stubs:8090") // Config for testing against stubs from local repository
@AutoConfigureStubRunner(repositoryRoot = "http://engci-maven.cisco.com/artifactory/webex-cca-group", workOffline = false, ids = "com.cisco.wx2:feature-server:+:stubs:8090")
public class GreetingServiceContractTest {

    static final String name = "hello-world";
    static final String message = "To alcohol! The cause of, and solution to, all of life's problems.";
    static final String trailer = " Proudly created by: ";
    private static final String JOE_RANDOM_TEST_USER = "Joe Random TestUser";
    private static final UUID JOE_RANDOM_TEST_UUID = UUID.fromString("b3474dff-8de5-4b12-8e2b-dfeee1cd11b2");

    private static final String BOB_RANDOM_UNAUTHORIZED_USER = "BOB Random TestUser";
    private static final UUID BOB_RANDOM_UNAUTHORIZED_TEST_UUID = UUID.fromString("2fdf0844-d1aa-4d4b-8699-8043b97ddac5");

    @MockBean
    private CiscoSparkServerProperties ciscoSparkServerProperties;

    @InjectMocks
    @Autowired
    private GreetingService greetingService;

    @Mock
    private AuthInfo authInfo;

    @Before
    public void init() {
        when(ciscoSparkServerProperties.getName()).thenReturn(name);
    }

    @Test
    public void testGetDefaultGreeting() throws Exception {
        Greeting expected = Greeting.builder().greeting("Doh! Homer Simpson").message(message).build();
        assertThat(greetingService.getGreeting("Homer Simpson", Optional.empty()))
                .isEqualTo(expected);
    }

    /**
     * GET that is done by an authorised user, which makes the Feature Service to return an enabled Feature Toggle and
     * tells the Hello World to include the trailer in the greeting message.
     * */
    @Test
    public void testGetDefaultWithTrailer() throws Exception {

        User user = Mockito.mock(User.class);
        when(user.getName()).thenReturn(JOE_RANDOM_TEST_USER);
        when(user.getId()).thenReturn(JOE_RANDOM_TEST_UUID);

        when(authInfo.getEffectiveUser()).thenReturn(user);
        when(authInfo.getAuthorization()).thenReturn("Basic dummy authorization string");

        Greeting expected = Greeting.builder().greeting("Doh! Homer Simpson").message(message + trailer + JOE_RANDOM_TEST_USER).build();
        assertThat(greetingService.getGreeting("Homer Simpson", Optional.of(authInfo)))
                .isEqualTo(expected);
    }

    /**
     * GET that is done by an non-authorised user, which makes the Feature Service to return an disabled Feature Toggle and
     * tells the Hello World to not include the trailer in the greeting message.
     * */
    @Test
    public void testGetDefaultWithTrailerFalseToggle() throws Exception {

        User user = Mockito.mock(User.class);
        when(user.getName()).thenReturn(BOB_RANDOM_UNAUTHORIZED_USER);
        when(user.getId()).thenReturn(BOB_RANDOM_UNAUTHORIZED_TEST_UUID);

        when(authInfo.getEffectiveUser()).thenReturn(user);
        when(authInfo.getAuthorization()).thenReturn("Basic dummy authorization string");

        Greeting expected = Greeting.builder().greeting("Doh! Homer Simpson").message(message).build();
        assertThat(greetingService.getGreeting("Homer Simpson", Optional.of(authInfo)))
                .isEqualTo(expected);
    }
}
