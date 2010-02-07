package cuke4duke

import internal.JRuby
import internal.language._
import internal.scala.{ScalaTransformations, ScalaStepDefinition}
import org.jruby.exceptions.RaiseException
import org.junit.{Test, Before => JunitBefore, Assert}
import Assert._

import _root_.scala.collection.mutable.{Map, ListBuffer}
import org.jruby.runtime.builtin.IRubyObject
import org.jruby.RubyArray
import java.lang.{Class, String}
import java.util.{Map => JMap, List => JList}

class ScalaDslTest extends ScalaDsl with Norwegian {

  var result = ""

  val step = Map[String, StepDefinition]()

  def regexp(step:ScalaStepDefinition) = step.regexp_source

  def rubyClassName(raise:RaiseException) = raise.getException.getType.getName

  def array(s:AnyRef*) = {
    val r = RubyArray.newArray(JRuby.getRuntime)
    s.foreach(r.add(_))
    r
  }

  val invokedStepdefinitions = new ListBuffer[String]
  val availableStepdefinitions = new ListBuffer[String]
  val calledFromStepdefintions = new ListBuffer[String]

  val programmingLanguage = new AbstractProgrammingLanguage(new LanguageMixin{
    override def invoked_step_definition(regexp_source: String, file_colon_line: String) = {
      invokedStepdefinitions += regexp_source
    }
    override def available_step_definition(regexp_source: String, file_colon_line: String) = {
      availableStepdefinitions += regexp_source
    }
    override def create_step_match(step_definition: StepDefinition, step_name: String, formatted_step_name: String, step_arguments: JList[StepArgument]) = null
    override def add_hook(phase: String, hook: Hook) = {}
    override def clear_hooks() = {}
  }){
    override def begin_scenario(scenario: IRubyObject) = {}
    override def end_scenario = {}
    override def load_code_file(file: String) = {}
    override def customTransform(arg: Object, parameterType: Class[_]) = {}
  }

  @JunitBefore
  def setUp{
    JRuby.getRuntime().evalScriptlet("""
    module Cucumber
      class Pending < StandardError; end
      class Undefined < StandardError; end
      class ArityMismatchError < StandardError; end 
    end
    """);

    val t = new ScalaTransformations
    t.addAll(transformations)

    for(s <- stepDefinitions){
      val sd = s(programmingLanguage, t)
      step(regexp(sd)) = sd
    }

    executionMode(new StepMother{
      def invoke(regex:String) = calledFromStepdefintions += regex
      def invoke(regex:String, table:Table){ step(regex).invoke(array(table)) }
      def invoke(regex:String, py:String){ step(regex).invoke(array(py)) }
    })
  }

  Before{
    result = "before hook 0"
  }

  @Test
  def test_before{
    val hook = beforeHooks(0)
    assertEquals(0, hook.tag_expressions.size)
    hook.invoke("", null)
    assertEquals("before hook 0", result)
  }


  Before("tag1", "tag2"){
    result = "tagged before"
  }

  @Test
  def test_taggedBefore{
    val hook = beforeHooks(1)
    assertEquals(2, hook.tag_expressions.size)
    assertEquals("tag1", hook.tag_expressions.apply(0))
    assertEquals("tag2", hook.tag_expressions.apply(1))
    hook.invoke("", null)
    assertEquals("tagged before", result)
  }

  After{
    result = "after hook 0"
  }

  @Test
  def test_after{
    val hook = afterHooks(0)
    assertEquals(0, hook.tag_expressions.size)
    hook.invoke("", null)
    assertEquals("after hook 0", result)
  }


  After("tag1", "tag2"){
    result = "tagged after"
  }

  @Test
  def test_taggedAfter{
    val hook = afterHooks(1)
    assertEquals(2, hook.tag_expressions.size)
    assertEquals("tag1", hook.tag_expressions.apply(0))
    assertEquals("tag2", hook.tag_expressions.apply(1))
    hook.invoke("", null)
    assertEquals("tagged after", result)
  }


  Given("call by name"){
    result = "given call-by-name"
  }

  @Test
  def test_CallByName{
    step("call by name").invoke(array())
    assertEquals("given call-by-name", result)
  }


  Given("given f1") { s: String =>
    result = "given f1 "+s
  }

  @Test
  def test_GivenF1{
    step("given f1").invoke(array("xxx"))
    assertEquals("given f1 xxx", result)
  }


  Given("pending comment"){
    pending("comment")
  }

  @Test
  def test_Pending{
    val comment = try{
      step("pending comment").invoke(array())
      fail("did not throw Cucumber::Pending")
    } catch {
      case e:RaiseException if rubyClassName(e) == "Cucumber::Pending" => e.getMessage
    }
    assertEquals("comment", comment)
  }

  When("pending no comment"){
    pending
  }

  @Test
  def test_PendingNoComment{
    val comment = try{
      step("pending no comment").invoke(array())
      fail("did not throw Cucumber::Pending")
    } catch {
      case e:RaiseException if rubyClassName(e) == "Cucumber::Pending" => e.getMessage
    }
    assertEquals("TODO", comment)
  }

  When("when f2 -> unit"){ (s: String, i: Int) =>
    result = "when f2 "+s + " " + i
  }

  @Test
  def test_WhenF2{
    step("when f2 -> unit").invoke(array("foo", "5"))
    assertEquals("when f2 foo 5", result)
  }

  Then("then f3 -> int"){ (s: String, i: Int, b: Boolean) =>
    result = "then f3 "+s+" "+i+" "+b
    5 // just to demonstrate that non-call-by-name can end in a non-unit expression
  }

  @Test
  def test_ThenF3{
    step("then f3 -> int").invoke(array("foo", "5", "true"))
    assertEquals("then f3 foo 5 true", result)
  }

