package models.apartment

import org.scalacheck.Gen
import org.scalacheck.Gen._
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class FloorSpec extends AnyFlatSpec with OptionValues with EitherValues with ScalaCheckDrivenPropertyChecks  {
    "Floor" should "allow being created when positive integer provided" in {
        forAll(posNum[Int]) { v: Int =>
            Floor.of(v.toString).get.floor shouldBe v
        }
    }

    it should "forbid being created with positive double provided" in {
        forAll(posNum[Double]) { v: Double =>
            Floor.of(v.toString) shouldBe None
        }
    }

    it should "forbid being created with negative double provided" in {
        forAll(negNum[Double]) { v: Double =>
            Floor.of(v.toString) shouldBe None
        }
    }

    it should "forbid being created with negative integer provided" in {
        forAll(negNum[Int]) { v: Int =>
            Floor.of(v.toString) shouldBe None
        }
    }

    it should "forbid being created when something besides digits provided" in {
        forAll(Gen.oneOf("1234.","123hg123","fff","1~","1'3")) { v: String =>
            Floor.of(v) shouldBe None
        }
    }

    it should "provide Floor object as integer" in {
        forAll(posNum[Int]) { v: Int =>
            Floor.toInt(Floor.of(v.toString).get) shouldBe v
        }
    }

    it should "provide integer as Floor object" in {
        forAll(posNum[Int]) { v: Int =>
            Floor.fromInt(v) shouldBe Floor.of(v.toString).get
        }
    }
}