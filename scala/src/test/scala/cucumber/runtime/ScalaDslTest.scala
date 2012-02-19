package cucumber.runtime

import org.junit.{Test, Assert}
import Assert._
import collection.JavaConverters._
import _root_.gherkin.I18n
import gherkin.formatter.model.Tag

class ScalaDslTest {

  @Test
  def emptyBefore {

    var called = false

    object Befores extends ScalaDsl with EN {
      Before {
        called = true
      }
    }

    assertEquals(1, Befores.beforeHooks.size)
    val hook = Befores.beforeHooks.head
    assertTrue(hook.matches(List[Tag]().asJava))
    hook.execute(null)
    assertTrue(called)
    assertEquals(Int.MaxValue, hook.getOrder)
  }

  @Test
  def taggedBefore {
    var called = false

    object Befores extends ScalaDsl with EN {
      Before("@foo,@bar", "@zap"){
        called = true
      }
    }

    assertEquals(1, Befores.beforeHooks.size)

    val hook = Befores.beforeHooks.head
    assertFalse(hook.matches(List[Tag]().asJava))
    assertTrue(hook.matches(List(new Tag("@bar", 0), new Tag("@zap", 0)).asJava))
    assertFalse(hook.matches(List(new Tag("@bar", 1)).asJava))

    hook.execute(null)
    assertTrue(called)
    assertEquals(Int.MaxValue, hook.getOrder)
  }

  @Test
  def orderedBefore {

    var called = false

    object Befores extends ScalaDsl with EN {
      Before(10){
        called = true
      }
    }

    val hook = Befores.beforeHooks(0)
    assertEquals(10, hook.getOrder)
  }

  @Test
  def taggedOrderedBefore {

    var called = false

    object Befores extends ScalaDsl with EN {
      Before(10, "@foo,@bar", "@zap"){
        called = true
      }
    }

    val hook = Befores.beforeHooks(0)
    assertEquals(10, hook.getOrder)
  }

  @Test
  def emptyAfter {

    var called = false

    object Afters extends ScalaDsl with EN {
      After {
        called = true
      }
    }

    assertEquals(1, Afters.afterHooks.size)
    val hook = Afters.afterHooks.head
    assertTrue(hook.matches(List[Tag]().asJava))
    hook.execute(null)
    assertTrue(called)
  }

  @Test
  def taggedAfter {
    var called = false

    object Afters extends ScalaDsl with EN {
      After("@foo,@bar", "@zap"){
        called = true
      }
    }

    assertEquals(1, Afters.afterHooks.size)

    val hook = Afters.afterHooks.head
    assertFalse(hook.matches(List[Tag]().asJava))
    assertTrue(hook.matches(List(new Tag("@bar", 0), new Tag("@zap", 0)).asJava))
    assertFalse(hook.matches(List(new Tag("@bar", 1)).asJava))

    hook.execute(null)
    assertTrue(called)
  }

  @Test
  def noArg {
    var called = false

    object Dummy extends ScalaDsl with EN {
      Given("x"){
        called = true
      }
    }

    assertEquals(1, Dummy.stepDefinitions.size)
    val step = Dummy.stepDefinitions.head
    assertEquals("ScalaDslTest.scala:126", step.getLocation) // be careful with formatting or this test will break
    assertEquals("x", step.getPattern)
    step.execute(new I18n("en"), Array())
    assertTrue(called)
  }

  @Test
  def args {
    var thenumber = 0
    var thecolour = ""

    object Dummy extends ScalaDsl with EN {
      Given("Oh boy, (\\d+) (\\s+) cukes"){ (num:Int, colour:String) =>
        thenumber = num
        thecolour = colour
      }
    }

    assertEquals(1, Dummy.stepDefinitions.size)
    val step = Dummy.stepDefinitions(0)
    step.execute(new I18n("en"), Array("5", "green"))
    assertEquals(5, thenumber)
    assertEquals("green", thecolour)
  }

  @Test
  def transformation {
    case class Person(name:String)

    var person:Person = null

    object Dummy extends ScalaDsl with EN {

      implicit val transformPerson = Transform(Person(_))

      Given("Person (\\s+)"){ p:Person =>
        person = p
      }
    }

    Dummy.stepDefinitions(0).execute(new I18n("en"), Array("Aslak"))
    assertEquals(Person("Aslak"), person)
  }
}
