Given(#/^I have some ({what}\w+) dudes:$/,
  what should == "fine"
  ; Either we have an 'table' implicit name for tables,
  ; Or we declare the step definition in such a way that we
  ; decide the table variable name ourself. Not sure how...
  firstDude = table raw get(1) get(0)
  firstDude asText should == "Ola"
)

Given(#/^I have another ({collection}\w+) of some fine dudes:$/, dudes,
  collection should == "set"
  secondDude = dudes raw get(2) get(0)
  secondDude asText should == "Sam"
)

Then(#/^they should win the lotto too$/,
  pending
)

Given(#/^I have a really long String:$/, solong,
  solong should == "OMG\nSo\nbig!"
)
