package dao

import models.{Cart, CartKey, Product, ProductInCart}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ProductDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                           (implicit executionContext: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {

    import profile.api._

    def all(): Future[Seq[Product]] = db.run(products.result)

    def insert(product: Product): Future[Unit] =
        db.run(products insertOrUpdate product).map(_ => ())

    private class ProductsTable(tag: Tag) extends Table[Product](tag, "products") {
        def name = column[String]("name")

        def code = column[String]("code")

        def description = column[String]("description")

        def price = column[Double]("price")

        override def * = (name, code, description, price) <> (Product.tupled, Product.unapply)
    }

    private val products = TableQuery[ProductsTable]

}

class CartsDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                        (implicit executionContext: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {

    import profile.api._

    def cart4(user: String): Future[Seq[Cart]] =
        db.run(carts.filter(_.user === user).result)

    def insert(cart: Cart): Future[_] = db.run(carts += cart)

    def remove(cart: ProductInCart): Future[Int] = db.run(carts.filter(c => matchKey(c, cart)).delete)

    def update(cart: Cart): Future[Int] = {
        val q = for {
            c <- carts if matchKey(c, cart)
        } yield c.quantity
        db.run(q.update(cart.quantity))
    }

    private def matchKey(c: CartsTable, cart: CartKey): Rep[Boolean] = {
        c.user === cart.user && c.productCode === cart.productCode
    }

    def all(): Future[Seq[Cart]] = db.run(carts.result)

    private class CartsTable(tag: Tag) extends Table[Cart](tag, "cart") {
        def user = column[String]("user")

        def productCode = column[String]("code")

        def quantity = column[Int]("qty")

        override def * = (user, productCode, quantity) <> (Cart.tupled, Cart.unapply)
    }

    private val carts = TableQuery[CartsTable]
}
