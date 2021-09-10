package com.effe.shopping.shared

object SharedMessages {
    def itWorks = "It works!"
}

sealed trait ActionOnCart

case object Add extends ActionOnCart

case object Remove extends ActionOnCart

sealed trait WebSocketMessage

case class CartEvent(user: String, product: Product, action: ActionOnCart) extends WebSocketMessage

case class Alarm(message: String, action: ActionOnCart)