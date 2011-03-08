package org.ensime.test.util

import java.io.File
import scala.actors.Actor._
import scala.tools.nsc.interactive.{Global, CompilerControl}
import scala.tools.nsc.{Settings, FatalError}
import scala.tools.nsc.reporters.{StoreReporter}
import scala.tools.nsc.util.{SourceFile, BatchSourceFile, Position}
import scala.tools.nsc.reporters.{Reporter}
import org.ensime.server._
import org.ensime.config._
import org.scalatest.TestFailedException


object Helpers{

  def withPresCompiler(action:RichCompilerControl => Any ) =  {
    val settings = new Settings(Console.println)


    val propFile = new File("./project/build.properties")
    val props = JavaProperties.load(propFile)
    val v = props.get("build.scala.versions") match {
      case Some(s) => s
      case None => throw new IllegalStateException("Failed to load sbt build version.")
    }
    settings.processArguments(List(
	"-classpath", "project/boot/scala-" + v + "/lib/scala-library.jar",
	"-verbose"
      ), false)
    settings.usejavacp.value = false
    val reporter = new StoreReporter()
    val cc:RichCompilerControl = new RichPresentationCompiler(
      settings, reporter, actor{}, actor{}, ProjectConfig.nullConfig)
    action(cc)
    cc.askShutdown()
  }

  def srcFile(name:String, content:String) = new BatchSourceFile(name, content)
  def contents(lines:String *) = lines.mkString("\n")

  def expectFailure(msgLines:String *)(action:() => Unit){
    try{
      action()
      throw new IllegalStateException("Expected failure! Should not have succeeded!")
    }
    catch{
      case e:TestFailedException =>
      {
	System.err.println("\n***************************************")
	System.err.println("Expected Failure:")
	System.err.println(msgLines.mkString("\n"))
	System.err.println("***************************************\n")
      }
      case e => throw e
    }
  }

}
