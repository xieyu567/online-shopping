import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.ws.WSClient
import scala.concurrent.duration.DurationInt
import play.api.test.Helpers._

import scala.concurrent.Await
import org.scalatest._
import Matchers._

class ApplicationSpec extends PlaySpec with GuiceOneServerPerSuite {
  "Application" should {
    val wsClient = app.injector.instanceOf[WSClient]
    val myPublicAddress = s"localhost:$port"

    "send 404 on a bad request" in {
      val testURL = s"http://$myPublicAddress/boom"

      val response = Await.result(wsClient.url(testURL).get(), 1 seconds)
      response.status mustBe NOT_FOUND
    }

    "render the index page" in  {
      val testURL = s"http://$myPublicAddress/"

      val response = Await.result(wsClient.url(testURL).get(), 1 seconds)

      response.status mustBe OK
      response.contentType should include ("text/html")
      response.body should include ("shouts out")

    }
  }
}
