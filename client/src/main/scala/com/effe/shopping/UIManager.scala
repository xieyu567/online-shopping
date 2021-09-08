package com.effe.shopping

import io.circe.parser.decode
import io.circe.generic.auto._
import org.querki.jquery.{$, JQueryAjaxSettings, JQueryDeferred, JQueryXHR}
import org.scalajs.dom
import com.effe.shopping.shared.{Cart, Product}

import scala.scalajs.js.UndefOr
import scala.util.{Random, Try}

object UIManager {
    val origin: UndefOr[String] = dom.document.location.origin
    val cart: CartDiv = CartDiv(Set.empty[CartLine])
    //    val webSocket = getWebSocket
    val dummyUserName: String = s"user-${Random.nextInt(1000)}"

    def main(args: Array[String]): Unit = {
        val settings = JQueryAjaxSettings
            .url(s"$origin/v1/login")
            .data(dummyUserName)
            .contentType("text/plain")
        $.post(settings._result).done((_: String) => initUI(origin))
    }

    private def initUI(origin: UndefOr[String]): JQueryDeferred = {
        $.get(url = s"$origin/v1/products", dataType = "text").done(
            (answers: String) => {
                val products = decode[Seq[Product]](answers)
                products.right.map { seq =>
                    seq.foreach(p =>
                        $("#products").append(ProductDiv(p).content)
                    )
                    initCartUI(origin, seq)
                }
            }
        ).fail((xhr: JQueryXHR, textStatus: String, textError: String) =>
            println(s"call failed: $textStatus with status code: ${xhr.status} $textError"))
    }

    private def initCartUI(origin: UndefOr[String], products: Seq[Product]): JQueryDeferred = {
        $.get(url = s"$origin/v1/cart/products", dataType = "text").done((answers: String) => {
            val carts = decode[Seq[Cart]](answers)
            carts.right.map { cartLines =>
                cartLines.foreach { cartDao =>
                    val product = products.find(_.code == cartDao.productCode)
                    product match {
                        case Some(p) =>
                            val cartLine = CartLine(cartDao.quantity, p)
                            val cartContent = UIManager.cart.addProduct(cartLine).content
                            $("#cartPanel").append(cartContent)
                        case None => println(s"product code ${cartDao.productCode} doesn't exists in the catalog")
                    }
                }
            }
        }).fail((xhr: JQueryXHR, textStatus: String, textError: String) =>
            println(s"call failed: $textStatus with status code: ${xhr.status} $textError"))
    }

    def addOneProduct(product: Product): JQueryDeferred = {
        val quantity = 1

        def onDone: () => Unit = () => {
            val cartContent = cart.addProduct(CartLine(quantity, product)).content
            $("#cartPanel").append(cartContent)
            println(s"Product $product added in the cart")
        }

        postInCart(product.code, quantity, onDone)
    }

    private def postInCart(productCode: String, quantity: Int, onDone: () => Unit): JQueryDeferred = {
        val url = s"${UIManager.origin}/v1/cart/products/$productCode/quantity/$quantity"
        $.post(JQueryAjaxSettings.url(url)._result).done(onDone)
            .fail(() => println("cannot add a product twice"))
    }

    def deleteProduct(product: Product): JQueryDeferred = {
        def onDone: () => Unit = () => {
            val cartContent = $(s"#cart-${product.code}-row")
            cartContent.remove()
            println(s"Product ${product.code} removed from the cart")
        }

        deletefromCart(product.code, onDone)
    }

    private def deletefromCart(productCode: String, onDone: () => Unit): JQueryDeferred = {
        val url = s"${UIManager.origin}/v1/cart/products/$productCode"
        $.ajax(JQueryAjaxSettings.url(url).method("DELETE")._result).done(onDone)
    }

    def updateProduct(product: Product): JQueryDeferred = {
        putInCart(product.code, quantity(product.code))
    }

    private def quantity(productCode: String): Int = Try {
        val inputText = $(s"#cart-$productCode-qty")
        if (inputText.length != 0)
            Integer.parseInt(inputText.`val`().asInstanceOf[String])
        else 1
    }.getOrElse(1)

    private def putInCart(productCode: String, updatedQuantity: Int): JQueryDeferred = {
        val url = s"${UIManager.origin}/v1/cart/products/$productCode/quantity/$updatedQuantity"
        $.ajax(JQueryAjaxSettings.url(url).method("PUT")._result).done()
    }

}
