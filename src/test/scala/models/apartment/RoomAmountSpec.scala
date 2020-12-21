package models.apartment

import org.scalacheck.Gen
import org.scalacheck.Gen._
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class RoomAmountSpec extends AnyFlatSpec with OptionValues with EitherValues with ScalaCheckDrivenPropertyChecks  {
    "RoomAmount" should "allow being created when positive integer provided" in {
        forAll(posNum[Int]) { v: Int =>
            RoomAmount.of(v.toString).get.roomAmount shouldBe v
        }
    }

    it should "forbid being created with positive double provided" in {
        forAll(posNum[Double]) { v: Double =>
            RoomAmount.of(v.toString) shouldBe None
        }
    }

    it should "forbid being created with negative double provided" in {
        forAll(negNum[Double]) { v: Double =>
            RoomAmount.of(v.toString) shouldBe None
        }
    }

    it should "forbid being created with negative integer provided" in {
        forAll(negNum[Int]) { v: Int =>
            RoomAmount.of(v.toString) shouldBe None
        }
    }

    it should "forbid being created when something besides digits provided" in {
        forAll(Gen.oneOf("123hg123","fff","1[]","1'''''3")) { v: String =>
            RoomAmount.of(v) shouldBe None
        }
    }

    it should "provide RoomAmount object as integer" in {
        forAll(posNum[Int]) { v: Int =>
            RoomAmount.toInt(RoomAmount.of(v.toString).get) shouldBe v
        }
    }

    it should "provide integer as RoomAmount object" in {
        forAll(posNum[Int]) { v: Int =>
            RoomAmount.fromInt(v) shouldBe RoomAmount.of(v.toString).get
        }
    }
}