  @Test
  def test_ArityMismatchException{
    try{
      step("then f3 -> int").invoke(array("foo", "5")) //missing the third argument
      fail("did not throw Cucumber::ArityMismatchError")
    } catch {
      case e:RaiseException if rubyClassName(e) == "Cucumber::ArityMismatchError" => e.getMessage
    }
  }

  Given("unknown type"){ a:(String,String) =>
    
  }

  @Test
  def test_Undefined{
    try{
      step("unknown type").invoke(array("foo"))
      fail("did not throw Cucumber::Undefined")
    } catch {
      case e:RaiseException if rubyClassName(e) == "Cucumber::Undefined" => e.getMessage
    }
  }

  class User(name:String){
    def friends_with(user:User):Boolean = name match {
      case "a" => true
      case "b" => false
    }
  }

  object User{
    def find_by_username(name:String):Option[User] = Some(new User(name))
  }

  //Transform[User](name => User.find_by_username(name))
  Transform(User.find_by_username)

  Then("""^(\w+) should be friends with (\w+)$"""){ (user:User, friend:User) =>
    assertTrue(user.friends_with(friend))
  }
  
  @Test
  def test_transform{
    val friends = step("""^(\w+) should be friends with (\w+)$""")
    friends.invoke(array("a", "b"))
    try{
      friends.invoke(array("b", "a"))
      fail("b should not be friends with a")
    } catch {
      case e:AssertionError =>
    }
  }

  Given("g file_colon_line"){}
  When("w file_colon_line"){ a:String => ()}
  Then("t file_colon_line"){ (a:User, b:String) => ()}

  @Test
  def test_file_colon_line{
    assertEquals("Given(\"g file_colon_line\"){ () => ... }", step("g file_colon_line").file_colon_line)
    assertEquals("When(\"w file_colon_line\"){ String => ... }", step("w file_colon_line").file_colon_line)
    assertEquals("Then(\"t file_colon_line\"){ (User,String) => ... }", step("t file_colon_line").file_colon_line)
  }

  Gitt("Gitt i18n"){}
  Når("Når i18n"){ a:String => ()}
  Så("Så i18n"){ (a:User, b:String) => ()}

  @Test
  def test_file_colon_line_i18n{
    assertEquals("Gitt(\"Gitt i18n\"){ () => ... }", step("Gitt i18n").file_colon_line)
    assertEquals("Når(\"Når i18n\"){ String => ... }", step("Når i18n").file_colon_line)
    assertEquals("Så(\"Så i18n\"){ (User,String) => ... }", step("Så i18n").file_colon_line)
  }

  Given("programmingLanguage.invoked_step_definition"){
    //whatever
  }

  @Test //issue#29
  def test_programmingLanguage_available_step_definition_is_called_when_creating_step_definition{
    assertTrue(availableStepdefinitions.contains("programmingLanguage.invoked_step_definition"))
  }

  @Test //issue#29
  def test_programmingLanguage_invoked_step_definition_is_invoked_when_invoking_step_definition{
    assertFalse(invokedStepdefinitions.contains("programmingLanguage.invoked_step_definition"))
    step("programmingLanguage.invoked_step_definition").invoke(array())
    assertTrue(invokedStepdefinitions.contains("programmingLanguage.invoked_step_definition"))
  }

  Given("step_can_call_other (.*)") { s:String =>
    Given("step_can_be "+s)
  }

  @Test //call_steps
  def test_steps_can_call_other_steps {
    step("step_can_call_other (.*)").invoke(array("x"))
    assertTrue(calledFromStepdefintions.contains("step_can_be x"))
  }

  Given("step_can_call_other_with_table_or_py"){
    Given("a table", DummyTable)
    Given("a pystring","""
      |a
      |multi
      |line
    """.stripMargin)
  }

  @Test
  def test_steps_can_call_other_steps_with_tables_and_py_strings{
    step("step_can_call_other_with_table_or_py").invoke(array())
    assertSame(DummyTable, table)
    assertEquals("""
    |a
    |multi
    |line
    """.stripMargin, pyString)
  }

  object DummyTable extends Table{
    def diffHashes(table: JList[JMap[String, String]], options: JMap[_, _]) = {}
    def diffHashes(table: JList[JMap[String, String]]) = {}
    def diffLists(table: JList[JList[String]], options: JMap[_, _]) = {}
    def diffLists(table: JList[JList[String]]) = {}
    def rows = null
    def raw = null
    def rowsHash = null
    def hashes = null
  }

  var table:Table = _

  Given("a table"){ t:Table =>
    table = t
  }

  @Test
  def test_tables_are_supported {
    step("a table").invoke(array(DummyTable))
    assertSame(DummyTable, table)
  }

  var pyString:String = _

  Given("a pystring"){ s:String =>
    pyString = s
  }

  @Test
  def test_pyStrings_are_supported {
    val expected = new PyString{
      def to_s = "foo"
    }
    step("a pystring").invoke(array(expected))
    assertEquals("foo", pyString)
  }

  //special case support for calling a no-arg step definition from a step-definition without the limitation described below
  Given("a"){
    Given("b")
  }

  //known limitation: call by name steps are defined 'f: => Unit' which gets converted to a 'f() => Unit'
  //if it was defined 'f: => Any' it would swallow the other variants (f0..f22 => _) as we capture the whole function
  //as a value
  //if your last expression in a call by name step has a non-unit returntype there are two ways to fix it
  //
  //Implementing it as 'Given("returnvalue"){ ()=> 5 }'
  //or explicitly returning unit 'Given("returnvalue"){ 5; () }
  //
  //for the reasons given, this will not compile
  //Given("call by name -> !unit"){
  //  5
  //}
}