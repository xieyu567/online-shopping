package com.effe.shopping

import com.effe.shopping.shared.Product
import org.scalajs.dom.html.Div
import scalatags.JsDom.all._

case class ProductDiv(product: Product) {
    def content: Div = div(`class` := "col")(getProductDescription, getButton).render

    private def getProductDescription =
        div(
            p(product.name),
            p(product.description),
            p(product.price)
        )

    private def getButton = button(
        `type` := "button",
        onclick := addToCart
    )("Add to Cart")

    private def addToCart(): () => Any = () => UIManager.addOneProduct(product)
}
