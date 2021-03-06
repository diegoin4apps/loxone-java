package cz.smarteon.loxone.message

import spock.lang.Specification

import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals
import static net.javacrumbs.jsonunit.core.util.ResourceUtils.resource
import static spock.util.matcher.HamcrestSupport.that


class ApiInfoTest extends Specification implements SerializationSupport {

    def "should deserialize"() {
        when:
        ApiInfo apiInfo = readResource('message/apiInfo.json', ApiInfo)

        then:
        apiInfo.mac == '50:4F:94:10:B8:4A'
        apiInfo.version == '9.1.10.30'
    }

    def "should deserialize version 10.3"() {
        when:
        ApiInfo apiInfo = readResource('message/apiInfo103.json', ApiInfo)

        then:
        apiInfo.mac == 'EE:E0:00:D8:0B:0E'
        apiInfo.version == '10.3.11.25'
        apiInfo.eventSlots == true
    }

    def "should serialize"() {
        expect:
        that new ApiInfo('50:4F:94:10:B8:4A', '9.1.10.30'), jsonEquals(resource('message/apiInfo.json'))
    }
}
