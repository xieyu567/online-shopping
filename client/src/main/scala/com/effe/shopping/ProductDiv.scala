package com.effe.shopping

import com.effe.shopping.shared.Product
import org.scalajs.dom.html.Div
import scalatags.JsDom.all.{div, p}

case class ProductDiv(product: Product) {
  def content: Div = div(`class` := "col")
  (getProductDescription, getButton).render

  private def getProductDescription =
    div(
      p(product.name),
      p(product.description),
      p(product.price)
    )

  
}
