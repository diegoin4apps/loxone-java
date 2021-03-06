package cz.smarteon.loxone

import cz.smarteon.loxone.app.Control
import cz.smarteon.loxone.app.LoxoneApp
import spock.lang.Specification
import spock.lang.Subject

class LoxoneTest extends Specification {

    @Subject Loxone loxone

    LoxoneAuth auth
    LoxoneWebSocket webSocket
    LoxoneHttp http
    CommandResponseListener appCmdListener

    void setup() {
        http = Mock(LoxoneHttp)
        webSocket = Mock(LoxoneWebSocket) {
            registerListener(*_) >> { args -> appCmdListener = args[0] }
        }
        auth = Mock(LoxoneAuth)

        loxone = new Loxone(http, webSocket, auth)
    }

    def "should initialize"() {
        expect:
        loxone.auth() == auth
        loxone.webSocket() == webSocket
        loxone.http() == http
        appCmdListener != null
    }

    def "test basic flow"() {
        given:
        def app = Mock(LoxoneApp)
        def appListener = Mock(LoxoneAppListener)
        def control  = Stub(Control) {
            getUuid() >> new LoxoneUuid('1177b172-020b-0b06-ffffc0f606ef595c')
            isSecured() >> false
        }
        def secControl  = Stub(Control) {
            getUuid() >> new LoxoneUuid('1177b172-020b-0b06-ffffc0f606ef595c')
            isSecured() >> true
        }

        when:
        loxone.setEventsEnabled(true)
        loxone.start()

        then:
        loxone.isEventsEnabled()
        loxone.app() == app
        1 * webSocket.sendCommand(Command.LOX_APP) >> {
            appCmdListener.onCommand(Command.LOX_APP, app)
        }
        1 * webSocket.sendCommand(Command.ENABLE_STATUS_UPDATE)
        1 * webSocket.getWebSocketListener() >> null
        1 * webSocket.setWebSocketListener(*_)

        when:
        loxone.registerLoxoneAppListener(appListener)
        appCmdListener.onCommand(Command.LOX_APP, app)

        then:
        1 * appListener.onLoxoneApp(app)

        when:
        loxone.sendControlPulse(control)
        loxone.sendControlOn(control)
        loxone.sendControlOff(secControl)

        then:
        1 * webSocket.sendCommand({ cmd -> cmd.command ==~ /.*1177b172-020b-0b06-ffffc0f606ef595c\/Pulse/ })
        1 * webSocket.sendCommand({ cmd -> cmd.command ==~ /.*1177b172-020b-0b06-ffffc0f606ef595c\/On/ })
        1 * webSocket.sendSecureCommand({ cmd -> cmd.command ==~ /.*1177b172-020b-0b06-ffffc0f606ef595c\/Off/ })

        when:
        loxone.stop()

        then:
        1 * webSocket.close()
    }
}
