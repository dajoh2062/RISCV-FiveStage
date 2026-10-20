package FiveStage

import java.io.File
import org.scalatest.{FlatSpec, Matchers}

class BasicTests extends FlatSpec with Matchers {
  private val basicPrograms =
    fileUtils.getListOfFilesRecursive(
      new File(fileUtils.getTestDir, "basic").getPath
    ).filter(_.getName.endsWith(".s")).sortBy(_.getName)

  require(basicPrograms.nonEmpty, "No basic assembly tests found")

  basicPrograms.foreach { program =>
    it should s"execute ${program.getName} without NOP padding" in {
      TestRunner.run(Manifest.allTestOptions(program.getName).copy(
        nopPadded = false,
        printErrors = true
      )) should be(true)
    }
  }
}
