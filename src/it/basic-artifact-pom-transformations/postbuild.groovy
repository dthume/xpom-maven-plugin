import groovy.lang.Binding;

new AssertModuleResultsEqualExpected(new Binding([
    "basedir": basedir
])).run();