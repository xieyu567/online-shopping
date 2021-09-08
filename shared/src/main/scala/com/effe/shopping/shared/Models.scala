package com.effe.shopping.shared

case class Product(name: String, code: String, description: String, price: Int)

abstract class CartKey {
    def user: String
    def productCode: String
}

case class ProductInCart(user: String, productCode: String) extends CartKey

case class Cart(user: String, productCode: String, quantity: Int) extends CartKey
