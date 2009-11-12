this.metaClass.mixin(cuke4duke.GroovyDsl)

class CustomWorld {
  String customMethod() {
    "foo"
  }
}

World {
  new CustomWorld()
}

println("NUUUUU")
Before("AA")
Before("AA", "BB")
println("NUUUUUBIE")
Before(["AAPE"])

Before() {
  assert "foo" == customMethod()
  calc = new calc.Calculator()
}

Before("@notused") {
  throw new RuntimeException("Never happens")
}

Before("@notused,@important", "@alsonotused") {
  throw new RuntimeException("Never happens")
}

Given(~"I have entered (\\d+) into (.*) calculator") { int number, String ignore ->
  calc.push number
}

Given(~"(\\d+) into the") { ->
  throw new RuntimeException("should never get here since we're running with --guess")
}

When(~"I press (\\w+)") { String opname ->
  result = calc."$opname"()
}

Then(~"the stored result should be (.*)") { double expected -> 
  assert expected == result
}
