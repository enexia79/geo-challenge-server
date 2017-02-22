package geochallenge

import com.sun.net.httpserver.Authenticator.Success

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(AuthService)
class AuthServiceSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test isAuthorized and getAuthFailedObject"() {
		expect:
			service.isAuthorized("geo-ninjas") == true
			service.isAuthorized("ad52843f7b3d323fc50bfd62613b064e") == false
    }
}
