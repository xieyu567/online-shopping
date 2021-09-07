import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import io.circe.generic.auto.exportDecoder
import io.circe.parser.decode
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.ws.{DefaultWSCookie, WSClient}
import models.Cart
import org.scalatest.time.{Millis, Seconds, Span}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

class ApiSpec extends PlaySpec with ScalaFutures with GuiceOneAppPerSuite {
    implicit val defaultPatience: PatienceConfig =
        PatienceConfig(timeout = Span(20, Seconds), interval = Span(100, Millis))

    val baseURL = s"localhost:9000/v1"
    val productsURL = s"http://$baseURL/products"
    val addProductsURL = s"http://$baseURL/products/add"
    val productsInCartURL = s"http://$baseURL/cart/products"
    val login = s"http://$baseURL/login"

    def deleteProductInCartURL(productID: String) =
        s"http://$baseURL/cart/products/$productID"

    def actionProductInCartURL(productID: String, quantity: Int) =
        s"http://$baseURL/cart/products/$productID/quantity/$quantity"

    "The API" should {
        val wsClient = app.injector.instanceOf[WSClient]

        lazy val defaultCookie = {
            val loginCookies = Await.result(wsClient.url(login).post("me").map(
                p => p.headers.get("Set-Cookie").map(_.head.split(";").head)), 1 seconds)
            val play_session = loginCookies.get.split("=").tail.mkString("")
            DefaultWSCookie("PLAY_SESSION", play_session)
        }

        "list all the product" in {
            val response = wsClient.url(productsURL).get().futureValue
            response.status mustBe 200
            response.body must include("PEPPER")
            response.body must include("NAO")
            response.body must include("BEOBOT")
        }

        "add a product" in {
            val newProduct =
                """
                  |{
                  |"name": "NewOne",
                  |"code": "New",
                  |"description": "The brand new product",
                  |"price": 100
                  |}
                  |""".stripMargin

            val posted = wsClient.url(addProductsURL).post(newProduct).futureValue
            posted.status mustBe 200
            val response = wsClient.url(productsURL).get().futureValue
            println(response.body)
            response.body must include("NewOne")
        }

        "add a product in the cart" in {
            val productID = "ALD1"
            val quantity = 1
            val posted = wsClient.url(actionProductInCartURL(productID, quantity))
                .addCookies(defaultCookie).post("").futureValue
            posted.status mustBe 200

            val response = wsClient.url(productsInCartURL)
                .addCookies(defaultCookie).get().futureValue
            response.status mustBe 200
        }

        "delete a product from the cart" in {
            val productID = "ALD1"
            val posted = wsClient.url(deleteProductInCartURL(productID))
                .addCookies(defaultCookie).delete().futureValue
            posted.status mustBe 200

            val response = wsClient.url(productsInCartURL)
                .addCookies(defaultCookie).get().futureValue
            println(response)
            response.status mustBe 200
            response.body mustNot include("ALD1")
        }

        "update a product quantity in the cart" in {
            val productID = "ALD1"
            val quantity = 1
            val posted = wsClient.url(actionProductInCartURL(productID, quantity))
                .addCookies(defaultCookie).post("").futureValue
            posted.status mustBe 200

            val newQuantity = 99
            val update = wsClient.url(actionProductInCartURL(productID, newQuantity))
                .addCookies(defaultCookie).put("").futureValue
            update.status mustBe 200

            val response = wsClient.url(productsInCartURL)
                .addCookies(defaultCookie).get().futureValue
            println(response)
            response.status mustBe 200
            response.body must include(productID)
            response.body must include(newQuantity.toString)
        }

        "return a cookie when a user logins" in {
            val cookieFuture = wsClient.url(login).post("myID").map {
                response =>
                    response.headers.get("Set-Cookie").map(
                        header => header.head.split(";")
                            .filter(_.startsWith("PLAY_SESSION")).head
                    )
            }

            val play_session_key = cookieFuture.futureValue.get.split("=").head
            play_session_key must equal("PLAY_SESSION")
        }
    }
}
