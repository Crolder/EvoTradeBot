package models.computer

import org.scalacheck.Gen._
import org.scalacheck.Gen
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class RamSpec extends AnyFlatSpec with OptionValues with EitherValues with ScalaCheckDrivenPropertyChecks  {
    "Ram" should "allow being created when positive integer provided" in {
        forAll(posNum[Int]) { v: Int =>
            Ram.of(v.toString).get.ram shouldBe v
        }
    }

    it should "forbid being created with positive double provided" in {
        forAll(posNum[Double]) { v: Double =>
            Ram.of(v.toString) shouldBe None
        }
    }

    it should "forbid being created with negative double provided" in {
        forAll(negNum[Double]) { v: Double =>
            Ram.of(v.toString) shouldBe None
        }
    }

    it should "forbid being created with negative integer provided" in {
        forAll(negNum[Int]) { v: Int =>
            Ram.of(v.toString) shouldBe None
        }
    }

    it should "forbid being created when something besides digits provided" in {
        forAll(Gen.oneOf("123hg123","fff","1[]","1'''''3")) { v: String =>
            Ram.of(v) shouldBe None
        }
    }

    it should "provide Ram object as integer" in {
        forAll(posNum[Int]) { v: Int =>
            Ram.toInt(Ram.of(v.toString).get) shouldBe v
        }
    }

    it should "provide integer as Ram object" in {
        forAll(posNum[Int]) { v: Int =>
            Ram.fromInt(v) shouldBe Ram.of(v.toString).get
        }
    }
}