package com.effe.shopping

import com.effe.shopping.shared.Product
import org.scalajs.dom.html.{Button, Div}
import scalatags.JsDom
import scalatags.JsDom.all._

case class ProductDiv(product: Product) {
    def content: Div = div(`class` := "col")(getProductDescription, getButton).render

    private def getProductDescription: JsDom.TypedTag[Div] =
        div(
            p(product.name),
            p(product.description),
            p(product.price)
        )

    private def getButton: JsDom.TypedTag[Button] = button(
        `type` := "button",
        onclick := addToCart
    )("Add to Cart")

    private def addToCart(): () => Any = () => UIManager.addOneProduct(product)
}
