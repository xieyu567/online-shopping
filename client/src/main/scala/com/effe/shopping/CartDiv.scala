package com.effe.shopping

import com.effe.shopping.shared.Product
import org.scalajs.dom.html.{Button, Div, Input, Label}
import scalatags.JsDom.all.{button, div, input, label}

case class CartDiv(lines: Set[CartLine]) {
    def content: Div = lines.foldLeft(div.render) { (a, b) =>
        a.appendChild(b.content)
        a
    }

    def addProduct(line: CartLine): CartDiv = {
        CartDiv(this.lines + line)
    }
}

case class CartLine(qty: Int, product: Product) {
    def content: Div = div(`class` := "row", id := s"cart-${product.code}-row")(
        div(`class` := "col-1")(getDeleteButton),
        div(`class` := "col-2")(getQuantityInput),
        div(`class` := "col-6")(getProductLabel),
        div(`class` := "col")(getPriceLabel)
    ).render

    private def getDeleteButton: Button = button(
        `type` := "button",
        onclick := removeFromCart)("X").render

    private def removeFromCart = ???

    private def getQuantityInput: Input = input(
        id := s"cart-${product.code}-qty",
        onchange := changeQty,
        value := qty.toString,
        `type` := "text",
        style := "width: 100%;"
    ).render

    private def changeQty = ???

    private def getProductLabel: Label = label(product.name).render

    private def getPriceLabel = label(product.price).render
}
