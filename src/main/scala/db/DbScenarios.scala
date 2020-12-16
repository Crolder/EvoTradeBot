package db

import models.Product.{Apartment, Car, Computer}
import cats.effect.{ExitCode, IO, IOApp}
import db.DbTransactor
import doobie.implicits._
import models.{Product, User}
import doobie.util.fragment.Fragment
import models.car.Manufacturer
import db.DbMappings._
import models.base.UserId
import models.user.PhoneNumber

object DbScenarios {
    val setupUsers = sql"CREATE TABLE USERS (id LONG NOT NULL, firstname VARCHAR, lastname VARCHAR, username VARCHAR, phoneNumber VARCHAR)"
    val setupCars = sql"CREATE TABLE CARS (id VARCHAR NOT NULL, userId LONG NOT NULL, price INT, manufacturer VARCHAR, year INT, mileage INT,description VARCHAR, imageKey VARCHAR)"
    val setupApartments = sql"CREATE TABLE APARTMENTS (id VARCHAR NOT NULL, userId LONG NOT NULL, price INT, district VARCHAR, area INT, roomAmount INT, floor INT,description VARCHAR, imageKey VARCHAR)"
    val setupComputers = sql"CREATE TABLE COMPUTERS (id VARCHAR NOT NULL, userId LONG NOT NULL, price INT, cpu VARCHAR, ram INT, videoCard VARCHAR, storage INT,description VARCHAR, imageKey VARCHAR)"

    val usersSelect = sql"SELECT * FROM USERS"

    val productsSelect = sql"SELECT * FROM CARS, APARTMENTS, COMPUTERS"

    val carsSelect = sql"SELECT * FROM CARS"

    val apartmentsSelect = sql"SELECT * FROM APARTMENTS"

    val computersSelect = sql"SELECT * FROM COMPUTERS"

    val imageSelect = sql"SELECT imageKey FROM IMAGES"

    def selectUserById(id: UserId) = sql"SELECT * FROM USERS WHERE id=$id"

    def selectProductsById(id: Long) = sql"SELECT * FROM CARS WHERE userId=$id"

    def selectCarById(id: Long) = sql"SELECT * FROM CARS WHERE userId=$id"

    def selectApartmentById(id: Long) = sql"SELECT * FROM APARTMENTS WHERE userId=$id"

    def selectComputerById(id: Long) = sql"SELECT * FROM COMPUTERS WHERE userId=$id"

    def selectImagesById(id: String) = sql"SELECT imageKey FROM IMAGES WHERE id=$id"

    def updatePhoneNumberById(id: UserId, phoneNumber: PhoneNumber) = sql"UPDATE USERS SET phoneNumber = $phoneNumber WHERE id=$id"

    def insertUser(user: User) = sql"INSERT INTO USERS VALUES (${user.id},${user.firstname},${user.lastname},${user.username}, ${user.phoneNumber})"

    def insertProduct(product: Product) = sql"INSERT INTO PRODUCTS VALUES (${product.id},${product.userId},${product.price})"

    def insertCar(car: Car) = sql"INSERT INTO CARS VALUES (${car.id},${car.userId},${car.price},${car.manufacturer},${car.year},${car.mileage},${car.descripion}, ${car.imageKey})"

    def insertApartment(apartment: Apartment) = sql"INSERT INTO APARTMENTS VALUES (${apartment.id},${apartment.userId},${apartment.price},${apartment.district},${apartment.area},${apartment.roomAmount},${apartment.floor},${apartment.descripion}, ${apartment.imageKey})"

    def insertComputer(computer: Computer) = sql"INSERT INTO COMPUTERS VALUES (${computer.id},${computer.userId},${computer.price},${computer.cpu},${computer.ram},${computer.videoCard},${computer.storage},${computer.descripion}, ${computer.imageKey})"

    def insertImage(productId: String, imageKey: String) = sql"INSERT INTO IMAGES VALUES ($productId, $imageKey)"

}
