package de.wayofquality.mill.docusaurus2

import mill._
import mill.define.Sources
import mill.modules.Jvm


trait Docusaurus2Module extends Module {

  def docusaurusSources : Sources
  def compiledMdocs : Sources

  def yarnInstall : T[PathRef] = T {
    val baseDir = T.dest

    docusaurusSources().foreach{ pr =>
      os.list(pr.path).foreach(p => os.copy.into(p, baseDir, followLinks = true, replaceExisting = true, copyAttributes = true, createFolders = true, mergeFolders = false))
    }

    val process = Jvm.spawnSubprocess(
      commandArgs = Seq(
        "yarn", "install", "--check-files"
      ),
      envArgs = Map.empty,
      workingDir = T.dest
    )
    process.join()
    T.log.info(new String(process.stdout.bytes))
    PathRef(T.dest)
  }

  def docusaurusBuild : T[PathRef] = T {
    val workDir = T.dest
    val yarnSetup = yarnInstall().path

    os.list(workDir).foreach(os.remove.all)
    os.list(yarnSetup).foreach { p =>
      os.copy.into(p, workDir, followLinks = true, replaceExisting = true, copyAttributes = true, createFolders = true, mergeFolders = false)
    }

    val docsDir = workDir / "docs"
    os.makeDir.all(docsDir)
    os.list(docsDir).foreach(os.remove.all)

    docusaurusSources().foreach { pr =>
      val bd = pr.path
      os.walk(pr.path / "docs").foreach { p =>
        val relPath = p.relativeTo(bd / "docs")
        T.log.info(relPath.toString())
        if (p.toIO.isFile) {
          os.copy.over(p, docsDir / relPath)
        }
      }
    }

    compiledMdocs().foreach{ pr =>
      os.list(pr.path).foreach(p => os.copy.into(p, docsDir, followLinks = true, replaceExisting = true, copyAttributes = true, createFolders = true, mergeFolders = true))
    }

    // For some reason we cant run yarn build otherwise
    val p1 = Jvm.spawnSubprocess(
      commandArgs = Seq(
        "yarn", "install", "--force"
      ),
      envArgs = Map.empty,
      workingDir = workDir
    )

    p1.join()

    val p2 = Jvm.spawnSubprocess(
      commandArgs = Seq(
        "yarn", "build"
      ),
      envArgs = Map.empty,
      workingDir = workDir
    )

    p2.join()

    PathRef(workDir)
  }
}
