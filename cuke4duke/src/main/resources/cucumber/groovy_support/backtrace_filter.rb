require 'cucumber/java_support/backtrace_filter'
Cucumber::Ast::StepInvocation::BACKTRACE_FILTER_PATTERNS << /org\/codehaus\/groovy|groovy\/lang/