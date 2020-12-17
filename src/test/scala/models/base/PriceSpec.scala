package models.base

import org.scalacheck.Gen._
import org.scalacheck.Gen
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class PriceSpec extends AnyFlatSpec with OptionValues with EitherValues with ScalaCheckDrivenPropertyChecks  {
    "Price" should "allow being created when positive double price provided" in {
        forAll(posNum[Double]) { v: Double =>
            Price.of(v.toString).get.price shouldBe v
        }
    }

    it should "allow being created when positive integer price provided" in {
        forAll(posNum[Int]) { v: Int =>
            Price.of(v.toString).get.price shouldBe v.toDouble
        }
    }

    it should "forbid being created with negative double price" in {
        forAll(negNum[Double]) { v: Double =>
            Price.of(v.toString) shouldBe None
        }
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

    it should "provide Price object as double" in {
        forAll(posNum[Double]) { v: Double =>
            Price.toDouble(Price.of(v.toString).get) shouldBe v
        }
    }

    it should "provide double as Price object" in {
        forAll(posNum[Double]) { v: Double =>
            Price.fromDouble(v) shouldBe Price.of(v.toString).get
        }
    }

}