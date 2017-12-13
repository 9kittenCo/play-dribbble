import org.junit.runner._
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import org.specs2.runner._
import play.api.libs.ws.WSClient
import play.api.test.Helpers._
import play.api.test._


@RunWith(classOf[JUnitRunner])
class IntegrationSpec extends PlaySpec with GuiceOneServerPerSuite with Injecting {

  "Routes" should {

    "send 404 on a bad request" in {
      route(app, FakeRequest(GET, "/boum")).map(status) mustBe Some(NOT_FOUND)
    }

    "send 400 on a request without login" in {
      route(app, FakeRequest(GET, "/top10")).map(status) mustBe Some(BAD_REQUEST)
    }
  }

  "Application" should {

    "test integration with dribbble" in {
      val wsClient = app.injector.instanceOf[WSClient]
      val myPublicAddress = s"localhost:$port"
      val username = "alagoon"
      val url = s"http://$myPublicAddress/top10?login=$username"
       val response = await(wsClient.url(url).get())

      response.status mustBe OK
    }
  }
}
