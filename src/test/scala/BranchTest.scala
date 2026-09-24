package FiveStage

import org.scalatest.{FlatSpec, Matchers}

class BranchTest extends FlatSpec with Matchers {
  it should "execute all branch conditions with NOP padding" in {
    TestRunner.run(Manifest.singleTestOptions.copy(
      testName = "branches.s",
      nopPadded = true,
      printIfSuccessful = false,
      printMergedTrace = false
    )) should be(true)
  }
}
