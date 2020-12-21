package models.base

import org.scalacheck.Gen._
import org.scalacheck.Gen
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class PriceSpec extends AnyFlatSpec with OptionValues with EitherValues with ScalaCheckDrivenPropertyChecks  {
    "Price" should "allow being created when positive BigDecimal price provided" in {
        val list: List[BigDecimal] = List(1.56, 2.54, 123.54, 123.31)
        list.foreach(v => Price.of(v.toString).get.price shouldBe v)
    }

    it should "allow being created when positive integer price provided" in {
        forAll(posNum[Int]) { v: Int =>
            Price.of(v.toString).get.price shouldBe v.toDouble
        }
    }

    it should "forbid being created with negative Double price" in {
        val list: List[BigDecimal] = List(-1.56, -2.54, -123.54, -123.31)
        list.foreach(v => Price.of(v.toString) shouldBe None)
    }

    it should "forbid being created with negative integer provided" in {
        forAll(negNum[Int]) { v: Int =>
            Price.of(v.toString) shouldBe None
        }
    }

    it should "forbid being created when something besides digits provided" in {
        forAll(Gen.oneOf("123hg123","fff","1[]","1'''''3")) { v: String =>
            Price.of(v) shouldBe None
        }
    }

    it should "provide Price object as Double" in {
        val list: List[BigDecimal] = List(1.56, 2.54, 123.54, 123.31)
        list.foreach(v => Price.toDecimal(Price.of(v.toString).get) shouldBe v)
    }

    it should "provide Double as Price object" in {
        val list: List[BigDecimal] = List(1.56, 2.54, 123.54, 123.31)
        list.foreach(v => Price.fromDecimal(v) shouldBe Price.of(v.toString).get)
    }

}