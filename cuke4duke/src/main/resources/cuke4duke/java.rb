require 'cucumber'
import 'cuke4duke.internal.StepDefinition'
import 'cuke4duke.Table'

module Cucumber
  class Ast::Table
    include ::Java::Cuke4duke::Table
  end

  module PureJava
    module StepDefinitionExtras
      def regexp
        Regexp.new(getRegexpString())
      end

      def invoke(world, args)
if Ast::Table === args[0]
  args = args[0] 
  puts "RUBY ARG: #{args.class} , #{args.class.ancestors}"
  return invokeSingleTarget(args)
end
        begin
          invokeOnTarget(args)
        rescue Exception => e
          java_exception_to_ruby_exception(e)
          raise(java_exception_to_ruby_exception(e))
        end
      end

      private

      def java_exception_to_ruby_exception(java_exception)
        bt = java_exception.backtrace
        Exception.cucumber_strip_backtrace!(bt, nil, nil)
        exception = JavaException.new(java_exception.message)
        exception.set_backtrace(bt)
        exception
      end
      
      class JavaException < Exception
      end
    end

    def step_mother=(step_mother)
      @__cucumber_java_step_mother = step_mother
    end

    def step_mother
      @__cucumber_java_step_mother
    end

    def register_class(clazz)
      @__cucumber_java_step_mother.registerClass(clazz)
    end

    # "Overridden" methods from cucumber's StepMother

    def new_world!
      @__cucumber_java_step_mother.newWorld
    end
    
    def step_definitions
      @__cucumber_java_step_mother.getStepDefinitions
    end
    
    def execute_before(scenario)
      @__cucumber_java_step_mother.executeBeforeHooks([scenario])
    end
  end
end
extend(Cucumber::PureJava)

class ::Java::Cuke4dukeInternal::StepDefinition
  include ::Cucumber::StepDefinitionMethods
  include ::Cucumber::PureJava::StepDefinitionExtras
end

Exception::CUCUMBER_FILTER_PATTERNS.unshift(/^org\/jruby|^cuke4duke\/java|^cuke4duke\/internal|^org\/junit|^java\/|^sun\/|^\$_dot_dot_/)
