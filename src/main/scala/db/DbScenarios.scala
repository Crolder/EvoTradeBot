package db

import cats.effect.{ExitCode, IO, IOApp}
import db.DbTransactor
import doobie.implicits._
import adt.{User, Product}

object DbScenarios {
    val setupUsers = sql"CREATE TABLE USERS (id LONG NOT NULL, firstname VARCHAR, lastname VARCHAR, username VARCHAR)"
    val setupProducts = sql"CREATE TABLE PRODUCTS (id VARCHAR NOT NULL, userId LONG NOT NULL, name VARCHAR, price INT)"

    val usersSelect = sql"SELECT id, firstname, lastname, username FROM USERS"

    val productsSelect = sql"SELECT id, userId, name, price FROM PRODUCTS"

    def selectUserById(id: Long) = sql"SELECT id, firstname, lastname, username FROM USERS WHERE id=$id"

    def selectProductsById(id: Long) = sql"SELECT * FROM PRODUCTS WHERE userId=$id"

    def selectProductsBy(id: Long) = sql"SELECT * FROM PRODUCTS WHERE userId=$id"

    def insertUser(user: User) = sql"INSERT INTO USERS VALUES (${user.id},${user.firstname},${user.lastname},${user.username})"

    def insertProduct(product: Product) = sql"INSERT INTO PRODUCTS VALUES (${product.id},${product.userId},${product.name},${product.price})"
}
