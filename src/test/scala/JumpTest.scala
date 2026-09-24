package FiveStage

import org.scalatest.{FlatSpec, Matchers}

class JumpTest extends FlatSpec with Matchers {
  it should "execute jumps and preserve return addresses with NOP padding" in {
    TestRunner.run(Manifest.singleTestOptions.copy(
      testName = "jumps.s",
      nopPadded = true,
      printIfSuccessful = false,
      printMergedTrace = false
    )) should be(true)
  }
}
