package com.codacy.tools.scala.seed.traits

trait Haltable {

  def halt(code: Int = 0): Unit = {
    if (code == 0) {
      System.exit(0)
    } else {
      Runtime.getRuntime.halt(code)
    }
  }

}
