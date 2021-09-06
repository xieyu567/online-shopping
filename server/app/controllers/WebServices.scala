package controllers

import dao.{CartsDao, ProductDao}
import io.circe.syntax.EncoderOps
import io.circe.parser.decode
import io.circe.generic.auto._
import models.{Cart, ProductInCart, Product}

import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, Result}
import play.api.Logger
import play.api.libs.circe.Circe

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class WebServices @Inject()(cc: ControllerComponents, productDao: ProductDao, cartsDao: CartsDao) extends AbstractController(cc) with Circe {
    val logger: Logger = Logger(this.getClass)

    val recoverError: PartialFunction[Throwable, Result] = {
        case e: Throwable =>
            logger.error("Error while Writing in the database", e)
            InternalServerError("Cannot write in the database")
    }

    // Cart Controler //
    def listCartProducts(): Action[AnyContent] = Action.async { request =>
        val userOption = request.session.get("user")
        userOption match {
            case Some(user) =>
                logger.info(s"User '$user' is asking for the list of product in the cart")
                val futureInsert = cartsDao.all()
                futureInsert.map(products => Ok(products.asJson)).recover(recoverError)
            case None => Future.successful(Unauthorized)
        }
    }

    def deleteCartProduct(id: String): Action[AnyContent] = Action.async { request =>
        val userOption = request.session.get("user")
        userOption match {
            case Some(user) =>
                logger.info(s"User '$user' is asking to delete the product '$id' from the cart")
                val futureInsert = cartsDao.remove(ProductInCart(user, id))
                futureInsert.map(_ => Ok).recover(recoverError)
            case None => Future.successful(Unauthorized)
        }
    }

    def addCartProduct(id: String, quantity: String): Action[AnyContent] = Action.async { request =>
        val user = request.session.get("user")
        user match {
            case Some(user) =>
                val futureInsert = cartsDao.insert(Cart(user, id, quantity.toInt))
                futureInsert.map(_ => Ok).recover(recoverError)
            case None => Future.successful(Unauthorized)
        }
    }

    def updateCartProduct(id: String, quantity: String): Action[AnyContent] = Action.async { request =>
        val user = request.session.get("user")
        user match {
            case Some(user) =>
                logger.info(s"User '$user' is updating the product '$id' in it is cart with a quantity of '$quantity'")
                val futureInsert = cartsDao.update(Cart(user, id, quantity.toInt))
                futureInsert.map(_ => Ok).recover(recoverError)
            case None => Future.successful(Unauthorized)
        }
    }

    // Product Controler //
    def listProduct(): Action[AnyContent] = Action.async { _ =>
        val futureProducts = productDao.all()
        for (
            products <- futureProducts
        ) yield Ok(products.asJson)
    }

    def addProduct(): Action[AnyContent] = Action.async { request =>
        val productOrNot = decode[Product](request.body.asText.getOrElse(""))
        productOrNot match {
            case Right(product) =>
                val futureInsert = productDao.insert(product)
                futureInsert.map(_ => Ok).recover(recoverError)

            case Left(error) =>
                logger.error("Error while adding a product", error)
                Future.successful(BadRequest)
        }
    }

    // Login //
    def login(): Action[AnyContent] = Action {
        request =>
            request.body.asText match {
                case None => BadRequest
                case Some(user) => Ok.withSession("user" -> user)
            }
    }
}
