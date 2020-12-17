package models.computer

import org.scalacheck.Gen._
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class StorageSpec extends AnyFlatSpec with OptionValues with EitherValues with ScalaCheckDrivenPropertyChecks  {
    "Storage" should "allow being created when positive integer provided" in {
        forAll(posNum[Int]) { v: Int =>
            Storage.of(v.toString).get.storage shouldBe v
        }
    }

    it should "forbid being created with positive double provided" in {
        forAll(posNum[Double]) { v: Double =>
            Storage.of(v.toString) shouldBe None
        }
    }

    it should "forbid being created with negative double provided" in {
        forAll(negNum[Double]) { v: Double =>
            Storage.of(v.toString) shouldBe None
        }
    }

    it should "forbid being created with negative integer provided" in {
        forAll(negNum[Int]) { v: Int =>
            Storage.of(v.toString) shouldBe None
        }
    }

    it should "forbid being created when something besides digits provided" in {
        forAll(alphaNumStr) { v: String =>
            Storage.of(v) shouldBe None
        }
    }

    it should "provide Storage object as integer" in {
        forAll(posNum[Int]) { v: Int =>
            Storage.toInt(Storage.of(v.toString).get) shouldBe v
        }
    }

    it should "provide integer as Storage object" in {
        forAll(posNum[Int]) { v: Int =>
            Storage.fromInt(v) shouldBe Storage.of(v.toString).get
        }
    }
}